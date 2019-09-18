package co.sepin.thedeal;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.itemanimators.AlphaCrossFadeAnimator;
import com.mikepenz.itemanimators.DefaultAnimator;
import com.mikepenz.itemanimators.ScaleUpAnimator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import co.sepin.thedeal.adapter.ChatAdapter;
import co.sepin.thedeal.application.ModeClass;
import co.sepin.thedeal.model.Chat;

import static co.sepin.thedeal.application.UpdateData.firebaseUser;
import static co.sepin.thedeal.application.UpdateData.sortChatList;


public class ChatActivity extends ModeClass {

    private DatabaseReference databaseMessages, databaseProfiles;
    private ChildEventListener messageListener;
    private ValueEventListener statusListener;
    private ChatAdapter chatAdapter;
    private FastItemAdapter<Chat> fastAdapter;
    private RecyclerView recyclerView;
    private EditText textInputET;
    private ProgressBar chatLoadingPB;
    private ConstraintLayout chatLayout;
    private List<Chat> chatList;
    private TextView subtitleTV;
    private String receiverId, receiverName, receiverTelephone, receiverStatus, receiverLastSeen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);
        hideKeyboardAndFocus(findViewById(R.id.chatCL), ChatActivity.this);

        chatLoadingPB = (ProgressBar) findViewById(R.id.chatPB);
        chatLayout = (ConstraintLayout) findViewById(R.id.chatCL1);
        textInputET = (EditText) findViewById(R.id.chat_input_messageET);

        Bundle extras = getIntent().getBundleExtra("Message");
        receiverId = extras.getString("Uid");
        receiverName = extras.getString("Name");
        receiverTelephone = extras.getString("Telephone");


        initToolbar();
        initRecyclerView();
        checkIsChatEmpty();
    }


    @Override
    protected void onStart() {
        super.onStart();

        setFirebaseMessagesListener();
        setFirebaseStatusListener();
    }


    @Override
    protected void onStop() {
        super.onStop();

        chatList.clear();

        if (databaseMessages != null && messageListener != null)
            databaseMessages.removeEventListener(messageListener);
        if (databaseProfiles != null && statusListener != null)
            databaseProfiles.removeEventListener(statusListener);
    }


    private void initToolbar() {

        Toolbar toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView titleTV = (TextView) findViewById(R.id.chat_titleTV);
        subtitleTV = (TextView) findViewById(R.id.chat_subtitleTV);
        titleTV.setText(receiverName);
        subtitleTV.setText(receiverStatus);
    }


    private void initRecyclerView() {

        recyclerView = (RecyclerView) findViewById(R.id.chatRV);
/*        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(this, chatList = new ArrayList<>());
        recyclerView.setAdapter(chatAdapter);*/

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(false);
        //recyclerView.setItemAnimator(new ScaleUpAnimator());

        fastAdapter = new FastItemAdapter<>();
        fastAdapter.setHasStableIds(false);
        fastAdapter.withSelectable(true);
        fastAdapter.withMultiSelect(true);
        fastAdapter.withSelectOnLongClick(true);
        recyclerView.setAdapter(fastAdapter);
        chatList = new ArrayList<>();
        fastAdapter.add(chatList);
    }


    private void checkIsChatEmpty() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseMessages = database.getReference().child("Messages").child(firebaseUser.getUid()).child(receiverId);

        databaseMessages.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()) {

                    chatLoadingPB.setVisibility(View.GONE);
                    chatLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    private void setFirebaseMessagesListener() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseMessages = database.getReference().child("Messages").child(firebaseUser.getUid()).child(receiverId);
        messageListener = new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {

                if (dataSnapshot.getValue() != null) {

                    Chat chat = new Chat();
                    chat.setId(dataSnapshot.child("id").getValue(String.class));
                    chat.setUserId(dataSnapshot.child("uid").getValue(String.class));
                    chat.setSenderTelephone(dataSnapshot.child("sender_tel_num").getValue(String.class));
                    chat.setReceiverId(dataSnapshot.child("receiver_id").getValue(String.class));
                    chat.setReceiverTelephone(dataSnapshot.child("receiver_tel_num").getValue(String.class));
                    chat.setMessage(dataSnapshot.child("message").getValue(String.class));
                    chat.setDateTime(dataSnapshot.child("date_time").getValue(Long.class));
                    chat.setTypes(dataSnapshot.child("type").getValue(String.class));

                    chatList.add(chat);
                    sortChatList(chatList);
                    //chatAdapter.notifyDataSetChanged();

                    //fastAdapter.clear();
                    //fastAdapter.add(chatList);
                    fastAdapter.setNewList(chatList);

                    //fastAdapter.withSavedInstanceState(savedInstanceState);



                    recyclerView.scrollToPosition(chatList.size() - 1);

                    chatLoadingPB.setVisibility(View.GONE);
                    chatLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };

        databaseMessages.addChildEventListener(messageListener);
    }


    private void setFirebaseStatusListener() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseProfiles = database.getReference().child("Profiles").child(receiverTelephone);
        statusListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                receiverStatus = dataSnapshot.child("status").getValue(String.class);
                String lastSeen = dataSnapshot.child("last_seen").getValue(String.class);
                receiverLastSeen = ModeClass.convertUnixToDate1(ChatActivity.this, Long.parseLong(lastSeen));

                if (receiverStatus != null && receiverStatus.equals("Online")) {

                    long currentTime = new Date().getTime();
                    long minuteBefore = (currentTime - 90 * 1000);

                    if (minuteBefore > Long.parseLong(lastSeen))
                        subtitleTV.setText(new StringBuilder(getString(R.string.last_seen)).append(" ").append(receiverLastSeen));
                    else
                        subtitleTV.setText(receiverStatus);
                }

                if (receiverStatus != null && receiverStatus.equals("Offline"))
                    subtitleTV.setText(receiverStatus);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };

        databaseProfiles.addValueEventListener(statusListener);
    }


    private void sendMessage() {

        String messageText = textInputET.getText().toString().trim();
        textInputET.setText("");

        if (TextUtils.isEmpty(messageText))
            Toast.makeText(this, "Wiadomość nie może być pusta", Toast.LENGTH_SHORT).show();
        else {

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseId = database.getReference("Messages");
            String id = databaseId.child(firebaseUser.getUid()).child(receiverId).push().getKey();

            Map<String, Object> messageBody = new HashMap<String, Object>();
            messageBody.put("id", id);
            messageBody.put("uid", firebaseUser.getUid());
            messageBody.put("sender_tel_num", Objects.requireNonNull(firebaseUser.getPhoneNumber()));
            messageBody.put("receiver_id", receiverId);
            messageBody.put("receiver_tel_num", receiverTelephone);
            messageBody.put("message", messageText);
            messageBody.put("date_time", new Date().getTime());
            messageBody.put("type", "text");

            String messageSenderRef = firebaseUser.getUid() + "/" + receiverId + "/" + id;
            String messageReceiverRef = receiverId + "/" + firebaseUser.getUid() + "/" + id;

            Map<String, Object> messageBodyDetails = new HashMap<String, Object>();
            messageBodyDetails.put(messageSenderRef, messageBody);
            messageBodyDetails.put(messageReceiverRef, messageBody);

            DatabaseReference databaseMessages = database.getReference("Messages");
            databaseMessages.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener<Void>() {

                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful())
                        Toast.makeText(ChatActivity.this, "Wiadomość wysłana pomyślnie", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    public void onClickSend(View view) {
        sendMessage();
    }


    public void onClickEditText(View view) {
        recyclerView.smoothScrollToPosition(fastAdapter.getItemCount() - 1);
    }
}