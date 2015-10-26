package com.example.dongja94.samplegooglemap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.Map;

/**
 * Created by dongja94 on 2015-10-26.
 */
public class MyInfoWindow implements GoogleMap.InfoWindowAdapter {
    View infoWindow;
    TextView titleView, snippetView;
    Map<Marker, POI> mPOIResolver;
    public MyInfoWindow(Context context, Map<Marker, POI> poiResolver) {
        infoWindow = LayoutInflater.from(context).inflate(R.layout.view_info_window, null);
        titleView = (TextView)infoWindow.findViewById(R.id.text_title);
        snippetView = (TextView)infoWindow.findViewById(R.id.text_snippet);
        mPOIResolver = poiResolver;
    }
    @Override
    public View getInfoWindow(Marker marker) {

        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        POI poi = mPOIResolver.get(marker);
        titleView.setText(poi.name);
        snippetView.setText(poi.getAddress());
        return infoWindow;
    }
}
