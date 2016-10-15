package vandy.mooc.assignments.framework.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class containing methods for creating and manipulating Uri
 * objects.
 */
public class UriUtils {
    /**
     * Logging tag.
     */
    private static final String TAG = "UriUtils";

    /**
     * File provider identifier.
     */
    private static final String FILE_PROVIDER_AUTHORITY =
            "vandy.mooc.assignments.fileprovider";

    /**
     * @return Application file provider authority.
     */
    public static String getFileProviderAuthority() {
        return FILE_PROVIDER_AUTHORITY;
    }

    /**
     * Converts a local file uri to a local path name. This will throw an
     * IllegalArgumentException if the passed uri is not a valid file uri.
     *
     * @param uri A uri that references a local file.
     * @return The path name suitable for passing to the File class.
     */
    public static String getPathNameFromFileUri(Uri uri) {
        Preconditions.checkArgument(URLUtil.isFileUrl(uri.toString()),
                                    "Invalid file uri");
        return uri.getPath();
    }

    /**
     * Determines if passed string is a valid URL.
     *
     * @param url A URL string.
     * @return {@code true} if passed URL is valid; {@code false} if not.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isValidUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        // Allow urls from testing framework that use images stored in the
        // application resources. Application resource urls will have a scheme
        // of "android.resource" which will not be recognized by
        // UrlUtil.isResourceUrl() which expects resource urls to start with
        // "file:///android_res/".
        return ContentResolver.SCHEME_ANDROID_RESOURCE.equals(
                Uri.parse(url).getScheme()) || URLUtil.isValidUrl(url);
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
    @Nullable
    public static Uri getFileUriFromPathName(String pathName) {
        // Prevent anyone from passing a Uri.toString() value to this method.
        Preconditions.checkArgument(!URLUtil.isValidUrl(pathName),
                                    "pathName must not be a uri string");
        // Safest method is to use fromFile().
        return Uri.fromFile(new File(pathName));
    }

    /**
     * Returns a File object for the passed file uri. Throws an exception if
     * the passed uri is not well constructed file uri.
     *
     * @param uri A file uri.
     * @return A File object.
     */
    @Nullable
    public static File getFileFromUri(Uri uri) {
        Preconditions.checkArgument(URLUtil.isFileUrl(uri.toString()),
                                    "uri must be of file uri");

        return new File(uri.getPath());
    }

    /**
     * Returns a file uri for the specified local file. This method is here only
     * to try to centralize all Uri calls for debugging purposes.
     *
     * @param file A local file.
     * @return A well constructed file uri.
     */
    public static Uri getUriFromFile(File file) {
        return Uri.fromFile(file);
    }

    /**
     * Converts the passed local path name to content uri suitable for passing
     * in an intent to any system activity.
     *
     * @param context  A context.
     * @param pathName A local path name.
     * @return A content uri.
     */
    public static Uri getFileContentUri(Context context, String pathName) {
        Preconditions.checkArgument(!URLUtil.isValidUrl(pathName),
                                    "pathName must not be a uri string");
        return FileProvider.getUriForFile(
                context, getFileProviderAuthority(), new File(pathName));
    }

    /**
     * Converts the passed local uri to a content uri suitable for passing in an
     * intent to any system activity.
     *
     * @param context A context.
     * @param uri     A local file uri.
     * @return A content uri.
     */
    public static Uri getFileContentUri(Context context, Uri uri) {
        return getFileContentUri(context, getPathNameFromFileUri(uri));
    }

    /**
     * Returns the default cache path name for this download request.
     *
     * @param context A context.
     * @param uri     A uri.
     * @return A cache path that matches the requested uri.
     */
    @Nullable
    @Deprecated
    private static String getCachePathName(Context context, Uri uri) {
        String filePath;
        try {
            String fileName = URLEncoder.encode(uri.toString(), "UTF-8");

            // Sanity check...
            String original = URLDecoder.decode(fileName, "UTF-8");
            Preconditions.checkState(Uri.parse(original).equals(uri),
                                     "Cache path name generation error");

            String dirPath = CacheUtils.getCacheDirPathName(context);
            filePath = dirPath + "/" + fileName;

            return filePath;
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "Download failed: Unable to convert URL to a file path");
            return null;
        }
    }

    /**
     * Returns the original uri from the passed cache path name.
     *
     * @param uri A cache uri.
     * @return The original uri that was used to create the cache path name.
     */
    @Nullable
    @Deprecated
    public static Uri getSourceUriFromCacheUri(Uri uri) {
        try {
            String encodedName = new File(uri.toString()).getName();
            return Uri.parse(URLDecoder.decode(encodedName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "Unable to retrieve URL from cached path name");
            return null;
        }
    }

    /**
     * Grants the specified uri permissions to all packages that can process the
     * intent. The most secure granting model is used for the current API. This
     * method is designed to work on all versions of Android but has been tested
     * only on API 23, and 24.
     *
     * @param context     A context.
     * @param intent      An intent containing a data uri that was obtained from
     *                    FileProvider.getUriForFile().
     * @param permissions The permissions to grant.
     */
    public static void grantUriPermissions(
            Context context, Intent intent, int permissions) {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            // Find all packages that support this intent and grant them
            // the specified permissions.
            List<ResolveInfo> resInfoList =
                    context.getPackageManager().queryIntentActivities(
                            intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(
                        packageName,
                        intent.getData(),
                        permissions);
            }
        } else {
            // Just grant permissions to all apps.
            intent.setFlags(permissions);
        }
    }

    /**
     * Builds an action intent and converts the passed local file uri to a
     * content uri with read permission for all applications that can process
     * the intent. This method is designed to work on all versions of Android
     * but has only been tested on API 23 and 24.
     *
     * @param context  A context.
     * @param pathName A local file path.
     * @param action   The intent action.
     * @param type     The intent type.
     * @return The built intent.
     */
    public static Intent buildReadPrivateUriIntent(
            Context context, String pathName, String action, String type) {
        // Build a content uri.
        Uri uri = FileProvider.getUriForFile(
                context, getFileProviderAuthority(), new File(pathName));

        // Create and initialize the intent.
        Intent intent = new Intent()
                .setAction(action)
                .setDataAndType(uri, type);

        // Call helper method that uses the most secure permission granting
        // model for the each API.
        grantUriPermissions(context, intent,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);

        return intent;
    }

    /**
     * Builds an action intent and converts the passed local file uri to a
     * content uri with read permission for all applications that can process
     * the intent. This method is designed to work on all versions of Android
     * but has only been tested on API 23 and 24.
     *
     * @param context A context.
     * @param uri     A local file uri.
     * @param action  The intent action.
     * @param type    The intent type.
     * @return The built intent.
     */
    public static Intent buildReadPrivateUriIntent(
            Context context, Uri uri, String action, String type) {
        return buildReadPrivateUriIntent(
                context, getPathNameFromFileUri(uri), action, type);
    }

    /**
     * Returns a uri's base data resource name without an extension.
     *
     * @param uri A uri.
     * @return The base data resource name.
     */
    public static @NonNull String getLastPathSegmentBaseName(Uri uri) {
        String name = null;

        if (uri != null && !TextUtils.isEmpty(uri.toString())) {
            name = uri.getLastPathSegment();
            if (name != null && name.contains(".")) {
                name = name.substring(0, name.lastIndexOf("."));
            }
        }

        return name != null ? name : "";
    }


    /**
     * Parses an array of string urls into an array of Uris. No error checking
     * is performed on the parsing.
     *
     * @return An array of parsed Uris.
     */
    public static ArrayList<Uri> parseAll(String... strings) {
        ArrayList<Uri> uris = new ArrayList<>(strings.length);
        for (final String string : strings) {
            uris.add(Uri.parse(string));
        }

        return uris;
    }
}
