package co.sepin.thedeal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.List;

import co.sepin.thedeal.adapter.DealAdapter;
import co.sepin.thedeal.application.DrawerClass;
import co.sepin.thedeal.database.Contact;
import co.sepin.thedeal.database.Contact_Table;
import co.sepin.thedeal.database.Deal;

import static co.sepin.thedeal.application.ModeClass.checkInternetConnection;
import static co.sepin.thedeal.application.UpdateData.businessCardHolderList;
import static co.sepin.thedeal.application.UpdateData.getContactsFromDB;
import static co.sepin.thedeal.application.UpdateData.sortDealsList;
import static co.sepin.thedeal.application.UpdateData.updateDealsFromFirebase;
import static co.sepin.thedeal.application.UpdateData.updateUserStatus;


public class MainActivity extends DrawerClass {

    private CoordinatorLayout mainLayout;
    private FloatingActionButton fab;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swiper;
    private DealAdapter dealAdapter;
    private Handler handler;
    private Runnable finalizer;
    private List<Deal> dealsList = new ArrayList<>();
    private boolean isFocus;
    //private boolean isListener = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fab = (FloatingActionButton) findViewById(R.id.mainFabBtn);
        recyclerView = (RecyclerView) findViewById(R.id.mainRV);
        swiper = (SwipeRefreshLayout) findViewById(R.id.mainSRL);
        mainLayout = (CoordinatorLayout) findViewById(R.id.mainCoL);

        SharedPreferences.Editor editor = getSharedPreferences("Login", MODE_PRIVATE).edit();
        editor.putBoolean("login", true);
        editor.apply();

        getContactsFromDB();
        getDealsFromDB();

        setFirebaseContactsInfoListener();
        updateUserStatus("Online");

        initRecyclerView();
        initSwiper();
        autohideFab();
        runAnimation();
        setFocusListener();
    }


    @Override
    public int getContentView() {
        return R.layout.activity_main;
    }


    @Override
    protected void onStart() {
        super.onStart();
        //updateUserStatus("Online");
    }


    @Override
    protected void onRestart() {
        super.onRestart();

        getContactsFromDB();
        getDealsFromDB();
        dealAdapter.reload(dealsList);
        //recyclerView.getAdapter().notifyDataSetChanged();
    }


    @Override
    protected void onStop() {
        super.onStop();

        if (swiper.isRefreshing()) {

            handler.removeCallbacks(finalizer);
            swiper.setRefreshing(false);
        }
    }


    @Override
    public void onBackPressed() {

        if (!searchView.isIconified())
            searchView.setIconified(true);
        else
            minimizeApp();
        //super.onBackPressed();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_search_menu, menu);
        final MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(myActionMenuItem);
        changeSearchViewTextColor(searchView);
        ((EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text)).setHintTextColor(getResources().getColor(R.color.colorDivider));


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                MenuItemCompat.collapseActionView(myActionMenuItem);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                final List<Deal> filtermodelist = filter(dealsList, newText);
                dealAdapter.reload(filtermodelist);
                return true;
            }
        });

        searchView.setOnSearchClickListener(new SearchView.OnClickListener() {

            @Override
            public void onClick(View v) {

                fab.hide();
                swiper.setEnabled(false);
                recyclerView.clearOnScrollListeners();
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {

            @Override
            public boolean onClose() {

                fab.show();
                swiper.setEnabled(true);
                autohideFab();
                return false;
            }
        });

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_search:
                //fab.hide();
                //fab.setVisibility(View.GONE);
                return true;

            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void minimizeApp() {

        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }


    private void initRecyclerView() {

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dealAdapter = new DealAdapter(MainActivity.this, dealsList);
        recyclerView.setAdapter(dealAdapter);
    }


    private void initSwiper() {

        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {

                if (isFocus) { // zapobieganie odświeżania podczas widocznego okna dialogowego deal'a

                    if (checkInternetConnection()) {

                        updateDealsFromFirebase();

                        handler = new Handler();
                        handler.postDelayed(finalizer = new Runnable() {
                            @Override
                            public void run() {

                                getDealsFromDB(); //pobranie danych na nowo
                                dealAdapter.reload(dealsList);
                                swiper.setRefreshing(false);
                                runAnimation();
                                handler = null;
                            }
                        }, 4000);
                    } else {

                        swiper.setRefreshing(false);
                        Toast.makeText(MainActivity.this, getString(R.string.no_Internet_connection), Toast.LENGTH_SHORT).show();
                    }
                } else
                    swiper.setRefreshing(false);
            }
        });

        swiper.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light)
        );
    }


    private void autohideFab() {// autoukrywanie fab podczas scrollowania

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


    private void runAnimation() {

        Context context = recyclerView.getContext();
        LayoutAnimationController controller = null;
        controller = AnimationUtils.loadLayoutAnimation(context, R.anim.recycler_layout_from_bottom);

        recyclerView.setLayoutAnimation(controller);
        //recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }


    private void setFocusListener() {

        mainLayout.getViewTreeObserver().addOnWindowFocusChangeListener(new ViewTreeObserver.OnWindowFocusChangeListener() {
            @Override
            public void onWindowFocusChanged(boolean hasFocus) {
                isFocus = hasFocus;
            }
        });
    }


    private void setFirebaseContactsInfoListener() {

        //isListener = true;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseProfiles = database.getReference("Profiles");

        for (int i = 0; i < businessCardHolderList.size(); i++) {

            int finalI = i;
            databaseProfiles.child(businessCardHolderList.get(i).getTelephone()).addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (checkIsStillContact(String.valueOf(dataSnapshot.getKey()))) // sprawdzenie czy nie został usunięty z wizytownika
                        updateProfilValues(dataSnapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
            /*
            addChildEventListener(new ChildEventListener() {

                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    System.out.println("onChildAdded: " + dataSnapshot.getKey() + ", " + dataSnapshot.getValue());

                    if (checkIsStillContact(String.valueOf(dataSnapshot.getRef().getParent().getKey()))) // sprawdzenie czy nie został usunięty z wizytownika
                        updateProfilValues(dataSnapshot);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                 *//*   System.out.println("onChildChanged: " + dataSnapshot.getKey());

                    if (checkIsStillContact(String.valueOf(dataSnapshot.getRef().getParent().getKey()))) // sprawdzenie czy nie został usunięty z wizytownika
                        updateProfilValues(dataSnapshot);*//*
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });*/
        }
    }


    private boolean checkIsStillContact(String telephone) {

        for (int i = 0; i < businessCardHolderList.size(); i++)
            if (businessCardHolderList.get(i).getTelephone().equals(telephone))
                return true;

        return false;
    }


    private void updateProfilValues(DataSnapshot dataSnapshot) {

        try {

            SQLite.update(Contact.class).set(
                    Contact_Table.userId.eq(dataSnapshot.child("uid").getValue(String.class)),
                    Contact_Table.avatarUri.eq(dataSnapshot.child("avatar_uri").getValue(String.class)),
                    Contact_Table.aboutMe.eq(dataSnapshot.child("about_me").getValue(String.class)),
                    Contact_Table.email.eq(dataSnapshot.child("email").getValue(String.class)),
                    Contact_Table.status.eq(dataSnapshot.child("status").getValue(String.class)),
                    Contact_Table.lastSeen.eq(dataSnapshot.child("last_seen").getValue(String.class)))
                    .where(Contact_Table.telephone.eq(dataSnapshot.getKey())).async().execute();

            getContactsFromDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void onClickFab(View view) {

        Intent intent = new Intent(MainActivity.this, AddDealActivity.class);
        startActivity(intent);
    }


    private List<Deal> filter(List<Deal> dealsList, String query) {

        query = query.toLowerCase();
        final List<Deal> filteredModeList = new ArrayList<>();
        for (Deal model : dealsList) {
            final String text = model.getTitle().toLowerCase();
            if (text.startsWith(query)) {
                filteredModeList.add(model);
            }
        }
        return filteredModeList;
    }


    private void changeSearchViewTextColor(View view) {

        if (view != null) {

            if (view instanceof TextView) {

                ((TextView) view).setTextColor(Color.WHITE);
                return;
            } else if (view instanceof ViewGroup) {

                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    changeSearchViewTextColor(viewGroup.getChildAt(i));
                }
            }
        }
    }


    public void getDealsFromDB() {

        dealsList = SQLite.select().from(Deal.class).queryList();
        sortDealsList(dealsList);

        //dealAdapter.reload(dealsList);


        /*
        dealsList = SQLite.select(Deal_Table.title, Deal_Table.description, Deal_Table.createDate, Deal_Table.categoryId)
                .from(Deal.class).queryList();*/
/*
        User userId = SQLite.select(User_Table.id).from(User.class).querySingle();

        Api api = RetrofitUtils.getInstance().create(Api.class);
        Call<List<Deal>> call = api.getDeal(userId); // id_usera pobrane z bazy

        call.enqueue(new Callback<List<Deal>>() {
            @Override
            public void onResponse(Call<List<Deal>> call, Response<List<Deal>> response) {

                if (response.code() == 200) {

                    List<Deal> listaDeali = response.body();
                    dealAdapter.reload(listaDeali);
                }
                else {

                    Toast.makeText(getApplicationContext(), getString(R.string.get_deals_failure), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Deal>> call, Throwable t) {

                Toast.makeText(getApplicationContext(), getString(R.string.get_deals_failure), Toast.LENGTH_LONG).show();
            }
        }); */
    }
}