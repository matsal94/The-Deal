package co.sepin.thedeal;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.master.glideimageview.GlideImageView;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.sepin.thedeal.adapter.DealAdapter;
import co.sepin.thedeal.application.ModeClass;
import co.sepin.thedeal.database.Deal;
import co.sepin.thedeal.database.Deal_Table;

import static co.sepin.thedeal.application.UpdateData.sortDealsList;


public class ContactActivity extends ModeClass {

    private static final int CALL_PERMISSIONS_REQUEST = 1;
    private Animation animShow, animHide;
    private GlideImageView avatarGIV;
    private ImageButton shareEmailIB;
    private DealAdapter dealAdapter;
    private RecyclerView recyclerView;
    private MaterialEditText aboutMeMET, telephoneMET, emailMET;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private String telNumber, userAvatarUri, userId, userName, userTelephone;
    private List<Deal> dealsList = new ArrayList<>();
    private boolean isCollapsed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contact);

        avatarGIV = (GlideImageView) findViewById(R.id.contact_avatarGIV);
        aboutMeMET = (MaterialEditText) findViewById(R.id.contact_about_meMET);
        telephoneMET = (MaterialEditText) findViewById(R.id.contact_telephoneMET);
        emailMET = (MaterialEditText) findViewById(R.id.contact_emailMET);
        shareEmailIB = (ImageButton) findViewById(R.id.contact_share_emailIB);
        recyclerView = (RecyclerView) findViewById(R.id.contactRV);

        animShow = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_button);
        animHide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.hide_button);

        initValues();
        initToolbar();
        initCollapsingToolbar();
        setAvatar();
        formatNumber();
        checkIsEmailEmpty();
        getDealsFromDB();
        initRecyclerView();
    }


    @Override
    protected void onResume() {
        super.onResume();

        aboutMeMET.setAnimation(animShow);
        aboutMeMET.startAnimation(animShow);
        telephoneMET.setAnimation(animShow);
        telephoneMET.startAnimation(animShow);
        emailMET.setAnimation(animShow);
        emailMET.startAnimation(animShow);
        shareEmailIB.setAnimation(animShow);
        shareEmailIB.startAnimation(animShow);
    }


    @Override
    public void onBackPressed() {

        if (isCollapsed) {

            collapsingToolbarLayout.setTransitionName(null);
            avatarGIV.setTransitionName(null);
            telephoneMET.setTransitionName(null);
            emailMET.setTransitionName(null);
        }

        super.onBackPressed();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        if (requestCode == CALL_PERMISSIONS_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                makeCall();
            else
                Toast.makeText(this, getString(R.string.permission_call_denied), Toast.LENGTH_LONG).show();

            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private void initToolbar() {

        final Toolbar toolbar = (Toolbar) findViewById(R.id.contactTbr);
        setSupportActionBar(toolbar);
/*
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onBackPressed();
            }
        });*/

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    private void initCollapsingToolbar() {

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.contactCTL);
        collapsingToolbarLayout.setTitle(userName);

        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.contactABL);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                if (Math.abs(verticalOffset) - appBarLayout.getTotalScrollRange() == 0)
                    isCollapsed = true;
                else isCollapsed = false;
            }
        });
    }


    private void initValues() {

        Bundle extras = getIntent().getBundleExtra("Contact");
        userAvatarUri = extras.getString("AvatarUri");
        userId = extras.getString("Uid");
        userName = extras.getString("Name");
        userTelephone = extras.getString("Telephone");
        aboutMeMET.setText(extras.getString("AboutMe"));
        emailMET.setText(extras.getString("Email"));
    }


    private void setAvatar() {

        RequestOptions options = new RequestOptions()
                .error(R.drawable.user_profil)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);

        Glide.with(this)
                .load(Uri.parse(userAvatarUri))
                .apply(options)
                .into(avatarGIV);
    }


    private void formatNumber() {

        if (userTelephone.startsWith("+")) {

            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String countryCode = tm.getSimCountryIso();
            userTelephone = PhoneNumberUtils.formatNumber(userTelephone, countryCode);
        }

        telephoneMET.setText(userTelephone);
        telNumber = userTelephone;
    }


    private void checkIsEmailEmpty() {

        if (String.valueOf(emailMET.getText()).trim().isEmpty())
            shareEmailIB.setVisibility(View.GONE);
    }


    public void getDealsFromDB() {

        dealsList = SQLite.select().from(Deal.class).where(Deal_Table.userId.eq(userId)).queryList();
        sortDealsList(dealsList);
    }


    private void initRecyclerView() {

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dealAdapter = new DealAdapter(ContactActivity.this, dealsList);
        recyclerView.setAdapter(dealAdapter);
    }


    private void viewAvatar() {

        if (!userAvatarUri.equals("default")) {

            List<String> list = new ArrayList<String>();
            list.add(userAvatarUri);

            Fresco.initialize(this);
            new ImageViewer.Builder(this, Collections.singletonList(userAvatarUri))
                    .hideStatusBar(true)
                    .allowZooming(true)
                    .allowSwipeToDismiss(true)
                    .show();
        }
    }


    private void shareEmail() {

        Intent emailIntent = new Intent();
        emailIntent.setAction(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + emailMET.getText()));

        try {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email_to)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.no_mail_app_on_phone), Toast.LENGTH_SHORT).show();
        }
    }


    private void makeCall() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSIONS_REQUEST);
        else {

            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + telNumber));
            startActivity(intent);
        }
    }


    private void writeMessage() {

        Bundle extras = new Bundle();
        extras.putString("Uid", userId);
        extras.putString("Name", userName);
        extras.putString("Telephone", userTelephone.replaceAll(" ", ""));

        Intent intent = new Intent(ContactActivity.this, ChatActivity.class);
        intent.putExtra("Message", extras);
        startActivity(intent);
    }


    public void onClickAvatar(View view) {
        viewAvatar();
    }


    public void onClickShareEmail(View view) {
        shareEmail();
    }


    public void onClickCall(View view) {
        makeCall();
    }


    public void onClickMessage(View view) {
        writeMessage();
    }
}