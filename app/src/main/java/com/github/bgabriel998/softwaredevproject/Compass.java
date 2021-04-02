package com.github.bgabriel998.softwaredevproject;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Compass implements SensorEventListener {

    private CompassListener listener;

    private final SensorManager sensorManager;

    private final Sensor accelerometer;
    private final Sensor magnetometer;

    //inclination Matrix
    float[] incMat = new float[9];
    //rotation Matrix
    float[] rotMat = new float[9];
    //Accelerometer Matrix
    private final float[] accMat = new float[3];
    //Magnetometer Matrix
    private final float[] magMat = new float[3];

    /**
     * Compass constructor, initializes the device sensors and starts the compass
     * @param context Context of the activity
     */
    public Compass(Context context) {
        //Initialize device sensors
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        //Initialize accelerometer
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //Initialize magnetometer
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        //Starts the compass
        start();
    }

    /**
     * Register the accelerometer and magnetometer listeners
     */
    private void start(){
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    /**
     * Unregisters the sensor listener
     */
    public void stop() {
        sensorManager.unregisterListener(this);
    }

    /**
     * Sets the compass listener
     * @param listener CompassListener
     */
    public void setListener(CompassListener listener) {
        this.listener = listener;
    }

    /**
     * Handle sensor changes for the accelerometer and magnetometer
     * @param event contains the data that has changed
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        final float alpha = 0.95f;

        //Get the accelerometer data, use filter to smooth the data
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            updateValues(accMat, event, alpha);
        }

        //Get the accelerometer data, use filter to smooth the data
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            updateValues(magMat, event, alpha);
        }

        //See https://developer.android.com/reference/android/hardware/SensorManager#getRotationMatrix(float[],%20float[],%20float[],%20float[])
        boolean success = SensorManager.getRotationMatrix(incMat, rotMat, accMat, magMat);
        if (success) {
            float[] orientation = new float[3];
            SensorManager.getOrientation(incMat, orientation);
            float heading = (float) Math.toDegrees(orientation[0]);
            //+90 to use the compass in landscape mode correctly
            heading = (heading + 360 + 90) % 360;

            float[] orientationVertical = new float[3];
            SensorManager.getOrientation(rotMat, orientationVertical);
            float headingV = (float) Math.toDegrees(orientation[2]);
            //HeadingV to use in landscape mode correctly
            headingV = (headingV * (-1)) % 360;

            //Update the horizontal and vertical heading
            if (listener != null) {
                listener.onNewHeading(heading, headingV);
            }
        }
    }

    /**
     * Update the values of the Matrices
     * @param mat Output matrice
     * @param event Input sensor event
     * @param alpha factor to smooth out the sensors
     */
    private void updateValues(float [] mat, SensorEvent event, float alpha){
        for(int i=0; i<3; i++)
            mat[i] = alpha * mat[i] + (1 - alpha) * event.values[i];
    }

    /**
     * Not used
     * @param sensor
     * @param accuracy
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}