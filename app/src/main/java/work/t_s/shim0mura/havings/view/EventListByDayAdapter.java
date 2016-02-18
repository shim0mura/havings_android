package work.t_s.shim0mura.havings.view;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wefika.flowlayout.FlowLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;
import work.t_s.shim0mura.havings.ImageDetailActivity;
import work.t_s.shim0mura.havings.ItemActivity;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.entity.CountDataEntity;
import work.t_s.shim0mura.havings.model.entity.EventEntity;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.presenter.ItemPresenter;
import work.t_s.shim0mura.havings.util.ViewUtil;

/**
 * Created by shim0mura on 2016/02/07.
 */
public class EventListByDayAdapter extends ArrayAdapter<CountDataEntity> {

    private LayoutInflater layoutInflater;
    private int layoutResource;
    private Context context;
    private List<CountDataEntity> eventList = new ArrayList<CountDataEntity>();

    public EventListByDayAdapter(Context c, int resource, List<CountDataEntity> countDataEntities){
        super(c, resource);
        context = c;
        layoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        Collections.reverse(countDataEntities);
        eventList = countDataEntities;
    }

    @Override
    public CountDataEntity getItem(int position) {
        return eventList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return eventList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResource, null);
            holder = new ViewHolder();

            holder.itemCount = (TextView)convertView.findViewById(R.id.item_count);
            holder.eventDate = (TextView)convertView.findViewById(R.id.event_date);
            holder.recentActivity = (LinearLayout)convertView.findViewById(R.id.recent_activity);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CountDataEntity countDataEntity = getItem(position);

        holder.itemCount.setText(String.valueOf(countDataEntity.count));
        holder.eventDate.setText(ViewUtil.dateToString(countDataEntity.date, true));
        holder.recentActivity.removeAllViews();

        if(countDataEntity.events != null && !countDataEntity.events.isEmpty()) {
            for (final EventEntity eventEntity : countDataEntity.events) {

                if(eventEntity.item == null) {
                    continue;
                }

                View eventView = layoutInflater.inflate(R.layout.partial_recent_activity_item, null);
                ImageView eventType = (ImageView)eventView.findViewById(R.id.event_type_icon);
                TextView eventTypeText = (TextView)eventView.findViewById(R.id.event_type_text);
                TextView itemName = (TextView)eventView.findViewById(R.id.event_item_name);
                CircleImageView thumbnail = (CircleImageView)eventView.findViewById(R.id.item_thumbnail);
                LinearLayout itemWrapper = (LinearLayout)eventView.findViewById(R.id.event_item);
                switch (eventEntity.eventType) {
                    case EventEntity.EVENT_TYPE_ADD_ITEM:
                        eventType.setImageResource(R.drawable.ic_add_black_18dp);
                        eventTypeText.setText(R.string.event_type_add_item);
                        eventView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ItemActivity.startActivity(context, eventEntity.item.id);
                            }
                        });
                        break;
                    case EventEntity.EVENT_TYPE_ADD_LIST:
                        eventType.setImageResource(R.drawable.ic_add_black_18dp);
                        eventTypeText.setText(R.string.event_type_add_list);
                        eventView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ItemActivity.startActivity(context, eventEntity.item.id);
                            }
                        });
                        break;
                    case EventEntity.EVENT_TYPE_ADD_IMAGE:
                        eventType.setImageResource(R.drawable.ic_image_black_18dp);
                        eventTypeText.setText(eventEntity.item.name + context.getString(R.string.event_type_add_image_to));
                        eventView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ImageDetailActivity.startActivity(context, eventEntity.item.id, eventEntity.item.itemImageId);
                            }
                        });
                        break;
                    case EventEntity.EVENT_TYPE_DUMP_ITEM:
                        eventType.setImageResource(R.drawable.ic_delete_black_18dp);
                        eventTypeText.setText(R.string.event_type_drop_item);
                        eventView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ItemActivity.startActivity(context, eventEntity.item.id);
                            }
                        });
                        break;
                }
                itemName.setText(eventEntity.item.name);
                if(eventEntity.item.thumbnail != null){
                    String thumbnailUrl = ApiService.BASE_URL + eventEntity.item.thumbnail;
                    Glide.with(context).load(thumbnailUrl).into(thumbnail);
                }
                itemWrapper.setVisibility(View.VISIBLE);

                holder.recentActivity.addView(eventView);

            }


        }

        /*
        if(item.thumbnail != null){
            String thumbnailUrl = ApiService.BASE_URL + item.thumbnail;
            Glide.with(context).load(thumbnailUrl).into(holder.thumbnail);
        }
        if(item.isList){
            holder.itemType.setImageResource(R.drawable.list_icon_for_tab);
        }
        holder.name.setText(item.name);
        holder.count.setText(String.valueOf(item.count));
        holder.favoriteCount.setText(String.valueOf(item.favoriteCount));
        if(item.tags != null && item.tags.size() > 0){
            holder.tags.removeAllViews();
            for(String tagString : item.tags){
                TextView tag = ItemPresenter.createTag(context, tagString, false);
                holder.tags.addView(tag);
            }
            Log.d("tags", item.tags.toString());
        }else if(item.tags != null && item.tags.size() == 0){
            holder.tags.removeAllViews();
        }
        */

        return convertView;
    }

    class ViewHolder{
        TextView eventDate;
        TextView itemCount;
        LinearLayout recentActivity;
    }

}
