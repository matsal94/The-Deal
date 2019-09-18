package co.sepin.thedeal.interfaces;

import java.util.List;

import co.sepin.thedeal.database.Contact;

public interface SelectedContactsInterface {

    void sendNewContactsList(List<Contact> newContactsList);
}
