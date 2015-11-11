package work.t_s.shim0mura.havings.model;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by shim0mura on 2015/11/07.
 */
public class BusHolder extends Bus {
    private static Bus bus = new BusHolder();

    // mainスレッド以外でも動かす
    // http://stackoverflow.com/questions/15431768/how-to-send-event-from-service-to-activity-with-otto-event-bus
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override public void post(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(event);
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    BusHolder.super.post(event);
                }
            });
        }
    }

    public static Bus get() {
        return bus;
    }

}
