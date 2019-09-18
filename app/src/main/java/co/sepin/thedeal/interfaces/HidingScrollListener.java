package co.sepin.thedeal.interfaces;

import androidx.recyclerview.widget.RecyclerView;

public abstract class HidingScrollListener extends RecyclerView.OnScrollListener {

    private static final int HIDE_THRESHOLD = 200; // zmienione z 20 na 200
    private int scrolledDistance = 0;
    public static boolean controlsVisible = true;


    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {

            onHide();
            controlsVisible = false;
            scrolledDistance = 0;
        } else if (scrolledDistance < -HIDE_THRESHOLD/2 && !controlsVisible) { // dodane /2

            onShow();
            controlsVisible = true;
            scrolledDistance = 0;
        }

        if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0))
            scrolledDistance += dy;
    }


    public abstract void onHide();

    public abstract void onShow();
}