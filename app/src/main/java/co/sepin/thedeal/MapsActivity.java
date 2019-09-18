package co.sepin.thedeal;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import co.sepin.thedeal.application.ModeClass;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 12f;
    private static final float LOCATION_ZOOM = 16f;

    private GoogleMap mMap;
    private int markerCount = 0;
    private EditText searchET;
    private Boolean locationPermissionsGranted = false;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private List<Address> address = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        searchET = (EditText) findViewById(R.id.map_searchET);

        //SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getLocationPermission();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in_activity, R.anim.fade_out_activity);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        if (locationPermissionsGranted) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            getDeviceLocation(DEFAULT_ZOOM);

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }

        mMap.setBuildingsEnabled(true);
        mMap.setTrafficEnabled(false);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                addMarker(point);
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        locationPermissionsGranted = false;

        switch (requestCode) {

            case LOCATION_PERMISSION_REQUEST_CODE:

                if (grantResults.length > 0) {

                    for (int i = 0; i < grantResults.length; i++)
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {

                            locationPermissionsGranted = false;
                            return;
                        }

                    locationPermissionsGranted = true;
                    initMap();
                }
        }
    }


    private void getLocationPermission() {

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationPermissionsGranted = true;
                initMap();
            } else
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        } else
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
    }


    private void getDeviceLocation(final float zoom) {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (locationPermissionsGranted) {

                final Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (task.isSuccessful()) {

                            Location currentLocation = (Location) task.getResult();
                            if (currentLocation != null)
                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), zoom, getString(R.string.my_localization));
                        } else
                            Toast.makeText(MapsActivity.this, getString(R.string.not_locate_position), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (SecurityException e) {
            Toast.makeText(MapsActivity.this, getString(R.string.not_locate_position) + " " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void initMap() {

        //SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);

        searchET.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {

                    searchLocalization();
                    return true;
                }
                return false;
            }
        });

        hideSoftKeyboard();
    }


    private void searchLocalization() {

        if (ModeClass.checkInternetConnection()) {

            mMap.clear();
            markerCount++;
            geoLocate();
            hideSoftKeyboard();
        } else {

            hideSoftKeyboard();
            Toast.makeText(MapsActivity.this, getString(R.string.no_Internet_connection), Toast.LENGTH_SHORT).show();
        }
    }


    private void moveCamera(LatLng latLng, float zoom, String title) {

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!title.equals(getString(R.string.my_localization))) {

            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options.draggable(true));
        }
    }


    private void geoLocate() {

        String searchString = searchET.getText().toString();

        try {

            Geocoder geocoder = new Geocoder(MapsActivity.this);
            address = geocoder.getFromLocationName(searchString, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (address.size() > 0) {

            float zoom = mMap.getCameraPosition().zoom;
            moveCamera(new LatLng(address.get(0).getLatitude(), address.get(0).getLongitude()), zoom, address.get(0).getAddressLine(0));
        }
    }


    private void geoLocateFromMarker(LatLng point) {

        try {

            Geocoder geocoder = new Geocoder(MapsActivity.this);
            address = geocoder.getFromLocation(point.latitude, point.longitude, 1);

            if (address.size() > 0) {

                moveCamera(point, LOCATION_ZOOM, address.get(0).getAddressLine(0));
                searchET.setText(address.get(0).getAddressLine(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void addMarker(LatLng point) {

        if (ModeClass.checkInternetConnection()) {


            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(point).draggable(true));
            markerCount++;
            geoLocateFromMarker(point);
        } else
            Toast.makeText(MapsActivity.this, getString(R.string.no_Internet_connection), Toast.LENGTH_SHORT).show();
    }


    public void onClickSearch(View view) {
        searchLocalization();
    }


    public void onClickPosition(View view) {

        //getDeviceLocation(zoom);
        getLocationPermission();
    }


    public void onClickSave(View view) {

        if (markerCount != 0) {

            try {

                SharedPreferences.Editor editor = getSharedPreferences("Map", MODE_PRIVATE).edit();
                editor.putBoolean("map", true);
                editor.putString("address", address.get(0).getAddressLine(0));
                putDouble(editor, "lat", address.get(0).getLatitude());
                putDouble(editor, "lon", address.get(0).getLongitude());
                editor.apply();

                finish();
            } catch (Exception e) {

                Toast.makeText(MapsActivity.this, getString(R.string.not_locate_marker_position), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else
            Toast.makeText(MapsActivity.this, getString(R.string.no_select_position), Toast.LENGTH_SHORT).show();

        overridePendingTransition(R.anim.fade_in_activity, R.anim.fade_out_activity);
    }


    SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }


    @Override
    public void onMarkerDragStart(Marker marker) {
    }


    @Override
    public void onMarkerDrag(Marker marker) {
    }


    @Override
    public void onMarkerDragEnd(Marker marker) {
        addMarker(marker.getPosition());
    }


    private void hideSoftKeyboard() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchET.getWindowToken(), 0);
    }
}
