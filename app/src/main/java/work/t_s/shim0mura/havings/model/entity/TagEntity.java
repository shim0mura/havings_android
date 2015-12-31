package work.t_s.shim0mura.havings.model.entity;

import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by shim0mura on 2015/12/19.
 */

@JsonIgnoreProperties(ignoreUnknown=true)
public class TagEntity implements Serializable {

    public int id;
    public String name;
    public String yomiJp = "";
    public String yomiRoma = "";
    public int parentId;
    public int priority;
    public int nest;
    public int tagType;
    public boolean isDeleted;

    /*
    public TagEntity(int i, String n, @Nullable String yj, @Nullable String yr){
        this.id = i;
        this.name = n;
        this.yomiJp = (yj == null) ? "" : yj;
        this.yomiRoma = (yr == null) ? "" : yr;
    }
    */

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

    public String toString(){
        return name;
    }

}