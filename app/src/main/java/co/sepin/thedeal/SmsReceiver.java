package co.sepin.thedeal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {
    //interface
    private static SmsListener mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getExtras();

        Object[] pdus = (Object[]) data.get("pdus");

        for (int i = 0; i < pdus.length; i++) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);

            String messageBody = smsMessage.getMessageBody();
            if (messageBody.startsWith("Twoje haslo jednorazowe to: ")) {

                System.out.println(messageBody.length());
                System.out.println(messageBody.substring(28, messageBody.length()));
                String message = messageBody.substring(28, messageBody.length());
                mListener.messageReceived(message);
            }
            else return;
        }

    }

    public static void bindListener(SmsListener listener) {
        mListener = listener;
    }
}
/*
    private static final String TAG = "SmsReceiver";

    private final String serviceProviderNumber;
    private final String serviceProviderSmsCondition;

    private Listener listener;

    public SmsReceiver(String serviceProviderNumber, String serviceProviderSmsCondition) {
        this.serviceProviderNumber = serviceProviderNumber;
        this.serviceProviderSmsCondition = serviceProviderSmsCondition;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            Toast.makeText(context, "Otrzymano SMS", Toast.LENGTH_LONG).show();
            String smsSender = "";
            String smsBody = "";
            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                smsSender = smsMessage.getDisplayOriginatingAddress();
                smsBody += smsMessage.getMessageBody();
            }

            if (smsBody.startsWith(serviceProviderSmsCondition)) {
                if (listener != null) {
                    listener.onTextReceived(smsBody);
                }
            }
        }
    }

    void setListener(Listener listener) {
        this.listener = listener;
    }

    interface Listener {
        void onTextReceived(String text);
    }*/
