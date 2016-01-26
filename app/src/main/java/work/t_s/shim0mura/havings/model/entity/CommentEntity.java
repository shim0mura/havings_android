package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by shim0mura on 2016/01/26.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class CommentEntity implements Serializable {

    public int id;
    public int itemId;
    public String content;
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    public Date commentedDate;
    public UserEntity commenter;
    public boolean canDelete;
    public boolean isDeleted;

}
