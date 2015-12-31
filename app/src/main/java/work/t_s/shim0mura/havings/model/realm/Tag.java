package work.t_s.shim0mura.havings.model.realm;

import android.support.annotation.Nullable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by shim0mura on 2015/12/20.
 */
public class Tag extends RealmObject {
    @PrimaryKey
    private int id;

    private String name;
    private String yomiJp = "";
    private String yomiRoma = "";

    private int parentId;
    private int priority;
    private int nest;
    private int tagType;
    private boolean isDeleted;

    public Tag(){}

    public Tag(String n){ this.name = n; }

    public Tag(int i, String n, @Nullable String yj, @Nullable String yr){
        this.id = i;
        this.name = n;
        this.yomiJp = (yj == null) ? "" : yj;
        this.yomiRoma = (yr == null) ? "" : yr;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getYomiJp() {
        return yomiJp;
    }

    public String getYomiRoma() {
        return yomiRoma;
    }


    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setYomiJp(String yomiJp) {
        this.yomiJp = yomiJp;
    }

    public void setYomiRoma(String yomiRoma) {
        this.yomiRoma = yomiRoma;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getNest() {
        return nest;
    }

    public void setNest(int nest) {
        this.nest = nest;
    }

    public int getTagType() {
        return tagType;
    }

    public void setTagType(int tagType) {
        this.tagType = tagType;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
