package vandy.mooc.assignments.framework.application.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import vandy.mooc.assignments.R;
import vandy.mooc.assignments.assignment.downloader.AsyncTaskDownloader;
import vandy.mooc.assignments.assignment.downloader.HaMeRDownloader;
import vandy.mooc.assignments.assignment.downloader.ThreadPoolDownloader;
import vandy.mooc.assignments.framework.application.adapters.BaseAdapter;
import vandy.mooc.assignments.framework.application.adapters.ImageAdapter;
import vandy.mooc.assignments.framework.application.adapters.UriAdapter;
import vandy.mooc.assignments.framework.application.fragments.PagedFragment;
import vandy.mooc.assignments.framework.application.fragments.RecyclerViewFragment;
import vandy.mooc.assignments.framework.downloader.DownloadManager;
import vandy.mooc.assignments.framework.downloader.DownloadPolicy;
import vandy.mooc.assignments.framework.downloader.Downloader;
import vandy.mooc.assignments.framework.downloader.NetworkPolicy;
import vandy.mooc.assignments.framework.utils.AssignmentUtils;
import vandy.mooc.assignments.framework.utils.Preconditions;
import vandy.mooc.assignments.framework.utils.ViewUtils;

public abstract class GalleryActivityBase
        extends ActivityBase
        implements RecyclerViewFragment.OnFragmentListener {
    /**
     * Logging tag.
     */
    private static final String TAG = "GalleryActivityBase";

    /**
     * This span count used only when using a GridLayoutManager in the contained
     * RecyclerViewFragment.
     */
    private static final int GRID_SPAN_COUNT = 3;

    /**
     * A generic selection adapter that can be used for strings or images. The
     * type of adapter depends on the assignment version.
     */
    private BaseAdapter mAdapter;

    /**
     * A bundle that is sent by PagedActivity when it is exiting and is received
     * in onActivityReenter() which is called by the Android activity transition
     * framework. We use the data in this bundle to decide which image will be
     * animated during the return shared element transition.
     */
    private Bundle mReenterState;

    /**
     * Reference to contained RecyclerView fragment container. For assignment 1
     * we load a fragment that displays and manages a simple vertical list of
     * strings (URLs) and for assignments 2, 3, and 4 we load a fragment that
     * contains a displays and manages a grid view of downloaded images.
     */
    private RecyclerViewFragment mFragment;

    /**
     * SwipeRefreshLayout from XML layout allowing to support a nice Material
     * style swipe refresh do reload images from the network.
     */
    private SwipeRefreshLayout mSwipeRefreshLayout;

    /*
     * Abstract methods required for implementation sub-classes.
     */

    /**
     * Required assignment 2 override that constructs an intent containing the
     * passed URLs as and intent extra and then sets that activity results to
     * this intent and finishes the activity.
     *
     * @param urls The currently displayed list of image URLs.
     */
    protected abstract void createAndReturnResultsIntent(
            @NonNull ArrayList<Uri> urls);

    /**
     * Required assignment 3 override that constructs an intent containing the
     * passed URLs as and intent extra and then broadcasts the intent to the
     * MainActivity.
     *
     * @param uris The currently displayed list of image URLs.
     */
    protected abstract void createAndBroadcastResultsIntent(
            @NonNull ArrayList<Uri> uris);

    /*
     * Activity construction hook methods and helpers.
     */

    /**
     * Hook method called when a new instance of Activity is created. One time
     * initialization code goes here, e.g., UI layout initialization.
     *
     * @param savedInstanceState A Bundle object that contains saved state
     *                           information.
     */
    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        // This activity uses a CoordinatorLayout with a custom ActionBar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initializes all layout views.
        initializeViews();

        // Setup the swipe refresh listener.
        setupSwipeRefresh();
    }

    /**
     * Initializes all layout views and sets up a download policy. Both the
     * layout and the download policy will depend on the assignment version that
     * is currently being run.
     */
    private void initializeViews() {
        // Get a reference to the contained RecyclerView fragment.
        mFragment = (RecyclerViewFragment) getSupportFragmentManager()
                .findFragmentById(R.id.recycler_view_fragment);

        // Assignment dependent implementations.
        DownloadPolicy downloadPolicy = null;
        LinearLayoutManager layoutManager;

        // The type of adapter depends on the assignment number. Assignment 1
        // simply shows a string list of input URLs that it receives from the
        // MainActivity, while Assignments 2, 3, and 4, all show downloaded
        // images in a grid view.
        switch (AssignmentUtils.getAssignment(this)) {
            case 1: {
                // Use a linear layout with a string adapter. The download
                // manager is not used in this assignment.
                mAdapter = new UriAdapter(this, mFragment);
                layoutManager = new LinearLayoutManager(this);
                break;
            }

            case 2: {
                // Use a grid layout with an image adapter and a HaMeR
                // download policy.
                downloadPolicy = DownloadPolicy.HaMeRDownloader;
                mAdapter = new ImageAdapter(this, mFragment);
                layoutManager = new GridLayoutManager(this, GRID_SPAN_COUNT);
                break;
            }

            case 3: {
                // Use a grid layout and an image adapter and an AsyncTask
                // download policy.
                downloadPolicy = DownloadPolicy.AsyncTaskDownloader;
                mAdapter = new ImageAdapter(this, mFragment);
                layoutManager = new GridLayoutManager(this, GRID_SPAN_COUNT);
                break;
            }

            case 4: {
                // Use a grid layout with an image adapter and a thread pool
                // executor download policy.
                downloadPolicy =
                        DownloadPolicy.ThreadPoolExecutorDownloader;

                mAdapter = new ImageAdapter(this, mFragment);
                layoutManager = new GridLayoutManager(this, GRID_SPAN_COUNT);
                break;
            }

            default:
                throw new IllegalStateException("Invalid assignment number");
        }

        // Only assignments 2, 3, and 4 will use a downloader.
        if (downloadPolicy != null) {
            DownloadManager.get().setDownloadPolicy(downloadPolicy);
        }

        // Customize RecyclerViewFragment using the assignment specific
        // adapter and layout.
        mFragment.initializeViews(mAdapter, layoutManager);

        // Install a SharedElementCallback to handle return shared element
        // transition animations when returning from PagedActivity.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            installEnterTransitionHandler();
        }
    }

    /**
     * Sets up the swipe refresh handler and supporting listeners.
     */
    private void setupSwipeRefresh() {
        // Swipe refresh listener can be disabled by not including the layout.
        mSwipeRefreshLayout =
                (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        if (mSwipeRefreshLayout == null) {
            return;
        }

        // Set a tri-color indeterminate progressbar.
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.progress_start,
                R.color.progress_mid,
                R.color.progress_end);

        // Install a swipe refresh listener.
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (mFragment.isActionModeEnabled()) {
                            // Refreshing during action mode.
                            mSwipeRefreshLayout.setRefreshing(false);
                        } else if (AssignmentUtils.getAssignment(
                                GalleryActivityBase.this) == 1) {
                            // No refreshing for assignment 1.
                            mSwipeRefreshLayout.setRefreshing(false);
                        } else {
                            // Clear the cache and force items to go to the
                            // network when the refresh their data.
                            mFragment.refresh();
                        }
                    }
                });

        // Initially show the swipe refresh spinner for all assignments that
        // do downloading (not assignment 1).
        if (AssignmentUtils.getAssignment(this) != 1) {
            showRefreshProgress(true);
        }
    }

    /**
     * Helper method that forwards the downloader class registration request to
     * the DownloadManager.
     *
     * @param downloaderClass A Downloader implementation class.
     */
    protected final void registerDownloader(
            Class<? extends Downloader> downloaderClass) {
        if (!AssignmentUtils.ASSIGNMENT_SWITCHING) {
            // Running app without assignment switching submenu which requires
            // an explicit call to registerDownloader
            DownloadManager.get().registerDownloader(downloaderClass);
        } else {
            switch (AssignmentUtils.getAssignment(this)) {
                case 1:
                    // First assignment does not actually download any
                    // files... it simply starts the gallery activity and
                    // displays copies of any files that were listed on
                    // the main activity page.
                    break;
                case 2:
                    DownloadManager.get()
                            .registerDownloader(AsyncTaskDownloader.class);
                    break;
                case 3:
                    DownloadManager.get()
                            .registerDownloader(HaMeRDownloader.class);
                    break;
                case 4:
                    DownloadManager.get()
                            .registerDownloader(ThreadPoolDownloader.class);
                    break;
                default:
                    throw new IllegalStateException(
                            "Invalid assignment number");
            }
        }
    }

    /**
     * Returns the list of currently displayed items as a ArrayList of Strings.
     *
     * @return An List of Strings.
     */
    @SuppressWarnings("unused")
    protected final List<Uri> getItems() {
        return mFragment.getItems();
    }

    /**
     * Sets the contained fragment's adapter to the passed list of items and
     * updates all views to reflect this change.
     *
     * @param items An ArrayList of Strings.
     */
    protected final void setItems(@NonNull List<Uri> items) {
        mFragment.setItems(items);
        updateViews();

        // Handle the case where the refresh progress may have been started,
        // but the passed URL list contains one or more invalid entries. When
        // this happens, the URL list is set to null and we need to stop the
        // refresh animation that was automatically started from onCreate().
        if (items.size() == 0) {
            showRefreshProgress(false);
        }
    }

    /**
     * Hook method called when the user clicks the system back button.
     */
    @CallSuper
    @Override
    public void onBackPressed() {
        switch (AssignmentUtils.getAssignment(this)) {
            case 1: {
                // Assignment 1 does not return an intent.
                break;
            }

            case 3: {
                // Assignment 3 is required to return the currently displayed
                // image URLs to the previous activity (MainActivity) through
                // a locally broadcast data intent.
                createAndBroadcastResultsIntent(mFragment.getItems());
                break;
            }

            case 2:
            case 4: {
                // Assignments 2 and 4 are required to return the currently
                // displayed image URLs to the parent activity using a returned
                // result data intent.
                createAndReturnResultsIntent(mFragment.getItems());
                break;
            }

            default:
                throw new IllegalStateException("Invalid assignment number");
        }

        // Shutdown the DownloadManager.
        DownloadManager.get().shutdown();

        super.onBackPressed();
    }

    /**
     * Start the activity using shared element transitions.
     *
     * @param view     The selected item view.
     * @param position The selected item position.
     */
    private void startPagedActivity(
            View view,
            int position,
            Class<? extends PagedFragment> fragment) {
        // Use the adapter position in the shared element name so that
        // it will map to the correct image in the PagedActivity.
        // The installed custom PagedSharedElementCallback class will
        // handle replacing this shared element with whatever image is
        // displayed when the PagedActivity returns.
        String transitionName = String.valueOf(position);
        ViewCompat.setTransitionName(view, transitionName);

        // Setup the scene transition which is passed to the
        // startActivity() call.
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation
                        (this,
                         view,
                         transitionName);

        // Call the class factory method to create the intent to start the
        // activity.
        //noinspection unchecked
        Intent intent =
                PagedActivity.makeIntent(this,
                                         mAdapter.getItems(),
                                         position,
                                         fragment);

        // Start the activity. Note that the onActivityReenter() hook will
        // be called when returning from this started activity. When this
        // occurs, we save the passed bundle that is then used in the custom
        // PagedSharedElementCallback class.
        ActivityCompat.startActivity(this, intent, options.toBundle());
    }

    /**
     * Hook method called by RecyclerViewFragment is about to start action
     * mode.
     *
     * @return Always returns true to allow ActionMode.
     */
    @Override
    public boolean onActionModeStarting() {
        return true;
    }

    /**
     * Hook method called by RecyclerViewFragment once ActionMode has been
     * started.
     */
    @CallSuper
    @Override
    public void onActionModeStarted() {
        updateViews();
    }

    /**
     * Hook method called by RecyclerViewFragment when ActionMode finishes.
     */
    @CallSuper
    @Override
    public void onActionModeFinished() {
        // Update widgets to reflect current state of the URL list.
        updateViews();
    }

    /**
     * Hook method called by RecyclerViewFragment when the underlying adapter
     * contents is changed.
     *
     * @param mAdapter The fragment RecyclerView adapter that was changed.
     */
    @CallSuper
    @Override
    public void onDataChanged(BaseAdapter mAdapter) {
        // Update widgets to reflect current state of the URL list.
        updateViews();
    }

    /**
     * Hook method called by RecyclerViewFragment when a single item has been
     * clicked (not in ACTION_MODE). If this activity is currently displaying
     * images, we start the PagedActivity to display the clicked image.
     * Otherwise, if the gallery is only showing a list of strings, we do
     * nothing.
     *
     * @param view     The clicked item's view.
     * @param position The adapter position of the clicked item.
     */
    @Override
    public void onItemClicked(View view, int position) {
        // Kludge for funky assignment 1 only displays a list of strings
        // and therefore does not have a paged details view. In this case,
        // a single click will select the item rather than activate it.
        if (AssignmentUtils.getAssignment(this) == 1) {
            mFragment.selectItem(position);
        } else {
            startPagedActivity(view, position, null);
        }
    }

    /**
     * Hook method called by RecyclerViewFragment when a single item has been
     * clicked (not in ACTION_MODE). If this activity is currently displaying
     * images, we start the PagedActivity to display the clicked image.
     * Otherwise, if the gallery is only showing a list of strings, we do
     * nothing.
     *
     * @param view     The clicked item's view.
     * @param position The adapter position of the clicked item.
     * @param fragment A PagedFragment implementation.
     */
    @Override
    public void onItemClicked(
            View view,
            int position,
            Class<? extends PagedFragment> fragment) {
        // Kludge for funky assignment 1 only displays a list of strings
        // and therefore does not have a paged details view. In this case,
        // a single click will select the item rather than activate it.
        if (AssignmentUtils.getAssignment(this) == 1) {
            mFragment.selectItem(position);
        } else {
            startPagedActivity(view, position, fragment);
        }
    }

    /**
     * Hook method called by adapter when any item is waiting for data to
     * refresh.
     *
     * @param show {@code true} to show progress bar, {@code false} to hide it.
     */
    @CallSuper
    @Override
    public void onShowRefresh(boolean show) {
        showRefreshProgress(show);
        if (!show) {
            // Remove special network policy override that forced items to
            // refresh.
            DownloadManager.get().setNetworkPolicy(NetworkPolicy.NONE);
        }
    }

    /*
     * View helper methods.
     */

    /**
     * Shows or hides the swipe refresh indeterminate progress bar.
     *
     * @param show true to show the progress bar; {@code false} to hide it.
     */
    @SuppressWarnings("SameParameterValue")
    private void showRefreshProgress(final boolean show) {
        if (mSwipeRefreshLayout != null) {
            /**
             * All animations only work after onCreate so we post a runnable
             * to be sure that it runs at the appropriate time on the main
             * thread.
             */
            mSwipeRefreshLayout.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            mSwipeRefreshLayout.setRefreshing(show);
                        }
                    }
            );
        }
    }

    /**
     * Called whenever the activity state has changed and may therefore require
     * view updating. Since the current implementation contains a single
     * recycler view fragment, there are no custom widgets that this activity
     * needs to update. However, in case some are added, this is where view
     * updating should be performed.
     */
    @SuppressWarnings("EmptyMethod")
    private void updateViews() {
    }

    /**
     * Called when an activity you launched with an activity transition exposes
     * this Activity through a returning activity transition, giving you the
     * resultCode and any additional data from it. This method will only be
     * called if the activity set a result code other than RESULT_CANCELED and
     * it supports activity transitions with FEATURE_ACTIVITY_TRANSITIONS.
     *
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data       An Intent, which can return result data to the caller
     *                   (various data can be attached to Intent "extras").
     */
    @CallSuper
    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        // Save the reentry extras so that they will be available when the
        // framework calls the onMapSharedElements() listen.
        mReenterState = new Bundle(data.getExtras());
    }

    /**
     * Setup a custom shared element callback that will update the return shared
     * element to the current paged image displayed when returning from the
     * PagedActivity (see PagedSharedElementCallback for a more detailed
     * description).
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void installEnterTransitionHandler() {
        ActivityCompat.setExitSharedElementCallback(
                this, new PagedSharedElementCallback());
    }

    /**
     * A shared element callback implementation that is installed in the
     * transition animation framework using setExitSharedElementCallback(). The
     * reason for installing an instance of this class is to handle the case
     * where the user clicks on image A to view it in the PagedActivity, and
     * then swipes to image B before returning to this activity. In this case,
     * the default Android shared element transition framework will attempt (and
     * fail) to animate image A back to its position in this activity's grid.
     * <p/>
     * The onMapSharedElement() hook, handles this case and replaces the shared
     * element information for A with that of B so that the shared element
     * transition animation framework will perform the animation using the
     * updated image source (image view B in the ViewPager) and target (image
     * view B in the grid).
     * <p/>
     * Each image in the PagedActivity uses a transitionName property equal to
     * "n" where n is the adapter position of the gallery item. By using this
     * transition naming convention, we can strip of the position number and use
     * it to retrieve the ViewHolder from the RecyclerView which contains the
     * new source image view that we add to the shared element name list and
     * view map.
     */
    private class PagedSharedElementCallback
            extends SharedElementCallback {
        @SuppressLint("Assert")
        @Override
        public void onMapSharedElements(
                List<String> names,
                Map<String, View> sharedElements) {
            // We are only interested in a re-enter event which is signaled
            // by the mReenterState bundle containing the information we need
            // to handle setting up a new shared element for the return.
            if (mReenterState == null) {
                return;
            }

            int position =
                    mReenterState.getInt(PagedActivity.EXTRA_POSITION, -1);
            if (position == -1) {
                Log.w(TAG,
                      "No adapter position was received for return transition");
                names.clear();
                sharedElements.clear();
                return;
            }

            // Get the recycler view.
            RecyclerView recyclerView =
                    (RecyclerView) Preconditions.checkNotNull(
                            findViewById(R.id.recycler_view_fragment));

            // Get the view holder so that we can access the actual image view.
            // If we are unable to get the view holder then the image view we
            // are looking for must not be visible, so cancel transition.
            RecyclerView.ViewHolder viewHolder =
                    recyclerView.findViewHolderForAdapterPosition(position);
            if (viewHolder == null) {
                Log.w(TAG, "Image view for return transition is not visible");
                names.clear();
                sharedElements.clear();
                return;
            }

            // Since this gallery activity supports multiple adapters we need
            // to find the ImageView that matches return transition name.
            String transitionName = String.valueOf(position);
            ImageView imageView =
                    ViewUtils.findImageViewWithTransitionName(
                            viewHolder.itemView, transitionName);
            if (imageView != null) {
                String exitName = ViewCompat.getTransitionName(imageView);

                // Add the exiting shared element name if not already added.
                if (!names.contains(exitName)) {
                    names.add(exitName);
                }

                // Add or update the associated shared element view.
                sharedElements.put(exitName, imageView);
            } else {
                Log.w(TAG, "Unable to locate return shared element image view");
                names.clear();
                sharedElements.clear();
            }

            mReenterState = null;
        }
    }
}
