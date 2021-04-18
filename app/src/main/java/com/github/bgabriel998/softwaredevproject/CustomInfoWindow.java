package com.github.bgabriel998.softwaredevproject;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.bgabriel998.softwaredevproject.R;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

/**
 * Custom info window for markers. The design from the info
 * window provided by osmdroid pack was ugly so this class
 * extends the default one
 */
public class CustomInfoWindow extends InfoWindow {
    private final Context context;
    public CustomInfoWindow(int layoutResId, MapView mapView, Context context) {
        super(layoutResId, mapView);
        this.context = context;
    }

    /**
     * Callback method called on maker info window closing
     */
    public void onClose() {

    }

    /**
     * Callback method called when the user clicks on the marker
     * @param arg0 marker object. The Title field of this object is used to pass
     *             info about the peak to the custom info window
     */
    public void onOpen(Object arg0) {
        //retrieve views
        LinearLayout layout = (LinearLayout) mView.findViewById(R.id.bubble_layout);
        TextView txtTitle = (TextView) mView.findViewById(R.id.bubble_title);
        TextView txtDescription = (TextView) mView.findViewById(R.id.bubble_description);

        //Text information are passed to this class using the arg0 marker input
        //The title field of the marker is used to transfer the string
        String[] rawStringArray = ((Marker)arg0).getTitle().split("\n");
        String peakName = rawStringArray[0];
        String peakHeight = rawStringArray[1];
        //... Handle other fields to display here

        //Set the peak name
        txtTitle.setText(peakName);
        //Set peak height in meters
        txtDescription.setText(context.getResources().getString(R.string.marker_altitude,peakHeight));

        layout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //... add here method to redirect to the collection of peaks (activity)
            }
        });

        //Handle hide and show Info window
        Marker current = (Marker) arg0;
        toggleWindowVisibility(current, layout);

    }

    /**
     * toggles the marker visibility
     * @param marker info window marker
     * @param layout info window layout
     */
    private void toggleWindowVisibility(Marker marker, LinearLayout layout){
        for (int i = 0; i < mMapView.getOverlays().size(); ++i) {
            Overlay o = mMapView.getOverlays().get(i);
            if (o instanceof Marker) {
                Marker m = (Marker) o;
                if (m != marker) {
                    //Toggle marker visibility
                    if (layout.getVisibility() == View.GONE) {
                        layout.setVisibility(View.VISIBLE);
                    } else {
                        layout.setVisibility(View.GONE);
                    }
                }
            }
        }
    }
}

