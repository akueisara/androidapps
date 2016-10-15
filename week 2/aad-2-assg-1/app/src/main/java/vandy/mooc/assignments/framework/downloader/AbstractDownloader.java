package vandy.mooc.assignments.framework.downloader;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;
import android.util.Log;

import vandy.mooc.assignments.framework.utils.Utils;

/**
 * An abstract class that can be used as a common base for any Downloader
 * implementation. Since all Downloader implementations use a common
 * constructor that initializes a common set of final fields, this abstract
 * class consolidates those common features in a single place as well as
 * providing a set of helper methods that will redirect download lifecycle
 * events to the controlling request object.
 */
public abstract class AbstractDownloader<R> implements Downloader<R> {
    /**
     * Debug logging tag.
     */
    private static final String TAG = "AbstractDownloader";

    /**
     * The download request that is being processed.
     */
    private Request<R> mRequest;

    /**
     * Constructor that supports newInstance() creation (assignments only).
     */
    protected AbstractDownloader() {
    }

    /**
     * Helper that returns the download request.
     *
     * @return The download request object.
     */
    @Override
    public Request<R> getRequest() {
        return mRequest;
    }

    /**
     * Sets the request and context for this download handler.
     *
     * @param request The request associated with this download operation.
     */
    public void setRequest(Request<R> request) {
        mRequest = request;
    }

    /**
     * Helper that returns the context for this download.
     *
     * @return The download context.
     */
    public Context getContext() {
        return mRequest.mContext;
    }

    /**
     * Calls the HttpDownloader helper method to download the request's URL data
     * source. The request networkPolicy settings will determine whether the
     * Http response cache will be used. The downloaded image file is then used
     * to load a bitmap which is returned to the caller.
     *
     * @return A Bitmap created from the downloaded image or null if the
     * download operation or bitmap creation fails.
     */
    @WorkerThread
    protected R download() {
        Utils.assertBackgroundThread();

        // Perform the download (or cache retrieval) and decode the result
        // into the target resource data type.
        return decode(HttpDownloader.download(mRequest));
    }

    /**
     * Helper method that forwards resource decode operation to the request.
     * @param uri The source file uri.
     * @return The target resource or null if the decoding failed or the
     * request has expired.
     */
    @WorkerThread
    @Override
    public final R decode(Uri uri) {
        return uri != null && mRequest != null
                ? mRequest.decodeResource(uri)
                : null;
    }

    /**
     * Helper that loads the passed resource into the request target.
     * This method can only be called from the main thread.
     *
     * @param resource The resource to load.
     */
    @MainThread
    @SuppressWarnings("UnusedReturnValue")
    protected void postResult(R resource) {
        if (mRequest != null) {
            if (resource != null) {
                mRequest.onResourceReady(resource);
            } else {
                mRequest.onLoadFailed();
            }
        } else {
            Log.w(TAG, "Orphaned downloader attempting to set resource");
        }
    }
}
