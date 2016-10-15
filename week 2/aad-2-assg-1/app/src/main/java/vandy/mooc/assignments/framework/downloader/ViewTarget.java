package vandy.mooc.assignments.framework.downloader;

import android.graphics.drawable.Drawable;
import android.support.annotation.CallSuper;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.view.View;

import java.lang.ref.WeakReference;

import vandy.mooc.assignments.framework.utils.Preconditions;
import vandy.mooc.assignments.framework.utils.Utils;

/**
 * A abstract view target that stores a weak reference to an view. The request
 * for this target is stored as the default view tag. This implementation design
 * prohibits any application from using this framework from setting custom view
 * tags. The benefit of this design is that the tight relationship between the
 * view target and the stored request makes is straightforward to determine if a
 * view has been recycled and therefore should have it's request cancelled for
 * starting a new one.
 *
 * @param <V> Any type of view.
 * @param <R> The encoded data resource type supported by this view target.
 */
@SuppressWarnings("WeakerAccess")
public abstract class ViewTarget<V extends View, R> implements Target<R> {
    /**
     * A weak reference to the target UI component.
     */
    private final WeakReference<V> mViewReference;

    /**
     * Constructor. Keeps a weak reference to the passed view.
     *
     * @param view The view.
     */
    public ViewTarget(V view) {
        mViewReference = new WeakReference<>(view);
    }

    /**
     * Package only helper that returns the request for the passed view.
     *
     * @return A request or null if no request has been set or is invalid.
     */
    @MainThread
    @Nullable
    static Request getRequest(View view) {
        Utils.assertMainThread();
        Object tag = view.getTag();
        return tag != null && tag instanceof Request ? (Request) tag : null;
    }

    /**
     * Hook method called to determine if the target has expired. This will
     * happen, for example, if the target implementation contains a weak
     * reference to an application object which has been GC'd. Both
     * onResourceReady() and onLoadFailed() will only be called if this method
     * returns false.
     */
    @Override
    public boolean hasExpired() {
        return mViewReference.get() == null;
    }

    /**
     * Called when the a load request has completed and the target encoded
     * resource has become available. This default implementation does nothing.
     *
     * @param resource The loaded and encoded target resource.
     */
    @Override
    public void onResourceReady(R resource) {
        Utils.assertMainThread();
    }

    /**
     * Default does nothing.
     *
     * @param drawable An optional placeholder drawable to display while the
     *                 request is being processed.
     */
    @MainThread
    @Override
    public void onLoadStarted(@Nullable Drawable drawable) {
        Utils.assertMainThread();
    }

    /**
     * Default does nothing.
     *
     * @param drawable An optional drawable to display when a request failed.
     */
    @MainThread
    @Override
    public void onLoadFailed(@Nullable Drawable drawable) {
        Utils.assertMainThread();
    }

    /**
     * Returns the request for this target.
     *
     * @return A request or null if no request has been set.
     */
    @MainThread
    @Nullable
    @Override
    public Request getRequest() {
        Utils.assertMainThread();
        V view = getView();
        if (view != null) {
            assertValidTagState();
            return (Request) view.getTag();
        } else {
            return null;
        }
    }

    /**
     * Stores the passed request as the default view tag or if null is passed,
     * clears the view tag. Note that target implementations are not responsible
     * for canceling old requests. This is performed by the framework request
     * lifecycle management.
     *
     * @param request A request or null to cancel current request and clear the
     *                view tag.
     */
    @MainThread
    @Override
    public void setRequest(@Nullable Request request) {
        Utils.assertMainThread();

        V view = getView();
        if (view != null) {
            assertValidTagState();
            Request oldRequest = (Request) view.getTag();
            Preconditions.checkState(
                    oldRequest == null || !oldRequest.isRunning(),
                    "Old request should be cancelled before "
                            + "calling setRequest.");
            view.setTag(request);
        }
    }

    /**
     * Returns a hard reference to the the view object if it has not been GC'd.
     *
     * @return A view object or null if the view has not been set or has been
     * GC'd.
     */
    @MainThread
    public V getView() {
        Utils.assertMainThread();
        return mViewReference.get();
    }

    /**
     * Returns a hard reference the view target or asserts if that target has
     * been released.
     *
     * @return The view target or throws an IllegalStateException.
     */
    @MainThread
    private V assertGetView() {
        Utils.assertMainThread();
        V view = getView();
        if (view == null) {
            throw new IllegalStateException(
                    "Attempt to access a recycled view");
        }

        return view;
    }

    /**
     * Throws and IllegalStateException if the application has set a custom view
     * tag.
     */
    @MainThread
    @SuppressWarnings({"UnusedReturnValue", "SameReturnValue"})
    private boolean assertValidTagState() {
        Utils.assertMainThread();
        V view = assertGetView();

        if (view.getTag() != null && !(view.getTag() instanceof Request)) {
            throw new IllegalStateException(
                    "ViewTarget requires exclusive access to View tag.");
        }

        return true;
    }

    /**
     * Called when the target request is being recycled. This is where you
     * should release any resources used by this target.
     */
    @MainThread
    @CallSuper
    @Override
    public void recycle() {
        Utils.assertMainThread();

        // Clear view tag.
        setRequest(null);

        // Release the weak reference to the view.
        mViewReference.clear();
    }
}
