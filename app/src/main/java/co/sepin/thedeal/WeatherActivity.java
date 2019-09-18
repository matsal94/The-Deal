package co.sepin.thedeal;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.label305.asynctask.SimpleAsyncTask;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

import co.sepin.thedeal.adapter.WeatherAdapter;
import co.sepin.thedeal.application.DrawerClass;
import co.sepin.thedeal.application.ModeClass;
import co.sepin.thedeal.fragment.WeatherForecastFragment;
import co.sepin.thedeal.fragment.WeatherTodayFragment;
import co.sepin.thedeal.interfaces.InternetChangeListener;
import co.sepin.thedeal.receiver.InternetChangeReceiver;


public class WeatherActivity extends DrawerClass {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private WeatherTodayFragment weatherTodayFragment;
    private WeatherForecastFragment weatherForecastFragment;
    private MaterialSearchView materialSearchView;
    private ConstraintLayout mainLayout, weatherNoInternetConnectionLayout, forecastNoInternetConnectionLayout, weatherLayout, forecastLayout;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private InternetChangeReceiver internetChangeReceiver;
    private ProgressBar weatherLoadingPB, forecastLoadingPB;
    protected String city;
    private boolean isRegister = false, isWeatherLocalization;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainLayout = (ConstraintLayout) findViewById(R.id.weatherLayout);
        materialSearchView = (MaterialSearchView) findViewById(R.id.weatherMSV);

        initToolbar();
        initValuesFromSP();

        if (isWeatherLocalization)
            getLocationPermission();
        else {

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    new LoadCities().execute();
                    setupViewPager();
                    initInternetListener();
                }
            }, 1);
        }
    }


    @Override
    public int getContentView() {
        return R.layout.activity_weather;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isRegister)
            unregisterBroadcast();
    }


    @Override
    public void onBackPressed() {

        if (materialSearchView.isSearchOpen())
            materialSearchView.closeSearch();
        else
            super.onBackPressed();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.weather_menu, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);

        if (isWeatherLocalization)
            searchItem.setVisible(false);
        else {

            searchItem.setVisible(true);
            materialSearchView.setMenuItem(searchItem);
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_map:

                Intent intent = new Intent(WeatherActivity.this, WeatherMapsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in_activity, R.anim.fade_out_activity);
                return true;

            case R.id.action_search:

                materialSearchView.setEnabled(true);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case LOCATION_PERMISSION_REQUEST_CODE:

                if (grantResults.length > 0) {

                    for (int grantResult : grantResults)
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {

                            //locationPermissionsGranted = false;
                            Snackbar.make(mainLayout, getString(R.string.permission_localization_denied), Snackbar.LENGTH_LONG).show();
                            return;
                        }

                    getLastPosition();
                }
        }
    }


    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.weatherToolbar);
        setSupportActionBar(toolbar);

        initDrawer(toolbar);
    }


    private void initValuesFromSP() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isWeatherLocalization = preferences.getBoolean("settings_weather_localization", true);

        preferences = getSharedPreferences("Settings", MODE_PRIVATE);
        city = preferences.getString("City", "London,GB");
    }


    private void getLocationPermission() {

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getLastPosition();
            } else
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        } else
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
    }


    private void getLastPosition() {

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {

                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                        if (report.areAllPermissionsGranted()) {

                            buildLocationRequest();
                            buildLocationCallBack();

                            if (ActivityCompat.checkSelfPermission(WeatherActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                    && ActivityCompat.checkSelfPermission(WeatherActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                                return;

                            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(WeatherActivity.this);
                            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

/*
                            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                            if (ModeClass.currentLocation == null) {
                                //if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                                //if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){

                                //snackbar = Snackbar.make(constraintLayout, "Brak włączonych usług lokalizacyjnych", Snackbar.LENGTH_INDEFINITE);
                                snackbar = Snackbar.make(constraintLayout, "Nie można uzyskać lokalizacji urządzenia", Snackbar.LENGTH_INDEFINITE);
                                snackbar.show();
                            }*/
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                    }
                }).check();
    }


    private void buildLocationRequest() {

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(7000);
        //locationRequest.setExpirationDuration(10000); // czas ważności
    }


    private void buildLocationCallBack() {

        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                ModeClass.currentLocation = locationResult.getLastLocation();

                setupViewPager();
                initInternetListener();
            }
        };
    }


    private void setupViewPager() {

        ViewPager viewPager = (ViewPager) findViewById(R.id.weatherVP);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.weatherTL);

        weatherTodayFragment = WeatherTodayFragment.newInstance();
        weatherForecastFragment = WeatherForecastFragment.newInstance();

        WeatherAdapter weatherAdapter = new WeatherAdapter(getSupportFragmentManager());
        weatherAdapter.addFragment(weatherTodayFragment, getString(R.string.now));
        weatherAdapter.addFragment(weatherForecastFragment, getString(R.string.five_days));

        viewPager.setAdapter(weatherAdapter);
        tabLayout.setupWithViewPager(viewPager);

        weatherNoInternetConnectionLayout = (ConstraintLayout) findViewById(R.id.weather_todayCL0);
        forecastNoInternetConnectionLayout = (ConstraintLayout) findViewById(R.id.weather_forecastCL0);
        weatherLayout = (ConstraintLayout) findViewById(R.id.weather_todayCL);
        forecastLayout = (ConstraintLayout) findViewById(R.id.weather_forecastCL1);
        weatherLoadingPB = (ProgressBar) findViewById(R.id.weather_todayPB);
        forecastLoadingPB = (ProgressBar) findViewById(R.id.weather_forecastPB);
    }


    private class LoadCities extends SimpleAsyncTask<List<String>> {

        @Override
        protected List<String> doInBackgroundSimple() {

            List<String> listCities = new ArrayList<>();

            try {

                StringBuilder stringBuilder = new StringBuilder();
                InputStream inputStream = getResources().openRawResource(R.raw.city_list);
                GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);

                InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String reader;
                while ((reader = bufferedReader.readLine()) != null) {

                    stringBuilder.append(reader);
                    listCities = new Gson().fromJson(stringBuilder.toString(), new TypeToken<List<String>>() {
                    }.getType());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return listCities;
        }

        @Override
        protected void onSuccess(List<String> listCity) {
            super.onSuccess(listCity);

            String[] suggestions = listCity.toArray(new String[0]);
            String[] continents = {"Africa,", "Antarctica,", "Asia,", "Europe,", "North America,", "Oceania,", "South America,"};

            materialSearchView.setEllipsize(true);
            materialSearchView.setEnabled(false);
            materialSearchView.setSuggestions(suggestions);
            materialSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String query) {

                    if (materialSearchView.isSearchOpen())
                        materialSearchView.closeSearch();

                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });

            materialSearchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    for (String continent : continents)
                        if (adapterView.getItemAtPosition(i).equals(continent)) {

                            Toast.makeText(WeatherActivity.this, getString(R.string.not_city), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    materialSearchView.dismissSuggestions();
                    materialSearchView.closeSearch();

                    saveCity(adapterView.getItemAtPosition(i).toString());
                }
            });
        }
    }


    private void saveCity(String cityName) {

        SharedPreferences.Editor settingsEditor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        settingsEditor.putString("City", cityName);
        settingsEditor.apply();
        city = cityName;

        initInternetListener();
    }


    private void initInternetListener() {

        if (!isRegister)
            registerBroadcast();

        InternetChangeReceiver.bindWeatherListener(new InternetChangeListener() {

            @Override
            public void internetConnected() {

                if (weatherLayout == null && forecastLayout == null)
                    return;

                if (isWeatherLocalization) {

                    weatherTodayFragment.getWeatherInformation();
                    weatherForecastFragment.getForecastInformation();
                } else {

                    weatherTodayFragment.getCityWeatherInformation(city);
                    weatherForecastFragment.getCityForecastInformation(city);
                }

                if (weatherLayout.getVisibility() == View.VISIBLE)
                    weatherLayout.setVisibility(View.VISIBLE);

                if (forecastLayout.getVisibility() == View.VISIBLE)
                    forecastLayout.setVisibility(View.VISIBLE);

                weatherNoInternetConnectionLayout.setVisibility(View.GONE);
                weatherLoadingPB.setVisibility(View.VISIBLE);

                forecastNoInternetConnectionLayout.setVisibility(View.GONE);
                forecastLoadingPB.setVisibility(View.VISIBLE);

                if (isRegister)
                    unregisterBroadcast();
            }

            @Override
            public void noInternetConnected() {

                if (weatherLayout == null && forecastLayout == null)
                    return;

                if (Objects.requireNonNull(weatherLayout).getVisibility() != View.VISIBLE) {

                    weatherNoInternetConnectionLayout.setVisibility(View.VISIBLE);
                    weatherLoadingPB.setVisibility(View.GONE);
                }

                if (forecastLayout.getVisibility() != View.VISIBLE) {

                    forecastNoInternetConnectionLayout.setVisibility(View.VISIBLE);
                    forecastLoadingPB.setVisibility(View.GONE);
                }
            }
        });
    }


    private void registerBroadcast() {

        InternetChangeReceiver.isNews = false;
        InternetChangeReceiver.isProfil = false;
        InternetChangeReceiver.isSelectContacts = false;
        InternetChangeReceiver.isWeather = true;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        //intentFilter.setPriority(100);

        internetChangeReceiver = new InternetChangeReceiver();
        registerReceiver(internetChangeReceiver, intentFilter);

        isRegister = true;
    }


    private void unregisterBroadcast() {

        if (isRegister) {

            InternetChangeReceiver.isWeather = false;
            InternetChangeReceiver.bindWeatherListener(null);
            unregisterReceiver(internetChangeReceiver);

            isRegister = false;
        }
    }
}