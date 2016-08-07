package work.t_s.shim0mura.havings.util;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by shim0mura on 2016/08/06.
 */

public class NotificationInstanceIDListenerService extends InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        Intent intent = new Intent(this, NotificationGcmIntentService.class);
        startService(intent);
    }
}
