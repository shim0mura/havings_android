package work.t_s.shim0mura.havings.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;

import timber.log.Timber;
import work.t_s.shim0mura.havings.HomeActivity;
import work.t_s.shim0mura.havings.ItemActivity;
import work.t_s.shim0mura.havings.R;

import static work.t_s.shim0mura.havings.ItemActivity.ITEM_ID;

/**
 * Created by shim0mura on 2016/08/06.
 */

public class NotificationGcmListenerService extends GcmListenerService {

    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;

    private static final String MESSAGE_KEY = "message";
    private static final String TYPE_INT_KEY = "type";
    private static final String ITEM_ID_KEY = "id";
    private static final int TYPE_TIMER = 0;

    @Override
    public void onMessageReceived(String s, Bundle bundle) {
        Timber.d("gcm_message from: %s", s);

        super.onMessageReceived(s, bundle);

        String message = bundle.getString(MESSAGE_KEY);
        int type = bundle.getInt(TYPE_INT_KEY);

        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, HomeActivity.class);
        switch(type){
            case TYPE_TIMER:
                intent = new Intent(this, ItemActivity.class);
                int itemId = bundle.getInt(ITEM_ID_KEY);
                intent.putExtra(ITEM_ID, itemId);
                break;
            default:
                break;
        }

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        builder = new NotificationCompat.Builder(this)
                .setContentTitle("Havings")
                .setSmallIcon(R.drawable.list_icon_for_tab)
                .setContentText(message);

        builder.setContentIntent(contentIntent);
        notificationManager.notify(0, builder.build());
    }
}
