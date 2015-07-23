package com.example.emanuele.fourclassactivityrecognition;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.widget.Button;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    //private float XMax = 0;
    //private float YMax = 0;
    //private float ZMax = 0;

    private float X = 0;
    private float Y = 0;
    private float Z = 0;

    private TextView currentX, currentY, currentZ, maxX, maxY, maxZ;

    Button startButton = null;
    OnClickListener startListener = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startListener = new OnClickListener() {
            @Override
            public void onClick(View v) {

                // 1. create an intent pass class name or intent action name
                Intent intent = new Intent("com.example.fourclassactivityrecognition.TrainingActivity");

                //3. start the activity
                startActivity(intent);
            }
        };

        startButton = (Button) findViewById(R.id.buttonTraining);
        startButton.setOnClickListener(startListener);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);           //inizializzazione oggetto sensor manager
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {            //
            // success! we have an accelerometer

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);      //
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);     //associa al nostro oggetto un listener, c'è this perchè il primo parametro è un oggetto di altra classe ovvero SensorEventListenere, all'inizio
        } else {
            // fail! we dont have an accelerometer!
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //onResume() register the accelerometer for listening the events

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {            //consiglio del prof: tenere leggere questa funzione

        // clean current values
        //displayCleanValues();
        // display the current x,y,z accelerometer values
        //displayCurrentValues();
        // display the max x,y,z accelerometer values
        //displayMaxValues();

        // set the last know values of x,y,z
        X = event.values[0];
        Y = event.values[1];
        Z = event.values[2];

    }

    public void displayCleanValues() {
        currentX.setText("0.0");
        currentY.setText("0.0");
        currentZ.setText("0.0");
    }

    // display the current x,y,z accelerometer values
    public void displayCurrentValues() {
        currentX.setText(Float.toString(X));
        currentY.setText(Float.toString(Y));
        currentZ.setText(Float.toString(Z));
    }
}
