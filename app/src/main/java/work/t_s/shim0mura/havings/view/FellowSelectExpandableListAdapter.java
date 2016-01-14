package work.t_s.shim0mura.havings.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;

/**
 * Created by shim0mura on 2016/01/11.
 */
public class FellowSelectExpandableListAdapter extends BaseExpandableListAdapter {

    final public static String MAIN_EXPLAIN = "MainExplain";
    final public static String SUB_EXPLAIN = "SubExplain";

    private Context context;
    private List<Map<String, String>> parentList = new ArrayList<Map<String, String>>();
    private List<List<Boolean>> allChildList = new ArrayList<List<Boolean>>();
    private List<ItemEntity> childItems;

    private Boolean showSubExplain = false;

    public FellowSelectExpandableListAdapter(Context context, List<ItemEntity> items, String mainExplain, String subExplain) {
        this.context = context;
        this.childItems = items;

        List<Boolean> childList =  new ArrayList<Boolean>();

        for(int i = 0; i < items.size(); i++){
            ItemEntity it = items.get(i);
            it.isSelectedForSomething = false;
            childList.add(true);
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put(MAIN_EXPLAIN, mainExplain);
        map.put(SUB_EXPLAIN, subExplain);

        parentList.add(map);

        allChildList.add(childList);
    }

    @Override
    public int getGroupCount() {
        return parentList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return allChildList.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return parentList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return allChildList.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ParentViewHolder holder;

        if (convertView == null) {
            convertView = (View) LayoutInflater.from(context).inflate(android.R.layout.simple_expandable_list_item_2, null);

            holder = new ParentViewHolder();

            holder.mainExplain = (TextView)convertView.findViewById(android.R.id.text1);
            holder.subExplain = (TextView)convertView.findViewById(android.R.id.text2);

            convertView.setTag(holder);
        } else {
            holder = (ParentViewHolder) convertView.getTag();
        }
        holder.mainExplain.setText(parentList.get(groupPosition).get(MAIN_EXPLAIN));
        if(showSubExplain) {
            holder.subExplain.setText(parentList.get(groupPosition).get(SUB_EXPLAIN));
        }else{
            holder.subExplain.setText(null);
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = (View) LayoutInflater.from(context).inflate(R.layout.list_item_fellow_ids, null);

            holder = new ViewHolder();

            holder.itemType = (ImageView)convertView.findViewById(R.id.item_type);
            holder.itemName = (TextView)convertView.findViewById(R.id.item_name);
            holder.itemCount = (TextView)convertView.findViewById(R.id.item_count);
            holder.checkBox = (CheckBox)convertView.findViewById(R.id.fellow_item);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ItemEntity item = childItems.get(childPosition);
        holder.itemName.setText(item.name);
        holder.itemCount.setText(String.valueOf(item.count));
        if(item.isList) {
            holder.itemType.setImageResource(R.drawable.list_icon_for_tab);
        }

        Boolean isChecked = item.isSelectedForSomething;

        holder.checkBox.setChecked(isChecked);
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox c = (CheckBox) v;
                if (c.isChecked()) {
                    selectItem(0, childPosition);
                } else {
                    deselectItem(0, childPosition);
                }
            }
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void selectItem(int groupPosition, int childPosition){
        childItems.get(childPosition).isSelectedForSomething = true;
    }

    public void deselectItem(int groupPosition, int childPosition){
        childItems.get(childPosition).isSelectedForSomething = false;
    }

    public void selectAll(){
        for(int i = 0; i < childItems.size(); i++){
            selectItem(0, i);
        }
        notifyDataSetChanged();
    }

    public void deselectAll(){
        for(int i = 0; i < childItems.size(); i++){
            deselectItem(0, i);
        }
        notifyDataSetChanged();
    }

    public void showSubText(){
        showSubExplain = true;
        notifyDataSetChanged();
    }

    public void hideSubText(){
        showSubExplain = false;
        notifyDataSetChanged();
    }

    class ViewHolder{

        ImageView itemType;
        TextView itemName;
        TextView itemCount;
        CheckBox checkBox;

    }

    class ParentViewHolder {
        TextView mainExplain;
        TextView subExplain;
    }
}
