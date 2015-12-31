package work.t_s.shim0mura.havings;

import android.app.Application;

import timber.log.Timber;
import work.t_s.shim0mura.havings.util.ExtTree;

/**
 * Created by shim0mura on 2015/12/19.
 */
public class HavingsGlobal extends Application {

    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            Timber.plant(new ExtTree());
        } else {
            //Timber.plant(new MyCrashReportingTree());
        }

        super.onCreate();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
