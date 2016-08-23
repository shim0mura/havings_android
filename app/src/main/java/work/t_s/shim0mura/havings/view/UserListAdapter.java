package work.t_s.shim0mura.havings.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wefika.flowlayout.FlowLayout;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import lecho.lib.hellocharts.model.Line;
import timber.log.Timber;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.User;
import work.t_s.shim0mura.havings.model.entity.UserEntity;
import work.t_s.shim0mura.havings.model.entity.UserListEntity;
import work.t_s.shim0mura.havings.presenter.UserPresenter;

/**
 * Created by shim0mura on 2016/01/24.
 */
public class UserListAdapter extends ArrayAdapter<UserEntity> {

    Context context;
    private LayoutInflater layoutInflater;
    private int layoutResource;
    private UserPresenter userPresenter;
    List<UserEntity> userEntityList;

    public UserListAdapter(Context context, int resource, List<UserEntity> users) {
        super(context, resource);
        this.context = context;
        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        userEntityList = users;
        userPresenter = new UserPresenter(context);
    }

    public void changeFollowingState(int userId, Boolean follow){
        for(UserEntity userEntity : userEntityList){
            if(userEntity.id == userId){
                if(follow) {
                    userEntity.relation = User.RELATION_FOLLOWED;
                }else{
                    userEntity.relation = User.RELATION_NOTHING;
                }
                notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public int getCount() {
        return userEntityList.size();
    }

    @Override
    public UserEntity getItem(int position) {
        return userEntityList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResource, null);
            holder = new ViewHolder();

            holder.thumbnail = (CircleImageView)convertView.findViewById(R.id.user_thumbnail);
            holder.name = (TextView)convertView.findViewById(R.id.user_name);
            holder.count = (TextView)convertView.findViewById(R.id.item_count);
            holder.description = (TextView)convertView.findViewById(R.id.description);
            holder.followingState = (LinearLayout)convertView.findViewById(R.id.following_state);
            holder.followUser = (Button)convertView.findViewById(R.id.follow_user);
            holder.unfollowUser = (Button)convertView.findViewById(R.id.unfollow_user);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final UserEntity user = getItem(position);
        final UserListAdapter self = this;

        convertView.setTag(R.id.TAG_USER_ID, user.id);
        if(user.image != null){
            String thumbnailUrl = ApiServiceManager.getSingleton(context).getApiUrl() + user.image;
            Glide.with(context).load(thumbnailUrl).into(holder.thumbnail);
        }
        holder.name.setText(user.name);
        holder.count.setText(String.valueOf(user.count));
        holder.description.setText(user.description);
        switch(user.relation){
            case User.RELATION_NOTHING:
                holder.followUser.setVisibility(View.VISIBLE);
                holder.unfollowUser.setVisibility(View.GONE);
                holder.followUser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        userPresenter.followUserFromAdapter(user.id, self);
                    }
                });
                break;
            case User.RELATION_FOLLOWED:
                holder.followUser.setVisibility(View.GONE);
                holder.unfollowUser.setVisibility(View.VISIBLE);
                holder.unfollowUser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(context)
                                .setTitle(context.getString(R.string.action_unfollow))
                                .setMessage(user.name + context.getString(R.string.prompt_action_unfollow))
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        userPresenter.unfollowUserFromAdapter(user.id, self);
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                });
                break;
            case User.RELATION_FRIEND:
                holder.followUser.setVisibility(View.GONE);
                holder.unfollowUser.setVisibility(View.VISIBLE);
                holder.unfollowUser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(context)
                                .setTitle(context.getString(R.string.action_unfollow))
                                .setMessage(user.name + context.getString(R.string.prompt_action_unfollow))
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        userPresenter.unfollowUserFromAdapter(user.id, self);
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                });
                break;
            case User.RELATION_HIMSELF:
                holder.followingState.setVisibility(View.GONE);
                break;
        }

        return convertView;
    }

    class ViewHolder{

        CircleImageView thumbnail;
        TextView name;
        TextView count;
        TextView description;
        LinearLayout followingState;
        Button followUser;
        Button unfollowUser;
    }
}