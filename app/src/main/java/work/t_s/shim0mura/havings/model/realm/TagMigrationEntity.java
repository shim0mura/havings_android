package work.t_s.shim0mura.havings.model.realm;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by shim0mura on 2015/12/29.
 */
public class TagMigrationEntity extends RealmObject {

    private int migrationVersion;
    private RealmList<TagEntity> updatedTags;

    public TagMigrationEntity(){}

    public int getMigrationVersion() {
        return migrationVersion;
    }

    public RealmList<TagEntity> getUpdatedTags(){
        return updatedTags;
    }

    public void setMigrationVersion(int migrationVersion) {
        this.migrationVersion = migrationVersion;
    }
}
