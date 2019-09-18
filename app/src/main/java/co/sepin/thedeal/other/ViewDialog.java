package co.sepin.thedeal.other;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.master.glideimageview.GlideImageView;

import co.sepin.thedeal.R;

public class ViewDialog {

    private Activity activity;
    private Dialog dialog;
    private String title;
    private ProgressBar loadingPG;
    private TextView titleTV;


    public ViewDialog(Activity activity, String title) {

        this.activity = activity;
        this.title = title;
    }


    public void showDialog() {

        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);

        dialog.setContentView(R.layout.dialog_loading);
        loadingPG = (ProgressBar) dialog.findViewById(R.id.dialog_loadingPB);
        GlideImageView glideImageView = dialog.findViewById(R.id.dialog_loadingGIV);

        titleTV = dialog.findViewById(R.id.dialog_titleTV);
        titleTV.setText(title);


        if (activity.getTitle().equals(activity.getString(R.string.title_activity_select_contacts))) {

            loadingPG.setVisibility(View.GONE);
            glideImageView.setVisibility(View.VISIBLE);

            Glide.with(activity)
                    .load(R.drawable.loading)
                    .placeholder(R.drawable.loading)
                    .centerCrop()
                    //.crossFade()
                    .into(glideImageView);
        }
        else {

            loadingPG.setVisibility(View.VISIBLE);
            glideImageView.setVisibility(View.GONE);

            loadingPG.setMax(100);
            loadingPG.setIndeterminate(false);
            loadingPG.setProgress(0);
        }

        dialog.show();
    }


    public void uploadProgress(int progress) {

        loadingPG.setProgress(progress);
        titleTV.setText(new StringBuilder(title).append(" ").append(progress).append("%"));
    }


    public void hideDialog() {
        dialog.dismiss();
    }
}