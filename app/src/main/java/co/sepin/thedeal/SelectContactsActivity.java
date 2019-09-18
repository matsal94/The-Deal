package co.sepin.thedeal;

import android.Manifest;
import android.content.ContentResolver;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import co.sepin.thedeal.adapter.SelectContactsAdapter;
import co.sepin.thedeal.database.Contact;
import co.sepin.thedeal.interfaces.InternetChangeListener;
import co.sepin.thedeal.interfaces.SelectedContactsInterface;
import co.sepin.thedeal.receiver.InternetChangeReceiver;
import co.sepin.thedeal.other.ViewDialog;

import static co.sepin.thedeal.application.UpdateData.firebaseUser;
import static co.sepin.thedeal.application.UpdateData.getContactsFromDB;
import static co.sepin.thedeal.application.UpdateData.getContactsInfoFromFirebase;


public class SelectContactsActivity extends AppCompatActivity implements SelectContactsAdapter.ItemClickListener, SelectedContactsInterface {

    private static final int READ_CONTACTS_PERMISSIONS_REQUEST = 1;
    private SelectContactsAdapter selectContactsAdapter;
    private List<Contact> selectContactsList = new ArrayList<>();
    private InternetChangeReceiver internetChangeReceiver;
    private ViewDialog loadingDialog;
    private List<ValueEventListener> usersListenerList = new ArrayList<>();
    private DatabaseReference databaseUsers;
    private boolean isRegister = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_contacts);

        Toolbar toolbar = findViewById(R.id.select_contactsTbr);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.select_contactsRV);

        getContactsFromDB(); // uaktualnienie businessCardholderList
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        selectContactsAdapter = new SelectContactsAdapter(SelectContactsActivity.this, recyclerView, selectContactsList, this, this);
        recyclerView.setAdapter(selectContactsAdapter);

        loadingDialog = new ViewDialog(this, getString(R.string.checking_contacts));
        getPermissionToReadContacts();
    }


    @Override
    protected void onStop() {
        unregisterBroadcast();
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (int i = 0; i < usersListenerList.size(); i++) // usuwanie listenerów aby nie działały po opuszczeniu ekranu
            databaseUsers.removeEventListener(usersListenerList.get(i));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        if (requestCode == READ_CONTACTS_PERMISSIONS_REQUEST) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                getContacts();
            else
                Toast.makeText(this, getString(R.string.permission_contacts_denied), Toast.LENGTH_LONG).show();

            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_contact_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_refresh:

                selectContactsList.clear();
                selectContactsAdapter.cleanArrayAndList();
                getPermissionToReadContacts();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemClick(View view, int position) {
        sendInvitation((CircularProgressButton) view, selectContactsList.get(position).getTelephone());
    }


    @Override
    public void sendNewContactsList(List<Contact> newContactsList) {

        Contact contact = new Contact();

        try {

            for (int i = 0; i < newContactsList.size(); i++) {

                contact.setAvatarUri("default");
                contact.setName(newContactsList.get(i).getName());
                contact.setTelephone(newContactsList.get(i).getTelephone());
                contact.setAboutMe("");
                contact.setEmail("");
                contact.save();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        getContactsFromDB();
        getContactsInfoFromFirebase();
        sendContactsToFirebase(newContactsList);

        finish();
    }


    public void getPermissionToReadContacts() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, READ_CONTACTS_PERMISSIONS_REQUEST);
        else
            getContacts();
    }


    private void getContacts() {

        if (!isRegister)
            registerBroadcast();

        ContentResolver contentResolver = getContentResolver();

        try {

            Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");

            if ((cursor != null ? cursor.getCount() : 0) > 0) {

                while (cursor != null && cursor.moveToNext()) {

                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String telephone = null;

                    if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {

                        Cursor cursor2 = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                        while (cursor2.moveToNext())
                            telephone = cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        cursor2.close();

                        selectContactsList.add(new Contact(name, telephone != null ? telephone.replaceAll("\\s+", "") : null, 2));
                    }
                }
            }

            if (!selectContactsList.isEmpty()) {

                checkFirebaseUsers();
                selectContactsAdapter.notifyDataSetChanged();
            }

            if (cursor != null)
                cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void checkFirebaseUsers() {

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseUsers = database.getReference("Users");

        for (int i = 0; i < selectContactsList.size(); i++) {

            final String telephone = selectContactsList.get(i).getTelephone();
            final int ii = i;

            ValueEventListener databaseUsersListener;
            databaseUsers.orderByChild("tel_num").equalTo(telephone).addValueEventListener(databaseUsersListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.getValue() != null) {

                        System.out.println("SelectContacts users = " + ii);
                        selectContactsList.get(ii).setIsUser(1);
                        selectContactsAdapter.notifyDataSetChanged();
                    } else {
                        checkFirebaseInvitations(database, telephone, ii);
                    }

                    if (ii == selectContactsList.size() - 1 && loadingDialog != null)
                        loadingDialog.hideDialog();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.d("SelectContactsActivity:", error.getMessage());
                }
            });
            usersListenerList.add(databaseUsersListener);
        }
        selectContactsAdapter.notifyDataSetChanged();
    }


    private void checkFirebaseInvitations(FirebaseDatabase database, String telephone, final int ii) {

        DatabaseReference databaseInvitations = database.getReference("Invitations");
        databaseInvitations.orderByChild("tel_num").equalTo(telephone).addListenerForSingleValueEvent(new ValueEventListener() { // tu musi być ListenerForSingleValueEvent

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {

                    System.out.println("SelectContacts invitations = " + ii);
                    selectContactsList.get(ii).setIsUser(2);
                    selectContactsAdapter.notifyDataSetChanged();
                } else {

                    selectContactsList.get(ii).setIsUser(0);
                    selectContactsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d("SelectContactsActivity:", error.getMessage());
            }
        });
    }


    private void sendInvitation(final CircularProgressButton inviteCPB, String telNumber) {

        inviteCPB.startAnimation();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseInvitations = database.getReference("Invitations");
        String id = databaseInvitations.push().getKey();

        telNumber = telNumber.replaceAll("\\s+", "").trim(); // usunięcie spacji w nr tel

        //databaseInvitations.child(id).child("tel_num").setValue(telNumber); // to przestało działać
        databaseInvitations.child(id).child("tel_num").setValue(telNumber); // to działa
        //databaseInvitations.push();
        databaseInvitations.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    public void run() {

                        Bitmap bitmapDone = BitmapFactory.decodeResource(getResources(), R.drawable.done_white_48);
                        inviteCPB.doneLoadingAnimation(getResources().getColor(R.color.colorAccent), bitmapDone);
                        //inviteTV.setText(R.string.invitation_sent);
                        //Toast.makeText(ContactActivity.this, R.string.invitation_sent, Toast.LENGTH_SHORT).show();
                    }
                }, 1000);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    private void sendContactsToFirebase(List<Contact> newContactsList) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseContacts = database.getReference("Contacts");

        Map<String, String> contactMap = new HashMap<>(); //stworzyc mape z num tel i nazwą

        for (int i = 0; i < newContactsList.size(); i++) {

            contactMap.put("tel_num", newContactsList.get(i).getTelephone());
            contactMap.put("name", newContactsList.get(i).getName());

            databaseContacts.child(firebaseUser.getUid()).child(newContactsList.get(i).getName()).setValue(contactMap);
        }
    }


    private void registerBroadcast() {

        InternetChangeReceiver.isNews = false;
        InternetChangeReceiver.isProfil = false;
        InternetChangeReceiver.isSelectContacts = true;
        InternetChangeReceiver.isWeather = false;


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");

        internetChangeReceiver = new InternetChangeReceiver();
        registerReceiver(internetChangeReceiver, intentFilter);

        isRegister = true;

        InternetChangeReceiver.bindSelectContactsListener(new InternetChangeListener() {

            @Override
            public void internetConnected() {

                if (!selectContactsList.isEmpty()) {

                    loadingDialog.showDialog();
                    unregisterBroadcast();
                }
            }

            @Override
            public void noInternetConnected() {
                Toast.makeText(SelectContactsActivity.this, "Połącz się z Internetem", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void unregisterBroadcast() {

        if (isRegister) {

            InternetChangeReceiver.isSelectContacts = false;
            InternetChangeReceiver.bindSelectContactsListener(null);
            unregisterReceiver(internetChangeReceiver);
            isRegister = false;
        }
    }
}
