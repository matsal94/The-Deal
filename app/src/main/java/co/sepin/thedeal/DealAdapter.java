package co.sepin.thedeal;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import co.sepin.thedeal.database.Deal;
import co.sepin.thedeal.database.DealType;
import co.sepin.thedeal.database.DealType_Table;
import co.sepin.thedeal.database.Deal_Table;
import co.sepin.thedeal.database.Event;
import co.sepin.thedeal.database.Event_Table;


public class DealAdapter extends RecyclerView.Adapter<DealAdapter.ViewHolder> {

    private List<Deal> dealsList;
    private LayoutInflater layoutInflater;
    private ItemClickListener itemClickListener;
    private Context context;

    // data is passed into the constructor
    DealAdapter(Context context, List<Deal> listaDeali) {

        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.dealsList = listaDeali;
    }


    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = layoutInflater.inflate(R.layout.item_deal_list, parent, false);
        return new ViewHolder(view);
    }


    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.titleTV.setText(dealsList.get(position).getTitle());
        holder.timeTV.setText(getDate(dealsList.get(position).getCreateDate(), "dd/MM/yyyy HH:mm"));
        holder.descTV.setText(dealsList.get(position).getDescription());
        holder.categoryTV.setText(swapCategoryId((dealsList.get(position).getCategoryId())));
    }

    // total number of rows
    @Override
    public int getItemCount() { return dealsList.size();
    }


    // convenience method for getting data at click position
    public Object getItem(int position) {
        return dealsList.get(position);
    }


    public void reload(List<Deal> listD) {

        dealsList = new ArrayList<>();
        dealsList.addAll(listD);
        //this.dealsList = listD;
        notifyDataSetChanged();
    }


    private String swapCategoryId(long categoryId) {

        return SQLite.select(DealType_Table.description).from(DealType.class)
                .where(DealType_Table.id.is(categoryId)).querySingle().getDescription();
    }


    private String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView titleTV, timeTV, descTV, categoryTV;

        ViewHolder(View itemView) {
            super(itemView);

            titleTV = itemView.findViewById(R.id.item_deal_list_titleTV);
            timeTV = itemView.findViewById(R.id.item_deal_list_timeTV);
            descTV = itemView.findViewById(R.id.item_deal_list_descriptionTV);
            categoryTV = itemView.findViewById(R.id.item_deal_list_categoryTV);
            itemView.setOnClickListener(this);


            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    //final LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

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
                    return false;
                }
            });
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) itemClickListener.onItemClick(view, getAdapterPosition());
        }
    }


    private void delete(int position) {

        if(dealsList != null && dealsList.size() != 0) {

            SQLite.delete().from(Deal.class).where(Deal_Table.id.is(dealsList.get(position).getId())).
                    and(Deal_Table.userId.is(dealsList.get(position).getUserId())).async().execute();

            // dodać usuwanie z serwera

            Toast.makeText(context, "Usunięto deal z bazy", Toast.LENGTH_LONG).show();

            dealsList.remove(position);
            notifyItemRemoved(position);
            reload(dealsList);
        }
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}