package vandy.mooc.assignments.framework.utils;

/**
 * A utility class that provides some common data and state validation methods
 * that will throw exceptions if preconditions are not met. These methods are
 * a small subset of those provided in Google Guava.
 */
@SuppressWarnings("unused")
public final class Preconditions {
    /**
     * Ensure this class is only used as a utility.
     */
    private Preconditions() {
        throw new AssertionError();
    }

    public static void checkArgument(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void checkState(boolean expression, String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }

    public static <T> T checkNotNull(T arg) {
        return checkNotNull(arg, "Argument must not be null");
    }

    @SuppressWarnings({"SameParameterValue", "WeakerAccess"})
    public static <T> T checkNotNull(T arg, String message) {
        if (arg == null) {
            throw new NullPointerException(message);
        }
        return arg;
    }
}
