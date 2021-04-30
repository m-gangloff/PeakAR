package com.github.bgabriel998.softwaredevproject.general;

import android.app.Activity;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.github.bgabriel998.softwaredevproject.R;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class SettingsMapActivityTest {

    @Rule
    public ActivityScenarioRule<SettingsMapActivity> testRule = new ActivityScenarioRule<>(SettingsMapActivity.class);

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

    /* Test that the "OK" button appears after adding a pin to the map */
    @Test
    public void TestMapLongPress(){
        // checks that button is not visible
        onView((withId((R.id.settingsMapOkButton))))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView((withId((R.id.settingsMapOkButton))))
                .check(matches(not(isDisplayed())));
        // performs long press
        ViewInteraction view = onView(withId(R.id.settingsMapView));
        view.perform(ViewActions.longClick());
        // check if ok button is visible and displayed
        onView((withId((R.id.settingsMapOkButton))))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView((withId((R.id.settingsMapOkButton))))
                .check(matches(isDisplayed()));
    }

    /* Test that pressing the back button finish the activity */
    @Test
    public void TestBackButton(){
        ViewInteraction button = onView(withId(R.id.settingsMapBackButton));
        button.perform(ViewActions.click());
        try {
            Thread.sleep(1000);
            assertSame(testRule.getScenario().getResult().getResultCode(), Activity.RESULT_CANCELED);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("TestBackButton failed");
        }
    }

    /* Tests that the activity is terminated after the ok button is pressed. */
    @Test
    public void okButtonPressed() {
        // performs long press
        ViewInteraction mapView = onView(withId(R.id.settingsMapView));
        mapView.perform(ViewActions.longClick());
        // press the ok button
        ViewInteraction view = onView(withId(R.id.settingsMapOkButton));
        view.perform(ViewActions.click());
        // Check if activity is stopped
        try {
            Thread.sleep(1000);
            assertSame(testRule.getScenario().getResult().getResultCode(), Activity.RESULT_CANCELED);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("TestBackButton failed");
        }

    }

    /* checks if the offline content is saved after the ok button is pressed */
    @Test
    public void saveJsonTest() throws InterruptedException {
        // performs long press
        ViewInteraction mapView = onView(withId(R.id.settingsMapView));
        mapView.perform(ViewActions.longClick());
        // press the ok button
        ViewInteraction view = onView(withId(R.id.settingsMapOkButton));
        view.perform(ViewActions.click());
        // Check if the file exists
        Thread.sleep(15000);
        readFromFile();
        Assert.assertTrue(true);
    }

    /* Helper method to read .txt file */
    private void readFromFile() {

        String ret = "";

        try {
            InputStream inputStream =  ApplicationProvider.getApplicationContext().openFileInput("save.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (IOException e) {
            fail();
        }

        Log.d("Debug", ret);
    }

}