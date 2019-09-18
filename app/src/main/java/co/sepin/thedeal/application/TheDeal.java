package co.sepin.thedeal.application;

import android.os.Handler;
import android.os.StrictMode;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import static co.sepin.thedeal.application.UpdateData.firebaseUser;
import static co.sepin.thedeal.application.UpdateData.updateUserStatus;


public class TheDeal extends android.app.Application {


    @Override
    public void onCreate() {
        super.onCreate();

        FlowManager.init(new FlowConfig.Builder(this).build());

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                if (firebaseUser != null /*&& checkInternetConnection()*/)
                    updateUserStatus("Online");

                handler.postDelayed(this, 60 * 1000);
            }
        }, 60 * 1000);
    }
}