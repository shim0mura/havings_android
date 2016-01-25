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

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.presenter.ItemPresenter;

/**
 * Created by shim0mura on 2015/12/09.
 */
public class ItemListAdapter extends ArrayAdapter<ItemEntity> {

    private LayoutInflater layoutInflater;
    private int layoutResource;
    private Context context;
    private List<ItemEntity> itemList = new ArrayList<ItemEntity>();
    private ItemEntity item;
    private int lastItemId = 0;
    private Boolean hasNextItemToLoad;
    private Boolean isLoadingNextItem = false;

    public ItemListAdapter(Context c, int resource, ItemEntity i){
        super(c, resource);
        layoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        context = c;
        item = i;
        addItem(i);
    }

    public Boolean hasNextItem(){
        return hasNextItemToLoad;
    }

    public int getLastItemId(){
        Log.d("last item id", String.valueOf(lastItemId));
        return lastItemId;
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

    public void addItem(ItemEntity item){
        Log.d("item", "added");
        if(item.owningItems != null) {
            itemList.addAll(item.owningItems);
            lastItemId = item.owningItems.get(item.owningItems.size() - 1).id;
        }
        hasNextItemToLoad = item.hasNextItem;
    }

    public void unshiftItem(ItemEntity item){
        itemList.add(0, item);
    }

    @Override
    public ItemEntity getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResource, null);
            holder = new ViewHolder();

            holder.thumbnail = (CircleImageView)convertView.findViewById(R.id.item_thumbnail);
            holder.itemType = (ImageView)convertView.findViewById(R.id.item_type);
            holder.name = (TextView)convertView.findViewById(R.id.item_name);
            holder.count = (TextView)convertView.findViewById(R.id.item_count);
            holder.favoriteCount = (TextView)convertView.findViewById(R.id.item_favorite_count);
            holder.tags = (FlowLayout)convertView.findViewById(R.id.item_tag);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ItemEntity item = getItem(position);
        convertView.setTag(R.string.tag_item_id, item.id);

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

        return convertView;
    }

    class ViewHolder{

        CircleImageView thumbnail;
        ImageView itemType;
        TextView name;
        TextView count;
        TextView favoriteCount;
        FlowLayout tags;

    }
}
