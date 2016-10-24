package vandy.mooc.assignments.framework.downloader;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.URLUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicInteger;

import vandy.mooc.assignments.framework.utils.Preconditions;
import vandy.mooc.assignments.framework.utils.ResourceUtils;
import vandy.mooc.assignments.framework.utils.Utils;

/**
 * An resource load request that is created using the Fluent pattern through the
 * enclosed RequestCreator and Builder static classes. This class supports
 * download requests for any data type using the asynchronous fetch() method and
 * the synchronous get() method. The into() and size() methods can be used to
 * asynchronously download and resize an image directly into a provided image
 * view.
 *
 * @param <R> The resource type then will be decoded from the load operation.
 */
@SuppressWarnings("WeakerAccess")
public final class Request<R> {
    /**
     * Logging tag.
     */
    private static final String TAG = "Request";
    /**
     * Static atomic ID generator.
     */
    private static final AtomicInteger nextId = new AtomicInteger();
    /**
     * String uri query keys used to encode request attributes into a file name
     * that can be used for caching the request results.
     */
    private static final String URI_KEY = "uri";
    private static final String TAG_KEY = "tag";
    private static final String WIDTH_KEY = "width";
    private static final String HEIGHT_KEY = "height";
    /**
     * A unique request id.
     */
    public final int mRequestId;
    /**
     * The context which determines the lifespan of this request.
     */
    public final Context mContext;
    /**
     * The target URL to download.
     */
    public final Uri mUri;
    /**
     * A placeholder drawable resource used to display while the target URL is
     * being downloaded.
     */
    @DrawableRes
    public final int mPlaceholderId;
    /**
     * An error drawable resource used to display when the target URL download
     * fails.
     */
    @DrawableRes
    public final int mErrorId;
    /**
     * User defined tag that can be used to cancel requests or to clear cached
     * request results.
     */
    public final String mTag;
    /**
     * The download policy to use for this request.
     */
    public final DownloadPolicy mDownloadPolicy;
    /**
     * Support target dimension fields invoked by into() method. Target decoders
     * can use these dimensions when converting from the downloaded data input
     * source to the final resource result data type. For example, the
     * BitmapDecoder uses these values to down sample the decoded image to
     * minimize memory usage.
     */
    public final int mWidth;
    public final int mHeight;
    /**
     * Note that {@link RequestListener} param is a strong reference and will
     * prevent your {@link android.app.Activity} or {@link android.app.Fragment}
     * from being garbage collected until the request is completed.
     */
    public final RequestListener<R> mRequestListener;
    /**
     * The target for this request.
     */
    public final Target<R> mTarget;
    /**
     * Network and memory policies to override default disk and memory caching
     * policies. These values are bit masks of the enumerated constants defined
     * in MemoryPolicy and NetworkPolicy.
     */
    public int mNetworkPolicy;
    public int mMemoryPolicy;
    /**
     * A downloader instance that matches the download policy setting and is
     * typed by the resource type of this request.
     */
    public Downloader<R> mDownloader;
    /**
     * Keeps track of the lifecycle state of the request.
     */
    private RequestState mState;
    /**
     * Set when a download completes.
     */
    private Status mStatus;

    /**
     * Constructor that is only called by the Builder helper class.
     */
    protected Request(
            Context context,
            Uri uri,
            Target<R> target,
            @DrawableRes int placeholderId,
            @DrawableRes int errorId,
            int width,
            int height,
            int networkPolicy,
            int memoryPolicy,
            DownloadPolicy downloadPolicy,
            RequestListener<R> listener,
            String tag) {
        mContext = context;
        mUri = uri;
        mTarget = target;
        mPlaceholderId = placeholderId;
        mErrorId = errorId;
        mNetworkPolicy = networkPolicy;
        mMemoryPolicy = memoryPolicy;
        mDownloadPolicy = downloadPolicy;
        mWidth = width;
        mHeight = height;
        mRequestListener = listener;
        mRequestId = nextId.addAndGet(1);

        // To make code simpler, never allow a null tag.
        mTag = tag != null ? tag : "";

        // Set first state.
        mState = RequestState.CREATED;
    }

    /**
     * Returns the request tag that was encoded by buildEncodedUri().
     *
     * @param uri An encoded uri created by
     * @return The original request's tag.
     */
    public static String getDecodedTag(Uri uri) {
        return getQueryParameter(uri, TAG_KEY);
    }

    /**
     * Returns the request uri that was encoded by buildEncodedUri().
     *
     * @param uri An encoded uri created by buildEncodedUri().
     * @return The original request's uri.
     */
    public static Uri getDecodedUri(Uri uri) {
        try {
            return Uri.parse(
                    URLDecoder.decode(getQueryParameter(uri, URI_KEY),
                                      "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
     * Returns the request width that was encoded by buildEncodedUri().
     *
     * @param uri An encoded uri created by buildEncodedUri().
     * @return The original request's width.
     */
    public static int getDecodedWidth(Uri uri) {
        return Integer.valueOf(getQueryParameter(uri, WIDTH_KEY));
    }

    /**
     * Returns the request height that was encoded by buildEncodedUri().
     *
     * @param uri An encoded uri created by buildEncodedUri().
     * @return The original request's height.
     */
    public static int getDecodedHeight(Uri uri) {
        return Integer.valueOf(getQueryParameter(uri, HEIGHT_KEY));
    }

    /**
     * Returns the request tag that was encoded in the passed uri.
     *
     * @param uri An encoded uri created by buildEncodedUri().
     * @return The original request's tag.
     */
    private static String getQueryParameter(Uri uri, String key) {
        if (URLUtil.isValidUrl(uri.toString())) {
            // Get the path part as a uri so that we can use uri query.
            uri = Uri.parse(
                    Preconditions.checkNotNull(uri.getLastPathSegment()));
        }

        return uri.getQueryParameter(key);
    }

    /**
     * Checks if the this request is attached to the target. When a target is
     * recycled, the attached request will be replaced with a new request. If
     * this request is not the current request for the target, then this request
     * has expired and should not do anymore processing.
     *
     * @return {@code true} if this request has expired and processing should be
     * discontinued; {@code false} if the request is valid and processing should
     * continue.
     */
    public boolean hasExpired() {
        Utils.assertMainThread();
        return mTarget != null && !this.equals(mTarget.getRequest());
    }

    /**
     * Checks if the passed request was built with the parameters that were used
     * to build this request. If request parameters match, then a running
     * downloader can be shared or transferred between the two requests. Note
     * that the big assumption here is that
     *
     * @param request A request
     * @return {@code true} if this request was built with the same parameters
     * as the passed request, {@code false} if not.
     */
    public boolean matches(Request request) {
        return getKey().equals(request.getKey());
    }

    /**
     * Helper that returns a reference to the request callback. Since the
     * callback methods can only be invoked in the main thread, a check is made
     * to ensure that the caller is running in the main thread.
     *
     * @return A request callback if one was set; null otherwise.
     */
    @SuppressWarnings("unused")
    public RequestListener getRequestListener() {
        Utils.assertMainThread();
        return mRequestListener;
    }

    /**
     * Returns true if this request has been cancelled.
     *
     * @return {@code}true if the request has been cancelled, {@code}false if
     * not.
     */
    public boolean isCancelled() {
        return mStatus == Status.CANCELLED;
    }

    /**
     * Builds an request key that can be used to compare two requests to see if
     * were built using that same parameters (excluding targets and listeners).
     * These keys can then be compared to determine if both requests are
     * effectively performing the same resource acquisition.
     *
     * @return A key used for comparing request builder parameters (excluding
     * targets and listeners).
     */
    @SuppressWarnings("unused")
    public String getKey() {
        return mUri.toString()
                + "+"
                + mPlaceholderId
                + "+"
                + mErrorId
                + "+"
                + mNetworkPolicy
                + "+"
                + mMemoryPolicy
                + "+"
                + mDownloadPolicy
                + "+"
                + mWidth
                + "+"
                + mHeight
                + "+"
                + mTag;
    }

    /**
     * Encodes the uri, width, height, and tag attributes into a file name
     * string that can be used for caching.
     *
     * @return A file name that can be used to save the request resource.
     */
    public String toFileName() {
        try {
            Uri.Builder builder = new Uri.Builder();
            String encodedUri = URLEncoder.encode(mUri.toString(), "UTF-8");
            builder.appendQueryParameter("uri", encodedUri);
            builder.appendQueryParameter("tag", mTag);
            builder.appendQueryParameter("width", String.valueOf(mWidth));
            builder.appendQueryParameter("height", String.valueOf(mHeight));
            return builder.build().toString();
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unable to build a uri request");
            return null;
        }
    }

    /**
     * Returns true if the passed file uri matches this requests attributes.
     * This method can be called to determine if a specific cache file can be
     * used to satisfy this request.
     *
     * @param uri A absolute file uri or just the last path segment (file
     *            name).
     * @return {@code true} if the passed uri can be used to satsify this
     * request.
     */
    public boolean isMatch(Uri uri) {
        return uri.equals(Preconditions.checkNotNull(toFileName()));
    }

    /**
     * Contains most of the request parameters.
     */
    @Override
    public String toString() {
        return "Request{" +
                "mRequestId=" + mRequestId +
                ", mState=" + mState +
                ", mStatus=" + mStatus +
                ", mUri=" + mUri +
                ", mWidth=" + mWidth +
                ", mHeight=" + mHeight +
                ", mTag=" + mTag +
                ", mPlaceholderId=" + mPlaceholderId +
                ", mErrorId=" + mErrorId +
                ", mDownloadPolicy=" + mDownloadPolicy +
                ", mNetworkPolicy=" + mNetworkPolicy +
                ", mMemoryPolicy=" + mMemoryPolicy +
                ", mRequestListener=" + mRequestListener +
                ", mDownloader=" + mDownloader +
                ", mTarget=" + mTarget +
                ", mContext=" + mContext +
                '}';
    }

    /**
     * Contains a more readable set of request parameters for debugging.
     *
     * @return
     */
    public String toShortString() {
        return "Request{" +
                "mRequestId=" + mRequestId +
                ", mState=" + mState +
                ", mStatus=" + mStatus +
                ", mUri=" + mUri +
                '}';
    }

    public void start() {
        Preconditions.checkNotNull(mDownloader,
                                   "Unable to start a request; no downloader "
                                           + "set");
        Preconditions.checkNotNull(mTarget,
                                   "Unable to start a request; no target set");
        Preconditions.checkState(isState(RequestState.CREATED),
                                 "Request can only be started once");

        setState(RequestState.STARTED);

        // Inform target that load operation is starting.
        mTarget.onLoadStarted(
                ResourceUtils.getResourceDrawable(mContext,
                                                  mPlaceholderId));

        // Execute the load operation. The downloader maintains a reference
        // to this request which it used to make numerous loading lifecycle
        // callbacks.
        mDownloader.execute();

        setState(RequestState.RUNNING);
    }

    /**
     * Cancel the currently running request and free any resources associated
     * with this request. The request will no longer be valid after this call.
     */
    public void cancel() {
        if (mDownloader != null) {
            Log.d(TAG, "Request attempting to cancel a download");

            // Immediately unhook the request back reference.
            mDownloader.setRequest(null);
            if (mDownloader.isRunning()) {
                mDownloader.cancel();
            }
            mDownloader = null;
        }
        setState(RequestState.FINISHED);
        setStatus(Status.CANCELLED);
        recycle();
    }

    /**
     * Must be called once a request has finished to clear all resources. Note
     * the downloader must be either finished or explicitly cancelled and then
     * cleared before calling this method. Also, the recycle() path should only
     * call other object recycle() (not cancel()). Each object is responsible
     * for calling any contained objects recycle methods. All recycle calls must
     * only go down the descendant hierarchy and never move up the ancestor
     * chain.
     */
    public void recycle() {
        if (mState == RequestState.RECYCLED) {
            Log.w(TAG, "recycle: request has already been recycled");
            return;
        }

        Preconditions.checkState(mDownloader == null,
                                 "recycle: downloader not properly terminated");

        // Now that we know we own this target (if one was set) we tell it to
        // release its resources.
        if (mTarget != null) {
            mTarget.recycle();
        }

        // Remove the request from the request manager's <context|requests> map.
        DownloadManager.get().recycleRequest(this);

        // Record this request as available to be reused.
        setState(RequestState.RECYCLED);
        setStatus(null);
    }

    /**
     * A started request always has a downloader.
     *
     * @return {@code}true if the request has started, {@code}false if not.
     */
    public boolean hasStarted() {
        return mDownloader != null;
    }

    /**
     * Returns the downloader for this request
     *
     * @return Downloader implementation.
     */
    public Downloader<R> getDownloader() {
        return mDownloader;
    }

    /**
     * Returns the downloader for this request
     *
     * @return Downloader implementation.
     */
    public Decoder getResourceDecoder() {
        return mTarget.getResourceDecoder();
    }

    /**
     * @return The request memory policy or 0 if not set.
     */
    public int getMemoryPolicy() {
        return mMemoryPolicy;
    }

    /**
     * @return The request network policy or 0 if not set.
     */
    public int getNetworkPolicy() {
        return mNetworkPolicy;
    }

    /**
     * @return The request download policy or null if not set.
     */
    @Nullable
    public DownloadPolicy getDownloadPolicy() {
        return mDownloadPolicy;
    }

    /**
     * Called after a download has completed to decode the cached file data into
     * the target resource data type. The decoder is obtained from the target.
     *
     * @return A decoded data object or null if the decoding fails.
     */
    public R decodeResource(Uri uri) {
        @SuppressWarnings("unchecked")
        Decoder<R> decoder = Preconditions.checkNotNull(getResourceDecoder());
        return decoder.decode(uri, mWidth, mHeight);
    }

    /*
     * State and status helper methods.
     */

    /**
     * Returns the current lifecycle state of this request.
     */
    public RequestState getState() {
        return mState;
    }

    /**
     * Sets the current state for this request and also ensures that the request
     * is moving between legal states. An IllegalStateException will thrown if
     * the state cannot be set.
     *
     * @param state The state to move to.
     */
    public void setState(RequestState state) {
        switch (state) {
            case CREATED:
                if (mState == null || mState == RequestState.RECYCLED) {
                    mState = state;
                    return;
                }
                break;

            case STARTED:
                if (mState == RequestState.CREATED) {
                    mState = state;
                    return;
                }
                break;

            case RUNNING:
                if (mState == RequestState.STARTED
                        || mState == RequestState.PAUSED) {
                    mState = state;
                    return;
                }
                break;

            case PAUSED:
                if (mState == RequestState.RUNNING) {
                    mState = state;
                    return;
                }
                break;

            case FINISHED:
                // Synchronous requests will move from STARTED to FINISHED
                // while asynchronous requests will move from STARTED to
                // RUNNING to FINISHED.
                if (mState == RequestState.STARTED
                        || mState == RequestState.RUNNING) {
                    mState = state;
                    return;
                }
                break;
            case RECYCLED:
                // A request should only be recycled if it has finished and has
                // a finish status of SUCCESS, FAILED, CANCELLED, or EXPIRED.
                if (mState == RequestState.FINISHED) {
                    mState = state;
                    return;
                }
                break;
        }

        throw new IllegalStateException(
                "setState: " + mState + " -> " + state + " is not supported");
    }

    /**
     * Checks if the request is currently in one of the passed states.
     *
     * @param states A list of states to check.
     * @return {@code true} if the current state matches any state in the passed
     * list, {@code false} if not.
     */
    private boolean isState(RequestState... states) {
        for (final RequestState state : states) {
            if (mState.equals(state)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Throws and IllegalStateException if the current state is not one of the
     * passed states.
     *
     * @param states A list of states to check.
     */
    private void assertState(RequestState... states) {
        if (isState(states)) {
            return;
        }

        throw new IllegalStateException(
                "Current state " + mState + " not in passed states");
    }

    /**
     * Called when a load operation has completed and the requested data has
     * been successfully decoded into the required resource data type.
     *
     * @param resource The resource result.
     */
    @MainThread
    public void onResourceReady(R resource) {
        Utils.assertMainThread();

        Preconditions.checkNotNull(
                resource, "Attempt to set a null resource");
        Preconditions.checkNotNull(
                mTarget, "Attempt to set resource on a request with no target");
        Preconditions.checkNotNull(
                hasExpired(), "Attempt to set resource on a recycled target");

        setState(RequestState.FINISHED);
        setStatus(Status.SUCCEEDED);

        // If the target has not expired (the application object that it may be
        // wrapping may have been GC'd) then redirect the event to the target.
        if (!mTarget.hasExpired()) {
            mTarget.onResourceReady(resource);
        }

        // Optional request listeners never expire, so always redirect event to
        // a registered listener.
        if (mRequestListener != null) {
            mRequestListener.onResourceReady(resource);
        }

        // Downloader must be explicitly detached before recycling.
        mDownloader = null;

        // Always call recycle to ensure that all attached implementations
        // get a chance to recycle any of their resources.
        recycle();
    }

    /**
     * Called when a load operation has failed and simply forwards this event to
     * the target.
     */
    @MainThread
    public void onLoadFailed() {
        Utils.assertMainThread();

        Preconditions.checkNotNull(
                mTarget, "Load failed for a null target");
        Preconditions.checkNotNull(
                hasExpired(), "Load failed for a recycled target");

        setState(RequestState.FINISHED);
        setStatus(Status.FAILED);

        // If the target has not expired (the application object that it may be
        // wrapping may have been GC'd) then redirect the event to the target.
        if (!mTarget.hasExpired()) {
            mTarget.onLoadFailed(
                    ResourceUtils.getResourceDrawable(mContext, mErrorId));
        }

        // Optional request listeners never expire, so always redirect event to
        // a registered listener.
        if (mRequestListener != null) {
            mRequestListener.onRequestFailed();
        }

        // Downloader must be explicitly detached before recycling.
        mDownloader = null;

        // Always call recycle to ensure that all attached implementations
        // get a chance to recycle any of their resources.
        recycle();
    }

    /**
     * Determines if a download is progress.
     *
     * @return {@code}true if download is running; {@code}false otherwise.
     */
    @SuppressWarnings("unused")
    public boolean isRunning() {
        return mDownloader != null && mDownloader.isRunning();
    }

    public boolean isPaused() {
        return isState(RequestState.PAUSED);
    }

    public void setStatus(Status status) {
        Preconditions.checkState(
                status == null || mState == RequestState.FINISHED,
                "Status can not be set when request state is " + mState);
        mStatus = status;
    }

    /**
     * The status of a FINISHED request.
     */
    public enum Status {
        SUCCEEDED,
        FAILED,
        CANCELLED,
        EXPIRED
    }

    /**
     * A Builder class for constructing an request.
     */
    @SuppressWarnings("unused")
    public static class Builder<R> {
        Context mContext;
        @DrawableRes
        int mErrorId;
        private Uri mUri;
        private RequestListener mRequestListener;
        private int mWidth;
        private int mHeight;
        private Target<R> mTarget;
        private int mNetworkPolicy;
        private int mMemoryPolicy;
        private DownloadPolicy mDownloadPolicy;
        @DrawableRes
        private int mPlaceholderId;
        private String mTag;

        public Builder(Context context, Uri uri) {
            mContext = context;
            mUri = uri;
        }

        /**
         * Sets a request listen.
         *
         * @param listener A RequestListener interface.
         * @return Fluent Builder instance.
         */
        public Builder setListener(RequestListener listener) {
            if (mRequestListener != null) {
                throw new IllegalStateException(
                        "A listen has already been set.");
            }
            mRequestListener = listener;
            return this;
        }

        /**
         * Sets the target for download request.
         *
         * @param target A target that will be automatically loaded with a
         *               downloaded resource.
         * @return Fluent Builder instance.
         */
        public Builder setTarget(Target<R> target) {
            if (mTarget != null) {
                throw new IllegalStateException(
                        "An target has already been set.");
            }
            mTarget = target;
            return this;
        }

        /**
         * Sets the resize width of the bitmap generated for a download image
         * request.
         *
         * @param width The resize width used when sampling the bitmap.
         * @return Fluent Builder instance.
         */
        public Builder setWidth(int width) {
            if (mWidth != 0) {
                throw new IllegalStateException(
                        "A width has already been set.");
            }
            mWidth = width;
            return this;
        }

        /**
         * Sets the resize height of the bitmap generated for a download image
         * request.
         *
         * @param height The resize height used when sampling the bitmap.
         * @return Fluent Builder instance.
         */
        public Builder setHeight(int height) {
            if (mHeight != 0) {
                throw new IllegalStateException(
                        "A height has already been set.");
            }
            mHeight = height;
            return this;
        }

        /**
         * Sets the placeholder drawable resource id to display while download
         * request is being processed.
         *
         * @param placeholderId An application image resource id to display in
         *                      the target while the downloading is in
         *                      progress.
         * @return Fluent Builder instance.
         */
        public Builder setPlaceholder(int placeholderId) {
            if (mPlaceholderId != 0) {
                throw new IllegalStateException(
                        "A placeholder id has already been set.");
            }
            mPlaceholderId = placeholderId;
            return this;
        }

        /**
         * Sets the error image drawable resource id.
         *
         * @param errorId An application image resource id to display in the
         *                target when a download fails.
         * @return Fluent Builder instance.
         */
        public Builder setError(@DrawableRes int errorId) {
            if (mErrorId != 0) {
                throw new IllegalStateException(
                        "An errorId has already been set.");
            }
            mErrorId = errorId;
            return this;
        }

        /**
         * Sets the network policies.
         *
         * @param policies The policies to set.
         * @return Fluent Builder instance.
         */
        public Builder setNetworkPolicy(NetworkPolicy... policies) {
            // buildMask() will assert passed parameters are not valid.
            mNetworkPolicy = NetworkPolicy.buildMask(policies);
            return this;
        }

        /**
         * Sets the memory policies.
         *
         * @param policies The policies to set.
         * @return Fluent Builder instance.
         */
        public Builder setMemoryPolicy(MemoryPolicy... policies) {
            // buildMask() will assert passed parameters are not valid.
            mMemoryPolicy = MemoryPolicy.buildMask(policies);
            return this;
        }

        /**
         * Sets the download policy.
         *
         * @param policy The policy to set.
         * @return Fluent Builder instance.
         */
        public Builder setDownloadPolicy(DownloadPolicy policy) {
            if (policy == null) {
                throw new IllegalArgumentException(
                        "downloadPolicy cannot be null.");
            }

            if (mDownloadPolicy != null) {
                throw new IllegalStateException(
                        "A download policy has already been set.");
            }

            mDownloadPolicy = policy;

            return this;
        }

        /**
         * Sets request tag.
         *
         * @param tag Any string.
         */
        public void setTag(String tag) {
            mTag = tag;
        }

        /**
         * Builds an download request from the Builder fields.
         *
         * @return A request.
         */
        @SuppressWarnings("unchecked")
        public Request<R> build() {
            return new Request(mContext,
                               mUri,
                               mTarget,
                               mPlaceholderId,
                               mErrorId,
                               mWidth,
                               mHeight,
                               mNetworkPolicy,
                               mMemoryPolicy,
                               mDownloadPolicy,
                               mRequestListener,
                               mTag);
        }
    }
}
