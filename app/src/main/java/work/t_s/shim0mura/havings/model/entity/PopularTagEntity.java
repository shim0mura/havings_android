package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

/**
 * Created by shim0mura on 2016/04/09.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class PopularTagEntity implements Serializable {

    public int tagId;
    public int tagCount;
    public String tagName;

    public List<ItemEntity> items;

}
