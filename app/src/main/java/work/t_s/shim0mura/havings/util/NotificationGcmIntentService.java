package work.t_s.shim0mura.havings.util;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.squareup.otto.Subscribe;

import timber.log.Timber;
import work.t_s.shim0mura.havings.model.ApiKey;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.di.Api;
import work.t_s.shim0mura.havings.model.entity.DeviceTokenEntity;
import work.t_s.shim0mura.havings.model.event.DeviceTokenCheckEvent;
import work.t_s.shim0mura.havings.model.event.NotificationEvent;
import work.t_s.shim0mura.havings.presenter.HomePresenter;

/**
 * Created by shim0mura on 2016/08/06.
 */

public class NotificationGcmIntentService extends IntentService {

    public static final String SENDER_ID = "542779438450";

    public NotificationGcmIntentService(){
        super("notificationlistnerservice");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Timber.d("regist_token %s", token);
            DeviceTokenEntity deviceTokenEntity = new DeviceTokenEntity();
            deviceTokenEntity.token = token;
            BusHolder.get().post(new DeviceTokenCheckEvent(deviceTokenEntity));
        }catch(Exception e){
            Timber.d("regist_error");
            Timber.d(e.toString());
        }
    }

}
