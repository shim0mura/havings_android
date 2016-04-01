package work.t_s.shim0mura.havings.model.event;

import java.util.ArrayList;

import work.t_s.shim0mura.havings.model.entity.NotificationEntity;

/**
 * Created by shim0mura on 2016/03/25.
 */
public class NotificationEvent {

    public ArrayList<NotificationEntity> notificationEntities;

    public NotificationEvent(ArrayList<NotificationEntity> notifications){
        notificationEntities = notifications;
    }
}
