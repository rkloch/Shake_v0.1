package com.example.shakev3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {


    private static final int SHAKE_THRESHOLD = 800;
    long lastUpdate = System.currentTimeMillis();
    float[] gravity = new float[3];
    float[] geomagnetic;
    float azimuth;
    float pitch;
    float roll;
    boolean switchIsOff = true;

    float last_x;
    float last_y;
    float last_z;
    SensorManager sensorManager;
    boolean onLightFrag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager fm = getSupportFragmentManager();


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        Sensor sensorMagnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, sensorMagnetic, SensorManager.SENSOR_DELAY_UI);

        Sensor sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener((SensorEventListener) this, sensorAccelerometer, SensorManager.SENSOR_DELAY_UI);

        Sensor sensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener((SensorEventListener) this, sensorLight, SensorManager.SENSOR_DELAY_UI);


        Button pictureButton = findViewById(R.id.button);

        pictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLightFrag = false;
                fm.beginTransaction()
                        .replace(R.id.fragmentContainerView2, PictureFragment.class, null) // gets the first animations
                        .commit();
            }
        });
        Button lightButton = findViewById(R.id.button2);
        lightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLightFrag = true;
                fm.beginTransaction()
                        .replace(R.id.fragmentContainerView2, LightFragment.class, null) // gets the first animations
                        .commit();
            }
        });

        Switch lockSwitch = findViewById(R.id.switch1);

        lockSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchIsOff = !switchIsOff;
            }
        });


    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(switchIsOff){
        if (event.sensor.getType() == Sensor.TYPE_LIGHT && onLightFrag) {
            TextView tempView = findViewById(R.id.textLight);
            tempView.setText(getString(R.string.light, String.format("%.6g%n", event.values[0])));
            Log.d("temperature", String.valueOf(event.values[0]));
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && !onLightFrag) {
            gravity = event.values;
            TextView xView = findViewById(R.id.textView);
            xView.setText(getString(R.string.xAxis, String.format("%.2g%n", gravity[0])));
            TextView yView = findViewById(R.id.textView2);
            yView.setText(getString(R.string.yAxis, String.format("%.2g%n", gravity[1])));
            TextView zView = findViewById(R.id.textView3);
            zView.setText(getString(R.string.zAxis, String.format("%.2g%n", gravity[2])));

            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((curTime - lastUpdate) > 400) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    Log.d("sensor", "X: " + String.format("%.2g%n", gravity[0]) + " Y: " + String.format("%.2g%n", gravity[1]) + " Z: " + String.format("%.2g%n", gravity[2]));
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values;
        }
        if (gravity != null && geomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);

            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = orientation[0]; // orientation contains: azimuth, pitch and roll
                pitch = orientation[1];
                roll = orientation[2];
            }
        }
        if (!onLightFrag) {
            ImageView imageView = findViewById(R.id.imageView);
            imageView.setRotationX((float) Math.toDegrees(-pitch));
            imageView.setRotationY((float) Math.toDegrees(-roll));
        }
    }}


    @Override
    public void onAccuracyChanged(Sensor sensorEvent, int i) {

    }

    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}