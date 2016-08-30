package work.t_s.shim0mura.havings;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

import timber.log.Timber;
import work.t_s.shim0mura.havings.util.ExtTree;

/**
 * Created by shim0mura on 2015/12/19.
 */
public class HavingsGlobal extends MultiDexApplication {


    // http://stackoverflow.com/questions/36785014/the-number-of-method-references-in-a-dex-file-cannot-exceed-64k-api-17
    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            Timber.plant(new ExtTree());
        } else {
            //Timber.plant(new MyCrashReportingTree());
            Timber.plant(new Timber.DebugTree());
            Timber.plant(new ExtTree());
        }

        super.onCreate();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
