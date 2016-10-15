package vandy.mooc.assignments.framework.application.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import vandy.mooc.assignments.R;
import vandy.mooc.assignments.framework.downloader.DownloadManager;
import vandy.mooc.assignments.framework.downloader.RequestListener;
import vandy.mooc.assignments.framework.utils.UriUtils;

/**
 * A generic details fragment whose main data element is a resource uri. This
 * class along with the parent PagedActivity provide shared element transition
 * animation for a single image.
 * <p>
 * {@link Fragment} subclass. Use the {@link PagedFragment#newInstance} factory
 * method to create an instance of this fragment.
 */
public class PagedFragment extends Fragment {

    /**
     * Key string names used by newInstance() to store parameters in a bundle
     * that can be then accessed from the fragment once it has been created.
     */
    public static final String ARG_RESOURCE_URI = "resource_uri";
    public static final String ARG_POSITION = "position";

    /**
     * The data source uri to display in this fragment. This uri will depend on
     * the application context. The default implementation assumes that this uri
     * is to an image resource that needs to be downloaded.
     */
    protected Uri mUri;

    /**
     * The layout contains a single ImageView.
     */
    protected ImageView mImageView;

    /**
     * The position of this paged fragment in the parent activities adapter.
     */
    protected int mPosition;

    /**
     * A listen set by the calling activity so that it will be notified when the
     * initial background thread load image operation has completed and the
     * target image view has been updated with the resulting bitmap. Once this
     * has happened any postponed shared element transition can be started now
     * that the view is ready to animate.
     */
    protected OnPagedFragmentCallback mPagedFragmentListener;

    /**
     * Required empty public constructor for FragmentManager reconstruction.
     */
    public PagedFragment() {
    }

    /**
     * Use this factory method to create a new instance of this fragment using
     * the provided parameters.
     *
     * @param uri URL of image to display
     * @return A new instance of fragment PagedFragment.
     */
    public static PagedFragment newInstance(Uri uri, int position) {
        PagedFragment fragment = new PagedFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_RESOURCE_URI, uri);
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called when a fragment is first attached to its context. {@link
     * #onCreate(Bundle)} will be called after this.
     *
     * @param context The Activity context.
     */
    @Override
    public void onAttach(Context context) {
        if (context instanceof OnPagedFragmentCallback) {
            mPagedFragmentListener = (OnPagedFragmentCallback) context;
        }
        super.onAttach(context);
    }

    /**
     * Hook method called when a new instance of Activity is created. One time
     * initialization code goes here, e.g., UI layout initialization.
     *
     * @param savedInstanceState A Bundle object that contains saved state
     *                           information.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Un-bundle arguments and save as members.
        if (getArguments() != null) {
            mUri = getArguments().getParcelable(ARG_RESOURCE_URI);
            mPosition = getArguments().getInt(ARG_POSITION, -1);
        }
    }

    /**
     * Called after onCreate() to create the fragment view.
     *
     * @param inflater           Inflater used to inflate the fragment XML
     *                           layout.
     * @param container          The parent container.
     * @param savedInstanceState A previously saved state that can be used to
     *                           retrieve and restore the fragment state.
     * @return The inflated top level view.
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(
                R.layout.fragment_paged, container, false);

        // Set the default title.
        setTitle(getTitle());

        // Load the default image.
        loadImage(view, mUri);

        return view;
    }

    /**
     * Loads the default image view.
     *
     * @param view The content view.
     * @param uri  The uri passed as an argument to this fragment.
     */
    protected void loadImage(View view, Uri uri) {
        mImageView = (ImageView) view.findViewById(R.id.image_view);
        assert mImageView != null;
        ViewCompat.setTransitionName(mImageView, String.valueOf(mPosition));
        loadImage(uri);
    }

    /**
     * Image loading helper.
     *
     * @param uri The image URL to load (may be local or remote).
     */
    private void loadImage(Uri uri) {
        // Asynchronously load the bitmap.
        DownloadManager.with(getActivity())
                .load(uri)
                .listen(new RequestListener<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource) {
                        if (mPagedFragmentListener != null) {
                            mPagedFragmentListener.onSharedElementReady(
                                    mImageView, true);
                        }
                    }

                    @Override
                    public void onRequestFailed() {
                        if (mPagedFragmentListener != null) {
                            mPagedFragmentListener.onSharedElementReady(
                                    mImageView, false);
                        }
                    }
                })
                .into(mImageView);
    }

    /**
     * Image load callbacks are optional and can be installed using this
     * method.
     *
     * @param listener An OnPagedFragmentCallback implementation.
     */
    public void setOnPagedFragmentCallback(OnPagedFragmentCallback listener) {
        mPagedFragmentListener = listener;
    }

    /**
     * Called by pager adapter once this fragment has been constructed to set
     * the uri to display within this fragment.
     *
     * @param uri A uri string.
     */
    public void setUrl(Uri uri) {
        mUri = uri;
    }

    /**
     * Returns the default title which is the last path component of the source
     * uri .
     *
     * @return The fragment title.
     */
    public String getTitle() {
        String title = null;

        if (mUri != null) {
            title = UriUtils.getLastPathSegmentBaseName(mUri);
            if (TextUtils.isEmpty(title)) {
                title = getString(R.string.no_title);
            }
        }

        return title != null ? title : "";
    }

    /**
     * Default implementation forwards the set title to request to the activity
     * to handle. Custom fragments can override to display the title in some
     * other way.
     */
    public void setTitle(String string) {
        getActivity().setTitle(string);
    }

    /**
     * Listener callback interface to inform when an image load operation was
     * successful or failed.
     */
    public interface OnPagedFragmentCallback {
        void onSharedElementReady(ImageView view, boolean success);
    }
}

