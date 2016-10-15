package vandy.mooc.assignments.framework.application.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import vandy.mooc.assignments.R;

public class UriAdapter
        extends BaseAdapter<Uri, UriAdapter.UriViewHolder> {

    /**
     * Constructor.
     *
     * @param listener A optional OnSelectionListener.
     * @param context The activity context.
     */
    public UriAdapter(Context context, @Nullable OnSelectionListener listener) {
        super(context, listener);
    }

    /**
     * Hook method called by framework to create a new custom ViewHolder. This
     * is where you perform expensive operations like inflating views.
     *
     * @param parent   View parent.
     * @param viewType Implementation defined type (not used here).
     * @return A new GridViewHolder instance.
     */
    @Override
    public UriViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list, parent, false);

        return new UriViewHolder(view);
    }

    /**
     * Called to bind you view to the adapter data associated with this view.
     *
     * @param holder   The GridViewHolder instance to bind
     * @param position The adapter position of the data for this view.
     */
    @Override
    public void onBindViewHolder(UriViewHolder holder, int position) {
        // Never rely on passed position; always use the real adapter position.
        final int adapterPosition = holder.getAdapterPosition();
        Uri uri = getItem(adapterPosition);

        // Setup up click listen callback.
        initializeListeners(holder.mTextView, adapterPosition);

        // Display the URL text.
        holder.mTextView.setText(uri.toString());

        holder.mTextView.setContentDescription(getItem(position).toString());

        // Draw the selection state.
        drawSelectionState(holder.itemView, adapterPosition);
    }

    /**
     * Installs click on long-click listeners that are then forwarded to the to
     * the OnSelectionListener passed into the adapter constructor.
     *
     * @param view     The clicked view.
     * @param position The clicked view's adapter position.
     */
    private void initializeListeners(View view, final int position) {
        // Redirect all selection handling to
        // registered click listen.
        view.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mOnSelectionListener != null) {
                            mOnSelectionListener.onItemClick(
                                    view, position);
                        }
                    }
                });

        // Redirect all selection handling to
        // registered click listen.
        view.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        return mOnSelectionListener != null
                                && mOnSelectionListener.onItemLongClick(
                                        view, position);
                    }
                });
    }

    /**
     * Helper method to draw the current item's selection state.
     *
     * @param view     Any view.
     * @param position The view's associated adapter position.
     */
    private void drawSelectionState(View view, int position) {
        // Set list item background color based on selection state.
        if (isItemSelected(position)) {
            view.setBackgroundColor(
                    ContextCompat.getColor(
                            view.getContext(),
                            R.color.grid_item_selected_color_filter));
        } else {
            view.setBackgroundColor(
                    ContextCompat.getColor(
                            view.getContext(), android.R.color.transparent));
        }
    }

    /**
     * A custom RecycleView.ViewHolder implementation that contains just one
     * TextView.
     */
    public static class UriViewHolder extends RecyclerView.ViewHolder {
        final TextView mTextView;

        /**
         * Constructor simply stores a reference to the contained ImageView.
         *
         * @param view The item view.
         */
        public UriViewHolder(View view) {
            super(view);
            mTextView = (TextView) view.findViewById(R.id.text_view);
        }
    }
}
