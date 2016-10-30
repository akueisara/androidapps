package vandy.mooc.assignments.framework.downloader;

/**
 * Download strategies supported by DownloadFactory.
 */
@SuppressWarnings({"unused", "PointlessBitwiseExpression"})
public enum DownloadPolicy {
    /**
     * Used to construct a HaMeR download handler.
     */
    HaMeRDownloader,

    /**
     * Used to construct an AsyncTask download handler.
     */
    AsyncTaskDownloader,

    /**
     * Used to construct a ThreadPoolExecutor download handler.
     */
    ThreadPoolExecutorDownloader,

    /**
     * Used to construct a Picasso download handler.
     */
    PicassoDownloader,

    /**
     * Used to construct a Glide download handler.
     */
    GlideDownloader
}
