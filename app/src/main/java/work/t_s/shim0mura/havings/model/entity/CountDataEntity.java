package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

/**
 * Created by shim0mura on 2015/12/12.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class CountDataEntity {

    public int count;
    public int[] events;

    @JsonFormat(shape=JsonFormat.Shape.STRING)
    public Date date;

}
