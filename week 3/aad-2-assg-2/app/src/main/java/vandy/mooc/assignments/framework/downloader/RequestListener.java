package vandy.mooc.assignments.framework.downloader;

/**
 * RequestListener interface that can be registered with each Request
 * using the listen() method.
 */
public interface RequestListener<R> {
    /**
     * Called when download succeeded.
     */
    void onResourceReady(R resource);

    /**
     * Called when download failed.
     */
    void onRequestFailed();
}
