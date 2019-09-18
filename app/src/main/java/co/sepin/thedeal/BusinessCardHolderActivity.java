package co.sepin.thedeal;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gjiazhe.wavesidebar.WaveSideBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import co.sepin.thedeal.adapter.BusinessCardHolderAdapter;
import co.sepin.thedeal.application.DrawerClass;
import co.sepin.thedeal.database.Contact;
import co.sepin.thedeal.interfaces.ContactInterface;

import static co.sepin.thedeal.application.UpdateData.businessCardHolderList;
import static co.sepin.thedeal.application.UpdateData.getContactsFromDB;


public class BusinessCardHolderActivity extends DrawerClass implements ContactInterface {

    private static final int CALL_PERMISSIONS_REQUEST = 1;
    private String telephone;
    private WaveSideBar waveSideBar;
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private BusinessCardHolderAdapter businessCardHolderAdapter;
    //private List<Contact> contactsList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fab = (FloatingActionButton) findViewById(R.id.business_card_holder_fabBtn);
        recyclerView = (RecyclerView) findViewById(R.id.business_card_holderRV);
        waveSideBar = (WaveSideBar) findViewById(R.id.business_card_holderWSB);

        //getContactsFromDB();
        //getContactsInfoFromFirebase();
        //updateAvatarsUriFromFirestore();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        businessCardHolderAdapter = new BusinessCardHolderAdapter(BusinessCardHolderActivity.this, businessCardHolderList, this);
        recyclerView.setAdapter(businessCardHolderAdapter);

        autoHideFab();
        initWaveSideBar();
        runAnimation();
    }


    @Override
    public int getContentView() {
        return R.layout.activity_businesscardholder;
    }


    @Override
    protected void onRestart() {
        super.onRestart();

        getContactsFromDB();
        businessCardHolderAdapter.reload(businessCardHolderList);
        //Toast.makeText(this, "Restart", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        if (requestCode == CALL_PERMISSIONS_REQUEST) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                makeCall(telephone);
            else
                Toast.makeText(this, getString(R.string.permission_call_denied), Toast.LENGTH_LONG).show();

            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    public void makeCall(String telephoneNumber) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSIONS_REQUEST);
            telephone = telephoneNumber;
        } else {

            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + telephoneNumber));
            startActivity(intent);
        }
    }


    @Override
    public void writeMessage(Contact contact) {

        Bundle extras = new Bundle();
        extras.putString("Uid", contact.getUserId());
        extras.putString("Name", contact.getName());
        extras.putString("Telephone", contact.getTelephone());
        extras.putString("Status", "Online");

        Intent intent = new Intent(BusinessCardHolderActivity.this, ChatActivity.class);
        intent.putExtra("Message", extras);
        startActivity(intent);
    }


    private void autoHideFab() {// autoukrywanie fab podczas scrollowania

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0 && fab.getVisibility() == View.VISIBLE)
                    fab.hide();
                else if (dy <= 0 && fab.getVisibility() != View.VISIBLE)
                    fab.show();
            }
        });
    }


    private void initWaveSideBar() {

        waveSideBar.setOnSelectIndexItemListener(new WaveSideBar.OnSelectIndexItemListener() {

            @Override
            public void onSelectIndexItem(String index) {
                for (int i = 0; i < businessCardHolderList.size(); i++) {
                    if (businessCardHolderList.get(i).getName().substring(0, 1).toUpperCase().equals(index)) {

                        ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(i, 0);
                        return;
                    }
                }
            }
        });
    }


    private void runAnimation() {

        Context context = recyclerView.getContext();
        LayoutAnimationController controller = null;
        controller = AnimationUtils.loadLayoutAnimation(context, R.anim.recycler_layout_fall_down);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.scheduleLayoutAnimation();
    }


    public void onClickFab(View view) {

        Intent intent = new Intent(BusinessCardHolderActivity.this, SelectContactsActivity.class);
        startActivity(intent);
    }
}