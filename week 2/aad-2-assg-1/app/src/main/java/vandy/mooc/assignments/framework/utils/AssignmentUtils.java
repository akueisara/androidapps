package vandy.mooc.assignments.framework.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import vandy.mooc.assignments.R;

import static android.text.Html.FROM_HTML_MODE_COMPACT;

/**
 * Class containing static helper methods to perform assignment related tasks.
 */
public final class AssignmentUtils {
    /**
     * Flag indicating whether or not the assignment switching submenu should be
     * displayed and supported.
     */
    public static final boolean ASSIGNMENT_SWITCHING = false;
    /**
     * The assignment identifier (only used by assignment package). When
     * dynamic assignment switching is enabled, the assignment runner will
     * allow the application to run any assignment that is less than or equal
     * to the ASSIGNMENT value. When dynamic assignment switching is disabled,
     * the assignment runner will only allow the specified ASSIGNMENT number
     * to be run.
     */
    private static final int ASSIGNMENT = 1;
    /**
     * Preference key for looking up assignment version.
     */
    private static final String PREF_ASSIGNMENT_VERSION =
            "pref_assignment_version";
    /**
     * Custom menu action (menu) ids used for dynamic menu support.
     */
    private static final int ACTION_ABOUT = 10;
    private static final int ACTION_ASSIGNMENTS = 11;
    /**
     * The assignment submenu needs a group id.
     */
    private static final int SUBMENU_GROUP_ID = 1;

    /**
     * Ensure this class is only used as a utility.
     */
    private AssignmentUtils() {
        throw new AssertionError();
    }

    /*
     * Activity lifecycle method helpers.
     */

    /**
     * Hook method helper that should be called from onCreateOptionsMenu() in
     * all assignment activities. This helper creates and adds menu items to the
     * passed menu. If the calling activity is the main activity, this method
     * will add the assignment choices submenu as well as an about menu item. If
     * the calling activity is not main activity, this method only adds the
     * about menu item. Note that if only 1 assignment can be run by the current
     * code base, no assignments submenu is added.
     *
     * @param activity Any activity instance.
     * @param menu     The menu that is being created.
     */
    public static void onCreateOptionsMenu(Activity activity, Menu menu) {
        // If this is not the first assignment, then add an assignments
        // submenu containing a list of checkable assignments that
        // can be run with this code base (any assignment number less
        // than or equal to the current assignment code base).
        //noinspection PointlessBooleanExpression,ConstantConditions
        if (showAssignmentSubMenu(activity)) {
            // Add the submenu. The title is set dynamically to reflect
            // the current assignment choice in onPrepareOptionsMenu.
            final SubMenu subMenu =
                    menu.addSubMenu(SUBMENU_GROUP_ID,
                                    ACTION_ASSIGNMENTS, Menu.NONE,
                                    null);

            // Now add each supported assignment to the submenu.
            for (int i = 1; i <= ASSIGNMENT; i++) {
                final String name =
                        AssignmentUtils.getAssignmentTitle(activity, i);
                subMenu.add(SUBMENU_GROUP_ID, i, i, name).setCheckable(true);
            }
        }

        // Now add the about menu item to end of the main menu.
        menu.add(0,
                 ACTION_ABOUT,
                 0xffff,
                 activity.getString(R.string.action_about));
    }

    /**
     * Hook method helper that should be called from onPrepareOptionsMenu()
     * method in all assignment activities. When called, we update the
     * Assignment submenu title to reflect the currently running assignment
     * selection and also check the appropriate submenu item (radio button
     * group).
     *
     * @param activity Any assignment activity.
     * @param menu     The menu that is about to be displayed.
     * @return {@code true} if the menu was updated; {@code false} if not.
     */
    @SuppressWarnings("UnusedReturnValue")
    public static boolean onPrepareOptionsMenu(Activity activity, Menu menu) {
        // When the code base supports more than one assignment,
        // then the assignment submenu is visible and set to
        // the currently running assignment selection.
        //noinspection PointlessBooleanExpression,ConstantConditions
        if (showAssignmentSubMenu(activity)) {
            // Find the assignments submenu.
            final MenuItem menuItem = menu.findItem(ACTION_ASSIGNMENTS);

            // Set its title to reflect the currently selected assignment.
            menuItem.setTitle(AssignmentUtils.getAssignmentTitle(activity));

            // Now make sure that the currently running assignment
            // submenu item is appropriately checked in this submenu.
            final SubMenu subMenu = menuItem.getSubMenu();

            // Set the the submenu to support a single checkable item.
            subMenu.setGroupCheckable(SUBMENU_GROUP_ID, true, true);
            final MenuItem subMenuItem =
                    subMenu.findItem(
                            AssignmentUtils.getAssignment(activity));

            // Finally, check the currently running assignment submenu item.
            subMenuItem.setChecked(true);

            // Return true to indicate that the menu was updated.
            return true;
        }

        // Return false to indicate that the menu was not updated.
        return false;
    }

    /**
     * Hook method helper that should be called from onOptionsMenuSelected() in
     * all assignment activities.
     *
     * @param activity An activity instance.
     * @param item     The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to proceed,
     * true to consume it here.
     */
    public static boolean onOptionsItemSelected(
            Activity activity,
            MenuItem item) {
        if (item.getItemId() == ACTION_ABOUT) {
            // Just show the appropriate about dialog for the
            // currently selected assignment.
            AssignmentUtils.showAboutDialog(activity);

            // Signal to caller that the command was handled.
            return true;
        } else if (showAssignmentSubMenu(activity)) {
            if (item.getItemId() == ACTION_ASSIGNMENTS) {
                // The menu framework code always forces a submenu header
                // when ever the parent menu item's title is set. To work
                // around this behaviour, force the header to be removed
                // each time the submenu is about to be displayed.
                item.getSubMenu().clearHeader();
            } else if (item.getGroupId() == SUBMENU_GROUP_ID) {
                // The assignment number is the menu item id.
                int assignment = item.getItemId();

                // Check if selected assignment is supported by this code base.
                if (AssignmentUtils.canRunAssignment(assignment)) {

                    // Sanity check: only allow changing assignments
                    // from the main activity.
                    if (!isMainActivity(activity)) {
                        throw new IllegalStateException(
                                "Assignments can only be changed from"
                                        + " the"
                                        + " main "
                                        + "activity");
                    }

                    // Check the submenu item and set the new assignment
                    // version.
                    item.setChecked(true);
                    AssignmentUtils.setAssignment(activity, assignment);

                    // Finally, set the action bar title to reflect the change.
                    setAppTitle(activity);
                }

                return true;
            }
        }

        // Signals that we did not recognize this menu item.
        return false;
    }

    /**
     * Helper method for updating action bar title to reflect the currently
     * running assignment number.
     *
     * @param activity An activity instance.
     */
    public static void setAppTitle(Activity activity) {
        ActionBar actionBar =
                ((AppCompatActivity) activity).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(
                    activity.getTitle()
                            + " - "
                            + AssignmentUtils.getAssignmentTitle(activity));
        }
    }

    /**
     * Helper to determine if the this activity is the main activity. We only
     * allow the user to switch assignment numbers from the main activity so
     * that the application never gets into a confused state. Normally, we
     * should check if the activity category is Intent.CATEGORY_LAUNCHER, but
     * this prevents Espresso tests from working because they do not set the
     * category when starting the main activity.
     *
     * @param activity An activity instance.
     * @return {@code true} if this is the main activity; {@code false} if not.
     */
    private static boolean isMainActivity(Activity activity) {
        return activity.getIntent().getAction() != null
                && activity.getIntent().getAction().equals(Intent.ACTION_MAIN);
    }

    /**
     * Helper used to throw assertions based the current assignment version. Use
     * this call when you want to ensure that a method or block of code is only
     * executed when the application is set to a specific assignment version.
     *
     * @param context Any context
     * @param numbers The assignment numbers to check against.
     */
    @SuppressWarnings("unused")
    public static void assertIsAssignment(Context context, Integer... numbers)
            throws IllegalStateException {

        final int current = getAssignment(context);

        for (int number : numbers) {
            if (current == number) {
                return;
            }
        }

        throw new IllegalStateException
                ("Current assignment is set to "
                         + current
                         + " but code"
                         + " for assignment "
                         + numbers[0]
                         + " is being used");
    }

    /**
     * Determines if the passed assignment number matches the currently running
     * assignment.
     *
     * @param context Any context.
     * @param numbers Assignment versions to check.
     * @return {@code true} if the passed version matches the currently running
     * version.
     */
    @SuppressWarnings("unused")
    public static boolean isAssignment(Context context, Integer... numbers) {
        final int current = getAssignment(context);

        for (int number : numbers) {
            if (current == number) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the current assignment version which is kept up to date in the
     * shared preferences. This functionality is only supported when the
     * application supports dynamic assignment switching. When dynamic
     * assignment switch is disabled, the current assignment is always
     * determined by the static constant ASSIGNMENT.
     *
     * @param context Any context.
     * @return the currently running assignment version
     */
    public static int getAssignment(Context context) {
        if (ASSIGNMENT_SWITCHING) {
            SharedPreferences preferences =
                    context.getSharedPreferences(null, Context.MODE_PRIVATE);

            int assignment = preferences.getInt
                    (PREF_ASSIGNMENT_VERSION, ASSIGNMENT);

            // Ensure that the restored assignment number does not
            // exceed the maximum supported assignment number for
            // this code base (set in Constants.java).
            return Math.min(assignment, ASSIGNMENT);
        } else {
            return ASSIGNMENT;
        }
    }

    /**
     * Switches to the specified assignment version by saving the version in
     * shared preferences.
     *
     * @param context    Any context.
     * @param assignment The new assignment version to set
     */
    @SuppressWarnings("WeakerAccess")
    public static void setAssignment(Context context, int assignment) {
        if (!canRunAssignment(assignment)) {
            throw new IllegalArgumentException
                    ("Assignment "
                             + assignment
                             + " is not supported by this code base.");
        }

        SharedPreferences preferences =
                context.getSharedPreferences(null, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREF_ASSIGNMENT_VERSION, assignment);
        editor.apply();
    }

    /**
     * Checks to see if the specified assignment can be run using the current
     * code base. The current implementation allows running any assignment less
     * that or equal to the ASSIGNMENT number specified the Constants.
     *
     * @param assignment The assignment number to check.
     * @return {@code true} if the code base supports running this assignment;
     * {@code false} if not.
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean canRunAssignment(int assignment) {
        return ASSIGNMENT_SWITCHING
               ? 1 <= assignment && assignment <= ASSIGNMENT
               : assignment == ASSIGNMENT;
    }

    /**
     * Convenience method that shows an about dialog for this application. It
     * has been placed in this ViewUtils class so that it can be easily accessed
     * from any activity.
     */
    private static void showAboutDialog(Context context) {
        final AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(context);

        // Set dialog title.
        alertDialogBuilder.setTitle(getAssignmentTitle(context));

        // This is a nice trick for loading an HTML string that will
        // support a limited subset of HTML tags.
        //
        // See: http://stackoverflow.com/a/3150456/4381679
        final Spanned spanned;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            spanned = Html.fromHtml(getAssignmentAbout(context),
                                    FROM_HTML_MODE_COMPACT);
        } else {
            spanned = Html.fromHtml(getAssignmentAbout(context));
        }

        // Set dialog message.
        alertDialogBuilder.setMessage(spanned);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton
                (R.string.ok_button,
                 new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         dialog.cancel();
                     }
                 });

        // Create alert dialog.
        final AlertDialog alertDialog = alertDialogBuilder.create();

        // Show it.
        alertDialog.show();
    }

    /**
     * Helper that returns the title for the current assignment.
     *
     * @param context Any context.
     * @return The title of the currently selected assignment.
     */
    @SuppressWarnings("WeakerAccess")
    public static String getAssignmentTitle(Context context) {
        return getAssignmentTitle(context, getAssignment(context));
    }

    /**
     * Helper that returns the title for the specified assignment.
     *
     * @param context    Any context.
     * @param assignment The assignment number for the title.
     * @return A string title suitable for a menu item or action bar title.
     */
    private static String getAssignmentTitle(Context context, int assignment) {
        final String[] stringArray =
                context.getResources().getStringArray(R.array.assignment_names);

        if (assignment - 1 < stringArray.length) {
            return stringArray[assignment - 1];
        } else {
            return "No Title";
        }
    }

    /**
     * Helper that returns the about html text for the current assignment.
     *
     * @param context Any context.
     * @return The about html text of the currently selected assignment.
     */
    private static String getAssignmentAbout(Context context) {
        final int assignment = getAssignment(context);

        final String[] stringArray =
                context.getResources().getStringArray
                        (R.array.about_assignments);

        if (assignment - 1 < stringArray.length) {
            return stringArray[assignment - 1];
        } else {
            return "No Title";
        }
    }

    /**
     * Returns the maximum assignment number than can be run with this code
     * base.
     *
     * @return The maximum assignment number.
     */
    public static int getMaxAssignment() {
        return ASSIGNMENT;
    }

    /**
     * Returns a flag indicating if the assignment switching submenu should be
     * displayed. This flag does not affect the about menu item which is always
     * available and shows the html about text that matches the currently
     * running assignment version.
     *
     * @return {@code true} if assignment switching is supported, {@code false}
     * if not.
     */
    public static boolean showAssignmentSubMenu(Activity activity) {
        return isMainActivity(activity) && ASSIGNMENT_SWITCHING
                && ASSIGNMENT > 1;
    }
}
