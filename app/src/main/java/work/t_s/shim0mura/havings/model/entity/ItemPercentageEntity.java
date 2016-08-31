package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import work.t_s.shim0mura.havings.R;

/**
 * Created by shim0mura on 2016/03/23.
 */

@JsonIgnoreProperties(ignoreUnknown=true)
public class ItemPercentageEntity implements Serializable {

    public static Map<Integer, String> categoryName = new TreeMap<Integer, String>(){{
        put(0, "未分類");
        put(1, "衣");
        put(2, "食");
        put(3, "住");
        put(4, "その他");
    }};
    public static Map<Integer, Integer> categoryColor = new TreeMap<Integer, Integer>(){{
        put(0, R.color.categoryNone);
        put(1, R.color.categoryClothing);
        put(2, R.color.categoryFood);
        put(3, R.color.categoryLiving);
        put(4, R.color.categoryEtc);
    }};

    public String tag;
    public int tagId;
    public int count;
    public int percentage;
    public int type;

    public List<ItemPercentageEntity> childs;

}
