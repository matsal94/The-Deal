package co.sepin.thedeal;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import co.sepin.thedeal.application.ModeClass;


public class EventDetailsActivity extends ModeClass implements OnMapReadyCallback {

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final float LOCATION_ZOOM = 14f;
    private static final float DEFAULT_ZOOM = 10f;
    private Animation animShow, animHide;
    private MapView mapView;
    private GoogleMap gMap;
    private LatLng placePosition;
    private ConstraintLayout backgroundCL;
    private TextView nameTV, dateTV;
    private MaterialEditText addressMET, descriptionMET;
    private String address;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_event_details);

        animShow = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_button);
        animHide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.hide_button);

        backgroundCL = (ConstraintLayout) findViewById(R.id.event_detail_backgroundLayout);
        nameTV = (TextView) findViewById(R.id.event_detail_nameTV);
        dateTV = (TextView) findViewById(R.id.event_detail_dateTV);
        addressMET = (MaterialEditText) findViewById(R.id.event_detail_addressMET);
        descriptionMET = (MaterialEditText) findViewById(R.id.event_detail_descriptionMET);

        initMapView(savedInstanceState);
        initValues();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {

            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }


    @Override
    protected void onResume() {
        super.onResume();

        mapView.onResume();
        mapView.setAnimation(animShow);
        mapView.startAnimation(animShow);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }


    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }


    @Override
    protected void onPause() {
        super.onPause();

        mapView.onPause();
        mapView.setAnimation(animHide);
        mapView.startAnimation(animHide);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        gMap = googleMap;
        gMap.setMinZoomPreference(DEFAULT_ZOOM);

        UiSettings uiSettings = gMap.getUiSettings();
        uiSettings.setRotateGesturesEnabled(true);
        uiSettings.setScrollGesturesEnabled(false);
        uiSettings.setTiltGesturesEnabled(false);
        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setZoomControlsEnabled(true);

        geoLocate();
    }


    private void initMapView(Bundle savedInstanceState) {

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = findViewById(R.id.event_detail_mapView);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }


    private void initValues() {

        Bundle extras = getIntent().getBundleExtra("Event Details");

        String name = extras.getString("Name");
        String dateTime = extras.getString("Date");
        address = extras.getString("Address");
        double lat = extras.getDouble("Lat");
        double lon = extras.getDouble("Lon");
        String description = extras.getString("Description");
        String comment = extras.getString("Comment");
        int color = extras.getInt("Color");


        backgroundCL.setBackgroundColor(color);
        nameTV.setText(name);
        placePosition = new LatLng(lat, lon);
        setDateTime(dateTime);
        setDescription(description);
        setAddressAndComment(comment);
    }


    private void setDateTime(String dateTime) {

        try {

            Date date = dateFormat.parse(dateTime);
            dateTV.setText(dateFormatForDay.format(date));
        } catch (ParseException e) {

            e.printStackTrace();
            dateTV.setText(dateTime);
        }
    }


    private void setAddressAndComment(String comment) {

        if (comment != null && !comment.isEmpty()) {

            addressMET.setText(new StringBuilder().append(address).append(" (").append(comment).append(")"));

            if (address == null || address.isEmpty())
                mapView.setVisibility(View.GONE);
        } else if (address == null || address.isEmpty()) {

            mapView.setVisibility(View.GONE);
            addressMET.setVisibility(View.GONE);
        } else {

            mapView.setVisibility(View.VISIBLE);
            addressMET.setVisibility(View.VISIBLE);
            addressMET.setText(address);
        }
    }


    private void setDescription(String description) {

        if (description == null || description.isEmpty())
            descriptionMET.setVisibility(View.GONE);
        else
            descriptionMET.setText(description);
    }


    private void geoLocate() {

        try {
/*
            Geocoder geocoder = new Geocoder(EventDetailsActivity.this); // z geocoderem
            String searchString = addressTV.getText().toString();
            List<Address> address;
            address = geocoder.getFromLocationName(searchString, 1);

            if (address.size() > 0)
                moveCamera(new LatLng(address.get(0).getLatitude(), address.get(0).getLongitude()), LOCATION_ZOOM, address.get(0).getAddressLine(0));
*/
            moveCamera(placePosition, LOCATION_ZOOM, address); // bez geocodera
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void moveCamera(LatLng latLng, float zoom, String title) {

        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        MarkerOptions options = new MarkerOptions().position(latLng).title(title);
        gMap.addMarker(options);
    }
}