package vandy.mooc.assignments.framework.utils;

import android.content.Context;
import android.net.Uri;
import android.os.StatFs;
import android.support.annotation.Nullable;

import java.io.File;

import vandy.mooc.assignments.framework.downloader.Request;

/**
 * A utility class containing disk cache management methods.
 */
public final class CacheUtils {
    /**
     * Logging tag.
     */
    private static final String TAG = "CacheUtils";

    /**
     * Application cache sub-directory. Note that this name must be exactly the
     * same name used in the resource xml/file_paths.xml to support the
     * applications file provider implementation.
     */
    private static final String CACHE_DIRNAME = "download";

    /**
     * Sub-folder within the cache that can be used to store temporary files.
     * This subfolder should be periodically cleared so that any file entries
     * left over from abnormal app termination will not waste system resources.
     */
    private static final String TEMP_DIRNAME = "temp";

    /**
     * Cache size limit constants.
     */
    private static final int MIN_DISK_CACHE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final int MAX_DISK_CACHE_AS_PERCENT = 2; // 2%

    /**
     * Ensure this class is only used as a utility.
     */
    private CacheUtils() {
        throw new AssertionError();
    }

    /**
     * @return Application cache directory sub-folder name.
     */
    private static String getCacheDirName() {
        return CACHE_DIRNAME;
    }

    /**
     * Gets the current application cache directory path in private storage.
     *
     * @param context Any context.
     * @return The default application cache directory path.
     */
    public static String getCacheDirPathName(Context context) {
        return context.getApplicationContext().getCacheDir()
                + File.separator
                + getCacheDirName();
    }

    /**
     * Constructs a path name to a unique temporary file within the caches'
     * temporary sub-directory.
     *
     * @return Application temporary sub-directory path.
     */
    public static String getTempDirPathName(Context context) {
        return getCacheDirPathName(context)
                + File.separator
                + TEMP_DIRNAME;
    }

    /**
     * Creates a file object with the specified name in the application cache
     * directory.
     *
     * @param fileName A file name string.
     * @return A file object with the specified name
     */
    @Nullable
    public static File getCacheFile(Context context, String fileName) {
        return new File(getCacheDirPathName(context)
                                + File.separator
                                + fileName);
    }

    /**
     * Creates the application cache directory if it does not already exist.
     *
     * @param context A context.
     * @return The cache directory as a File object.
     */
    public static File createCacheDir(Context context) {
        return FileUtils.createDir(context, getCacheDirPathName(context));
    }

    /**
     * Returns the amount of storage currently available in the cache.
     *
     * @param dir The cache directory path name.
     * @return The amount of storage available in bytes.
     */
    public static long calculateDiskCacheSize(File dir) {
        long size = MIN_DISK_CACHE_SIZE;

        try {
            StatFs statFs = new StatFs(dir.getAbsolutePath());
            long available =
                    statFs.getBlockCountLong() * statFs.getBlockSizeLong();
            // Target 2% of the total space.
            size = available * MAX_DISK_CACHE_AS_PERCENT / 100;
        } catch (IllegalArgumentException ignored) {
        }

        // Bound inside min/max size for disk cache.
        return Math.max(Math.min(size, MAX_DISK_CACHE_SIZE),
                        MIN_DISK_CACHE_SIZE);
    }

    /**
     * Returns a unique temporary cache file suitable for downloading streamed
     * data that can then be moved to a permanent cache file. To provide better
     * cleanup/recovery support, all returned files are located in a temporary
     * sub-directory within the cache. This allows them to be easily deleted.
     *
     * @param context Any context.
     * @return The file uri.
     */
    public static File getNewTempFile(Context context) {
        // Make sure the temp dir is created before returning temp file.
        FileUtils.createDir(context, getTempDirPathName(context));
        return new File(getNewTempFilePathName(context, null));
    }

    /**
     * Returns a unique temporary cache file suitable for downloading streamed
     * data that can then be moved to a permanent cache file. To provide better
     * cleanup/recovery support, all returned files are located in a temporary
     * sub-directory within the cache. This allows them to be easily deleted.
     *
     * @param context   Any context.
     * @param extension An optional path extension.
     * @return The file uri.
     */
    public static File getNewTempFile(
            Context context, @Nullable String extension) {
        // Make sure the temp dir is created before returning temp file.
        FileUtils.createDir(context, getTempDirPathName(context));
        return new File(getNewTempFilePathName(context, extension));
    }

    /**
     * Returns a unique temporary cache file path name suitable for downloading
     * streamed data that can then be moved to a permanent cache file. To
     * provide better cleanup/recovery support, all returned files are located
     * in a temporary sub-directory within the cache. This allows them to be
     * easily deleted.
     *
     * @param context Any context.
     * @return A unique path name within the cache temporary directory.
     */
    public static String getNewTempFilePathName(
            Context context, String extension) {
        return FileUtils.createUniqueFileName(
                getTempDirPathName(context), extension);
    }

    /**
     * Deletes all cached files with the specified tag.
     *
     * @param context A context.
     * @param tag The tag to match.
     * @return The count of deleted files.
     */
    public static int clearTaggedFiles(Context context, String tag) {
        return walkAndDelete(getCacheDirPathName(context), tag);
    }

    /**
     * Recursively walks the cache directory and deletes all files with the
     * specified tag.
     *
     * @param path The directory to walk.
     * @param tag The tag to match.
     * @return The count of deleted files.
     */
    private static int walkAndDelete(String path, String tag) {
        File root = new File(path);
        File[] files = root.listFiles();

        int count = 0;

        if (files == null) {
            return count;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                count += walkAndDelete(file.getAbsolutePath(), tag);
            } else {
                if (tag.equals(Request.getDecodedTag(Uri.fromFile(file)))) {
                    if (file.delete()) {
                        count++;
                    }
                }
            }
        }

        return count;
    }
}
