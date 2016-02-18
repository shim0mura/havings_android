package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

/**
 * Created by shim0mura on 2016/01/30.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ItemImageListEntity implements Serializable {

    public List<ItemImageEntity> images;
    public boolean hasNextImage;

}
