package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by shim0mura on 2016/02/13.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class NotificationEntity implements Serializable {

    public int eventId;
    public String type;
    public boolean unread;
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    public Date date;

    public List<?> acter;

    public List<?> target;
}
