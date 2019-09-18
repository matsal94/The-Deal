package co.sepin.thedeal;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.google.android.material.snackbar.Snackbar;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import co.sepin.thedeal.application.ModeClass;


public class WeatherMapsActivity extends ModeClass implements OnMapReadyCallback {

    private static final float DEFAULT_ZOOM = 4f;
    private GoogleMap mMap;
    private TileOverlay tileOver;
    private Boolean locationPermissionsGranted = false;
    private String tileType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_weather_maps);
        Spinner weatherMapSpn = (Spinner) findViewById(R.id.weather_mapSpinner);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.weather_map);
        mapFragment.getMapAsync(this);

        String[] tileName = new String[]{getString(R.string.clouds).toUpperCase(), getString(R.string.temperature).toUpperCase(),
                getString(R.string.precipitations).toUpperCase(), getString(R.string.snow).toUpperCase(),
                getString(R.string.wind).toUpperCase(), getString(R.string.pressure).toUpperCase()};

        ArrayAdapter spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, tileName);
        weatherMapSpn.setAdapter(spinnerAdapter);
        weatherMapSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onNothingSelected(AdapterView parent) {
            }

            @Override
            public void onItemSelected(AdapterView parent, View view, int position, long id) {

                switch (position) {

                    case 0:
                        tileType = "clouds_new";
                        break;
                    case 1:
                        tileType = "temp_new";
                        break;
                    case 2:
                        tileType = "precipitation_new";
                        break;
                    case 3:
                        tileType = "snow_new";
                        break;
                    case 4:
                        tileType = "wind_new";
                        break;
                    case 5:
                        tileType = "pressure_new";
                        break;
                }

                if (mMap != null) {

                    tileOver.remove();
                    addTileToMap();
                }
            }
        });
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

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setCompassEnabled(false);
        }

        if (currentLocation != null)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM));
        else
            Toast.makeText(WeatherMapsActivity.this, getString(R.string.permission_localization_denied), Toast.LENGTH_LONG).show();

        addTileToMap();
    }


    private void addTileToMap() {

        TileProvider tileProvider = new UrlTileProvider(256, 256) {

            @Override
            public URL getTileUrl(int x, int y, int zoom) {

                String url = String.format(Locale.US, "https://tile.openweathermap.org/map/%s/%d/%d/%d.png?appid=%s", tileType, zoom, x, y, ModeClass.WEATHER_KEY);

                try {
                    return new URL(url);
                } catch (MalformedURLException e) {
                    throw new AssertionError(e);
                }
            }
        };
        tileOver = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));
    }
}
