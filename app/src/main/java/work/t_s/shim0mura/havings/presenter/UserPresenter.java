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
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.view.LineChartView;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import timber.log.Timber;
import work.t_s.shim0mura.havings.DetailGraphActivity;
import work.t_s.shim0mura.havings.ImageDetailActivity;
import work.t_s.shim0mura.havings.ItemActivity;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.GeneralResult;
import work.t_s.shim0mura.havings.model.StatusCode;
import work.t_s.shim0mura.havings.model.User;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageListEntity;
import work.t_s.shim0mura.havings.model.entity.ModelErrorEntity;
import work.t_s.shim0mura.havings.model.entity.NotificationEntity;
import work.t_s.shim0mura.havings.model.entity.ResultEntity;
import work.t_s.shim0mura.havings.model.entity.TimerEntity;
import work.t_s.shim0mura.havings.model.entity.UserEntity;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.util.ApiErrorUtil;
import work.t_s.shim0mura.havings.util.ViewUtil;
import work.t_s.shim0mura.havings.view.GraphRenderer;
import work.t_s.shim0mura.havings.view.ItemImageListAdapter;
import work.t_s.shim0mura.havings.view.ItemListAdapter;
import work.t_s.shim0mura.havings.view.NestedItemListAdapter;
import work.t_s.shim0mura.havings.view.UserListAdapter;

/**
 * Created by shim0mura on 2016/01/30.
 */
public class UserPresenter {

    Activity activity;
    static ApiService service;

    public UserPresenter(Context c){
        activity = (Activity)c;
        service = ApiServiceManager.getService(activity);
    }

    public void getUser(int userId){
        Call<UserEntity> call = service.getUser(userId);
        call.enqueue(new Callback<UserEntity>() {
            @Override
            public void onResponse(Response<UserEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    UserEntity user = response.body();
                    BusHolder.get().post(user);
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

    public void getNextItemList(int userId, int offset, final ItemListAdapter adapter, final ListView listView, final View footerView){
        Call<ItemEntity> call = service.getUserItemList(userId, offset);

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

    public void getNextImageList(int userId, int offset, final ItemImageListAdapter adapter, final View footerView){
        Call<ItemImageListEntity> call = service.getUserItemImages(userId, offset);
        call.enqueue(new Callback<ItemImageListEntity>() {
            @Override
            public void onResponse(Response<ItemImageListEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    ItemImageListEntity itemImages = response.body();
                    adapter.finishLoadNextItem();
                    footerView.setVisibility(View.GONE);
                    adapter.addItem(itemImages);
                    adapter.notifyDataSetChanged();

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

    public void getDumpItemList(int userId, int offset){
        Call<ItemEntity> call = service.getDumpItemList(userId, offset);

        call.enqueue(new Callback<ItemEntity>() {
            @Override
            public void onResponse(Response<ItemEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    ItemEntity item = response.body();
                    BusHolder.get().post(item);

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

    public void getNotifications(){
        Call<List<NotificationEntity>> call = service.getNotifications();

        call.enqueue(new Callback<List<NotificationEntity>>() {
            @Override
            public void onResponse(Response<List<NotificationEntity>> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    List<NotificationEntity> notifications = response.body();
                    BusHolder.get().post(notifications);

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

    public void getNotificationCount(){
        Call<List<NotificationEntity>> call = service.getNotificationCount();

        call.enqueue(new Callback<List<NotificationEntity>>() {
            @Override
            public void onResponse(Response<List<NotificationEntity>> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    List<NotificationEntity> notifications = response.body();
                    BusHolder.get().post(notifications);

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

    public void readNotifications(){
        Call<ResultEntity> call = service.readNotifications();

        call.enqueue(new Callback<ResultEntity>() {
            @Override
            public void onResponse(Response<ResultEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    ResultEntity result = response.body();
                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else if (response.code() == StatusCode.UnprocessableEntity) {
                    Timber.d("failed to post");

                    ModelErrorEntity error = ApiErrorUtil.parseError(response, retrofit);

                    BusHolder.get().post(new SetErrorEvent(GeneralResult.RESULT_READ_NOTIFICATIONS, error));

                } else {

                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("user", "get failed");
            }
        });
    }

    public void followUser(int userId){
        Call<ResultEntity> call = service.followUser(userId);
        call.enqueue(getCallbackOfSuccessToActionUser(GeneralResult.RESULT_FOLLOW_USER));
    }

    public void unfollowUser(int userId){
        Call<ResultEntity> call = service.unfollowUser(userId);
        call.enqueue(getCallbackOfSuccessToActionUser(GeneralResult.RESULT_UNFOLLOW_USER));
    }

    public void followUserFromAdapter(int userId, UserListAdapter adapter){
        Call<ResultEntity> call = service.followUser(userId);
        call.enqueue(getCallbackOfChangeFollowingState(userId, true, adapter, GeneralResult.RESULT_FOLLOW_USER));
    }

    public void unfollowUserFromAdapter(int userId, UserListAdapter adapter){
        Call<ResultEntity> call = service.unfollowUser(userId);
        call.enqueue(getCallbackOfChangeFollowingState(userId, false, adapter, GeneralResult.RESULT_FOLLOW_USER));
    }

    private Callback<ResultEntity> getCallbackOfSuccessToActionUser(final int resultType){
        return new Callback<ResultEntity>() {
            @Override
            public void onResponse(Response<ResultEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    ResultEntity result = response.body();
                    if(result.resultType == 0){
                        result.resultType = resultType;
                    }
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

    private Callback<ResultEntity> getCallbackOfChangeFollowingState(final int userId, final Boolean follow, final UserListAdapter adapter, final int resultType){
        return new Callback<ResultEntity>() {
            @Override
            public void onResponse(Response<ResultEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    ResultEntity result = response.body();
                    if(result.resultType == 0){
                        result.resultType = resultType;
                    }
                    Timber.d("result %s, result.resultType: %s, result.id: %s", resultType, result.resultType, result.relatedId);
                    adapter.changeFollowingState(userId, follow);
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

    public static class UserPagerAdapter extends PagerAdapter {

        private Activity activity;
        private StickyScrollPresenter stickyScrollPresenter;
        private UserPresenter userPresenter;
        private ItemPresenter itemPresenter;
        private UserEntity user;
        private View loader;

        private ItemListAdapter itemListAdapter;
        private NestedItemListAdapter nestedItemListAdapter;
        private ListView itemListView;
        private ListView nestedItemListView;
        private LinearLayout showNestedItemList;
        private LinearLayout showItemList;

        private boolean isNestedItemShown = false;

        public UserPagerAdapter(Activity a, StickyScrollPresenter s, UserPresenter up, ItemPresenter ip, UserEntity u){
            activity = a;
            stickyScrollPresenter = s;
            userPresenter = up;
            itemPresenter = ip;
            user = u;
            loader = View.inflate(a, R.layout.loading, null);
        }

        public void unshiftItem(ItemEntity itemEntity) {
            itemListAdapter.unshiftItem(itemEntity);
            itemListAdapter.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==((View)object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            Timber.d("customview ,position %s", position);
            View v;
            if(position == 0) {
                v = attachItemList(container);
            }else if (position == 1){
                v = attachImageGrid(container);
            }else if (position == 2){
                v = attachGraph(container);
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

            itemListView = (ListView)v.findViewById(R.id.page_text);
            nestedItemListView = (ListView)v.findViewById(R.id.nested_list_view);

            itemListAdapter = new ItemListAdapter(activity, R.layout.item_list, user.homeList);
            List<ItemEntity> items;
            if(user.nestedItems.size() > 0){
                items = user.nestedItems.get(0).owningItems;
            }else{
                items = user.nestedItems;
            }
            nestedItemListAdapter = new NestedItemListAdapter(activity, R.layout.list_nested_item, items);

            itemListView.setAdapter(itemListAdapter);
            nestedItemListView.setAdapter(nestedItemListAdapter);

            itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                    @Override
                                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                        Log.d("click position", String.valueOf(position));
                                                        ItemActivity.startActivity(activity, (int) view.getTag(R.string.tag_item_id));
                                                    }
                                                }
            );

            itemListView.setOnTouchListener(new StickyScrollPresenter.CustomTouchListener(stickyScrollPresenter));
            nestedItemListView.setOnTouchListener(new StickyScrollPresenter.CustomTouchListener(stickyScrollPresenter));

            View headerView = View.inflate(activity, R.layout.partial_item_list_type_change_header, null);
            showNestedItemList = (LinearLayout)headerView.findViewById(R.id.show_nested);
            showNestedItemList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleItemListView();
                }
            });
            showItemList = (LinearLayout)headerView.findViewById(R.id.show_list);
            showItemList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleItemListView();
                }
            });
            itemListView.addHeaderView(headerView);
            nestedItemListView.addHeaderView(headerView);

            itemListView.setOnScrollListener(new AbsListView.OnScrollListener() {

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
                            itemListView.addFooterView(loader);
                            userPresenter.getNextItemList(user.id, itemListAdapter.getLastItemId(), itemListAdapter, itemListView, loader);
                        }
                    }
                }
            });
            toggleItemListView();

            if(itemListAdapter.getCount() == 0 && !itemListAdapter.hasNextItem()){
                itemListView.setVisibility(View.GONE);
                nestedItemListView.setVisibility(View.GONE);
                v.findViewById(R.id.no_item).setVisibility(View.VISIBLE);
            }

            return v;
        }

        public View attachImageGrid(ViewGroup container){
            View v = activity.getLayoutInflater().inflate(R.layout.item_image_tab, container, false);

            final ProgressBar imageLoader = (ProgressBar)v.findViewById(R.id.image_loader);
            final GridView gridView = (GridView)v.findViewById(R.id.item_image_tab);
            final ItemImageListAdapter adapter = new ItemImageListAdapter(activity, R.layout.item_image_list, user.homeList, itemPresenter);
            gridView.setAdapter(adapter);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                    Timber.d("image id %s ", adapter.getItem(position).id);
                                                    //ImageDetailActivity.startActivity(activity, item, (String) view.getTag(R.string.tag_image_url), (Date) view.getTag(R.string.tag_image_date));
                                                    //ImageDetailActivity.startActivity(activity, item, adapter.getItem(position));

                                                    ItemImageEntity i = adapter.getItem(position);
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

                    if ((totalItemCount == firstVisibleItem + visibleItemCount) && adapter.hasNextItem()) {
                        Log.d("request", "to image api");
                        if (!adapter.getIsLoadingNextItem()) {
                            adapter.startLoadNextItem();
                            imageLoader.setVisibility(View.VISIBLE);
                            //itemPresenter.getNextItemImageList(item.id, adapter.getLastItemImageId(), adapter, gridView, imageLoader);
                            userPresenter.getNextImageList(user.id, adapter.getLastItemImageId(), adapter, imageLoader);

                        }
                    }

                }
            });

            if(adapter.getCount() == 0 && !adapter.hasNextItem()){
                gridView.setVisibility(View.GONE);
                v.findViewById(R.id.no_image).setVisibility(View.VISIBLE);
            }

            return v;
        }

        public View attachGraph(ViewGroup container){
            View v = activity.getLayoutInflater().inflate(R.layout.item_graph_tab, container, false);

            final RelativeLayout graphView = (RelativeLayout)v.findViewById(R.id.item_graph_tab_wrapper);

            graphView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("graph", "clicked");

                }
            });

            LineChartView lineChartView = (LineChartView)graphView.findViewById(R.id.item_graph);

            lineChartView.setOnTouchListener(new StickyScrollPresenter.CustomTouchListener(stickyScrollPresenter));
            GraphRenderer.renderSimpleGraph(lineChartView, user.homeList.countProperties);
            graphView.setOnTouchListener(new StickyScrollPresenter.CustomTouchListener(stickyScrollPresenter));

            v.findViewById(R.id.navigate_to_detailgraph).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DetailGraphActivity.startActivity(activity, user.homeList);
                }
            });

            int countSize = user.homeList.countProperties.size();
            TextView graphFrom = (TextView)v.findViewById(R.id.item_graph_date_from);
            graphFrom.setText(ViewUtil.dateToString(user.homeList.countProperties.get(0).date, true));
            TextView graphTo = (TextView)v.findViewById(R.id.item_graph_date_to);
            graphTo.setText(ViewUtil.dateToString(user.homeList.countProperties.get(countSize - 1).date, true));

            return v;
        }

        public void toggleItemListView(){
            if(isNestedItemShown){
                nestedItemListView.setVisibility(View.GONE);
                itemListView.setVisibility(View.VISIBLE);
                //showNestedItemList.setVisibility(View.VISIBLE);
                //showItemList.setVisibility(View.GONE);
                showItemList.setBackgroundColor(ContextCompat.getColor(activity, R.color.unable));
                showNestedItemList.setBackgroundColor(ContextCompat.getColor(activity, android.R.color.transparent));

                isNestedItemShown = false;
            }else{
                nestedItemListView.setVisibility(View.VISIBLE);
                itemListView.setVisibility(View.GONE);
                //showNestedItemList.setVisibility(View.GONE);
                //showItemList.setVisibility(View.VISIBLE);
                showNestedItemList.setBackgroundColor(ContextCompat.getColor(activity, R.color.unable));
                showItemList.setBackgroundColor(ContextCompat.getColor(activity, android.R.color.transparent));
                isNestedItemShown = true;
            }
        }
    }

}
