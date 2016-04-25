package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by shim0mura on 2016/04/24.
 */

@JsonIgnoreProperties(ignoreUnknown=true)
public class TaskEntity  implements Serializable {

    public TimerEntity timer;

    @JsonFormat(shape=JsonFormat.Shape.STRING)
    public List<Date> events;

}
