package com.zaleslaw.osmzombies;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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

import static com.zaleslaw.osmzombies.R.drawable.church;
import static com.zaleslaw.osmzombies.R.drawable.zombie;

public class ZombieMapFragment extends Fragment {

    public static final String ME = "Me";
    private MapView mapView;
    private MapController mapController;

    private DefaultResourceProxyImpl mResourceProxy;
    private ItemizedIconOverlay<OverlayItem> mMyLocationOverlay;
    private ZombieDao dao = new ZombieDao();
    private LocationManager lm;


    public ZombieMapFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_zombie_map, container, false);
        if (this.isAdded()) {
            mResourceProxy = new DefaultResourceProxyImpl(getActivity().getApplicationContext());
            mapView = (MapView) rootView.findViewById(R.id.mapview);
            mapView.setBuiltInZoomControls(true);
            mapView.setMultiTouchControls(true);

            mapController = (MapController) this.mapView.getController();
            mapController.setZoom(20);

            Location location = setLastKnownLocation();
            GeoPoint center = new GeoPoint(location.getLatitude(), location.getLongitude());
            mapController.setCenter(center);


            ArrayList<OverlayItem> items = getZombieOverlayItems(location);


            OverlayItem item = new OverlayItem(ME, ME, center);
            item.setMarker(getResources().getDrawable(church));
            items.add(item);

            this.mMyLocationOverlay = new ItemizedIconOverlay<OverlayItem>(items,
                    new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                        @Override
                        /*
                           It removes tapped marker
                         */
                        public boolean onItemSingleTapUp(final int index,
                                                         final OverlayItem item) {
                            removeMarker(item);
                            return true;
                        }

                        @Override
                        public boolean onItemLongPress(final int index,
                                                       final OverlayItem item) {

                            return false;
                        }
                    }, mResourceProxy);


            mapView.getOverlays().add(this.mMyLocationOverlay);
            mapView.invalidate();
        } else {

        }


        return rootView;
    }

    // It removes marker if it is not 'me' marker
    private void removeMarker(OverlayItem item) {
        if (!item.getTitle().equals(ME)) {
            mMyLocationOverlay.removeItem(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        lm.removeUpdates(myLocationListener);
    }

    private ArrayList<OverlayItem> getZombieOverlayItems(Location location) {

        final Drawable drawable = getResources().getDrawable(zombie);
        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        List<Zombie> zombies = dao.getZombies(location);
        for (Zombie z : zombies) {
            OverlayItem item = new OverlayItem(z.getId(), z.getName(), new GeoPoint(z.getLat(), z.getLon()));
            item.setMarker(drawable);
            items.add(item);
        }
        return items;
    }

    private LocationListener myLocationListener
            = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
            updateLoc(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }
    };

    private void updateLoc(Location loc) {
        GeoPoint locGeoPoint = new GeoPoint(loc.getLatitude(), loc.getLongitude());

        removeMarkerById(ME);
        addMyNewLocationMarker(loc);
        mapController.setCenter(locGeoPoint);
        Log.d("Update location", loc.toString());
        mapView.invalidate();
    }

    private void addMyNewLocationMarker(Location loc) {
        OverlayItem item = new OverlayItem(ME, ME, new GeoPoint(loc.getLatitude(), loc.getLongitude()));
        item.setMarker(getResources().getDrawable(church));
        mMyLocationOverlay.addItem(item);
    }

    private void removeMarkerById(String id) {

        for (int i = 0; i < mMyLocationOverlay.size(); i++) {
            OverlayItem item = mMyLocationOverlay.getItem(i);
            if (item == null) {
                continue;
            } else {
                Log.d("Zombie maps", item + " " + item.getTitle() + " " + item.getTitle());
                if (item.getTitle().equals(id)) { // TODO: title can be null
                    //unsafe removing if you delete more than one time
                    mMyLocationOverlay.removeItem(item);
                }
            }

        }

    }

    private Location setLastKnownLocation() {
        lm = (LocationManager) getActivity().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        final Location location = LocationUtils.getLastKnownLocation(lm);

        if (location != null) {

            Log.d("Zombie maps", location.toString());
            return location;
        }
        return null;
    }
}