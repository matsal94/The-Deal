package co.sepin.thedeal.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.github.pwittchen.weathericonview.WeatherIconView;

import co.sepin.thedeal.R;
import co.sepin.thedeal.api.RetrofitUtils2;
import co.sepin.thedeal.api.WeatherApi;
import co.sepin.thedeal.application.ModeClass;
import co.sepin.thedeal.model.WeatherResult;
import co.sepin.thedeal.model.WeatherUvi;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import xyz.matteobattilana.library.Common.Constants;
import xyz.matteobattilana.library.WeatherView;

import static android.content.Context.MODE_PRIVATE;


public class WeatherTodayFragment extends Fragment {

    private ImageView todayWindDirectionIV;
    private TextView todayCityTV, todayTemperatureTV, todayDescriptionTV, todayDateTV;
    private TextView todayWindTV, todayWindDirectionTV, todayPressureTV, todayHumidityTV;
    private TextView todayUvTV, todayVisibilityTV, todaySunriseTV, todaySunsetTV;
    private ProgressBar todayLoadingPB;
    private ConstraintLayout todayLayout, todayNoInternetConnectionLayout;
    private CompositeDisposable compositeDisposable;
    private WeatherApi service;
    private WeatherIconView todayIconView;
    private WeatherView todayView;
    private Toolbar weatherToolbar;
    private String unit;
    private boolean isTablet, isBigTablet;


    public WeatherTodayFragment() {
    }


    public static WeatherTodayFragment newInstance() {
        return new WeatherTodayFragment();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitUtils2.getInstance();
        service = retrofit.create(WeatherApi.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View itemView = inflater.inflate(R.layout.fragment_weather_today, container, false);

        todayView = (WeatherView) itemView.findViewById(R.id.weather_today_ANIM);
        todayWindDirectionIV = (ImageView) itemView.findViewById(R.id.weather_today_wind_directionIV);
        todayIconView = (WeatherIconView) itemView.findViewById(R.id.weather_today_iconWIV);
        todayCityTV = (TextView) itemView.findViewById(R.id.weather_today_cityTV);
        todayTemperatureTV = (TextView) itemView.findViewById(R.id.weather_today_temperatureTV);
        todayDescriptionTV = (TextView) itemView.findViewById(R.id.weather_today_descriptionTV);
        todayDateTV = (TextView) itemView.findViewById(R.id.weather_today_dateTV);
        todayWindTV = (TextView) itemView.findViewById(R.id.weather_today_windTV);
        todayWindDirectionTV = (TextView) itemView.findViewById(R.id.weather_today_wind_directionTV);
        todayPressureTV = (TextView) itemView.findViewById(R.id.weather_today_pressureTV);
        todayHumidityTV = (TextView) itemView.findViewById(R.id.weather_today_humidityTV);
        todayVisibilityTV = (TextView) itemView.findViewById(R.id.weather_today_visibilityTV);
        todaySunriseTV = (TextView) itemView.findViewById(R.id.weather_today_sunriseTV);
        todaySunsetTV = (TextView) itemView.findViewById(R.id.weather_today_sunsetTV);
        todayUvTV = (TextView) itemView.findViewById(R.id.weather_today_uvTV);
        todayLayout = (ConstraintLayout) itemView.findViewById(R.id.weather_todayCL);
        todayNoInternetConnectionLayout = (ConstraintLayout) itemView.findViewById(R.id.weather_todayCL0);
        todayLoadingPB = (ProgressBar) itemView.findViewById(R.id.weather_todayPB);

        weatherToolbar = (Toolbar) getActivity().findViewById(R.id.weatherToolbar);

        return itemView;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isTablet = getResources().getBoolean(R.bool.isTablet);
        isBigTablet = getResources().getBoolean(R.bool.isBigTablet);
        unit = getWeatherScale();
    }


    @Override
    public void onPause() {
        super.onPause();
        todayView.stopAnimation();
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

            weatherToolbar.setTitle(getResources().getString(R.string.title_activity_weather));
            weatherToolbar.setSubtitle("");
        }
    }


    public void getWeatherInformation() {

        compositeDisposable.add(service.getWeatherByLatLng(String.valueOf(ModeClass.currentLocation.getLatitude()),
                String.valueOf(ModeClass.currentLocation.getLongitude()),
                ModeClass.WEATHER_KEY, unit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new io.reactivex.functions.Consumer<WeatherResult>() {

                    @Override
                    public void accept(WeatherResult weatherResult) throws Exception {

                        setWeatherIcon(weatherResult);
                        setAnimation(weatherResult.getWeather().get(0).getId(), weatherResult.getWeather().get(0).getDescription());
                        todayWindDirectionIV.setRotation((weatherResult.getWind().getDeg() + 180 % 360));

                        todayCityTV.setText(new StringBuilder(String.valueOf(weatherResult.getName())).append(", ").append(weatherResult.getSys().getCountry()));

                        switch (unit) {

                            case "Metric":
                                todayTemperatureTV.setText(new StringBuilder(String.valueOf(ModeClass.convertOneDecimalPlace(weatherResult.getMain().getTemp()))).append("°C").toString());
                                break;

                            case "Imperial":
                                todayTemperatureTV.setText(new StringBuilder(String.valueOf(ModeClass.convertOneDecimalPlace(weatherResult.getMain().getTemp()))).append("°F").toString());
                                break;

                            default:
                                todayTemperatureTV.setText(new StringBuilder(String.valueOf(ModeClass.convertOneDecimalPlace(weatherResult.getMain().getTemp()))).append("K").toString());

                        }
                        try {

                            int descriptionId = getResources().getIdentifier(weatherResult.getWeather().get(0).getDescription()
                                    .replaceAll(" ", "_")
                                    .replaceAll("-", "_")
                                    .replaceAll("/", "_")
                                    .replaceAll("%", "")
                                    .replaceAll(":", ""), "string", getActivity().getPackageName());
                            todayDescriptionTV.setText(getString(descriptionId));
                        } catch (Exception e) {
                            todayDescriptionTV.setText(weatherResult.getWeather().get(0).getDescription());
                        }

                        todayDateTV.setText(ModeClass.convertUnixToDate(weatherResult.getDt()));
                        todayWindTV.setText(new StringBuilder(String.valueOf(weatherResult.getWind().getSpeed())).append(" m/s").toString());
                        todayWindDirectionTV.setText(setWindDirection(weatherResult.getWind().getDeg()));
                        todayPressureTV.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getPressure())).append(" hPa").toString());
                        todayHumidityTV.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getHumidity())).append(" %").toString());
                        todayVisibilityTV.setText(new StringBuilder(String.valueOf(ModeClass.convertDistance(weatherResult.getVisibility()))).append(" km").toString());
                        todaySunriseTV.setText(ModeClass.convertUnixToTime(weatherResult.getSys().getSunrise()));
                        todaySunsetTV.setText(ModeClass.convertUnixToTime(weatherResult.getSys().getSunset()));

                        todayLayout.setVisibility(View.VISIBLE);
                        todayNoInternetConnectionLayout.setVisibility(View.GONE);
                        todayLoadingPB.setVisibility(View.GONE);
                    }

                }, new io.reactivex.functions.Consumer<Throwable>() {

                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(getActivity(), R.string.not_Internet_connection, Toast.LENGTH_LONG).show();
                    }
                })
        );

        compositeDisposable.add(service.getUviWeatherByLatLng(ModeClass.WEATHER_KEY,
                String.valueOf(ModeClass.currentLocation.getLatitude()),
                String.valueOf(ModeClass.currentLocation.getLongitude()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new io.reactivex.functions.Consumer<WeatherUvi>() {

                    @Override
                    public void accept(WeatherUvi weatherUvi) throws Exception {
                        todayUvTV.setText(String.valueOf(weatherUvi.getValue()));
                    }

                }, new io.reactivex.functions.Consumer<Throwable>() {

                    @Override
                    public void accept(Throwable throwable) throws Exception {
                    }
                })
        );
    }


    public void getCityWeatherInformation(String city) {

        compositeDisposable.add(service.getWeatherByCityName(city, ModeClass.WEATHER_KEY, unit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new io.reactivex.functions.Consumer<WeatherResult>() {

                    @Override
                    public void accept(WeatherResult weatherResult) throws Exception {

                        setWeatherIcon(weatherResult);
                        setAnimation(weatherResult.getWeather().get(0).getId(), weatherResult.getWeather().get(0).getDescription());
                        todayWindDirectionIV.setRotation((weatherResult.getWind().getDeg() + 180 % 360));

                        todayCityTV.setText(new StringBuilder(String.valueOf(weatherResult.getName())).append(", ").append(weatherResult.getSys().getCountry()));

                        switch (unit) {

                            case "Metric":
                                todayTemperatureTV.setText(new StringBuilder(String.valueOf(ModeClass.convertOneDecimalPlace(weatherResult.getMain().getTemp()))).append("°C").toString());
                                break;

                            case "Imperial":
                                todayTemperatureTV.setText(new StringBuilder(String.valueOf(ModeClass.convertOneDecimalPlace(weatherResult.getMain().getTemp()))).append("°F").toString());
                                break;

                            default:
                                todayTemperatureTV.setText(new StringBuilder(String.valueOf(ModeClass.convertOneDecimalPlace(weatherResult.getMain().getTemp()))).append("K").toString());

                        }
                        try {

                            int descriptionId = getResources().getIdentifier(weatherResult.getWeather().get(0).getDescription()
                                    .replaceAll(" ", "_")
                                    .replaceAll("-", "_")
                                    .replaceAll("/", "_")
                                    .replaceAll("%", "")
                                    .replaceAll(":", ""), "string", getActivity().getPackageName());
                            todayDescriptionTV.setText(getString(descriptionId));
                        } catch (Exception e) {
                            todayDescriptionTV.setText(weatherResult.getWeather().get(0).getDescription());
                        }

                        todayDateTV.setText(ModeClass.convertUnixToDate(weatherResult.getDt()));
                        todayWindTV.setText(new StringBuilder(String.valueOf(weatherResult.getWind().getSpeed())).append(" m/s").toString());
                        todayWindDirectionTV.setText(setWindDirection(weatherResult.getWind().getDeg()));
                        todayPressureTV.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getPressure())).append(" hPa").toString());
                        todayHumidityTV.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getHumidity())).append(" %").toString());
                        todayVisibilityTV.setText(new StringBuilder(String.valueOf(ModeClass.convertDistance(weatherResult.getVisibility()))).append(" km").toString());
                        todaySunriseTV.setText(ModeClass.convertUnixToTime(weatherResult.getSys().getSunrise()));
                        todaySunsetTV.setText(ModeClass.convertUnixToTime(weatherResult.getSys().getSunset()));

                        todayLayout.setVisibility(View.VISIBLE);
                        todayNoInternetConnectionLayout.setVisibility(View.GONE);
                        todayLoadingPB.setVisibility(View.GONE);
                    }
                }, new io.reactivex.functions.Consumer<Throwable>() {

                    @Override
                    public void accept(Throwable throwable) throws Exception {

                        System.out.println("WEATHER: " + throwable.getMessage() + ", " + throwable.toString() + ", " + throwable + ", " + throwable.getLocalizedMessage());

                        if (throwable.getMessage().equals("HTTP 404 Not Found")) {
                            Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
                            todayLoadingPB.setVisibility(View.GONE);
                        }
                        else
                            Toast.makeText(getActivity(), R.string.not_Internet_connection, Toast.LENGTH_SHORT).show();
                    }
                })
        );

        todayUvTV.setText("-");
/*
        compositeDisposable.add(service.getUviWeatherByCityName(ModeClass.WEATHER_KEY, city)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new io.reactivex.functions.Consumer<WeatherUvi>() {

                    @Override
                    public void accept(WeatherUvi weatherUvi) throws Exception {
                        todayUvTV.setText(String.valueOf(weatherUvi.getValue()));
                    }

                }, new io.reactivex.functions.Consumer<Throwable>() {

                    @Override
                    public void accept(Throwable throwable) throws Exception {
                    }
                })
        );*/
    }


    private String getWeatherScale() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return unit = preferences.getString("settings_weather_scale", "");
    }


    private void setWeatherIcon(WeatherResult weatherResult) {

        String iconName;
        int sunriseHour = ModeClass.convertUnixToHour(weatherResult.getSys().getSunrise());
        int sunriseMinute = ModeClass.convertUnixToMinute(weatherResult.getSys().getSunrise());
        int sunsetHour = ModeClass.convertUnixToHour(weatherResult.getSys().getSunset());
        int sunsetMinute = ModeClass.convertUnixToMinute(weatherResult.getSys().getSunset());
        int currentHour = ModeClass.getCurrentHour();
        int currentMinute = ModeClass.getCurrentMinute();

        SharedPreferences.Editor weatherEditor = getActivity().getSharedPreferences("Weather", MODE_PRIVATE).edit();
        weatherEditor.putInt("sunriseHour", sunriseHour);
        weatherEditor.putInt("sunsetHour", sunsetHour);
        weatherEditor.apply();

        if (isBigTablet)
            todayIconView.setIconSize(280);
        else if (isTablet)
            todayIconView.setIconSize(200);
        else
            todayIconView.setIconSize(110);

        setBackground(weatherResult);

        if (currentHour >= sunriseHour && currentHour <= sunsetHour) {

            if (currentHour > sunriseHour && currentHour < sunsetHour)
                iconName = new StringBuilder("wi_owm_day_").append(weatherResult.getWeather().get(0).getId()).toString();
            else if (currentHour == sunriseHour && currentMinute >= sunriseMinute || currentHour == sunsetHour && currentMinute <= sunsetMinute)
                iconName = new StringBuilder("wi_owm_day_").append(weatherResult.getWeather().get(0).getId()).toString();
            else
                iconName = new StringBuilder("wi_owm_night_").append(weatherResult.getWeather().get(0).getId()).toString();
            //todayLayout.setBackground(getResources().getDrawable(R.drawable.weather_night_background));
        } else
            iconName = new StringBuilder("wi_owm_night_").append(weatherResult.getWeather().get(0).getId()).toString();
        //todayLayout.setBackground(getResources().getDrawable(R.drawable.weather_night_background));

        int iconId = getResources().getIdentifier(iconName, "string", getActivity().getPackageName());
        todayIconView.setIconResource(getString(iconId));
    }


    private void setBackground(WeatherResult weatherResult) {

        switch (unit) {

            case "Metric":
                if (weatherResult.getMain().getTemp() <= 0)
                    todayLayout.setBackground(getResources().getDrawable(R.drawable.weather_day_background));
                else if (weatherResult.getMain().getTemp() > 0 && weatherResult.getMain().getTemp() <= 10)
                    todayLayout.setBackground(getResources().getDrawable(R.drawable.weather_day_background1));
                else if (weatherResult.getMain().getTemp() > 10 && weatherResult.getMain().getTemp() <= 20)
                    todayLayout.setBackground(getResources().getDrawable(R.drawable.weather_day_background2));
                else if (weatherResult.getMain().getTemp() > 20 && weatherResult.getMain().getTemp() <= 30)
                    todayLayout.setBackground(getResources().getDrawable(R.drawable.weather_day_background3));
                else
                    todayLayout.setBackground(getResources().getDrawable(R.drawable.weather_day_background4));
                break;

            case "Imperial":
                if (weatherResult.getMain().getTemp() <= 32)
                    todayLayout.setBackground(getResources().getDrawable(R.drawable.weather_day_background));
                else if (weatherResult.getMain().getTemp() > 32 && weatherResult.getMain().getTemp() <= 50)
                    todayLayout.setBackground(getResources().getDrawable(R.drawable.weather_day_background1));
                else if (weatherResult.getMain().getTemp() > 50 && weatherResult.getMain().getTemp() <= 68)
                    todayLayout.setBackground(getResources().getDrawable(R.drawable.weather_day_background2));
                else if (weatherResult.getMain().getTemp() > 68 && weatherResult.getMain().getTemp() <= 86)
                    todayLayout.setBackground(getResources().getDrawable(R.drawable.weather_day_background3));
                else
                    todayLayout.setBackground(getResources().getDrawable(R.drawable.weather_day_background4));
                break;

            default:
                if (weatherResult.getMain().getTemp() <= 273)
                    todayLayout.setBackground(getResources().getDrawable(R.drawable.weather_day_background));
                else if (weatherResult.getMain().getTemp() > 273 && weatherResult.getMain().getTemp() <= 283)
                    todayLayout.setBackground(getResources().getDrawable(R.drawable.weather_day_background1));
                else if (weatherResult.getMain().getTemp() > 283 && weatherResult.getMain().getTemp() <= 293)
                    todayLayout.setBackground(getResources().getDrawable(R.drawable.weather_day_background2));
                else if (weatherResult.getMain().getTemp() > 293 && weatherResult.getMain().getTemp() <= 303)
                    todayLayout.setBackground(getResources().getDrawable(R.drawable.weather_day_background3));
                else
                    todayLayout.setBackground(getResources().getDrawable(R.drawable.weather_day_background4));
        }
    }


    private void setAnimation(int id, String description) {

        if ((id >= 500 && id <= 531) || (id >= 300 && id <= 321) || (id >= 200 && id <= 232)) {

            if (description.contains("light")) {

                todayView.setWeather(Constants.WeatherStatus.RAIN)
                        .setRainAngle(-5)
                        .setRainFadeOutTime(500)
                        .setFPS(30)
                        .setRainParticles(40)
                        .setRainTime(1000)
                        .startAnimation();
            } else if (description.contains("heavy") || description.contains("extreme")) {

                todayView.setWeather(Constants.WeatherStatus.RAIN)
                        .setRainAngle(-5)
                        .setRainFadeOutTime(2500)
                        .setFPS(100)
                        .setRainParticles(80)
                        .setRainTime(4000)
                        .startAnimation();
            } else {

                todayView.setWeather(Constants.WeatherStatus.RAIN)
                        .setRainAngle(-5)
                        .setRainFadeOutTime(1500)
                        .setFPS(60)
                        .setRainParticles(60)
                        .setRainTime(2000)
                        .startAnimation();
            }
        } else if (id >= 600 && id <= 622) {

            if (description.contains("light")) {

                todayView.setWeather(Constants.WeatherStatus.SNOW)
                        .setSnowAngle(-5)
                        .setSnowFadeOutTime(500)
                        .setFPS(30)
                        .setSnowParticles(20)
                        .setSnowTime(1500)
                        .startAnimation();
            } else if (description.contains("heavy")) {

                todayView.setWeather(Constants.WeatherStatus.SNOW)
                        .setSnowAngle(-5)
                        .setSnowFadeOutTime(1500)
                        .setFPS(80)
                        .setSnowParticles(60)
                        .setSnowTime(4000)
                        .startAnimation();
            } else {

                todayView.setWeather(Constants.WeatherStatus.SNOW)
                        .setSnowAngle(-5)
                        .setSnowFadeOutTime(1000)
                        .setFPS(60)
                        .setSnowParticles(50)
                        .setSnowTime(4000)
                        .startAnimation();
            }
        } else
            todayView.setWeather(Constants.WeatherStatus.SUN)
                    .startAnimation();
    }


    private String setWindDirection(float deg) {

        if (deg >= 23 && deg < 68) return getActivity().getString(R.string.north_east);
        else if (deg >= 68 && deg < 113) return getActivity().getString(R.string.east);
        else if (deg >= 113 && deg < 158) return getActivity().getString(R.string.south_east);
        else if (deg >= 158 && deg < 203) return getActivity().getString(R.string.south);
        else if (deg >= 203 && deg < 248) return getActivity().getString(R.string.south_west);
        else if (deg >= 248 && deg < 293) return getActivity().getString(R.string.west);
        else if (deg >= 293 && deg < 338) return getActivity().getString(R.string.north_west);
        else return getActivity().getString(R.string.north);
    }
}