package com.github.bgabriel998.softwaredevproject.camera;

import android.app.Activity;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.bgabriel998.softwaredevproject.map.MapActivity;
import com.github.bgabriel998.softwaredevproject.R;
import com.github.bgabriel998.softwaredevproject.points.ComputePOIPointsTest;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertSame;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


@RunWith(AndroidJUnit4.class)
public class CameraActivityTest {

    @Rule
    public ActivityScenarioRule<CameraActivity> testRule = new ActivityScenarioRule<>(CameraActivity.class);

    /* Setup environment */
    @BeforeClass
    public static void computePOIPoints(){
        new ComputePOIPointsTest();
    }

    /* Create Intent */
    @Before
    public void setup(){
        Intents.init();
    }

    /* Release Intent */
    @After
    public void cleanUp(){
        Intents.release();
    }


    /* Test that pressing the map icon button changes view to MapActivity */
    @Test
    public void TestMapIconButton(){
        ViewInteraction button = Espresso.onView(ViewMatchers.withId(R.id.mapButton));
        button.perform(ViewActions.click());
        // Catch intent
        intended(IntentMatchers.hasComponent(MapActivity.class.getName()));
    }

    /* Test that pressing the back button finish the activity */
    @Test
    public void TestBackButton(){
        ViewInteraction button = Espresso.onView(withId(R.id.cameraBackButton));
        button.perform(ViewActions.click());
        try {
            Thread.sleep(1000);
            assertSame(testRule.getScenario().getResult().getResultCode(), Activity.RESULT_CANCELED);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("TestBackButton failed");
        }
    }
}