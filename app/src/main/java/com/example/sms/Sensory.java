package com.example.sms;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Sensory extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private TextView mAzimuthTextView;
    private ListView mSensorsListView;
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> mSensorNames = new ArrayList<>();
    public static String azymutPublic;

    float[] mGravity;
    float[] mGeomagnetic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensory);
        Button buttonSaveAzimuth = findViewById(R.id.buttonSaveAzimuth);

        buttonSaveAzimuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float[] rotationMatrix = new float[9];
                if (SensorManager.getRotationMatrix(rotationMatrix, null, mGravity, mGeomagnetic)) {
                    float[] orientation = new float[3];
                    SensorManager.getOrientation(rotationMatrix, orientation);
                    float azimuthInRadians = orientation[0];
                    float azimuthInDegrees = (float) Math.toDegrees(azimuthInRadians);
                    azymutPublic = ("AZYMUT: " + azimuthInDegrees);
                }
            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mAzimuthTextView = findViewById(R.id.azimuthTextView);
        mSensorsListView = findViewById(R.id.sensorsListView);

        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mSensorNames);
        mSensorsListView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;
        }
        if (mGravity != null && mGeomagnetic != null) {
            float[] rotationMatrix = new float[9];
            if (SensorManager.getRotationMatrix(rotationMatrix, null, mGravity, mGeomagnetic)) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(rotationMatrix, orientation);
                float azimuthInRadians = orientation[0];
                float azimuthInDegrees = (float) Math.toDegrees(azimuthInRadians);
                mAzimuthTextView.setText("AZYMUT: " + azimuthInDegrees);
            }
        }

        mSensorNames.clear();
        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : deviceSensors) {
            String sensorName = sensor.getName() + " (" + sensor.getVendor() + ")";
            mSensorNames.add(sensorName);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}