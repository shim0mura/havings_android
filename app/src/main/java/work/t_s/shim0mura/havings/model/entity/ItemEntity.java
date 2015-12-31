package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

/**
 * Created by shim0mura on 2015/12/06.
 */

@JsonIgnoreProperties(ignoreUnknown=true)
public class ItemEntity implements Serializable {

    public int id;
    public String name;
    public String description;
    public String image;
    public String thumbnail;
    public Boolean isList;
    public int listId;
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
    // POST,PUTするとき用 タグをひとまとめにしたもの
    public String tagList;

    public List<ItemImageEntity> images;
    public Boolean hasNextImage;

    // 画像追加時に使用するやつ
    public List<ItemImageEntity> imageDataForPost;

    public List<ItemEntity> owningItems;
    public Boolean hasNextItem;

    public List<CountDataEntity> countProperties;

}
