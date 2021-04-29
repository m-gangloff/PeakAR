package com.github.bgabriel998.softwaredevproject.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceRequest;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.test.annotation.UiThreadTest;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.github.bgabriel998.softwaredevproject.R;
import com.github.bgabriel998.softwaredevproject.camera.CameraActivity;
import com.github.bgabriel998.softwaredevproject.camera.CameraUiView;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class CameraPreviewTest implements LifecycleOwner, ImageReader.OnImageAvailableListener, Consumer<SurfaceRequest.Result> {
    @Rule
    public ActivityScenarioRule<CameraActivity> testRule = new ActivityScenarioRule<>(CameraActivity.class);
    @Rule
    public GrantPermissionRule grantCameraPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA);

    private final LifecycleRegistry registry = new LifecycleRegistry(this);
    private final HandlerThread thread = new HandlerThread("CameraPreviewTest");

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ProcessCameraProvider provider;

    @Before
    public void setup() throws ExecutionException, InterruptedException {
        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
        provider = ProcessCameraProvider.getInstance(context).get();
        Assert.assertNotNull(provider);
    }

    @UiThreadTest
    @After
    public void teardown() {
        provider.unbindAll();
        executor.shutdown();
    }

    /**
     * In checkPreviewUseCase, ImageReader will provide a Surface for camera preview test.
     * When each ImageProxy is acquired, the AtomicInteger will be incremented.
     * By doing so we can ensure the camera binding is working as expected.
     */
    private final ImageReader reader = ImageReader.newInstance(1440, 1080, ImageFormat.YUV_420_888, 30);
    private final AtomicInteger counter = new AtomicInteger(0);

    @Before
    public void setupImageReader() {
        thread.start();
        reader.setOnImageAvailableListener(this,new Handler(thread.getLooper()));
    }

    @After
    public void teardownImageReader() {
        reader.close();
        thread.quit();
    }

    public void onImageAvailable(ImageReader reader) {
        Image image = reader.acquireNextImage();
        int imageNumber = counter.getAndIncrement();
        Log.i("CameraPreviewTest", String.format("image: %d %s", imageNumber, image));
    }

    @UiThreadTest
    @Before
    public void markCreated() {
        registry.setCurrentState(Lifecycle.State.INITIALIZED);
        registry.setCurrentState(Lifecycle.State.CREATED);
    }

    @UiThreadTest
    @After
    public void markDestroyed() {
        registry.setCurrentState(Lifecycle.State.DESTROYED);
    }

    public void accept(SurfaceRequest.Result result) {
        switch(result.getResultCode()) {
            case SurfaceRequest.Result.RESULT_SURFACE_USED_SUCCESSFULLY:
                Log.i("CameraPreviewTest", result.toString());
                break;
            case SurfaceRequest.Result.RESULT_REQUEST_CANCELLED:
            case SurfaceRequest.Result.RESULT_INVALID_SURFACE:
            case SurfaceRequest.Result.RESULT_SURFACE_ALREADY_PROVIDED:
            case SurfaceRequest.Result.RESULT_WILL_NOT_PROVIDE_SURFACE:
                Log.e("CameraPreviewTest", result.toString());
        }
    }

    /**
     * Check that camera use cases are correctly set and that the camera is correctly binded
     * See https://github.com/android/camera-samples/tree/main/CameraXBasic
     */
    @UiThreadTest
    @Test
    public void checkPreviewUseCase() throws InterruptedException, CameraInfoUnavailableException {
        // life cycle owner
        registry.setCurrentState(Lifecycle.State.STARTED);

        // select Back camera
        CameraSelector.Builder selectorBuilder = new CameraSelector.Builder();
        assertTrue(provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA));
        selectorBuilder.requireLensFacing(CameraSelector.LENS_FACING_BACK);

        // fit the preview size to ImageReader
        Preview.Builder previewBuilder = new Preview.Builder();
        previewBuilder.setTargetResolution(new Size(reader.getWidth(), reader.getHeight()));
        previewBuilder.setTargetRotation(Surface.ROTATION_90);
        Preview preview = previewBuilder.build();

        // acquire camera binding
        provider.unbindAll();
        Camera camera = provider.bindToLifecycle(this, selectorBuilder.build(), preview);
        Assert.assertNotNull(camera);
        preview.setSurfaceProvider(executor, request -> {
            Surface surface = reader.getSurface();
            Log.i("CameraPreviewTest", String.format("providing: %s", surface));
            request.provideSurface(surface, executor, this::accept);
        });
        // wait until onImageAvailable is invoked. retry several times
        for (int repeat=50; repeat>=0; repeat--) {
            Thread.sleep(1000);
            int value = counter.get();
            Log.i("CameraPreviewTest", String.format("count: %d", value));
            if (value > 0) return;
        }
        Assert.assertNotEquals(0, counter.get());
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return registry;
    }

    /**
     * Check if the correct orientation string is returned
     */
    @Test
    public void getCorrectOrientationString(){
        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
        CameraUiView cameraUiView = new CameraUiView(context, null);
        assertEquals(cameraUiView.selectHeadingString(0), "N");
        assertEquals(cameraUiView.selectHeadingString(45), "NE");
        assertEquals(cameraUiView.selectHeadingString(90), "E");
        assertEquals(cameraUiView.selectHeadingString(135), "SE");
        assertEquals(cameraUiView.selectHeadingString(180), "S");
        assertEquals(cameraUiView.selectHeadingString(225), "SW");
        assertEquals(cameraUiView.selectHeadingString(270), "W");
        assertEquals(cameraUiView.selectHeadingString(315), "NW");
        assertEquals(cameraUiView.selectHeadingString(360), "N");
        assertEquals(cameraUiView.selectHeadingString(1), "");
    }

    /**
     * Test that toast is displayed with correct text after taking a picture in portrait and landscape mode
     */
    @Test
    public void takePictureTest() throws NoSuchMethodException, InterruptedException {
        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);
        String pictureTakenCorrectly = context.getResources().getString(R.string.pictureTakenToast);

        //Method to get the last displayed toast
        Method getLastToast = CameraActivity.class.getDeclaredMethod("getLastToast");
        getLastToast.setAccessible(true);

        //Method to get the last displayed toast
        Method setLastToast = CameraActivity.class.getDeclaredMethod("setLastToast", String.class);
        getLastToast.setAccessible(true);

        testRule.getScenario().onActivity(activity -> {
            try {
                //Check that last displayed toast is null
                assertNull(getLastToast.invoke(activity));
                //Set orientation to portrait
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        //Wait for the view to be correctly displayed
        Thread.sleep(6000);

        //Take a picture, needs to be done outside of onActivity
        onView(withId(R.id.takePicture)).perform(click());

        Thread.sleep(6000);

        testRule.getScenario().onActivity(activity -> {
            try {
                //Check that correct toast was displayed
                assertEquals(pictureTakenCorrectly, getLastToast.invoke(activity));
                //Reset toast
                setLastToast.invoke(activity, (Object) null);
                //Check that last toast was reset
                assertNull(getLastToast.invoke(activity));
                //Rotate screen back to portrait
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                //Wait for orientation changes

            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });

        //Wait for orientation changes
        Thread.sleep(6000);

        //Take a picture
        onView(withId(R.id.takePicture)).perform(click());

        //Wait for the toast to get displayed
        Thread.sleep(6000);

        testRule.getScenario().onActivity(activity -> {
            try {
                //Check that correct toast was displayed
                assertEquals(pictureTakenCorrectly, getLastToast.invoke(activity));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }
}