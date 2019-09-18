package co.sepin.thedeal.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.List;

import co.sepin.thedeal.R;
import co.sepin.thedeal.application.ModeClass;
import co.sepin.thedeal.database.Contact;
import co.sepin.thedeal.database.Contact_Table;
import co.sepin.thedeal.database.Deal;
import co.sepin.thedeal.database.DealType;
import co.sepin.thedeal.database.DealType_Table;
import co.sepin.thedeal.database.User;
import co.sepin.thedeal.database.User_Table;
import de.hdodenhof.circleimageview.CircleImageView;

import static co.sepin.thedeal.application.ModeClass.checkInternetConnection;
import static co.sepin.thedeal.application.UpdateData.businessCardHolderList;
import static co.sepin.thedeal.application.UpdateData.firebaseUser;


public class DealAdapter extends RecyclerView.Adapter<DealAdapter.ViewHolder> {

    private List<Deal> dealsList;
    private LayoutInflater layoutInflater;
    private ItemClickListener itemClickListener;
    private Context context;


    public DealAdapter(Context context, List<Deal> dealsList) {

        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.dealsList = dealsList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = layoutInflater.inflate(R.layout.item_deal_list, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        YoYo.with(Techniques.FadeInLeft).duration(600).playOn(holder.cardView); //animacja cardView podczas przewijania

        setAvatars(holder, position);
        setNames(holder, position);
        holder.timeTV.setText(ModeClass.convertUnixToDate1(context, dealsList.get(position).getCreateDate()));
        holder.categoryTV.setText(swapCategoryId((dealsList.get(position).getCategoryId())));
        holder.titleTV.setText(dealsList.get(position).getTitle());
        holder.descTV.setText(dealsList.get(position).getDescription());
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
        return dealsList.size();
    }


    public Object getItem(int position) {
        return dealsList.get(position);
    }


    private void setAvatars(ViewHolder holder, final int position) {

        String telNumber = dealsList.get(position).getPhone();
        String avatarUri = "";

        if (telNumber.equals(firebaseUser.getPhoneNumber())) {

            try {
                avatarUri = SQLite.select(User_Table.avatarUri).from(User.class).where(User_Table.phone.eq(telNumber)).querySingle().getAvatarUri();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

            try {
                avatarUri = SQLite.select(Contact_Table.avatarUri).from(Contact.class).where(Contact_Table.telephone.eq(telNumber)).querySingle().getAvatarUri();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        RequestOptions requestOptions = new RequestOptions()
                .error(R.drawable.user_profil)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);

        Glide.with(context)
                .load(avatarUri)
                .apply(requestOptions)
                .into(holder.avatarCIV);
    }


    private void setNames(ViewHolder holder, int position) {

        if (dealsList.get(position).getUserId().equals(firebaseUser.getUid())) {

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String name = preferences.getString("user_name","");

            if (name.equals(""))
                holder.nameTV.setText(new StringBuilder(context.getResources().getString(R.string.me)));
            else
                holder.nameTV.setText(new StringBuilder(context.getResources().getString(R.string.me)).append(" (").append(name).append(")"));

            return;
        }

        for (int i = 0; i < businessCardHolderList.size(); i++)
            if (dealsList.get(position).getPhone().equals(businessCardHolderList.get(i).getTelephone())) {

                holder.nameTV.setText(businessCardHolderList.get(i).getName());
                break;
            }
    }


    public void reload(List<Deal> listD) {

        dealsList = new ArrayList<>();
        dealsList.addAll(listD);
        //this.dealsList = listD;
        notifyDataSetChanged();
    }


    private String swapCategoryId(long categoryId) {
        return SQLite.select(DealType_Table.description).from(DealType.class).where(DealType_Table.id.is(categoryId)).querySingle().getDescription();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CardView cardView;
        CircleImageView avatarCIV;
        TextView timeTV, categoryTV, nameTV, titleTV, descTV;

        ViewHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.item_deal_listCV);
            avatarCIV = itemView.findViewById(R.id.item_deal_list_avatarCIV);
            nameTV = itemView.findViewById(R.id.item_deal_list_nameTV);
            timeTV = itemView.findViewById(R.id.item_deal_list_timeTV);
            categoryTV = itemView.findViewById(R.id.item_deal_list_categoryTV);
            titleTV = itemView.findViewById(R.id.item_deal_list_titleTV);
            descTV = itemView.findViewById(R.id.item_deal_list_descriptionTV);

            itemView.setOnClickListener(this);
            itemView.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {

                        Animation animIn = new ScaleAnimation(
                                1f, 0.95f,
                                1f, 0.95f,
                                Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f);

                        animIn.setFillAfter(true);
                        animIn.setDuration(context.getResources().getInteger(R.integer.animation_dur_medium));
                        cardView.startAnimation(animIn);
                    } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {

                        Animation animOut = new ScaleAnimation(
                                0.95f, 1f,
                                0.95f, 1f,
                                Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f);

                        animOut.setFillAfter(true);
                        animOut.setDuration(context.getResources().getInteger(R.integer.animation_dur_medium));
                        cardView.startAnimation(animOut);
                    }
                    return false;
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    if (dealsList.get(getAdapterPosition()).getUserId().equals(firebaseUser.getUid())) {

                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext()); // getContext()
                        View dialogView = layoutInflater.inflate(R.layout.deal_dialog, null);
                        dialogBuilder.setView(dialogView);

                        Button delete = (Button) dialogView.findViewById(R.id.deal_dialog_deleteBtn);
                        TextView title = (TextView) dialogView.findViewById(R.id.deal_dialog_titleTV);
                        title.setText(dealsList.get(getAdapterPosition()).getTitle());

                        final AlertDialog alertDialog = dialogBuilder.create();
                        alertDialog.show();

                        delete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                delete(getAdapterPosition());
                                alertDialog.dismiss();
                            }
                        });
                    }
                    return false;
                }
            });
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null)
                itemClickListener.onItemClick(view, getAdapterPosition());
        }
    }


    private void delete(final int position) {

        if (checkInternetConnection() && dealsList != null && dealsList.size() != 0) {

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseDeals = database.getReference("Deals");

            databaseDeals.child(dealsList.get(position).getId()).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {

                        @Override
                        public void onSuccess(Void aVoid) {

                            dealsList.get(position).delete(); // usuwanie z bazy
                            dealsList.remove(position); // usuwanie z listy
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, dealsList.size());

                            Toast.makeText(context, context.getResources().getString(R.string.deleted_deal), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {

                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Nie można usunąć deal'a", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else if (!checkInternetConnection())
            Toast.makeText(context, context.getResources().getString(R.string.no_Internet_connection), Toast.LENGTH_SHORT).show();
    }


    void setClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }


    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}