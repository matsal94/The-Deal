package co.sepin.thedeal;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import co.sepin.thedeal.fragment.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.settingsTlb);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        if (findViewById(R.id.fragment_containerFL) != null) {

            if (savedInstanceState != null)
                return;

            getFragmentManager().beginTransaction().add(R.id.fragment_containerFL, new SettingsFragment()).commit();
        }

        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, false);
    }
}