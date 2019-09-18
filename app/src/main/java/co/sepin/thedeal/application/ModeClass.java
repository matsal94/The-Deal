package co.sepin.thedeal.application;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import co.sepin.thedeal.R;


@SuppressLint("Registered")
public class ModeClass extends AppCompatActivity {

    public static final SimpleDateFormat dateFormatForDay = new SimpleDateFormat("EEEE, d MMMM HH:mm");
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    public static final String WEATHER_KEY = "d902ffaaccc913f387dab8925aa6eb57"; //weather
    public static final String NEWS_KEY = "9d6d95efda80429b8fcb2802ff756e29"; //news
    public static final int TYPE_WIFI = 1;
    public static final int TYPE_MOBILE = 2;
    public static final int TYPE_NOT_CONNECTED = 0;
    public static final int NETWORK_STATUS_NOT_CONNECTED = 0;
    public static final int NETWORK_STATUS_WIFI = 1;
    public static final int NETWORK_STATUS_MOBILE = 2;
    public static Location currentLocation = null; // weather


    public static int getConnectivityStatus(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (null != activeNetwork) {

            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }


    public static int getConnectivityStatusString(Context context) {

        int conn = ModeClass.getConnectivityStatus(context);
        int status;
        //int status = 0;

        if (conn == ModeClass.TYPE_WIFI)
            status = NETWORK_STATUS_WIFI;
        else if (conn == ModeClass.TYPE_MOBILE)
            status = NETWORK_STATUS_MOBILE;
        else //if (conn == ModeClass.TYPE_NOT_CONNECTED)
            status = NETWORK_STATUS_NOT_CONNECTED;

        return status;
    }


    public static String formatTelephoneNumber(Context context, String phoneNumber) {

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = telephonyManager.getSimCountryIso();
        phoneNumber = PhoneNumberUtils.formatNumber(phoneNumber, countryCode);
        return phoneNumber;
    }

    public static String convertUnixToDate(long dt) {

        Date date = new Date(dt * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm EEEE, d MMMM yyyy");
        String formatted = sdf.format(date);
        return formatted;
    }

    public static String convertUnixToDate1(Context context, long dt) {

        //Date date = new Date(dt * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy, HH:mm");
        String formatted = sdf.format(dt);

        if (formatted.substring(0, 10).equals(getCurrentDate2().substring(0, 10)))
            return context.getResources().getString(R.string.today) + " " + formatted.substring(12);
        else
            return formatted;
    }


    public static String convertUnixToDate2(Context context, long dt) {

        Date date = new Date(dt * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm EEEE, d MMMM");
        String formatted = sdf.format(date);

        if (formatted.substring(7).equals(getCurrentDate().substring(7)))
            return formatted.substring(0, 6) + context.getResources().getString(R.string.today);
        else
            return formatted;
    }


    public static String convertUnixToTime(long dt) {

        Date date = new Date(dt * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String formatted = sdf.format(date);
        return formatted;
    }


    public static int convertUnixToHour(long dt) {

        Date date = new Date(dt * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        String formatted = sdf.format(date);
        return Integer.parseInt(formatted);
    }


    public static int convertUnixToMinute(long dt) {

        Date date = new Date(dt * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("mm");
        String formatted = sdf.format(date);
        return Integer.parseInt(formatted);
    }


    public static String convertOneDecimalPlace(double temp) {

        DecimalFormat df = new DecimalFormat("0.0");
        return df.format(temp);
    }

    public static String convertTwoDecimalPlace(double temp) {

        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(temp);
    }


    public static int convertDistance(int meters) {
        return meters / 1000;
    }


    public static String getCurrentDate() {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm EEEE, d MMMM");
        String formatted = sdf.format(calendar.getTime());
        return formatted;
    }


    public static String getCurrentDate2() {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy, HH:mm");
        String formatted = sdf.format(calendar.getTime());
        return formatted;
    }


    public static int getCurrentHour() {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        String formatted = sdf.format(calendar.getTime());
        return Integer.parseInt(formatted);
    }


    public static int getCurrentMinute() {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("mm");
        String formatted = sdf.format(calendar.getTime());
        return Integer.parseInt(formatted);
    }


    public boolean checkVibrationMode() {

        AudioManager audio = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        if (audio.getRingerMode() == 2 && AudioManager.RINGER_MODE_VIBRATE == 1)
            return true;
        return audio.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE;
    }


    public static boolean checkInternetConnection() {
        Log.d("NetworkChangeReceiver", "ModeClass");

        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }


    public void hideKeyboardAndFocus(final View view, final AppCompatActivity activity) {

        if (!(view instanceof EditText))
            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {

                    hideSoftKeyboard(activity, view);
                    v.requestFocus();
                    return false;
                }
            });

        if (this.getCurrentFocus() != null)
            if (view instanceof ViewGroup)
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                    View innerView = ((ViewGroup) view).getChildAt(i);
                    hideKeyboardAndFocus(innerView, activity);
                }
    }


    public static void hideSoftKeyboard(Activity activity, View view) {

        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE);
        //inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public void onClickClose(View view) {
        onBackPressed();
    }
}
