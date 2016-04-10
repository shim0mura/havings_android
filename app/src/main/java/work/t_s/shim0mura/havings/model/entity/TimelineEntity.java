package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.List;

/**
 * Created by shim0mura on 2016/04/10.
 */

@JsonIgnoreProperties(ignoreUnknown=true)
public class TimelineEntity  implements Serializable {
    public List<NotificationEntity> timeline;
    public boolean hasNextEvent;
}
