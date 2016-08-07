package work.t_s.shim0mura.havings.model.event;

import java.io.Serializable;
import java.util.ArrayList;

import work.t_s.shim0mura.havings.model.entity.ItemPercentageEntity;

/**
 * Created by shim0mura on 2016/03/25.
 */
public class ItemPercentageGraphEvent  implements Serializable {

    public ArrayList<ItemPercentageEntity> itemPercentageEntities;

    public ItemPercentageGraphEvent(ArrayList<ItemPercentageEntity> entities){
        itemPercentageEntities = entities;
    }
}
