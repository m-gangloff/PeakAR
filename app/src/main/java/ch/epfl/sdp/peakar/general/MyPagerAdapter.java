package ch.epfl.sdp.peakar.general;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ch.epfl.sdp.peakar.camera.CameraPreview;
import ch.epfl.sdp.peakar.fragments.GalleryFragment;
import ch.epfl.sdp.peakar.fragments.MapFragment;
import ch.epfl.sdp.peakar.fragments.SocialFragment;

public class MyPagerAdapter extends FragmentStateAdapter {

    private static final int CAMERA_PREVIEW = 2;
    private static final int MAP_FRAGMENT = 3;
    private static final int SOCIAL_FRAGMENT = 4;
    private static final int SETTINGS_FRAGMENT = 0;
    private static final int GALLERY_FRAGMENT = 1;

    public MyPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position){
            case CAMERA_PREVIEW:
                return CameraPreview.newInstance();
            case MAP_FRAGMENT:
                return MapFragment.newInstance();
            case SOCIAL_FRAGMENT:
                return SocialFragment.newInstance();
            case SETTINGS_FRAGMENT:
                //return SettingsFragment.newInstance();
                return null;
            case GALLERY_FRAGMENT:
                return GalleryFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
