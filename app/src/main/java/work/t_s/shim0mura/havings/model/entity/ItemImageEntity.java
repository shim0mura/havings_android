package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by shim0mura on 2015/12/10.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class ItemImageEntity implements Serializable {

    public static int MAX_MEMO_SIZE = 100;

    public int id;
    public int itemId;
    public String itemName;
    public String url;
    public String imageData;
    // 画像URLはシェア用にアイテムに紐付いたちゃんとしたURLを用意したい
    // それ用のpath
    // 後々urlと統合する予定
    public String path;

    @JsonFormat(shape=JsonFormat.Shape.STRING)
    public Date date;

    @JsonFormat(shape=JsonFormat.Shape.STRING)
    public Date addedDate;

    public String memo;

    public int imageFavoriteCount;
    public Boolean isFavorited;

    public String ownerName;
}
