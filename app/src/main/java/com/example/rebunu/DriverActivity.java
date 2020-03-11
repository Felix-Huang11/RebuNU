package com.example.rebunu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;
import org.imperiumlabs.geofirestore.listeners.GeoQueryDataEventListener;
import org.imperiumlabs.geofirestore.listeners.GeoQueryEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DriverActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Reference: https://www.zoftino.com/android-mapview-tutorial posted on November 14, 2017
    private MapView mapView;
    private GoogleMap gmap;
    private static final int TAG_CODE_PERMISSION_LOCATION = 1;
    private String floatingButtonStatus = "VISIBLE";
    private LocationManager locationManager;
    private Criteria criteria;

    public void updateMap(ArrayList<Map<String, Object>> locations) {
        if (!(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, TAG_CODE_PERMISSION_LOCATION);
        }

        Location currentLocation = locationManager.getLastKnownLocation(Objects.requireNonNull(locationManager.getBestProvider(criteria, false)));
        float[] distance = new float[1];

        for(Map<String, Object> l: locations) {
            try {
                assert currentLocation != null;
                ArrayList<GeoPoint> pos = (ArrayList<GeoPoint>)l.get("pos");
                Integer price = ((Long) l.get("price")).intValue();
                String riderId = (String) l.get("riderId");
                Map<String, Object> tag = new HashMap<>();
                tag.put("price", price);
                tag.put("pos", pos);
                tag.put("riderId", riderId);
                assert pos != null;
                Location.distanceBetween(pos.get(0).getLatitude(), pos.get(0).getLongitude(), currentLocation.getLatitude(), currentLocation.getLongitude(), distance);
                Marker marker = gmap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.available_request_icon_small))
                        .position(new LatLng(pos.get(0).getLatitude(), pos.get(0).getLongitude()))
                        .title("Distance: " + String.valueOf(Double.valueOf(distance[0]).intValue()) + " Meters. " +
                                "Geolocation: (" + pos.get(0).getLatitude() + ", " + pos.get(0).getLongitude()+ ")"));
                marker.setTag(tag);
            } catch (Exception ignored){
               String error =  ignored.toString();
            };
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        Database db = new Database();
        String TAG = "RebuNu";

        Button button_searchNearby_floating;

        button_searchNearby_floating = findViewById(R.id.driverSearchRequest_button_searchNearby_floating);
        mapView = findViewById(R.id.postRequest_mapView);

        button_searchNearby_floating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gmap.clear();
                if (!(ContextCompat.checkSelfPermission(DriverActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(DriverActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                                PackageManager.PERMISSION_GRANTED)) {
                    ActivityCompat.requestPermissions(DriverActivity.this, new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, TAG_CODE_PERMISSION_LOCATION);
                }
                assert locationManager != null;
                Location currentLocation = locationManager.getLastKnownLocation(Objects.requireNonNull(locationManager.getBestProvider(criteria, false)));
                assert currentLocation != null;
                double lat = currentLocation.getLatitude();
                double lng = currentLocation.getLongitude();
                gmap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));

                Database db = new Database();

                GeoQuery geoQuery = db.geoFirestore.queryAtLocation(new GeoPoint(lat, lng), 5.0);
                geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
                    @Override
                    public void onDocumentEntered(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {
                        Map<String, Object> dataMap = documentSnapshot.getData();
                        ArrayList<Map<String, Object>> dataMapList = new ArrayList<>();
                        dataMapList.add(dataMap);
                        updateMap(dataMapList);
                    }
                    @Override
                    public void onDocumentExited(DocumentSnapshot documentSnapshot) {}

                    @Override
                    public void onDocumentMoved(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {}

                    @Override
                    public void onDocumentChanged(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {}
                    @Override
                    public void onGeoQueryReady() {}

                    @Override
                    public void onGeoQueryError(Exception e) {}
                });
            }
        });


        // Reference: https://developer.android.com/training/permissions/requesting.html Posted on 2019-12-27.
        if (!(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, TAG_CODE_PERMISSION_LOCATION);
        }
        mapView.onCreate(null);
        mapView.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;
        gmap.setMinZoomPreference(16);
        gmap.setMyLocationEnabled(true);
        UiSettings uiSettings = gmap.getUiSettings();
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setZoomControlsEnabled(true);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();

        if (!(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, TAG_CODE_PERMISSION_LOCATION);
        }
        // get current location
        // Reference: https://stackoverflow.com/questions/36878087/get-current-location-lat-long-in-android-google-map-when-app-start Posted on Apr 27 '16 at 21:04 by Dijkstra
        assert locationManager != null;
        Location currentLocation = locationManager.getLastKnownLocation(Objects.requireNonNull(locationManager.getBestProvider(criteria, false)));
        assert currentLocation != null;
        double lat = currentLocation.getLatitude();
        double lng = currentLocation.getLongitude();
        LatLng cur = new LatLng(lat, lng);
        gmap.moveCamera(CameraUpdateFactory.newLatLng(cur));
    }
}


