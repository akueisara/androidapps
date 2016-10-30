package vandy.mooc.assignments.framework.downloader;

/**
 * States for the lifecycle of a request. The order here reflects the
 * lifecycle time line and can be used for ordinal comparisons.
 */
public enum RequestState {
    // Lifecycle states
    RECYCLED,
    CREATED,
    STARTED,
    RUNNING,
    PAUSED,
    FINISHED;

    static boolean isRunning(RequestState state) {
        return (state.ordinal() >= RequestState.STARTED.ordinal()
                && state.ordinal() < RequestState.FINISHED.ordinal());
    }

    static boolean hasFinished(RequestState state) {
        return (state == FINISHED);
    }
}
