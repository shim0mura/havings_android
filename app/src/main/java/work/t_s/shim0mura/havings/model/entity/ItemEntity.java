package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by shim0mura on 2015/12/06.
 */

@JsonIgnoreProperties(ignoreUnknown=true)
public class ItemEntity {

    public int id;
    public String name;
    public String description;
    public String thumbnail;
    public Boolean isList;
    public Boolean isGarbage;
    public String garbageReason;
    public int count;

    public String breadcrumb;
    public int favoriteCount;
    public Boolean isFavorited;
    public int commentCount;

    public int owningItemCount;
    public int imageCount;

    public UserEntity owner;

    // tag_idをもったtagEntityに置き換える
    // TextViewのtag属性にtagのidを持たせる
    public List<String> tags;

    public List<ItemImageEntity> images;
    public Boolean hasNextImage;

    public List<ItemEntity> owningItems;
    public Boolean hasNextItem;

    public List<CountDataEntity> countProperties;

}
