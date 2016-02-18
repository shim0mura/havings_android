package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by shim0mura on 2015/12/16.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class EventEntity implements Serializable{

    public static final String EVENT_TYPE_ADD_ITEM = "create_item";
    public static final String EVENT_TYPE_ADD_LIST = "create_list";
    public static final String EVENT_TYPE_ADD_IMAGE = "add_image";
    public static final String EVENT_TYPE_DUMP_ITEM = "dump";

    public int id;
    public String eventType;
    public Date date;
    public ItemEntity item;

}
