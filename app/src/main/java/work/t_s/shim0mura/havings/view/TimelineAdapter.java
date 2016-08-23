package work.t_s.shim0mura.havings.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.klinker.android.link_builder.Link;
import com.klinker.android.link_builder.LinkBuilder;
import com.wefika.flowlayout.FlowLayout;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;
import work.t_s.shim0mura.havings.ItemActivity;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.UserActivity;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.User;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.NotificationEntity;
import work.t_s.shim0mura.havings.model.entity.SearchResultEntity;
import work.t_s.shim0mura.havings.model.entity.TimelineEntity;
import work.t_s.shim0mura.havings.presenter.ItemPresenter;
import work.t_s.shim0mura.havings.util.ViewUtil;

/**
 * Created by shim0mura on 2016/04/10.
 */
public class TimelineAdapter extends ArrayAdapter<NotificationEntity> {
    protected LayoutInflater layoutInflater;
    protected int layoutResource;
    protected Context context;
    protected List<NotificationEntity> eventList = new ArrayList<NotificationEntity>();
    protected int lastEventId = 0;
    protected Boolean hasNextItemToLoad;
    protected Boolean isLoadingNextItem = false;
    private String honorific;

    public TimelineAdapter(Context c, int resource, TimelineEntity timelineEntity){
        super(c, resource);
        layoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        context = c;
        honorific = context.getString(R.string.honorific);
        addItem(timelineEntity);
    }

    public Boolean hasNextItem(){
        return hasNextItemToLoad;
    }

    public int getLastEventId(){
        return lastEventId;
    }

    public void startLoadNextItem(){
        isLoadingNextItem = true;
    }

    public void finishLoadNextItem(){
        isLoadingNextItem = false;
    }

    public Boolean getIsLoadingNextItem(){
        return isLoadingNextItem;
    }

    public void addItem(TimelineEntity timelineEntity){
        if(timelineEntity.timeline != null && !timelineEntity.timeline.isEmpty()) {
            eventList.addAll(timelineEntity.timeline);
            lastEventId = timelineEntity.timeline.get(timelineEntity.timeline.size() -1).eventId;
        }
        hasNextItemToLoad = timelineEntity.hasNextEvent;
    }

    @Override
    public NotificationEntity getItem(int position) {
        return eventList.get(position);
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

            holder.eventTypeIcon = (ImageView)convertView.findViewById(R.id.event_type_icon);
            holder.acter = (TextView)convertView.findViewById(R.id.acter);
            holder.subText = (TextView)convertView.findViewById(R.id.notification_sub_text);
            holder.targetImage = (CircleImageView)convertView.findViewById(R.id.target_image);
            holder.target = (TextView)convertView.findViewById(R.id.target);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        NotificationEntity event = getItem(position);

        // List<?>だとそれぞれの型にキャストできないので
        // しょうがないからacterとtargetはLinkedHashMapのまま使ってる
        final LinkedHashMap acter = (LinkedHashMap)event.acter.get(0);
        LinkedHashMap target = (LinkedHashMap)event.target.get(0);

        if(acter == null || target == null){
            Timber.d("acter or target null %s %s", acter == null, target == null);
            return null;
        }

        //holder.acter.setText((String) acter.get("name") + honorific);
        holder.acter.setTag(R.id.TAG_USER_ID, (int) acter.get("id"));
        /*
        holder.acter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserActivity.startActivity(context, (int) v.getTag(R.id.TAG_USER_ID));
            }
        });
        */

        if(target.get("image") != null){
            String thumbnailUrl = ApiServiceManager.getSingleton(context).getApiUrl() + target.get("image");
            Glide.with(context).load(thumbnailUrl).into(holder.targetImage);
        }else{
            holder.targetImage.setImageResource(R.drawable.bg);
        }
        holder.target.setText((String) target.get("name"));
        //holder.acter.setText((String) acter.get("name") + honorific);

        String acterName = (String) acter.get("name") + honorific;
        Link acterLink = new Link(acterName)
                .setTextColor(Color.parseColor("#81D4FA"))
                .setUnderlined(false)
                .setOnClickListener(new Link.OnClickListener() {
                    @Override
                    public void onClick(String clickedText) {
                        UserActivity.startActivity(context, (int)acter.get("id"));
                    }
                });


        switch(event.type) {

            case User.NOTIFICATION_TYPE_COMMENT:
                holder.subText.setText(context.getString(R.string.postfix_timeline_commented, acterName));
                LinkBuilder.on(holder.subText).addLink(acterLink).build();

                holder.eventTypeIcon.setImageResource(R.drawable.ic_chat_bubble_yellow_400_36dp);
                holder.target.setTag(R.id.TAG_ITEM_ID, (int) target.get("id"));
                holder.target.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ItemActivity.startActivity(context, (int) v.getTag(R.id.TAG_ITEM_ID));
                    }
                });

                break;
            case User.NOTIFICATION_TYPE_IMAGE_FAVORITE:
                holder.subText.setText(context.getString(R.string.postfix_timeline_image_favorite, acterName));
                LinkBuilder.on(holder.subText).addLink(acterLink).build();

                holder.eventTypeIcon.setImageResource(R.drawable.ic_favorite_pink_300_36dp);
                holder.target.setTag(R.id.TAG_ITEM_ID, (int) target.get("id"));
                holder.target.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ItemActivity.startActivity(context, (int) v.getTag(R.id.TAG_ITEM_ID));
                    }
                });

                break;
            case User.NOTIFICATION_TYPE_FAVORITE:
                holder.subText.setText(context.getString(R.string.postfix_timeline_favorite, acterName));
                LinkBuilder.on(holder.subText).addLink(acterLink).build();

                holder.eventTypeIcon.setImageResource(R.drawable.ic_favorite_pink_300_36dp);
                holder.target.setTag(R.id.TAG_ITEM_ID, (int) target.get("id"));
                holder.target.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ItemActivity.startActivity(context, (int) v.getTag(R.id.TAG_ITEM_ID));
                    }
                });

                break;
            case User.NOTIFICATION_TYPE_FOLLOW:
                holder.subText.setText(context.getString(R.string.postfix_timeline_follow, acterName));
                LinkBuilder.on(holder.subText).addLink(acterLink).build();

                holder.eventTypeIcon.setImageResource(R.drawable.ic_person_add_green_400_36dp);
                holder.target.setTag(R.id.TAG_USER_ID, (int) target.get("id"));
                holder.target.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UserActivity.startActivity(context, (int) v.getTag(R.id.TAG_USER_ID));
                    }
                });

                break;
            case User.NOTIFICATION_TYPE_CREATE_ITEM:
                holder.subText.setText(context.getString(R.string.postfix_timeline_create_item, acterName));
                LinkBuilder.on(holder.subText).addLink(acterLink).build();

                holder.eventTypeIcon.setImageResource(R.drawable.ic_add_circle_cyan_300_36dp);
                holder.target.setTag(R.id.TAG_ITEM_ID, (int) target.get("id"));
                holder.target.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ItemActivity.startActivity(context, (int) v.getTag(R.id.TAG_ITEM_ID));
                    }
                });

                break;
            case User.NOTIFICATION_TYPE_CREATE_LIST:
                holder.subText.setText(context.getString(R.string.postfix_timeline_create_list, acterName));
                LinkBuilder.on(holder.subText).addLink(acterLink).build();

                holder.eventTypeIcon.setImageResource(R.drawable.ic_add_circle_blue_300_36dp);
                holder.target.setTag(R.id.TAG_ITEM_ID, (int) target.get("id"));
                holder.target.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ItemActivity.startActivity(context, (int) v.getTag(R.id.TAG_ITEM_ID));
                    }
                });

                break;
            case User.NOTIFICATION_TYPE_ADD_IMAGE:
                holder.subText.setText(context.getString(R.string.postfix_timeline_add_image, acterName));
                LinkBuilder.on(holder.subText).addLink(acterLink).build();

                holder.eventTypeIcon.setImageResource(R.drawable.ic_photo_blue_grey_400_36dp);
                holder.target.setTag(R.id.TAG_ITEM_ID, (int) target.get("id"));
                holder.target.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ItemActivity.startActivity(context, (int) v.getTag(R.id.TAG_ITEM_ID));
                    }
                });

                break;
            case User.NOTIFICATION_TYPE_DUMP:
                holder.subText.setText(context.getString(R.string.postfix_timeline_dump, acterName));
                LinkBuilder.on(holder.subText).addLink(acterLink).build();

                holder.eventTypeIcon.setImageResource(R.drawable.ic_delete_red_300_36dp);
                holder.target.setTag(R.id.TAG_ITEM_ID, (int) target.get("id"));
                holder.target.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ItemActivity.startActivity(context, (int) v.getTag(R.id.TAG_ITEM_ID));
                    }
                });

                break;
            default:
                convertView.setVisibility(View.GONE);
                break;
        }

        return convertView;
    }

    public static class ViewHolder {

        ImageView eventTypeIcon;
        CircleImageView targetImage;
        TextView target;
        TextView acter;
        TextView subText;
    }
}
