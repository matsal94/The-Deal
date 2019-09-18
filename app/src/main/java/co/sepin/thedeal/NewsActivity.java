package co.sepin.thedeal;

import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import co.sepin.thedeal.adapter.NewsAdapter;
import co.sepin.thedeal.application.DrawerClass;
import co.sepin.thedeal.fragment.NewsCategoryFragment;
import co.sepin.thedeal.interfaces.InternetChangeListener;
import co.sepin.thedeal.receiver.InternetChangeReceiver;


public class NewsActivity extends DrawerClass {

    private NewsCategoryFragment newsGeneralFragment, newsBusinessFragment, newsEntertainmentFragment;
    private NewsCategoryFragment newsHealthFragment, newsScienceFragment, newsSportsFragment, newsTechnologyFragment;
    private InternetChangeReceiver internetChangeReceiver;
    private TabLayout newsTabLayout;
    private ViewPager newsViewPager;
    private boolean isRegister = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        newsTabLayout = (TabLayout) findViewById(R.id.newsTL);
        newsViewPager = (ViewPager) findViewById(R.id.newsVP);

        initToolbar();
        setupViewPager();
    }


    @Override
    public int getContentView() {
        return R.layout.activity_news;
    }


    @Override
    public void onStop() {
        if (isRegister)
            unregisterBroadcast();

        super.onStop();
    }


    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.newsToolbar);
        setSupportActionBar(toolbar);

        initDrawer(toolbar);
    }


    private void setupViewPager() {

        newsGeneralFragment = NewsCategoryFragment.newInstance("general");
        newsBusinessFragment = NewsCategoryFragment.newInstance("business");
        newsEntertainmentFragment = NewsCategoryFragment.newInstance("entertainment");
        newsHealthFragment = NewsCategoryFragment.newInstance("health");
        newsScienceFragment = NewsCategoryFragment.newInstance("science");
        newsSportsFragment = NewsCategoryFragment.newInstance("sports");
        newsTechnologyFragment = NewsCategoryFragment.newInstance("technology");

        NewsAdapter newsAdapter = new NewsAdapter(getSupportFragmentManager());
        newsAdapter.addFragment(newsGeneralFragment, getString(R.string.general));
        newsAdapter.addFragment(newsBusinessFragment, getString(R.string.business));
        newsAdapter.addFragment(newsEntertainmentFragment, getString(R.string.entertainment));
        newsAdapter.addFragment(newsHealthFragment, getString(R.string.health));
        newsAdapter.addFragment(newsScienceFragment, getString(R.string.science));
        newsAdapter.addFragment(newsSportsFragment, getString(R.string.sports));
        newsAdapter.addFragment(newsTechnologyFragment, getString(R.string.technology));

        newsViewPager.setAdapter(newsAdapter);
        newsViewPager.setOffscreenPageLimit(6); // N-1
        newsTabLayout.setupWithViewPager(newsViewPager);

        registerBroadcast();

        InternetChangeReceiver.bindNewsListener(new InternetChangeListener() {

            @Override
            public void internetConnected() {

                newsGeneralFragment.getNewsInformation();
                newsBusinessFragment.getNewsInformation();
                newsEntertainmentFragment.getNewsInformation();
                newsHealthFragment.getNewsInformation();
                newsScienceFragment.getNewsInformation();
                newsSportsFragment.getNewsInformation();
                newsTechnologyFragment.getNewsInformation();
            }

            @Override
            public void noInternetConnected() {

                newsGeneralFragment.noInternetConnection();
                newsBusinessFragment.noInternetConnection();
                newsEntertainmentFragment.noInternetConnection();
                newsHealthFragment.noInternetConnection();
                newsScienceFragment.noInternetConnection();
                newsSportsFragment.noInternetConnection();
                newsTechnologyFragment.noInternetConnection();
            }
        });
    }

    private void registerBroadcast() {

        InternetChangeReceiver.isNews = true;
        InternetChangeReceiver.isProfil = false;
        InternetChangeReceiver.isSelectContacts = false;
        InternetChangeReceiver.isWeather = false;

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

            InternetChangeReceiver.isNews = false;
            InternetChangeReceiver.bindNewsListener(null);
            unregisterReceiver(internetChangeReceiver);
            isRegister = false;
        }
    }
}
