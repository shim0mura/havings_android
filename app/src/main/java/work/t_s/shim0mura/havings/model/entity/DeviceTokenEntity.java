package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by shim0mura on 2016/08/07.
 */

@JsonIgnoreProperties(ignoreUnknown=true)
public class DeviceTokenEntity {

    public int userId;
    public String token;
    public int type;
    public boolean isEnable;
}
