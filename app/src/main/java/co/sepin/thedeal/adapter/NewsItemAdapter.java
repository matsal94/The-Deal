package co.sepin.thedeal.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.master.glideimageview.GlideImageView;

import co.sepin.thedeal.R;
import co.sepin.thedeal.model.NewsResult;


public class NewsItemAdapter extends RecyclerView.Adapter<NewsItemAdapter.ViewHolder> {

    private Context context;
    private NewsResult newsResult;
    private RecyclerView recyclerView;
    private TextView descriptionTV, publishedAtTV;
    private ImageButton upIB, downIB;
    private View view;
    private int expandedPosition = -1;
    private int prevExpandedPosition = -1;


    class ViewHolder extends RecyclerView.ViewHolder {

        ProgressBar loadingPB;
        GlideImageView articleImageGIV;
        TextView titleTV, descriptionTV, publishedAtTV;
        ImageView downIB, upIB;
        ConstraintLayout itemCL;
        CardView cardView;


        ViewHolder(@NonNull View itemView) {
            super(itemView);

            loadingPB = (ProgressBar) itemView.findViewById(R.id.item_news_list_loadingPB);
            articleImageGIV = (GlideImageView) itemView.findViewById(R.id.item_news_list_articleImageGIV);
            titleTV = (TextView) itemView.findViewById(R.id.item_news_list_titleTV);
            descriptionTV = (TextView) itemView.findViewById(R.id.item_news_list_descriptionTV);
            publishedAtTV = (TextView) itemView.findViewById(R.id.item_news_list_publishedAtTV);
            downIB = (ImageButton) itemView.findViewById(R.id.item_news_list_downIB);
            upIB = (ImageButton) itemView.findViewById(R.id.item_news_list_upIB);
            itemCL = (ConstraintLayout) itemView.findViewById(R.id.item_news_listCL);
            cardView = (CardView) itemView.findViewById(R.id.item_news_listCV);
        }
    }


    public NewsItemAdapter() {
        super();
    }


    public NewsItemAdapter(Context context, NewsResult newsResult, RecyclerView recyclerView) {

        this.context = context;
        this.newsResult = newsResult;
        this.recyclerView = recyclerView;
    }


    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }


    @Override
    public int getItemCount() {
        return newsResult.getArticles().size();
    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }


    @NonNull
    @Override
    public NewsItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_news_list, parent, false);
        return new ViewHolder(itemView);
    }


    @Override
    public void onViewDetachedFromWindow(@NonNull NewsItemAdapter.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        setCollapseOtherItems(holder);
    }


    @Override
    public void onViewAttachedToWindow(@NonNull NewsItemAdapter.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        setCollapseOtherItems(holder);
    }


    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(@NonNull final NewsItemAdapter.ViewHolder holder, final int position) {

        YoYo.with(Techniques.FadeIn).duration(600).playOn(holder.cardView); //animacja cardView podczas przewijania
        holder.articleImageGIV.layout(0, 0, 0, 0); //zablokowanie zmniejszania sie zdjec podczas scrollowania

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);


        Glide.with(context)
                .load(newsResult.getArticles().get(position).getUrlToImage())
                .apply(requestOptions)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                        holder.loadingPB.setVisibility(View.GONE);
                        noCollapseOpenCard(holder, position);

                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                        holder.loadingPB.setVisibility(View.GONE);
                        noCollapseOpenCard(holder, position);

                        return false;
                    }
                })
                .error(Glide.with(context).load(R.drawable.ic_broken_image_grey))
                .into(holder.articleImageGIV)
                .clearOnDetach()
                .waitForLayout();

        //Picasso.get().load(newsResult.getArticles().get(position).getUrlToImage()).into(holder.imageGIV);
        //Glide.with(context).load(newsResult.getArticles().get(position).getUrlToImage()).into(holder.imageGIV);
        //final boolean isExpanded = position == mExpandedPosition;

        holder.titleTV.setText(newsResult.getArticles().get(position).getTitle());
        holder.descriptionTV.setText(newsResult.getArticles().get(position).getDescription());
        holder.publishedAtTV.setText(new StringBuilder(newsResult.getArticles().get(position).getPublishedAt().substring(0, 10))
                .append(" ").append(newsResult.getArticles().get(position).getPublishedAt().substring(11, 16)));


        holder.itemCL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String url = newsResult.getArticles().get(position).getUrl();

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
            }
        });

        holder.downIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ChangeBounds transition = new ChangeBounds();
                transition.setDuration(context.getResources().getInteger(R.integer.animation_dur_low));
                TransitionManager.beginDelayedTransition(recyclerView, transition);

                holder.descriptionTV.setVisibility(View.VISIBLE);
                holder.publishedAtTV.setVisibility(View.VISIBLE);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {

                        holder.upIB.setVisibility(View.VISIBLE);
                        holder.downIB.setVisibility(View.GONE);
                    }
                }, 300);

                expandedPosition = position;

                if (prevExpandedPosition != -1 && prevExpandedPosition != position) {

                    if (recyclerView.findViewHolderForAdapterPosition(prevExpandedPosition) != null) {

                        view = recyclerView.findViewHolderForAdapterPosition(prevExpandedPosition).itemView;

                        descriptionTV = (TextView) view.findViewById(R.id.item_news_list_descriptionTV);
                        publishedAtTV = (TextView) view.findViewById(R.id.item_news_list_publishedAtTV);
                        upIB = (ImageButton) view.findViewById(R.id.item_news_list_upIB);
                        downIB = (ImageButton) view.findViewById(R.id.item_news_list_downIB);

                        descriptionTV.setVisibility(View.GONE);
                        publishedAtTV.setVisibility(View.GONE);
                        upIB.setVisibility(View.GONE);
                        downIB.setVisibility(View.VISIBLE);
                    }
                }
                prevExpandedPosition = position;
            }
        });

        holder.upIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ChangeBounds transition = new ChangeBounds();
                transition.setDuration(context.getResources().getInteger(R.integer.animation_dur_low));
                TransitionManager.beginDelayedTransition(recyclerView, transition);

                holder.descriptionTV.setVisibility(View.GONE);
                holder.publishedAtTV.setVisibility(View.GONE);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {

                        holder.upIB.setVisibility(View.GONE);
                        holder.downIB.setVisibility(View.VISIBLE);
                    }
                }, 300);

                expandedPosition = -1;
            }
        });
    }


    private void noCollapseOpenCard(@NonNull NewsItemAdapter.ViewHolder holder, int position) {

        if (expandedPosition == position) {

            holder.descriptionTV.setVisibility(View.VISIBLE);
            holder.publishedAtTV.setVisibility(View.VISIBLE);
            holder.upIB.setVisibility(View.VISIBLE);
            holder.downIB.setVisibility(View.GONE);
        } else {

            holder.descriptionTV.setVisibility(View.GONE);
            holder.publishedAtTV.setVisibility(View.GONE);
            holder.upIB.setVisibility(View.GONE);
            holder.downIB.setVisibility(View.VISIBLE);
        }
    }


    private void setCollapseOtherItems(NewsItemAdapter.ViewHolder holder) {

        if (holder.getAdapterPosition() != expandedPosition) {

            holder.descriptionTV.setVisibility(View.GONE);
            holder.publishedAtTV.setVisibility(View.GONE);
            holder.upIB.setVisibility(View.GONE);
            holder.downIB.setVisibility(View.VISIBLE);
        }
    }
}
