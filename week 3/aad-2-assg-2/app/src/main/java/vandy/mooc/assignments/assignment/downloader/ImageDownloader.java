package vandy.mooc.assignments.assignment.downloader;

import android.graphics.Bitmap;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;

import vandy.mooc.assignments.framework.downloader.AbstractDownloader;
import vandy.mooc.assignments.framework.downloader.HttpDownloader;

/**
 * An image downloader base class that can subclassed to create custom image
 * downloaders that use different asynchronous download mechanisms to retrieve
 * images from the web servers.
 * <p/>
 * This class provides a number of helper methods that can be called during the
 * lifecycle stages of an image download operation.
 */
public abstract class ImageDownloader extends AbstractDownloader<Bitmap> {
    /**
     * Calls the HttpDownloader helper method to download the request's URL data
     * source. The downloaded image file is then converted into a bitmap which
     * then returned to the caller.
     *
     * @return A Bitmap created from the downloaded image or null if the
     * download operation or bitmap creation fails.
     */
    @WorkerThread
    protected Bitmap download() {
        // Perform the download (or cache retrieval) and decoding in one step.
        return decode(HttpDownloader.download(getRequest()));
    }

    /**
     * Forwards bitmap to super class method to route to the application UI.
     * This method can only be called from the main thread.
     *
     * @param bitmap  The downloaded image as a decoded bitmap.
     */
    @MainThread
    protected void postResult(Bitmap bitmap) {
        super.postResult(bitmap);
    }
}
