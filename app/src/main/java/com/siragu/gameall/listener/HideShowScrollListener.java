package com.siragu.gameall.listener;

import androidx.recyclerview.widget.RecyclerView;

public abstract class HideShowScrollListener extends RecyclerView.OnScrollListener {
    static final float MINIMUM = 25;

    int scrollDist = 0;
    boolean isVisible = true;

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (isVisible && scrollDist > MINIMUM) {
            hide();
            scrollDist = 0;
            isVisible = false;
        } else if (!isVisible && scrollDist < -MINIMUM) {
            show();
            scrollDist = 0;
            isVisible = true;
        }

        if ((isVisible && dy > 0) || (!isVisible && dy < 0)) {
            scrollDist += dy;
        }
    }

    protected abstract void show();

    protected abstract void hide();
}
