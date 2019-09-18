package co.sepin.thedeal.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.sepin.thedeal.ChatActivity;
import co.sepin.thedeal.R;
import co.sepin.thedeal.database.Contact;
import de.hdodenhof.circleimageview.CircleImageView;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Contact> contactsList;
    private LayoutInflater layoutInflater;
    private Context context;


    public MessageAdapter(Context context, List<Contact> contactList) {

        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.contactsList = contactList;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = layoutInflater.inflate(R.layout.item_message_list, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.nameTV.setText(contactsList.get(position).getName());

        setAvatars(holder, position);
        setStatus(holder, position);
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


    private void setAvatars(final ViewHolder holder, final int position) {

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


    private void setStatus(ViewHolder holder, int position) {

        String status = contactsList.get(position).getStatus();
        long lastSeen = Long.parseLong(contactsList.get(position).getLastSeen());

        Drawable white = new ColorDrawable(context.getResources().getColor(R.color.white));
        Drawable green = new ColorDrawable(context.getResources().getColor(R.color.green));
        Drawable orange = new ColorDrawable(context.getResources().getColor(R.color.orange2));

        switch (status) {

            case "Offline":

                holder.statusCIV.setImageDrawable(white);
                holder.statusTV.setText(status);
                break;
            case "Online":

                long currentTime = new Date().getTime();
                long minuteBefore = (currentTime - 90 * 1000);

                if (minuteBefore > lastSeen) {

                    holder.statusCIV.setImageDrawable(orange);
                    holder.statusTV.setVisibility(View.GONE);
                }
                else {

                    holder.statusCIV.setImageDrawable(green);
                    holder.statusTV.setText(status);
                    holder.statusTV.setVisibility(View.VISIBLE);
                }
                break;
        }
    }


    public void reload(List<Contact> listC) {

        contactsList = new ArrayList<>();
        contactsList.addAll(listC);
        notifyDataSetChanged();
    }


    private void onClickItemView(final ViewHolder holder, final int position) {

        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent messageIntent = new Intent(context, ChatActivity.class);

                Bundle extras = new Bundle();
                extras.putString("Uid", contactsList.get(position).getUserId());
                extras.putString("Name", contactsList.get(position).getName());
                extras.putString("Telephone", contactsList.get(position).getTelephone());
                //extras.putString("Status", contactsList.get(position).getStatus());
                //extras.putLong("Last seen", Long.parseLong(contactsList.get(position).getLastSeen()));
                messageIntent.putExtra("Message", extras);

                context.startActivity(messageIntent);
            }
        });
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView avatarCIV, statusCIV;
        TextView nameTV, statusTV;


        ViewHolder(View itemView) {
            super(itemView);

            avatarCIV = (CircleImageView) itemView.findViewById(R.id.item_message_list_avatarCIV);
            statusCIV = (CircleImageView) itemView.findViewById(R.id.item_message_list_statusCIV);
            nameTV = (TextView) itemView.findViewById(R.id.item_message_list_nameTV);
            statusTV = (TextView) itemView.findViewById(R.id.item_message_list_statusTV);
        }
    }
}