package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

/**
 * Created by shim0mura on 2016/02/08.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class FavoriteItemImageListEntity implements Serializable {

    public List<ItemImageEntity> images;
    public int nextPageForImage;
    public boolean hasNextImage;

}