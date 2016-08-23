package work.t_s.shim0mura.havings.view;

import android.content.Context;
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

import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.SearchResultEntity;
import work.t_s.shim0mura.havings.presenter.ItemPresenter;

/**
 * Created by shim0mura on 2016/04/06.
 */
public class SearchResultAdapter extends ArrayAdapter<ItemEntity> {

    protected LayoutInflater layoutInflater;
    protected int layoutResource;
    protected Context context;
    protected List<ItemEntity> itemList = new ArrayList<ItemEntity>();
    protected int currentPage = 1;
    protected Boolean hasNextItemToLoad;
    protected Boolean isLoadingNextItem = false;

    public SearchResultAdapter(Context c, int resource, SearchResultEntity searchResult){
        super(c, resource);
        layoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        context = c;
        addItem(searchResult);
    }

    public Boolean hasNextItem(){
        return hasNextItemToLoad;
    }

    public int getCurrentPage(){
        return currentPage;
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

    public void addItem(SearchResultEntity searchResult){
        if(searchResult.items != null && !searchResult.items.isEmpty()) {
            itemList.addAll(searchResult.items);
            currentPage = searchResult.currentPage;
        }
        hasNextItemToLoad = searchResult.hasNextPage;
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
            String thumbnailUrl = ApiServiceManager.getSingleton(context).getApiUrl() + item.thumbnail;
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
