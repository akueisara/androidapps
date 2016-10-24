package vandy.mooc.assignments.assignment.downloader;

import android.content.Context;

import vandy.mooc.assignments.framework.downloader.DownloadPolicy;
import vandy.mooc.assignments.framework.downloader.Downloader;
import vandy.mooc.assignments.framework.downloader.Request;

/**
 * A utility class (https://en.wikipedia.org/wiki/Utility_class) that uses the
 * factory pattern to construct and return a supported Downloader implementation.
 * DownloadPolicy contains an enumerated list of all supported downloader
 * implementations of the Download interface.
 */
@SuppressWarnings("WeakerAccess")
public final class DownloadFactory {
    /**
     * Utility classes should always be final and have a private constructor.
     */
    private DownloadFactory() {
    }

    /**
     * Uses the Factory pattern to construct and return the requested Downloader
     * implementation.
     *
     * @param policy The implementation policy to return.
     * @return An new instance of the specified downloader policy.
     */
    @SuppressWarnings("unchecked")
    public static Downloader getDownloader(
            DownloadPolicy policy,
            Context context,
            Request request) {
        /*
         * Simplified for this assignment, would normally be a switch statement based on which
         * version of the assignment as being ran.
         */
        Downloader downloader;
        downloader = new HaMeRDownloader();
        return downloader;
    }
}
