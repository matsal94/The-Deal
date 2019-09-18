package co.sepin.thedeal;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import co.sepin.thedeal.database.Event;
import co.sepin.thedeal.database.Event_Table;


public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private List<Event> eventsList;
    private LayoutInflater layoutInflater;
    private ItemClickListener itemClickListener;
    private Context context;
    private CalendarInterface calendarInterface;

    EventAdapter(Context context, List<Event> eventsList, CalendarInterface calendarInterface) {

        this.layoutInflater = LayoutInflater.from(context);
        this.eventsList = eventsList;
        this.context = context;
        this.calendarInterface = calendarInterface;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = layoutInflater.inflate(R.layout.item_event_list, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        String date = getDate(eventsList.get(position).getCreateDate());
        String loc = eventsList.get(position).getLocalization();
        String com = eventsList.get(position).getComment();
        String des = eventsList.get(position).getDescription();

        holder.nameTV.setText(eventsList.get(position).getName());
        holder.dotTV.setTextColor(eventsList.get(position).getColor());
        holder.dateTV.setText(date);

        if (loc == null || loc.isEmpty())
            holder.localizationTV.setVisibility(View.GONE);
        else if (com == null || com.isEmpty()) {

            holder.localizationTV.setVisibility(View.VISIBLE);
            holder.localizationTV.setText(String.format(" | %s", loc));
        } else {

            holder.localizationTV.setVisibility(View.VISIBLE);
            holder.localizationTV.setText(String.format(" | %s (%s)", loc, com));
        }

        if (des == null || des.isEmpty())
            holder.descriptionTV.setVisibility(View.GONE);
        else {

            holder.descriptionTV.setVisibility(View.VISIBLE);
            holder.descriptionTV.setText(eventsList.get(position).getDescription());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent eventDetailsIntent = new Intent(context, EventDetailsActivity.class);

                Bundle extras = new Bundle();
                extras.putInt("Color", eventsList.get(position).getColor());
                extras.putString("Name", eventsList.get(position).getName());
                extras.putString("Date", getDate(eventsList.get(position).getCreateDate()));
                extras.putString("Localization", eventsList.get(position).getLocalization());
                extras.putString("Comment", eventsList.get(position).getComment());
                extras.putString("Description", eventsList.get(position).getDescription());
                eventDetailsIntent.putExtra("Event Details", extras);

                Pair<View, String> pair1 = Pair.create((View) holder.nameTV, holder.nameTV.getTransitionName());
                Pair<View, String> pair2 = Pair.create((View) holder.dateTV, holder.dateTV.getTransitionName());
                Pair<View, String> pair3 = Pair.create((View) holder.localizationTV, holder.localizationTV.getTransitionName());
                Pair<View, String> pair4 = Pair.create((View) holder.descriptionTV, holder.descriptionTV.getTransitionName());

                int i = 4;
                if (eventsList.get(position).getLocalization().isEmpty())
                    i--;
                if (eventsList.get(position).getDescription().isEmpty())
                    i--;

                Pair[] pairs = new Pair[i];
                pairs[0] = pair1;
                pairs[1] = pair2;

                if (i == 3) {

                    if ((eventsList.get(position).getLocalization().isEmpty()))
                        pairs[2] = pair4;

                    if ((eventsList.get(position).getDescription().isEmpty()))
                        pairs[2] = pair3;
                }
                if (i == 4) {

                    pairs[2] = pair3;
                    pairs[3] = pair4;
                }


                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation((Activity) context, pairs);
                context.startActivity(eventDetailsIntent, activityOptions.toBundle());
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext()); // getContext()
                View dialogView = layoutInflater.inflate(R.layout.event_dialog, null);
                dialogBuilder.setView(dialogView);

                Button edit = (Button) dialogView.findViewById(R.id.event_dialog_editBtn);
                Button delete = (Button) dialogView.findViewById(R.id.event_dialog_deleteBtn);
                TextView title = (TextView) dialogView.findViewById(R.id.event_dialog_titleTV);
                title.setText(eventsList.get(position).getName());

                final AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();

                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.edit(position);
                        alertDialog.cancel();
                    }
                });

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        holder.delete(position);
                        alertDialog.dismiss();
                    }
                });
                return false;
            }
        });

    }


    // total number of rows
    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    // convenience method for getting data at click position
    public Object getItem(int position) {
        return eventsList.get(position);
    }


    public void reload(List<Event> listE) {

        //eventsList = new ArrayList<>();
        //eventsList.addAll(listE);
        this.eventsList = listE;
        notifyDataSetChanged();
    }


    private String getDate(long milliSeconds) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView nameTV, dotTV, dateTV, localizationTV, descriptionTV;

        ViewHolder(View itemView) {
            super(itemView);

            nameTV = itemView.findViewById(R.id.item_event_list_nameTV);
            dateTV = itemView.findViewById(R.id.item_event_list_dateTV);
            dotTV = itemView.findViewById(R.id.item_event_list_dot);
            localizationTV = itemView.findViewById(R.id.item_event_list_localizationTV);
            descriptionTV = itemView.findViewById(R.id.item_event_list_descriptionTV);
        }


        private void edit(int position) {

            SharedPreferences.Editor editor = context.getSharedPreferences("Update", Context.MODE_PRIVATE).edit();
            editor.putBoolean("update", true);
            editor.putLong("id", eventsList.get(position).getId());
            editor.putLong("userId", eventsList.get(position).getUserId());
            editor.putString("name", eventsList.get(position).getName());
            editor.putString("date", getDate(eventsList.get(position).getCreateDate()).substring(0, 10));
            editor.putString("time", getDate(eventsList.get(position).getCreateDate()).substring(11, 16));
            editor.putString("localization", eventsList.get(position).getLocalization());
            editor.putString("comment", eventsList.get(position).getComment());
            editor.putString("description", eventsList.get(position).getDescription());
            editor.putInt("color", eventsList.get(position).getColor());
            editor.apply();

            calendarInterface.updateEvent();
        }


        private void delete(int position) {

            if (eventsList != null && eventsList.size() != 0) {

                SQLite.delete().from(Event.class).where(Event_Table.name.is(eventsList.get(position).getName())).
                        and(Event_Table.userId.is(eventsList.get(position).getUserId())).async().execute();


                // dodać usuwanie z serwera

                Toast.makeText(context, "Usunięto wydarzenie z bazy", Toast.LENGTH_LONG).show();

                eventsList.remove(position);
                notifyItemRemoved(position);
                reload(eventsList);

                calendarInterface.removeDot(); // usuniecie kropki w CalendarActivity*/
            }
        }


        @Override
        public void onClick(View view) {
            if (itemClickListener != null)
                itemClickListener.onItemClick(view, getAdapterPosition());
        }
    }


    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}