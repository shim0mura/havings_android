package work.t_s.shim0mura.havings.model;

import android.app.Activity;
import android.content.res.Resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import timber.log.Timber;

/**
 * Created by shim0mura on 2016/01/02.
 */
public class Item {

    public static final String PRIVATE_TYPE_GLOBAL = "prompt_private_type_global";
    public static final String PRIVATE_TYPE_FOLLOWER = "prompt_private_type_follower";
    public static final String PRIVATE_TYPE_FRIENDS = "prompt_private_type_friends";
    public static final String PRIVATE_TYPE_SECRET = "prompt_private_type_secret";

    public static final Map<Integer, String> privateTypeMap = new TreeMap<Integer, String>(){{
        put(0, PRIVATE_TYPE_GLOBAL);
        put(1, PRIVATE_TYPE_FOLLOWER);
        put(2, PRIVATE_TYPE_FRIENDS);
        put(3, PRIVATE_TYPE_SECRET);
    }};

    public static List<PrivateType> getPrivateTypeObj(Activity activity){
        List<PrivateType> p = new ArrayList<PrivateType>();
        for(Map.Entry<Integer, String> type : privateTypeMap.entrySet()) {
            p.add(new PrivateType(type.getKey(), activity));
            Timber.d("create type %s", type.getKey());
        }
        return p;
    }

    public boolean isValidName(String name){
        if(name.isEmpty()){
            return false;
        }else{
            return true;
        }
    }

    public void createItem(){

    }

    public static class PrivateType{

        final private int typeId;
        final private String typePrompt;

        public PrivateType(int type, Activity activity){
            typeId = type;
            Resources resources = activity.getResources();
            int prompt_id = resources.getIdentifier(privateTypeMap.get(type), "string", activity.getPackageName());
            typePrompt = resources.getString(prompt_id);
        }

        public int getTypeId(){
            return typeId;
        }

        @Override
        public String toString() {
            return typePrompt;
        }
    }

}
