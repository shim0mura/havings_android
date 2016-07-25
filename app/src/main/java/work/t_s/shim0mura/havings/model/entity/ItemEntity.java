package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by shim0mura on 2015/12/06.
 */

@JsonIgnoreProperties(ignoreUnknown=true)
public class    ItemEntity implements Serializable {

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
    public int privateType;

    public int itemImageId;

    public String breadcrumb;
    public int favoriteCount;
    public Boolean isFavorited;
    public int commentCount;

    public int owningItemCount;
    public int imageCount;

    public int nest;

    public UserEntity owner;

    // tag_idをもったtagEntityに置き換える
    // TextViewのtag属性にtagのidを持たせる
    public List<String> tags;
    // POST,PUTするとき用 タグをひとまとめにしたもの
    public String tagList;

    //public List<ItemImageEntity> images;
    //public Boolean hasNextImage;
    public ItemImageListEntity itemImages;

    // 画像追加時のみに使用するやつ
    public List<ItemImageEntity> imageDataForPost;
    // 画像編集時のみに使用するやつ
    public Map<Integer, Map<String, String>> imageMetadataForUpdate;
    // 画像削除時のみに使用するやつ
    public List<Integer> imageDeleting;

    public List<ItemEntity> owningItems;
    public Boolean hasNextItem;

    public List<CountDataEntity> countProperties;

    public List<TimerEntity> timers;
    public Boolean canAddTimer;

    // dumpとdeleteの選択時に使う
    // listAdapter側での制御がうまく出来ないのでしょうがなくここで使用…
    public Boolean isSelectedForSomething;

    // dumpとdeleteの選択したものをpostする時に使う
    public List<Integer> fellowIds;

}
