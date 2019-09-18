package co.sepin.thedeal.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import co.sepin.thedeal.interfaces.SmsListener;

public class SmsReceiver extends BroadcastReceiver {

    private static SmsListener smsListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getExtras();

        Object[] pdus = (Object[]) data.get("pdus");

        for (int i = 0; i < pdus.length; i++) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);

            String messageBody = smsMessage.getMessageBody();
           // if (messageBody.startsWith("Twoje haslo jednorazowe to: ")) {
            if (messageBody.startsWith("Twój kod weryfikacyjny to: ")) {

                System.out.println("SmsReciver messageBody.length(): " + messageBody.length());
                System.out.println("SmsReciver message: " + messageBody.substring(27));
                String message = messageBody.substring(27);
                smsListener.messageReceived(message);
            }
            else return;
        }

    }

    public static void bindListener(SmsListener listener) {
        smsListener = listener;
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
