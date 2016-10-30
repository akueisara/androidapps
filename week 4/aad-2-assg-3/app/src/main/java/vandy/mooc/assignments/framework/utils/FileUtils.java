package vandy.mooc.assignments.framework.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import vandy.mooc.assignments.framework.application.Constants;

/**
 * A utility class containing file related methods.
 */
@SuppressWarnings("WeakerAccess")
public final class FileUtils {
    /**
     * Default buffer size used for copying files.
     */
    private static final int BUFFER_LENGTH = 1024;

    /**
     * Ensure this class is only used as a utility.
     */
    private FileUtils() {
    }

    /**
     * Creates the directory if it does not already exist.
     *
     * @param context A context.
     * @return The directory as a File object.
     */
    public static File createDir(Context context, String pathName) {
        File dir = new File(pathName);
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }

        return dir;
    }

    /**
     * Returns the default application image directory.
     *
     * @return The application image directory.
     */
    @SuppressWarnings("UnusedParameters")
    public static String getImageDirectory() {
        return getExternalStorageImageDirectory();
    }

    /**
     * Returns an application defined directory name within the device's
     * external storage DCIM directory. Note that this Android API 23 is more
     * string about apps using external storage so simply declaring
     * READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE will not automatically
     * allow this directory to be used under API 23.
     *
     * @return The download directory path
     */
    @SuppressWarnings("WeakerAccess")
    public static String getExternalStorageImageDirectory() {
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM).getAbsolutePath() +
                "/" +
                Constants.DEFAULT_APPLICATION_DIRECTORY_NAME +
                "/";
    }

    /**
     * Returns the directory path for the application's default image download
     * (sub)directory. The directory is located in the application's private
     * storage area so that there will be no permission issues with the new
     * Android API 23 permission model.
     *
     * @return The download directory path
     */
    @SuppressWarnings("unused")
    public static String getApplicationImageDirectory(Context context) {
        return context.getFilesDir() + "/" +
                Constants.DEFAULT_APPLICATION_DIRECTORY_NAME +
                "/";
    }

    /**
     * Delete the specified directory and contained files. This implementation
     * does not delete subdirectories.
     *
     * @param context   Any context.
     * @param directory The application private or public directory or
     *                  sub-directory. Directories not owned by the application
     *                  will not be deleted.
     * @return returns The number of files that were deleted.
     */
    @SuppressWarnings("UnusedReturnValue")
    public static int deleteDirectory(Context context, String directory) {
        int deleted = 0;

        File dir = new File(directory);

        if (dir.isDirectory()) {
            String[] files = dir.list();

            if (files != null) {
                for (String name : files) {
                    if (deleteFile(context, dir + "/" + name)) {
                        deleted++;
                    }
                }
            }
        }

        return deleted;
    }

    /**
     * Deletes the specified file within the application private or public
     * directory.
     *
     * @param filePath local filePath under the application private or public
     *                 storage directory. Files not owned by the application
     *                 will not be deleted.
     * @return {@code true} if image was deleted; {@code false} if not
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean deleteFile(Context context, String filePath) {
        File file = new File(filePath);
        return file.isFile() && safeDelete(context, file);
    }

    /**
     * Deletes a list of images from the default image directory in local
     * storage.
     *
     * @param filePaths A list of fully qualified files to delete.
     * @return The number of deleted files.
     */
    @SuppressWarnings("unused")
    public static int deleteFiles(Context context, List<String> filePaths) {
        // A list was specified, so only delete the images in that list.
        int deleted = 0;

        // Delete each of the specified images.
        for (String filePath : filePaths) {
            if (deleteFile(context, filePath)) {
                deleted++;
            }
        }

        return deleted;
    }

    /**
     * Security check to ensure that delete is restricted to a predictable set
     * of application directories. The contained directory list can be updated
     * as required.
     *
     * @param file The file or directory that is to be deleted.
     * @return {@code true} if the file was successfully deleted; {@code false}
     * otherwise.
     * @throws SecurityException thrown if file is not, or is not in the
     *                           application cache directory returned by
     *                           getCacheDirPathName().
     */
    public static boolean safeDelete(Context context, File file)
            throws SecurityException {

        String[] appDirs =
                new String[]{
                        CacheUtils.getCacheDirPathName(context),
                        getImageDirectory(),
                        getExternalStorageImageDirectory()};

        for (final String appDir : appDirs) {
            if (file.getAbsolutePath().startsWith(appDir)) {
                // File is safe to delete, so delete it.
                return file.delete();
            }
        }

        // File/dir is not in a safe location so throw an exception.
        throw new SecurityException(
                "An attempt was made to delete than is not in cache."
                        + file.getAbsolutePath());
    }

    /**
     * Returns the number of files in the specified directory.
     *
     * @return The directory's file count.
     */
    @SuppressWarnings("unused")
    public static int getFileCount(String directoryPath) {
        File dir = new File(directoryPath);
        if (!dir.isDirectory()) {
            return 0;
        } else {
            String[] files = dir.list();
            return files != null ? files.length : 0;
        }
    }

    /**
     * Construct a guaranteed unique path name.
     *
     * @return A unique path name based on a random UUI generator.
     */
    public static String createUniqueFileName() {
        // Creating a random UUID (Universally unique identifier).
        return UUID.randomUUID().toString();
    }

    /**
     * Construct a guaranteed unique path name with the specified extension.
     * This is useful not only to pass to data consumers that require a
     * recognized extension, but also as a way to store a name along with the
     * temp file that can be easily retrieved by calling
     * <pre>
     * String extension = tempFileName.substring(tempFileName.indexOf("."));
     * </pre>
     *
     * @param ext An optional file extension.
     * @return A unique path name based on a random UUI generator with the
     * specified extension.
     */
    public static String createUniqueFileName(@NonNull String ext) {
        // Creating a random UUID (Universally unique identifier).
        return createUniquePathName(null, null, ext);
    }

    /**
     * Construct a guaranteed unique file name with an optional prefix and
     * extension. If an extension is specified but doesn't start with a ".", a
     * "." will be injected.
     * <p>
     *
     * @param prefix An optional prefix to prepend to the file name.
     * @param ext    An optional extension to append to the file name.
     * @return A unique file name based on a random UUI generator.
     */
    public static String createUniqueFileName(
            @Nullable String prefix, @Nullable String ext) {
        return createUniquePathName(null, prefix, ext);
    }

    /**
     * Construct a guaranteed unique path name in the specified folder with the
     * specified extension. Both the directory and extension parameters my be
     * null. If an extension is specified but doesn't start with a ".", a "."
     * will be injected.
     * <p>
     * This extension is useful not only to pass to data consumers that require
     * a recognized extension, but also as a way to store a name along with the
     * temp file that can be easily retrieved by calling
     * <pre>
     * String ext = tempFileName.substring(tempFileName.indexOf("."));
     * </pre>
     *
     * @param dir    An optional directory to include in the path.
     * @param prefix An optional prefix to prepend to the file name.
     * @param ext    An optional file extension to append.
     * @return A unique path name based on a random UUI generator in the
     * specified directory with the specified extension.
     */
    public static String createUniquePathName(
            @Nullable String dir,
            @Nullable String prefix,
            @Nullable String ext) {

        String pathName = "";

        // Add directory.
        if (!TextUtils.isEmpty(dir)) {
            pathName += dir;
            if (!pathName.endsWith(File.separator)) {
                pathName += File.separator;
            }
        }

        // Add prefix.
        if (!TextUtils.isEmpty(prefix)) {
            pathName += prefix;
            if (!pathName.endsWith("_")) {
                pathName += "_";
            }
        }

        // Add the unique file name.
        pathName += createUniqueFileName();

        // Add suffix.
        if (!TextUtils.isEmpty(ext)) {
            if (!ext.startsWith(".")) {
                pathName += ".";
            }
            pathName += ext;
        }

        return pathName;
    }

    /**
     * Constructs a proper "file://" uri for the passed local path name. The
     * most reliable way to create this kind of uri is create a File object with
     * the passed path name and then use the Uri.fromFile() helper to build the
     * uri.
     *
     * @param pathName An absolute path to a local file.
     * @return The file uri.
     */
    public static Uri getFileUri(String pathName) {
        Preconditions.checkArgument(!TextUtils.isEmpty(pathName),
                                    "pathName must be an absolute path");
        return Uri.fromFile(new File(pathName));
    }

    /**
     * Copies a file.
     *
     * @param src Source file.
     * @param dst Destination file.
     * @throws IOException
     */
    public static void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dst)) {
            // Transfer bytes from in to out
            byte[] buf = new byte[BUFFER_LENGTH];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }

    /**
     * Returns the file extension.
     *
     * @param pathName A path name.
     * @return The path extension (with no ".") or an empty string.
     */
    public static String getExtension(String pathName) {
        int index = pathName.lastIndexOf(".");
        return (0 <= index && index < pathName.length() - 1)
               ? pathName.substring(index + 1)
               : "";
    }
}
