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
import com.wefika.flowlayout.FlowLayout;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.entity.UserEntity;
import work.t_s.shim0mura.havings.model.entity.UserListEntity;

/**
 * Created by shim0mura on 2016/01/24.
 */
public class UserListAdapter extends ArrayAdapter<UserEntity> {

    Context context;
    private LayoutInflater layoutInflater;
    private int layoutResource;
    List<UserEntity> userEntityList;

    public UserListAdapter(Context context, int resource, List<UserEntity> users) {
        super(context, resource);
        this.context = context;
        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        userEntityList = users;
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

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        UserEntity user = getItem(position);

        convertView.setTag(R.id.TAG_USER_ID, user.id);
        if(user.image != null){
            String thumbnailUrl = ApiService.BASE_URL + user.image;
            Glide.with(context).load(thumbnailUrl).into(holder.thumbnail);
        }
        holder.name.setText(user.name);
        holder.count.setText(String.valueOf(user.count));
        holder.description.setText(user.description);

        return convertView;
    }

    class ViewHolder{

        CircleImageView thumbnail;
        TextView name;
        TextView count;
        TextView description;

    }
}