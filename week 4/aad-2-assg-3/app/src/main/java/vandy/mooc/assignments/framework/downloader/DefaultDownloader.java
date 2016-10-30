package vandy.mooc.assignments.framework.downloader;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.Future;

/**
 * A downloader implementation that uses a fixed thread pools executor to
 * download a single image in a background thread.
 * <p/>
 * The base ImageDownloader class provides helper methods to perform the
 * download operation as well as to return the resulting image bitmap to the
 * framework where it will be displayed in a layout ImageView.
 */
public class DefaultDownloader<R> extends AbstractDownloader<R> {
    /**
     * Logging tag.
     */
    private static final String TAG = "DefaultDownloader";
    /**
     * Create a new handler that is linked to the main thread looper.
     */
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * A reference to the background thread Future to support the cancel hook.
     */
    private Future<?> mFuture;

    /**
     * Starts the asynchronous download request.
     */
    @Override
    public void execute() {
        // Create a new DownloadRunnable and set its future to the result
        // of the asynchronously submitted command.
        mFuture = DownloadManager.get().mExecutor.submit(
                new Runnable() {
                    @Override
                    public void run() {
                        final R resource = download();

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                postResult(resource);
                            }
                        });
                    }
                });
    }

    /**
     * Cancels the current download operation.
     */
    @Override
    public void cancel() {
        // If the download thread is alive and running, cancel it by
        // invoking an interrupt.
        if (mFuture != null && !mFuture.isCancelled()) {
            Log.d(TAG, "Cancelling download");
            mFuture.cancel(true);
        }
    }

    /**
     * Reports if the task is currently running.
     *
     * @return {@code true} if the task is running; {@code false} if not.
     */
    @Override
    public boolean isRunning() {
        return mFuture != null && !mFuture.isDone();
    }

    /**
     * Reports if the task has been cancelled.
     *
     * @return {@code true} if the task has cancelled ; {@code false} if not.
     */
    @Override
    public boolean isCancelled() {
        return mFuture != null && mFuture.isCancelled();
    }

    /**
     * Reports if the task has completed.
     *
     * @return {@code true} if the task has successfully completed; {@code
     * false} if not.
     */
    @Override
    public boolean hasCompleted() {
        return mFuture != null && mFuture.isDone();
    }
}
