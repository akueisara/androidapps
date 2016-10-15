package vandy.mooc.assignments.framework.application.activities;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import vandy.mooc.assignments.R;
import vandy.mooc.assignments.framework.application.adapters.BaseAdapter;
import vandy.mooc.assignments.framework.application.adapters.UriAdapter;
import vandy.mooc.assignments.framework.application.fragments.PagedFragment;
import vandy.mooc.assignments.framework.application.fragments.RecyclerViewFragment;
import vandy.mooc.assignments.framework.application.views.InputPanelView;
import vandy.mooc.assignments.framework.utils.AssignmentUtils;
import vandy.mooc.assignments.framework.utils.UriUtils;

/**
 * Framework base class that should be sub-classed the the applications main
 * activity. This class supports add and download FABs and a fragment that
 * contains a RecyclerView that manages a list of strings. This class also
 * manages all configuration changes and also saving and restoring the displayed
 * list of strings between application sessions.
 * <p>
 * In addition to managing the string list, this class also supports starting
 * the GalleryActivity which is receives, downloads, and displays the data
 * associated with the list of strings (URLs) managed by this class. The intent
 * handling and the download policy used by the GalleryActivity will depend on
 * the assignment number that is by the extended ActivityBase class. The
 * ActivityBase class manages menu entries for switching between different
 * assignment numbers and this class uses the current assignment number setting
 * to call abstract methods that are to be implemented by students in each of
 * the supported assignments. The GalleryActivity also extends ActivityBase and
 * therefore is also designed with abstract methods that students are required
 * to implement for each assignment.
 * <p>
 * In addition to these abstract method calls, portions of assignments solutions
 * are also implemented by overriding certain methods in this class.
 */
public abstract class MainActivityBase
        extends ActivityBase
        implements RecyclerViewFragment.OnFragmentListener,
                   InputPanelView.InputListener {
    /**
     * Debug logging tag.
     */
    private static final String TAG = "MainActivityBase";

    /**
     * Key used for saving input URLs in bundle during a configuration change.
     */
    private static final String KEY_INPUT_URLS = "input_urls";

    /**
     * Custom SnackBar like input view used for entering URLs.
     */
    private InputPanelView mInputPanel;

    /**
     * FAB view used for adding items to the list.
     */
    private FloatingActionButton mAddFab;

    /**
     * FAB view used to perform the download operation.
     */
    private FloatingActionButton mDownloadFab;

    /**
     * Reference to contained RecyclerView fragment container.
     */
    private RecyclerViewFragment mFragment;

    /*
     * Activity Construction
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
        // Always call the superclass onCreate().
        super.onCreate(savedInstanceState);

        // Load the activity layout.
        setContentView(R.layout.activity_main);

        // This activity uses a CoordinatorLayout with a custom ActionBar.
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize all layout views.
        initializeViews();

        if (savedInstanceState == null) {
            // The activity is being created for the first time so restore
            // session state from shared preferences. Configuration change
            // save/restore state are performed in onSaveInstanceState() and
            // onRestoreInstanceState().
            restoreStateFromPreferences();
        }
    }

    /**
     * Initializes all views and registers the FAB click listeners.
     */
    private void initializeViews() {
        // The input URL list is managed by a single RecyclerViewFragment.
        mFragment = (RecyclerViewFragment) getSupportFragmentManager()
                .findFragmentById(R.id.recycler_view_fragment);

        // Initialize this fragment with and adapter, layout. The data will
        // is either set by the user or is restored by the fragment from a
        // previous session.
        UriAdapter adapter = new UriAdapter(this, mFragment);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mFragment.initializeViews(adapter, layoutManager);

        // Get a reference to the input panel layout and text view.
        mInputPanel = (InputPanelView) findViewById(R.id.input_panel);
        assert mInputPanel != null;
        mInputPanel.setKeyboardListener(this);

        // Create a floating action button (FAB) used for adding new Urls.
        mAddFab = (FloatingActionButton) findViewById(R.id.add_fab);
        assert mAddFab != null;
        mAddFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                showInputPanel(!mInputPanel.isPanelShown());
            }
        });

        // Create a floating action button (FAB) used for download Urls.
        mDownloadFab = (FloatingActionButton) findViewById(R.id.download_fab);
        assert mDownloadFab != null;
        mDownloadFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDownloadClicked();
            }
        });
    }

    /*
     * Activity lifecycle hook methods.
     */

    /**
     * Hook method called when the activity is about to be resumed. The purpose
     * of implementing this hook is to update the delete FAB widget each time
     * this activity becomes the foreground activity.
     */
    @CallSuper
    @Override
    protected void onResume() {
        // Whenever the activity is being resumed ensure that the layout
        // views are all up to date.
        updateViews();

        // Always call super class method.
        super.onResume();
    }

    /**
     * Hook method called when an activity is about to be destroyed.
     */
    @CallSuper
    @Override
    protected void onDestroy() {
        // Call helper to save the application state between invocations.
        // The state will be restored in onCreate() when the app is run
        // for the first time (next session).
        saveApplicationState();

        // Always call super method.
        super.onDestroy();
    }

    /*
     * Session state helper methods.
     */

    /**
     * Saves input URLs the application preferences so that they can be restored
     * the next time the application is run.
     */
    private void saveApplicationState() {
        // Get the private shared preferences.
        final SharedPreferences preferences =
                getPreferences(MODE_PRIVATE);

        // Get the preferences editor.
        final SharedPreferences.Editor editor = preferences.edit();

        // Convert url list to a hash set of strings for saving.
        ArrayList<Uri> inputUrls = mFragment.getItems();
        HashSet<String> hashSet = new HashSet<>(inputUrls.size());
        for (final Uri inputUrl : inputUrls) {
            hashSet.add(inputUrl.toString());
        }

        // Save the input URLs.
        editor.putStringSet(KEY_INPUT_URLS, hashSet);

        // Apply the update.
        editor.apply();
    }

    /**
     * Restores input URLs that were previously saved the last time the
     * application was destroyed.
     */
    private void restoreStateFromPreferences() {
        // Get the private shared preferences.
        final SharedPreferences preferences =
                getPreferences(MODE_PRIVATE);

        // Restore input URLs list into the RecyclerViewFragment.
        final Set<String> stringSet =
                preferences.getStringSet(KEY_INPUT_URLS, new HashSet<String>());

        // Convert hash set of strings back to a uri list.
        ArrayList<Uri> urls = new ArrayList<>(stringSet.size());
        for (final String string : stringSet) {
            urls.add(Uri.parse(string));
        }

        mFragment.setItems(urls);
    }

    /*
     * UI command input hooks and helper methods.
     */

    /**
     * Hook method called to create and add menu items to the passed menu.
     *
     * @param menu The menu to update.
     * @return The super class method call determines the return value.
     */
    @CallSuper
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Always call super class method.
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to proceed,
     * true to consume it here.
     */
    @CallSuper
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case R.id.action_load_defaults:
                loadDefaultUrls();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * View updating helper methods.
     */

    /**
     * Updates all layout views to be consistent with the current data state.
     */
    private void updateViews() {
        updateDownloadFabState();
        updateAddFabState();
    }

    /**
     * Shows or hides the input panel.
     *
     * @param show Pass true to show, false to hide.
     */
    @SuppressWarnings("WeakerAccess")
    public void showInputPanel(boolean show) {
        if (show != mInputPanel.isPanelShown()) {
            mInputPanel.show(show);

            // Rotate FAB from + to x or from x to +.
            int animResId = show
                            ? R.anim.fab_rotate_forward
                            : R.anim.fab_rotate_backward;

            // Load and start the animation.
            mAddFab.startAnimation(
                    AnimationUtils.loadAnimation(this, animResId));
        }
    }

    /**
     * Hook method called by RecyclerViewFragment when ActionMode is about to be
     * started. We simply allow action mode to start and then update the FABs
     * once ActionMode calls the onActionModeStarted() hook.
     *
     * @return Always returns true to allow ActionMode to start.
     */
    @Override
    public boolean onActionModeStarting() {
        return true;
    }

    /**
     * Hook method called by RecyclerViewFragment once ActionMode has been
     * started.
     */
    @Override
    public void onActionModeStarted() {
        // Always hide the input panel when action mode is started.
        showInputPanel(false);
        // Update widgets to reflect the started ActionMode state.
        updateViews();
    }

    /**
     * Hook method called by RecyclerViewFragment when ActionMode completes.
     */
    @Override
    public void onActionModeFinished() {
        // Update widgets to reflect current state of the URL list.
        updateViews();
    }

    /**
     * Hook method called by RecyclerViewFragment when the underlying adapter
     * contents is changed.
     *
     * @param mAdapter A reference to the underlying fragment adapter.
     */
    @Override
    public void onDataChanged(BaseAdapter mAdapter) {
        // Update widgets to reflect current state of the URL list.
        updateViews();
    }

    /**
     * Hook method called by RecyclerViewFragment when a single item has been
     * clicked (not in ACTION_MODE). Since this activity simply shows a list of
     * strings (URLs) and there is no details activity for each item, the UI
     * feels more intuitive when a single click simply starts a multiple
     * selection using ActionMode. This is accomplished by simply calling the
     * contained fragment's selectItem() helper method.
     *
     * @param view     The item view that was clicked.
     * @param position The clicked item's adapter position.
     */
    @Override
    public void onItemClicked(View view, int position) {
        mFragment.selectItem(position);
    }

    /**
     * Called when a single item is clicked (not in ACTION_MODE) so that the
     * listen can typically being a "details view" operation.
     *
     * @param view     Item view.
     * @param position Item adapter position.
     * @param fragment The PagedFragment where the click occurred.
     */
    @Override
    public void onItemClicked(
            View view, int position, Class<? extends PagedFragment> fragment) {
        onItemClicked(view, position);
    }

    /*
     * Widget event hook methods.
     */

    /**
     * Hook method invoked by InputPanelView when the user has entered a URL.
     *
     * @param text URL text entered by the user.
     */
    @Override
    public void onInputReceived(String text) {
        if (!TextUtils.isEmpty(text)) {
            // Pass the new URL string to the fragment to add to it's list.
            mFragment.addItems(Uri.parse(text));
        }

        // Hide the input panel.
        showInputPanel(false);

        updateViews();
    }

    /**
     * Hook method invoked by InputPanelView when the input action was
     * cancelled.
     */
    @Override
    public void onInputCancelled() {
        showInputPanel(false);
    }

    /**
     * Hook method called when download FAB is clicked.
     */
    private void onDownloadClicked() {
        // Cancel all input states and start the download.
        showInputPanel(false);
        if (mFragment.isActionModeEnabled()) {
            throw new IllegalStateException(
                    "Download should be disabled during action mode");
        }

        // Get the currently displayed list of strings from the fragments
        // adapter.
        ArrayList<Uri> inputUrls = mFragment.getItems();

        // Control flow is based on assignment version.
        switch (AssignmentUtils.getAssignment(this)) {
            case 1:
                startDownload(inputUrls);
                break;

            case 2:
                startDownloadForResult(inputUrls);
                break;

            case 3:
                startDownload(inputUrls);
                break;

            case 4:
                startDownloadForResult(inputUrls);
                break;

            default:
                throw new IllegalStateException("Invalid assignment version");
        }
    }

    /*
     * Action command helper methods.
     */

    /**
     * Method that is called to construct a GalleryActivity starting
     * intent containing the passed list of urls, and then uses that intent to
     * start the GalleryActivity.
     * <p>
     * Required for: assignment 1 and 3.
     *
     * @param urls The list of URLs to pass to the gallery activity.
     */
    protected void startDownload(ArrayList<Uri> urls){
        throw new UnsupportedOperationException();
    }

    /**
     * Method that is called to construct a GalleryActivity starting
     * intent containing the passed list of urls, and then uses that intent to
     * start the GalleryActivity for returned results.
     * <p>
     * Required for: assignment 2.
     *
     * @param urls The list of URLs to pass to the gallery activity.
     */
    protected void startDownloadForResult(ArrayList<Uri> urls){
        throw new UnsupportedOperationException();
    }

    /**
     * Appends a set of default URLs (stored in the app resources) to the
     * current list.
     */
    private void loadDefaultUrls() {
        final String[] stringArray =
                getResources().getStringArray(
                        AssignmentUtils.getAssignment(this) <= 4
                        ? R.array.default_image_urls
                        : R.array.default_sound_urls);
        ArrayList<Uri> uris = UriUtils.parseAll(stringArray);
        mFragment.addItems(uris);
        updateViews();
    }

    /**
     * Shows or hides the download FAB based on the current list state.
     */
    private void updateDownloadFabState() {
        // FAB is enabled if the we are not in action mode
        // and the list contains items.
        boolean show = !mFragment.isActionModeEnabled()
                && mFragment.getItemCount() > 0;
        Log.d(TAG, show ? "Showing" : "hiding" + " download FAB");
        animateFab(mDownloadFab, show);
    }

    /**
     * Shows or hides the download FAB based on the current list state.
     */
    private void updateAddFabState() {
        // FAB is enabled if the we are not in action mode
        // and the list contains items.
        boolean show = !mFragment.isActionModeEnabled();
        Log.d(TAG, show ? "Showing" : "hiding" + " add FAB");
        animateFab(mAddFab, show);
    }

    /**
     * Common FAB animation helper used for showing and hiding both the add and
     * download FAB animations. The animation simple adds a nice resizing effect
     * to the normal FAB show()/hide() translation animation.
     * <p>
     * Note that any call to FloatingActionButton#show() or to
     * FloatingActionButton#hide() will fail here because the FABs are anchored
     * to a bottom sheet which uses the default bottom sheet layout behaviour.
     * This behaviour, will automatically cancel any attempt to hide an
     * anchored FAB (for an unknown reason). To work around this limitation,
     * we show and hide the FABs a custom animation (by passing the default
     * show() and hide() methods).
     *
     * @param fab  The FAB to animate
     * @param show true if FAB should be shown, false to hide.
     */
    private void animateFab(FloatingActionButton fab, boolean show) {
        // Since we can't rely on show and hide we use the scale to determine
        // if the FAB is currently shown or hidden.
        if (!show && fab.getScaleX() > 0) {
            // Hide the fab with a nice double animation. Note DO NOT call
            // the fab.hide() here because it will not hide the FAB.
            fab.animate()
                    .scaleX(0)
                    .scaleY(0)
                    .translationY(fab.getHeight() + 100)
                    .setInterpolator(new AccelerateInterpolator(2))
                    .start();
        } else if (show && fab.getScaleX() == 0) {
            // Show the fab with a nice double animation. Note DO NOT call
            // the fab.show() here because it will not show the FAB.
            fab.animate()
                    .scaleX(1)
                    .scaleY(1)
                    .translationY(0)
                    .setInterpolator(new DecelerateInterpolator(2))
                    .start();
        }
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity as
     * appropriate. We override this method to prevent the application from being
     * dismissed when the input panel is visible.
     */
    @Override
    public void onBackPressed() {
        if (mInputPanel.isPanelShown()) {
            mInputPanel.show(false);
        } else {
            super.onBackPressed();
        }
    }

    /*
     * Property accessors and mutators.
     */

    /**
     * Returns the list of currently displayed items as a ArrayList of Strings.
     *
     * @return An ArrayList of Strings.
     */
    @CallSuper
    @SuppressWarnings("unused")
    protected ArrayList<Uri> getItems() {
        return mFragment.getItems();
    }

    /**
     * Sets the contained fragment's adapter to the passed list of items and
     * updates all views to reflect this change.
     *
     * @param items An ArrayList of Strings.
     */
    @CallSuper
    protected void setItems(ArrayList<Uri> items) {
        mFragment.setItems(items);
        updateViews();
    }
}
