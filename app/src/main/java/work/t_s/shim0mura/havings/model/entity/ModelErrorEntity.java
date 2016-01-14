package work.t_s.shim0mura.havings.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * Created by shim0mura on 2016/01/03.
 */

@JsonIgnoreProperties(ignoreUnknown=true)
public class ModelErrorEntity {
    public Map<String, List<String>> errors;
}
