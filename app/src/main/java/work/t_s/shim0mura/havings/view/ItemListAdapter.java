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

    protected LayoutInflater layoutInflater;
    protected int layoutResource;
    protected Context context;
    protected List<ItemEntity> itemList = new ArrayList<ItemEntity>();
    protected ItemEntity item;
    protected int nextPage = 1;
    protected Boolean hasNextItemToLoad;
    protected Boolean isLoadingNextItem = false;

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

    public int getNextPage(){
        return nextPage;
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
        if(item.owningItems != null && !item.owningItems.isEmpty()) {
            itemList.addAll(item.owningItems);
            nextPage = item.nextPageForItem;
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

            holder.thumbnail = (ImageView)convertView.findViewById(R.id.item_thumbnail);
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
        }else{
            holder.thumbnail.setImageResource(R.drawable.bg);
        }

        if(item.isList){
            holder.itemType.setImageResource(R.drawable.list_icon_for_tab);
        }else {
            holder.itemType.setImageResource(R.drawable.item_icon_for_tab);
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
        }else if(item.tags != null && item.tags.size() == 0){
            holder.tags.removeAllViews();
        }

        return convertView;
    }

    public static class ViewHolder {

        public ImageView thumbnail;
        public ImageView itemType;
        public TextView name;
        public TextView count;
        public TextView favoriteCount;
        public FlowLayout tags;

    }
}
