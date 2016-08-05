package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

/**
 * Created by shim0mura on 2015/11/07.
 */

@JsonIgnoreProperties(ignoreUnknown=true)
public class UserEntity implements Serializable {

    public int id;
    public String name;
    public String image;
    public String description;
    public int count;
    public String path;

    public int followingCount;
    public int followerCount;
    public int dumpItemsCount;
    public int imageFavoritesCount;
    public int favoritesCount;
    public int registeredItemCount;
    public int registeredItemImageCount;

    public String backgroundImage;

    public boolean isFollowingViewer;

    public int relation;

    public ItemEntity homeList;
    public List<ItemEntity> owningItems;
    //public List<ItemEntity> nestedItems;

    public ItemEntity nestedItemFromHome;

    public String email;
    public String password;

    public void setRegisterInfo(String name, String email, String password){
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
