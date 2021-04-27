package com.github.bgabriel998.softwaredevproject.camera;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Compass implements SensorEventListener {
    //Compass listener to update the compass heading
    private CompassListener compassListener;

    //SensorManager to access the sensors
    private final SensorManager sensorManager;

    //Low-pass filter constants
    private final float ALPHA = 0.8f;
    private final float SMOOTH_FACTOR_COMPASS = 0.2f;
    private final float SMOOTH_THRESHOLD_COMPASS = 20f;

    //rotation Matrix
    private final float[] rotMat = new float[16];
    //orientation Matrix
    private final float[] orientationMat = new float[3];
    //orientation Matrix
    private final float[] oldOrientationMat = new float[3];
    //orientation vector
    private final float[] orientationVector = new float[4];



    /**
     * Compass constructor, initializes the device sensors and registers the listener
     * @param context Context of the activity
     */
    public Compass(Context context) {
        //Initialize device sensors
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        //Initialize rotation vector
        Sensor rotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        //Register the listener for the rotation type vector
        sensorManager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_GAME);
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
        this.compassListener = listener;
    }

    /**
     * Handle sensor changes for the rotation type vector
     * TYPE_ROTATION_VECTOR combines the accelerometer, magnetometer and gyroscope
     * to get the best values and eliminates the gimbal lock
     * @param event contains the data that has changed
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        //Get the rotation vector data
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            //Apply low-pass filter to the event.values
            updateSensorValues(orientationVector, event);

            //Matrix to cache the rotation vector
            float[] rotMatFromVector = new float[16];

            // Convert the rotation-vector to a 4x4 matrix.
            SensorManager.getRotationMatrixFromVector(rotMatFromVector, event.values);

            //Rotates the rotation matrix to be expressed in a different coordinate system
            SensorManager.remapCoordinateSystem(rotMatFromVector, SensorManager.AXIS_X,
                    SensorManager.AXIS_Z, rotMat);

            //Compute the device orientation with the rotation matrix
            SensorManager.getOrientation(rotMat, orientationMat);

            //Convert values to degrees
            convertArrToDegrees(orientationMat);

            //Apply low-pass filter
            //lowPassFilter(orientationMat, oldOrientationMat);

            //Add 360° to only get positive values
            float headingHorizontal = (orientationMat[0] + 360) % 360;

            //Multiply by -1 to get increasing values when inclining the device
            //Add 90° to get values from 0° to 180°
            float headingVertical = (orientationMat[1] + 90) % 360;

            //Update the horizontal and vertical heading
            if (compassListener != null) {
                compassListener.onNewHeading(headingHorizontal, headingVertical);
            }
        }
    }

    /**
     * Convert all values of array to degrees and makes the values go from 0 to 360 degrees
     * @param orientationMat Array of values that gets converted from radians to degrees
     */
    private void convertArrToDegrees(float[] orientationMat) {
        for(int i=0; i<orientationMat.length; i++){
            //orientationMat[i] = (float)(Math.toDegrees(orientationMat[i]) + 360) % 360;
            orientationMat[i] = (float)(Math.toDegrees(orientationMat[i]) + 360) % 360;
        }
    }

    /**
     * Applies a low-pass filter on the SensorEvent values
     * @param mat Output matrix
     * @param event Input sensor event
     */
    private void updateSensorValues(float [] mat, SensorEvent event){
        for(int i=0; i<mat.length; i++){
            double newSin = ALPHA * Math.sin(mat[i]) + (1 - ALPHA) *
                    Math.sin(event.values[i]);
            double newCos = ALPHA * ALPHA * Math.cos(mat[i]) + (1 - ALPHA) *
                    Math.cos(event.values[i]);
            mat[i] = (float) Math.atan2(newSin, newCos);
        }
    }

    /**
     * Applies a low-pass filter on the sensor values and updates the oldMat array
     * @param newMat Output matrix that contains new values
     * @param oldMat old values
     */
    private void lowPassFilter(float[] newMat, float[] oldMat){
        for(int i=0; i<newMat.length; i++) {
//            if (Math.abs(newMat[i] - oldMat[i]) < 180) {
//                if (Math.abs(newMat[i] - oldMat[i]) > SMOOTH_THRESHOLD_COMPASS) {
//                    newMat[i] = newMat[i];
//                }
//                else {
//                    newMat[i] = oldMat[i] + SMOOTH_FACTOR_COMPASS * (newMat[i] - oldMat[i]);
//                }
//            }
//            else {
//                if (360.0 - Math.abs(newMat[i] - oldMat[i]) < SMOOTH_THRESHOLD_COMPASS) {
//                    if (newMat[i] > oldMat[i]) {
//                        newMat[i] = (newMat[i] + SMOOTH_FACTOR_COMPASS * ((360 + oldMat[i] - newMat[i]) % 360) + 360) % 360;
//                    }
//                    else {
//                        newMat[i] = (newMat[i] - SMOOTH_FACTOR_COMPASS * ((360 - oldMat[i] + newMat[i]) % 360) + 360) % 360;
//                    }
//                }
//            }
            double newSin = ALPHA * Math.sin(Math.toRadians(oldMat[i])) + (1 - ALPHA) *
                    Math.sin(Math.toRadians(newMat[i]));
            double newCos = ALPHA * Math.cos(Math.toRadians(oldMat[i])) + (1 - ALPHA) *
                    Math.cos(Math.toRadians(newMat[i]));
            newMat[i] = (float) Math.toDegrees(Math.atan2(newSin, newCos));
            oldMat[i] = newMat[i];
        }
    }

    /**
     * Not used
     * @param sensor Sensor
     * @param accuracy Accuracy of sensor
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
