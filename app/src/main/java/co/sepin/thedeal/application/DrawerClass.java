package co.sepin.thedeal.application;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import co.sepin.thedeal.BusinessCardHolderActivity;
import co.sepin.thedeal.CalendarActivity;
import co.sepin.thedeal.MessageActivity;
import co.sepin.thedeal.NewsActivity;
import co.sepin.thedeal.ProfilActivity;
import co.sepin.thedeal.R;
import co.sepin.thedeal.WeatherActivity;
import co.sepin.thedeal.database.User;
import co.sepin.thedeal.database.User_Table;
import de.hdodenhof.circleimageview.CircleImageView;

import static co.sepin.thedeal.application.UpdateData.firebaseUser;


public abstract class DrawerClass extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private CircleImageView imageCIV;
    private TextView nameTV;
    private boolean isDrawer2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout mainLayout;

        if (getContentView() == R.layout.activity_weather || getContentView() == R.layout.activity_news) {

            isDrawer2 = true;

            setContentView(R.layout.activity_drawer2);
            navigationView = (NavigationView) findViewById(R.id.drawer2NV);
            mainLayout = (FrameLayout) findViewById(R.id.drawer2FL);
        } else {

            isDrawer2 = false;

            setContentView(R.layout.activity_drawer);
            Toolbar toolbar = (Toolbar) findViewById(R.id.drawerToolbar);
            setSupportActionBar(toolbar);

            initDrawer(toolbar);

            navigationView = (NavigationView) findViewById(R.id.drawerNV);
            mainLayout = (FrameLayout) findViewById(R.id.drawerFL);
        }

        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        imageCIV = (CircleImageView) headerView.findViewById(R.id.drawerCIV);
        nameTV = (TextView) headerView.findViewById(R.id.drawer_nameTV);
        TextView telephoneTV = (TextView) headerView.findViewById(R.id.drawer_telephoneTV);

        loadAvatar();
        displayName();
        displayTelephone(telephoneTV);

        //FrameLayout mainLayout = (FrameLayout) findViewById(R.id.drawerFL);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(getContentView(), mainLayout, true);
    }


    public abstract int getContentView();


    @Override
    protected void onResume() {
        super.onResume();

        selectMenuItem(getLocalClassName());
        if (getContentView() == R.layout.activity_main) {

            loadAvatar();
            displayName();
        }
    }


    @Override
    public void onBackPressed() {

        if (isDrawer2)
            drawer = (DrawerLayout) findViewById(R.id.drawer2_layout);
        else
            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings)
            return true;

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.menu_home:
                if (getContentView() == R.layout.activity_main)
                    break;
                else {
                    selectMenuItem("MainActivity");
                    finish();
                }
                break;

            case R.id.menu_message:
                if (getContentView() == R.layout.activity_message)
                    break;
                else {
                    if (getContentView() != R.layout.activity_main)
                        finish();

                    selectMenuItem("MessageActivity");
                    Intent mess = new Intent(DrawerClass.this, MessageActivity.class);
                    startActivity(mess);
                }
                break;

            case R.id.menu_calendar:
                if (getContentView() == R.layout.activity_calendar)
                    break;
                else {
                    if (getContentView() != R.layout.activity_main)
                        finish();

                    selectMenuItem("CalendarActivity");
                    Intent cale = new Intent(DrawerClass.this, CalendarActivity.class);
                    startActivity(cale);
                }
                break;

            case R.id.menu_businesscardholder:
                if (getContentView() == R.layout.activity_businesscardholder)
                    break;
                else {
                    if (getContentView() != R.layout.activity_main)
                        finish();

                    selectMenuItem("BusinessCardHolderActivity");
                    Intent busi = new Intent(DrawerClass.this, BusinessCardHolderActivity.class);
                    startActivity(busi);
                }
                break;

            case R.id.menu_weather:
                if (getContentView() == R.layout.activity_weather)
                    break;
                else {
                    if (getContentView() != R.layout.activity_main)
                        finish();

                    selectMenuItem("WeatherActivity");
                    Intent weat = new Intent(DrawerClass.this, WeatherActivity.class);
                    startActivity(weat);
                }
                break;

            case R.id.menu_news:
                if (getContentView() == R.layout.activity_news)
                    break;
                else {
                    if (getContentView() != R.layout.activity_main)
                        finish();

                    selectMenuItem("NewsActivity");
                    Intent news = new Intent(DrawerClass.this, NewsActivity.class);
                    startActivity(news);
                }
                break;

            case R.id.menu_profil:
                if (getContentView() == R.layout.activity_profil)
                    break;
                else {
                    if (getContentView() != R.layout.activity_main)
                        finish();

                    selectMenuItem("ProfilActivity");
                    Intent prof = new Intent(DrawerClass.this, ProfilActivity.class);
                    startActivity(prof);
                }
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void initDrawer(Toolbar toolbar) {

        if (isDrawer2)
            drawer = (DrawerLayout) findViewById(R.id.drawer2_layout);
        else
            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }


    public void selectMenuItem(String className) {

        switch (className) {
            case "MainActivity":
                navigationView.getMenu().getItem(0).setChecked(true);
                break;
            case "MessageActivity":
                navigationView.getMenu().getItem(1).setChecked(true);
                break;
            case "CalendarActivity":
                navigationView.getMenu().getItem(2).setChecked(true);
                break;
            case "BusinessCardHolderActivity":
                navigationView.getMenu().getItem(3).setChecked(true);
                break;
            case "WeatherActivity":
                navigationView.getMenu().getItem(4).setChecked(true);
                break;
            case "NewsActivity":
                navigationView.getMenu().getItem(5).setChecked(true);
                break;
            case "ProfilActivity":
                navigationView.getMenu().getItem(6).setChecked(true);
                break;
        }
    }


    public void loadAvatar() {

        String avatarUri = "";

        try {
            avatarUri = SQLite.select().from(User.class).where(User_Table.userId.eq(firebaseUser.getUid())).querySingle().getAvatarUri();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Glide.with(this)
                .load(avatarUri)
/*                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Delete.table(User.class);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })*/
                .error(Glide.with(this).load(R.drawable.user_profil))
                .into(imageCIV);
    }


    public void displayName() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String name = preferences.getString("settings_user_name","");

        if (name.equals(""))
            nameTV.setVisibility(View.GONE);
        else {

            nameTV.setVisibility(View.VISIBLE);
            nameTV.setText(name);
        }
    }


    private void displayTelephone(TextView telephoneTV) {

        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = tm.getNetworkCountryIso();//getSimCountryIso();
        String formattedNumber = PhoneNumberUtils.formatNumber(firebaseUser.getPhoneNumber(), countryCode);

        telephoneTV.setText(formattedNumber);
    }
}