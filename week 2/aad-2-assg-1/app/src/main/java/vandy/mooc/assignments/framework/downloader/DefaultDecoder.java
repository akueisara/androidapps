package vandy.mooc.assignments.framework.downloader;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import vandy.mooc.assignments.framework.utils.CacheUtils;
import vandy.mooc.assignments.framework.utils.FileUtils;
import vandy.mooc.assignments.framework.utils.UriUtils;

/**
 * This default decoder produces a resource that is simply a uniquely named copy
 * of any downloaded cached file. Once a resource is produced by this decoder,
 * it will exist until either the application cache is cleared by Android or the
 * user, or until the target decides to remove the resource. The only validation
 * performed by this decoder is to ensure that the produced uri resource does
 * not reference an empty or non-existent file.
 * <p>
 * To help with applications that me require resources with meaningful
 * extensions, this decoder will also preserve the extension of the original
 * network resource when producing a unique copy.
 */
public class DefaultDecoder implements Decoder<Uri> {
    /**
     * Logging tag.
     */
    private static final String TAG = "DefaultDecoder";

    /**
     * Buffer length from stream to stream copy.
     */
    private static final int BUFFER_LENGTH = 1024;

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
        return canDecodeFrom(source);
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
        return File.class.isAssignableFrom(source)
                || Uri.class.isAssignableFrom(source)
                || InputStream.class.isAssignableFrom(source);
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
            return ((File) input).length() > 0;
        } else if (input instanceof Uri) {
            File file = UriUtils.getFileFromUri((Uri) input);
            return file != null && file.length() > 0;
        } else if (input instanceof InputStream) {
            // We can only validate a stream that can be reset.
            InputStream inputStream = (InputStream) input;
            if (!inputStream.markSupported()) {
                // Assume that the content is valid.
                return true;
            }

            try {
                // Return true if the stream has at least 2 bytes of data.
                inputStream.mark(2);
                byte[] buf = new byte[2];
                int read = inputStream.read(buf);
                inputStream.reset();
                return read > 0;
            } catch (IOException e) {
                Log.e(TAG, "InputStream is empty.");
                return false;
            }
        } else {
            throw new IllegalArgumentException(
                    "Unable to validate content of unsupported data type");
        }
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
        try {
            if (input instanceof File) {
                return decodeFile(((File) input).getPath());
            } else if (input instanceof Uri) {
                return decodeFile(UriUtils.getPathNameFromFileUri((Uri) input));
            } else if (input instanceof InputStream) {
                return decodeStream((InputStream) input, "");
            } else {
                throw new IllegalArgumentException(
                        "Unable to decode content of unsupported data type");
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Decoder source not found.");
        } catch (IOException e) {
            Log.e(TAG, "Decoder failed to copy input source to temp file");
        }

        return null;
    }

    /**
     * Decodes an image from a file path.
     *
     * @param pathName A path to an image file.
     * @return A uri of a local temp file copy of the original cache file.
     * @throws IOException
     */
    @SuppressWarnings("WeakerAccess")
    private Uri decodeFile(String pathName)
            throws IOException {
        return decodeStream(new FileInputStream(pathName),
                            FileUtils.getExtension(pathName));
    }

    /**
     * Decodes from an input stream to a temporary file.
     *
     * @param inputStream A input stream.
     * @return A bitmap or null.
     */
    @SuppressWarnings("WeakerAccess")
    private Uri decodeStream(InputStream inputStream, String extension)
            throws IOException {
        File file =
                CacheUtils.getNewTempFile(
                        DownloadManager.get().getContext(), extension);

        try (OutputStream outputStream = new FileOutputStream(file)) {
            // Transfer bytes from in to out
            byte[] buf = new byte[BUFFER_LENGTH];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
        }

        return UriUtils.getUriFromFile(file);
    }
}
