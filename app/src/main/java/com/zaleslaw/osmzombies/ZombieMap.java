package com.zaleslaw.osmzombies;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

import static com.zaleslaw.osmzombies.R.drawable.zombie;


public class ZombieMap extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zombie_map);


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment(this))
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_zombie_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    @SuppressLint("ValidFragment")
    public static class PlaceholderFragment extends Fragment {


        private final ZombieMap activity;
        private MapView mapView;
        private MapController mapController;

        private DefaultResourceProxyImpl mResourceProxy;
        private ItemizedIconOverlay<OverlayItem> mMyLocationOverlay;

        @SuppressLint("ValidFragment")
        public PlaceholderFragment(ZombieMap zombieMap) {
            this.activity = zombieMap;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {


            View rootView = inflater.inflate(R.layout.fragment_zombie_map, container, false);
            mResourceProxy = new DefaultResourceProxyImpl(this.activity.getApplicationContext());
            mapView = (MapView) rootView.findViewById(R.id.mapview);
            mapView.setBuiltInZoomControls(true);
            mapView.setMultiTouchControls(true);

            mapController = (MapController) this.mapView.getController();
            mapController.setZoom(12);
            this.setLastKnownLocation();


            OverlayItem item = new OverlayItem("Zombie1", "Old Frank", new GeoPoint(59.9431889, 30.3292));
            item.setMarker(getResources().getDrawable(zombie));

            ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
            items.add(item);


            this.mMyLocationOverlay = new ItemizedIconOverlay<OverlayItem>(items,
                    new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                        @Override
                        public boolean onItemSingleTapUp(final int index,
                                                         final OverlayItem item) {
                            mapController.setCenter(new GeoPoint(59.93983, 30.314559));
                            return true;
                        }

                        @Override
                        public boolean onItemLongPress(final int index,
                                                       final OverlayItem item) {

                            return false;
                        }
                    }, mResourceProxy);
            this.mapView.getOverlays().add(this.mMyLocationOverlay);
            mapView.invalidate();


            return rootView;
        }

        private void setLastKnownLocation() {
            final LocationManager lm = (LocationManager) this.activity.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            final Location location = LocationUtils.getLastKnownLocation(lm);
            if (location != null) {
                mapController.setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));
                Log.d("Zombie maps", location.toString());
            }
        }
    }
}
