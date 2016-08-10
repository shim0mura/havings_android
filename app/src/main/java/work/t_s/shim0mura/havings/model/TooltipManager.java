package work.t_s.shim0mura.havings.model;

import android.content.Context;
import android.content.SharedPreferences;

import work.t_s.shim0mura.havings.R;

/**
 * Created by shim0mura on 2016/08/10.
 */

public class TooltipManager {

    private static final String TOOLTIP_MANAGER = "tooltipManager";
    private static final String TOOLTIP_STATUS = "tooltipStatus";

    public static final int STATUS_LIST = 0;
    public static final int STATUS_ITEM = 1;
    public static final int STATUS_DUMP = 2;
    public static final int STATUS_IMAGE = 3;
    public static final int STATUS_NOMORE = 4;

    private static TooltipManager tooltipManager;
    private Context context;
    private int status;

    private TooltipManager(Context c){
        context = c;

        SharedPreferences preferences = context.getSharedPreferences(TOOLTIP_MANAGER, Context.MODE_PRIVATE);
        status = preferences.getInt(TOOLTIP_STATUS, 0);
    }

    public int getStatus(){
        return status;
    }

    public String getStatusText(){
        String result = "";
        switch(status){
            case STATUS_LIST:
                result = context.getString(R.string.prompt_tooltip_list);
                break;
            case STATUS_ITEM:
                result = context.getString(R.string.prompt_tooltip_item);
                break;
            case STATUS_DUMP:
                result = context.getString(R.string.prompt_tooltip_dump);
                break;
            case STATUS_IMAGE:
                result = context.getString(R.string.prompt_tooltip_image);
                break;
        }
        return result;
    }

    public void setNextStatus(){
        SharedPreferences preferences = context.getSharedPreferences(TOOLTIP_MANAGER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor;

        editor = preferences.edit();
        status = status + 1;

        editor.putInt(TOOLTIP_STATUS, status);
        editor.apply();
    }

    public static synchronized TooltipManager getSingleton(Context context){
        if(tooltipManager == null){
            tooltipManager = new TooltipManager(context);
        }

        return tooltipManager;
    }



}

