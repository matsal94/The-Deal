package co.sepin.thedeal;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import gr.net.maroulis.library.EasySplashScreen;


public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*
        EasySplashScreen config = new EasySplashScreen(SplashScreenActivity.this)
                .withFullScreen()
                .withTargetActivity(RegistryActivity.class)
                .withSplashTimeOut(1500)
                .withAfterLogoText("The Deal")
                .withBackgroundColor(getResources().getColor(R.color.primary_dark))
                .withLogo(R.mipmap.ic_the_deal);

        View easySplashScreen = config.create();
        setContentView(easySplashScreen);*/

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {

                Intent intent = new Intent(SplashScreenActivity.this, RegistryActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in_activity, R.anim.fade_out_activity);
                finish();
            }
        }, 100);

    }
}