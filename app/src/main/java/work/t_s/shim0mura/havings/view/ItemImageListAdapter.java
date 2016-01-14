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

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageEntity;
import work.t_s.shim0mura.havings.presenter.ItemPresenter;
import work.t_s.shim0mura.havings.util.ViewUtil;

/**
 * Created by shim0mura on 2015/12/10.
 */
public class ItemImageListAdapter extends ArrayAdapter<ItemImageEntity> {
    private LayoutInflater layoutInflater;
    private int layoutResource;
    private Context context;
    private List<ItemImageEntity> itemImageList = new ArrayList<ItemImageEntity>();
    private ItemEntity item;
    private int lastItemImageId = 0;
    private Boolean hasNextItemToLoad;
    private Boolean isLoadingNextItem = false;

    public ItemImageListAdapter(Context c, int resource, ItemEntity i){
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

    public int getLastItemImageId(){
        return lastItemImageId;
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
        if(item.images != null) {
            itemImageList.addAll(item.images);
            lastItemImageId = item.images.get(item.images.size() - 1).id;
        }
        hasNextItemToLoad = item.hasNextImage;
    }

    @Override
    public ItemImageEntity getItem(int position) {
        return itemImageList.get(position);
    }

    @Override
    public int getCount() {
        return itemImageList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResource, null);
            holder = new ViewHolder();

            holder.image = (ImageView)convertView.findViewById(R.id.item_image);
            holder.imageDate = (TextView)convertView.findViewById(R.id.image_date);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ItemImageEntity itemImage = getItem(position);

        convertView.setTag(R.string.tag_image_url, itemImage.url);
        convertView.setTag(R.string.tag_image_date, itemImage.date);

        String thumbnailUrl = ApiService.BASE_URL + itemImage.url;
        Glide.with(context).load(thumbnailUrl).into(holder.image);

        holder.imageDate.setText(ViewUtil.dateToString(itemImage.date, true));

        return convertView;
    }

    class ViewHolder{

        ImageView image;
        TextView imageDate;

    }
}
