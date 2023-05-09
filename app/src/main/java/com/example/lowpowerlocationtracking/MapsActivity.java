package com.example.lowpowerlocationtracking;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.pm.PackageManager;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, View.OnClickListener, SensorEventListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private Button currentLocationBtn, plusSignBtn, minusSignBtn;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    final float alpha = 0.01f;

    // Initialize the displacement variables
    float[] sensorPosition = {0, 0, 0};
    float sensorDistance = 0;
    float[] sensorGravity = {0, 0, 0};
    float[] sensorVelocity = {0, 0, 0};
    float differTime;

    long lastTime = System.currentTimeMillis();
    long oldTime = 0;
    long presentTime;

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
        mMap.addMarker(new MarkerOptions().position(latLng).title("Inital Point"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        Toast.makeText(getApplicationContext(), "click current location again", Toast.LENGTH_LONG).show();
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
            if (latLng != null) {
                setZoom();
            }
            sensorManager.registerListener((SensorEventListener) MapsActivity.this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else if (v.getId() == R.id.plusSign) {
            if (latLng != null) {
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
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 6000, 0,this);
//        sensorManager.registerListener((SensorEventListener) MapsActivity.this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }



    @Override
    public void onLocationChanged(@NonNull Location location) {
        mMap.clear();
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latLng).title("GPS Location"));
        sensorGravity = new float[]{0, 0, 0};
         sensorPosition = new float[]{0, 0, 0};
       sensorDistance = 0;
        sensorGravity = new float[]{0, 0, 0};
        sensorVelocity = new float[]{0, 0, 0};

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){

//            sensorGravity[0] = alpha * sensorGravity[0] + (1 - alpha) * event.values[0];
//            sensorGravity[1] = alpha * sensorGravity[1] + (1 - alpha) * event.values[1];
//            sensorGravity[2] = alpha * sensorGravity[2] + (1 - alpha) * event.values[2];

            float[] filteredAcceleration = {
//                    event.values[0] - sensorGravity[0],
//                    event.values[1] - sensorGravity[1],
//                    event.values[2] - sensorGravity[2]
                    event.values[0] ,
                    (float) (event.values[1] - 9.8),
                    event.values[2]
            };

            // Calculate the time elapsed since the last sensor reading
            presentTime = System.currentTimeMillis();
            differTime = (presentTime - lastTime) / 1000.0f;
            lastTime = presentTime;

            // Integrate the linear acceleration to estimate the velocity and displacement
            for (int i = 0; i < 3; i++) {
                sensorVelocity[i] += filteredAcceleration[i] * differTime;
                sensorPosition[i] += sensorVelocity[i] * differTime + 1/2 * filteredAcceleration[i] * Math.pow(differTime,2);
            }

            // Update the distance traveled variable
            sensorDistance = (float) Math.sqrt(Math.pow(sensorPosition[0],2) + Math.pow(sensorPosition[1],2) + Math.pow(sensorPosition[2],2));
            sensorDistance = sensorDistance /1000;

                if(oldTime ==0){
                    oldTime = presentTime;
                }
               //if(presentTime - oldTime > 500) {
                   oldTime =presentTime;
            float[] results = new float[1];
            Location.distanceBetween(latLng.latitude, latLng.longitude,
                    latLng.latitude + sensorDistance, latLng.longitude+sensorDistance, results);
            float distance = results[0];
                   LatLng newLatLng = SphericalUtil.computeOffset(latLng, distance, 0);
                   System.out.println("lat" + newLatLng.latitude);
                   System.out.println("distance Travelled"+ sensorDistance);
                   mMap.clear();
                   mMap.addMarker(new MarkerOptions().position(newLatLng));
                  // setZoom();
             //  }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}