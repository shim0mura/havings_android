package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * Created by shim0mura on 2015/12/10.
 */
public class ItemImageEntity {

    public int id;
    public String url;

    @JsonFormat(shape=JsonFormat.Shape.STRING)
    public Date date;
}
