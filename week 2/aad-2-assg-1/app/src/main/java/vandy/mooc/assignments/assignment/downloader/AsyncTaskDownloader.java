package vandy.mooc.assignments.assignment.downloader;

/**
 * THIS CLASS IS NOT USED FOR ASSIGNMENT 1.
 * </p>
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
     * Starts the asynchronous download request.
     */
    @Override
    public void execute() {

    }

    /**
     * Cancels the current download operation.
     */
    @Override
    public void cancel() {

    }

    /**
     * Reports if the task is currently running.
     *
     * @return {@code true} if the task is running; {@code false} if not.
     */
    @Override
    public boolean isRunning() {
        return false;
    }

    /**
     * Reports if the task has been cancelled.
     *
     * @return {@code true} if the task has cancelled ; {@code false} if not.
     */
    @Override
    public boolean isCancelled() {
        return false;
    }

    /**
     * Reports if the task has completed.
     *
     * @return {@code true} if the task has successfully completed; {@code
     * false} if not.
     */
    @Override
    public boolean hasCompleted() {
        return false;
    }


}
