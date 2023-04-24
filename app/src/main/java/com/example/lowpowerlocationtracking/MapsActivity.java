package com.example.lowpowerlocationtracking;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.lowpowerlocationtracking.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, View.OnClickListener, SensorEventListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private Button currentLocationBtn, plusSignBtn, minusSignBtn;
    float acclX = 0, acclY = 0, acclZ = 0;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    float acceleration = 0;
    //float velocity = 0;
    float displacement = 0;
    float timeStep = 0.01f; // time step in seconds
    long lastUpdate = System.currentTimeMillis();

    final float alpha = 0.8f;
    float[] gravity = {0, 0, 0};

    // Initialize the displacement variables
    float distanceTraveled = 0;
    float[] velocity = {0, 0, 0};
    float[] position = {0, 0, 0};
    long lastTime = System.currentTimeMillis();

    LatLng latLng;
    int zoom = 16;
    private static final int PERMISSIONS_REQUEST_LOCATION = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        currentLocationBtn = findViewById(R.id.getLocBtn);
        plusSignBtn = findViewById(R.id.plusSign);
        minusSignBtn = findViewById(R.id.minusSign);

        currentLocationBtn.setOnClickListener(this);
        plusSignBtn.setOnClickListener(this);
        minusSignBtn.setOnClickListener(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);
       // latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener((SensorEventListener) this, accelerometerSensor);
        locationManager.removeUpdates(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        latLng = new LatLng(39.261, -76.699);
        mMap.addMarker(new MarkerOptions().position(latLng).title("Random Point"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        mMap.setOnCamaeraChangeListener(new GoogleMap.OnCameraChangeListener() {
//            @Override
//            public void onCameraChange(@NonNull CameraPosition cameraPosition) {
//                if (cameraPosition.zoom != zoom) {
//                    zoom = (int) cameraPosition.zoom;
//                    // do you action here
//                }
//            }
//        });
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.getLocBtn) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Request the permission if it hasn't been granted
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
                return;
            }
            // If permission is already granted, request location updates
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, this);
            if(latLng != null){
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            }
            sensorManager.registerListener((SensorEventListener) MapsActivity.this,accelerometerSensor,SensorManager.SENSOR_DELAY_NORMAL);
        } else if(v.getId() == R.id.plusSign){
            if(latLng != null){
                zoom = zoom + 3;
                setZoom();
            }
        } else if (v.getId() == R.id.minusSign) {
            zoom = zoom - 3;
            setZoom();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setZoom() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
//        latLng = new LatLng(location.getLatitude(), location.getLongitude());
//        gravity = new float[]{0, 0, 0};
//
//        // Initialize the displacement variables
//        distanceTraveled = 0;
//         velocity = new float[]{0, 0, 0};
//         position = new float[]{0, 0, 0};
//        mMap.clear();
//        mMap.addMarker(new MarkerOptions().position(latLng).title("New Location"));
//        setZoom();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
//            long timeElapsed = System.currentTimeMillis() - lastUpdate;
//            lastUpdate = System.currentTimeMillis();
//
//            acclX = event.values[0];
//            acclY = event.values[1];
//            acclZ = event.values[2];
//            acceleration = (float) Math.sqrt(acclX * acclX + acclY * acclY + acclZ * acclZ);
//            velocity = (float) ((acceleration-9.8) * timeElapsed / 1000.0f);
//
//            // integrate velocity to get displacement
//            displacement  =  displacement +  velocity * timeElapsed / 1000.0f;
//            System.out.println("acceleration" + acceleration + " velocity "+velocity +" displacement "+ displacement +" timeElapsed "+ timeElapsed);
//               // LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//                float[] results = new float[1];
//                Location.distanceBetween(latLng.latitude, latLng.longitude,
//                        latLng.latitude + displacement, latLng.longitude, results);
//                float distance = results[0];
//                LatLng newLatLng = SphericalUtil.computeOffset(latLng, distance, 0);
//                mMap.addPolyline(new PolylineOptions().add(latLng, newLatLng).width(5).color(Color.RED));
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 17));

            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
            float[] linearAcceleration = {
                    event.values[0] - gravity[0],
                    event.values[1] - gravity[1],
                    event.values[2] - gravity[2]
            };

            // Calculate the time elapsed since the last sensor reading
            long currentTime = System.currentTimeMillis();
            float dt = (currentTime - lastTime) / 1000.0f;
            lastTime = currentTime;

            // Integrate the linear acceleration to estimate the velocity and displacement
            for (int i = 0; i < 3; i++) {
                velocity[i] += linearAcceleration[i] * dt;
                position[i] += velocity[i] * dt + 0.5f * linearAcceleration[i] * dt * dt;
            }

            // Update the distance traveled variable
            distanceTraveled = (float) Math.sqrt(position[0] * position[0] + position[1] * position[1] + position[2] * position[2]);
            distanceTraveled = distanceTraveled/10000000;
            System.out.println("distance Travelled"+ distanceTraveled);
                            float[] results = new float[1];
            Location.distanceBetween(latLng.latitude, latLng.longitude,
                        latLng.latitude + distanceTraveled, latLng.longitude, results);
                float distance = results[0];
                LatLng newLatLng = SphericalUtil.computeOffset(latLng, distance, 0);
                System.out.println("lat" +newLatLng.latitude );
               // mMap.clear();
                mMap.addMarker(new MarkerOptions().position(newLatLng).title("Random Point"));
                mMap.addPolyline(new PolylineOptions().add(latLng, newLatLng).width(5).color(Color.RED));
                //latLng = newLatLng;
                setZoom();
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}