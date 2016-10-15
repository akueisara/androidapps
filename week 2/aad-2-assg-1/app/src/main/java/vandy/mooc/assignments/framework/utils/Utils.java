package vandy.mooc.assignments.framework.utils;

import android.os.Looper;

/**
 * A utility class containing miscellaneous helper methods.
 */
public final class Utils {
    /**
     * Ensure this class is only used as a utility.
     */
    private Utils() {
        throw new AssertionError();
    }

    /**
     * Throws an {@link java.lang.IllegalStateException} if not called on the
     * main thread.
     */
    public static void assertMainThread() {
        if (!isOnMainThread()) {
            throw new IllegalStateException(
                    "Method must be called on the main thread");
        }
    }

    /**
     * Throws an {@link java.lang.IllegalStateException} if called on the main
     * thread.
     */
    public static void assertBackgroundThread() {
        if (!isOnBackgroundThread()) {
            throw new IllegalStateException(
                    "Method must be called on a background thread");
        }
    }

    /**
     * Returns {@code true} if called on the main thread, {@code false}
     * otherwise.
     */
    @SuppressWarnings({"BooleanMethodIsAlwaysInverted", "WeakerAccess"})
    public static boolean isOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    /**
     * Returns {@code true} if called on the main thread, {@code false}
     * otherwise.
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean isOnBackgroundThread() {
        return !isOnMainThread();
    }
}
