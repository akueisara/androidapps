package vandy.mooc.assignments.framework.downloader;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

/**
 * In interface that defines the behaviour of a generic target of a download
 * request. The target is responsible for displaying UI feedback during the
 * lifecycle of the download and encoding operation.
 *
 * @param <R> The type of resource type that can be displayed by this target,
 *            (Bitmap, Byte[], sound, or any other data type).
 */
public interface Target<R> {
    /**
     * Returns the request for this target.
     *
     * @return A request or null if no request has been set.
     */
    @Nullable
    Request getRequest();

    /**
     * Sets the request for this target.
     *
     * @param request A request or null to cancel and clear the current
     *                request.
     */
    void setRequest(@Nullable Request request);

    /**
     * Called when an asynchronous load request is started.
     *
     * @param drawable A placeholder drawable that should be displayed while the
     *                 load is running.
     */
    void onLoadStarted(@Nullable Drawable drawable);

    /**
     * Called when a load request fails.
     *
     * @param drawable An optional drawable to display when a load request
     *                 fails.
     */
    void onLoadFailed(@Nullable Drawable drawable);

    /**
     * Called when the a load request has completed and the target encoded
     * resource has become available.
     *
     * @param resource The loaded and encoded target resource.
     */
    void onResourceReady(R resource);

    /**
     * Called to set the target's resource object.
     *
     * @param resource The resource object to set.
     */
    void setResource(R resource);

    /**
     * Called to determine if the target has expired. This will happen, for
     * example, if the target implementation contains a weak reference to an
     * application object which has been GC'd. Both onResourceReady() and
     * onLoadFailed() will only be called if this method returns false.
     */
    boolean hasExpired();

    /**
     * Returns the Decoder for this typed target.
     */
    Decoder getResourceDecoder();

    /**
     * Called when the associated request is being recycled. Target
     * implementations should recycle any associated resources.
     */
    void recycle();
}
