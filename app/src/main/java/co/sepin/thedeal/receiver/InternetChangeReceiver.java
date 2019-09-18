package co.sepin.thedeal.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Observable;

import co.sepin.thedeal.application.UpdateData;
import co.sepin.thedeal.interfaces.InternetChangeListener;
import co.sepin.thedeal.application.ModeClass;

public class InternetChangeReceiver extends BroadcastReceiver {

    private static InternetChangeListener internetChangeListener;
    private static InternetChangeListener internetChangeListener2;
    private static InternetChangeListener internetChangeListener3;
    private static InternetChangeListener internetChangeListener4;

    public static boolean isNews;
    public static boolean isProfil;
    public static boolean isSelectContacts;
    public static boolean isWeather;


    @Override
    public void onReceive(final Context context, final Intent intent) {

        Log.d("News", "Connection status changed");
        getObservable().connectionChanged(context, intent);
    }


    public static NetworkObservable getObservable() {
        return NetworkObservable.getInstance();
    }


    public static class NetworkObservable extends Observable {

        private static NetworkObservable instance = null;


        public static NetworkObservable getInstance() {

            if (instance == null)
                instance = new NetworkObservable();

            return instance;
        }


        private NetworkObservable() {
            // Exist to defeat instantiation.
        }


        private void connectionChanged(Context context, final Intent intent) {

            int status = ModeClass.getConnectivityStatusString(context);

            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {

                if (status == ModeClass.NETWORK_STATUS_NOT_CONNECTED) {

                    if (internetChangeListener != null && isNews)
                        internetChangeListener.noInternetConnected();
                    else if (internetChangeListener2 != null && isProfil)
                        internetChangeListener2.noInternetConnected();
                    else if (internetChangeListener3 != null && isSelectContacts)
                        internetChangeListener3.noInternetConnected();
                    else if (internetChangeListener4 != null && isWeather)
                        internetChangeListener4.noInternetConnected();

                } else {

                    //new ResumeForceExitPause(context).execute();
                    if (internetChangeListener != null && isNews)
                        internetChangeListener.internetConnected();
                    else if (internetChangeListener2 != null && isProfil)
                        internetChangeListener2.internetConnected();
                    else if (internetChangeListener3 != null && isSelectContacts)
                        internetChangeListener3.internetConnected();
                    else if (internetChangeListener4 != null && isWeather)
                        internetChangeListener4.internetConnected();
                }
            }
        }
    }


    public static void bindNewsListener(InternetChangeListener listener) {
        internetChangeListener = listener;
    }


    public static void bindProfilListener(InternetChangeListener listener) {
        internetChangeListener2 = listener;
    }


    public static void bindSelectContactsListener(InternetChangeListener listener) {
        internetChangeListener3 = listener;
    }


    public static void bindWeatherListener(InternetChangeListener listener) {
        internetChangeListener4 = listener;
    }
}

