package vandy.mooc.assignments.framework.downloader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vandy.mooc.assignments.assignment.downloader.DownloadFactory;
import vandy.mooc.assignments.framework.application.DownloadApplication;
import vandy.mooc.assignments.framework.utils.CacheUtils;
import vandy.mooc.assignments.framework.utils.Preconditions;
import vandy.mooc.assignments.framework.utils.Utils;

/**
 * An download manager designed to use customizable cache, executor, and
 * download strategies. This class along with all the supporting classes and
 * interfaces follow the general design and patterns used by the open source
 * Picasso Image downloader (https://github.com/square/picasso) and
 * Bump Technologies Glide downloader (https://github.com/bumptech/glide).
 * Although this framework adaptation has been designed to provide a mechanism
 * to asynchronously download remote data sources of any data type, it also
 * provides special support for downloading images into ImageView UI targets
 * using a Fluent interface that matches both Picasso and Google's Glide. As a
 * result, any application that uses this framework for downloading images can
 * be easily modified to use either Picasso or Glide which provide more features
 * as well as efficient memory caching.
 * <p/>
 * Note: Although this framework was designed to provide hooks to support a
 * memory cache, the current implementation does not provide any memory caching
 * implementation.
 */
@SuppressWarnings("FieldCanBeLocal")
public class DownloadManager {
    /**
     * Debug logging tag.
     */
    private static final String TAG = "DownloadManager";
    /**
     * Default executor service thread pool size.
     */
    private static final int EXECUTOR_THREAD_COUNT = 4;
    /**
     * The singleton instance. The context stored in this singleton is the
     * application context and therefore will not cause a "StaticFieldLeak".
     */
    @SuppressLint("StaticFieldLeak")
    private static volatile DownloadManager singleton = null;
    /**
     * The executor service to support ThreadPoolDownloader policy.
     */
    public final ExecutorService mExecutor;
    /**
     * The application context.
     */
    private final Context mContext;
    /**
     * A memory caching implementation (for future versions).
     */
    @SuppressWarnings("unused")
    private final Cache mCache;

    /**
     * The default download policy (only to be used with DownloaderFactory).
     */
    private DownloadPolicy mDownloadPolicy;

    /**
     * The default memory policy (mask);
     */
    private int mMemoryPolicy;

    /**
     * The default network policy;
     */
    private int mNetworkPolicy;

    /**
     * Flag to indicate if debug logging is enabled.
     */
    private boolean mLogging;

    /**
     * The default Download class implementation to use when building a download
     * request.
     */
    private Class<?> mDownloaderClass;

    /**
     * Manages all request objects and ties their lifecycle to the lifecycle of
     * the activity context that is passed in by the application during request
     * creation (see with() method for more details).
     */
    private RequestManager mRequestManager;

    /**
     * Constructor.
     *
     * @param context        The application context.
     * @param cache          A cache implementation or null for the default.
     * @param downloadPolicy A downloading policy or null for the default.
     * @param executor       An executor service implementation or null for the
     *                       default.
     * @param logging        boolean for enabling or disabling debug logging
     *                       output.
     */
    @SuppressWarnings({"SameParameterValue", "WeakerAccess"})
    public DownloadManager(
            Context context,
            @Nullable Cache cache,
            @Nullable DownloadPolicy downloadPolicy,
            @Nullable ExecutorService executor,
            boolean logging) {
        // Replace optional parameters with defaults where required.
        if (cache == null) {
            cache = createDefaultCache();
        }

        if (downloadPolicy == null) {
            downloadPolicy = DownloadPolicy.AsyncTaskDownloader;
        }

        if (executor == null) {
            executor = createDefaultExecutor();
        }

        // Initialize finals.
        mContext = context;
        mLogging = logging;
        mCache = cache;
        mExecutor = executor;

        // Non-final default.
        mDownloadPolicy = downloadPolicy;

        // Create a request manager.
        mRequestManager = new RequestManager(mContext);
        mRequestManager.startup();
    }

    /**
     * Fluent interface method for creating/acquiring this singleton instance.
     * If this is the first call to this singleton, it will automatically create
     * itself using the application context.
     *
     * @return Download manager for Fluent interface builder pattern.
     */
    public static DownloadManager get() {
        if (singleton == null) {
            synchronized (DownloadManager.class) {
                if (singleton == null) {
                    singleton =
                            new DownloadManager(
                                    DownloadApplication.getContext(),
                                    null,
                                    null,
                                    null,
                                    true);
                }
            }
        }

        return singleton;
    }

    /**
     * Fluent interface method for creating a request that will be tightly bound
     * to the lifecycle of the passed activity.
     * <p/>
     * The request will track the lifecycle of this activity and if the context
     * is destroyed, the request will be automatically cancelled and all
     * resources associated with the request will be released.
     *
     * @param activity An activity.
     * @return A RequestCreator for Fluent interface builder pattern.
     */
    public static RequestCreator with(Activity activity) {
        return new RequestCreator(get(), activity);
    }

    /**
     * Sets this singleton to the passed instance.
     *
     * @param instance A DownloadManager created using the Builder helper.
     * @return The DownloadManger singleton instance.
     */
    @SuppressWarnings({"UnusedReturnValue", "unused"})
    public static DownloadManager setSingletonInstance(
            DownloadManager instance) {
        synchronized (DownloadManager.class) {
            // We allow resetting the singleton for assignments although
            // this would not be recommended in real production application.
            if (singleton != null) {
                Log.w(TAG, "Resetting DownloadManager singleton");
            }
            singleton = instance;
        }

        return singleton;
    }

    /**
     * Creates the default executor service used to execute and manage
     * background download threads. To use a custom executor service use the
     * DownloadManager constructor to pass in the custom ExecutorService as a
     * parameter and then call setSingletonInstance() passing in the created
     * DownloadManager.
     *
     * @return An ExecutorService implementation.
     */
    private static ExecutorService createDefaultExecutor() {
        return Executors.newFixedThreadPool(EXECUTOR_THREAD_COUNT);
    }

    /**
     * Creates the default cache used to save and manage data returned by the
     * Downloader implementation.
     * <p/>
     * NOTE: not currently implemented in this framework.
     *
     * @return A Cache implementation.
     */
    @SuppressWarnings("SameReturnValue")
    private static Cache createDefaultCache() {
        return null;
    }

    /**
     * Returns the application context used to construct this singleton.
     *
     * @return The application context.
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * Start an asynchronous download request. This DownloadManager is
     * responsible for constructing and starting the asynchronous download
     * request. The request itself manages the target, decoding, and callbacks.
     *
     * @param request A download request containing an application defined
     */
    void dispatch(Request request) {
        if (mDownloaderClass == null) {
            throw new IllegalStateException(
                    "No downloader class has been set.");
        }

        if (request.mTarget == null) {
            throw new IllegalArgumentException(
                    "Target download request requires a non-null target");
        }

        // There should never be a downloader instance once a request had been
        // cancelled so this is just a sanity check.
        if (request.getDownloader() != null) {
            throw new IllegalStateException(
                    "Unable to start request, a download is still pending");
        }

        // If the request does not have it's own policies, set them to the
        // application wide defaults.
        request.mNetworkPolicy = getNetworkPolicy(request);
        request.mMemoryPolicy = getMemoryPolicy(request);

        // All target implementations are required to manage a back reference
        // to their request object by implementing setRequest() and
        // getRequest().
        // In some cases a target implementation may choose to store the request
        // reference in an object that may be recycled. For example, the default
        // ViewTarget implementation stores the request as the default View tag.
        // If this view is recycled while a download request is still running,
        // then we need to either cancel that download, or, if the old target
        // request matches this new request, we can simply transfer the
        // downloader instance to the new request and then recycle the old
        // request.
        if (request.mTarget.getRequest() != null) {
            // Sanity check... back references should never be orphaned.
            Preconditions.checkState(
                    mRequestManager.hasRequest(request.mContext, request),
                    "dispatch: recycled target contains an orphaned request");

            // Check if the request download is still running and if so, just
            // transfer it to the new request and recycle the old request.
            Request oldRequest =
                    Preconditions.checkNotNull(request.mTarget.getRequest());
            request.mDownloader = oldRequest.mDownloader;
            oldRequest.recycle();
            Log.w(TAG, "dispatch: transferred downloader to new request");
        } else {
            // Create a new downloader that uses the specified download
            // policy and
            // attach it to the request.
            request.mDownloader = getDownloader(request);
        }

        // Store the request in the target so that we can determine at any
        // point if a target was recycled and has been assigned to a new
        // request.
        request.mTarget.setRequest(request);

        // Now that the request is ready to run, pass it to the request
        // manager to bind it to its context's lifecycle.
        mRequestManager.addRequest(request);

        // Finally, start the asynchronous download request. The downloader is
        // responsible for calling the request's download lifecycle hook
        // methods.
        request.start();
    }

    /**
     * Constructs a new downloader instance and attaches a request.
     *
     * @return A new downloader instance bound to the specified request.
     */
    private Downloader getDownloader(Request request) {
        try {
            Downloader downloader = (Downloader) mDownloaderClass.newInstance();

            // Note that there is no way to verify if the registered downloader
            // type matches request type.
            //noinspection unchecked
            downloader.setRequest(request);

            return downloader;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Constructs a new downloader using an application factory.
     *
     * @return A new downloader instance that matches the passed policy.
     */
    private Downloader getFactoryDownloader(
            DownloadPolicy policy,
            Request request) {
        return DownloadFactory.getDownloader(policy, mContext, request);
    }

    /**
     * Executes a synchronous download.
     *
     * @param request The download request.
     * @return The local cache URL of the downloaded data object.
     */
    public Uri execute(Request request) {
        Utils.assertBackgroundThread();
        return HttpDownloader.download(request);
    }

    /**
     * Changes the current download policy used for all subsequent download
     * operations.
     *
     * @param policy A download policy.
     */
    public void setDownloadPolicy(DownloadPolicy policy) {
        mDownloadPolicy = policy;
    }

    /**
     * Helper method that returns the request network policy (mMask) if set, or
     * the default application network policy if not set.
     *
     * @param request A load request.
     * @return The network policy to use for this request.
     */
    @SuppressWarnings("unused")
    private int getNetworkPolicy(Request request) {
        return request.getNetworkPolicy() != 0
               ? request.getNetworkPolicy() : mNetworkPolicy;
    }

    /**
     * Sets the network policies.
     *
     * @param policies The network policies to set.
     */
    @SuppressWarnings("unused")
    public void setNetworkPolicy(NetworkPolicy... policies) {
        // buildMask() will assert passed parameters are not valid.
        mNetworkPolicy = NetworkPolicy.buildMask(policies);
    }

    /**
     * Helper method that returns the request memory policy (mMask) if set, or
     * the default application memory policy if not set.
     *
     * @param request A load request.
     * @return The memory policy to use for this request.
     */
    @SuppressWarnings("unused")
    private int getMemoryPolicy(Request request) {
        return request.getMemoryPolicy() != 0
               ? request.getMemoryPolicy() : mMemoryPolicy;
    }

    /**
     * Sets the memory policies.
     *
     * @param policies The memory policies to set.
     */
    @SuppressWarnings("unused")
    public void setMemoryPolicy(MemoryPolicy... policies) {
        // buildMask() will assert passed parameters are not valid.
        mMemoryPolicy = MemoryPolicy.buildMask(policies);
    }

    /**
     * Helper method that returns the request download policy if set, or the
     * default application download policy if not set.
     *
     * @param request A load request.
     * @return The download policy to use for this request.
     */
    private DownloadPolicy getDownloadPolicy(Request request) {
        return request.getDownloadPolicy() != null
               ? request.getDownloadPolicy() : mDownloadPolicy;
    }

    /**
     * Convenience method that cancels a request that was specifically started
     * with into(ImageView imageView). Requests started with custom Targets are
     * required to handle their own cancel handling.
     *
     * @param imageView An image view that was used to request using the
     *                  into(ImageView imageView Fluent interface method.
     * @return {@code true} if a request was cancelled, {@code false} if no
     * request was running for this view.
     */
    public boolean cancelRequest(ImageView imageView) {
        Utils.assertMainThread();
        Request request = ImageViewTarget.getRequest(imageView);
        if (request != null) {
            // cancel the request.
            cancelRequest(request);
            return true;
        } else {
            // No request to cancel.
            return false;
        }
    }

    /**
     * Cancels the passed asynchronous download request. Since the
     * DownloadManager does not currently maintain a list of running requests
     * that needs to be updated, the cancel operation is simply delegate to the
     * request to perform.
     *
     * @param request The request to cancel.
     */
    public void cancelRequest(Request request) {
        Utils.assertMainThread();
        request.cancel();
    }

    /**
     * Terminates DownloadManger and closes the HttpDownloader response cache.
     * The current implementation does not maintain a list of running downloader
     * operations and therefore this is no mechanism that can be used to cancel
     * any currently running downloader operations.
     */
    public void shutdown() {
        // Shutdown the request manager (cancels all running requests).
        mRequestManager.shutdown();

        // Shutdown the executor service.
        if (mExecutor != null && !mExecutor.isShutdown()) {
            mExecutor.shutdownNow();
        }

        // Shutdown the HttpDownloader (closes the response cache).
        HttpDownloader.shutdown();

        // Clear this singleton.
        singleton = null;
    }

    /**
     * Returns debug output logging flag.
     *
     * @return {@code true} if logging should be displayed; {@code false} if
     * not.
     */
    public boolean isLoggingEnabled() {
        return mLogging;
    }

    /**
     * Enables or disables debug logging output.
     *
     * @param enable true to enable logging; {@code false} to disable logging.
     */
    @SuppressWarnings("unused")
    public void setLogging(boolean enable) {
        mLogging = enable;
    }

    /**
     * Called after a request has been cancelled to release the request back to
     * the quest pool (there currently is no request pool). The request is
     * simply forwarded to the request manager to process.
     *
     * @param request The request to recycle.
     */
    public void recycleRequest(Request request) {
        mRequestManager.recycleRequest(request);
    }

    /**
     * Installs the default downloader class to use for all requests.
     *
     * @param downloaderClass A Downloader interface implementation.
     */
    public void registerDownloader(
            Class<? extends Downloader> downloaderClass) {
        mDownloaderClass = downloaderClass;
    }

    /**
     * Clears all cached items marked with the specified tag.
     * @param tag A tag string
     */
    public static int clearCache(String tag) {
        int count = CacheUtils.clearTaggedFiles(get().mContext, tag);
        Log.d(TAG, "Cleared " + count + " files with tag " + tag);
        return count;
    }

    /**
     * All memory cache implementations must support this interface. (For future
     * versions)
     */
    @SuppressWarnings("unused")
    public interface Cache {
    }
}
