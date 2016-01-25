package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

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

    public String email;
    public String password;

    public void setRegisterInfo(String name, String email, String password){
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
