package ch.epfl.sdp.peakar.rankings;

import android.app.Activity;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.UITestHelper;
import ch.epfl.sdp.peakar.database.Database;
import ch.epfl.sdp.peakar.user.services.AuthService;
import ch.epfl.sdp.peakar.user.services.providers.firebase.FirebaseAuthService;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.sdp.peakar.TestingConstants.BASIC_USERNAME;
import static ch.epfl.sdp.peakar.TestingConstants.SHORT_SLEEP_TIME;
import static ch.epfl.sdp.peakar.user.AccountTest.registerAuthUser;
import static ch.epfl.sdp.peakar.user.AccountTest.removeAuthUser;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class RankingsActivityTest {

    private static final Integer MAXIMUM_POINTS = Integer.MAX_VALUE;
    private static final String TESTING_USERNAME = "test@user";
    private static final List<Integer> mockPoints = IntStream.rangeClosed(MAXIMUM_POINTS-19, MAXIMUM_POINTS-1).boxed().collect(Collectors.toList());
    private static final List<Integer> mockPositions = IntStream.rangeClosed(2, 20).boxed().collect(Collectors.toList());

    /* Set up the environment */
    @BeforeClass
    public static void init() {
        Collections.reverse(mockPoints);
        // Add the mock user on the database
        for(int i=0; i < mockPoints.size(); i++) {
            Task<Void> taskName = Database.refRoot.child(Database.CHILD_USERS).child(BASIC_USERNAME + mockPositions.get(i)).child(Database.CHILD_USERNAME).setValue(TESTING_USERNAME + mockPositions.get(i));
            Task<Void> taskPoints =Database.refRoot.child(Database.CHILD_USERS).child(BASIC_USERNAME + mockPositions.get(i)).child(Database.CHILD_SCORE).setValue(mockPoints.get(i));
            try {
                Tasks.await(taskName);
                Tasks.await(taskPoints);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /* Make sure no user is signed in before a test */
        AuthService.getInstance().signOut(InstrumentationRegistry.getInstrumentation().getTargetContext());

        /* Create a new one */
        registerAuthUser();

        Task<Void> taskName = Database.refRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_USERNAME).setValue(TESTING_USERNAME);
        Task<Void> taskPoints =Database.refRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).child(Database.CHILD_SCORE).setValue(MAXIMUM_POINTS);
        try {
            Tasks.await(taskName);
            Tasks.await(taskPoints);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FirebaseAuthService.getInstance().forceRetrieveData();
    }

    /* Clean environment */
    @AfterClass
    public static void end() {
        for(int i=0; i < mockPoints.size(); i++) {
            Task<Void> taskRemove = Database.refRoot.child(Database.CHILD_USERS).child(BASIC_USERNAME + mockPositions.get(i)).removeValue();
            try {
                Tasks.await(taskRemove);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Task<Void> taskRemove = Database.refRoot.child(Database.CHILD_USERS).child(AuthService.getInstance().getID()).removeValue();
        try {
            Tasks.await(taskRemove);
        } catch (Exception e) {
            e.printStackTrace();
        }
        removeAuthUser();
    }

    @Rule
    public ActivityScenarioRule<RankingsActivity> testRule = new ActivityScenarioRule<>(RankingsActivity.class);

    /* Test that the toolbar title is set as expected */
    @Test
    public void TestToolbarTitle(){
        String TOOLBAR_TITLE = "Rankings";
        ViewInteraction greetingText = onView(ViewMatchers.withId(R.id.toolbarTitle));
        greetingText.check(matches(withText(TOOLBAR_TITLE)));
    }

    /* Test that the activity finishes when the toolbar back button is pressed. */
    @Test
    public void TestToolbarBackButton(){
        onView(withId(R.id.toolbarBackButton)).perform(click());
        try {
            Thread.sleep(SHORT_SLEEP_TIME);
            assertSame(testRule.getScenario().getResult().getResultCode(), Activity.RESULT_CANCELED);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("TestToolbarBackButton failed");
        }
    }

    /* Test that mock user sin list view are at correct places and contain correct data */
    @Test
    public void TestContentOfListView() {
        // Check correct data
        DataInteraction interaction =  onData(instanceOf(RankingItem.class));
        for(int i=0; i <= mockPoints.size(); i++) {
            DataInteraction listItem = interaction.atPosition(i);

            listItem.onChildView(withId(R.id.ranking_item_position))
                    .check(matches(withText((i == 0 ? 1 : mockPositions.get(i-1)) + ".")));
            listItem.onChildView(withId(R.id.ranking_item_username))
                    .check(matches(withText(TESTING_USERNAME + (i == 0 ? "" : mockPositions.get(i-1)))));
            listItem.onChildView(withId(R.id.ranking_item_points))
                    .check(matches(withText(String.format("%d", MAXIMUM_POINTS - i))));
        }
    }

    /* Test that all elements colors in list view are correct */
    @Test
    public void TestColorOfListView() {
        DataInteraction interaction =  onData(instanceOf(RankingItem.class));

        // Check correct colors on current fake user
        DataInteraction listItem = interaction.atPosition(0);
        listItem.onChildView(withId(R.id.ranking_item_container))
                .check(matches(UITestHelper.withBackgroundColor(R.color.DarkGreen)));
        listItem.onChildView(withId(R.id.ranking_item_position))
                .check(matches(UITestHelper.withTextColor(R.color.LightGrey)));
        listItem.onChildView(withId(R.id.ranking_item_username))
                .check(matches(UITestHelper.withTextColor(R.color.LightGrey)));
        listItem.onChildView(withId(R.id.ranking_item_points))
                .check(matches(UITestHelper.withTextColor(R.color.LightGrey)));

        // Check correct colors on other fake users
        for(int i=0; i < mockPoints.size() - 1; i++) {
            listItem = interaction.atPosition(i+1);

            listItem.onChildView(withId(R.id.ranking_item_container))
                    .check(matches(UITestHelper.withBackgroundColor(R.color.LightGrey)));
            listItem.onChildView(withId(R.id.ranking_item_position))
                    .check(matches(UITestHelper.withTextColor(R.color.DarkGreen)));
            listItem.onChildView(withId(R.id.ranking_item_username))
                    .check(matches(UITestHelper.withTextColor(R.color.DarkGreen)));
            listItem.onChildView(withId(R.id.ranking_item_points))
                    .check(matches(UITestHelper.withTextColor(R.color.DarkGreen)));
        }
    }
}
