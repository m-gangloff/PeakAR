package ch.epfl.sdp.peakar.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.epfl.sdp.peakar.R;

import static ch.epfl.sdp.peakar.utils.StatusBarHandlerFragments.StatusBarLightGrey;
import static ch.epfl.sdp.peakar.utils.StatusBarHandlerFragments.StatusBarTransparentBlack;
import static ch.epfl.sdp.peakar.utils.TopBarHandlerFragments.setupGreyTopBar;
import static ch.epfl.sdp.peakar.utils.TopBarHandlerFragments.setupTransparentTopBar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GalleryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GalleryFragment extends Fragment {

    private boolean returnToFragment;

    public GalleryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GalleryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GalleryFragment newInstance() {
        GalleryFragment fragment = new GalleryFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        returnToFragment = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        initFragment();
        if(returnToFragment){

        }
        else
            returnToFragment = true;
    }

    private void initFragment() {
        StatusBarLightGrey(this);
        setupGreyTopBar(this);
        //MenuBarHandler.setup(this);
    }
}