package work.t_s.shim0mura.havings.util;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import timber.log.Timber;
import work.t_s.shim0mura.havings.model.ApiKey;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.entity.DeviceTokenEntity;
import work.t_s.shim0mura.havings.model.event.DeviceTokenCheckEvent;

/**
 * Created by shim0mura on 2016/08/06.
 */

public class NotificationInstanceIDListenerService extends FirebaseInstanceIdService {

    /*
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        Intent intent = new Intent(this, NotificationGcmIntentService.class);
        startService(intent);
    }
    */

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Timber.d("refresh_token %s", refreshedToken);

        String key = ApiKey.getSingleton(this).getDeviceToken();
        Timber.d("refresh_token current_key %s", key);

        if(key == null || !key.equals(refreshedToken)){
            ApiKey.getSingleton(this).updateTmpDeviceToken(refreshedToken);
        }

    }
}
