package vandy.mooc.assignments.framework.downloader;

import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * A decoder that simply exposes any downloaded cached file as a uri resource.
 * Note that this decoder should be used with care since cache files may be
 * frequently replaced by recycled targets and by duplicated concurrently
 * running download requests.
 */
public class CacheDecoder implements Decoder<Uri> {
    /**
     * Logging tag.
     */
    private static final String TAG = "DefaultDecoder";

    /**
     * Called by the framework to determine if the decoder can or needs to
     * validate incoming the data by calling the canDecodeFrom() method. This
     * default implementation doesn't really need to do any validating, but does
     * so as an example for other Decoder implementations.
     *
     * @param source An input source data type.
     * @return {@code true} if canDecodeFrom should be called, {@code false} if
     * not.
     */
    @Override
    public boolean canValidateContent(Class source) {
        return false;
    }

    /**
     * Returns whether or not the decoder implementation can read and convert
     * from the specified input source.
     *
     * @param source An input class type.
     * @return {@code true} if passed source is a File class.
     */
    @Override
    public boolean canDecodeFrom(Class source) {
        return Uri.class.isAssignableFrom(source);
    }

    /**
     * Validates the input data source contents to ensure that it's data can be
     * decoded.
     *
     * @param input An input data source.
     * @return {@code true} if the data source contains a valid image.
     */
    public boolean isContentValid(Object input) {
        throw new IllegalStateException(
                "isContentValid: should not be called");
    }

    /**
     * Decodes a data object from the passed input source to a bitmap.
     *
     * @param input  An input data source.
     * @param width  not used
     * @param height not used
     * @return An instance of the decoded data resource.
     */
    @Nullable
    public Uri decode(Object input, int width, int height) {
        if (input instanceof Uri) {
            return (Uri) input;
        } else {
            throw new IllegalArgumentException(
                    "Unable to decode content of unsupported data type");
        }
    }
}
