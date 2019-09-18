package co.sepin.thedeal.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;

import co.sepin.thedeal.R;

public class SettingsFragment extends PreferenceFragment {

    private static final String USER_NAME = "settings_user_name";
    private static final String WEATHER_LOCALIZATION = "settings_weather_localization";
    private static final String WEATHER_SCALE = "settings_weather_scale";
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_settings);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                switch (key) {

                    case USER_NAME:

                        Preference userNamePref = findPreference(key);
                        userNamePref.setSummary(sharedPreferences.getString(key, ""));
                        break;

                    case WEATHER_LOCALIZATION:

                        Preference weatherLocPref = findPreference(key);
                        weatherLocPref.setShouldDisableView(sharedPreferences.getBoolean(key, true));
                        break;

                    case WEATHER_SCALE:

                        Preference weatherScalePref = findPreference(key);
                        weatherScalePref.setSummary(changeValueWeatherScale(getPreferenceScreen().getSharedPreferences().getString(WEATHER_SCALE, "")));
                        break;
                }
            }
        };
    }


    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        Preference userNamePref = findPreference(USER_NAME);
        userNamePref.setSummary(getPreferenceScreen().getSharedPreferences().getString(USER_NAME, ""));

        Preference weatherLocPref = findPreference(WEATHER_LOCALIZATION);
        weatherLocPref.setShouldDisableView(getPreferenceScreen().getSharedPreferences().getBoolean(WEATHER_LOCALIZATION, true));

        Preference weatherScalePref = findPreference(WEATHER_SCALE);
        weatherScalePref.setSummary(changeValueWeatherScale(getPreferenceScreen().getSharedPreferences().getString(WEATHER_SCALE, "")));
    }


    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }


    private String changeValueWeatherScale(String value) {

        switch (value) {

            case "Metric":
                return "Celsius";

            case "Imperial":
                return "Fahrenheit";

            default:
                return "Kelvin";
        }
    }
}