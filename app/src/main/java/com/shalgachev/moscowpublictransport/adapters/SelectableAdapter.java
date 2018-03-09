package com.shalgachev.moscowpublictransport.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anton on 2/11/2018.
 */

public abstract class SelectableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private boolean mIsSelectingActive = false;
    private SparseBooleanArray mSelectedItems;

    public SelectableAdapter() {
        mSelectedItems = new SparseBooleanArray();
    }

    protected Object getSelectionEnabledPayload(boolean enabled)
    {
        return null;
    }
    protected Object getItemSelectedPayload(boolean selected)
    {
        return null;
    }

    public void enableSelecting(boolean enable) {
        mIsSelectingActive = enable;

        if (!enable)
            clearSelection();

        Object payload = getSelectionEnabledPayload(enable);
        notifyItemRangeChanged(0, getItemCount(), payload);
    }

    public boolean isSelectingEnabled() {
        return mIsSelectingActive;
    }

    /**
     * Indicates if the item at position position is selected
     * @param position Position of the item to check
     * @return true if the item is selected, false otherwise
     */
    public boolean isSelected(int position) {
        return getSelectedItems().contains(position);
    }

    public void selectAll() {
        int size = getItemCount();
        for (int i = 0; i < size; i++) {
            mSelectedItems.put(i, true);
        }

        notifyItemRangeChanged(0, size, getItemSelectedPayload(true));
    }

    /**
     * Toggle the selection status of the item at a given position
     * @param position Position of the item to toggle the selection status for
     */
    public void toggleSelection(int position) {
        boolean selecting;
        if (mSelectedItems.get(position, false)) {
            mSelectedItems.delete(position);
            selecting = false;
        } else {
            mSelectedItems.put(position, true);
            selecting = true;
        }
        notifyItemChanged(position, getItemSelectedPayload(selecting));
    }

    /**
     * Clear the selection status for all items
     */
    public void clearSelection() {
        List<Integer> selection = getSelectedItems();
        mSelectedItems.clear();
        for (Integer i : selection) {
            notifyItemChanged(i, getItemSelectedPayload(false));
        }
    }

    /**
     * Count the selected items
     * @return Selected items count
     */
    public int getSelectedItemCount() {
        return mSelectedItems.size();
    }

    /**
     * Indicates the list of selected items
     * @return List of selected items ids
     */
    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(mSelectedItems.size());
        for (int i = 0; i < mSelectedItems.size(); ++i) {
            items.add(mSelectedItems.keyAt(i));
        }
        return items;
    }
}
