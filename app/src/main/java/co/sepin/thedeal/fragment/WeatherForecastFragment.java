package co.sepin.thedeal.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

import co.sepin.thedeal.R;
import co.sepin.thedeal.adapter.WeatherForecastAdapter;
import co.sepin.thedeal.api.RetrofitUtils2;
import co.sepin.thedeal.api.WeatherApi;
import co.sepin.thedeal.application.ModeClass;
import co.sepin.thedeal.interfaces.HidingScrollListener;
import co.sepin.thedeal.model.WeatherForecastResult;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


public class WeatherForecastFragment extends Fragment {

    private CompositeDisposable compositeDisposable;
    private ConstraintLayout forecastLayout, forecastNoInternetConnectionLayout;
    private ProgressBar forecastLoadingPB;
    private WeatherApi service;
    private RecyclerView forecastRecyclerView;
    private TabLayout weatherTabLayout;
    private AppBarLayout weatherAppBarLayout;
    private Toolbar weatherToolbar;
    private String title, subtitle, unit;
    private boolean isToolbarUp;


    public WeatherForecastFragment() {
    }


    public static WeatherForecastFragment newInstance() {
        return new WeatherForecastFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View itemView = inflater.inflate(R.layout.fragment_weather_forecast, container, false);

        forecastLayout = (ConstraintLayout) itemView.findViewById(R.id.weather_forecastCL1);
        forecastNoInternetConnectionLayout = (ConstraintLayout) itemView.findViewById(R.id.weather_forecastCL0);
        forecastLoadingPB = (ProgressBar) itemView.findViewById(R.id.weather_forecastPB);
        forecastRecyclerView = (RecyclerView) itemView.findViewById(R.id.weather_forecastRV);

        weatherToolbar = (Toolbar) getActivity().findViewById(R.id.weatherToolbar);
        weatherAppBarLayout = (AppBarLayout) getActivity().findViewById(R.id.weatherAPL);
        weatherTabLayout = (TabLayout) getActivity().findViewById(R.id.weatherTL);

        //forecastLayout.setBackgroundResource(R.drawable.weather_night_background);

        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitUtils2.getInstance();
        service = retrofit.create(WeatherApi.class);

        initRecyclerView();

        return itemView;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        unit = getWeatherScale();
    }


    @Override
    public void onResume() {
        super.onResume();

        if (isToolbarUp) {

            showViews();
            HidingScrollListener.controlsVisible = true;
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        compositeDisposable.clear();
    }


    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);

        if (visible && isResumed()) {

            if (title != null && subtitle != null) {

                weatherToolbar.setTitle(title);
                weatherToolbar.setSubtitle(subtitle);
            }
        } else {

            if (isToolbarUp) {

                showViews();
                HidingScrollListener.controlsVisible = true;
            }
        }
    }


    private void initRecyclerView() {

        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        boolean isBigTablet = getResources().getBoolean(R.bool.isBigTablet);

        if (isTablet || isBigTablet)
            forecastRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2, RecyclerView.VERTICAL, false));
        else
            forecastRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        forecastRecyclerView.setHasFixedSize(true);
        forecastRecyclerView.setOnScrollListener(new HidingScrollListener() {

            @Override
            public void onHide() {
                hideViews();
            }

            @Override
            public void onShow() {
                showViews();
            }
        });
    }


    public void getForecastInformation() {

        compositeDisposable.add(service.getForecastWeatherByLatLng(
                String.valueOf(ModeClass.currentLocation.getLatitude()), //String.valueOf(53.021707),
                String.valueOf(ModeClass.currentLocation.getLongitude()), //String.valueOf(18.609144),
                ModeClass.WEATHER_KEY, unit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherForecastResult>() {

                    @Override
                    public void accept(WeatherForecastResult weatherForecastResult) throws Exception {

                        displayForecastResult(weatherForecastResult);

                        forecastLayout.setVisibility(View.VISIBLE);
                        forecastNoInternetConnectionLayout.setVisibility(View.GONE);
                        forecastLoadingPB.setVisibility(View.GONE);
                    }
                }, new Consumer<Throwable>() {

                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        System.out.println("WeatherForecastFragment: " + throwable);
                    }
                })
        );
    }


    public void getCityForecastInformation(String city) {

        compositeDisposable.add(service.getForecastWeatherByCityName(city, ModeClass.WEATHER_KEY, unit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherForecastResult>() {

                    @Override
                    public void accept(WeatherForecastResult weatherForecastResult) throws Exception {

                        displayForecastResult(weatherForecastResult);

                        forecastLayout.setVisibility(View.VISIBLE);
                        forecastNoInternetConnectionLayout.setVisibility(View.GONE);
                        forecastLoadingPB.setVisibility(View.GONE);
                    }
                }, new Consumer<Throwable>() {

                    @Override
                    public void accept(Throwable throwable) throws Exception {

                        forecastLoadingPB.setVisibility(View.GONE);
                        System.out.println("WeatherForecastFragment: " + throwable);
                    }
                })
        );
    }


    private void displayForecastResult(WeatherForecastResult weatherForecastResult) {

        title = weatherForecastResult.city.name + ", " + weatherForecastResult.city.country;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (preferences.getBoolean("settings_weather_localization", true))
            subtitle = String.valueOf(weatherForecastResult.city.coord);
        else
            subtitle = "";

        WeatherForecastAdapter adapter = new WeatherForecastAdapter(getContext(), weatherForecastResult, unit);
        forecastRecyclerView.setAdapter(adapter);
    }


    private String getWeatherScale() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return unit = preferences.getString("settings_weather_scale", "");
    }


    private void hideViews() {

        isToolbarUp = true;
        weatherAppBarLayout.animate().translationY(-weatherAppBarLayout.getBottom()).setInterpolator(new AccelerateInterpolator(3)).start();
        weatherTabLayout.animate().translationY(-weatherAppBarLayout.getBottom()).setInterpolator(new AccelerateInterpolator(2)).start();
    }


    private void showViews() {

        isToolbarUp = false;
        weatherAppBarLayout.animate().translationY(0).setInterpolator(new DecelerateInterpolator(3));
        weatherTabLayout.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }
}