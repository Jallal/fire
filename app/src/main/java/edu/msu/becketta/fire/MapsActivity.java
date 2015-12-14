package edu.msu.becketta.fire;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager locationManager;

    double myLat = 0.0;
    double myLon = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        setUpMapIfNeeded();
    }

    @Override
    protected void onPause() {
        unregisterListeners();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerListeners();
        setUpMapIfNeeded();
    }

    private void unregisterListeners() {
        try {
            locationManager.removeUpdates(activeListener);
        } catch (SecurityException ex) {
            // Fail silently
        }
    }

    private void registerListeners() {
        unregisterListeners();

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(true);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(false);

        String bestPossible= locationManager.getBestProvider(criteria, false);
        String bestAvailable= locationManager.getBestProvider(criteria, true);

        try {
            if (bestAvailable != null) {
                // We have an available provider
                locationManager.requestLocationUpdates(bestAvailable, 1000, 1, activeListener);
            } else {
                Toast.makeText(MapsActivity.this,
                        R.string.no_loc_provider,
                        Toast.LENGTH_LONG).show();
            }
            if (bestPossible != null && !bestPossible.equals(bestAvailable)) {
                // There is a better provider possible, but not currently available
                // Install a listener to see when it becomes available
                locationManager.requestLocationUpdates(bestPossible,
                        1000, 1, bestPossibleListener);
            }
        } catch (SecurityException ex) {
            // Fail silently... whatever
        }
    }

    private LocationListener activeListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            onLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {
            registerListeners();
        }
    };

    private LocationListener bestPossibleListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {
            registerListeners();
        }

        @Override
        public void onProviderDisabled(String provider) {}
    };

    private void onLocation(Location location) {
        myLat = location.getLatitude();
        myLon = location.getLongitude();
    }

    private GoogleMap.OnMapClickListener mapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            float[] results = new float[1];
            Location.distanceBetween(myLat, myLon, latLng.latitude, latLng.longitude, results);
            if (results[0] < 100) {
                mMap.addMarker(new MarkerOptions()
                        .position(latLng).title("Fire")
                        .icon(BitmapDescriptorFactory.fromAsset("fire.png")));
            } else {
                Toast.makeText(MapsActivity.this,
                        R.string.too_far_to_report,
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(mapClickListener);
    }
}
