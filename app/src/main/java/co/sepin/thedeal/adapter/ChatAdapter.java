package co.sepin.thedeal.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import co.sepin.thedeal.R;
import co.sepin.thedeal.application.ModeClass;
import co.sepin.thedeal.model.Chat;
import de.hdodenhof.circleimageview.CircleImageView;

import static co.sepin.thedeal.application.UpdateData.firebaseUser;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private Context context;
    private List<Chat> chatList;


    public ChatAdapter(Context context, List<Chat> chatList) {

        this.context = context;
        this.chatList = chatList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_list, viewGroup, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        String senderId = chatList.get(position).getUserId();
        String senderTelephone = chatList.get(position).getSenderTelephone();
        String message = chatList.get(position).getMessage();
        long dateTime = chatList.get(position).getDateTime();
        String messageType = chatList.get(position).getTypes();

        setAvatar(holder, senderTelephone);
        setMessage(holder, senderId, message, dateTime, messageType);
    }


    private void setAvatar(ViewHolder holder, String senderTelephone) {

        if (!senderTelephone.equals(firebaseUser.getPhoneNumber())) {

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseProfiles = database.getReference("Profiles");
            databaseProfiles.child(senderTelephone).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.hasChild("avatar_uri")) {

                        String receiverAvatarUri = String.valueOf(dataSnapshot.child("avatar_uri").getValue());

                        RequestOptions requestOptions = new RequestOptions()
                                .error(R.drawable.user_profil)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .priority(Priority.HIGH);

                        Glide.with(context)
                                .load(receiverAvatarUri)
                                .apply(requestOptions)
                                .into(holder.receiverAvatarCIV);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }


    private void setMessage(ViewHolder holder, String senderId, String message, long dateTime, String messageType) {

        holder.senderCL.setVisibility(View.GONE);
        holder.receiverCL.setVisibility(View.GONE);
        holder.receiverAvatarCIV.setVisibility(View.GONE);

        if (!chatList.isEmpty() && messageType.equals("text")) {

            if (senderId != null && senderId.equals(firebaseUser.getUid())) {

                holder.senderCL.setVisibility(View.VISIBLE);
                holder.senderCL.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderMessageTV.setText(message);
                holder.senderDateTimeTV.setText(ModeClass.convertUnixToDate1(context, dateTime));
            } else {

                holder.receiverAvatarCIV.setVisibility(View.VISIBLE);
                holder.receiverCL.setVisibility(View.VISIBLE);
                holder.receiverCL.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverMessageTV.setText(message);
                holder.receiverDateTimeTV.setText(ModeClass.convertUnixToDate1(context, dateTime));
            }
        }
    }


    @Override
    public int getItemCount() {
        return chatList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView receiverAvatarCIV;
        TextView senderMessageTV, receiverMessageTV;
        TextView senderDateTimeTV, receiverDateTimeTV;
        ConstraintLayout senderCL, receiverCL;


        ViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageTV = (TextView) itemView.findViewById(R.id.item_chat_list_sender_messageTV);
            receiverMessageTV = (TextView) itemView.findViewById(R.id.item_chat_list_receiver_messageTV);
            receiverAvatarCIV = (CircleImageView) itemView.findViewById(R.id.item_chat_list_avatarCIV);
            senderCL = (ConstraintLayout) itemView.findViewById(R.id.item_chat_list_senderCL1);
            receiverCL = (ConstraintLayout) itemView.findViewById(R.id.item_chat_list_receiverCL1);
            senderDateTimeTV = (TextView) itemView.findViewById(R.id.item_chat_list_sender_date_timeTV);
            receiverDateTimeTV = (TextView) itemView.findViewById(R.id.item_chat_list_receiver_date_timeTV);
        }
    }
}