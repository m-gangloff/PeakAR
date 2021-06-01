package ch.epfl.sdp.peakar.utils;

import android.app.Activity;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.util.HashMap;
import java.util.Map.Entry;

import ch.epfl.sdp.peakar.R;
import ch.epfl.sdp.peakar.fragments.CameraFragment;
import ch.epfl.sdp.peakar.fragments.GalleryFragment;
import ch.epfl.sdp.peakar.fragments.MapFragment;
import ch.epfl.sdp.peakar.fragments.SettingsFragment;
import ch.epfl.sdp.peakar.fragments.SocialFragment;

import static ch.epfl.sdp.peakar.utils.MyPagerAdapter.CAMERA_FRAGMENT_INDEX;
import static ch.epfl.sdp.peakar.utils.MyPagerAdapter.GALLERY_FRAGMENT_INDEX;
import static ch.epfl.sdp.peakar.utils.MyPagerAdapter.MAP_FRAGMENT_INDEX;
import static ch.epfl.sdp.peakar.utils.MyPagerAdapter.SETTINGS_FRAGMENT_INDEX;
import static ch.epfl.sdp.peakar.utils.MyPagerAdapter.SOCIAL_FRAGMENT_INDEX;
import static ch.epfl.sdp.peakar.utils.UIUtils.setTintColor;

/**
 * Static handler for MenuBar.
 * With the function setup to be called from fragments that show the menu bar.
 */
public class MenuBarHandlerFragments {

    /**
     * Map for pairing the icon with class to send intent
     */
    private static final HashMap<Integer, Class<?>> iconClassMap = new HashMap<>();
    static {
        iconClassMap.put(R.id.menu_bar_settings, SettingsFragment.class);
        iconClassMap.put(R.id.menu_bar_gallery, GalleryFragment.class);
        iconClassMap.put(R.id.menu_bar_camera, CameraFragment.class);
        iconClassMap.put(R.id.menu_bar_map, MapFragment.class);
        iconClassMap.put(R.id.menu_bar_social, SocialFragment.class);
    }

    /**
     * Map for pairing icon with pointer to icon
     */
    private static final HashMap<Integer, Integer> iconPointerMap = new HashMap<>();
    static {
        iconPointerMap.put(R.id.menu_bar_settings, R.id.menu_bar_settings_pointer);
        iconPointerMap.put(R.id.menu_bar_gallery, R.id.menu_bar_gallery_pointer);
        iconPointerMap.put(R.id.menu_bar_camera, R.id.menu_bar_camera_pointer);
        iconPointerMap.put(R.id.menu_bar_map, R.id.menu_bar_map_pointer);
        iconPointerMap.put(R.id.menu_bar_social, R.id.menu_bar_social_pointer);
    }

    /**
     * Sets up the Menu Bar, given the activity the bar is currently displaying
     * to make that icon non clickable and appear selected.
     * All other icons will send intent to start the corresponding activity.
     * @param activity current active activity.
     */
    public static void setup(Activity activity, ViewPager2 viewPager) {
        for (Entry<Integer, Class<?>> pair : iconClassMap.entrySet()) {
            activity.findViewById(pair.getKey()).setOnClickListener(v ->  {
                Class<?> classToStart = pair.getValue();
                viewPager.setCurrentItem(getFragmentIndex(classToStart));
            });
        }
    }

    /**
     * Updates the UI of the menu bar
     * @param fragment active fragment
     */
    public static void updateSelectedIcon(Fragment fragment){
        Activity activity = fragment.requireActivity();
        Class<?> activeClass = fragment.getClass();
        for (Entry<Integer, Class<?>> pair : iconClassMap.entrySet()) {
            int key = pair.getKey();
            if (pair.getValue() == activeClass) {
                selectIcon(activity, key);
            }
            else {
                unSelectIcon(activity, key);
            }
        }
    }

    /**
     * Gets the index for the viewPager depending on the class that is selected
     * @param classToStart class that is started
     * @return Index of the page
     */
    private static int getFragmentIndex(Class<?> classToStart){
        switch(classToStart.getSimpleName()){
            case "SettingsFragment":
                return SETTINGS_FRAGMENT_INDEX;
            case "GalleryFragment":
                return GALLERY_FRAGMENT_INDEX;
            case "MapFragment":
                return MAP_FRAGMENT_INDEX;
            case "SocialFragment":
                return SOCIAL_FRAGMENT_INDEX;
            case "CameraFragment":
            default:
                return CAMERA_FRAGMENT_INDEX;
        }
    }

    /**
     * Gets the class from the viewPager index
     * @param index class that is started
     * @return Index of the page
     */
    private static Class<?> getClassFromIndex(int index){
        switch(index){
            case SETTINGS_FRAGMENT_INDEX:
                return SettingsFragment.class;
            case GALLERY_FRAGMENT_INDEX:
                return GalleryFragment.class;
            case MAP_FRAGMENT_INDEX:
                return MapFragment.class;
            case SOCIAL_FRAGMENT_INDEX:
                return SocialFragment.class;
            case CAMERA_FRAGMENT_INDEX:
            default:
                return CameraFragment.class;
        }
    }

    /**
     * Given an id to an icon make it appear selected.
     * @param activity given activity.
     * @param viewId id to icon to "select".
     */
    private static void selectIcon(Activity activity, int viewId) {
        setTintColor(activity.findViewById(viewId), R.color.LightGreen);
        Object obj = iconPointerMap.get(viewId);
        int id = obj!=null ? (int)obj : R.id.menu_bar_camera_pointer;
        activity.findViewById(id).setVisibility(View.VISIBLE);
    }

    /**
     * Given an id to an icon make it appear un selected.
     * @param activity given activity.
     * @param viewId id to icon to "select".
     */
    private static void unSelectIcon(Activity activity, int viewId) {
        Object obj = iconPointerMap.get(viewId);
        int id = obj!=null ? (int)obj : R.id.menu_bar_camera_pointer;
        activity.findViewById(id).setVisibility(View.INVISIBLE);
        setTintColor(activity.findViewById(viewId), R.color.White);
    }
}
