package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by shim0mura on 2015/12/29.
 */

@JsonIgnoreProperties(ignoreUnknown=true)
public class TagMigrationEntity {

    public int migrationVersion;
    public List<TagEntity> updatedTags;

}
