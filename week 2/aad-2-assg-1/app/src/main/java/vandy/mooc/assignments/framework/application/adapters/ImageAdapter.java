package vandy.mooc.assignments.framework.application.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import vandy.mooc.assignments.R;
import vandy.mooc.assignments.framework.downloader.DownloadManager;
import vandy.mooc.assignments.framework.downloader.RequestListener;
import vandy.mooc.assignments.framework.utils.Preconditions;

public class ImageAdapter
        extends BaseAdapter<Uri, ImageAdapter.GridViewHolder> {
    /**
     * Logging tag.
     */
    private static final String TAG = "ImageAdapter";

    /**
     * Constructor.
     *
     * @param listener A optional OnSelectionListener.
     * @param context  The activity context.
     */
    public ImageAdapter(
            Context context,
            @Nullable OnSelectionListener listener) {
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
    public GridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grid, parent, false);

        return new GridViewHolder(view);
    }

    /**
     * Called when a view created by this adapter has been recycled. Although
     * the downloader framework handles recycled views, we also need to catch
     * them here so that we can maintain an accurate refresh count for the
     * activities progress indicator. Also, since we are trapping this event, we
     * may as well cancel an pending download to save battery. Downloader has a
     * convenience method for cancelling requests that were started with
     * into(ImageView).
     *
     * @param holder The ViewHolder for the view being recycled
     */
    @Override
    public void onViewRecycled(GridViewHolder holder) {
        if (DownloadManager.get().cancelRequest(holder.mImageView)) {
            Preconditions.checkState(mRefreshCount >= 0,
                                     "Invalid refresh count " + mRefreshCount);
            Log.d(TAG, "onViewRecycled: request cancelled");
            if (--mRefreshCount == 0) {
                showRefresh(false);
            }
        }
        super.onViewRecycled(holder);
    }

    /**
     * Removes all cached images and then forces full refresh.
     */
    public void refresh() {
        DownloadManager.clearCache(getClass().getSimpleName());
        notifyDataSetChanged();
    }

    /**
     * Called to bind you view to the adapter data associated with this view.
     *
     * @param holder   The GridViewHolder instance to bind
     * @param position The adapter position of the data for this view.
     */
    @Override
    public void onBindViewHolder(GridViewHolder holder, int position) {
        // Never rely on passed position; always use the real adapter position.
        final int adapterPosition = holder.getAdapterPosition();

        // Initialize all user action event listeners.
        initializeListeners(holder.mImageView, adapterPosition);

        Preconditions.checkState(
                0 <= mRefreshCount && mRefreshCount <= getItemCount(),
                "Invalid refresh count "
                        + mRefreshCount);

        // Keep track of how many items are loading.
        if (++mRefreshCount == 1) {
            showRefresh(true);
        }

        // Asynchronously download and display the target URL image.

        DownloadManager.with((Activity) mContext)
                .load(getItem(position))
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .resize(140, 140)
                .tag(getClass().getSimpleName())
                .listen(new RequestListener() {
                    @Override
                    public void onResourceReady(Object resource) {
                        Preconditions.checkState(
                                mRefreshCount > 0, "Invalid refresh count "
                                        + mRefreshCount);
                        if (--mRefreshCount == 0) {
                            showRefresh(false);
                        }
                    }

                    @Override
                    public void onRequestFailed() {
                        Preconditions.checkState(
                                mRefreshCount > 0, "Invalid refresh count "
                                        + mRefreshCount);
                        if (--mRefreshCount == 0) {
                            showRefresh(false);
                        }
                    }
                })
                .into(holder.mImageView);

        // Draw the current view selection state.
        drawSelectionState(holder.mImageView, position);

        // Set a unique shared element transitionName to supporting return
        // shared element transition animations.
        ViewCompat.setTransitionName(
                holder.mImageView, String.valueOf(adapterPosition));
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
        // registered click listen (activity).
        view.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mOnSelectionListener != null) {
                            mOnSelectionListener.onItemClick(view, position);
                        }
                    }
                });

        // Redirect all selection handling to
        // registered click listen (activity).
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
    private void drawSelectionState(ImageView view, int position) {
        // Set list item background color based on selection state.
        if (isItemSelected(position)) {
            view.setColorFilter(
                    ContextCompat.getColor(
                            view.getContext(),
                            R.color.grid_item_selected_color_filter),
                    PorterDuff.Mode.SRC_ATOP);
        } else {
            view.clearColorFilter();
        }
    }

    /**
     * A custom RecycleView.ViewHolder implementation that contains just one
     * ImageView.
     */
    public static class GridViewHolder extends RecyclerView.ViewHolder {
        public final ImageView mImageView;

        /**
         * Constructor simply stores a reference to the contained ImageView.
         *
         * @param view The item view.
         */
        public GridViewHolder(View view) {
            super(view);
            mImageView = (ImageView) view.findViewById(R.id.image_view);
        }
    }
}
