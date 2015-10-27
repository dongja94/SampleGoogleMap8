package com.example.dongja94.samplegooglemap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMapClickListener,
    GoogleMap.OnMarkerDragListener, GoogleMap.OnCameraChangeListener {

    private GoogleMap mMap;
    LocationManager mLM;
    final Map<POI, Marker> mMarkerResolver = new HashMap<POI, Marker>();
    final Map<Marker, POI> mPOIResolver = new HashMap<Marker, POI>();
    ListView listView;
    EditText keywordVIew;
    ArrayAdapter<POI> mAdapter;
    RadioGroup group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        listView = (ListView)findViewById(R.id.listView);
        group = (RadioGroup)findViewById(R.id.group);
        keywordVIew = (EditText)findViewById(R.id.edit_keyword);
        mAdapter = new ArrayAdapter<POI>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                POI poi = (POI)listView.getItemAtPosition(position);
                Marker m = mMarkerResolver.get(poi);
                moveMap(m);
            }
        });
//        SupportMapFragment smf = null;
//        if (savedInstanceState == null) {
//            smf = new SupportMapFragment();
//            getSupportFragmentManager().beginTransaction().add(R.id.container, smf, "map").commit();
//        } else {
//            smf = (SupportMapFragment) getSupportFragmentManager().findFragmentByTag("map");
//        }
//
//        smf.getMapAsync(this);

        mLM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Button btn = (Button)findViewById(R.id.btn_marker);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MarkerOptions options = new MarkerOptions();
                CameraPosition position = mMap.getCameraPosition();
                options.position(position.target);
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                options.anchor(0.5f, 1);
                POI poi = new POI();

                poi.name = "My Marker";
                poi.upperAddrName = "Marker Test...";

                options.title(poi.name);
                options.snippet(poi.getAddress());
                options.draggable(true);

                Marker m = mMap.addMarker(options);

                mMarkerResolver.put(poi, m);
                mPOIResolver.put(m, poi);
            }
        });

        btn = (Button)findViewById(R.id.btn_search);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = keywordVIew.getText().toString();
                if (!TextUtils.isEmpty(keyword)) {
                    NetworkManager.getInstance().findPOI(MapsActivity.this, keyword, new NetworkManager.OnResultListener<SearchPOIInfo>() {
                        @Override
                        public void onSuccess(SearchPOIInfo result) {
                            clearAll();
                            for (POI poi : result.pois.poilist) {
                                mAdapter.add(poi);
                                addMarker(poi);
                            }
                            if (result.pois.poilist.size() > 0) {
                                moveMap(result.pois.poilist.get(0).getLatitude(), result.pois.poilist.get(0).getLongitude());
                            }
                        }

                        @Override
                        public void onFail(int code) {

                        }
                    });
                }
            }
        });

        btn = (Button)findViewById(R.id.btn_route);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (start != null && end != null) {
                    NetworkManager.getInstance().findPath(MapsActivity.this, start, end, new NetworkManager.OnResultListener<CarRouteInfo>() {
                        @Override
                        public void onSuccess(CarRouteInfo result) {
                            if (result.features != null && result.features.size() > 0) {
                                int totalDistance = result.features.get(0).properties.totalDistance;
                                int totalTime = result.features.get(0).properties.totalTime;
                                int totalFare = result.features.get(0).properties.totalFare;
                                Toast.makeText(MapsActivity.this, "total : " + totalDistance + "," + totalTime, Toast.LENGTH_SHORT).show();
                            }

                            if (result.features != null && result.features.size() > 0) {
                                PolylineOptions options = new PolylineOptions();
                                for (CarFeature feature : result.features) {
                                    if (feature.geometry.type.equals("LineString")) {
                                        double[] coord = feature.geometry.coordinates;
                                        for (int i = 0; i < coord.length; i+= 2) {
                                            options.add(new LatLng(coord[i+1],coord[i]));
                                        }
                                    }
                                }
                                options.color(Color.RED);
                                options.width(10);
                                mMap.addPolyline(options);
                            }


                            start = null;
                            end = null;
                        }

                        @Override
                        public void onFail(int code) {
                            start = null;
                            end = null;
                        }
                    });
                }
            }
        });
    }

    private void moveMap(final Marker marker) {
        CameraUpdate update = CameraUpdateFactory.newLatLng(marker.getPosition());
        mMap.animateCamera(update, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                marker.showInfoWindow();
            }

            @Override
            public void onCancel() {

            }
        });

    }

    private void clearAll() {
        for (int i = 0; i < mAdapter.getCount(); i++) {
            POI poi = mAdapter.getItem(i);
            Marker m = mMarkerResolver.get(poi);
            mMarkerResolver.remove(m);
            mPOIResolver.remove(poi);
            m.remove();
        }

        mAdapter.clear();
    }

    private void addMarker(POI poi) {
        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(poi.getLatitude(), poi.getLongitude()));
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        options.anchor(0.5f, 1);

        options.title(poi.name);
        options.snippet(poi.getAddress());
        options.draggable(true);

        Marker m = mMap.addMarker(options);

        mMarkerResolver.put(poi, m);
        mPOIResolver.put(m, poi);
    }

    LocationListener mListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (mMap != null) {
                moveMap(location.getLatitude(), location.getLongitude());
            } else {
                cacheLocation = location;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    Location cacheLocation;
    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT > 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLM.requestSingleUpdate(LocationManager.GPS_PROVIDER, mListener, null);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT > 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLM.removeUpdates(mListener);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.setMyLocationEnabled(true);

        mMap.setIndoorEnabled(true);
        mMap.setTrafficEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerDragListener(this);

        mMap.setInfoWindowAdapter(new MyInfoWindow(this, mPOIResolver));

        if (cacheLocation != null) {
            moveMap(cacheLocation.getLatitude(), cacheLocation.getLongitude());
            cacheLocation = null;
        }
    }

    LatLng start, end;

    @Override
    public void onInfoWindowClick(Marker marker) {
//        Toast.makeText(this, "infowindow : " + marker.getTitle() , Toast.LENGTH_SHORT).show();
        marker.hideInfoWindow();
        switch (group.getCheckedRadioButtonId()) {
            case R.id.radio_start :
                start = marker.getPosition();
                Toast.makeText(this, "set start", Toast.LENGTH_SHORT).show();
                break;
            case R.id.radio_end :
                end = marker.getPosition();
                Toast.makeText(this, "set end", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        POI poi = mPOIResolver.get(marker);
        Toast.makeText(this, "title : " + poi.name , Toast.LENGTH_SHORT).show();
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng latLng = marker.getPosition();
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        mMap.getProjection();
    }

    private void moveMap(double lat, double lng) {
        CameraPosition.Builder builder = new CameraPosition.Builder();
        builder.target(new LatLng(lat, lng));
        builder.zoom(16);
//        builder.bearing(30);
//        builder.tilt(30);
        CameraPosition position = builder.build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
//        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 10);
        mMap.animateCamera(update);
    }
}
