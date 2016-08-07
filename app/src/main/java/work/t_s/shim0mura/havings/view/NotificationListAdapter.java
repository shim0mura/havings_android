package work.t_s.shim0mura.havings.view;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.User;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.NotificationEntity;
import work.t_s.shim0mura.havings.model.entity.TimerEntity;
import work.t_s.shim0mura.havings.model.entity.UserEntity;
import work.t_s.shim0mura.havings.presenter.UserPresenter;
import work.t_s.shim0mura.havings.util.ViewUtil;

/**
 * Created by shim0mura on 2016/02/13.
 */

public class NotificationListAdapter extends ArrayAdapter<NotificationEntity> {

    Context context;
    private LayoutInflater layoutInflater;
    private int layoutResource;
    List<NotificationEntity> notifications;
    private final String separator = ", ";
    private final int maxActers = 3;
    private int unreadColor;
    private int readColor;
    private Date currentDate;
    private String honorific;

    public NotificationListAdapter(Context context, int resource, List<NotificationEntity> notificationEntities) {
        super(context, resource);
        this.context = context;
        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        notifications = notificationEntities;
        unreadColor = ContextCompat.getColor(context, R.color.unreadNotification);
        readColor = ContextCompat.getColor(context, android.R.color.transparent);
        currentDate = new Date();
        honorific = context.getString(R.string.honorific);
    }

    @Override
    public int getCount() {
        return notifications.size();
    }

    @Override
    public NotificationEntity getItem(int position) {
        return notifications.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResource, null);
            holder = new ViewHolder();

            holder.wrapper = (RelativeLayout)convertView.findViewById(R.id.notification_wrapper);
            holder.thumbnail = (CircleImageView)convertView.findViewById(R.id.thumbnail);
            holder.notificationText = (TextView)convertView.findViewById(R.id.notification_text);
            holder.subText = (TextView)convertView.findViewById(R.id.notification_sub_text);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        NotificationEntity notification = getItem(position);

        if(notification.unread) {
            convertView.setBackgroundColor(unreadColor);
        }else{
            convertView.setBackgroundColor(readColor);
        }

        int length;
        String thumbnail;
        StringBuilder targets = new StringBuilder();;
        StringBuilder acters = new StringBuilder();

        // List<?>だとそれぞれの型にキャストできないので
        // しょうがないからacterとtargetはLinkedHashMapのまま使ってる
        switch(notification.type) {

            case User.NOTIFICATION_TYPE_TIMER:
                if (notification.acter.isEmpty() || notification.target.isEmpty()) {
                    break;
                }

                holder.thumbnail.setImageResource(R.drawable.ic_alarm_black_18dp);

                length = (notification.acter.size() > maxActers) ? maxActers : notification.acter.size();
                for(int i = 0; i < length; i++){
                    LinkedHashMap t = (LinkedHashMap)notification.acter.get(i);
                    if(acters.length() > 0){
                        acters.append(separator);
                    }
                    acters.append(t.get("name"));
                }

                if(notification.acter.size() > maxActers){
                    acters.append("ほか" + String.valueOf(notification.acter.size() - maxActers) + "つ");
                }

                LinkedHashMap timerItem = (LinkedHashMap) notification.target.get(0);
                holder.notificationText.setText(timerItem.get("name") + "のタスク: " + acters.toString() + "の期限が切れました");
                holder.subText.setText(ViewUtil.secondsToEasyDateFormat((Activity)context, currentDate.getTime() - notification.date.getTime()) + "前");
                convertView.setTag(R.id.NOTIFICATION_TYPE, notification.type);
                convertView.setTag(R.id.NOTIFICATION_RELATED_ID, (int)timerItem.get("id"));

                break;
            case User.NOTIFICATION_TYPE_COMMENT:
                if(notification.acter.isEmpty() || notification.target.isEmpty()){
                    break;
                }

                LinkedHashMap commentUser = (LinkedHashMap)notification.acter.get(0);
                if(commentUser.get("image") != null){
                    String thumbnailUrl = ApiService.BASE_URL + commentUser.get("image");
                    Glide.with(context).load(thumbnailUrl).into(holder.thumbnail);
                }else{
                    holder.thumbnail.setImageResource(R.drawable.bg);
                }

                length = (notification.acter.size() > maxActers) ? maxActers : notification.acter.size();
                for(int i = 0; i < length; i++){
                    LinkedHashMap cu = (LinkedHashMap)notification.acter.get(i);
                    if(acters.length() > 0){
                        acters.append(separator);
                    }
                    acters.append(cu.get("name") + honorific);
                }

                if(notification.acter.size() > maxActers){
                    acters.append("ほか" + String.valueOf(notification.acter.size() - maxActers) + "人");
                }

                LinkedHashMap commentTarget = (LinkedHashMap)notification.target.get(0);
                holder.notificationText.setText(acters.toString() + "が" + commentTarget.get("name") + "にコメントをしました");
                holder.subText.setText(ViewUtil.secondsToEasyDateFormat((Activity)context, currentDate.getTime() - notification.date.getTime()) + "前");

                convertView.setTag(R.id.NOTIFICATION_TYPE, notification.type);
                convertView.setTag(R.id.NOTIFICATION_RELATED_ID, (int)commentTarget.get("id"));

                break;
            case User.NOTIFICATION_TYPE_IMAGE_FAVORITE:
                if(notification.acter.isEmpty() || notification.target.isEmpty()){
                    break;
                }

                LinkedHashMap imageFavoritesUser = (LinkedHashMap)notification.acter.get(0);
                if(imageFavoritesUser.get("image") != null){
                    String thumbnailUrl = ApiService.BASE_URL + imageFavoritesUser.get("image");
                    Glide.with(context).load(thumbnailUrl).into(holder.thumbnail);
                }else{
                    holder.thumbnail.setImageResource(R.drawable.bg);
                }

                length = (notification.acter.size() > maxActers) ? maxActers : notification.acter.size();
                for(int i = 0; i < length; i++){
                    LinkedHashMap ifu = (LinkedHashMap)notification.acter.get(i);
                    if(acters.length() > 0){
                        acters.append(separator);
                    }
                    acters.append(ifu.get("name") + honorific);
                }

                if(notification.acter.size() > maxActers){
                    acters.append("ほか" + String.valueOf(notification.acter.size() - maxActers) + "人");
                }

                LinkedHashMap imageFavoritesTarget = (LinkedHashMap)notification.target.get(0);
                holder.notificationText.setText(acters.toString() + "が" + imageFavoritesTarget.get("item_name") + "の画像にいいね!しました");
                holder.subText.setText(ViewUtil.secondsToEasyDateFormat((Activity)context, currentDate.getTime() - notification.date.getTime()) + "前");

                convertView.setTag(R.id.NOTIFICATION_TYPE, notification.type);
                convertView.setTag(R.id.NOTIFICATION_RELATED_ID, (int)imageFavoritesTarget.get("item_id"));

                break;

            case User.NOTIFICATION_TYPE_FAVORITE:
                if(notification.acter.isEmpty() || notification.target.isEmpty()){
                    break;
                }

                LinkedHashMap favoritesUser = (LinkedHashMap)notification.acter.get(0);
                if(favoritesUser.get("image") != null){
                    String thumbnailUrl = ApiService.BASE_URL + favoritesUser.get("image");
                    Glide.with(context).load(thumbnailUrl).into(holder.thumbnail);
                }else{
                    holder.thumbnail.setImageResource(R.drawable.bg);
                }

                length = (notification.acter.size() > maxActers) ? maxActers : notification.acter.size();
                for(int i = 0; i < length; i++){
                    LinkedHashMap fu = (LinkedHashMap)notification.acter.get(i);
                    if(acters.length() > 0){
                        acters.append(separator);
                    }
                    acters.append(fu.get("name") + honorific);
                }

                if(notification.acter.size() > maxActers){
                    acters.append("ほか" + String.valueOf(notification.acter.size() - maxActers) + "人");
                }

                LinkedHashMap favoritesTarget = (LinkedHashMap)notification.target.get(0);
                holder.notificationText.setText(acters.toString() + "が" + favoritesTarget.get("name") + "にいいね!しました");
                holder.subText.setText(ViewUtil.secondsToEasyDateFormat((Activity)context, currentDate.getTime() - notification.date.getTime()) + "前");

                convertView.setTag(R.id.NOTIFICATION_TYPE, notification.type);
                convertView.setTag(R.id.NOTIFICATION_RELATED_ID, (int)favoritesTarget.get("id"));

                break;

            case User.NOTIFICATION_TYPE_FOLLOW:
                if(notification.acter.isEmpty()){
                    break;
                }

                LinkedHashMap followUser = (LinkedHashMap)notification.acter.get(0);
                if(followUser.get("image") != null){
                    String thumbnailUrl = ApiService.BASE_URL + followUser.get("image");
                    Glide.with(context).load(thumbnailUrl).into(holder.thumbnail);
                }else{
                    holder.thumbnail.setImageResource(R.drawable.bg);
                }

                length = (notification.acter.size() > maxActers) ? maxActers : notification.acter.size();
                for(int i = 0; i < length; i++){
                    LinkedHashMap flu = (LinkedHashMap)notification.acter.get(i);
                    if(acters.length() > 0){
                        acters.append(separator);
                    }
                    acters.append(flu.get("name") + honorific);
                }

                if(notification.acter.size() > maxActers){
                    acters.append("ほか" + String.valueOf(notification.acter.size() - maxActers) + "人");
                }

                holder.notificationText.setText(acters.toString() + "があなたをフォローしました");
                holder.subText.setText(ViewUtil.secondsToEasyDateFormat((Activity)context, currentDate.getTime() - notification.date.getTime()) + "前");

                convertView.setTag(R.id.NOTIFICATION_TYPE, notification.type);
                convertView.setTag(R.id.NOTIFICATION_RELATED_ID, (int)followUser.get("id"));

                break;

            default:
                convertView.setVisibility(View.GONE);
                break;
        }

        return convertView;
    }

    class ViewHolder{

        RelativeLayout wrapper;
        CircleImageView thumbnail;
        TextView notificationText;
        TextView subText;

    }
}