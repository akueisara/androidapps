package vandy.mooc.assignments.framework.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * BitmapUtils
 * <p/>
 * This helper class encapsulates Bitmap-specific processing methods.
 */
public final class BitmapUtils {
    /**
     * Debug logging tag.
     */
    private static final String TAG = "BitmapUtils";

    /**
     * Ensure this class is only used as a utility.
     */
    private BitmapUtils() {
        throw new AssertionError();
    }

    /**
     * This returns the sample size that should be used when down-sampling the
     * image. This ensures that the image is scaled appropriately with respect
     * to it's final display size.
     */
    @SuppressWarnings("WeakerAccess")
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2
            // and keeps both height and width larger than the requested
            // height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * This will return a bitmap that is loaded and appropriately scaled from
     * the filePath parameter.
     */
    public static Bitmap decodeSampledBitmapFromFile(
            String pathName, int width, int height) {

        // First decode with inJustDecodeBounds=true to check dimensions.
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);

        // If either width or height is passed in as 0, then use the actual
        // stored image dimension.
        if (width == 0) {
            width = options.outWidth;
        }
        if (height == 0) {
            height = options.outHeight;
        }

        // Calculate inSampleSize
        options.inSampleSize =
                calculateInSampleSize(options, width, height);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }

    /**
     * Decodes from an input stream that optimally supports mark and reset
     * operations. If a maximum width and/or height are specified then the
     * passed stream must support mark and reset so that the bitmap can be down
     * sampled properly. If the width and/or height are specified and the input
     * stream does not support mark and reset, then an IllegalArgumentException
     * will be throw.
     */
    public static Bitmap decodeSampledBitmapFromStream(
            InputStream inputStream, int width, int height) {
        if ((width != 0 || height != 0) && !inputStream.markSupported()) {
            throw new IllegalArgumentException(
                    "Bitmap decoding requires an input stream that supports "
                            + "mark and reset");
        }

        // Set a mark for reset. Since we have no idea of the size of this
        // image, just set the maximum value possible.
        inputStream.mark(Integer.MAX_VALUE);

        // First decode with inJustDecodeBounds=true to check dimensions.
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);

        // Reset the stream for the actual decoding phase.
        try {
            inputStream.reset();
        } catch (IOException e) {
            Log.e(TAG, "Failed to reset input stream during bitmap decoding");
            return null;
        }

        // If either width or height is passed in as 0, then use the actual
        // stored image dimension.
        if (width == 0) {
            width = options.outWidth;
        }
        if (height == 0) {
            height = options.outHeight;
        }

        // Calculate inSampleSize
        options.inSampleSize =
                calculateInSampleSize(options, width, height);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(inputStream, null, options);
    }

    /**
     * This will return a bitmap that is loaded and appropriately scaled from
     * the application resources.
     */
    @SuppressWarnings("unused")
    public static Bitmap decodeSampledBitmapFromResource(
            Resources res, int resId, int width, int height) {
        if (resId == 0) {
            return null;
        }

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // If either width or height is passed in as 0, then use the actual
        // stored image dimension.
        if (width == 0) {
            width = options.outWidth;
        }
        if (height == 0) {
            height = options.outHeight;
        }

        // Calculate inSampleSize
        options.inSampleSize =
                calculateInSampleSize(options, width, height);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * Determines if the file contains a valid image.
     *
     * @param pathName A file path.
     * @return {@code true} if the stream contains a valid image; {@code false} if not.
     */
    public static boolean hasImageContent(String pathName) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        return options.outMimeType != null;
    }

    /**
     * Determines if the stream contains a valid image.
     *
     * @param inputStream An input stream.
     * @return {@code true} if the stream contains a valid image; {@code false} if not.
     */
    public static boolean hasImageContent(InputStream inputStream) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        return options.outMimeType != null;
    }
}
