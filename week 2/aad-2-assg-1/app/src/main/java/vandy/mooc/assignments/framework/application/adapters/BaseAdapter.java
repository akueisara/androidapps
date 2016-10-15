package vandy.mooc.assignments.framework.application.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import vandy.mooc.assignments.framework.application.fragments.PagedFragment;

/**
 * This abstract adapter class manages the item selection state for any List of
 * objects. It uses an ArrayList wrapper that maintains an additional selections
 * state boolean for each item in the adapter's data list. Note that since this
 * implementation uses its own ArrayList wrapper, any changes the application
 * makes on the originally passed list will not be reflected in the adapter
 * list. This means that the adapter owns the data and any changes such as
 * adding, deleting, or sorting, must be done by the adapter itself through
 * provided methods.
 * <p>
 * Perhaps a better and more flexible implementation would be to use a separate
 * item selection list so that fragment or activity remains the owner of the
 * backing data list. This adapter would then not be required to provide list
 * editing support. This is however a little more complex to manage in terms of
 * keeping the selection list and the data list in sync, so the simpler approach
 * has been chosen for this framework.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class BaseAdapter<TYPE, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {
    /**
     * The activity context.
     */
    protected final Context mContext;

    /**
     * This class supports the OnSelectionListener interface for a single
     * listen. This interface has hooks for both click and long-click item view
     * events.
     */
    protected final OnSelectionListener mOnSelectionListener;
    /**
     * Keeps track of refreshing item count.
     */
    protected int mRefreshCount;
    /**
     * ArrayList containing an Item wrapper that contains each adapter item
     * along with a its selection state.
     */
    private ArrayList<Item> mItems;

    /**
     * Constructor.
     *
     * @param context  The activity context.
     * @param listener A optional OnSelectionListener.
     */
    public BaseAdapter(
            Context context,
            @Nullable OnSelectionListener listener) {
        mContext = context;
        mOnSelectionListener = listener;

        // Make sure we never have to deal with a null list.
        mItems = new ArrayList<>();
    }

    /**
     * Sets the adapter data to the contents of the passed list. It does not
     * keep a reference to the past list and stores the list contents in an
     * ArrayList<TYPE>.
     *
     * @param itemTypes Data list to set.
     */
    public void setData(List<TYPE> itemTypes) {
        // We never want a null list.
        mItems = new ArrayList<>();

        if (itemTypes != null) {
            for (TYPE itemType : itemTypes) {
                mItems.add(new Item(itemType));
            }
        }

        notifyDataSetChanged();
    }

    /**
     * Convenience method for setting a TYPE[] array. It does not keep a
     * reference to the past array and stores * the list contents in an
     * ArrayList<TYPE>.
     *
     * @param array Data array to set.
     */
    public void setData(TYPE[] array) {
        List<TYPE> list = new ArrayList<>(array.length);
        Collections.addAll(list, array);
        setData(list);
    }

    /**
     * Convenience method for setting a HashSet<TYPE>. It does not keep a
     * reference to the past array and stores * the list contents in an
     * ArrayList<TYPE>.
     *
     * @param hashSet set to convert and store.
     */
    public void setData(HashSet<TYPE> hashSet) {
        List<TYPE> list = new ArrayList<>(hashSet.size());
        list.addAll(hashSet);
        setData(list);
    }

    /**
     * Returns the specified adapter item.
     *
     * @param position The item position.
     * @return The item at the specified position.
     */
    public TYPE getItem(int position) {
        return mItems.get(position).getData();
    }

    /**
     * Adds an item to the adapter list.
     *
     * @param item The item to add.
     */
    public void addItem(TYPE item) {
        mItems.add(new Item(item));
        notifyItemInserted(mItems.size() - 1);
    }

    /**
     * Adds a list of items to the end of the adapter list.
     *
     * @param list List of items to add.
     */
    public void addAll(List<TYPE> list) {
        int start = mItems.size();
        int end = start;
        if (list != null) {
            for (TYPE itemType : list) {
                mItems.add(new Item(itemType));
                end++;
            }
        }

        notifyItemRangeInserted(start, end);
    }

    /**
     * Adds an array of items to the end of the adapter list.
     *
     * @param array of items to add.
     */
    public void addAll(TYPE[] array) {
        List<TYPE> list = new ArrayList<>(array.length);
        Collections.addAll(list, array);
        addAll(list);
    }

    /**
     * Removes the items at the specified list of positions from the adapter.
     *
     * @param list The adapter positions of the items to remove.
     */
    public void removeItems(List<Integer> list) {
        selectItem(-1, false);

        // Delete backwards to keep iterator valid.
        for (int item = list.size() - 1; 0 <= item; item--) {
            removeItem(list.get(item));
        }

        notifyDataSetChanged();
    }

    /**
     * Removes all items.
     */
    public void clear() {
        selectItem(-1, false);
        mItems = new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Returns the number of selected items.
     *
     * @return The number of selected items.
     */
    public int getSelectedCount() {
        int count = 0;
        for (Item item : mItems) {
            if (item.mSelected) {
                count++;
            }
        }

        return count;
    }

    /**
     * Helper method for sub-classes. Redirects request to listener if a
     * listener has been set.
     */
    protected void showRefresh(boolean show) {
        if (mOnSelectionListener != null) {
            mOnSelectionListener.onShowRefresh(show);
        }

    }

    /**
     * Toggles the selection state of the specified item.
     *
     * @param position The adapter position of the item whose state is to be
     *                 toggled.
     */
    public void toggleSelection(int position) {
        mItems.get(position).mSelected = !mItems.get(position).mSelected;
        notifyItemChanged(position);
    }

    /**
     * Change the selection state of the specified item.
     *
     * @param position position of item to select or -1 for all items
     * @param select   true or false
     */
    public void selectItem(int position, boolean select) {
        if (position == -1) {
            // Recursively call this method to deselect each item.
            for (int i = 0; i < mItems.size(); i++) {
                selectItem(i, select);
            }

            return;
        }

        Item item = mItems.get(position);
        if (item.mSelected != select) {
            item.mSelected = select;
            notifyItemChanged(position);
        }
    }

    /**
     * Removes the specified item.
     *
     * @param position The adapter position of the item to be removed.
     */
    public void removeItem(int position) {
        if (mItems.get(position).mSelected) {
            selectItem(position, false);
        }
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Returns all adapter items as and ArrayList.
     *
     * @return An array list of ITEMS.
     */
    public
    @NonNull
    ArrayList<TYPE> getItems() {
        ArrayList<TYPE> list = new ArrayList<>(mItems.size());
        for (Item item : mItems) {
            list.add(item.getData());
        }

        return list;
    }

    /**
     * Returns the list of selected items.
     *
     * @return The list of currently selected items.
     */
    public
    @NonNull
    List<Integer> getSelectedItemsPositions() {
        Vector<Integer> selectedItems = new Vector<>();
        for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i).mSelected) {
                selectedItems.add(i);
            }
        }

        return selectedItems;
    }

    /**
     * Checks if an item is selected.
     *
     * @param position The adapter position of the item to check.
     * @return {@code true} if the item is selected; {@code false} if not.
     */
    public boolean isItemSelected(int position) {
        return mItems.get(position).isSelected();
    }

    /**
     * Returns the number of items in this adapter.
     *
     * @return The number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mItems.size();
    }

    /**
     * Returns extra data that has been set for this item.
     *
     * @param position The item whose extra data is to be returned.
     * @return The extra data set by calling setExtraData.
     */
    public Object getExtraData(int position) {
        if (position < 0 || position >= mItems.size()) {
            throw new IllegalArgumentException("Invalid adapter item position");
        }

        return mItems.get(position).getExtraData();
    }

    /**
     * Sets an extra application defined data object for the specified item.
     *
     * @param position  The adapter position of the item whose extra data is to
     *                  be set.
     * @param extraData The data object to store with the item.
     */
    public void setExtraData(int position, Object extraData) {
        if (position < 0 || position >= mItems.size()) {
            throw new IllegalArgumentException("Invalid adapter item position");
        }

        mItems.get(position).setExtraData(extraData);
    }

    /**
     * Listener interface used to be notified when an item is clicked or long
     * clicked.
     */
    public interface OnSelectionListener {
        /**
         * Hook method called when an item is clicked.
         *
         * @param view     The clicked item's view.
         * @param position The clicked item's adapter position.
         */
        void onItemClick(View view, int position);

        /**
         * Hook method called when an item is clicked.
         *
         * @param view     The clicked item's view.
         * @param position The clicked item's adapter position.
         */
        void onItemClick(
                View view,
                int position,
                Class<? extends PagedFragment> fragment);

        /**
         * Hook method called when an item is long clicked.
         *
         * @param view     The long clicked item's view.
         * @param position The long clicked item's adapter position.
         */
        @SuppressWarnings({"SameReturnValue", "UnusedParameters"})
        boolean onItemLongClick(View view, int position);

        /**
         * Hook method Called when any items are waiting for data.
         *
         * @param show {@code true} to show progress, {@code false} to hide it.
         */
        void onShowRefresh(boolean show);
    }

    /**
     * Simple wrapper class that keeps track of selected items.
     */
    protected class Item {
        /**
         * The original item set by the application.
         */
        private final TYPE mData;

        /**
         * Extra data object set according to application needs.
         */
        private Object mExtraData;

        /**
         * Selection state of the item.
         */
        private boolean mSelected;

        /**
         * Constructor.
         *
         * @param data The original application item to wrap.
         */
        public Item(TYPE data) {
            this.mData = data;
        }

        /**
         * Returns the original application item.
         *
         * @return The original application item.
         */
        protected TYPE getData() {
            return mData;
        }

        /**
         * Returns extra data that has been set for this item.
         *
         * @return The extra data set by calling setExtraData.
         */
        protected Object getExtraData() {
            return mExtraData;
        }

        /**
         * Sets an extra application defined data object for this item.
         *
         * @param extraData The data object to store with this item.
         */
        protected void setExtraData(Object extraData) {
            mExtraData = extraData;
        }

        /**
         * Checks if this item is selected.
         *
         * @return {@code true} if the item is selected; {@code false} if not.
         */
        protected boolean isSelected() {
            return mSelected;
        }
    }
}
