package vandy.mooc.assignments.framework.downloader;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import vandy.mooc.assignments.framework.utils.Utils;

/**
 * A wrapper class around Builder to provide a more Fluent API. The methods
 * fetch(), get(), and into() automatically invoke the contained Builder's
 * build() method to construct the Request.
 */
@SuppressWarnings("unused")
public class RequestCreator<R> {
    /**
     * DownloadManger instance.
     */
    private final DownloadManager mManager;

    /**
     * The context for the request.
     */
    private final Context mContext;

    /**
     * Builder helper used to build the Request.
     */
    private Request.Builder mBuilder;

    /**
     * Constructor.
     *
     * @param manager A DownloadManger instance.
     * @param context A context for this request.
     */
    public RequestCreator(@NonNull DownloadManager manager, Context context) {
        mManager = manager;
        mContext = context;
    }

    /**
     * Sets the uri for this request.
     *
     * @param uri A URL download target.
     * @return Fluent RequestCreator instance.
     */
    public RequestCreator load(@NonNull Uri uri) {
        if (mBuilder != null) {
            throw new IllegalStateException("A uri has already been set");
        }
        mBuilder = new Request.Builder(mContext, uri);
        return this;
    }

    /**
     * Sets the network policies.
     *
     * @param policy A list of network policies
     * @return Fluent RequestCreator instance.
     */
    public RequestCreator networkPolicy(@NonNull NetworkPolicy... policy) {
        mBuilder.setNetworkPolicy(policy);
        return this;
    }

    /**
     * Sets the memory policies.
     *
     * @param policies The memory policies to set.
     * @return Fluent RequestCreator instance.
     */
    public RequestCreator setMemoryPolicy(@NonNull MemoryPolicy... policies) {
        mBuilder.setMemoryPolicy(policies);
        return this;
    }

    /**
     * Sets placeholder drawable.
     *
     * @param placeholderId The placeholder drawable resource id.
     * @return Fluent RequestCreator instance.
     */
    @SuppressWarnings("SameParameterValue")
    public RequestCreator placeholder(@DrawableRes int placeholderId) {
        mBuilder.setPlaceholder(placeholderId);
        return this;
    }

    /**
     * Sets the error drawable.
     *
     * @param errorId The error drawable resource id.
     * @return Fluent RequestCreator instance.
     */
    @SuppressWarnings("SameParameterValue")
    public RequestCreator error(@DrawableRes int errorId) {
        mBuilder.setError(errorId);
        return this;
    }

    /**
     * Sets the resize dimensions used when constructing a Bitmap.
     *
     * @param width  Bitmap width.
     * @param height Bitmap height.
     * @return Fluent RequestCreator instance.
     */
    @SuppressWarnings("SameParameterValue")
    public RequestCreator resize(int width, int height) {
        mBuilder.setWidth(width);
        mBuilder.setHeight(height);
        return this;
    }

    /**
     *
     * Sets request tag.
     *
     * @param tag Any string.
     * @return Fluent RequestCreator instance.
     */
    public RequestCreator tag(String tag) {
        mBuilder.setTag(tag);
        return this;
    }

    /**
     * Sets the download policy.
     *
     * @param policy The download policies to set.
     * @return Fluent RequestCreator instance.
     */
    public RequestCreator download(DownloadPolicy policy) {
        mBuilder.setDownloadPolicy(policy);
        return this;
    }

    /**
     * Sets an optional listen that will be called when the resource is ready or
     * when the load operation fails.
     *
     * @param listener The listen to install.
     * @return Fluent RequestCreator instance.
     */
    public RequestCreator listen(RequestListener listener) {
        mBuilder.setListener(listener);
        return this;
    }

    /**
     * Constructs the request and dispatches it to the DownloadManager which
     * downloads the target data asynchronous background thread.
     *
     * @param listener RequestListener implementation invoked when download
     *                 completes or fails.
     */
    public void fetch(RequestListener listener) {
        mBuilder.setListener(listener);
        Request request = mBuilder.build();
        mManager.dispatch(request);
    }

    /**
     * Constructs the request and synchronously executes the download operation
     * in the current thread. Note that this call must be made from a background
     * thread.
     */
    public Uri get() {
        Utils.assertBackgroundThread();
        Request request = mBuilder.build();
        return mManager.execute(request);
    }

    /**
     * Convenience method that constructs an download request along with a
     * default ImageViewTarget handler to wrap the passed ImageView.
     *
     * @param imageView An image view target.
     */
    public void into(@NonNull ImageView imageView) {
        Utils.assertMainThread();

        // Construct a default ImageViewTarget and pass it to the default
        // into() handler.
        //noinspection unchecked
        into(new ImageViewTarget(imageView));
    }

    /**
     * Constructs the request and dispatches it to the DownloadManager which
     * asynchronously downloads and encodes the resource in a background thread
     * and then loads the resource into the target.
     *
     * @param target The target into which the downloaded resource is loaded.
     */
    public void into(@NonNull Target target) {
        Utils.assertMainThread();

        // Set the target.
        //noinspection unchecked
        mBuilder.setTarget(target);

        // Build the request.
        Request request = mBuilder.build();

        // Dispatch the download request to the DownloadManager.
        mManager.dispatch(request);
    }
}
