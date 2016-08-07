package work.t_s.shim0mura.havings.model.event;

import work.t_s.shim0mura.havings.model.entity.DeviceTokenEntity;

/**
 * Created by shim0mura on 2016/08/07.
 */

public class DeviceTokenCheckEvent {

    public DeviceTokenEntity deviceTokenEntity;

    public DeviceTokenCheckEvent(DeviceTokenEntity deviceToken){
        deviceTokenEntity = deviceToken;

    }
}
