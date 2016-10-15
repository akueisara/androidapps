package vandy.mooc.assignments.framework.application;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

public class DownloadApplication extends Application {
    /**
     * For enabling/disabling strict mode.
     */
    private static final boolean DEVELOPER_MODE = false;
    /**
     * Static self-reference for static helpers.
     */
    private static DownloadApplication sInstance;

    /**
     * Save application instance in static for easy access.
     */
    public DownloadApplication() {
        sInstance = this;
    }

    /**
     * Returns application instance.
     *
     * @return The application instance.
     */
    public static DownloadApplication getInstance() {
        return sInstance;
    }

    /**
     * Convenience method that returns the application context. Note that the
     * application context does not provide theme support.
     *
     * @return The application context.
     */
    public static Context getContext() {
        return sInstance.getApplicationContext();
    }

    /**
     * Hook method overridden to install debug options.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(
                    new StrictMode.ThreadPolicy.Builder()
                            .detectDiskReads()
                            .detectDiskWrites()
                            //.detectNetwork()
                            .detectAll()
                            .penaltyLog()
                            .build());
            StrictMode.setVmPolicy(
                    new StrictMode.VmPolicy.Builder()
                            .detectLeakedSqlLiteObjects()
                            .detectLeakedClosableObjects()
                            .penaltyLog()
                            .penaltyDeath()
                            .build());
        }
    }
}
