package co.sepin.thedeal.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import co.sepin.thedeal.R;
import co.sepin.thedeal.database.Contact;
import co.sepin.thedeal.interfaces.SelectedContactsInterface;
import de.hdodenhof.circleimageview.CircleImageView;

import static co.sepin.thedeal.application.ModeClass.checkInternetConnection;
import static co.sepin.thedeal.application.ModeClass.formatTelephoneNumber;
import static co.sepin.thedeal.application.UpdateData.businessCardHolderList;
import static co.sepin.thedeal.application.UpdateData.firebaseUser;


public class SelectContactsAdapter extends RecyclerView.Adapter<SelectContactsAdapter.ViewHolder> {

    private SelectedContactsInterface selectedContactsInterface;
    private List<Contact> oldContactsList;//, newContactsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private LayoutInflater layoutInflater;
    private ItemClickListener itemClickListener;
    private ActionMode actionMode = null;
    private Context context;
    private List<Integer> selectedList = new ArrayList<>();
    private boolean[] selectedArray;
    private boolean isOnCreate = true;
    private MenuItem shareBtn;


    public SelectContactsAdapter(Context context, RecyclerView recyclerView, List<Contact> selectContactsList, ItemClickListener itemClickListener, SelectedContactsInterface selectedContactsInterface) {

        this.context = context;
        this.recyclerView = recyclerView;
        this.layoutInflater = LayoutInflater.from(context);
        this.oldContactsList = selectContactsList;
        this.itemClickListener = itemClickListener;
        this.selectedContactsInterface = selectedContactsInterface;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = layoutInflater.inflate(R.layout.item_select_contacts_list, parent, false);
        return new ViewHolder(view, itemClickListener);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.nameTV.setText(oldContactsList.get(position).getName());

        setAvatar(holder, position);
        formatPhoneNumber(holder, position);
        checkIsUser(holder, position);
        initSelectedArray(); // inicjalizacja tablicy wartością false


        if (selectedArray[position])
            holder.userMCB.setChecked(true);
        else
            holder.userMCB.setChecked(false);


        holder.userMCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (actionMode == null)
                    actionMode = ((Activity) context).startActionMode(actionModeCallback);

                if (isChecked) {

                    selectedList.add(position);
                    selectedArray[position] = true;
                } else {

                    selectedArray[position] = false;

                    Iterator itr = selectedList.iterator();
                    while (itr.hasNext())
                        if (itr.next().equals(position))
                            itr.remove();

                    if (selectedList.isEmpty()) {

                        actionMode.finish();
                        return;
                    }
                }

                if (selectedList.size() == 1)
                    shareBtn.setVisible(true);
                else
                    shareBtn.setVisible(false);

                actionMode.setTitle(context.getString(R.string.selected) + selectedList.size());
            }
        });
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
        return oldContactsList.size();
    }


    public Object getItem(int position) {
        return oldContactsList.get(position);
    }


    private void formatPhoneNumber(ViewHolder holder, int position) {

        String phoneNumber = oldContactsList.get(position).getTelephone();
        if (phoneNumber.startsWith("+"))
            holder.telephoneTV.setText(formatTelephoneNumber(context, phoneNumber));
        else
            holder.telephoneTV.setText(oldContactsList.get(position).getTelephone());
    }


    private void setAvatar(ViewHolder holder, int position) {

        //Random random = new Random();
        //int currentColor = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        Drawable color = new ColorDrawable(context.getResources().getColor(R.color.colorAccent));

        holder.avatarCIV.setImageDrawable(color);
        holder.avatarLetterTV.setText(oldContactsList.get(position).getName().substring(0, 1).toUpperCase());
    }


    private void checkIsUser(ViewHolder holder, int position) {

        int isUser = oldContactsList.get(position).getIsUser();
        switch (isUser) {

            case 0: // nie jest
                holder.userMCB.setVisibility(View.GONE);
                holder.inviteCPB.setVisibility(View.VISIBLE);
                break;

            case 1: // jest
                holder.userMCB.setVisibility(View.VISIBLE);
                holder.inviteCPB.setVisibility(View.GONE);
                break;

            case 2: // nie jest, ale jest zaproszony
                holder.userMCB.setVisibility(View.GONE);
                holder.inviteCPB.setVisibility(View.GONE);
                break;
        }
    }


    private void initSelectedArray() {

        if (isOnCreate) { // inicjalizacja tablicy wartością false

            isOnCreate = false;
            selectedArray = new boolean[oldContactsList.size()];

            for (int i = 0; i < selectedArray.length; i++)
                selectedArray[i] = false;
        }
    }


    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {

            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.contextual_action_bar_menu, menu);
            shareBtn = menu.findItem(R.id.action_share);

            mode.setTitle(context.getString(R.string.selected) + selectedList.size());
            return true;
        }


        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            switch (item.getItemId()) {

                case R.id.action_add:

                    if (checkInternetConnection()) {

                        if (checkDoubleContacts())
                            sendContactsList();
                        mode.finish();
                        notifyDataSetChanged();
                        return true;
                    } else
                        Toast.makeText(context, context.getResources().getString(R.string.no_Internet_connection), Toast.LENGTH_SHORT).show();

                    return false;

                case R.id.action_share:

                    setShareIntent();
                    mode.finish();
                    notifyDataSetChanged();
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

            actionMode = null;
            cleanArrayAndList();
            recyclerView.post(new Runnable() { // uruchomienie metody notifyDataSetChanged() w kolejce na końcu po przeliczeniu układu Recyclerview inaczej błąd

                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }
    };


    private boolean checkDoubleContacts() {

        for (int i = 0; i < selectedList.size(); i++) {
            for (int j = 0; j < selectedList.size(); j++) {

                if (i != j && oldContactsList.get(selectedList.get(i)).getTelephone().equals(oldContactsList.get(selectedList.get(j)).getTelephone())) {

                    Toast.makeText(context, "Wybrane kontakty mają ten sam numer telefonu", Toast.LENGTH_SHORT).show();
                    cleanArrayAndList();
                    return false;
                }
            }

            if (oldContactsList.get(i).getTelephone().replace(" ", "").equals(firebaseUser.getPhoneNumber())) {

                Toast.makeText(context, "Nie możesz dodać swojego numeru telefonu", Toast.LENGTH_SHORT).show();
                cleanArrayAndList();
                return false;
            }
        }

        for (int k = 0; k < businessCardHolderList.size(); k++)
            for (int l = 0; l < selectedList.size(); l++) {

                if (businessCardHolderList.get(k).getTelephone().equals(oldContactsList.get(selectedList.get(l)).getTelephone())) {

                    Toast.makeText(context, "Wybrany numer telefonu jest już w Wizytowniku", Toast.LENGTH_SHORT).show();
                    cleanArrayAndList();
                    return false;
                }
            }

        return true;
    }


    private void sendContactsList() {

        getNewContactsList();
        if (!businessCardHolderList.isEmpty())
            selectedContactsInterface.sendNewContactsList(businessCardHolderList);
    }


    private void getNewContactsList() {

        for (int i = 0; i < selectedArray.length; i++)
            if (selectedArray[i])
                businessCardHolderList.add(oldContactsList.get(i));
    }


    public void cleanArrayAndList() {

        if (selectedArray != null)
            for (int i = 0; i < selectedArray.length; i++)
                selectedArray[i] = false;

        if (!selectedList.isEmpty())
            selectedList.clear();
    }


    private void setShareIntent() {

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, oldContactsList.get(selectedList.get(0)).getTelephone());
        shareIntent.setType("text/*");
        context.startActivity(Intent.createChooser(shareIntent, "Wyślij numer telefonu do:"));
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CircleImageView avatarCIV;
        TextView avatarLetterTV, nameTV, telephoneTV;
        CircularProgressButton inviteCPB;
        CheckBox userMCB;
        ItemClickListener itemClickListener;

        ViewHolder(View itemView, ItemClickListener itemClickListener) {
            super(itemView);

            avatarCIV = (CircleImageView) itemView.findViewById(R.id.item_select_contact_list_avatarCIV);
            avatarLetterTV = (TextView) itemView.findViewById(R.id.item_select_contact_list_avatarLetterTV);
            nameTV = (TextView) itemView.findViewById(R.id.item_select_contacts_list_nameTV);
            telephoneTV = (TextView) itemView.findViewById(R.id.item_select_contacts_list_telephoneTV);
            inviteCPB = (CircularProgressButton) itemView.findViewById(R.id.item_select_contacts_listCPB);
            userMCB = (CheckBox) itemView.findViewById(R.id.item_select_contacts_listMCB);

            this.itemClickListener = itemClickListener;
            inviteCPB.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {

            if (itemClickListener != null)
                itemClickListener.onItemClick(view, getAdapterPosition());
        }
    }


    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}