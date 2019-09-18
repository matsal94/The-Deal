package co.sepin.thedeal;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import co.sepin.thedeal.application.ModeClass;
import co.sepin.thedeal.database.Event;
import petrov.kristiyan.colorpicker.ColorPicker;

import static co.sepin.thedeal.application.UpdateData.firebaseUser;


public class AddEventActivity extends ModeClass {

    private boolean update;
    private Animation animShake;
    private Vibrator vib;
    private ImageButton colorIB, clearAddressIB;
    private ImageView desciptionIV, addressIV;
    private EditText nameET, dateET, timeET, addressET, commentET, descriptionET;
    private TextInputLayout layoutName, layoutDate, layoutTime, layoutComment;
    private double lat, lon;
    private String id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_addevent);
        hideKeyboardAndFocus(findViewById(R.id.addEventSV), AddEventActivity.this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        animShake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        desciptionIV = (ImageView) findViewById(R.id.addEvent_descriptionIV);
        addressIV = (ImageView) findViewById(R.id.addEvent_addressIV);
        colorIB = (ImageButton) findViewById(R.id.addEvent_colorIB);
        clearAddressIB = (ImageButton) findViewById(R.id.addEvent_address_clearIB);
        nameET = (EditText) findViewById(R.id.addEvent_nameET);
        dateET = (EditText) findViewById(R.id.addEvent_dateET);
        timeET = (EditText) findViewById(R.id.addEvent_timeET);
        addressET = (EditText) findViewById(R.id.addEvent_addressET);
        commentET = (EditText) findViewById(R.id.addEvent_commentET);
        descriptionET = (EditText) findViewById(R.id.addEvent_descriptionET);
        layoutName = (TextInputLayout) findViewById(R.id.addEvent_nameTIL);
        layoutDate = (TextInputLayout) findViewById(R.id.addEvent_dateTIL);
        layoutTime = (TextInputLayout) findViewById(R.id.addEvent_timeTIL);
        layoutComment = (TextInputLayout) findViewById(R.id.addEvent_commentTIL);

        setDateTimeOnClickListeners();
        setChangeColorIconListener();
    }


    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefsUpdate = getSharedPreferences("Update", MODE_PRIVATE);
        update = prefsUpdate.getBoolean("update", false);

        SharedPreferences prefsMap = getSharedPreferences("Map", MODE_PRIVATE);
        boolean map = prefsMap.getBoolean("map", false);

        if (update)
            initUpdateValues(prefsUpdate);
        else
            initStartValues();

        if (map)
            initMapValues(prefsMap);
    }


    @Override
    protected void onPause() {
        super.onPause();

        if (update) {

            SharedPreferences.Editor updateEditor = getSharedPreferences("Update", MODE_PRIVATE).edit();
            updateEditor.putString("id", id);
            updateEditor.putString("name", nameET.getText().toString());
            updateEditor.putString("date", dateET.getText().toString());
            updateEditor.putString("time", timeET.getText().toString());
            updateEditor.putString("address", addressET.getText().toString());
            putDouble(updateEditor, "lat", lat);
            putDouble(updateEditor, "lon", lon);
            updateEditor.putString("comment", commentET.getText().toString());
            updateEditor.putString("description", descriptionET.getText().toString());
            updateEditor.putInt("color", ((ColorDrawable) colorIB.getBackground()).getColor());
            updateEditor.apply();
        } else {

            SharedPreferences.Editor eventEditor = getSharedPreferences("Event", MODE_PRIVATE).edit();
            eventEditor.putInt("color", ((ColorDrawable) colorIB.getBackground()).getColor());
            eventEditor.apply();
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        addressIV.setImageDrawable(getDrawable(R.drawable.ic_position_gray));
    }


    @Override
    protected void onDestroy() {

        if (update) {

            SharedPreferences prefsUpdate = getSharedPreferences("Update", MODE_PRIVATE);
            prefsUpdate.edit().clear().apply();
        }

        SharedPreferences prefsMap = getSharedPreferences("Map", MODE_PRIVATE);
        prefsMap.edit().clear().apply();

        super.onDestroy();
    }


    @Override
    protected Dialog onCreateDialog(int id) {

        final Calendar calendar = Calendar.getInstance();
        int year_x = calendar.get(Calendar.YEAR);
        int month_x = calendar.get(Calendar.MONTH);
        int day_x = calendar.get(Calendar.DAY_OF_MONTH);
        int hour_x = calendar.get(Calendar.HOUR_OF_DAY);
        int minute_x = calendar.get(Calendar.MINUTE);

        if (id == 0)
            return new DatePickerDialog(this, datePickerListener, year_x, month_x, day_x);
        else
            return new TimePickerDialog(this, timePickerListener, hour_x, minute_x, true);
    }


    private void setChangeColorIconListener() {

        descriptionET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (hasFocus)
                    desciptionIV.setImageDrawable(getDrawable(R.drawable.ic_description_blue));
                else
                    desciptionIV.setImageDrawable(getDrawable(R.drawable.ic_description_gray));
            }
        });
    }


    private void setDateTimeOnClickListeners() {

        dateET.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDialog(0);
            }
        });

        timeET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(1);
            }
        });
    }


    private void initUpdateValues(SharedPreferences prefsUpdate) {

        id = prefsUpdate.getString("id", null);
        String name = prefsUpdate.getString("name", null);
        String date = prefsUpdate.getString("date", null);
        String time = prefsUpdate.getString("time", null);
        String address = prefsUpdate.getString("address", getString(R.string.calendar_localization));
        lat = getDouble(prefsUpdate, "lat", 0);
        lon = getDouble(prefsUpdate, "lon", 0);
        String comment = prefsUpdate.getString("comment", null);
        String description = prefsUpdate.getString("description", null);
        int color = prefsUpdate.getInt("color", 14654801);

        System.out.println("adress: " + address);
        nameET.setText(name);
        dateET.setText(date);
        timeET.setText(time);
        addressET.setText(address);
        descriptionET.setText(description);
        colorIB.setBackgroundColor(color);

        if (address.equals(getString(R.string.calendar_localization))) {

            layoutComment.setVisibility(View.GONE);
        } else if (address.equals("")) {

            clearAddressIB.setVisibility(View.GONE);
            layoutComment.setVisibility(View.GONE);
            addressET.setText(getString(R.string.calendar_localization));
        } else {

            commentET.setText(comment);
            addressET.setTextColor(getResources().getColor(R.color.textColorTertiary));
            layoutComment.setVisibility(View.VISIBLE);
        }
    }


    private void initStartValues() {

        SharedPreferences prefsEvent = getSharedPreferences("Event", MODE_PRIVATE);
        colorIB.setBackgroundColor(prefsEvent.getInt("color", getResources().getColor(R.color.colorPrimary)));
        layoutComment.setVisibility(View.GONE);
        clearAddressIB.setVisibility(View.GONE);
    }


    private void initMapValues(SharedPreferences prefsMap) {

        System.out.println("Address: " + prefsMap.getString("address", null) + ", lat: " + lat + ", lon: " + lon);

        lat = getDouble(prefsMap, "lat", 0);
        lon = getDouble(prefsMap, "lon", 0);

        String address = prefsMap.getString("address", getString(R.string.calendar_localization));
        addressET.setText(address);

        if (address.equals(getString(R.string.calendar_localization)))
            layoutComment.setVisibility(View.GONE);
        else {

            clearAddressIB.setVisibility(View.VISIBLE);
            layoutComment.setVisibility(View.VISIBLE);
            addressET.setTextColor(getResources().getColor(R.color.textColorTertiary));
        }
    }


    private void checkIsSomethingNew(String name, String date, String time, String address, String comment, String description, int color) {

        SharedPreferences prefsUpdate = getSharedPreferences("Update", MODE_PRIVATE);

        String prefsId = prefsUpdate.getString("id", null);
        String prefsName = prefsUpdate.getString("name", null);
        String prefsDate = prefsUpdate.getString("date", null);
        String prefsTime = prefsUpdate.getString("time", null);
        String prefsAddress = prefsUpdate.getString("address", "");
        String prefsComment = prefsUpdate.getString("comment", "");
        String prefsDescription = prefsUpdate.getString("description", null);
        int prefsColor = prefsUpdate.getInt("color", 0);

        if (prefsAddress.equals(getString(R.string.calendar_localization)))
            prefsAddress = "";

        if (checkLayoutsErrors(name, date, time) && !(name.equals(prefsName)) || !(date.equals(prefsDate)) || !(time.equals(prefsTime)) || !(address.equals(prefsAddress)) || !(comment.equals(prefsComment)) || !(description.equals(prefsDescription)) || !(color == prefsColor))
            updateEvent(prefsUpdate, prefsId);
        else
            Toast.makeText(this, "Nic nie zostało zmienione", Toast.LENGTH_SHORT).show();
    }


    private void updateEvent(SharedPreferences prefsUpdate, String id) {

        String userId = firebaseUser.getUid();
        String name = nameET.getText().toString().trim();
        long dateTime = 0;
        String address = addressET.getText().toString().trim();
        String comment = commentET.getText().toString().trim();
        String description = descriptionET.getText().toString().trim();
        int color = ((ColorDrawable) colorIB.getBackground()).getColor();

        try {
            dateTime = swapDateToMiliseconds(dateET.getText().toString(), timeET.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (address.equals(getString(R.string.calendar_localization))) {

            address = "";
            lat = 0;
            lon = 0;
        }


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseEvents = database.getReference("Events");

        Map<String, String> eventMap = new HashMap<>();
        eventMap.put("uid", userId);
        eventMap.put("name", name);
        eventMap.put("date_time", String.valueOf(dateTime));
        eventMap.put("address", address);
        eventMap.put("lat", String.valueOf(lat));
        eventMap.put("lon", String.valueOf(lon));
        eventMap.put("comment", comment);
        eventMap.put("description", description);
        eventMap.put("color", String.valueOf(color));

        databaseEvents.child(userId).child(id).setValue(eventMap);
        databaseEvents.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {

                    Event event = new Event();
                    event.setId(id);
                    event.setUserId(dataSnapshot.child(userId).child(id).child("uid").getValue(String.class));
                    event.setName(dataSnapshot.child(userId).child(id).child("name").getValue(String.class));
                    event.setDateTime(Long.valueOf(Objects.requireNonNull(dataSnapshot.child(userId).child(id).child("date_time").getValue(String.class))));
                    event.setAddress(dataSnapshot.child(userId).child(id).child("address").getValue(String.class));
                    event.setLat(Double.valueOf(Objects.requireNonNull(dataSnapshot.child(userId).child(id).child("lat").getValue(String.class))));
                    event.setLon(Double.valueOf(Objects.requireNonNull(dataSnapshot.child(userId).child(id).child("lon").getValue(String.class))));
                    event.setComment(dataSnapshot.child(userId).child(id).child("comment").getValue(String.class));
                    event.setDescription(dataSnapshot.child(userId).child(id).child("description").getValue(String.class));
                    event.setColor(Integer.valueOf(Objects.requireNonNull(dataSnapshot.child(userId).child(id).child("color").getValue(String.class))));
                    event.update();

                    clearEditText();
                    Toast.makeText(getApplicationContext(), getString(R.string.add_event), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AddEventActivity.this, "Błąd podczas dodawania", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void addEvent() {

        String userId = firebaseUser.getUid();
        String name = nameET.getText().toString().trim();
        long dateTime = 0;
        String address = addressET.getText().toString().trim();
        String comment = commentET.getText().toString().trim();
        String description = descriptionET.getText().toString().trim();
        int color = ((ColorDrawable) colorIB.getBackground()).getColor();

        try {
            dateTime = swapDateToMiliseconds(dateET.getText().toString(), timeET.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (address.equals(getString(R.string.calendar_localization))) {

            address = "";
            lat = 0;
            lon = 0;
        }


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseEvents = database.getReference("Events").child(userId).push();

        Map<String, String> eventMap = new HashMap<>();
        eventMap.put("uid", userId);
        eventMap.put("name", name);
        eventMap.put("date_time", String.valueOf(dateTime));
        eventMap.put("address", address);
        eventMap.put("lat", String.valueOf(lat));
        eventMap.put("lon", String.valueOf(lon));
        eventMap.put("comment", comment);
        eventMap.put("description", description);
        eventMap.put("color", String.valueOf(color));

        databaseEvents.setValue(eventMap);
        databaseEvents.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {

                    Event event = new Event();
                    event.setId(dataSnapshot.getKey());
                    event.setUserId(dataSnapshot.child("uid").getValue(String.class));
                    event.setName(dataSnapshot.child("name").getValue(String.class));
                    event.setDateTime(Long.valueOf(Objects.requireNonNull(dataSnapshot.child("date_time").getValue(String.class))));
                    event.setAddress(dataSnapshot.child("address").getValue(String.class));
                    event.setLat(Double.valueOf(Objects.requireNonNull(dataSnapshot.child("lat").getValue(String.class))));
                    event.setLon(Double.valueOf(Objects.requireNonNull(dataSnapshot.child("lon").getValue(String.class))));
                    event.setComment(dataSnapshot.child("comment").getValue(String.class));
                    event.setDescription(dataSnapshot.child("description").getValue(String.class));
                    event.setColor(Integer.valueOf(Objects.requireNonNull(dataSnapshot.child("color").getValue(String.class))));
                    event.insert();

                    clearEditText();
                    Toast.makeText(getApplicationContext(), getString(R.string.add_event), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AddEventActivity.this, "Błąd podczas dodawania", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean checkLayoutsErrors(String name, String date, String time) {

        if (name.isEmpty()) {

            layoutDate.setErrorEnabled(false);
            layoutTime.setErrorEnabled(false);

            layoutDate.clearAnimation();
            layoutTime.clearAnimation();

            layoutName.setAnimation(animShake);
            layoutName.startAnimation(animShake);

            if (checkVibrationMode())
                vib.vibrate(120);

            layoutName.setErrorEnabled(true);
            layoutName.setError(getString(R.string.add_error));

            return false;
        } else if (date.isEmpty()) {

            layoutName.setErrorEnabled(false);
            layoutTime.setErrorEnabled(false);

            layoutName.clearAnimation();
            layoutTime.clearAnimation();

            layoutDate.setAnimation(animShake);
            layoutDate.startAnimation(animShake);

            if (checkVibrationMode())
                vib.vibrate(120);
            layoutDate.setErrorEnabled(true);
            layoutDate.setError(getString(R.string.add_error));

            return false;
        } else if (time.isEmpty()) {

            layoutName.setErrorEnabled(false);
            layoutDate.setErrorEnabled(false);

            layoutName.clearAnimation();
            layoutDate.clearAnimation();

            layoutTime.setAnimation(animShake);
            layoutTime.startAnimation(animShake);

            if (checkVibrationMode())
                vib.vibrate(120);
            layoutTime.setErrorEnabled(true);
            layoutTime.setError(getString(R.string.add_error));

            return false;
        }

        return true;
    }


    private void clearEditText() {

        nameET.setText("");
        dateET.setText("");
        timeET.setText("");
        addressET.setText("");
        commentET.setText("");
        descriptionET.setText("");
    }


    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

            month++;
            dateET.setText((dayOfMonth + "/" + month + "/" + year));
        }
    };


    private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            timeET.setText((hourOfDay + ":" + checkDigit(minute))); //dodanie 0 do minut
        }
    };


    private String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }


    SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }


    double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }


    private long swapDateToMiliseconds(String date, String time) throws ParseException {

        String myDate = date + " " + time; //"2014/10/29 18:10:45";
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date d = sdf.parse(myDate);
        return Long.parseLong(String.valueOf(d.getTime()));
    }


    public void onClickAddress(View view) {

        addressIV.setImageDrawable(getDrawable(R.drawable.ic_position_blue));

        Intent intent = new Intent(AddEventActivity.this, MapsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in_activity, R.anim.fade_out_activity);
    }


    public void onClickClearAddress(View view) {

        addressET.setText("");
        layoutComment.setVisibility(View.GONE);
        clearAddressIB.setVisibility(View.GONE);
    }


    public void onClickColor(View view) {

        ColorPicker colorPicker = new ColorPicker(AddEventActivity.this).setTitle(getString(R.string.chooseColor));
        colorPicker.show();

        colorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
            @Override
            public void onChooseColor(int position, int color) {

                if (color != 0)
                    colorIB.setBackgroundColor(color);
            }

            @Override
            public void onCancel() {
            }
        });
    }


    public void onClickDone(View view) {

        if (checkInternetConnection()) {

            String name = nameET.getText().toString().trim();
            String date = dateET.getText().toString().trim();
            String time = timeET.getText().toString().trim();
            String address = addressET.getText().toString().trim();
            String comment = commentET.getText().toString().trim();
            String description = descriptionET.getText().toString().trim();
            int color = ((ColorDrawable) colorIB.getBackground()).getColor();

            if (address.equals(getString(R.string.calendar_localization))) {

                address = "";
                lat = 0;
                lon = 0;
            }

            if (update)
                checkIsSomethingNew(name, date, time, address, comment, description, color);
            else {

                if (checkLayoutsErrors(name, date, time)) {

                    layoutTime.clearFocus();
                    addEvent();
                }
            }
        } else
            Toast.makeText(this, getString(R.string.no_Internet_connection), Toast.LENGTH_SHORT).show();
    }
}