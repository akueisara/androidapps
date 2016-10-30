package vandy.mooc.assignments.framework.downloader;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.net.http.HttpResponseCache;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import vandy.mooc.assignments.framework.utils.CacheUtils;
import vandy.mooc.assignments.framework.utils.FileUtils;
import vandy.mooc.assignments.framework.utils.Preconditions;
import vandy.mooc.assignments.framework.utils.UriUtils;

/**
 * A utility class (https://en.wikipedia.org/wiki/Utility_class) that supports
 * an generic HTTP download handler and HTTP response cache to reduce redundant
 * network hits. The design supports downloading of any type of network resource
 * but also supports a callback hook to any passed OnPreValidateData interface
 * implementation which can be used to restrict downloads and caching to a
 * specific data type. The current framework implementation provides a single
 * static ImageValidator that can be used to restrict downloads to image data
 * sources.
 */
public final class HttpDownloader {
    /**
     * Logging tag.
     */
    private static final String TAG = "HttpDownloader";

    /**
     * Connection timeout constants.
     */
    private static final int DEFAULT_READ_TIMEOUT_MILLIS = 20 * 1000; // 20s
    private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
    private static final int DEFAULT_STALE_TIME = 60 * 60 * 24 * 28; // 4-weeks

    /**
     * Caching policy constants used in header.
     */
    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String CACHE_ONLY =
            "only-if-cached,max-age=2147483647";
    private static final String NO_CACHE = "no-cache";
    private static final String MAX_STALE = "max-stale=";
    private static final String CONTENT_LENGTH = "Content-Length";
    /**
     * Used to determine if the connection used the cache or the web.
     */
    private static final String RESPONSE_SOURCE = "X-Android-Response-Source";

    /**
     * Size of each file I/O operation.
     */
    private static final int BUFFER_LENGTH = 1024;
    private static final Object sLock = new Object();
    /**
     * Http response cache and sLock object used during cache creation.
     */
    private static volatile Object sResponseCache;

    /**
     * Utility classes should always be final and have a private constructor.
     */
    private HttpDownloader() {
    }

    /**
     * Loads the requested resource either from the HTTP cache or from the
     * network. In either case, the data is copied to a local file on the
     * device.
     *
     * @param request The download request.
     * @return The cached URL if the download is successful; null otherwise.
     */
    public static Uri download(Request request) {
        // Sanity check, although theoretically the request can be recycled
        // in the MainThread as a callback from a RecyclerViewHolder recycle
        // event.
        Preconditions.checkNotNull(
                request, "HttpDownload attempted on an unlinked request");

        // HttpResponseCache was only added in API 14. caching supported
        // added in API 13
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            //installCacheIfNeeded(request.mContext);
        }

        // Cancel checkpoint.
        if (request.isCancelled()) {
            Log.d(TAG, "HTTP download was cancelled.");
            return null;
        }

        /**
         * Check if the request URL has recently been saved to disk and if so
         * and the network policy allows caching, then simply return the
         * existing cached file.
         */
        if (NetworkPolicy.readFromCache(request.mNetworkPolicy)) {
            File file =
                    Preconditions.checkNotNull(
                            CacheUtils.getCacheFile(
                                    request.mContext, request.toFileName()));

            if (file.isFile()) {
                if (DownloadManager.get().isLoggingEnabled()) {
                    Log.d(TAG, "Content loaded from LOCAL CACHE"
                            + " (size = "
                            + file.length() + ")");
                }

                return UriUtils.getUriFromFile(file);
            }
        }

        // Cancel checkpoint.
        if (request.isCancelled()) {
            Log.d(TAG, "HTTP download was cancelled.");
            return null;
        }

        // Check if this request has a decoder and is able to pre-validate
        // stream content, then open the stream and pass it to the decoder
        // to pre-validate.
        if (request.getResourceDecoder() != null &&
                request.getResourceDecoder()
                        .canValidateContent(InputStream.class)) {
            // Open the input stream and pass to decoder to pre-validate.
            try (final InputStream inputStream =
                         getInputStream(request.mContext,
                                        request.mUri,
                                        request.mNetworkPolicy)) {
                // See if the request target can handle this stream.
                Preconditions.checkNotNull(inputStream);
                if (!request.getResourceDecoder().isContentValid(inputStream)) {
                    Log.w(TAG,
                          "Decoder reported invalid content for " + request);
                    return null;
                }
            } catch (Exception e) {
                Log.w(TAG,
                      "HTTP download was unable to open an input stream: " + e);
                return null;
            }
        }

        // Cancel checkpoint.
        if (request.isCancelled()) {
            Log.d(TAG, "HTTP download was cancelled.");
            return null;
        }

        /**
         * Get the cache file for this source uri (may or may not already
         * exist).
         */
        File file =
                Preconditions.checkNotNull(
                        CacheUtils.getCacheFile(
                                request.mContext,
                                request.toFileName()));

        // Always delete any existing cached download here so we can gracefully
        // handle concurrent downloads of the same resource by simply renaming
        // the download temp file to the cache file. If the rename fails, it
        // simply means that a concurrent download for the same resource
        // completed before this one and we can assume that the resulting
        // cached file will satisfy this request as well.
        synchronized (sLock) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }

        File tempFile = CacheUtils.getNewTempFile(request.mContext);

        // Get the content of the resource at the url and save it
        // to an output file. Note that the size of the storage location of
        // output files is not managed by this Http response cache. The app
        // is responsible for managing these output resources.
        try (final InputStream inputStream =
                     getInputStream(request.mContext,
                                    request.mUri,
                                    request.mNetworkPolicy);
             final OutputStream outputStream = new FileOutputStream(tempFile)) {
            copyStream(inputStream, outputStream, request);
            if (!tempFile.renameTo(file)) {
                Log.w(TAG,
                      "HTTP download: cache file already created by a "
                              + "duplicate concurrent download");
            }

            // Now cleanup the temp file.
            FileUtils.safeDelete(request.mContext, tempFile);

            // Return the cached file's uri.
            return UriUtils.getUriFromFile(file);
        } catch (IOException e) {
            Log.w(TAG, "HTTP download encountered an IOException:" + e);
        }

        Log.d(TAG, "Download failed for " + request.mUri.toString());

        // At this point the above block must have thrown an exception.
        // Make sure any partially written cache file is deleted.
        FileUtils.safeDelete(request.mContext, file);

        return null;
    }

    /**
     * Closes the HTTP response cache.
     */
    public static void shutdown() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ResponseCache.close(sResponseCache);
        }
    }

    /**
     * Clears the cache directory contents and deletes the directory.
     *
     * @param context Any context.
     */
    public static void clearCache(Context context) {
        FileUtils.deleteDirectory(context,
                                  CacheUtils.getCacheDirPathName(context));
    }

    /**
     * Copy the contents of the @a inputStream to the @a outputStream.
     *
     * @param inputStream  An input stream.
     * @param outputStream An output stream.
     * @param request      The download request.
     * @throws IOException
     */
    private static void copyStream(
            InputStream inputStream,
            OutputStream outputStream,
            Request request)
            throws IOException {
        byte[] buffer = new byte[BUFFER_LENGTH];

        for (int n; (n = inputStream.read(buffer)) >= 0; ) {
            // Cancel checkpoint.
            if (request.isCancelled()) {
                throw new InterruptedIOException("Download interrupted");
            }

            outputStream.write(buffer, 0, n);
        }

        outputStream.flush();
    }

    /**
     * Creates an input stream for the passed URL. This method will support both
     * normal URLs and any URL located in the application resources. Note that
     * since resource URLs do not use the HTTP cache, the networkPolicy is
     * ignored for these resources.
     *
     * @param context A context.
     * @param uri     A target URL.
     * @return An input stream.
     * @throws IOException
     */
    private static InputStream getInputStream(
            Context context,
            Uri uri,
            int networkPolicy)
            throws IOException {
        if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(uri.getScheme())) {
            // Handle URLs that map into application resources.
            if (DownloadManager.get().isLoggingEnabled()) {
                Log.d(TAG, "Loading image from application resources");
            }
            return context.getContentResolver().openInputStream(uri);
        } else {
            // Normal URL.
            return getNetworkInputStream(uri, networkPolicy);
        }
    }

    /**
     * Creates an HTTPUrlConnection and returns a input stream that can be used
     * to read the data contents at the specified URL. The data may either
     * original from network or from a local disk cache.
     *
     * @param uri           The URL target resource.
     * @param networkPolicy The current network policy for this download.
     * @return An input stream that can be used to retrieve the data contents.
     */
    private static InputStream getNetworkInputStream(
            Uri uri,
            int networkPolicy) throws IOException {
        HttpURLConnection connection;

        // Open a new connection.
        connection = openConnection(uri);

        // Set the cache control based on the request network policy.
        if (NetworkPolicy.readFromCache(networkPolicy)) {
            connection.setUseCaches(true);
        } else if (NetworkPolicy.isOfflineOnly(networkPolicy)) {
            connection.setUseCaches(true);
            connection.addRequestProperty(CACHE_CONTROL, CACHE_ONLY);
        } else {
            connection.setUseCaches(false);
            connection.addRequestProperty(CACHE_CONTROL, NO_CACHE);
        }

        // If the cache is being used then set the stale timeout.
        if (connection.getUseCaches()) {
            connection.addRequestProperty(
                    CACHE_CONTROL, MAX_STALE + DEFAULT_STALE_TIME);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode >= 300) {
            connection.disconnect();
            Log.w(TAG, "Download failed: HTTP response code was "
                    + responseCode);
            return null;
        }

        long contentLength = connection.getHeaderFieldInt(CONTENT_LENGTH, -1);

        if (DownloadManager.get().isLoggingEnabled()) {
            Log.d(TAG, "Content size = " + contentLength);

            boolean fromCache =
                    parseResponseSourceHeader
                            (connection.getHeaderField(RESPONSE_SOURCE));

            Log.d(TAG, "Content loaded from "
                    + (fromCache ? "HTTP CACHE" : "NETWORK")
                    + " (size = "
                    + contentLength + ")");
        }

        return connection.getInputStream();
    }

    /**
     * Opens a new URL connection using the connection and read timeouts defined
     * as constants fields.
     *
     * @param path The URL of the remote data target.
     * @return An HttpURLConnection
     * @throws IOException Exception is thrown if connection cannot be
     *                     established.
     */
    private static HttpURLConnection openConnection(Uri path)
            throws IOException {
        HttpURLConnection connection =
                (HttpURLConnection) new URL(path.toString()).openConnection();
        connection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT_MILLIS);
        connection.setReadTimeout(DEFAULT_READ_TIMEOUT_MILLIS);
        return connection;
    }

    /**
     * Parses the HTTP response header to determine if the response is from the
     * network or from the disk cache.
     *
     * @param header HTTP header to parse.
     * @return {@code true} if response is from the disk cache; {@code false} if
     * not.
     */
    private static boolean parseResponseSourceHeader(String header) {
        if (header == null) {
            return false;
        }
        String[] parts = header.split(" ", 2);
        if ("CACHE".equals(parts[0])) {
            return true;
        }
        if (parts.length == 1) {
            return false;
        }
        try {
            return "CONDITIONAL_CACHE".equals(parts[0])
                    && Integer.parseInt(parts[1])
                    == HttpURLConnection.HTTP_NOT_MODIFIED;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Creates and installs the HTTP response cache.
     *
     * @param context Application context.
     */
    private static void installCacheIfNeeded(Context context) {
        if (sResponseCache == null) {
            try {
                synchronized (sLock) {
                    if (sResponseCache == null) {
                        sResponseCache = ResponseCache.install(context);
                    }
                }
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Simple HTTP response cache implementation to reduce the number of web
     * hits when downloading remote data. The cache limits are defined as static
     * fields in the enclosing class.
     */
    private final static class ResponseCache {
        /**
         * Installs the response cache.
         *
         * @param context The application context.
         * @return An response cache instance.
         * @throws IOException
         */
        static Object install(Context context) throws IOException {
            File cacheDir = CacheUtils.createCacheDir(context);
            HttpResponseCache cache = HttpResponseCache.getInstalled();
            if (cache == null) {
                long maxSize = CacheUtils.calculateDiskCacheSize(cacheDir);
                cache = HttpResponseCache.install(cacheDir, maxSize);
            }
            return cache;
        }

        /**
         * Closes the installed response cache.
         *
         * @param cache The response cache to close.
         */
        static void close(Object cache) {
            if (cache != null) {
                try {
                    ((HttpResponseCache) cache).close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
