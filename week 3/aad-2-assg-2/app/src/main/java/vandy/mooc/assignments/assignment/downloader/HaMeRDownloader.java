package vandy.mooc.assignments.assignment.downloader;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * A HaMeR downloader implementation that uses two Runnables and a background
 * thread to download a single image in a background thread.
 * <p/>
 * The base ImageDownloader class provides helper methods to perform the
 * download operation as well as to return the resulting image bitmap to the
 * framework where it will be displayed in a layout ImageView.
 */
public class HaMeRDownloader extends ImageDownloader {
    /**
     * Logging tag.
     */
    private static final String TAG = "HaMeRDownloader";

    /**
     * Create a new handler that is associated with the main thread looper.
     */
    // Create a private final Handler associated with the main thread looper.
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * A reference to the background thread to support the cancel hook.
     */
    private Thread mThread;

    private boolean finish = false;

    /**
     * Starts the asynchronous download request.
     */
    @Override
    public void execute() {
        // Create a new runnable called 'downloadRunnable' to process the download request.
        // Within the new runnable:
        Runnable downloadRunnable = new Runnable () {
            @Override
            public void run() {
                finish = false;
                // Make sure that this thread has not been interupted.
                if(isCancelled()) {
                    return;
                }
                // Call Downloader abstract class helper to perform the request download and decoding into the required resource type.
                final Bitmap image = download();

                // Make sure (again) that this thread has not been interupted.
                if(isCancelled()) {
                    return;
                }

                // Create a new runnable that calls setResource() and post it to be run on the main thread.
                final Runnable setResource = new Runnable() {
                    // Call the super class setResource helper method to set the ImageView bitmap and to make any callbacks needed.
                    @Override
                    public void run() {
                        postResult(image);
                    }
                };

                mHandler.post(setResource);
            }
        };
        mThread = new Thread(downloadRunnable);
        // Create and start an anonymous Thread to execute 'downloadRunnable'.
        mThread.start();
        finish = true;
    }

    /**
     * Cancels the current download operation.
     */
    @Override
    public void cancel() {
        // If the download thread is alive and running, cancel it by invoking an interrupt.
        if(isRunning()) {
            mThread.interrupt();
        }
    }

    /**
     * Reports if the task is currently running.
     *
     * @return {@code true} if the task is running; {@code false} if not.
     */
    @Override
    public boolean isRunning() {
        // Return 'true' if mThread is running.
        return mThread != null && mThread.isAlive();
    }

    /**
     * Reports if the task has been cancelled.
     *
     * @return {@code true} if the task has cancelled ; {@code false} if not.
     */
    @Override
    public boolean isCancelled() {
        // Return 'true' if mThread has been cancelled.
        return !mThread.isAlive();
    }

    /**
     * Reports if the task has completed.
     *
     * @return {@code true} if the task has successfully completed; {@code
     * false} if not.
     */
    @Override
    public boolean hasCompleted() {
        // Return 'true' if mThread has sucessfully completed.
        return finish;
    }
}
