package vandy.mooc.assignments.framework.downloader;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * A default ImageView target implementation that will automatically handle the
 * default updating any ImageView target during the request download lifecycle.
 * Applications can use this class by passing a new instance of this class when
 * the a new request is created:
 * <pre> {@code
 * DownloadManger.with(context)
 *     .placeholder(R.drawable.placeholder)
 *     .error(R.drawable.error)
 *     .into(new ImageViewTarget(imageView) {
 *         @Override
 *         public void onResourceReady(Bitmap bitmap) {
 *             ... update an image view
 *         }
 *     });} </pre>
 * The application can override any of the public methods to customize the
 * behaviour of this implementation or call the super method implementation to
 * get the default behaviour.
 * <p/>
 * Alternatively, invoking a image load request with the following command
 * <pre> {@code
 * DownloadManger.with(context)
 *     .placeholder(R.drawable.placeholder)
 *     .error(R.drawable.error)
 *     .into(imageView);} </pre>
 * will result in this implementation being fully used to process the all of the
 * download request lifecycle callbacks.
 * <p/>
 * Note that since wrapper class only maintains a weak reference to the view to
 * prevent the DownloadManager framework from leaking activity contexts.
 * Additionally, this framework will properly handle recycled views so the
 * application should not clear recycled views before submitting new load
 * requests.
 */
public class ImageViewTarget extends ViewTarget<ImageView, Bitmap> {
    /**
     * Debug logging tag.
     */
    private static final String TAG = "ImageViewTarget";

    /**
     * Constructor. Keeps a weak reference to the passed image view and
     * registers a bitmap resource decoder.
     *
     * @param view The view to wrap.
     */
    public ImageViewTarget(ImageView view) {
        super(view);
        DecoderRegistry.get().registerDecoder(Bitmap.class, new BitmapDecoder());
    }

    /**
     * Sets the ImageView bitmap.
     *
     * @param bitmap A bitmap or null to clear the image view.
     */
    @Override
    public void setResource(Bitmap bitmap) {
        ImageView imageView = getView();

        // We can only set the bitmap if the view has not been released.
        if (imageView != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            Log.w(TAG, "Attempt to set a bitmap on a recycled image view");
        }
    }

    /**
     * Hook method called when a load request is started.
     *
     * @param drawable Drawable to display while an asynchronous load is
     *                 running.
     */
    @Override
    public void onLoadStarted(@Nullable Drawable drawable) {
        setImageDrawable(drawable);
        super.onLoadStarted(drawable);
    }

    /**
     * Hook method called when a load request failed.
     *
     * @param drawable Drawable to display when a load request has failed.
     */
    @Override
    public void onLoadFailed(@Nullable Drawable drawable) {
        setImageDrawable(drawable);
        super.onLoadFailed(drawable);
    }

    /**
     * Called when the data source has been loaded and encoded into a bitmap.
     *
     * @param bitmap The encoded bitmap.
     */
    @Override
    public void onResourceReady(Bitmap bitmap) {
        setResource(bitmap);
    }

    /**
     * Common helper method to set the image drawable. If the image view has
     * been released, the method will just return.
     *
     * @param drawable A Drawable.
     */
    private void setImageDrawable(Drawable drawable) {
        ImageView imageView = getView();

        // We can only set the drawable if the view has not been released.
        if (imageView != null) {
            imageView.setImageDrawable(drawable);
        } else {
            Log.w(TAG, "Attempt to set a drawable on a recycled image view");
        }
    }

    /**
     * Returns the Decoder for this typed target.
     */
    @Override
    public Decoder getResourceDecoder() {
        return DecoderRegistry.get().getDecoder(InputStream.class, Bitmap.class);
    }
}

