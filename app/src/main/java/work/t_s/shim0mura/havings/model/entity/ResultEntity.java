package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by shim0mura on 2016/01/24.
 */

@JsonIgnoreProperties(ignoreUnknown=true)
public class ResultEntity {
    public int resultType;
    public int relatedId;
}
