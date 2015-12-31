package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by shim0mura on 2015/12/10.
 */
public class ItemImageEntity implements Serializable {

    public int id;
    public String url;
    public String imageData;

    @JsonFormat(shape=JsonFormat.Shape.STRING)
    public Date date;
}
