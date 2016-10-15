package vandy.mooc.assignments.framework.downloader;

/**
 * A enumerated set of network policies.
 * <p/>
 * Each enumerated value is defined as a unique set bit so that multiple
 * enumerated values can be OR'd together to form a single bit mask using the
 * buildMask() static helper method.
 */
@SuppressWarnings({"unused", "PointlessBitwiseExpression"})
public enum MemoryPolicy {
    /**
     * Skips memory cache lookup when processing a request.
     */
    SKIP_CACHE(1 << 0),

    /**
     * Skips storing the final result into memory cache.
     */
    NO_CACHE(1 << 1);

    /**
     * Convenience static method that takes a network policy mask and determines
     * if the the SKIP_CACHE policy is set.
     *
     * @param policy A set of network policies (mask)
     * @return {@code true} if SKIP_CACHE is set, false if not.
     */
    static boolean readFromCache(int policy) {
        return (policy & SKIP_CACHE.mValue) == 0;
    }

    /**
     * Convenience static method that takes a network policy mask and determines
     * if the the SKIP_CACHE policy is set.
     *
     * @param policy A set of network policies (mask)
     * @return {@code true} if STORE_CACHE is set, false if not.
     */
    static boolean writeToCache(int policy) {
        return (policy & NO_CACHE.mValue) == 0;
    }

    /**
     * The value of this enumerated instance. Each enumerated value is a unique
     * bit shift so that multiple enumerated values can be OR'd together to form
     * a single bit mask.
     */
    final int mValue;

    /**
     * Constructor.
     *
     * @param value The enumerated value for this object.
     */
    MemoryPolicy(int value) {
        this.mValue = value;
    }

    /**
     * Convenience helper that builds a bit mask from the passed policies. This
     * method is for internal framework use and is not intended to be used by
     * applications the use this framework.
     *
     * @param policies The policies to combine into a mMask.
     * @return An int bit mask containing the passed policies.
     */
    static int buildMask(MemoryPolicy... policies) {
        if (policies == null || policies.length == 0) {
            throw new IllegalArgumentException(
                    "Memory policy cannot be null.");
        }

        int mask = 0;

        for (MemoryPolicy policy : policies) {
            if (policy == null) {
                throw new IllegalArgumentException(
                        "Memory policy cannot be null.");
            }

            mask |= policy.mValue;
        }

        return mask;
    }
}

