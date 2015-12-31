package work.t_s.shim0mura.havings.presenter;

import android.app.Activity;
import android.content.Context;
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
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wefika.flowlayout.FlowLayout;

import java.util.Date;

import butterknife.ButterKnife;
import lecho.lib.hellocharts.view.LineChartView;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import work.t_s.shim0mura.havings.DetailGraphActivity;
import work.t_s.shim0mura.havings.ImageDetailActivity;
import work.t_s.shim0mura.havings.ItemActivity;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.StatusCode;
import work.t_s.shim0mura.havings.model.User;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.util.ViewUtil;
import work.t_s.shim0mura.havings.view.GraphRenderer;
import work.t_s.shim0mura.havings.view.ItemImageListAdapter;
import work.t_s.shim0mura.havings.view.ItemListAdapter;

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

    public void test(){
        ImageView imageView = ButterKnife.findById(activity, R.id.image);
        //Glide.get(activity).register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(asm.getHttpClient()));
        Glide.with(activity).load("https://192.168.1.25:9292/uploads/item_image/image/23/e79877ee5f3f1e9ab2b4a9c8a289e3f16dfb25cb.jpg").into(imageView);
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
        Call<ItemEntity> call = service.getNextItemImage(itemId, offset);

        call.enqueue(new Callback<ItemEntity>() {
            @Override
            public void onResponse(Response<ItemEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    ItemEntity item = response.body();
                    adapter.finishLoadNextItem();
                    footerView.setVisibility(View.GONE);
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



    public View getTabView(int position, int count){
        View tab = activity.getLayoutInflater().inflate(R.layout.item_tab_header, null);
        switch(position){
            case 0:
                TextView itemCount = (TextView)tab.findViewById(R.id.tab_count);
                itemCount.setText(String.valueOf(count));
                break;
            case 1:
                ImageView iconTypeImage = (ImageView)tab.findViewById(R.id.tab_icon);
                iconTypeImage.setImageResource(R.drawable.ic_image_black_18dp);
                TextView imageTab = (TextView)tab.findViewById(R.id.tab_name);
                imageTab.setText(R.string.item_images);
                TextView imageCount = (TextView)tab.findViewById(R.id.tab_count);
                imageCount.setText(String.valueOf(count));
                break;
            case 2:
                ImageView iconTypeGraph = (ImageView)tab.findViewById(R.id.tab_icon);
                iconTypeGraph.setImageResource(R.drawable.ic_timeline_black_18dp);
                TextView graphTab = (TextView)tab.findViewById(R.id.tab_name);
                graphTab.setText(R.string.item_graph);
                TextView graphCount = (TextView)tab.findViewById(R.id.tab_count);
                graphCount.setVisibility(View.GONE);
        }

        return tab;
    }

    public static TextView createTag(Context context, String tag, @Nullable Boolean isMultiline){
        TextView baseTag = new TextView(context);
        FlowLayout.LayoutParams lp = new FlowLayout.LayoutParams(FlowLayout.LayoutParams.WRAP_CONTENT, FlowLayout.LayoutParams.WRAP_CONTENT);
        int marginTop = 0;
        if(isMultiline){
            marginTop = 15;
        }
        lp.setMargins(0, marginTop, 20, 0);
        baseTag.setBackgroundColor(ContextCompat.getColor(context, R.color.tagColor));
        baseTag.setPadding(10, 0, 10, 0);
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

        public ItemPagerAdapter(Activity a, StickyScrollPresenter s, ItemPresenter ip, ItemEntity i){
            activity = a;
            stickyScrollPresenter = s;
            itemPresenter = ip;
            item = i;
            loader = View.inflate(a, R.layout.loading, null);
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

            Log.d("customview", "position;" + position);
            View v;
            if(position == 0) {
                v = activity.getLayoutInflater().inflate(R.layout.item_list_tab, container, false);

                final ListView listView = (ListView) v.findViewById(R.id.page_text);

                final ItemListAdapter adapter = new ItemListAdapter(activity, R.layout.item_list, item);
                //BusHolder.get().register(adapter);
                //listView.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, items));

                listView.setAdapter(adapter);

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
                        //一番下にいったかどうかと次があるかのフラグのチェック
                        //trueならapi取得
                        //callback+eventbusでなんとかする
                        if ((totalItemCount == firstVisibleItem + visibleItemCount) && adapter.hasNextItem()) {
                            Log.d("request", "to api");
                            if (!adapter.getIsLoadingNextItem()) {
                                adapter.startLoadNextItem();
                                listView.addFooterView(loader);
                                itemPresenter.getNextItemList(item.id, adapter.getLastItemId(), adapter, listView, loader);
                            }
                        }
                    }
                });
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
            Log.d("customview", "destroyed:" + position);
            container.removeView((View) object);
        }

        public View attachImageGrid(ViewGroup container){
            View v = activity.getLayoutInflater().inflate(R.layout.item_image_tab, container, false);

            final ProgressBar imageLoader = (ProgressBar)v.findViewById(R.id.image_loader);
            final GridView gridView = (GridView)v.findViewById(R.id.item_image_tab);
            final ItemImageListAdapter adapter = new ItemImageListAdapter(activity, R.layout.item_image_list, item);
            gridView.setAdapter(adapter);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                    Log.d("image click position", String.valueOf(position));
                                                    ImageDetailActivity.startActivity(activity, item, (String)view.getTag(R.string.tag_image_url), (Date)view.getTag(R.string.tag_image_date));
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
                            itemPresenter.getNextItemImageList(item.id, adapter.getLastItemImageId(), adapter, gridView, imageLoader);
                        }
                    }

                }
            });


            return v;
        }

        public View attachGraph(ViewGroup container){
            View v = activity.getLayoutInflater().inflate(R.layout.item_graph_tab, container, false);

            /*
            final ProgressBar imageLoader = (ProgressBar)v.findViewById(R.id.image_loader);
            final GridView gridView = (GridView)v.findViewById(R.id.item_image_tab);
            final ItemImageListAdapter adapter = new ItemImageListAdapter(activity, R.layout.item_image_list, item);
            gridView.setAdapter(adapter);
            */
            final RelativeLayout graphView = (RelativeLayout)v.findViewById(R.id.item_graph_tab_wrapper);

            graphView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("graph", "clicked");

                }
            });

            LineChartView lineChartView = (LineChartView)graphView.findViewById(R.id.item_graph);

            lineChartView.setOnTouchListener(new StickyScrollPresenter.CustomTouchListener(stickyScrollPresenter));
            GraphRenderer.renderSimpleGraph(lineChartView, item.countProperties);
            graphView.setOnTouchListener(new StickyScrollPresenter.CustomTouchListener(stickyScrollPresenter));

            v.findViewById(R.id.navigate_to_detailgraph).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DetailGraphActivity.startActivity(activity, item);
                }
            });

            int countSize = item.countProperties.size();
            TextView graphFrom = (TextView)v.findViewById(R.id.item_graph_date_from);
            graphFrom.setText(ViewUtil.dateToString(item.countProperties.get(0).date, true));
            TextView graphTo = (TextView)v.findViewById(R.id.item_graph_date_to);
            graphTo.setText(ViewUtil.dateToString(item.countProperties.get(countSize - 1).date, true));

            return v;
        }
    }

}
