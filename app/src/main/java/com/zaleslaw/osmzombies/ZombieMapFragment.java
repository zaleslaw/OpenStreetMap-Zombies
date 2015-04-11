package com.zaleslaw.osmzombies;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.LocationUtils;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

import static com.zaleslaw.osmzombies.R.drawable.center;
import static com.zaleslaw.osmzombies.R.drawable.zombie;

public class ZombieMapFragment extends Fragment {


    private MapView mapView;
    private MapController mapController;

    private DefaultResourceProxyImpl mResourceProxy;
    private ItemizedIconOverlay<OverlayItem> mMyLocationOverlay;

    private LocationManager lm;
    private Location gamerLocation;

    private MainGameThread loopThread;
    private ZombieService service = new ZombieService();
    private boolean isMapInitialized;
    private boolean isLBSrequested;


    public ZombieMapFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_zombie_map, container, false);

        lm = (LocationManager) getActivity().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        mResourceProxy = new DefaultResourceProxyImpl(getActivity().getApplicationContext());
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);


        prepareMap(rootView);
        startGame();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        lm.removeUpdates(myLocationListener);
        stopGameLoop();
    }

    private void stopGameLoop() {
        // Stop game loop
        boolean retry = true;
        if (loopThread != null) {
            loopThread.setRunning(false);
            while (retry) {
                try {
                    loopThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                }
            }
        }
    }


    public void startGame() {
        if (this.isAdded()) {
            loopThread = new MainGameThread(this);
            loopThread.setRunning(true);
            loopThread.start();
        } else {
            Log.e("ZM", "Fragment isn't added");
        }
    }

    public void initializeMap() {

        gamerLocation = getLastKnownLocation();

        if (gamerLocation != null) {

            GeoPoint centerPoint = new GeoPoint(gamerLocation.getLatitude(), gamerLocation.getLongitude());
            mapController.setCenter(centerPoint);

            // Load initial zombie state
            List<Zombie> zombies = service.getFirstGeneration(gamerLocation);
            ArrayList<OverlayItem> items = getZombieOverlayItems(zombies);

            mMyLocationOverlay = new ItemizedIconOverlay<OverlayItem>(items,
                    new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                        @Override
                    /*
                       It removes tapped marker
                     */
                        public boolean onItemSingleTapUp(final int index,
                                                         final OverlayItem item) {
                            service.kickZombie(item.getUid());
                            return true;
                        }

                        @Override
                        public boolean onItemLongPress(final int index,
                                                       final OverlayItem item) {
                            service.killZombie(item.getUid());
                            return true;
                        }
                    }, mResourceProxy);


            Survivor survivor = service.createSurvivor(10, gamerLocation.getLatitude(), gamerLocation.getLongitude());
            mMyLocationOverlay.addItem(getSurvivorOverlayItem(survivor));

            mapView.getOverlays().add(mMyLocationOverlay);
            mapView.invalidate();
            isMapInitialized = true;
        }


    }

    private OverlayItem getSurvivorOverlayItem(Survivor s) {
        OverlayItem gamerItem = new OverlayItem(s.getId(), s.getName(), s.getDescription(), new GeoPoint(s.getLat(), s.getLon()));
        gamerItem.setMarker(getResources().getDrawable(center));
        return gamerItem;
    }

    private ArrayList<OverlayItem> getZombieOverlayItems(List<Zombie> zombies) {
        Drawable zombiePic = getResources().getDrawable(zombie);
        ArrayList<OverlayItem> items = new ArrayList<>();
        for (Zombie z : zombies) {
            OverlayItem item = new OverlayItem(z.getId(), z.getName(), new GeoPoint(z.getLat(), z.getLon()));
            item.setMarker(zombiePic);
            items.add(item);
        }
        return items;
    }

    private LocationListener myLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            updateLoc(location);

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }
    };

    private void updateLoc(Location location) {
        gamerLocation = location;
        Log.d("ZM", "Update location " + location.toString());
    }

    private GeoPoint addMyNewLocationMarker() {
        final Survivor s = service.getSurvivor();
        OverlayItem item = getSurvivorOverlayItem(s);
        mMyLocationOverlay.addItem(item);

        return new GeoPoint(s.getLat(), s.getLon());

    }

    private Location getLastKnownLocation() {
        final Location location = LocationUtils.getLastKnownLocation(lm);
        if (location != null) {
            return location;
        }
        return null;
    }

    private boolean isLocationServicesEnabled() {
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return false;
        } else {
            return true;
        }
    }

    /*
    * Simple implementation
    * */
    public void updateGameState() {
        service.updateSurvivorPosition(gamerLocation);
        service.filterAliveZombies();
        service.updateZombieLocations();
        service.generateNewZombies();
        boolean endGame = service.verifyEndGameCondition();
        if (endGame) {
            loopThread.setRunning(false);
            Toast.makeText(getActivity(), "You are death!", Toast.LENGTH_SHORT).show();
        }
    }


    public void displayGameState() {
        if (gamerLocation != null) {
            displaySurvivorLocation();
            displayZombieLocations();
        }
    }

    private void displayZombieLocations() {
        mMyLocationOverlay.removeAllItems();
        mMyLocationOverlay.addItems(getZombieOverlayItems(service.getCurrentGeneration()));
    }

    private void displaySurvivorLocation() {
        IGeoPoint survivorGeoPoint = addMyNewLocationMarker();
        mapController.setCenter(survivorGeoPoint);
    }

    public void requestLocationServicesEnabling() {
        if (!isLocationServicesEnabled()) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
        isLBSrequested = true;
    }

    private void prepareMap(View rootView) {
        mapView = (MapView) rootView.findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapController = (MapController) this.mapView.getController();
        mapController.setZoom(17);
    }

    public synchronized boolean isLBSrequested() {
        return isLBSrequested;
    }

    public synchronized boolean isMapInitialized() {
        return isMapInitialized;
    }
}