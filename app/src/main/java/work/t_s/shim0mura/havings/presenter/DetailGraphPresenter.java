package work.t_s.shim0mura.havings.presenter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.StatusCode;
import work.t_s.shim0mura.havings.model.User;
import work.t_s.shim0mura.havings.model.entity.CountDataEntity;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;

/**
 * Created by shim0mura on 2015/12/16.
 */
public class DetailGraphPresenter {

    Activity activity;
    static ApiService service;

    protected String TAG = "DetailGraphPresenter: ";

    public DetailGraphPresenter(Context c){
        activity = (Activity)c;
        service = ApiServiceManager.getService(activity);
    }

    public void getShowingEvent(final int itemId){
        Call<List<CountDataEntity>> call = service.getShowingEvent(itemId);

        call.enqueue(new Callback<List<CountDataEntity>>() {
            @Override
            public void onResponse(Response<List<CountDataEntity>> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    List<CountDataEntity> eventList = response.body();

                    ItemEntity item = new ItemEntity();
                    item.id = itemId;
                    item.countProperties = eventList;
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
}
