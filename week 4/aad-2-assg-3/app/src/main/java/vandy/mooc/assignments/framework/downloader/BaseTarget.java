package vandy.mooc.assignments.framework.downloader;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

/**
 * An abstract target that establishes a link between the target and a
 * request. This class is not currently used but will be required for non-view
 * based download targets.
 *
 * @param <R> The resource object.
 */
@SuppressWarnings("unused")
public abstract class BaseTarget<R> implements Target<R> {
    /**
     * The request for this target.
     */
    private Request mRequest;

    /**
     * Returns the request for this target.
     *
     * @return A request or null if none has be set.
     */
    @Nullable
    @Override
    public Request getRequest() {
        return mRequest;
    }

    /**
     * Sets the current request.
     *
     * @param request A request or null to cancel and clear the current
     */
    @Override
    public void setRequest(@Nullable Request request) {
        mRequest = request;
    }

    /**
     * Default does nothing.
     *
     * @param drawable An optional placeholder drawable to display while the
     *                 request is being processed.
     */
    @Override
    public void onLoadStarted(@Nullable Drawable drawable) {
    }

    /**
     * Default does nothing.
     *
     * @param drawable An optional drawable to display when a request failed.
     */
    @Override
    public void onLoadFailed(@Nullable Drawable drawable) {
    }

    /**
     * Default implementation returns no decoder.
     */
    @Override
    public Decoder getResourceDecoder() {
        return null;
    }
}
