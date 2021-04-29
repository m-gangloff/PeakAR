package com.github.bgabriel998.softwaredevproject.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.SizeF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.github.bgabriel998.softwaredevproject.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A {@link Fragment} subclass that represents the camera-preview.
 * Use the {@link CameraPreview#newInstance} factory method to
 * create an instance of this fragment.
 * See: https://github.com/android/camera-samples/blob/main/CameraXBasic
 */
public class CameraPreview extends Fragment{

    private PreviewView previewView;
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private Context context;

    private final String FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS";
    private final String PHOTO_EXTENSION = ".jpg";
    private final int PICTURE_TAKEN = 1;
    private final int FAILED_TO_TAKE_PICTURE = 0;
    private final int FILE_LENGTH = 27;

    private ImageAnalysis imageAnalysis;

    private int previewDisplayId = -1;

    private DisplayManager displayManager;

    private DisplayManager.DisplayListener displayListener;

    String lastToast = null;

    /**
     * Constructor for the CameraPreview
     * Is required to be empty for the fragments
     */
    public CameraPreview() {}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CameraPreview.
     */
    public static CameraPreview newInstance() {
        return new CameraPreview();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.camera_preview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ConstraintLayout container = (ConstraintLayout) view;
        previewView = container.findViewById(R.id.cameraPreview);

        //Initialize background executor
        cameraExecutor = Executors.newSingleThreadExecutor();

        //Register display listener
        displayManager.registerDisplayListener(displayListener, null);

        //Configure context
        context = getContext();

        //Wait for the view to be properly laid out
        previewView.post(() -> {
            previewDisplayId = previewView.getDisplay().getDisplayId();

            setUpCamera();
        });
    }

    /**
     * Create listeners after that the Fragment was attached and has a context
     * @param context context
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        displayManager = (DisplayManager) requireContext().getSystemService(Context.DISPLAY_SERVICE);

        displayListener = new DisplayManager.DisplayListener() {
            @Override
            public void onDisplayAdded(int displayId) {}

            @Override
            public void onDisplayRemoved(int displayId) {}

            @Override
            public void onDisplayChanged(int displayId) {
                if(displayId == previewDisplayId){
                    imageCapture.setTargetRotation(getView().getDisplay().getRotation());
                    imageAnalysis.setTargetRotation(getView().getDisplay().getRotation());
                }
            }
        };
    }

    /**
     *  Unbind and shutdown camera before exiting camera
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        //Unbind use-cases before exiting
        cameraProvider.unbindAll();

        // Shut down our background executor
        cameraExecutor.shutdown();


        displayManager.unregisterDisplayListener(displayListener);
    }

    /**
     *  Setup cameraProvider and call bindPreview
     */
    private void setUpCamera(){
        //ProcessCameraProvider: Used to bind the lifecycle of cameras
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            //CameraProvider
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(context));
    }

    /**
     * Declare and bind preview and analysis use cases
     * @param cameraProvider used to bind the lifecycle of the camera
     */
    private void bindPreview(ProcessCameraProvider cameraProvider) {
        //Get screen metrics
        DisplayMetrics displayMetrics = new DisplayMetrics();
        previewView.getDisplay().getRealMetrics(displayMetrics);

        //Calculate aspectRatio
        int screenAspectRatio = aspectRatio(displayMetrics.widthPixels, displayMetrics.heightPixels);

        //Get screen rotation
        int rotation = previewView.getDisplay().getRotation();

        //CameraSelector
        CameraSelector cameraSelector = new CameraSelector.Builder()
                //Only use back facing camera
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        //preview
        Preview preview = new Preview.Builder()
                //Set aspect ratio but not resolution, resolution is optimized by CameraX
                .setTargetAspectRatio(screenAspectRatio)
                //Set initial rotation
                .setTargetRotation(rotation)
                .build();

        // ImageCapture
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                // Set aspect ratio, let cameraX handle the resolution
                .setTargetAspectRatio(screenAspectRatio)
                // Set rotation
                .setTargetRotation(rotation)
                .build();

        //ImageAnalysis
        //Only deliver latest image to the analyzer
        imageAnalysis = new ImageAnalysis.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                //Only deliver latest image to the analyzer
                //.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        //Assign analyzer to the instance
        imageAnalysis.setAnalyzer(cameraExecutor, ImageProxy::close);

        //Unbind use-cases before rebinding
        cameraProvider.unbindAll();

        //Bind use cases to camera
        cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, preview, imageCapture, imageAnalysis);

        //Attach the viewfinder's surface provider to preview
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
    }

    /**
     * Redraws the camera preview when configuration gets changed
     * @param newConfig new configuration
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //Redraw the cameraUI
        setUpCamera();
    }

    /**
     * Calculate the aspect ratio of the display in function of the width and height of the screen
     * @param width width of the preview in Pixels
     * @param height height of the preview in Pixels
     * @return Aspect ratio of the phone
     */
    private int aspectRatio(int width, int height){
        double previewRatio = (double)Math.max(width, height)/Math.min(width, height);
        double RATIO_16_9_VALUE = 16.0 / 9.0;
        double RATIO_4_3_VALUE = 4.0 / 3.0;
        return (Math.abs(previewRatio - RATIO_4_3_VALUE) <= Math.abs(previewRatio - RATIO_16_9_VALUE))
                ? AspectRatio.RATIO_4_3 : AspectRatio.RATIO_16_9;
    }

    /**
     * Get the horizontal and vertical field of view of the back-facing amera
     * @return null if there is no camera, else the horizontal and vertical
     * field of view in degrees
     */
    public Pair<Float, Float> getFieldOfView(Context context) throws CameraAccessException {
        //Create package manager to check if the device has a camera
        PackageManager pm = context.getPackageManager();
        return (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) ? calculateFOV(context) : null;
    }

    /**
     * Calculates the horizontal and vertical field of view of the back-facing camera
     * @return Pair of the horizontal and vertical fov
     */
    private Pair<Float, Float> calculateFOV(Context context) throws CameraAccessException {
        //Create camera manager
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        double horizontalAngle = 0;
        double verticalAngle = 0;
        //Go through every camera to get the back-facing camera
        for (final String cameraId : cameraManager.getCameraIdList()) {
            //Check if camera is back-facing
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            int lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                //If camera is back-facing, calculate the fov
                //Initialize horiontal and vertical fov
                //Get sizes of the lenses
                float focalLength = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)[0];
                SizeF physicalSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
                float width = physicalSize.getWidth();
                float height = physicalSize.getHeight();
                //Calculate the fovs
                horizontalAngle = 2 * Math.atan(width / (2 * focalLength));
                verticalAngle = 2 * Math.atan(height / (2 * focalLength));

            }
        }
        return new Pair<>((float) Math.toDegrees(horizontalAngle), (float) Math.toDegrees(verticalAngle));
    }
    
    /**
     * Takes a picture of the camera-preview without the canvas drawn
     */
    public void takePicture(){
        //Create the file
        File photoFile = CameraActivity.createFile(context, FILENAME, PHOTO_EXTENSION);

        //Configure output options
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(
                photoFile).build();

        //Take the picture
        imageCapture.takePicture(outputOptions, cameraExecutor, onImageSavedCallback(photoFile));
    }

    /**
     * Creates the onImageSavedCallback for when a picture is saved
     * @param photoFile File that is saved
     * @return ImageCapture.onImageSavedCallback
     */
    private ImageCapture.OnImageSavedCallback onImageSavedCallback(File photoFile){
        return new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                //Get the Uri of the saved picture
                Uri savedUri = outputFileResults.getSavedUri() != null ? outputFileResults.getSavedUri() : Uri.fromFile(photoFile);
                handler.sendMessage(handler.obtainMessage(PICTURE_TAKEN, savedUri));
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                handler.sendMessage(handler.obtainMessage(FAILED_TO_TAKE_PICTURE, exception));
            }

            //Create a handler to display the toast inside of the onImageSaved callback
            final Handler handler = pictureMessageHandler();
        };
    }

    /**
     * Handler that is used to display the toast inside the onImageSavedCallback
     * @return Handler for the toast messages
     */
    private Handler pictureMessageHandler(){
        return new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                //Make the toasts depending on the Message code
                if(msg.what == FAILED_TO_TAKE_PICTURE){
                    String toastMessage = getResources().getString(R.string.pictureSavedFailed) + " " + msg.obj.toString();
                    lastToast = toastMessage;
                    Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show();
                }
                else if(msg.what == PICTURE_TAKEN){
                    //Display only the file location
                    String toastMessage = getResources().getString(R.string.pictureSavedSuccessfully) + " " +
                            msg.obj.toString().substring(0, msg.obj.toString().length() - FILE_LENGTH);
                    lastToast = toastMessage;
                    Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    /**
     * Get the camera-preview as a bitmap
     * @return a bitmap of the camera-preview
     */
    public Bitmap getBitmap(){
        return previewView.getBitmap();
    }
}