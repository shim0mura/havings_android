package work.t_s.shim0mura.havings.model.entity;

import java.io.Serializable;

/**
 * Created by shim0mura on 2015/12/19.
 */
public class UserListEntity implements Serializable {

    public int id;
    public String name;
    public int count;
    public int nest;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < nest; i++){
            sb.append("\u0020\u0020\u0020\u0020");
        }
        sb.append(name);
        sb.append("(" + String.valueOf(count) + ")");

        return sb.toString();
    }
}
