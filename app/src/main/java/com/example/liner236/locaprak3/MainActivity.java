package com.example.liner236.locaprak3;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.StrictMode;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.vision.text.TextBlock;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
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
    private int trackingCounter = 0;
    //Locas für Distance
    private Location loca1 = null;
    private Location loca2 = null;

    //Gyroskop
    private SensorManager sensorManager;
    private Sensor gyro;
    private double x,y,z;
    //Checkboxen
    private CheckBox cb_periodic;
    private CheckBox cb_meter;
    private CheckBox cb_speed;
    private CheckBox cb_movement;

    private TextView tv_speed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Damit die APP wegen den Rechten nicht direkt crasht
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

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

        Spinner spin_speed = (Spinner) findViewById(R.id.spin_speed);
        spin_speed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                speed = Integer.parseInt(parent.getItemAtPosition(position).toString());
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

        loca1 = new Location("loca1");
        loca2 = new Location("loca2");



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

        List<String> speedList = new ArrayList<String>();
        speedList.add("1");
        speedList.add("2");
        speedList.add("3");
        speedList.add("5");
        speedList.add("10");
        speedList.add("15");


        //Spinner Style
        ArrayAdapter<String> dataAdapterSec = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, secList);
        dataAdapterSec.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> dataAdapterMeter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, meterList);
        dataAdapterMeter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> dataAdapterSpeed = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, speedList);
        dataAdapterSpeed.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spin_periodic.setAdapter(dataAdapterSec);
        spin_meter.setAdapter(dataAdapterMeter);
        spin_speed.setAdapter(dataAdapterSpeed);


        tv_speed = (TextView)findViewById(R.id.tv_speed);



        Button btn_start_tracking = (Button)findViewById(R.id.btn_start_tracking);
        btn_start_tracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRunning = true;
                cb_movement.setEnabled(false);
                cb_meter.setEnabled(false);
                cb_speed.setEnabled(false);
                cb_periodic.setEnabled(false);

                // Festlegen der First Location fürs Tracking in Metern
                loca1.setLatitude(latitude);
                loca1.setLongitude(longitude);

                startGoogleLocationUpdates();
                System.out.println("FUCK U !! ");

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
                trackingCounter = 0;

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
            tv_speed.setText(location.getSpeed() +" km/h");
            if(cb_periodic.isChecked()){
                trackGpsValues(location);
                trackingCounter ++;
                System.out.println("TRACKED !! " + longitude);
                try {
                    senden();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (cb_meter.isChecked()){
                trackGpsValues(location);
                loca2.setLatitude(latitude);
                loca2.setLongitude(longitude);
                trackingCounter ++;
                if (loca1.distanceTo(loca2) >= meter){
                    try {
                        senden();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    loca1.setLatitude(latitude);
                    loca1.setLongitude(longitude);
                }
            }
            else if(cb_speed.isChecked()){
                trackingCounter ++;
                if (location.getSpeed() >= speed){
                    trackGpsValues(location);

                    try {
                        senden();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            else if(cb_movement.isChecked()){
                if(x >= 0.5 || y >= 0.5 || z >= 0.5 || x <= -0.5 || y <= -0.5 || z <= -0.5){
                    trackGpsValues(location);
                    trackingCounter ++;
                    try {
                        senden();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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

    public void senden() throws IOException {
        String sendename;
        sendename = "Huren";
        if (cb_periodic.isChecked()){
            sendename = "Periodisch";
        }
        else if (cb_meter.isChecked()){
            sendename = "Meter";
        }
        else if (cb_speed.isChecked()){
            sendename = "Speed";
        }
        else {
            sendename = "Movement";
        }

        // Stuff für die Verbindung
        URL url = new URL("http://mapps.sittekonline.de/index.php");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        // Schreibe Daten im KML auf Server
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write("werte="+"<?xml version=\"1.0\" encoding=\"utf-8\"?>"+"\n"+"<kml xmlns=\"http://www.opengis.net/kml/2.2>"+"\n"+"<Document>"+"\n"+"<Placemark> "+
                "\n" +"<name>"+sendename+"</name>"+"\n"+"<Point>"+"\n"+"<coordinates>"+latitude+","+longitude+
                "</coordinates>"+"\n"+"</Point>"+"\n"+"</Placemark>"+"\n"+"<GPSFix>"+trackingCounter+"</GPSFix>"+"</Document>"+"\n"+"</kml>"+"          "+sendename);
        writer.close();

        // Dateicheck zum Server
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            System.out.println("SENDEN ERFOLGREICH");
            Toast.makeText(this,"Senden erfolgreich",Toast.LENGTH_SHORT).show();
            connection.disconnect();
        }
        else {
            System.out.println("SENDEN FEHLGESCHLAGEN");
            Toast.makeText(this,"Senden fehlgeschlagen",Toast.LENGTH_SHORT).show();
            connection.disconnect();
        }
        trackingCounter = 0;

    }
}
