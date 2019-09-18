package co.sepin.thedeal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import co.sepin.thedeal.adapter.EventAdapter;
import co.sepin.thedeal.application.DrawerClass;
import co.sepin.thedeal.database.Event;
import co.sepin.thedeal.database.Event_Table;
import co.sepin.thedeal.interfaces.CalendarInterface;


public class CalendarActivity extends DrawerClass implements CalendarInterface {

    private SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("LLLL - yyyy");
    private SimpleDateFormat dateFormatForDay = new SimpleDateFormat("dd/MM/yyyy");
    private String date = dateFormatForDay.format(Calendar.getInstance().getTime());
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private CompactCalendarView calendarView;
    private ConstraintLayout calendarLayout;
    private TextView actionBarTitle;
    private List<Event> eventsList = new ArrayList<>();
    private boolean refresh = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        calendarLayout = (ConstraintLayout) findViewById(R.id.calendarCL1);
        actionBarTitle = (TextView) findViewById(getResources().getIdentifier("calendar_action_bar_title", "id", getPackageName()));

        actionBarTitle.setText(new StringBuilder().append(dateFormatForMonth.format(Calendar.getInstance().getTime())));

        initCalendarView();
        initRecyclerView();
        showDayEvents(date); // wyświetlenie wydarzeń w aktualny dzień
    }


    @Override
    public int getContentView() {
        return R.layout.activity_calendar;
    }


    @Override
    protected void onStart() {
        super.onStart();
        showDotsEvents(); // pobranie z bazy i wyswietlenie kropek dla wszystkich wydarzeń
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (refresh) {

            refresh = false;
            showDayEvents(date); // wyświetlenie wydarzeń w aktualny dzień
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        calendarView.removeAllEvents();
    }


    @Override
    public void removeDot() {

        calendarView.removeAllEvents();
        showDotsEvents();
    }


    @Override
    public void updateEvent() {

        Intent intent = new Intent(CalendarActivity.this, AddEventActivity.class);
        startActivity(intent);
        refresh = true;
    }


    private void initRecyclerView() {

        recyclerView = (RecyclerView) findViewById(R.id.calendarRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(CalendarActivity.this, eventsList, this);
        recyclerView.setAdapter(eventAdapter);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (calendarLayout != null) {

                    if (!recyclerView.canScrollVertically(-1))
                        calendarLayout.setElevation(0f);
                    else
                        calendarLayout.setElevation(10f);
                }
            }
        });
    }


    private void initCalendarView() {

        calendarView = (CompactCalendarView) findViewById(R.id.calendarCCV);
        calendarView.setCurrentDayIndicatorStyle(CompactCalendarView.NO_FILL_LARGE_INDICATOR); // Okrąg w około dziesiejszej daty
        calendarView.shouldDrawIndicatorsBelowSelectedDays(true); // wyświetlenie kropek pod wybranym dniem
        calendarView.setUseThreeLetterAbbreviation(true); // 3 literowe nazwy tygodnia

        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {

                date = dateFormatForDay.format(dateClicked); // zamiana daty z Sat Dec 01 00:00:00 GMT 2018 na 01/12/2018
                showDayEvents(date); // wyświetlenie zdarzen w kliknietym dniu
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                actionBarTitle.setText(new StringBuilder().append(dateFormatForMonth.format(firstDayOfNewMonth)));
            }
        });
    }


    private void showDotsEvents() {

        try {

            Cursor cursor = SQLite.select(Event_Table.dateTime, Event_Table.color).from(Event.class).query();
            if ((cursor != null ? cursor.getCount() : 0) > 0) {

                long dateInMilliseconds;
                int color;

                while (cursor != null && cursor.moveToNext()) {

                    color = cursor.getInt(cursor.getColumnIndex("color"));
                    dateInMilliseconds = cursor.getLong(cursor.getColumnIndex("dateTime"));
                    com.github.sundeepk.compactcalendarview.domain.Event event = new com.github.sundeepk.compactcalendarview.domain.Event(color, dateInMilliseconds);
                    calendarView.addEvent(event);
                }
            }

            if (cursor != null)
                cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void runAnimation() {

        Context context = recyclerView.getContext();
        LayoutAnimationController controller = null;
        controller = AnimationUtils.loadLayoutAnimation(context, R.anim.recycler_layout_from_right);

        recyclerView.setLayoutAnimation(controller);
        recyclerView.scheduleLayoutAnimation();
    }


    private String getDate(long milliSeconds) {

        //SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return dateFormatForDay.format(calendar.getTime());
    }


    private void showDayEvents(String date) {

        getEventList();
        final List<Event> filtermodelist = filter(eventsList, date); // filtrowanie po wskazanej dacie
        eventAdapter.reload(filtermodelist);
        runAnimation();
    }


    private List<Event> filter(List<Event> eventsList, String query) {

        final List<Event> filteredModeList = new ArrayList<>();
        for (Event model : eventsList) {

            String createDate = getDate(model.getDateTime());
            String date = createDate.substring(0, 10); // pobranie samej daty, bez godziny
            //String date = dateFormatForDay.format(createDate);

            if (query.equals(date))
                filteredModeList.add(model);
        }
        return filteredModeList;
    }


    private void getEventList() {
        eventsList = SQLite.select().from(Event.class).queryList();
    }


    private void clearAddEventSP() { // czyszczenie na wypadek zamknięcia aplikacji bedąc w AddEvent lub Map aby pola ET były puste po ponownym uruchomieniu

        SharedPreferences prefsUpdate = getSharedPreferences("Update", MODE_PRIVATE);
        prefsUpdate.edit().clear().apply();

        SharedPreferences prefsMap = getSharedPreferences("Map", MODE_PRIVATE);
        prefsMap.edit().clear().apply();
    }


    public void onClickDone(View view) {

        refresh = true;
        clearAddEventSP();

        Intent intent = new Intent(CalendarActivity.this, AddEventActivity.class);
        startActivity(intent);
    }
}