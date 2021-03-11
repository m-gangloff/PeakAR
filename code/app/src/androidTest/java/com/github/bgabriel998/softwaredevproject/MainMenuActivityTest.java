package com.github.bgabriel998.softwaredevproject;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class MainMenuActivityTest {

    @Rule
    public ActivityScenarioRule<MainMenuActivity> testRule = new ActivityScenarioRule<>(MainMenuActivity.class);

    /* Test that pressing the profile button changes view to ProfileActivity */
    @Test
    public void TestProfileButton(){
        Intents.init();
        ViewInteraction button = Espresso.onView(withId(R.id.profileButton));
        button.perform(ViewActions.click());
        // Catch intent
        intended(IntentMatchers.hasComponent(ProfileActivity.class.getName()));
        Intents.release();
    }

    /* Test that pressing the settings button changes view to SettingsActivity */
    @Test
    public void TestSettingsButton(){
        Intents.init();
        ViewInteraction button = Espresso.onView(withId(R.id.settingsButton));
        button.perform(ViewActions.click());
        // Catch intent
        intended(IntentMatchers.hasComponent(SettingsActivity.class.getName()));
        Intents.release();
    }

    /* Test that pressing the camera button changes view to CameraActivity */
    @Test
    public void TestCameraButton(){
        Intents.init();
        ViewInteraction button = Espresso.onView(withId(R.id.startCameraButton));
        button.perform(ViewActions.click());
        // Catch intent
        intended(IntentMatchers.hasComponent(CameraActivity.class.getName()));
        Intents.release();
    }

    /* Test that pressing the collection button changes view to CollectionActivity */
    @Test
    public void TestCollectionButton(){
        Intents.init();
        ViewInteraction button = Espresso.onView(withId(R.id.collectionButton));
        button.perform(ViewActions.click());
        // Catch intent
        intended(IntentMatchers.hasComponent(CollectionActivity.class.getName()));
        Intents.release();
    }

    /* Test that pressing the ranking button changes view to RankingsActivity */
    @Test
    public void TestRankingsButton(){
        Intents.init();
        ViewInteraction button = Espresso.onView(withId(R.id.rankingsButton));
        button.perform(ViewActions.click());
        // Catch intent
        intended(IntentMatchers.hasComponent(RankingsActivity.class.getName()));
        Intents.release();
    }
}