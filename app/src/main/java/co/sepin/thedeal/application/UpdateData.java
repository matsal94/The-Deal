package co.sepin.thedeal.application;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import co.sepin.thedeal.database.Contact;
import co.sepin.thedeal.database.Deal;
import co.sepin.thedeal.database.Event;
import co.sepin.thedeal.database.User;
import co.sepin.thedeal.model.Chat;


public class UpdateData {

    public static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    public static FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    public static List<Contact> businessCardHolderList = new ArrayList<>();


    public static void getContactsFromDB() {

        System.out.println("getContactsFromDB()");

        try {

            businessCardHolderList = SQLite.select().from(Contact.class).queryList();
            sortContactsList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void sortContactsList() {

        Collections.sort(businessCardHolderList, new Comparator<Contact>() {
            @Override
            public int compare(Contact o1, Contact o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
    }


    public static void sortDealsList(List<Deal> dealsList) {

        Collections.sort(dealsList, new Comparator<Deal>() {
            @Override
            public int compare(Deal o1, Deal o2) {
                return Long.compare(o1.getCreateDate(), o2.getCreateDate());
            }
        });

        Collections.reverse(dealsList);
    }

    public static void sortChatList(List<Chat> chatList) {

        Collections.sort(chatList, new Comparator<Chat>() {
            @Override
            public int compare(Chat o1, Chat o2) {
                return Long.compare(o1.getDateTime(), o2.getDateTime());
            }
        });
    }


    public static void getContactsFromFirebase() { // przy starcie aplikacji

        System.out.println("getContactsFromFirebase()");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseContacts = database.getReference("Contacts");

        Delete.table(Contact.class); // usunąc jak wszystko bedzie dobrze działać
        databaseContacts.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                        Contact contact = new Contact();
                        contact.setUserId("");
                        contact.setAvatarUri("");
                        contact.setName(ds.child("name").getValue(String.class));
                        contact.setTelephone(ds.child("tel_num").getValue(String.class));
                        contact.setAboutMe("");
                        contact.setEmail("");
                        contact.setStatus("");
                        contact.setLastSeen("");
                        contact.insert();
                    }

                    getContactsFromDB();
                    getContactsInfoFromFirebase();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    public static void getContactsInfoFromFirebase() {

        System.out.println("getContactsInfoFromFirebase()");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseProfiles = database.getReference("Profiles");

        for (int i = 0; i < businessCardHolderList.size(); i++) {

            final int finalI = i;
            databaseProfiles.child(businessCardHolderList.get(i).getTelephone()).addListenerForSingleValueEvent/*addValueEventListener*/(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    Map<String, String> mapa = new HashMap<>();

                    for (DataSnapshot dsp : dataSnapshot.getChildren())
                        mapa.put(String.valueOf(dsp.getKey()), String.valueOf(dsp.getValue()));

                    Contact contact = new Contact();
                    contact.setUserId(mapa.get("uid"));
                    contact.setAvatarUri(mapa.get("avatar_uri"));
                    contact.setName(businessCardHolderList.get(finalI).getName());
                    contact.setTelephone(businessCardHolderList.get(finalI).getTelephone());
                    contact.setAboutMe(mapa.get("about_me"));
                    contact.setEmail(mapa.get("email"));
                    contact.setStatus(mapa.get("status"));
                    contact.setLastSeen(mapa.get("last_seen"));
                    contact.update();

                    if (finalI == businessCardHolderList.size() - 1) {

                        getContactsFromDB();
                        updateDealsFromFirebase();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }


    public static void updateDealsFromFirebase() {

        System.out.println("updateDealsFromFirebase()");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseDeals = database.getReference("Deals");

        businessCardHolderList.add(new Contact(null, null, null, firebaseUser.getPhoneNumber(), null, null, null, null));

        Delete.table(Deal.class); // nie usuwać!

        for (int i = 0; i < businessCardHolderList.size(); i++) {

            databaseDeals.orderByChild("tel_num").equalTo(businessCardHolderList.get(i).getTelephone()).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    System.out.println("updateDealsFromFirebase() onDataChange");

                    if (dataSnapshot.getValue() != null) {

                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                            Map dealMap = (Map) postSnapshot.getValue();

                            Deal deal = new Deal();
                            deal.id = postSnapshot.getKey();
                            deal.userId = String.valueOf(dealMap.get("uid"));
                            deal.phone = String.valueOf(dealMap.get("tel_num"));
                            deal.title = String.valueOf(dealMap.get("title"));
                            deal.description = String.valueOf(dealMap.get("description"));
                            deal.createDate = Long.valueOf(String.valueOf(dealMap.get("date_time")));
                            deal.categoryId = Long.parseLong(String.valueOf(dealMap.get("category_id")));
                            deal.insert();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

            if (businessCardHolderList.get(i).getTelephone().equals(firebaseUser.getPhoneNumber()))
                businessCardHolderList.remove(businessCardHolderList.size() - 1);
        }
        System.out.println("updateDealsFromFirebase() listSize(): " + businessCardHolderList.size());
    }


    public static void updateProfilInfoFromFirebase() {

        System.out.println("updateProfilInfoFromFirebase()");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseProfiles = database.getReference("Profiles");

        //Delete.table(User.class); // usunąć jak bedzie wszystko dobrze działać
        databaseProfiles.child(Objects.requireNonNull(firebaseUser.getPhoneNumber())).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {

                    User user = new User();
                    user.userId = dataSnapshot.child("uid").getValue(String.class);
                    user.avatarUri = dataSnapshot.child("avatar_uri").getValue(String.class);
                    user.phone = dataSnapshot.child("tel_num").getValue(String.class);
                    user.aboutMe = dataSnapshot.child("about_me").getValue(String.class);
                    user.email = dataSnapshot.child("email").getValue(String.class);
                    user.insert();

                    System.out.println("baza nie jest pusta");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    public static void updateEventsFromFirebase() {

        System.out.println("updateEventsFromFirebase()");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseEvents = database.getReference("Events");

        databaseEvents.child(Objects.requireNonNull(firebaseUser.getUid())).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                        Event event = new Event();
                        event.setId(postSnapshot.getKey());
                        event.setUserId(postSnapshot.child("uid").getValue(String.class));
                        event.setName(postSnapshot.child("name").getValue(String.class));
                        event.setDateTime(Long.valueOf(Objects.requireNonNull(postSnapshot.child("date_time").getValue(String.class))));
                        event.setAddress(postSnapshot.child("address").getValue(String.class));
                        event.setLat(Double.valueOf(Objects.requireNonNull(postSnapshot.child("lat").getValue(String.class))));
                        event.setLon(Double.valueOf(Objects.requireNonNull(postSnapshot.child("lon").getValue(String.class))));
                        event.setComment(postSnapshot.child("comment").getValue(String.class));
                        event.setDescription(postSnapshot.child("description").getValue(String.class));
                        event.setColor(Integer.valueOf(Objects.requireNonNull(postSnapshot.child("color").getValue(String.class))));
                        event.insert();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    public static void updateUserStatus(String state) {

        Map<String, Object> stateMap = new HashMap<>();
        stateMap.put("last_seen", String.valueOf(new Date().getTime()));
        stateMap.put("status", state);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseProfiles = database.getReference("Profiles");
        databaseProfiles.child(Objects.requireNonNull(firebaseUser.getPhoneNumber())).updateChildren(stateMap);
    }
}