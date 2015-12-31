package work.t_s.shim0mura.havings.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.entity.CountDataEntity;
import work.t_s.shim0mura.havings.model.entity.EventEntity;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageEntity;
import work.t_s.shim0mura.havings.util.ViewUtil;

/**
 * Created by shim0mura on 2015/12/16.
 */
public class EventListInPopupAdapter extends ArrayAdapter<EventEntity> {

    private LayoutInflater layoutInflater;
    private int layoutResource;
    private Context context;
    private List<EventEntity> eventList = new ArrayList<EventEntity>();
    private Date date;

    public EventListInPopupAdapter(Context c, int resource){
        super(c, resource);
        layoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        context = c;
    }

    public void setDate(Date d){
        date = d;
    }

    public void changeEvents(List<EventEntity> events){
        eventList = events;
    }

    public void resetEvents(){
        eventList = new ArrayList<EventEntity>();
    }

    @Override
    public EventEntity getItem(int position) {
        return eventList.get(position);
    }

    @Override
    public int getCount() {
        return eventList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        Log.d("event list create", "start");
        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResource, null);
            holder = new ViewHolder();

            holder.eventType = (ImageView)convertView.findViewById(R.id.event_type_icon);
            holder.eventTypeText = (TextView)convertView.findViewById(R.id.event_type_text);
            holder.eventItem = (TextView)convertView.findViewById(R.id.event_item_link);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        EventEntity event = getItem(position);
        event.date = date;
        convertView.setTag(R.string.tag_event, event);

        switch(event.eventType){
            case EventEntity.EVENT_TYPE_ADD_ITEM:
                holder.eventType.setImageResource(R.drawable.ic_add_black_18dp);
                holder.eventTypeText.setText(R.string.event_type_add_item);
                holder.eventItem.setText(event.item.name);
                holder.eventItem.setVisibility(View.VISIBLE);
                break;
            case EventEntity.EVENT_TYPE_ADD_LIST:
                holder.eventType.setImageResource(R.drawable.ic_add_black_18dp);
                holder.eventTypeText.setText(R.string.event_type_add_list);
                holder.eventItem.setText(event.item.name);
                holder.eventItem.setVisibility(View.VISIBLE);
                break;
            case EventEntity.EVENT_TYPE_ADD_IMAGE:
                holder.eventType.setImageResource(R.drawable.ic_image_black_18dp);
                holder.eventTypeText.setText(R.string.event_type_add_image);
                holder.eventItem.setVisibility(View.GONE);
                break;
            case EventEntity.EVENT_TYPE_DUMP_ITEM:
                holder.eventType.setImageResource(R.drawable.ic_delete_black_18dp);
                holder.eventTypeText.setText(R.string.event_type_drop_item);
                holder.eventItem.setText(event.item.name);
                holder.eventItem.setVisibility(View.VISIBLE);
                break;
        }

        return convertView;
    }

    class ViewHolder{

        ImageView eventType;
        TextView eventTypeText;
        TextView eventItem;

    }

}
