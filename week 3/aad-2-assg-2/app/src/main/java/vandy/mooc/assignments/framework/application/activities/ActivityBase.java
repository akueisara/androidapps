package vandy.mooc.assignments.framework.application.activities;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;

import vandy.mooc.assignments.framework.utils.AssignmentUtils;
import vandy.mooc.assignments.framework.utils.PermissionUtils;

/**
 * This class can be used as the base class for a set of MOOC assignments that
 * are designed to be runnable from a single code base. It handles all the
 * required menu items and actions to support switching between different
 * assignment solutions. When an assignment number is chosen from the provided
 * assignments submenu, the assignment number is saved to shared preferences
 * where it can then be retrieved from anywhere in the application by simply
 * calling AssignmentUtils.getAssignment(). AssignmentUtils contains a number of
 * support methods used by this class and any code base that supports this
 * framework.
 * <p>
 * The UI component of this Assignment runner framework can be made unavailable
 * to the user by simply not having the MainActivity extent this class.
 * <p>
 * This class could be easily eliminated by moving it to a "helper" class that
 * is called from the 3 hook callbacks in the MainActivity, but the current
 * sub-classing approach makes it easy to add new features that are
 * transparently available to existing and future assignments without requiring
 * assignment modifications.
 */
public abstract class ActivityBase extends LifecycleLoggingActivity {
    /**
     * Handle the onPostCreate() hook to call permission helper to handle all
     * permission requests using the API 23 permission model framework.
     * <p>
     * The framework will callback to request this application to provide a
     * descriptive reason for the permission request that is then displayed to
     * the user. The user has the opportunity to grant or deny the permission
     * request. The callback is also handled automatically by the permission
     * helper class.
     *
     * @param savedInstanceState A saved state or null.
     */
    @CallSuper
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        // Call permission helper to manage all API 23 permission requests.
        PermissionUtils.onPostCreate(this, savedInstanceState);

        // Always call super class method.
        super.onPostCreate(savedInstanceState);
    }

    /**
     * API 23 (M) callback received when a permissions request has been
     * completed. Redirect request to permission helper.
     */
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Redirect hook call to permission helper method.
        PermissionUtils.onRequestPermissionsResult(this,
                                                   requestCode,
                                                   permissions,
                                                   grantResults);
    }

    /**
     * Hook method called by Android lifecycle framework. Here we call the
     * assignment helper to set the application title to display the currently
     * running assignment number.
     */
    @Override
    protected void onResume() {
        // Call assignment helper to update the activity action bar title.
        AssignmentUtils.setAppTitle(this);

        // Always call super class method.
        super.onResume();
    }

    /**
     * Hook method called to create and add menu items to the passed
     * menu.
     *
     * @param menu The menu to update.
     * @return We pass control to the super method to allow it to make its own
     * menu updates and decide on a return value.
     */
    @CallSuper
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Call assignment utils helper to add assignment related menu items.
        AssignmentUtils.onCreateOptionsMenu(this, menu);

        // Always call super class method.
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Hook method called just before the menu is displayed. Passes control to
     * the assignment helper to add assignment related menu items.
     *
     * @param menu The menu that is about to be displayed.
     * @return We pass control to the super method to allow it to make its own
     * menu updates and decide on a return value.
     */
    @CallSuper
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Call assignment utils helper to update assignment related menu items.
        AssignmentUtils.onPrepareOptionsMenu(this, menu);

        // Always call super class method.
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to proceed,
     * true to consume it here.
     */
    @CallSuper
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int assignmentBefore = AssignmentUtils.getAssignment(this);

        // Call assignment utils helper to update assignment related menu items.
        if (AssignmentUtils.onOptionsItemSelected(this, item)) {
            // Notify sub-class if there was a change.
            int assignmentAfter = AssignmentUtils.getAssignment(this);
            if (assignmentBefore != assignmentAfter) {
                onAssignmentChanged(assignmentBefore, assignmentAfter);
            }

            // Signal that we have handled this menu command.
            return true;
        }

        // Call the super class to allow it to handle it's own menu items.
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when the assignment number has changed to a new one. The default
     * does nothing.
     */
    void onAssignmentChanged(int from, int to) {
    }
}
