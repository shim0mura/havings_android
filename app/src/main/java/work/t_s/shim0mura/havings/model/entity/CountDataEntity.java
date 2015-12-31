package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by shim0mura on 2015/12/12.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class CountDataEntity implements Serializable {

    public int count;
    public int[] eventIds;
    public List<EventEntity> events;

    @JsonFormat(shape=JsonFormat.Shape.STRING)
    public Date date;

}
