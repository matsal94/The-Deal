package co.sepin.thedeal;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import co.sepin.thedeal.adapter.MessageAdapter;
import co.sepin.thedeal.application.DrawerClass;

import static co.sepin.thedeal.application.UpdateData.businessCardHolderList;
import static co.sepin.thedeal.application.UpdateData.getContactsFromDB;


public class MessageActivity extends DrawerClass {

    private MessageAdapter messageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initRecyclerView();
    }


    @Override
    public int getContentView() {
        return R.layout.activity_message;
    }


    @Override
    protected void onRestart() {
        super.onRestart();

        getContactsFromDB();
        messageAdapter.reload(businessCardHolderList);
    }


    private void initRecyclerView() {

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.messageRV);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(this, businessCardHolderList);
        recyclerView.setAdapter(messageAdapter);
    }
}