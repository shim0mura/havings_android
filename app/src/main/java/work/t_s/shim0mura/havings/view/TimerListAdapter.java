package work.t_s.shim0mura.havings.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import work.t_s.shim0mura.havings.ItemActivity;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.entity.TimerEntity;
import work.t_s.shim0mura.havings.presenter.TimerPresenter;

/**
 * Created by shim0mura on 2016/04/04.
 */
public class TimerListAdapter extends ArrayAdapter<TimerEntity> {

    Context context;
    private LayoutInflater layoutInflater;
    private int layoutResource;
    List<TimerEntity> timerEntityList;

    public TimerListAdapter(Context context, int resource, List<TimerEntity> timers) {
        super(context, resource);
        this.context = context;
        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        timerEntityList = timers;
    }

    @Override
    public int getCount() {
        return timerEntityList.size();
    }

    @Override
    public TimerEntity getItem(int position) {
        return timerEntityList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResource, null);
            holder = new ViewHolder();

            holder.timerContent = (LinearLayout)convertView.findViewById(R.id.timer_content);
            holder.listNameWrapper = (LinearLayout)convertView.findViewById(R.id.list_name_wrapper);
            holder.listName = (TextView)convertView.findViewById(R.id.list_name);
            holder.timerMenu = (ImageView)convertView.findViewById(R.id.timer_menu);
            holder.timerMenu.setVisibility(View.GONE);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TimerEntity timer = getItem(position);

        convertView.setTag(R.id.TAG_ITEM_ID, timer.listId);

        if(timer.listName != null) {
            holder.listNameWrapper.setVisibility(View.VISIBLE);
            holder.listName.setText(timer.listName);
        }

        TimerPresenter.assignTimerText(convertView, timer, (Activity)context);

        holder.timerContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemId = (int)v.getTag(R.id.TAG_ITEM_ID);
                ItemActivity.startActivity(context, itemId);
            }
        });

        return convertView;
    }

    class ViewHolder{

        LinearLayout timerContent;
        LinearLayout listNameWrapper;
        TextView listName;
        ImageView timerMenu;

    }
}
