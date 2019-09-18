package co.sepin.thedeal.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.List;

import co.sepin.thedeal.ContactActivity;
import co.sepin.thedeal.R;
import co.sepin.thedeal.database.Contact;
import co.sepin.thedeal.database.Contact_Table;
import co.sepin.thedeal.interfaces.ContactInterface;
import de.hdodenhof.circleimageview.CircleImageView;

import static co.sepin.thedeal.application.ModeClass.checkInternetConnection;
import static co.sepin.thedeal.application.UpdateData.firebaseUser;
import static co.sepin.thedeal.application.UpdateData.updateDealsFromFirebase;


public class BusinessCardHolderAdapter extends RecyclerView.Adapter<BusinessCardHolderAdapter.ViewHolder> {

    private List<Contact> contactsList;
    private LayoutInflater layoutInflater;
    private ItemClickListener itemClickListener;
    private ContactInterface contactInterface;
    private Context context;
    private int deletePosition = -1;


    public BusinessCardHolderAdapter(Context context, List<Contact> contactList, ContactInterface contactInterface) {

        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.contactsList = contactList;
        this.contactInterface = contactInterface;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = layoutInflater.inflate(R.layout.item_contact_list, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.nameTV.setText(contactsList.get(position).getName());
        String phoneNumber = contactsList.get(position).getTelephone();

        formatPhoneNumber(phoneNumber, holder, position);
        setUpAvatars(holder, position);
        onClickItemView(holder, position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    } // nie powoduje dublowania items co kilka pozycji po kliknięciu


    @Override
    public int getItemViewType(int position) {
        return position;
    } // nie powoduje dublowania items co kilka pozycji po kliknięciu


    @Override
    public int getItemCount() {
        return contactsList.size();
    }


    public Object getItem(int position) {
        return contactsList.get(position);
    }


    private void formatPhoneNumber(String phoneNumber, ViewHolder holder, int position) {

        if (phoneNumber.startsWith("+")) {

            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String countryCode = telephonyManager.getSimCountryIso();
            phoneNumber = PhoneNumberUtils.formatNumber(phoneNumber, countryCode);
            holder.telephoneTV.setText(phoneNumber);
        } else
            holder.telephoneTV.setText(contactsList.get(position).getTelephone());
    }


    private void setUpAvatars(final ViewHolder holder, final int position) {

        final String uri = contactsList.get(position).getAvatarUri();

        RequestOptions requestOptions = new RequestOptions()
                .error(R.drawable.user_profil)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);

        Glide.with(context)
                .load(uri)
                .apply(requestOptions)
                .into(holder.avatarCIV);
    }


    private void onClickItemView(final ViewHolder holder, final int position) {

        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (deletePosition != position) { // zapobiegnięcie ponownego kliknięcia podczas usuwania

                    Intent contactIntent = new Intent(context, ContactActivity.class);

                    Bundle extras = new Bundle();
                    extras.putString("AvatarUri", contactsList.get(position).getAvatarUri());
                    extras.putString("Uid", contactsList.get(position).getUserId());
                    extras.putString("Name", contactsList.get(position).getName());
                    extras.putString("Telephone", contactsList.get(position).getTelephone());
                    extras.putString("AboutMe", contactsList.get(position).getAboutMe());
                    extras.putString("Email", contactsList.get(position).getEmail());
                    contactIntent.putExtra("Contact", extras);


                    Pair<View, String> pair1 = Pair.create((View) holder.avatarCIV, holder.avatarCIV.getTransitionName());
                    //Pair<View, String> pair2 = Pair.create((View) holder.nameTV, holder.nameTV.getTransitionName());
                    //Pair<View, String> pair3 = Pair.create((View) holder.telephoneTV, holder.telephoneTV.getTransitionName());
                    Pair pair4 = Pair.create(holder.iconCall, holder.iconCall.getTransitionName());
                    Pair pair5 = Pair.create(holder.iconMessage, holder.iconMessage.getTransitionName());

                    ActivityOptions activityOptions = (ActivityOptions) ActivityOptions.makeSceneTransitionAnimation((Activity) context, pair1/*, pair2*//*, pair3*/, pair4, pair5);
                    //ActivityOptionsCompat transitionActivityOptions = (ActivityOptionsCompat) ActivityOptions.makeSceneTransitionAnimation((Activity) context, pair1/*, pair2*//*, pair3*/, pair5);
                    context.startActivity(contactIntent, activityOptions.toBundle());
                }
            }
        });
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CircleImageView avatarCIV;
        ProgressBar loadingPB;
        ImageView iconCall, iconMessage;
        TextView nameTV, telephoneTV;


        ViewHolder(View itemView) {
            super(itemView);

            avatarCIV = (CircleImageView) itemView.findViewById(R.id.item_contact_list_avatarCIV);
            loadingPB = (ProgressBar) itemView.findViewById(R.id.item_contact_listPB);
            iconCall = (ImageView) itemView.findViewById(R.id.item_contact_list_icon_call);
            iconMessage = (ImageView) itemView.findViewById(R.id.item_contact_list_icon_message);
            nameTV = (TextView) itemView.findViewById(R.id.item_contact_list_nameTV);
            telephoneTV = (TextView) itemView.findViewById(R.id.item_contact_list_telephoneTV);

            //itemView.setOnClickListener(this);
            iconCall.setOnClickListener(this);
            iconMessage.setOnClickListener(this);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {

                    if (deletePosition != getAdapterPosition()) { // zapobiegnięcie ponownego kliknięcia podczas usuwania

                        //final LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext()); // getContext()
                        View dialogView = layoutInflater.inflate(R.layout.deal_dialog, null);
                        dialogBuilder.setView(dialogView);

                        Button delete = (Button) dialogView.findViewById(R.id.deal_dialog_deleteBtn);
                        TextView title = (TextView) dialogView.findViewById(R.id.deal_dialog_titleTV);
                        title.setText(contactsList.get(getAdapterPosition()).getName());

                        final AlertDialog alertDialog = dialogBuilder.create();
                        alertDialog.show();

                        delete.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {

                                delete(getAdapterPosition());
                                alertDialog.dismiss();
                            }
                        });
                    }
                    return false;
                }
            });
        }


        @Override
        public void onClick(View view) {

            if (itemClickListener != null)
                itemClickListener.onItemClick(view, getAdapterPosition());

            String telephoneNumber = contactsList.get(getAdapterPosition()).getTelephone();

            if (view.getId() == iconCall.getId())
                contactInterface.makeCall(telephoneNumber);

            if (view.getId() == iconMessage.getId())
                contactInterface.writeMessage(contactsList.get(getAdapterPosition()));
        }
    }


    public void reload(List<Contact> listC) {

        contactsList = new ArrayList<>();
        contactsList.addAll(listC);
        notifyDataSetChanged();
    }


    private void delete(int position) {

        if (contactsList != null && contactsList.size() != 0 && checkInternetConnection()) {

            deletePosition = position;
            deleteFromFirebase(position);
        } else
            Toast.makeText(context, context.getResources().getString(R.string.no_Internet_connection), Toast.LENGTH_SHORT).show();
    }


    private void deleteFromFirebase(final int position) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseContacts = database.getReference("Contacts");

        databaseContacts.child(firebaseUser.getUid()).child(contactsList.get(position).getName()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        deletePosition = -1;
                        Toast.makeText(context, "Usunięto kontakt z listy", Toast.LENGTH_LONG).show();
                    }
                });

        SQLite.delete().from(Contact.class).where(Contact_Table.name.is(contactsList.get(position).getName())).async().execute();
        updateDealsFromFirebase();
        contactsList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, contactsList.size());
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