package co.sepin.thedeal.model;


import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.materialize.holder.StringHolder;

import java.util.List;

import co.sepin.thedeal.R;
import co.sepin.thedeal.application.ModeClass;
import de.hdodenhof.circleimageview.CircleImageView;

import static co.sepin.thedeal.application.UpdateData.firebaseUser;

public class Chat extends AbstractItem<Chat, Chat.ViewHolder> {

    private String id;
    private String userId;
    private String senderTelephone;
    private String receiverId;
    private String receiverTelephone;
    private String message;
    private long dateTime;
    private String types;


    public Chat() {
    }


    public Chat(String id, String userId, String senderTelephone, String receiverId, String receiverTelephone, String message, long dateTime, String types) {

        this.id = id;
        this.userId = userId;
        this.senderTelephone = senderTelephone;
        this.receiverId = receiverId;
        this.receiverTelephone = receiverTelephone;
        this.message = message;
        this.dateTime = dateTime;
        this.types = types;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSenderTelephone() {
        return senderTelephone;
    }

    public void setSenderTelephone(String senderTelephone) {
        this.senderTelephone = senderTelephone;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverTelephone() {
        return receiverTelephone;
    }

    public void setReceiverTelephone(String receiverTelephone) {
        this.receiverTelephone = receiverTelephone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }


    @Override
    public int getType() {
        return R.id.item_message_listCL;
    }


    @Override
    public int getLayoutRes() {
        return R.layout.item_chat_list;
    }
    

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }


    protected static class ViewHolder extends FastAdapter.ViewHolder<Chat> {

        CircleImageView receiverAvatarCIV;
        TextView senderMessageTV, receiverMessageTV;
        TextView senderDateTimeTV, receiverDateTimeTV;
        ConstraintLayout senderCL, receiverCL;

        public ViewHolder(View itemView) {
            super(itemView);

            senderMessageTV = (TextView) itemView.findViewById(R.id.item_chat_list_sender_messageTV);
            receiverMessageTV = (TextView) itemView.findViewById(R.id.item_chat_list_receiver_messageTV);
            receiverAvatarCIV = (CircleImageView) itemView.findViewById(R.id.item_chat_list_avatarCIV);
            senderCL = (ConstraintLayout) itemView.findViewById(R.id.item_chat_list_senderCL1);
            receiverCL = (ConstraintLayout) itemView.findViewById(R.id.item_chat_list_receiverCL1);
            senderDateTimeTV = (TextView) itemView.findViewById(R.id.item_chat_list_sender_date_timeTV);
            receiverDateTimeTV = (TextView) itemView.findViewById(R.id.item_chat_list_receiver_date_timeTV);
        }

        @Override
        public void bindView(Chat item, List<Object> payloads) {
            //StringHolder.applyTo(item., name);
            //StringHolder.applyToOrHide(item.description, description);

            setAvatar(item);
            setMessage(item);
        }


        @Override
        public void unbindView(Chat item) {

        }


        private void setAvatar(Chat item) {

            if (!item.senderTelephone.equals(firebaseUser.getPhoneNumber())) {

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference databaseProfiles = database.getReference("Profiles");
                databaseProfiles.child(item.senderTelephone).addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild("avatar_uri")) {

                            String receiverAvatarUri = String.valueOf(dataSnapshot.child("avatar_uri").getValue());

                            RequestOptions requestOptions = new RequestOptions()
                                    .error(R.drawable.user_profil)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .priority(Priority.HIGH);

                            Glide.with(itemView.getContext())
                                    .load(receiverAvatarUri)
                                    .apply(requestOptions)
                                    .into(receiverAvatarCIV);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        }


        private void setMessage(Chat item) {

            senderCL.setVisibility(View.GONE);
            receiverCL.setVisibility(View.GONE);
            receiverAvatarCIV.setVisibility(View.GONE);

            if (item.types.equals("text")) {

                if (item.userId != null && item.userId.equals(firebaseUser.getUid())) {

                    senderCL.setVisibility(View.VISIBLE);
                    senderCL.setBackgroundResource(R.drawable.sender_messages_layout);
                    senderMessageTV.setText(item.message);
                    senderDateTimeTV.setText(ModeClass.convertUnixToDate1(itemView.getContext(), item.dateTime));
                } else {

                    receiverAvatarCIV.setVisibility(View.VISIBLE);
                    receiverCL.setVisibility(View.VISIBLE);
                    receiverCL.setBackgroundResource(R.drawable.receiver_messages_layout);
                    receiverMessageTV.setText(item.message);
                    receiverDateTimeTV.setText(ModeClass.convertUnixToDate1(itemView.getContext(), item.dateTime));
                }
            }
        }
    }
}