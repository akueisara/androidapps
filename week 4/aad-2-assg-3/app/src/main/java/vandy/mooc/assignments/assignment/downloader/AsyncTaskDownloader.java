package vandy.mooc.assignments.assignment.downloader;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import static android.R.attr.bitmap;

/**
 * A AsyncTask downloader implementation that uses an AsyncTask to download a
 * single image in a background thread.
 * <p/>
 * The base ImageDownloader class provides helper methods to perform the
 * download operation as well as to return the resulting image bitmap to the
 * framework where it will be displayed in a layout ImageView.
 */
public class AsyncTaskDownloader extends ImageDownloader {
    /**
     * Logging tag.
     */
    private static final String TAG = "AsyncTaskDownloader";

    /**
     * A reference to the background task to support the cancel hook.
     */
    // Create a private AsyncTask called 'mTask'.
    private AsyncTask<Void, Void, Bitmap> mTask;

    /**
     * Starts the asynchronous download request.
     */
    @Override
    public void execute() {
        //  Initialize mTask.
        mTask = new AsyncTask<Void, Void, Bitmap>() {
            //  In the background: Call abstract class helper method to perform the download request and decode the resource.
            @Override
            protected Bitmap doInBackground(Void... params) {
                return download();
            }
            //  After downloading is complete: Call the super class setResource helper method to set the resource.
            //  The helper will also display and error bitmap if the passed bitmap is null (signalling a failed download).
            @Override
            protected void onPostExecute(Bitmap image) {
                if(image != null)
                    postResult(image);
                else
                    postResult(null);

            }
        };

        //  run mTask.
        mTask.execute();
    }

    /**
     * Cancels the current download operation.
     */
    @Override
    public void cancel() {
        // If the download thread is alive and running, cancel it by
        // invoking an interrupt.
        mTask.cancel(isRunning());
    }

    /**
     * Reports if the task is currently running.
     *
     * @return {@code true} if the task is running; {@code false} if not.
     */
    @Override
    public boolean isRunning() {
        // Return 'true' if mTask is currently running.
       return mTask.getStatus() == AsyncTask.Status.RUNNING;
    }

    /**
     * Reports if the task has been cancelled.
     *
     * @return {@code true} if the task has cancelled ; {@code false} if not.
     */
    @Override
    public boolean isCancelled() {
        // Return 'true' if mTask has been cancelled.
       return mTask.isCancelled();
    }

    /**
     * Reports if the task has completed.
     *
     * @return {@code true} if the task has successfully completed; {@code
     * false} if not.
     */
    @Override
    public boolean hasCompleted() {
        // Return 'true' if mTask has finished running.
        return mTask.getStatus() == AsyncTask.Status.FINISHED;
    }
}
