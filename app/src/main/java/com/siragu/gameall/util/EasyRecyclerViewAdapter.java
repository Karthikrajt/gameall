package com.siragu.gameall.util;

import android.content.Context;
import android.graphics.PorterDuff;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.siragu.gameall.R;

import java.util.ArrayList;
import java.util.List;

/**
 * An extension of {@link androidx.recyclerview.widget.RecyclerView.Adapter} with additional functions that makes addition and removal of items easy
 *
 * @param <T>
 */
public abstract class EasyRecyclerViewAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = EasyRecyclerViewAdapter.class.getSimpleName();
    public List<T> itemsList;
    private static final int LOADING_VIEW = 1;
    private boolean isLoaderShowing = false;

    public EasyRecyclerViewAdapter() {
        this.itemsList = new ArrayList<>();
        //showLoading();
    }

    public void addItemOnTop(T item) {
        ArrayList<T> items = new ArrayList<>();
        items.add(item);
        addItemsOnTop(items);
    }

    public void addItemAtPos(T item, int pos) {
        this.itemsList.add(pos, item);
        notifyItemInserted(pos);
        hideLoading();
    }

    public void addItemsOnTop(List<T> items) {
        addItemsOnTop(items, null);
    }

    public void addItemsOnTop(List<T> items, @Nullable EmptyViewListener emptyViewListener) {
        if (checkListEmpty(items, emptyViewListener)) return;
        this.itemsList.addAll(0, items);
        notifyItemRangeInserted(0, items.size());
        hideLoading();
    }

    public void addItemAtBottom(T item) {
        addItemAtBottom(item, null);
    }

    public void addItemAtBottom(T item, @Nullable EmptyViewListener emptyViewListener) {
        ArrayList<T> items = new ArrayList<>();
        items.add(item);
        addItemsAtBottom(items, emptyViewListener);
    }

    public void addItemsAtBottom(@Nullable List<T> items) {
        addItemsAtBottom(items, null);
    }

    public void addItemsAtBottom(@Nullable List<T> items, @Nullable EmptyViewListener emptyViewListener) {
        if (checkListEmpty(items, emptyViewListener)) return;
        int size = itemsList.size();
        //Handles a bug where nothing appears on first position in staggered grid layout manager
        if (size == 0) notifyDataSetChanged();
        //items will never be null at this point
        this.itemsList.addAll(items);
        notifyItemRangeInserted(size, items.size());
        hideLoading();
    }

    private boolean checkListEmpty(@Nullable List<T> items, @Nullable EmptyViewListener emptyViewListener) {
        if (items == null || items.size() == 0) {
            if (emptyViewListener != null) emptyViewListener.showEmptyView();
            hideLoading();
            return true;
        } else {
            if (emptyViewListener != null) emptyViewListener.hideEmptyView();
            return false;
        }
    }

    public void removeItemAtBottom() {
        int size = itemsList.size();
        if (itemsList.get(size - 1) == null)
            this.itemsList.remove(size - 1);
        notifyItemRemoved(size - 1);
    }

    public void removeItemsRange(int start, int end) {
        if (start < 0 || end >= getItemCount() || start > end) return;

        for (int i = end; i >= start; i--) {
            itemsList.remove(i);
        }
        notifyItemRangeRemoved(start, end - start + 1);
    }

    public void removeItemAt(int pos) {
        if (pos < 0 || pos > getItemsListSize()) return;
        itemsList.remove(pos);
        notifyItemRemoved(pos);
    }

    public void clear() {
        int size = itemsList.size();
        itemsList.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void findAndRemoveItem(T item) {
        for (int i = 0; i < getItemsListSize(); i++) {
            if (getItem(i).equals(item)) {
                removeItemAt(i);
                return;
            }
        }
    }

    public void updateItem(T item) {
        int position = -1;
        for (int i = 0; i < getItemsListSize(); i++) {
            if (getItem(i).equals(item)) {
                position = i;
                break;
            }
        }
        if (position != -1)
            updateItem(position, item, false);
    }

    public void updateItem(int position, T item) {
        updateItem(position, item, false);
    }

    public void updateItem(int position, T item, boolean hasPayload) {
        if (position >= 0 && position <= getItemsListSize()) {
            itemsList.set(position, item);
            if (hasPayload) notifyItemChanged(position, true);
            else notifyItemChanged(position);
        }
    }

    public void hideLoading() {
        if (isLoaderShowing) {
            notifyItemRemoved(getItemCount() - 1);
            isLoaderShowing = false;
        }
    }

    public void showLoading() {
        if (!isLoaderShowing) {
            isLoaderShowing = true;
            notifyItemInserted(getItemCount() - 1);
        }
    }

    public boolean isLoaderShowing() {
        return isLoaderShowing;
    }

    public T getItem(int position) {
        return itemsList.get(position);
    }

    public int getItemsListSize() {
        if (itemsList == null) return -1;
        return itemsList.size();
    }

    public abstract RecyclerView.ViewHolder onCreateItemView(ViewGroup parent, int viewType);

    public abstract void onBindItemView(RecyclerView.ViewHolder holder, T item, int position);

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        if (viewType == LOADING_VIEW) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.footer_view, parent, false);
            return new LoadingViewHolder(itemView);
        } else {
            return onCreateItemView(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (!(holder instanceof EasyRecyclerViewAdapter.LoadingViewHolder)) {
            if (!itemsList.isEmpty()) {
                onBindItemView(holder, getItem(position), position);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoaderShowing && position == getItemCount() - 1) {
            return LOADING_VIEW;
        } else {
            return super.getItemViewType(position);
        }
    }

    @Override
    public int getItemCount() {
        int count = itemsList.size();
        if (isLoaderShowing) {
            count++;
        }
        return count;
    }

    public List<T> getItemsList() {
        return itemsList;
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {

        private ProgressBar progressBar;

        LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.loading_progress_bar);
            progressBar.getIndeterminateDrawable().setColorFilter(
                    ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);

            if (getAdapterPosition() % 2 == 0 && itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) itemView.getLayoutParams();
                layoutParams.setFullSpan(true);
            }
        }
    }

    public interface EmptyViewListener {
        void showEmptyView();

        void hideEmptyView();
    }
}