package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

/**
 * Created by shim0mura on 2016/04/06.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class SearchResultEntity implements Serializable {

    public List<ItemEntity> items;
    public int searchType;
    public int totalCount;
    public int currentPage;
    public boolean hasNextPage;

}