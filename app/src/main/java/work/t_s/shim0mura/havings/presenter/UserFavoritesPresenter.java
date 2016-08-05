package work.t_s.shim0mura.havings.presenter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wefika.flowlayout.FlowLayout;

import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.view.LineChartView;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;
import work.t_s.shim0mura.havings.DetailGraphActivity;
import work.t_s.shim0mura.havings.ImageDetailActivity;
import work.t_s.shim0mura.havings.ItemActivity;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.UserListActivity;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.StatusCode;
import work.t_s.shim0mura.havings.model.entity.FavoriteItemImageListEntity;
import work.t_s.shim0mura.havings.model.entity.FavoriteItemListEntity;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageListEntity;
import work.t_s.shim0mura.havings.model.entity.ModelErrorEntity;
import work.t_s.shim0mura.havings.model.entity.UserEntity;
import work.t_s.shim0mura.havings.util.ApiErrorUtil;
import work.t_s.shim0mura.havings.util.ViewUtil;
import work.t_s.shim0mura.havings.view.GraphRenderer;
import work.t_s.shim0mura.havings.view.ItemImageListAdapter;
import work.t_s.shim0mura.havings.view.ItemListAdapter;
import work.t_s.shim0mura.havings.view.NestedItemListAdapter;

/**
 * Created by shim0mura on 2016/02/08.
 */
public class UserFavoritesPresenter {

    Activity activity;
    static ApiService service;

    public UserFavoritesPresenter(Context c){
        activity = (Activity)c;
        service = ApiServiceManager.getService(activity);
    }


    public void getNextItemList(int userId, int offset, final FavoriteItemListAdapter adapter, final ListView listView, final View footerView, final View noItem){
        Call<FavoriteItemListEntity> call = service.getFavoriteItemList(userId, offset);

        call.enqueue(new Callback<FavoriteItemListEntity>() {
            @Override
            public void onResponse(Response<FavoriteItemListEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    FavoriteItemListEntity favorites = response.body();
                    adapter.finishLoadNextItem();
                    listView.removeFooterView(footerView);
                    adapter.addItem(favorites);
                    adapter.notifyDataSetChanged();
                    Timber.d("get_page %s",adapter.getNextPage());
                    if(adapter.getTotalCount() != 0){
                        listView.findViewById(R.id.page_text).setVisibility(View.VISIBLE);
                        noItem.setVisibility(View.GONE);
                    }

                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else if (response.code() == StatusCode.UnprocessableEntity) {
                    Timber.d("failed to post");

                    ModelErrorEntity error = ApiErrorUtil.parseError(response, retrofit);

                    Timber.d(error.toString());
                    for(Map.Entry<String, List<String>> e: error.errors.entrySet()){
                        switch(e.getKey()){
                            default:
                                //sendErrorToGetUser();
                                break;
                        }
                    }
                } else {

                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("user", "get failed");
            }
        });
    }

    public void getNextImageList(int userId, int offset, final FavoriteItemImageListAdapter adapter, final GridView gridView, final View footerView, final TextView noImage){
        Call<FavoriteItemImageListEntity> call = service.getFavoriteItemImages(userId, offset);
        call.enqueue(new Callback<FavoriteItemImageListEntity>() {
            @Override
            public void onResponse(Response<FavoriteItemImageListEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    FavoriteItemImageListEntity favorites = response.body();
                    adapter.finishLoadNextItem();
                    footerView.setVisibility(View.GONE);
                    adapter.addItem(favorites);
                    adapter.notifyDataSetChanged();
                    if(adapter.getTotalCount() != 0){
                        gridView.findViewById(R.id.item_image_tab).setVisibility(View.VISIBLE);
                        noImage.setVisibility(View.GONE);
                    }

                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else if (response.code() == StatusCode.UnprocessableEntity) {
                    Timber.d("failed to post");

                    ModelErrorEntity error = ApiErrorUtil.parseError(response, retrofit);

                    Timber.d(error.toString());
                    for(Map.Entry<String, List<String>> e: error.errors.entrySet()){
                        switch(e.getKey()){
                            default:
                                //sendErrorToGetUser();
                                break;
                        }
                    }
                } else {

                }
            }

            @Override
            public void onFailure(Throwable t) {
                Timber.d("failed to post timer");
                t.printStackTrace();
            }
        });
    }

    public View getTabView(int position, int count){
        View tab = activity.getLayoutInflater().inflate(R.layout.partial_favorite_item_tab_header, null);
        switch(position){
            case 0:
                TextView itemCount = (TextView)tab.findViewById(R.id.tab_count);
                itemCount.setText(String.valueOf(count));
                break;
            case 1:
                ImageView iconTypeImage = (ImageView)tab.findViewById(R.id.tab_icon);
                iconTypeImage.setImageResource(R.drawable.ic_image_black_18dp);
                TextView imageTab = (TextView)tab.findViewById(R.id.tab_name);
                imageTab.setText(R.string.favorite_image_list);
                TextView imageCount = (TextView)tab.findViewById(R.id.tab_count);
                imageCount.setText(String.valueOf(count));
                break;
        }

        return tab;
    }

    public static class UserFavoritesPagerAdapter extends PagerAdapter {

        private Activity activity;
        private StickyScrollPresenter stickyScrollPresenter;
        private UserFavoritesPresenter userFavoritesPresenter;
        private UserEntity user;
        private ItemEntity item;

        private View loader;
        private View imageLoader;

        private FavoriteItemListAdapter itemListAdapter;
        private ListView itemListView;
        private FavoriteItemImageListAdapter imageListAdapter;
        private GridView gridView;
        private TextView noItem;
        private TextView noImage;

        public UserFavoritesPagerAdapter(Activity a, StickyScrollPresenter s, UserFavoritesPresenter up, UserEntity u, ItemEntity i){
            activity = a;
            stickyScrollPresenter = s;
            userFavoritesPresenter = up;
            user = u;
            item = i;
            loader = View.inflate(a, R.layout.loading, null);
        }

        public void initialize(){
            if(itemListAdapter != null && imageListAdapter != null) {
                userFavoritesPresenter.getNextItemList(user.id, 0, itemListAdapter, itemListView, loader, noItem);
                userFavoritesPresenter.getNextImageList(user.id, 0, imageListAdapter, gridView, imageLoader, noImage);
            }
        }

        public void unshiftItem(ItemEntity itemEntity) {
            itemListAdapter.unshiftItem(itemEntity);
            itemListAdapter.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==((View)object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            View v;
            if(position == 0) {
                v = attachItemList(container);
            }else if (position == 1){
                v = attachImageGrid(container);
            }else {
                v = activity.getLayoutInflater().inflate(R.layout.item_list_tab, container, false);
            }
            container.addView(v);
            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        public View attachItemList(ViewGroup container){
            View v = activity.getLayoutInflater().inflate(R.layout.item_list_tab, container, false);

            noItem = (TextView)v.findViewById(R.id.no_item);

            itemListView = (ListView)v.findViewById(R.id.page_text);

            itemListAdapter = new FavoriteItemListAdapter(activity, R.layout.item_list, item);

            itemListView.setAdapter(itemListAdapter);

            itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                    @Override
                                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                        Log.d("click position", String.valueOf(position));
                                                        ItemActivity.startActivity(activity, (int) view.getTag(R.string.tag_item_id));
                                                    }
                                                }
            );

            itemListView.setOnTouchListener(new StickyScrollPresenter.CustomTouchListener(stickyScrollPresenter));

            itemListView.setOnScrollListener(new AbsListView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    Log.d("schroll state", "changed");
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    Log.d("scroll from listview", String.valueOf(visibleItemCount));
                    Timber.d("is null %s", itemListAdapter.hasNextItem());
                    if ((totalItemCount == firstVisibleItem + visibleItemCount) && itemListAdapter.hasNextItem()) {
                        if (!itemListAdapter.getIsLoadingNextItem()) {
                            itemListAdapter.startLoadNextItem();
                            itemListView.addFooterView(loader);
                            userFavoritesPresenter.getNextItemList(user.id, itemListAdapter.getNextPage(), itemListAdapter, itemListView, loader, noItem);
                        }
                    }
                }
            });

            itemListView.setVisibility(View.GONE);
            v.findViewById(R.id.no_item).setVisibility(View.VISIBLE);
            v.findViewById(R.id.no_item).setOnTouchListener(new StickyScrollPresenter.CustomTouchListener(stickyScrollPresenter));

            return v;
        }

        public View attachImageGrid(ViewGroup container){
            View v = activity.getLayoutInflater().inflate(R.layout.item_image_tab, container, false);

            noImage = (TextView)v.findViewById(R.id.no_image);
            imageLoader = (ProgressBar)v.findViewById(R.id.image_loader);
            gridView = (GridView)v.findViewById(R.id.item_image_tab);
            imageListAdapter = new FavoriteItemImageListAdapter(activity, R.layout.favorited_item_image_list, item);
            gridView.setAdapter(imageListAdapter);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                    ItemImageEntity i = imageListAdapter.getItem(position);
                                                    ImageDetailActivity.startActivity(activity, i.itemId, i.id);
                                                }
                                            }
            );

            gridView.setOnTouchListener(new StickyScrollPresenter.CustomTouchListener(stickyScrollPresenter));

            gridView.setOnScrollListener(new AbsListView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    Log.d("schroll state", "changed");
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    Log.d("scroll from gridview", String.valueOf(visibleItemCount));

                    if ((totalItemCount == firstVisibleItem + visibleItemCount) && imageListAdapter.hasNextItem()) {
                        Log.d("request", "to image api");
                        if (!imageListAdapter.getIsLoadingNextItem()) {
                            imageListAdapter.startLoadNextItem();
                            imageLoader.setVisibility(View.VISIBLE);
                            userFavoritesPresenter.getNextImageList(user.id, imageListAdapter.getNextPage(), imageListAdapter, gridView, imageLoader, noImage);
                        }
                    }

                }
            });

            gridView.setVisibility(View.GONE);
            v.findViewById(R.id.no_image).setVisibility(View.VISIBLE);
            v.findViewById(R.id.no_image).setOnTouchListener(new StickyScrollPresenter.CustomTouchListener(stickyScrollPresenter));

            return v;
        }

    }

    public static class FavoriteItemListAdapter extends ItemListAdapter {

        private int nextPageForItem;


        public FavoriteItemListAdapter(Context c, int resource, ItemEntity i) {
            super(c, resource, i);
            hasNextItemToLoad = true;
        }

        public void addItem(FavoriteItemListEntity favorites){
            Timber.d("add favorite items");

            itemList.addAll(favorites.owningItems);
            nextPageForItem = favorites.nextPageForItem;
            hasNextItemToLoad = favorites.hasNextItem;

        }

        public int getNextPage(){
            return nextPageForItem;
        }
        public int getTotalCount(){
            return itemList.size();
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = this.layoutInflater.inflate(layoutResource, null);
                holder = new ViewHolder();

                holder.ownerInfo = (LinearLayout)convertView.findViewById(R.id.owner_info);
                holder.ownerName = (TextView)convertView.findViewById(R.id.owner_name);

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

            holder.ownerInfo.setVisibility(View.VISIBLE);

            String prompt;
            if(item.isList){
                prompt = context.getString(R.string.owning_list);
            }else{
                prompt = context.getString(R.string.owning_item);
            }
            holder.ownerName.setText(item.owner.name + prompt);


            return super.getView(position, convertView, parent);
        }

        class ViewHolder extends ItemListAdapter.ViewHolder {

            LinearLayout ownerInfo;
            TextView ownerName;

            /*
            CircleImageView thumbnail;
            ImageView itemType;
            TextView name;
            TextView count;
            TextView favoriteCount;
            FlowLayout tags;
            */
        }
    }

    public static class FavoriteItemImageListAdapter extends ItemImageListAdapter {

        private int nextPageForImage;

        public FavoriteItemImageListAdapter(Context c, int resource, ItemEntity i) {
            super(c, resource, i, new ItemPresenter(c));
            hasNextItemToLoad = true;
        }

        public void addItem(FavoriteItemImageListEntity favorites){
            itemImageList.addAll(favorites.images);
            nextPageForImage = favorites.nextPageForImage;
            hasNextItemToLoad = favorites.hasNextImage;
        }

        public int getNextPage(){
            return nextPageForImage;
        }
        public int getTotalCount(){
            return itemImageList.size();
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = layoutInflater.inflate(layoutResource, null);
                holder = new ViewHolder();

                holder.image = (ImageView)convertView.findViewById(R.id.item_image);
                holder.itemName = (TextView)convertView.findViewById(R.id.item_name);
                holder.ownerName = (TextView)convertView.findViewById(R.id.owner_name);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ItemImageEntity itemImage = getItem(position);

            String thumbnailUrl = ApiService.BASE_URL + itemImage.url;
            Glide.with(context).load(thumbnailUrl).into(holder.image);

            holder.itemName.setText(itemImage.itemName);
            holder.ownerName.setText(itemImage.ownerName + context.getString(R.string.owning_item));

            return convertView;
        }

        class ViewHolder{

            ImageView image;
            TextView itemName;
            TextView ownerName;

        }

    }
}
