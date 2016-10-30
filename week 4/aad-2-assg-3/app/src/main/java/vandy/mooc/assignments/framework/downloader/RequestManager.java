package vandy.mooc.assignments.framework.downloader;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manages the lifecycle of all requests. When a request is started it is added
 * to it's context's request list. When an request is cancelled, completed, or
 * fails, it is removed from its context's request list. If the context is
 * destroyed while any of its asynchronous requests are still running, these
 * requests are immediately cancelled. Note that when an activity is destroyed
 * due to a configuration change, its requests are not cancelled so that the may
 * have the chance to cache their results event though they will be unable to
 * post those results to any request target. When the activity is recreated
 * after a configuration change, any cached resources that completed while the
 * activity was destroyed, will be made available for faster loading.
 */
class RequestManager
        implements Application.ActivityLifecycleCallbacks {
    /**
     * Logging tag.
     */
    private static final String TAG = "RequestManager";
    /**
     * The application context.
     */
    private final Application mApplication;
    /**
     * Maps requests to activities.
     */
    private HashMap<Context, List<Request>> mActivityRequestMap;

    public RequestManager(Context context) {
        mApplication = (Application) context.getApplicationContext();
        mActivityRequestMap = new HashMap<>();
    }

    /**
     * Registers the class as an application wide activity lifecycle listener.
     */
    public void startup() {
        if (mActivityRequestMap.size() != 0) {
            throw new IllegalStateException(
                    "startup should only be called once");
        }

        mApplication.registerActivityLifecycleCallbacks(this);
    }

    /**
     * Unregisters this class as an application wide activity lifecycle listener
     * and then cancels all outstanding requests.
     */
    public void shutdown() {
        mApplication.unregisterActivityLifecycleCallbacks(this);
        cancelAllRequests();
    }

    /**
     * Adds a request to the context's list.
     *
     * @param request A request.
     */
    public void addRequest(Request request) {
        List<Request> requests = getRequests(request.mContext);
        requests.add(request);
        mActivityRequestMap.put(request.mContext, requests);
    }

    /**
     * Cancels all requests for all contexts.
     */
    private void cancelAllRequests() {
        for (Context context : mActivityRequestMap.keySet()) {
            cancelRequests(context);
        }
    }

    /**
     * Cancels all requests for a single context.
     *
     * @param context A context.
     */
    private void cancelRequests(Context context) {
        List<Request> requests = mActivityRequestMap.remove(context);
        if (requests != null) {
            Log.d(TAG, "Cancelling "
                    + requests.size()
                    + " requests for context "
                    + context);
            cancelRequests(requests);
        }
    }

    /**
     * Helper method that cancels a list of requests.
     *
     * @param requests A list of requests.
     */
    private void cancelRequests(@NonNull List<Request> requests) {
        for (final Request request : requests) {
            request.cancel();
        }
    }

    /**
     * Helper method to return a context's request list.
     *
     * @param context A list of requests.
     */
    @NonNull
    private List<Request> getRequests(Context context) {
        List<Request> requests = mActivityRequestMap.get(context);
        return requests != null ? requests : new ArrayList<Request>();
    }

    /**
     * Checks if a given request is currently attached to the specified
     * context.
     *
     * @param context The context to search in.
     * @param request The request to search for.
     * @return {@code true} if the context request list contains this request,
     * {@code false} if not.
     */
    public boolean hasRequest(Context context, Request request) {
        return getRequests(context).contains(request);
    }

    /**
     * Determines the number of active requests for the specified context. This
     * method can be used by UI components such as progress bars etc.
     *
     * @param context A context.
     * @return The number of active request for the specified context.
     */
    public int getRequestCount(Context context) {
        List<Request> requests = getRequests(context);
        return requests.size();
    }

    /**
     * Removes the request from the context request map and removes the context
     * from the map if its list becomes empty.
     *
     * @param request The request to recycle.
     */
    public void recycleRequest(Request request) {
        List<Request> requests = getRequests(request.mContext);
        if (requests.contains(request)) {
            // Remove the request.
            requests.remove(request);
            if (requests.isEmpty()) {
                // Remove the context.
                mActivityRequestMap.remove(request.mContext);
            }

            return;
        }

        // Since this request was never bound to a context, make sure that
        // it wasn't started. Started requests are required to be bound
        // a context by calling addRequest().
        if (request.hasStarted()) {
            throw new IllegalStateException(
                    "Attempt to recycle an unbound request");
        }
    }

    /*
     * Activity lifecycle callbacks.
     */

    @Override
    public void onActivityCreated(
            Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(
            Activity activity, Bundle outState) {
    }

    /**
     * Hook method called when any activity is destroyed.
     *
     * @param activity An activity that is being destroyed.
     */
    @Override
    public void onActivityDestroyed(Activity activity) {
        // Allow downloads to continue during a configuration change so that
        // they have the opportunity to cache their results even if they will
        // be unable to post those results to request targets.
        if (!activity.isChangingConfigurations()) {
            cancelRequests(activity);
        }
    }
}
