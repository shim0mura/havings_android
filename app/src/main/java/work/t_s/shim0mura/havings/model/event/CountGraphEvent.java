package work.t_s.shim0mura.havings.model.event;

import java.io.Serializable;
import java.util.List;

import work.t_s.shim0mura.havings.model.entity.CountDataEntity;

/**
 * Created by shim0mura on 2016/08/08.
 */

public class CountGraphEvent implements Serializable {

    public List<CountDataEntity> countDataEntities;

    public CountGraphEvent(List<CountDataEntity> countData){
        countDataEntities = countData;

    }

}
