package vandy.mooc.assignments.assignment.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import vandy.mooc.assignments.framework.application.activities.MainActivityBase;

/**
 * This is the main activity class for the application and will be created and
 * be displayed when the application is first started. It extends the base class
 * MainActivityBase which supports add and download FABs and a fragment that
 * contains a RecyclerView that manages a list of strings. This base class also
 * manages all configuration changes and also saving and restoring the displayed
 * list of strings between application sessions.
 * <p/>
 * In addition to managing the string list, the base class expects this sub
 * class to handle creating intents that are used to start a GalleryActivity. In
 * some assignments, there will also be a requirement to have the
 * GalleryActivity return a result intent back to the MainActivity so that the
 * two activities can keep the displayed lists in sync.
 */
public class MainActivity extends MainActivityBase {
    /**
     * String used in logging output.
     */
    private static final String TAG = "MainActivity";

    /*
     * Activity lifecycle callback methods.
     */

    /**
     * Hook method called when a new instance of Activity is created. One time
     * initialization code goes here, e.g., UI layout initialization.
     *
     * @param savedInstanceState A Bundle object that contains saved state
     *                           information.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() called.");
        // Always call super class method first unless you need to request
        // window features using the requestWindowFeature() method which always
        // must precede any call to super.onCreate(). Note that the framework's
        // super class implementation will automatically load the XML layout for
        // this activity.
        super.onCreate(savedInstanceState);
    }

    /**
     * Template method hook method required fro starting a download operation.
     *
     * @param urls list of URLs to download.
     */
    @Override
    protected void startDownload(ArrayList<Uri> urls) {
        Intent intent = GalleryActivity.makeStartIntent(this, urls);
        startActivity(intent);
    }

    /**
     * Called from adapter when items are being pending loads. Loading is not
     * supported for a this adapter so we can safely ignore this callback (it
     * will never be made).
     *
     * @param notUsed not used
     */
    @Override
    public void onShowRefresh(boolean notUsed) {
    }

}
