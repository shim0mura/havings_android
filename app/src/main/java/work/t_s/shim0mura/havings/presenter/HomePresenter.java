package work.t_s.shim0mura.havings.presenter;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wefika.flowlayout.FlowLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;
import work.t_s.shim0mura.havings.ItemActivity;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.SearchTagResultActivity;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.StatusCode;
import work.t_s.shim0mura.havings.model.User;
import work.t_s.shim0mura.havings.model.entity.CountDataEntity;
import work.t_s.shim0mura.havings.model.entity.DeviceTokenEntity;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ItemPercentageEntity;
import work.t_s.shim0mura.havings.model.entity.ModelErrorEntity;
import work.t_s.shim0mura.havings.model.entity.PopularTagEntity;
import work.t_s.shim0mura.havings.model.entity.TimelineEntity;
import work.t_s.shim0mura.havings.model.entity.TimerEntity;
import work.t_s.shim0mura.havings.model.event.CountGraphEvent;
import work.t_s.shim0mura.havings.model.event.ItemPercentageGraphEvent;
import work.t_s.shim0mura.havings.model.event.TimerListRenderEvent;
import work.t_s.shim0mura.havings.util.ApiErrorUtil;
import work.t_s.shim0mura.havings.util.ViewUtil;
import work.t_s.shim0mura.havings.view.DashboardTabFragment;
import work.t_s.shim0mura.havings.view.SearchTabFragment;
import work.t_s.shim0mura.havings.view.SocialTabFragment;
import work.t_s.shim0mura.havings.view.UserFragment;

/**
 * Created by shim0mura on 2016/03/29.
 */
public class HomePresenter {

    private static String DEVICE_TOKEN_KEY = "device_token";
    private static int POPULAR_TAG_IMAGE_SIZE = 120;
    private static int MAX_SHOWING_POPULAR_LIST = 5;

    Activity activity;
    static ApiService service;

    public HomePresenter(Context c){
        activity = (Activity)c;
        service = ApiServiceManager.getService(activity);
    }

    public void getAllTimers(){
        Call<List<TimerEntity>> call = service.getAllTimers();

        call.enqueue(new Callback<List<TimerEntity>>() {
            @Override
            public void onResponse(Response<List<TimerEntity>> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    List<TimerEntity> timers = response.body();
                    TimerListRenderEvent event = new TimerListRenderEvent(new ArrayList<TimerEntity>(timers));
                    BusHolder.get().post(event);

                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else if (response.code() == StatusCode.UnprocessableEntity) {
                    Timber.d("failed to post");

                    ModelErrorEntity error = ApiErrorUtil.parseError(response, retrofit);

                    Timber.d(error.toString());
                    for (Map.Entry<String, List<String>> e : error.errors.entrySet()) {
                        switch (e.getKey()) {
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
                Timber.d("timer get failed");
            }
        });
    }

    public void getTimeline(int from){
        Call<TimelineEntity> call = service.getTimeline(from);

        call.enqueue(new Callback<TimelineEntity>() {
            @Override
            public void onResponse(Response<TimelineEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    TimelineEntity timeline = response.body();
                    BusHolder.get().post(timeline);

                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else if (response.code() == StatusCode.UnprocessableEntity) {
                    Timber.d("failed to post");

                    ModelErrorEntity error = ApiErrorUtil.parseError(response, retrofit);

                    Timber.d(error.toString());
                    for (Map.Entry<String, List<String>> e : error.errors.entrySet()) {
                        switch (e.getKey()) {
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
                Timber.d("timer get failed");
            }
        });
    }

    public void getCountData(){
        Call<List<CountDataEntity>> call = service.getCountData();

        call.enqueue(new Callback<List<CountDataEntity>>() {
            @Override
            public void onResponse(Response<List<CountDataEntity>> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    List<CountDataEntity> data = response.body();
                    BusHolder.get().post(new CountGraphEvent(data));

                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else if (response.code() == StatusCode.UnprocessableEntity) {

                } else {

                }
            }

            @Override
            public void onFailure(Throwable t) {
                Timber.d("timer get failed");
            }
        });
    }

    public void postDeviceToken(String token){
        DeviceTokenEntity deviceTokenEntity = new DeviceTokenEntity();
        deviceTokenEntity.token = token;
        HashMap<String, DeviceTokenEntity> hashItem = new HashMap<String, DeviceTokenEntity>();
        hashItem.put(DEVICE_TOKEN_KEY, deviceTokenEntity);
        Call<DeviceTokenEntity> call = service.postDeviceToken(0, hashItem);

        call.enqueue(new Callback<DeviceTokenEntity>() {
            @Override
            public void onResponse(Response<DeviceTokenEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    DeviceTokenEntity deviceToken = response.body();
                    BusHolder.get().post(deviceToken);

                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else if (response.code() == StatusCode.UnprocessableEntity) {
                    Timber.d("failed to post");

                    ModelErrorEntity error = ApiErrorUtil.parseError(response, retrofit);

                    Timber.d(error.toString());
                    for (Map.Entry<String, List<String>> e : error.errors.entrySet()) {
                        switch (e.getKey()) {
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
                Timber.d("timer get failed");
            }
        });
    }

    public void putDeviceToken(String token){
        DeviceTokenEntity deviceTokenEntity = new DeviceTokenEntity();
        deviceTokenEntity.token = token;
        HashMap<String, DeviceTokenEntity> hashItem = new HashMap<String, DeviceTokenEntity>();
        hashItem.put(DEVICE_TOKEN_KEY, deviceTokenEntity);
        Call<DeviceTokenEntity> call = service.putDeviceToken(0, hashItem);

        call.enqueue(new Callback<DeviceTokenEntity>() {
            @Override
            public void onResponse(Response<DeviceTokenEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    DeviceTokenEntity deviceToken = response.body();
                    BusHolder.get().post(deviceToken);

                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else if (response.code() == StatusCode.UnprocessableEntity) {
                    Timber.d("failed to post");

                    ModelErrorEntity error = ApiErrorUtil.parseError(response, retrofit);

                    Timber.d(error.toString());
                    for (Map.Entry<String, List<String>> e : error.errors.entrySet()) {
                        switch (e.getKey()) {
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
                Timber.d("timer get failed");
            }
        });
    }

    public void changeDeviceTokenState(boolean isEnable){
        int value = isEnable ? 1 : 0;
        Call<DeviceTokenEntity> call = service.changeDeviceTokenState(0, value);

        call.enqueue(new Callback<DeviceTokenEntity>() {
            @Override
            public void onResponse(Response<DeviceTokenEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    DeviceTokenEntity deviceToken = response.body();
                    BusHolder.get().post(deviceToken);

                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else if (response.code() == StatusCode.UnprocessableEntity) {
                    Timber.d("failed to post");

                    ModelErrorEntity error = ApiErrorUtil.parseError(response, retrofit);

                    Timber.d(error.toString());
                    for (Map.Entry<String, List<String>> e : error.errors.entrySet()) {
                        switch (e.getKey()) {
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
                Timber.d("timer get failed");
            }
        });
    }

    public static void setPopularTag(final Context context, LinearLayout popularTagWrapper, List<PopularTagEntity> popularTagEntities, boolean addAll){

        int showingCount;

        if(addAll) {
            showingCount = popularTagEntities.size();
        }else{
            showingCount = (popularTagEntities.size() > MAX_SHOWING_POPULAR_LIST) ? MAX_SHOWING_POPULAR_LIST : popularTagEntities.size();
        }

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        popularTagWrapper.removeAllViews();

        for (int i = 0; i < showingCount; i++) {
            PopularTagEntity popularTag = popularTagEntities.get(i);
            View v = layoutInflater.inflate(R.layout.partial_popular_tag, popularTagWrapper, false);
            TextView tagName = (TextView)v.findViewById(R.id.tag_name);
            TextView tagCount = (TextView)v.findViewById(R.id.item_tag_count);
            LinearLayout imageWrapper = (LinearLayout)v.findViewById(R.id.image_wrapper);

            tagName.setText(popularTag.tagName);
            tagCount.setText(String.format(context.getString(R.string.postfix_item_count), popularTag.tagCount));

            for(ItemEntity item : popularTag.items){
                ImageView image = new ImageView(context);
                if(item.thumbnail != null) {
                    String thumbnailUrl = ApiService.BASE_URL + item.thumbnail;
                    Glide.with(context).load(thumbnailUrl).into(image);
                }else{
                    image.setImageResource(R.drawable.ic_image_black_18dp);
                }
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageWrapper.addView(image);

                int px = ViewUtil.dpToPix(context, POPULAR_TAG_IMAGE_SIZE);
                ViewGroup.LayoutParams layoutParams = image.getLayoutParams();
                layoutParams.width = px;
                layoutParams.height = px;
                image.setLayoutParams(layoutParams);


            }
            v.setTag(R.id.TAG_POPULAR_TAG_ID, popularTag.tagId);
            v.setTag(R.id.TAG_POPULAR_TAG_NAME, popularTag.tagName);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String tagName = (String)v.getTag(R.id.TAG_POPULAR_TAG_NAME);
                    SearchTagResultActivity.startActivity(context, tagName);
                }
            });

            popularTagWrapper.addView(v);
        }
    }

    public static void setPopularList(final Context context, LinearLayout popularListWrapper, List<ItemEntity> itemEntities, boolean addAll){
        if(itemEntities == null){
            return;
        }

        int showingCount;
        if(addAll){
            showingCount = itemEntities.size();
        }else{
            showingCount = (itemEntities.size() > MAX_SHOWING_POPULAR_LIST) ? MAX_SHOWING_POPULAR_LIST : itemEntities.size();
        }
        popularListWrapper.removeAllViews();
        int px = ViewUtil.dpToPix(context, 16);
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        for (int i = 0; i < showingCount; i++){
            ItemEntity item = itemEntities.get(i);
            View v = layoutInflater.inflate(R.layout.partial_popular_item_list, popularListWrapper, false);

            LinearLayout wrapper = (LinearLayout)v.findViewById(R.id.wrapper);
            ImageView thumbnail = (ImageView)v.findViewById(R.id.item_thumbnail);
            ImageView itemType = (ImageView)v.findViewById(R.id.item_type);
            TextView name = (TextView)v.findViewById(R.id.item_name);
            TextView count = (TextView)v.findViewById(R.id.item_count);
            TextView favoriteCount = (TextView)v.findViewById(R.id.item_favorite_count);
            FlowLayout tags = (FlowLayout)v.findViewById(R.id.item_tag);

            v.setTag(R.string.tag_item_id, item.id);

            wrapper.setPadding(0, 0, 0, px);
            if(item.thumbnail != null){
                String thumbnailUrl = ApiService.BASE_URL + item.thumbnail;
                Glide.with(context).load(thumbnailUrl).into(thumbnail);
            }
            if(item.isList){
                itemType.setImageResource(R.drawable.list_icon_for_tab);
            }
            name.setText(item.name);
            count.setText(String.valueOf(item.count));
            favoriteCount.setText(String.valueOf(item.favoriteCount));
            if(item.tags != null && item.tags.size() > 0){
                tags.removeAllViews();
                for(String tagString : item.tags){
                    TextView tag = ItemPresenter.createTag(context, tagString, false);
                    tags.addView(tag);
                }
            }else if(item.tags != null && item.tags.size() == 0){
                tags.removeAllViews();
            }

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ItemActivity.startActivity(context, (int)v.getTag(R.string.tag_item_id));
                }
            });

            popularListWrapper.addView(v);
        }
    }

    public static class HomeTabPagerAdapter extends FragmentPagerAdapter {

        private final String[] tabTitle = new String[4];
        private Context context;
        private LayoutInflater layoutInflater;

        private android.support.v4.app.Fragment homeFragment;
        private android.support.v4.app.Fragment socialFragment;
        private android.support.v4.app.Fragment searchFragment;
        private android.support.v4.app.Fragment accountFragment;


        public HomeTabPagerAdapter(android.support.v4.app.FragmentManager fm, Context c){
            super(fm);
            context = c;
            layoutInflater = LayoutInflater.from(c);
            tabTitle[0] = context.getString(R.string.prompt_home);
            tabTitle[1] = context.getString(R.string.prompt_social);
            tabTitle[2] = context.getString(R.string.prompt_search);
            tabTitle[3] = context.getString(R.string.prompt_account);
        }

        public String getTabTitle(int position){
            return tabTitle[position];
        }

        public int getTabIcon(int position, boolean isSelected){
            int icon = R.drawable.item_icon_for_tab;

            switch(position){
                case 0:
                    if(isSelected){
                        icon = R.drawable.ic_home_white_36dp;
                    }else {
                        icon = R.drawable.ic_home_black_36dp;
                    }
                    break;
                case 1:
                    if(isSelected){
                        icon = R.drawable.ic_face_white_36dp;
                    }else {
                        icon = R.drawable.ic_face_black_36dp;
                    }
                    break;
                case 2:
                    if(isSelected){
                        icon = R.drawable.ic_search_white_36dp;
                    }else {
                        icon = R.drawable.ic_search_black_36dp;
                    }
                    break;
                case 3:
                    if(isSelected){
                        icon = R.drawable.ic_account_circle_white_36dp;
                    }else {
                        icon = R.drawable.ic_account_circle_black_36dp;
                    }
                    break;
            }

            return icon;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {

            if(position == 0) {
                return DashboardTabFragment.newInstance();
            }else if(position == 1) {
                if(socialFragment == null){
                    socialFragment = SocialTabFragment.newInstance();
                    Timber.d("create_social");

                }
                return socialFragment;
            }else if(position == 2){
                if(searchFragment == null){
                    searchFragment = SearchTabFragment.newInstance();
                    Timber.d("create_search");

                }
                return searchFragment;
            }else if(position == 3){
                if(accountFragment == null){
                    accountFragment = UserFragment.newInstance();
                    Timber.d("create_account");

                }
                return accountFragment;
            }else{
                return UserFragment.newInstance();
            }
        }

    }
}
