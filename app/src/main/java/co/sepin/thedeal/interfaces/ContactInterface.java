package co.sepin.thedeal.interfaces;


import co.sepin.thedeal.database.Contact;

public interface ContactInterface {

    void makeCall(String telephoneNumber);

    void writeMessage(Contact contact);
}
