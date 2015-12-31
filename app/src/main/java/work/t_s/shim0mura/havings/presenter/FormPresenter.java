package work.t_s.shim0mura.havings.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.okhttp.RequestBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import lecho.lib.hellocharts.view.LineChartView;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import timber.log.Timber;
import work.t_s.shim0mura.havings.DetailGraphActivity;
import work.t_s.shim0mura.havings.ImageDetailActivity;
import work.t_s.shim0mura.havings.ItemActivity;
import work.t_s.shim0mura.havings.ItemFormActivity;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.RegisterActivity;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.StatusCode;
import work.t_s.shim0mura.havings.model.entity.CountDataEntity;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.UserListEntity;
import work.t_s.shim0mura.havings.util.ViewUtil;
import work.t_s.shim0mura.havings.view.GraphRenderer;
import work.t_s.shim0mura.havings.view.ItemImageListAdapter;
import work.t_s.shim0mura.havings.view.ItemListAdapter;
import work.t_s.shim0mura.havings.view.ListSelectAdapter;

/**
 * Created by shim0mura on 2015/12/18.
 */
public class FormPresenter {
    Activity activity;
    static ApiService service;

    protected String TAG = "FormPresenter: ";

    public FormPresenter(Context c){
        activity = (Activity)c;
        service = ApiServiceManager.getService(activity);
    }

    public void postItem(HashMap<String, ItemEntity> item, HashMap<String, RequestBody> fileParams){
        Call<ItemEntity> call = service.postItem(item);

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
                Timber.d("failed to post item");
                t.printStackTrace();
            }
        });
    }

    public void getUserListTree(){
        Call<UserListEntity[]> call = service.getUserList();

        call.enqueue(new Callback<UserListEntity[]>() {
            @Override
            public void onResponse(Response<UserListEntity[]> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    UserListEntity[] list = response.body();

                    BusHolder.get().post(list);
                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else {

                }
            }

            @Override
            public void onFailure(Throwable t) {
                Timber.d("failed to get userlist");
                t.printStackTrace();
            }
        });
    }

    public static class ListSelectPagerAdapter extends PagerAdapter {

        private Activity activity;
        private View loader;

        public ListSelectPagerAdapter(Activity a){
            activity = a;
            loader = View.inflate(a, R.layout.loading, null);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //return super.getPageTitle(position);
            return position + "";
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

            View view;
            if(position == 0) {
                view = activity.getLayoutInflater().inflate(R.layout.category_list_tab, container, false);

                final ExpandableStickyListHeadersListView listView = (se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView)view.findViewById(R.id.list);

                ListSelectAdapter adapter = new ListSelectAdapter(activity);
                listView.setAdapter(adapter);
                listView.setOnHeaderClickListener(new StickyListHeadersListView.OnHeaderClickListener() {
                    @Override
                    public void onHeaderClick(StickyListHeadersListView l, View header, int itemPosition, long headerId, boolean currentlySticky) {
                        if (listView.isHeaderCollapsed(headerId)) {
                            listView.expand(headerId);
                        } else {
                            listView.collapse(headerId);
                        }
                    }
                });

                for(int i: adapter.getKindIds()){
                    listView.collapse(i);
                }

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Timber.d("selected item %s", view.getTag(R.id.TAG_ITEM_ID));
                        Intent data = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putInt(ItemFormActivity.LIST_NAME_TAG_ID_KEY, (int)view.getTag(R.id.TAG_ITEM_ID));
                        data.putExtras(bundle);

                        activity.setResult(Activity.RESULT_OK, data);
                        activity.finish();
                    }
                });
            }else {
                view = activity.getLayoutInflater().inflate(R.layout.item_list_tab, container, false);
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
