package co.sepin.thedeal.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import co.sepin.thedeal.EventDetailsActivity;
import co.sepin.thedeal.R;
import co.sepin.thedeal.database.Event;
import co.sepin.thedeal.interfaces.CalendarInterface;

import static co.sepin.thedeal.application.ModeClass.checkInternetConnection;


public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private List<Event> eventsList;
    private LayoutInflater layoutInflater;
    private ItemClickListener itemClickListener;
    private Context context;
    private CalendarInterface calendarInterface;


    public EventAdapter(Context context, List<Event> eventsList, CalendarInterface calendarInterface) {

        this.layoutInflater = LayoutInflater.from(context);
        this.eventsList = eventsList;
        this.context = context;
        this.calendarInterface = calendarInterface;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = layoutInflater.inflate(R.layout.item_event_list, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        String date = getDate(eventsList.get(position).getDateTime());
        String loc = eventsList.get(position).getAddress();
        String com = eventsList.get(position).getComment();
        String des = eventsList.get(position).getDescription();

        holder.nameTV.setText(eventsList.get(position).getName());
        holder.dotTV.setTextColor(eventsList.get(position).getColor());
        holder.dateTV.setText(date);

        if (loc == null || loc.isEmpty())
            holder.addressTV.setVisibility(View.GONE);
        else if (com == null || com.isEmpty()) {

            holder.addressTV.setVisibility(View.VISIBLE);
            holder.addressTV.setText(String.format(" | %s", loc));
        } else {

            holder.addressTV.setVisibility(View.VISIBLE);
            holder.addressTV.setText(String.format(" | %s (%s)", loc, com));
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
                extras.putString("Name", eventsList.get(position).getName());
                extras.putString("Date", getDate(eventsList.get(position).getDateTime()));
                extras.putString("Address", eventsList.get(position).getAddress());
                extras.putDouble("Lat", eventsList.get(position).getLat());
                extras.putDouble("Lon", eventsList.get(position).getLon());
                extras.putString("Comment", eventsList.get(position).getComment());
                extras.putString("Description", eventsList.get(position).getDescription());
                extras.putInt("Color", eventsList.get(position).getColor());

                eventDetailsIntent.putExtra("Event Details", extras);

                Pair<View, String> pair1 = Pair.create((View) holder.nameTV, holder.nameTV.getTransitionName());
                Pair<View, String> pair2 = Pair.create((View) holder.dateTV, holder.dateTV.getTransitionName());
                Pair<View, String> pair3 = Pair.create((View) holder.addressTV, holder.addressTV.getTransitionName());
                Pair<View, String> pair4 = Pair.create((View) holder.descriptionTV, holder.descriptionTV.getTransitionName());

                int i = 4;
                if (eventsList.get(position).getAddress().isEmpty())
                    i--;
                if (eventsList.get(position).getDescription().isEmpty())
                    i--;

                Pair[] pairs = new Pair[i];
                pairs[0] = pair1;
                pairs[1] = pair2;

                if (i == 3) {

                    if ((eventsList.get(position).getAddress().isEmpty()))
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

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext());
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

                        editEvent(position);
                        alertDialog.cancel();
                    }
                });

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        deleteEvent(position);
                        alertDialog.dismiss();
                    }
                });
                return false;
            }
        });

    }


    @Override
    public int getItemCount() {
        return eventsList.size();
    }


    public Object getItem(int position) {
        return eventsList.get(position);
    }


    public void reload(List<Event> listE) {

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


    private void editEvent(int position) {

        SharedPreferences.Editor editor = context.getSharedPreferences("Update", Context.MODE_PRIVATE).edit();
        editor.putBoolean("update", true);
        editor.putString("id", eventsList.get(position).getId());
        editor.putString("userId", eventsList.get(position).getUserId());
        editor.putString("name", eventsList.get(position).getName());
        editor.putString("date", getDate(eventsList.get(position).getDateTime()).substring(0, 10));
        editor.putString("time", getDate(eventsList.get(position).getDateTime()).substring(11, 16));
        editor.putString("address", eventsList.get(position).getAddress());
        putDouble(editor, "lat", eventsList.get(position).getLat());
        putDouble(editor, "lon", eventsList.get(position).getLon());
        editor.putString("comment", eventsList.get(position).getComment());
        editor.putString("description", eventsList.get(position).getDescription());
        editor.putInt("color", eventsList.get(position).getColor());
        editor.apply();

        calendarInterface.updateEvent();
    }


    private void deleteEvent(int position) {

        String id = eventsList.get(position).getId();
        String userId = eventsList.get(position).getUserId();

        if (checkInternetConnection() && eventsList != null && eventsList.size() != 0) {

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseEvents = database.getReference("Events");

            databaseEvents.child(userId).child(id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {

                @Override
                public void onSuccess(Void aVoid) {

                    eventsList.get(position).delete(); // usuwanie z bazy
                    eventsList.remove(position); // usuwanie z listy
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, eventsList.size());
                    calendarInterface.removeDot(); // usuniecie kropki w CalendarActivity*/

                    Toast.makeText(context, context.getResources().getString(R.string.remove_event_from_calendar), Toast.LENGTH_SHORT).show();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {

                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, context.getResources().getString(R.string.not_remove_event), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else if (!checkInternetConnection())
            Toast.makeText(context, context.getResources().getString(R.string.no_Internet_connection), Toast.LENGTH_SHORT).show();
    }


    SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView nameTV, dotTV, dateTV, addressTV, descriptionTV;

        ViewHolder(View itemView) {
            super(itemView);

            nameTV = itemView.findViewById(R.id.item_event_list_nameTV);
            dateTV = itemView.findViewById(R.id.item_event_list_dateTV);
            dotTV = itemView.findViewById(R.id.item_event_list_dotTV);
            addressTV = itemView.findViewById(R.id.item_event_list_addressTV);
            descriptionTV = itemView.findViewById(R.id.item_event_list_descriptionTV);
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