package work.t_s.shim0mura.havings.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import timber.log.Timber;
import work.t_s.shim0mura.havings.HomeActivity;
import work.t_s.shim0mura.havings.ItemActivity;
import work.t_s.shim0mura.havings.R;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static work.t_s.shim0mura.havings.ItemActivity.ITEM_ID;

/**
 * Created by shim0mura on 2016/08/06.
 */

public class NotificationFcmListenerService extends FirebaseMessagingService {

    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;

    private static final String MESSAGE_KEY = "message";
    private static final String TYPE_INT_KEY = "type";
    private static final String ITEM_ID_KEY = "id";
    private static final int TYPE_TIMER = 0;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Map<String, String> data = remoteMessage.getData();
        String message = data.get(MESSAGE_KEY);
        int itemId = Integer.valueOf(data.get(ITEM_ID_KEY));
        int typeId = Integer.valueOf(data.get(TYPE_INT_KEY));

        Timber.d("notification_list, %s %s", itemId, typeId);

        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, HomeActivity.class);
        switch(typeId){
            case TYPE_TIMER:
                intent = new Intent(this, ItemActivity.class);
                intent.putExtra(ITEM_ID, itemId);
                Timber.d("notification_extra %s", itemId);
                break;
            default:
                break;
        }

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, FLAG_UPDATE_CURRENT);

        builder = new NotificationCompat.Builder(this)
                .setContentTitle("Havings")
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.icon))
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setSmallIcon(R.drawable.list_icon)
                .setContentText(message);

        builder.setContentIntent(contentIntent);
        notificationManager.notify(0, builder.build());
    }
}
