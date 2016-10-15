package vandy.mooc.assignments.framework.downloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.InputStream;

import vandy.mooc.assignments.framework.utils.BitmapUtils;
import vandy.mooc.assignments.framework.utils.UriUtils;

/**
 * A Bitmap resource wrapper that handles bitmap encoding.
 */
public class BitmapDecoder implements Decoder<Bitmap> {
    /**
     * Returns whether or not the decoder implementation can read and convert
     * from the specified input source.
     *
     * @param source An input class type.
     * @return {@code true} if passed source is a File class.
     */
    @Override
    public boolean canDecodeFrom(Class source) {
        return File.class.isAssignableFrom(source)
                || InputStream.class.isAssignableFrom(source)
                || Uri.class.isAssignableFrom(source);

    }

    /**
     * Called by the framework to determine if the decoder can or needs to pre
     * validate incoming content by calling the isContentValid() method.
     *
     * @return {@code true} if canValidateContent should be called, {@code
     * false} if not.
     * @param source An input source class type.
     */
    @Override
    public boolean canValidateContent(Class source) {
        return canDecodeFrom(source);
    }

    /**
     * Validates the input data source contents to ensure that it's data can be
     * decoded.
     *
     * @param input An input data source.
     * @return {@code true} if the data source contains a valid image.
     */
    public boolean isContentValid(Object input) {
        if (input instanceof File) {
            return BitmapUtils.hasImageContent(((File) input).getPath());
        } else if (input instanceof Uri) {
            return BitmapUtils.hasImageContent(
                    UriUtils.getPathNameFromFileUri((Uri) input));
        } else if (input instanceof InputStream) {
            return BitmapUtils.hasImageContent((InputStream) input);
        } else {
            throw new IllegalArgumentException(
                    "Unable to validate content of unsupported input source");
        }
    }

    /**
     * Decodes a data object from the passed input source to a bitmap.
     *
     * @param input  An input data source.
     * @param width  Maximum width.
     * @param height Maximum height.
     * @return An instance of the decoded data resource.
     */
    @Nullable
    public Bitmap decode(Object input, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        if (input instanceof File) {
            return decodeFile(((File)input).getPath(), width, height);
        } else if (input instanceof Uri) {
            return decodeFile(UriUtils.getPathNameFromFileUri((Uri) input),
                              width, height);
        } else if (input instanceof InputStream) {
            return decodeStream(((InputStream) input), width, height);
        }

        return null;
    }

    /**
     * Decodes an image from a file path.
     *
     * @param pathName A file path.
     * @param width    Maximum width.
     * @param height   Maximum height.
     * @return A bitmap or null.
     */
    @SuppressWarnings("WeakerAccess")
    @Nullable
    public Bitmap decodeFile(String pathName, int width, int height) {
        return BitmapUtils.decodeSampledBitmapFromFile(pathName, width, height);
    }

    /**
     * Decodes from an input stream that optimally supports mark and reset
     * operations. If a maximum width and/or height are specified then the
     * passed stream must support mark and reset so that the bitmap can be down
     * sampled properly. If the width and/or height are specified and the input
     * stream does not support mark and reset, then an IllegalArgumentException
     * will be throw.
     *
     * @param inputStream An input stream containing an image source.
     * @param width       Maximum width.
     * @param height      Maximum height.
     * @return A bitmap or null.
     */
    @SuppressWarnings("WeakerAccess")
    @Nullable
    public Bitmap decodeStream(InputStream inputStream, int width, int height) {
        return BitmapUtils.decodeSampledBitmapFromStream(
                inputStream, width, height);
    }
}
