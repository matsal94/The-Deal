package co.sepin.thedeal.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.HashSet;
import java.util.Set;

public class NetworkStateReceiver extends BroadcastReceiver {

    protected Set<NetworkStateReceiverListener> listeners;
    protected Boolean isConnected;

    public NetworkStateReceiver() {

        listeners = new HashSet<NetworkStateReceiverListener>();
        isConnected = null;
    }

    public void onReceive(Context context, Intent intent) {

        if (intent == null || intent.getExtras() == null)
            return;

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
            isConnected = true;
        } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
            isConnected = false;
        }

        notifyStateToAll();
    }

    private void notifyStateToAll() {

        for (NetworkStateReceiverListener listener : listeners)
            notifyState(listener);
    }

    private void notifyState(NetworkStateReceiverListener listener) {

        if (isConnected == null || listener == null)
            return;

        if (isConnected == true)
            listener.networkAvailable();
        else
            listener.networkUnavailable();
    }

    public void addListener(NetworkStateReceiverListener l) {

        listeners.add(l);
        notifyState(l);
    }

    public void removeListener(NetworkStateReceiverListener l) {
        listeners.remove(l);
    }

    public interface NetworkStateReceiverListener {

        public void networkAvailable();

        public void networkUnavailable();
    }
}