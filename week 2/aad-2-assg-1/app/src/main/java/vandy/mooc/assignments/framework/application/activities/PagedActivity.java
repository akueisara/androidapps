package vandy.mooc.assignments.framework.application.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vandy.mooc.assignments.R;
import vandy.mooc.assignments.framework.application.fragments.PagedFragment;
import vandy.mooc.assignments.framework.application.views.ZoomOutPageTransformer;
import vandy.mooc.assignments.framework.utils.Preconditions;
import vandy.mooc.assignments.framework.utils.ViewUtils;

/**
 * A generic activity that contains a set of PagedFragment objects and supports
 * a shared element image transition with the parent activity.
 */
public class PagedActivity
        extends ActivityBase
        implements PagedFragment.OnPagedFragmentCallback {
    /**
     * Debug logging tag.
     */
    private static final String TAG = "PagedActivity";
    /**
     * String key names used for intent extras.
     */
    protected static final String EXTRA_ITEMS = "Items";
    protected static final String EXTRA_POSITION = "Position";
    protected static final String EXTRA_FRAGMENT_CLASS = "FragmentClass";

    /**
     * The adapter used for the view pager containing that contains fragments.
     */
    private FragmentPagerAdapter mPagerAdapter;

    /**
     * The list urls that can be swiped to display.
     */
    private List<Uri> mUrls;

    /**
     * The adapter position of the initially displayed item.
     */
    private int mPosition;

    /**
     * A reference to the layout view pager.
     */
    private ViewPager mViewPager;

    /**
     * The transition name of a shared element (image view) when this activity
     * first exits.
     */
    private boolean mExitTransition;

    /**
     * Class to use in the view pager.
     */
    private Class mFragmentClass;

    /**
     * Factory method that can be called to construct an intent to start this
     * activity.
     *
     * @param context  An activity context.
     * @param items    The list of items can be swiped/viewed.
     * @param position The index of the initial item to display.
     * @return An intent that can be used to start this activity.
     */
    public static Intent makeIntent(
            Context context,
            ArrayList<Uri> items,
            int position,
            Class<? extends PagedFragment> fragment) {
        Intent intent = new Intent(context, PagedActivity.class);
        intent.putParcelableArrayListExtra(EXTRA_ITEMS, items);
        intent.putExtra(EXTRA_POSITION, position);
        intent.putExtra(EXTRA_FRAGMENT_CLASS, fragment);
        return intent;
    }

    /**
     * Hook method called when a new instance of Activity is created. One time
     * initialization code goes here, e.g., UI layout initialization.
     *
     * @param savedInstanceState A Bundle object that contains saved state
     *                           information.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Always call the superclass onCreate().
        super.onCreate(savedInstanceState);

        // Load the view from the XML layout.
        setContentView(R.layout.activity_paged);

        // This activity uses a CoordinatorLayout with a custom ActionBar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize fields from the starting intent extras.
        Intent intent = getIntent();
        mUrls = intent.getParcelableArrayListExtra(EXTRA_ITEMS);
        mPosition = intent.getIntExtra(EXTRA_POSITION, 0);
        mFragmentClass =
                (Class) intent.getSerializableExtra(EXTRA_FRAGMENT_CLASS);
        Serializable serializableExtra =
                intent.getSerializableExtra(EXTRA_FRAGMENT_CLASS);
        mFragmentClass = (Class) serializableExtra;

        // Initialize all layout views.
        initializeViews();

        if (mPosition >= 0) {
            // Postpone the shared element transition until the current
            // fragment has loaded the shared element target image view
            // and the view has been fully laid out.
            Log.d(TAG, "Postponing shared element transition...");
            supportPostponeEnterTransition();
            installExitTransitionHandler();
        }
    }

    /**
     * Initializes view pager adapter and view pager.
     */
    private void initializeViews() {
        // Create the adapter.
        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager());

        // Create and initialize the view pager.
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        assert mViewPager != null;
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());

        // Always notify adapter when contents is changed.
        mPagerAdapter.notifyDataSetChanged();
    }

    /**
     * Activity lifecycle hook method called when an activity is being started.
     */
    @Override
    protected void onStart() {
        super.onStart();

        // Set the current page to image position passed by the activity.
        mViewPager.setCurrentItem(mPosition);
    }

    /**
     * Hook method called when the user clicks the system back button. Sets a
     * result intent that will be received by the activity's the result intent
     * that will be received in private void onMapSharedElementsForExit(
     */
    @Override
    public void onBackPressed() {
        // Set the activity result to an intent that contains the transition
        // name of the currently displayed image view.
        int position = mViewPager.getCurrentItem();
        Intent intent = new Intent();
        intent.putExtra(EXTRA_POSITION, position);
        setResult(RESULT_OK, intent);

        // Set flag for shared element transition callback which
        // will update the return shared element to reflect which
        // ever image the user has paged to.
        mExitTransition = true;
        super.onBackPressed();
    }

    /**
     * Hook method called by adapter to notify that the background image loading
     * operation has completed. Now that the ImageView contains a valid image,
     * we schedule a postponed shared element transition using an
     * OnPreDrawListener that will wait until the image view has been fully laid
     * out before starting the postponed shared element transition.
     *
     * @param view    The Image view that was just loaded.
     * @param success true if the download image was set; {@code false} if not.
     */
    @Override
    public void onSharedElementReady(ImageView view, boolean success) {
        // This method will be called by each fragment once its image has been
        // loaded. Fragments are required to set their image view transition
        // names.
        try {
            int position = Integer.valueOf(ViewCompat.getTransitionName(view));
            if (position == mPosition) {
                if (success) {
                    Log.d(TAG,
                          "scheduling a startPostponedEnterTransition ...");
                    scheduleStartPostponedEnterTransition(view);
                } else {
                    Log.d(TAG,
                          "Shared element load failed; starting transition");
                    supportStartPostponedEnterTransition();
                }
            }
        } catch (NumberFormatException e) {
            Log.w(TAG, "Shared element view transition name not set properly");
        }
    }

    /**
     * Registers an view tree observer that will start a previously postponed
     * shared element transition once the target view has been fully laid out.
     *
     * @param observerView shared element target view
     */
    private void scheduleStartPostponedEnterTransition(
            final View observerView) {
        if (observerView == null) {
            return;
        }

        // First postpone the transition and then setup a tree observer
        // to look wait for the layout to complete after which the
        // postponed transition will be started.
        observerView.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        observerView.getViewTreeObserver()
                                .removeOnPreDrawListener(this);
                        Log.d(TAG, "Starting the postponed transition ...");
                        supportStartPostponedEnterTransition();
                        return true;
                    }
                });
    }

    /*
     * Animation support helpers
     */

    /**
     * Install a shared element callback that will be used to update the shared
     * element map with the movie id and poster image view of whatever movie is
     * currently being displayed in the ViewPager when this activity finishes.
     */
    private void installExitTransitionHandler() {
        // This callback occurs for the the enter and exit transitions
        // for this activity. We are only interested in modifying the
        // mapped shared element to the the poster image of the currently
        // displayed movie in the ViewPager.
        ActivityCompat.setEnterSharedElementCallback(
                this, new SharedElementCallback() {
                    @Override
                    public void onMapSharedElements(
                            List<String> names,
                            Map<String, View> sharedElements) {
                        onMapSharedElementsForExit(names, sharedElements);
                    }
                });
    }

    /**
     * Hook method called by the shared element support framework when the
     * current activity is being entered or exited. When the activity is being
     * exited, whe update the shared element name and view map to reflect the
     * view and name of the last displayed image view so that it will be used in
     * the shared element transition animation rather than the original item
     * that was used when this activity was first started.
     *
     * @param names          The list of shared element names.
     * @param sharedElements The list of shared element views.
     */
    private void onMapSharedElementsForExit(
            List<String> names, Map<String, View> sharedElements) {
        // Only interested in handling an exit transition.
        if (!mExitTransition) {
            return;
        }

        int position = mViewPager.getCurrentItem();
        Fragment fragment = mPagerAdapter.getItem(position);

        // The fragment view will unfortunately be null if an orientation change
        // occurred while viewing a paged image. When this occurs, we will just
        // abandon the attempt to do a return shared element transition.
        if (fragment == null || fragment.getView() == null) {
            return;
        }

        // Shared element transition requires a layout with an image view with
        // id "R.id.image_view". This should be rethought...
        String transitionName = String.valueOf(position);
        ImageView imageView =
                ViewUtils.findImageViewWithTransitionName(
                        fragment.getView(), transitionName);
        if (imageView == null) {
            Log.w(TAG,
                  "Unable to locate image view for shared element transition");
            names.clear();
            sharedElements.clear();
            return;
        }

        // Get enter and exit transition names.
        String enterName = String.valueOf(mPosition);
        String exitName = ViewCompat.getTransitionName(imageView);

        if (exitName != null) {
            // Check if the exit shared element is different
            // from the original enter shared element.
            if (!TextUtils.equals(enterName, exitName)) {
                // The exit shared element not the same as the
                // enter shared element so update the name list
                // new name of the new shared element.
                names.set(names.indexOf(enterName), exitName);
                sharedElements.remove(enterName);
            }

            // Update the shared element map with the exiting
            // shared element name and view.
            sharedElements.put(exitName, imageView);
        } else {
            Log.w(TAG, "Unable to locate return shared element image view");
            names.clear();
            sharedElements.clear();
        }
    }

    /**
     * Static subclass FragmentStatePagerAdapter implementation that is suitable
     * to support swiping between a potentially large number of dynamically
     * created fragment pages.
     **/
    private class FragmentPagerAdapter extends FragmentStatePagerAdapter {

        /**
         * Maintain in local fragment map since the default implementation does
         * not handle this properly.
         */
        @SuppressLint("UseSparseArrays")
        final HashMap<Integer, PagedFragment> mFragmentMap = new HashMap<>();

        /**
         * Constructor.
         *
         * @param fm FragmentManager (support FragmentManager in this case)
         */
        public FragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Method called to either return an existing page instance or to
         * construct a new one if not already cached.
         *
         * @param position The adapter position of the page to get.
         * @return Returns a new or cached fragment.
         */
        @Override
        public Fragment getItem(int position) {
            PagedFragment fragment = mFragmentMap.get(position);
            if (fragment != null) {
                return fragment;
            }

            try {
                fragment =
                        Preconditions.checkNotNull(
                                (PagedFragment) mFragmentClass.newInstance());
                Bundle args = new Bundle();
                args.putParcelable(
                        PagedFragment.ARG_RESOURCE_URI, mUrls.get(position));
                args.putInt(PagedFragment.ARG_POSITION, position);
                fragment.setArguments(args);
            } catch (InstantiationException e) {
                e.printStackTrace();
                return null;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }

            // Set the url for this fragment to display.
            fragment.setUrl(mUrls.get(position));

            // Keep track of fragments.
            mFragmentMap.put(position, fragment);

            return fragment;
        }

        /**
         * Called when a fragment is to be destroyed. We ensure that we remove
         * the destroyed fragment from the internally maintained map.
         */
        @Override
        public void destroyItem(
                ViewGroup container,
                int position,
                Object object) {
            super.destroyItem(container, position, object);
            mFragmentMap.remove(position);
        }

        /**
         * Returns the count of items in this pager adapter.
         *
         * @return Number of pageable fragments.
         */
        @Override
        public int getCount() {
            return mUrls.size();
        }
    }
}
