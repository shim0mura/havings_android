package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

/**
 * Created by shim0mura on 2016/02/08.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class FavoriteItemListEntity implements Serializable {

    public List<ItemEntity> owningItems;
    //public int lastFavoriteId;
    //public boolean hasNextItem;

    public boolean hasNextItem;
    public int nextPageForItem;

}
