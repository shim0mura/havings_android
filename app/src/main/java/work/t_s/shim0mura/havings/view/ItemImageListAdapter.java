package work.t_s.shim0mura.havings.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wefika.flowlayout.FlowLayout;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.UserListActivity;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageEntity;
import work.t_s.shim0mura.havings.presenter.ItemPresenter;
import work.t_s.shim0mura.havings.presenter.UserListPresenter;
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
    private ItemPresenter itemPresenter;
    private int lastItemImageId = 0;
    private Boolean hasNextItemToLoad;
    private Boolean isLoadingNextItem = false;
    private ItemImageListAdapter self;

    public ItemImageListAdapter(Context c, int resource, ItemEntity i, ItemPresenter p){
        super(c, resource);
        layoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        context = c;
        item = i;
        itemPresenter = p;
        self = this;
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

    public void changeImageFavoriteState(int imageId, Boolean isFavorited){
        for(ItemImageEntity itemImageEntity : itemImageList){
            if(itemImageEntity.id == imageId){
                Timber.d("image_id %s, %s", imageId, isFavorited);
                itemImageEntity.isFavorited = isFavorited;
                itemImageEntity.imageFavoriteCount = (isFavorited ? itemImageEntity.imageFavoriteCount + 1 : itemImageEntity.imageFavoriteCount -1);
                notifyDataSetChanged();
                break;
            }
        }
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
            holder.imageFavoriteCount = (TextView)convertView.findViewById(R.id.image_favorite_count);
            holder.imageFavoriteButton = (ImageView)convertView.findViewById(R.id.image_favorite_button);
            holder.imageFavoriteCountWrapper = (LinearLayout)convertView.findViewById(R.id.favorite_count_wrapper);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final ItemImageEntity itemImage = getItem(position);

        //convertView.setTag(R.string.tag_image_url, itemImage.url);
        //convertView.setTag(R.string.tag_image_date, itemImage.date);

        String thumbnailUrl = ApiService.BASE_URL + itemImage.url;
        Glide.with(context).load(thumbnailUrl).into(holder.image);

        holder.imageDate.setText(ViewUtil.dateToString(itemImage.addedDate, true));
        holder.imageFavoriteCount.setText(String.valueOf(itemImage.imageFavoriteCount));
        if(itemImage.isFavorited){
            holder.imageFavoriteButton.setImageResource(R.drawable.ic_already_favorite_36dp);
        }else{
            holder.imageFavoriteButton.setImageResource(R.drawable.ic_favorite_border_white_36dp);
        }
        holder.imageFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemImage.isFavorited) {
                    itemPresenter.unfavoriteItemImageFromAdapter(itemImage.id, self);
                } else {
                    itemPresenter.favoriteItemImageFromAdapter(itemImage.id, self);
                }
            }
        });
        holder.imageFavoriteCountWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserListActivity.startActivity(context, UserListPresenter.ITEM_IMAGE_FAVORITE_USER_LIST, itemImage.id);
            }
        });

        return convertView;
    }

    class ViewHolder{

        ImageView image;
        TextView imageDate;
        TextView imageFavoriteCount;
        ImageView imageFavoriteButton;
        LinearLayout imageFavoriteCountWrapper;

    }
}
