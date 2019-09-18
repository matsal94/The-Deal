package co.sepin.thedeal;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.FlowCursor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import co.sepin.thedeal.application.ModeClass;
import co.sepin.thedeal.database.Deal;
import co.sepin.thedeal.database.DealType;
import co.sepin.thedeal.database.DealType_Table;
import co.sepin.thedeal.database.User;
import co.sepin.thedeal.database.User_Table;
import de.hdodenhof.circleimageview.CircleImageView;

import static co.sepin.thedeal.application.UpdateData.firebaseUser;


public class AddDealActivity extends ModeClass {

    private CircleImageView avatarCIV;
    private EditText titleET, descriptionET;
    private TextView nameTV;
    private Spinner spinner;
    private ExtendedFloatingActionButton doneEFab;
    private Animation animShake;
    private Vibrator vib;
    private TextInputLayout layoutTitle, layoutDescription;
    private ArrayAdapter<String> spinnerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_adddeal);
        hideKeyboardAndFocus(findViewById(R.id.addDealCL), AddDealActivity.this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.addDeal_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        animShake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        avatarCIV = (CircleImageView) findViewById(R.id.addDeal_avatarCIV);
        nameTV = (TextView) findViewById(R.id.addDeal_nameTV);
        titleET = (EditText) findViewById(R.id.addDeal_TitleET);
        descriptionET = (EditText) findViewById(R.id.addDeal_descET);
        spinner = (Spinner) findViewById(R.id.addDealSpn);
        doneEFab = (ExtendedFloatingActionButton) findViewById(R.id.addDeal_doneEFab);
        layoutTitle = (TextInputLayout) findViewById(R.id.addDeal_layout_title);
        layoutDescription = (TextInputLayout) findViewById(R.id.addDeal_layout_description);

        setAvatar();
        setName();
        initSpinner();
    }


    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences("Deal", MODE_PRIVATE);
        int spinnerPosition = spinnerAdapter.getPosition(prefs.getString("spinner", null));// pobranie wartosci z Shared i dodanie do spinnera
        spinner.setSelection(spinnerPosition);
    }


    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = getSharedPreferences("Deal", MODE_PRIVATE).edit();
        editor.putString("spinner", spinner.getSelectedItem().toString());// pobranie wartości ze spinnera
        editor.apply();
    }


    private void setAvatar() {

        String avatarUri = "";

        try {
            avatarUri = SQLite.select(User_Table.avatarUri).from(User.class).where(User_Table.userId.eq(firebaseUser.getUid())).querySingle().getAvatarUri();
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestOptions requestOptions = new RequestOptions()
                .error(R.drawable.user_profil)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);

        Glide.with(this)
                .load(avatarUri)
                .apply(requestOptions)
                .into(avatarCIV);
    }


    private void setName() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String name = preferences.getString("user_name","");

        if (name.equals(""))
            nameTV.setText(getString(R.string.me));
        else
            nameTV.setText(new StringBuilder(getString(R.string.me)).append(" (").append(name).append(")"));

    }


    private void initSpinner() {

        spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getCategory()) {

            Typeface typeface = ResourcesCompat.getFont(AddDealActivity.this, R.font.nunito);

            public View getView(int position, View convertView, ViewGroup parent) {

                View v = super.getView(position, convertView, parent);
                ((TextView) v).setTypeface(typeface);
                ((TextView) v).setTextSize(14);
                ((TextView) v).setTextColor(getResources().getColor(R.color.colorAccent));

                return v;
            }

            public View getDropDownView(int position, View convertView, ViewGroup parent) {

                View v = super.getDropDownView(position, convertView, parent);
                ((TextView) v).setTypeface(typeface);
                ((TextView) v).setTextSize(14);
                ((TextView) v).setTextColor(getResources().getColor(R.color.colorAccent));

                return v;
            }
        };

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
    }


    private List<String> getCategory() {

        List<String> lista = new ArrayList<>();
        FlowCursor cursor = SQLite.select(DealType_Table.description).from(DealType.class).query();

        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {

                String description = cursor.getString(cursor.getColumnIndex("description"));
                lista.add(description);
            }
        }
        cursor.close();
        return lista;
    }


    private long getCategoryId() {

        String value = spinner.getSelectedItem().toString();
        return SQLite.select().from(DealType.class).where(DealType_Table.description.eq(value)).querySingle().getId();
    }


    private void addDeal(String title, String desc, long timestampInMilliseconds, long categoryId) {

        if (checkInternetConnection()) {

            doneEFab.setEnabled(false);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseDeals = database.getReference("Deals").push();
            //String id = databaseDeals.push().getKey();

            Map<String, String> dealMap = new HashMap<>();
            dealMap.put("uid", firebaseUser.getUid());
            dealMap.put("tel_num", Objects.requireNonNull(firebaseUser.getPhoneNumber()));
            dealMap.put("title", title);
            dealMap.put("description", desc);
            dealMap.put("date_time", String.valueOf(timestampInMilliseconds));
            dealMap.put("category_id", String.valueOf(categoryId));

            databaseDeals.setValue(dealMap);
            databaseDeals.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.getValue() != null) {

                        Deal deal = new Deal();
                        deal.setId(dataSnapshot.getKey());
                        deal.setUserId(dataSnapshot.child("uid").getValue(String.class));
                        deal.setPhone(dataSnapshot.child("tel_num").getValue(String.class));
                        deal.setTitle(dataSnapshot.child("title").getValue(String.class));
                        deal.setDescription(dataSnapshot.child("description").getValue(String.class));
                        deal.setCreateDate(Long.valueOf(Objects.requireNonNull(dataSnapshot.child("date_time").getValue(String.class))));
                        deal.setCategoryId(Long.valueOf(Objects.requireNonNull(dataSnapshot.child("category_id").getValue(String.class))));
                        deal.insert();

                        Toast.makeText(getApplicationContext(), getString(R.string.add_deal), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    doneEFab.setEnabled(true);
                    Toast.makeText(AddDealActivity.this, "Błąd podczas dodawania", Toast.LENGTH_SHORT).show();
                }
            });
        } else
            Toast.makeText(this, getString(R.string.no_Internet_connection), Toast.LENGTH_SHORT).show();

        //ModelAdapter<Deal> adapter = FlowManager.getModelAdapter(Deal.class);
        //adapter.insert(deal);

/*
        Deal deal = new Deal(userId, title, desc, timestampInMilliseconds, categoryId);

        Api api = RetrofitUtils.getInstance().create(Api.class);
        Call<Deal> call = api.setDeal(deal);


        call.enqueue(new Callback<Deal>() {
            @Override
            public void onResponse(Call<Deal> call, Response<Deal> response) {

                if (response.code() == 200) {

                    response.body().save();
                    dealTitle.setText("");
                    dealDesc.setText("");

                    Toast.makeText(getApplicationContext(), getString(R.string.add_deal), Toast.LENGTH_LONG).show();
                    finish();
                }

                else
                    Toast.makeText(getApplicationContext(), getString(R.string.add_deal_failure), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<Deal> call, Throwable t) {

                Toast.makeText(getApplicationContext(), getString(R.string.add_deal_failure), Toast.LENGTH_LONG).show();
            }
        });*/
    }


    public void onClickFab(View view) {

        String title = titleET.getText().toString().trim();
        String desc = descriptionET.getText().toString().trim();

        if (title.isEmpty()) {

            layoutTitle.setAnimation(animShake);
            layoutTitle.startAnimation(animShake);

            if (checkVibrationMode())
                vib.vibrate(120);

            layoutTitle.setErrorEnabled(true);
            layoutTitle.setError(getString(R.string.add_error));

            layoutDescription.clearAnimation();
            return;
        }

        layoutTitle.setErrorEnabled(false);
        layoutTitle.clearAnimation();

        if (desc.isEmpty()) {

            layoutTitle.setErrorEnabled(false);

            layoutDescription.setAnimation(animShake);
            layoutDescription.startAnimation(animShake);

            if (checkVibrationMode())
                vib.vibrate(120);

            layoutDescription.setErrorEnabled(true);
            layoutDescription.setError(getString(R.string.add_error));
            return;
        }

        layoutDescription.setErrorEnabled(false);

        long timestampInMilliseconds = new Date().getTime();
        long categoryId = getCategoryId(); // pobranie id po nazwie kategorii

        addDeal(title, desc, timestampInMilliseconds, categoryId);
    }
}