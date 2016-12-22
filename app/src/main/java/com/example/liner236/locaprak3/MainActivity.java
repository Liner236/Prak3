package com.example.liner236.locaprak3;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener,LocationListener, com.google.android.gms.location.LocationListener,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double latitude;
    private double longitude;

    private boolean isRunning = false;
    private int periodicTime = 1000; // Standart 1 Sekunde
    private int meter;
    private int speed;

    //Gyroskop
    private SensorManager sensorManager;
    private Sensor gyro;
    private double x,y,z;

    private CheckBox cb_periodic;
    private CheckBox cb_meter;
    private CheckBox cb_speed;
    private CheckBox cb_movement;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner spin_periodic = (Spinner) findViewById(R.id.spin_peridoic);
        spin_periodic.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                periodicTime = Integer.parseInt(parent.getItemAtPosition(position).toString())  * 1000;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        Spinner spin_meter = (Spinner) findViewById(R.id.spin_meter);
        spin_meter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                meter = Integer.parseInt(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        cb_periodic = (CheckBox)findViewById(R.id.cb_periodic);
        cb_meter = (CheckBox)findViewById(R.id.cb_meter);
        cb_speed = (CheckBox)findViewById(R.id.cb_speed);
        cb_movement = (CheckBox)findViewById(R.id.cb_movement);

        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this,gyro,SensorManager.SENSOR_DELAY_NORMAL);



        List<String> secList = new ArrayList<String>();
        secList.add("1");
        secList.add("2");
        secList.add("5");
        secList.add("10");
        secList.add("20");
        secList.add("30");
        secList.add("60");

        List<String> meterList = new ArrayList<String>();
        meterList.add("5");
        meterList.add("10");
        meterList.add("20");
        meterList.add("50");
        meterList.add("75");
        meterList.add("100");


        //Spinner Style
        ArrayAdapter<String> dataAdapterSec = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, secList);
        dataAdapterSec.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> dataAdapterMeter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, meterList);
        dataAdapterMeter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spin_periodic.setAdapter(dataAdapterSec);
        spin_meter.setAdapter(dataAdapterMeter);


        Button btn_start_tracking = (Button)findViewById(R.id.btn_start_tracking);
        btn_start_tracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRunning = true;
                cb_movement.setEnabled(false);
                cb_meter.setEnabled(false);
                cb_speed.setEnabled(false);
                cb_periodic.setEnabled(false);

                startGoogleLocationUpdates();

            }
        });

        Button btn_stop = (Button)findViewById(R.id.btn_stop);
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRunning = false;
                cb_movement.setEnabled(true);
                cb_meter.setEnabled(true);
                cb_speed.setEnabled(true);
                cb_periodic.setEnabled(true);

            }
        });

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (isRunning == true){
            if(cb_periodic.isChecked()){
                trackGpsValues(location);
            }
            else if (cb_meter.isChecked()){

            }
            else if(cb_speed.isChecked()){
                if (location.getSpeed() == speed){
                    trackGpsValues(location);
                }
            }
            else if(cb_movement.isChecked()){
                if(x >= 0.5 || y >= 0.5 || z >= 0.5 || x <= -0.5 || y <= -0.5 || z <= -0.5){
                    trackGpsValues(location);
                    System.out.println("Latitude: " + getLatitude() + "\tLongitude: " + getLongitude());
                }
            }


        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void trackGpsValues(Location location) {

        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

        } else {
            Toast.makeText(this, "location = NULL", Toast.LENGTH_LONG).show();
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationRequest.setInterval(periodicTime);
    }

    protected void startGoogleLocationUpdates(){
        createLocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
        //locationGPS = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isRunning){
            if (cb_movement.isChecked()){
                if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
                    x = event.values[0];
                    y = event.values[1];
                    z = event.values[2];
                    System.out.println("X: " + x + "\tY: " + y + "\tZ: " + z);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
