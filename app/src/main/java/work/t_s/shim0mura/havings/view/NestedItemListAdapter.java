package work.t_s.shim0mura.havings.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import timber.log.Timber;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.presenter.StickyScrollPresenter;
import work.t_s.shim0mura.havings.util.ViewUtil;

/**
 * Created by shim0mura on 2016/01/29.
 */
public class NestedItemListAdapter extends ArrayAdapter<ItemEntity> {

    Context context;
    private LayoutInflater layoutInflater;
    private int layoutResource;
    private ArrayList<ItemEntity> itemEntityList = new ArrayList<ItemEntity>();
    private int nestDepthMargin;

    public NestedItemListAdapter(Context context, int resource, List<ItemEntity> items) {
        super(context, resource);
        this.context = context;
        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        nestDepthMargin = (int)context.getResources().getDimension(R.dimen.nested_item_depth_unit);

        Timber.d("nest depth %s", nestDepthMargin);
        setItemEntityList(new ArrayList<ItemEntity>(items), 0);
    }

    private void setItemEntityList(ArrayList<ItemEntity> items, int nest){

        for(ItemEntity item : items){
            //Timber.d("before remove, items.size %s, nest: %s", items.size(), nest);

            item.nest = nest;
            itemEntityList.add(item);

            //items.remove(item);
            //Timber.d("after remove, items.size %s, nest: %s", items.size(), nest);
            setItemEntityList(new ArrayList<ItemEntity>(item.owningItems), nest + 1);
        }
    }

    @Override
    public int getCount() {
        return itemEntityList.size();
    }

    @Override
    public ItemEntity getItem(int position) {
        return itemEntityList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Timber.d("position %s", position);
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResource, null);
            holder = new ViewHolder();

            holder.imageWrapper = (LinearLayout)convertView.findViewById(R.id.image_wrapper);
            holder.itemType = (ImageView)convertView.findViewById(R.id.item_type);
            holder.name = (TextView)convertView.findViewById(R.id.item_name);
            holder.count = (TextView)convertView.findViewById(R.id.item_count);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ItemEntity item = getItem(position);

        convertView.setTag(R.id.TAG_ITEM_ID, item.id);
        if(item.isList){
            holder.itemType.setImageResource(R.drawable.list_icon_for_tab);
        }else {
            holder.itemType.setImageResource(R.drawable.item_icon_for_tab);
        }
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.imageWrapper.getLayoutParams();
        Timber.d("nest deep %s , %s", item.nest, item.nest * nestDepthMargin);
        params.setMargins(item.nest * nestDepthMargin, 0, 0, 0);
        holder.imageWrapper.setLayoutParams(params);
        holder.name.setText(item.name);
        holder.count.setText(String.valueOf(item.count));

        return convertView;
    }

    /*
    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResource, null);
            holder = new ViewHolder();

            holder.wrapper = (LinearLayout)convertView.findViewById(R.id.wrapper);
            holder.imageWrapper = (LinearLayout)convertView.findViewById(R.id.image_wrapper);
            holder.itemType = (ImageView)convertView.findViewById(R.id.item_type);
            holder.name = (TextView)convertView.findViewById(R.id.item_name);
            holder.count = (TextView)convertView.findViewById(R.id.item_count);

            convertView.setTag(holder);

            convertView.setOnTouchListener(new StickyScrollPresenter.CustomTouchListenerForStickyHeader(stickyScrollPresenter));
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Timber.d("tag id %s", v.getTag(R.id.TAG_ITEM_ID));
                }
            });

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ItemEntity item = getItem(position);

        if(item.isList){
            holder.itemType.setImageResource(R.drawable.list_icon_for_tab);
            holder.wrapper.setVisibility(View.VISIBLE);

        }else{
            holder.itemType.setImageResource(R.drawable.item_icon_for_tab);
            holder.wrapper.setVisibility(View.GONE);

        }

        convertView.setTag(R.id.TAG_ITEM_ID, item.id);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.imageWrapper.getLayoutParams();
        params.setMargins(item.nest * 50, 0, 0, 0);
        holder.name.setText(item.name);
        holder.count.setText(String.valueOf(item.count));

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        ItemEntity item = getItem(position);
        int id = 0;
        if(item.isList && !item.owningItems.isEmpty()){
            id= item.id;
            Timber.d("is list id %s", id);
        }else{
            id= item.listId;
            Timber.d("not list id %s", id);

        }
        return id;
    }
    */

    class ViewHolder{

        LinearLayout imageWrapper;
        ImageView itemType;
        TextView name;
        TextView count;

    }
}