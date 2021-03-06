package work.t_s.shim0mura.havings.presenter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.Nullable;
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

import com.wefika.flowlayout.FlowLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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
import work.t_s.shim0mura.havings.model.ApiKey;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.GeneralResult;
import work.t_s.shim0mura.havings.model.StatusCode;
import work.t_s.shim0mura.havings.model.User;
import work.t_s.shim0mura.havings.model.entity.CountDataEntity;
import work.t_s.shim0mura.havings.model.entity.EventEntity;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageListEntity;
import work.t_s.shim0mura.havings.model.entity.ModelErrorEntity;
import work.t_s.shim0mura.havings.model.entity.ResultEntity;
import work.t_s.shim0mura.havings.model.event.AlertEvent;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.util.ApiErrorUtil;
import work.t_s.shim0mura.havings.util.ViewUtil;
import work.t_s.shim0mura.havings.view.GraphRenderer;
import work.t_s.shim0mura.havings.view.ItemImageListAdapter;
import work.t_s.shim0mura.havings.view.ItemListAdapter;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;

/**
 * Created by shim0mura on 2015/11/14.
 */
public class ItemPresenter {
    User user;
    Activity activity;
    static ApiService service;

    protected String TAG = "ItemPresenter: ";

    public ItemPresenter(Context c){
        user = User.getSingleton(c);
        activity = (Activity)c;
        service = ApiServiceManager.getService(activity);
    }

    public void getItem(int itemId){
        Call<ItemEntity> call = service.getItem(itemId);

        call.enqueue(new Callback<ItemEntity>() {
            @Override
            public void onResponse(Response<ItemEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    ItemEntity item = response.body();
                    BusHolder.get().post(item);

                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else {
                    BusHolder.get().post(new AlertEvent(AlertEvent.SOMETHING_OCCURED_IN_SERVER));

                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("item", "get failed");
                t.printStackTrace();
            }
        });
    }

    public void getNextItemList(int itemId, int offset, final ItemListAdapter adapter, final ListView listView, final View footerView){
        Call<ItemEntity> call = service.getNextItem(itemId, offset);

        call.enqueue(new Callback<ItemEntity>() {
            @Override
            public void onResponse(Response<ItemEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    ItemEntity item = response.body();
                    adapter.finishLoadNextItem();
                    listView.removeFooterView(footerView);
                    adapter.addItem(item);
                    adapter.notifyDataSetChanged();

                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else {

                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("user", "get failed");
            }
        });
    }

    public void getNextItemImageList(int itemId, int offset, final ItemImageListAdapter adapter, final ViewGroup gridView, final View footerView){
        Call<ItemImageListEntity> call = service.getNextItemImage(itemId, offset);

        call.enqueue(new Callback<ItemImageListEntity>() {
            @Override
            public void onResponse(Response<ItemImageListEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    ItemImageListEntity itemImageList = response.body();
                    adapter.finishLoadNextItem();
                    footerView.setVisibility(View.GONE);
                    adapter.addItem(itemImageList);
                    adapter.notifyDataSetChanged();

                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else {

                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("user", "get failed");
            }
        });
    }

    public void getItemImage(int itemId, int itemImageId){
        Call<ItemImageEntity> call = service.getItemImage(itemId, itemImageId);

        call.enqueue(new Callback<ItemImageEntity>() {
            @Override
            public void onResponse(Response<ItemImageEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    ItemImageEntity itemImage = response.body();
                    BusHolder.get().post(itemImage);
                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else {
                    Timber.d("failed to post");

                    ModelErrorEntity error = ApiErrorUtil.parseError(response, retrofit);

                    BusHolder.get().post(new SetErrorEvent(GeneralResult.RESULT_GET_ITEM_IMAGE, error));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("user", "get failed");
            }
        });
    }

    public void updateImageData(int itemId, int itemImageId, Date date, String memo){

        ItemEntity item = new ItemEntity();
        ItemImageEntity image = new ItemImageEntity();
        image.date = date;
        image.memo = memo;
        item.imageDataForPost = new ArrayList<ItemImageEntity>();
        item.imageDataForPost.add(image);
        item.id = itemId;
        HashMap<String, ItemEntity> hashItem = new HashMap<String, ItemEntity>();
        hashItem.put(FormPresenter.ITEM_POST_HASH_KEY, item);

        Call<ResultEntity> call = service.updateImageData(itemId, itemImageId, hashItem);

        call.enqueue(new Callback<ResultEntity>() {
            @Override
            public void onResponse(Response<ResultEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    ResultEntity result = response.body();
                    result.resultType = GeneralResult.RESULT_UPDATE_ITEM_IMAGE;
                    BusHolder.get().post(result);
                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else {
                    Timber.d("failed to post");

                    ModelErrorEntity error = ApiErrorUtil.parseError(response, retrofit);

                    BusHolder.get().post(new SetErrorEvent(GeneralResult.RESULT_UPDATE_ITEM_IMAGE, error));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("user", "get failed");
            }
        });
    }

    public void deleteImage(int itemId, int itemImageId){

        Call<ResultEntity> call = service.deleteImage(itemId, itemImageId);

        call.enqueue(new Callback<ResultEntity>() {
            @Override
            public void onResponse(Response<ResultEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    ResultEntity result = response.body();
                    result.resultType = GeneralResult.RESULT_DELETE_ITEM_IMAGE;
                    BusHolder.get().post(result);
                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else {
                    Timber.d("failed to post");

                    ModelErrorEntity error = ApiErrorUtil.parseError(response, retrofit);

                    BusHolder.get().post(new SetErrorEvent(GeneralResult.RESULT_DELETE_ITEM_IMAGE, error));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("user", "get failed");
            }
        });
    }

    public void favoriteItem(int itemId){
        Call<ResultEntity> call = service.favoriteItem(itemId);
        call.enqueue(getCallbackOfSuccessToPostItem(GeneralResult.RESULT_FAVORITE_ITEM));
    }

    public void unfavoriteItem(int itemId){
        Call<ResultEntity> call = service.unfavoriteItem(itemId);
        call.enqueue(getCallbackOfSuccessToPostItem(GeneralResult.RESULT_UNFAVORITE_ITEM));
    }

    public void favoriteItemImage(int itemImageId){
        Call<ResultEntity> call = service.favoriteItemImage(itemImageId);
        call.enqueue(getCallbackOfSuccessToPostItem(GeneralResult.RESULT_FAVORITE_ITEM_IMAGE));
    }

    public void unfavoriteItemImage(int itemImageId){
        Call<ResultEntity> call = service.unfavoriteItemImage(itemImageId);
        call.enqueue(getCallbackOfSuccessToPostItem(GeneralResult.RESULT_UNFAVORITE_ITEM_IMAGE));
    }

    public void favoriteItemImageFromAdapter(int itemImageId, ItemImageListAdapter adapter){
        Call<ResultEntity> call = service.favoriteItemImage(itemImageId);
        call.enqueue(getCallbackOfSuccessToChangeItemImageData(itemImageId, true, adapter, GeneralResult.RESULT_FAVORITE_ITEM_IMAGE));
    }

    public void unfavoriteItemImageFromAdapter(int itemImageId, ItemImageListAdapter adapter){
        Call<ResultEntity> call = service.unfavoriteItemImage(itemImageId);
        call.enqueue(getCallbackOfSuccessToChangeItemImageData(itemImageId, false, adapter, GeneralResult.RESULT_UNFAVORITE_ITEM_IMAGE));
    }

    private Callback<ResultEntity> getCallbackOfSuccessToPostItem(final int resultType){
        return new Callback<ResultEntity>() {
            @Override
            public void onResponse(Response<ResultEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    ResultEntity result = response.body();
                    if(result.resultType == 0){
                        result.resultType = resultType;
                    }
                    Timber.d("result %s, result.resultType: %s, result.id: %s", resultType, result.resultType, result.relatedId);
                    BusHolder.get().post(result);
                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else if (response.code() == StatusCode.UnprocessableEntity) {
                    Timber.d("failed to post");

                    ModelErrorEntity error = ApiErrorUtil.parseError(response, retrofit);

                    BusHolder.get().post(new SetErrorEvent(resultType, error));

                } else {

                }
            }

            @Override
            public void onFailure(Throwable t) {
                Timber.d("failed to post item");
                t.printStackTrace();
            }
        };
    }

    private Callback<ResultEntity> getCallbackOfSuccessToChangeItemImageData(final int imageId, final Boolean isFavorited, final ItemImageListAdapter adapter, final int resultType){
        return new Callback<ResultEntity>() {
            @Override
            public void onResponse(Response<ResultEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    ResultEntity result = response.body();
                    if(result.resultType == 0){
                        result.resultType = resultType;
                    }
                    Timber.d("result %s, result.resultType: %s, result.id: %s", resultType, result.resultType, result.relatedId);
                    adapter.changeImageFavoriteState(imageId, isFavorited);
                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else if (response.code() == StatusCode.UnprocessableEntity) {
                    Timber.d("failed to post");

                    ModelErrorEntity error = ApiErrorUtil.parseError(response, retrofit);

                    BusHolder.get().post(new SetErrorEvent(resultType, error));

                } else {

                }
            }

            @Override
            public void onFailure(Throwable t) {
                Timber.d("failed to post item");
                t.printStackTrace();
            }
        };
    }

    public View getTabView(int position, boolean isList, int count){
        View tab = activity.getLayoutInflater().inflate(R.layout.partial_item_tab_header, null);
        if(isList){
            switch(position) {
                case 0:
                    TextView itemCount = (TextView) tab.findViewById(R.id.tab_count);
                    itemCount.setText(String.valueOf(count));
                    break;
                case 1:
                    ImageView iconTypeImage = (ImageView) tab.findViewById(R.id.tab_icon);
                    iconTypeImage.setImageResource(R.drawable.ic_image_white_24dp);
                    TextView imageTab = (TextView) tab.findViewById(R.id.tab_name);
                    imageTab.setText(R.string.item_images);
                    TextView imageCount = (TextView) tab.findViewById(R.id.tab_count);
                    imageCount.setText(String.valueOf(count));
                    break;
                case 2:
                    ImageView iconTypeGraph = (ImageView) tab.findViewById(R.id.tab_icon);
                    iconTypeGraph.setImageResource(R.drawable.ic_timeline_white_24dp);
                    TextView graphTab = (TextView) tab.findViewById(R.id.tab_name);
                    graphTab.setText(R.string.item_graph);
                    TextView graphCount = (TextView) tab.findViewById(R.id.tab_count);
                    graphCount.setVisibility(View.GONE);
            }
        }else{
            switch(position) {
                case 0:
                    ImageView iconTypeImage = (ImageView) tab.findViewById(R.id.tab_icon);
                    iconTypeImage.setImageResource(R.drawable.ic_image_black_18dp);
                    TextView imageTab = (TextView) tab.findViewById(R.id.tab_name);
                    imageTab.setText(R.string.item_images);
                    TextView imageCount = (TextView) tab.findViewById(R.id.tab_count);
                    imageCount.setText(String.valueOf(count));
                    break;
                case 1:
                    ImageView iconTypeGraph = (ImageView) tab.findViewById(R.id.tab_icon);
                    iconTypeGraph.setImageResource(R.drawable.ic_timeline_black_18dp);
                    TextView graphTab = (TextView) tab.findViewById(R.id.tab_name);
                    graphTab.setText(R.string.item_graph);
                    TextView graphCount = (TextView) tab.findViewById(R.id.tab_count);
                    graphCount.setVisibility(View.GONE);
            }

        }

        return tab;
    }

    public static TextView createTag(Context context, String tag, @Nullable Boolean isMultiline){
        TextView baseTag = new TextView(context);
        Paint mPaint = new Paint();
        mPaint.setTextSize(14);
        float width = mPaint.measureText(tag, 0, tag.length()) + 30 * tag.length();

        FlowLayout.LayoutParams lp;

        // 何故かよく分からないけど、ItemActivityから呼ぶとき(=isMultiline)は
        // widthが小さくなって文字が見えなくなるので、自分で計算してる
        if(isMultiline){
            lp = new FlowLayout.LayoutParams((int)width, 55);
            //lp = new FlowLayout.LayoutParams(FlowLayout.LayoutParams.WRAP_CONTENT, FlowLayout.LayoutParams.WRAP_CONTENT);

        }else{
            lp = new FlowLayout.LayoutParams(FlowLayout.LayoutParams.WRAP_CONTENT, FlowLayout.LayoutParams.WRAP_CONTENT);
            baseTag.setPadding(10, 0, 10, 0);

        }
        int marginTop = 0;
        if(isMultiline){
            marginTop = 15;
        }
        lp.setMargins(0, marginTop, 20, 0);

        baseTag.setTextSize(COMPLEX_UNIT_DIP, 13);
        baseTag.setBackgroundColor(ContextCompat.getColor(context, R.color.tagColor));
        baseTag.setLayoutParams(lp);
        baseTag.setText(tag);
        return baseTag;
    }

    public static class ItemPagerAdapter extends PagerAdapter {

        private Activity activity;
        private StickyScrollPresenter stickyScrollPresenter;
        private ItemPresenter itemPresenter;
        private ItemEntity item;
        private View loader;
        private int userId;

        private ItemListAdapter itemListAdapter;
        private ItemImageListAdapter imageListAdapter;
        private ListView itemListView;

        public ItemPagerAdapter(Activity a, StickyScrollPresenter s, ItemPresenter ip, ItemEntity i){
            activity = a;
            stickyScrollPresenter = s;
            itemPresenter = ip;
            item = i;
            loader = View.inflate(a, R.layout.loading, null);
            userId = ApiKey.getSingleton(a).getUserId();
        }

        public void unshiftItem(ItemEntity itemEntity) {
            itemListAdapter.unshiftItem(itemEntity);
            itemListAdapter.notifyDataSetChanged();
        }

        public void shiftItem(ItemEntity itemEntity) {
            itemListAdapter.shiftItem(itemEntity);
            itemListAdapter.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if(item.isList){
                return 3;
            }else{
                return 2;
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==((View)object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            Timber.d("customview ,position %s", position);
            View v;

            if(item.isList){

                if(position == 0) {
                    v = attachOwningItem(container);
                }else if (position == 1){
                    v = attachImageGrid(container);
                }else if (position == 2){
                    v = attachGraph(container);
                }else {
                    v = activity.getLayoutInflater().inflate(R.layout.item_list_tab, container, false);
                }
            }else{
                if (position == 0){
                    v = attachImageGrid(container);
                }else if (position == 1){
                    v = attachGraph(container);
                }else {
                    v = activity.getLayoutInflater().inflate(R.layout.item_list_tab, container, false);
                }
            }

            container.addView(v);
            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        public void addImage(ItemImageListEntity itemImageList){
            imageListAdapter.unshiftItem(itemImageList);
            imageListAdapter.notifyDataSetChanged();
        }

        public View attachOwningItem(ViewGroup container){
            View v = activity.getLayoutInflater().inflate(R.layout.item_list_tab, container, false);

            final ListView listView = (ListView) v.findViewById(R.id.page_text);

            if(item.owningItems.isEmpty()) {

                TextView nothing = (TextView) v.findViewById(R.id.no_item);
                nothing.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
                nothing.setOnTouchListener(new StickyScrollPresenter.CustomTouchListener(stickyScrollPresenter));
                if(item.owner.id == userId && item.isList){
                    nothing.setText(R.string.prompt_no_item_with_self_list);
                }else{
                    nothing.setText(R.string.prompt_no_item);
                }
            }

            itemListAdapter = new ItemListAdapter(activity, R.layout.item_list, item);

            listView.setAdapter(itemListAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                    Log.d("click position", String.valueOf(position));
                                                    ItemActivity.startActivity(activity, (int)view.getTag(R.string.tag_item_id));
                                                }
                                            }
            );

            listView.setOnTouchListener(new StickyScrollPresenter.CustomTouchListener(stickyScrollPresenter));

            listView.setOnScrollListener(new AbsListView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    Log.d("schroll state", "changed");
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    Log.d("scroll from listview", String.valueOf(visibleItemCount));
                    if ((totalItemCount == firstVisibleItem + visibleItemCount) && itemListAdapter.hasNextItem()) {
                        if (!itemListAdapter.getIsLoadingNextItem()) {
                            itemListAdapter.startLoadNextItem();
                            listView.addFooterView(loader);
                            itemPresenter.getNextItemList(item.id, itemListAdapter.getNextPage(), itemListAdapter, listView, loader);
                        }
                    }
                }
            });

            return v;
        }

        public View attachImageGrid(ViewGroup container){
            View v = activity.getLayoutInflater().inflate(R.layout.item_image_tab, container, false);

            final ProgressBar imageLoader = (ProgressBar)v.findViewById(R.id.image_loader);
            final GridView gridView = (GridView)v.findViewById(R.id.item_image_tab);
            if(item.itemImages.images.isEmpty()) {

                TextView nothing = (TextView) v.findViewById(R.id.no_image);
                nothing.setVisibility(View.VISIBLE);
                gridView.setVisibility(View.GONE);
                nothing.setOnTouchListener(new StickyScrollPresenter.CustomTouchListener(stickyScrollPresenter));
                if(item.owner.id == userId){
                    nothing.setText(R.string.prompt_no_image_with_self_list);
                }
            }

            imageListAdapter = new ItemImageListAdapter(activity, R.layout.item_image_list, item, itemPresenter);
            gridView.setAdapter(imageListAdapter);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                    Timber.d("image id %s ", imageListAdapter.getItem(position).id);
                                                    //ImageDetailActivity.startActivity(activity, item, (String) view.getTag(R.string.tag_image_url), (Date) view.getTag(R.string.tag_image_date));
                                                    ImageDetailActivity.startActivity(activity, item, imageListAdapter.getItem(position));

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
                            itemPresenter.getNextItemImageList(item.id, imageListAdapter.getNextPage(), imageListAdapter, gridView, imageLoader);
                        }
                    }

                }
            });

            return v;
        }

        public View attachGraph(ViewGroup container){
            View v = activity.getLayoutInflater().inflate(R.layout.item_graph_tab, container, false);

            final RelativeLayout graphView = (RelativeLayout)v.findViewById(R.id.item_graph_tab_wrapper);

            LineChartView lineChartView = (LineChartView)graphView.findViewById(R.id.item_graph);

            lineChartView.setOnTouchListener(new StickyScrollPresenter.CustomTouchListener(stickyScrollPresenter));
            GraphRenderer.renderSimpleLineGraph(lineChartView, new ArrayList<CountDataEntity>(item.countProperties));
            graphView.setOnTouchListener(new StickyScrollPresenter.CustomTouchListener(stickyScrollPresenter));

            v.findViewById(R.id.navigate_to_detailgraph).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DetailGraphActivity.startActivity(activity, item);
                }
            });

            LinearLayout graphAct = (LinearLayout)v.findViewById(R.id.graph_activity);

            int activityCount = 0;
            for(int i = 0; i < item.countProperties.size(); i++){
                CountDataEntity countDataEntity = item.countProperties.get(item.countProperties.size() - 1 - i);
                View activityWrapper = activity.getLayoutInflater().inflate(R.layout.partial_recent_activity_wrapper, null);

                TextView eventDate = (TextView)activityWrapper.findViewById(R.id.event_date);
                eventDate.setText(ViewUtil.dateToString(countDataEntity.date, true));
                TextView itemCount = (TextView)activityWrapper.findViewById(R.id.item_count);
                itemCount.setText(String.valueOf(countDataEntity.count));
                LinearLayout recentActivity = (LinearLayout)activityWrapper.findViewById(R.id.recent_activity);

                if(countDataEntity.events != null && !countDataEntity.events.isEmpty()) {
                    for (EventEntity eventEntity : countDataEntity.events) {
                        if(eventEntity.item == null) {
                            continue;
                        }

                        View activityItem = activity.getLayoutInflater().inflate(R.layout.partial_recent_activity_item, null);

                        TextView eventTypeText = (TextView) activityItem.findViewById(R.id.event_type_text);
                        ImageView eventType = (ImageView) activityItem.findViewById(R.id.event_type_icon);
                        switch (eventEntity.eventType) {
                            case EventEntity.EVENT_TYPE_ADD_ITEM:
                                eventType.setImageResource(R.drawable.ic_add_black_18dp);
                                eventTypeText.setText(eventEntity.item.name + activity.getString(R.string.event_type_add_something_to));
                                break;
                            case EventEntity.EVENT_TYPE_ADD_LIST:
                                eventType.setImageResource(R.drawable.ic_add_black_18dp);
                                eventTypeText.setText(eventEntity.item.name + activity.getString(R.string.event_type_add_something_to));
                                break;
                            case EventEntity.EVENT_TYPE_ADD_IMAGE:
                                eventType.setImageResource(R.drawable.ic_image_black_18dp);
                                eventTypeText.setText(eventEntity.item.name + activity.getString(R.string.event_type_add_image_to));
                                break;
                            case EventEntity.EVENT_TYPE_DUMP_ITEM:
                                eventType.setImageResource(R.drawable.ic_delete_black_18dp);
                                eventTypeText.setText(eventEntity.item.name + activity.getString(R.string.event_type_drop_something_to));
                                break;
                        }
                        activityCount += 1;
                        Timber.d("activicount %s", activityCount);
                        if(activityCount > 3){
                            break;
                        }
                        recentActivity.addView(activityItem);

                    }


                }

                graphAct.addView(activityWrapper);
                activityCount += 1;
                Timber.d("activicount %s", activityCount);

                if(activityCount > 3){
                    break;
                }

            }

            int countSize = item.countProperties.size();
            TextView graphFrom = (TextView)v.findViewById(R.id.item_graph_date_from);
            graphFrom.setText(ViewUtil.dateToString(item.countProperties.get(0).date, true));
            TextView graphTo = (TextView)v.findViewById(R.id.item_graph_date_to);
            graphTo.setText(ViewUtil.dateToString(item.countProperties.get(countSize - 1).date, true));

            return v;
        }
    }

}
