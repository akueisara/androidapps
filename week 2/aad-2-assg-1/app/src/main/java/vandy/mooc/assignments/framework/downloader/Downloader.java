package vandy.mooc.assignments.framework.downloader;

import android.net.Uri;

/**
 * All downloader implementations must support this interface.
 */
public interface Downloader<R> {
    /**
     * Starts an asynchronous download.
     */
    void execute();

    /**
     * Handles the decoding phase of a download request.
     * @param url The downloaded (or cached) file uri.
     * @return The target decoded resource.
     */
    R decode(Uri url);

    /**
     * Cancels the asynchronous download.
     */
    void cancel();

    /**
     * Sets the request and context for this download handler.
     *
     * @param request The request associated with this download operation.
     */
    void setRequest(Request<R> request);

    /**
     * Required by framework for cancelling a request.
     */
    Request<R> getRequest();

    /**
     * Returns the running status of the download operation.
     *
     * @return {@code true} if the download is still running; {@code false} if it has
     * completed or has been cancelled.
     */
    boolean isRunning();

    /**
     * Returns the cancelled status of the download operation.
     *
     * @return {@code true} if the download has been cancelled; {@code false} if not.
     */
    boolean isCancelled();

    /**
     * Returns the completed status of the download operation.
     *
     * @return {@code true} if the download has successfully completed; {@code false} if
     * not.
     */
    boolean hasCompleted();
}
