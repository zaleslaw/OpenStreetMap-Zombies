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

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.LocationUtils;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.zaleslaw.osmzombies.R.drawable.center;
import static com.zaleslaw.osmzombies.R.drawable.zombie;

public class ZombieMapFragment extends Fragment {

    public static final String ME = "Me";
    private MapView mapView;
    private MapController mapController;

    private DefaultResourceProxyImpl mResourceProxy;
    private ItemizedIconOverlay<OverlayItem> mMyLocationOverlay;
    private ZombieDao dao = new ZombieDao();
    private LocationManager lm;
    private Location gamerLocation;

    private MainGameThread loopThread;
    private List<Zombie> zombies = new ArrayList<>();

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

        // Start game loop

    }

    @Override
    public void onPause() {
        super.onPause();

        //
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

            GeoPoint centerPoint = new GeoPoint(gamerLocation.getLatitude(), gamerLocation.getLongitude());// TODO : NullPointer if location is disabled
            mapController.setCenter(centerPoint);

            // Load initial zombie state
            zombies = dao.getZombies(gamerLocation);
            ArrayList<OverlayItem> items = getZombieOverlayItems(zombies);

            mMyLocationOverlay = new ItemizedIconOverlay<OverlayItem>(items,
                    new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                        @Override
                    /*
                       It removes tapped marker
                     */
                        public boolean onItemSingleTapUp(final int index,
                                                         final OverlayItem item) {
                            // TODO: simple record increasing. Should to change on call special game logic method
                            loopThread.setRecord(loopThread.getRecord() + 1);
                            removeMarker(item);
                            return true;
                        }

                        @Override
                        public boolean onItemLongPress(final int index,
                                                       final OverlayItem item) {

                            return false;
                        }
                    }, mResourceProxy);

            OverlayItem gamerItem = new OverlayItem(ME, ME, centerPoint);
            gamerItem.setMarker(getResources().getDrawable(center));

            mMyLocationOverlay.addItem(gamerItem);

            mapView.getOverlays().add(mMyLocationOverlay);
            mapView.invalidate();
            isMapInitialized = true;
        }


    }

    // It removes marker if it is not 'me' marker
    private void removeMarker(OverlayItem item) {
        if (!item.getTitle().equals(ME)) {
            mMyLocationOverlay.removeItem(item);
        }
    }

    private ArrayList<OverlayItem> getZombieOverlayItems(List<Zombie> zombies) {

        final Drawable drawable = getResources().getDrawable(zombie);
        ArrayList<OverlayItem> items = new ArrayList<>();
        for (Zombie z : zombies) {
            OverlayItem item = new OverlayItem(z.getId(), z.getName(), new GeoPoint(z.getLat(), z.getLon()));
            item.setMarker(drawable);
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

    private void updateLoc(Location loc) {
        gamerLocation = loc;
        Log.d("ZM", "Update location " + loc.toString());
    }

    private void addMyNewLocationMarker(Location loc) {
        if (gamerLocation != null) {
            OverlayItem item = new OverlayItem(ME, ME, new GeoPoint(loc.getLatitude(), loc.getLongitude()));
            item.setMarker(getResources().getDrawable(center));
            mMyLocationOverlay.addItem(item);
        }
    }

    private void removeMarkerById(String id) {
        for (int i = 0; i < mMyLocationOverlay.size(); i++) {
            OverlayItem item = mMyLocationOverlay.getItem(i);
            if (item == null) {
                continue;
            } else {
                //Log.d("ZM", item + " " + item.getTitle() + " " + item.getTitle());
                if (item.getTitle().equals(id)) { // TODO: title can be null
                    //unsafe removing if you delete more than one time
                    mMyLocationOverlay.removeItem(item);
                }
            }
        }

    }

    private Location getLastKnownLocation() {
        final Location location = LocationUtils.getLastKnownLocation(lm);
        if (location != null) {
            //Log.d("ZM", location.toString());
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
        Location lastGamerPosition = gamerLocation;
        if (lastGamerPosition != null) {
            double zombieLat = lastGamerPosition.getLatitude() * (1 - new Random().nextDouble() / 1000);
            double zombieLon = lastGamerPosition.getLongitude() * (1 - new Random().nextDouble() / 1000);
            for (Zombie z : zombies) {
                z.setLat(zombieLat);
                z.setLon(zombieLon);
            }
        }
    }

    private void updateGamerLocation() {
        if (gamerLocation != null) {
            GeoPoint locGeoPoint = new GeoPoint(gamerLocation.getLatitude(), gamerLocation.getLongitude());
            removeMarkerById(ME);
            addMyNewLocationMarker(gamerLocation);
            mapController.setCenter(locGeoPoint);
        }

    }

    public void displayGameState() {
        if (gamerLocation != null) {
            updateGamerLocation();
            displayZombieLocations();
        }

    }

    private void displayZombieLocations() {
        // TODO : simple clearing. Should to filter items and keep part of them
        mMyLocationOverlay.removeAllItems();
        mMyLocationOverlay.addItems(getZombieOverlayItems(zombies));
        addMyNewLocationMarker(gamerLocation);
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
        mapController.setZoom(18);
    }

    public synchronized boolean isLBSrequested() {
        return isLBSrequested;
    }

    public synchronized boolean isMapInitialized() {
        return isMapInitialized;
    }
}