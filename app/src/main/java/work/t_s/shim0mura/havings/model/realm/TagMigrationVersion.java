package work.t_s.shim0mura.havings.model.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by shim0mura on 2015/12/29.
 */
public class TagMigrationVersion extends RealmObject {

    @PrimaryKey
    private int currentVersion;

    public TagMigrationVersion(){}

    public int getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(int currentVersion) {
        this.currentVersion = currentVersion;
    }
}
