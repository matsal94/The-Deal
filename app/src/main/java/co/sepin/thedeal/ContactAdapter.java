package co.sepin.thedeal;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import co.sepin.thedeal.database.Contact;


public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private List<Contact> contactsList;
    private LayoutInflater layoutInflater;
    private ItemClickListener itemClickListener;
    private ContactInterface contactInterface;
    Context context;


    // data is passed into the constructor
    ContactAdapter(Context context, List<Contact> contactList, ContactInterface contactInterface) {

        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.contactsList = contactList;
        this.contactInterface = contactInterface;
    }


    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = layoutInflater.inflate(R.layout.item_contact_list, parent, false);
        return new ViewHolder(view);
    }


    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.avatarIV.setImageResource(contactsList.get(position).getAvatar());
        //String email = contactsList.get(position).getEmail();
        holder.nameTV.setText(contactsList.get(position).getName());
        holder.telephoneTV.setText(contactsList.get(position).getTelephone());

        /*
        if (email == null)
            holder.emailTV.setVisibility(View.GONE);
        else
            holder.emailTV.setText(contactsList.get(position).getEmail());
*/



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent contactIntent = new Intent(context, ContactActivity.class);

                Bundle extras = new Bundle();
                extras.putInt("Avatar", contactsList.get(position).getAvatar());
                extras.putString("Nazwa", contactsList.get(position).getName());
                extras.putString("Telefon", contactsList.get(position).getTelephone());
                extras.putString("Email", contactsList.get(position).getEmail());
                contactIntent.putExtra("Kontakt", extras);

                Pair<View, String> pair1 = Pair.create((View) holder.avatarIV, holder.avatarIV.getTransitionName());
                Pair<View, String> pair2 = Pair.create((View) holder.nameTV, holder.nameTV.getTransitionName());
                Pair<View, String> pair3 = Pair.create((View) holder.telephoneTV, holder.telephoneTV.getTransitionName());

                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation((Activity) context, pair1, pair2, pair3);
                context.startActivity(contactIntent, activityOptions.toBundle());
            }
        });
    }

    // total number of rows
    @Override
    public int getItemCount() { return contactsList.size();
    }


    // convenience method for getting data at click position
    public Object getItem(int position) {
        return contactsList.get(position);
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView nameTV, telephoneTV, emailTV;
        ImageView avatarIV, iconCall, iconMessage;

        ViewHolder(View itemView) {
            super(itemView);

            nameTV = (TextView) itemView.findViewById(R.id.item_contact_list_nameTV);
            telephoneTV = (TextView) itemView.findViewById(R.id.item_contact_list_telephoneTV);
            //emailTV = (TextView) itemView.findViewById(R.id.item_contact_list_emailTV);
            avatarIV = (ImageView) itemView.findViewById(R.id.item_contact_list_avatarIV);
            iconCall = (ImageView) itemView.findViewById(R.id.item_contact_list_icon_call);
            iconMessage = (ImageView) itemView.findViewById(R.id.item_contact_list_icon_message);

            //itemView.setOnClickListener(this);
            iconCall.setOnClickListener(this);
            iconMessage.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {

            if (itemClickListener != null)
                itemClickListener.onItemClick(view, getAdapterPosition());

            String telephoneNumber = contactsList.get(getAdapterPosition()).getTelephone();

            if (view.getId() == iconCall.getId())
                contactInterface.makeCall(telephoneNumber);

            if (view.getId() == iconMessage.getId()) {

                Toast.makeText(view.getContext(), "Wysyłanie wiadomości w aplikacji nie jest jeszcze dostępne", Toast.LENGTH_SHORT).show();
            }
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