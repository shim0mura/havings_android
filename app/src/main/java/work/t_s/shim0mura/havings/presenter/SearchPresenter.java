package work.t_s.shim0mura.havings.presenter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.StatusCode;
import work.t_s.shim0mura.havings.model.entity.ModelErrorEntity;
import work.t_s.shim0mura.havings.model.entity.PickupEntity;
import work.t_s.shim0mura.havings.model.entity.SearchResultEntity;
import work.t_s.shim0mura.havings.model.entity.TaskWrapperEntity;
import work.t_s.shim0mura.havings.util.ApiErrorUtil;

/**
 * Created by shim0mura on 2016/04/06.
 */
public class SearchPresenter {

    Activity activity;
    static ApiService service;

    public SearchPresenter(Context c){
        activity = (Activity)c;
        service = ApiServiceManager.getService(activity);
    }

    public void getTagSearchResult(String searchWord, int page){
        Call<SearchResultEntity> call = service.getTagSearchResult(page, searchWord);

        call.enqueue(new Callback<SearchResultEntity>() {
            @Override
            public void onResponse(Response<SearchResultEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    SearchResultEntity searchResultEntity = response.body();
                    BusHolder.get().post(searchResultEntity);

                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else if (response.code() == StatusCode.UnprocessableEntity) {
                    Timber.d("failed to post");

                    ModelErrorEntity error = ApiErrorUtil.parseError(response, retrofit);

                    Timber.d(error.toString());
                    if(error.errors != null) {
                        for (Map.Entry<String, List<String>> e : error.errors.entrySet()) {
                            switch (e.getKey()) {
                                default:
                                    //sendErrorToGetUser();
                                    break;
                            }
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

    public void getUserSearchResult(String searchWord, int page){
        Call<SearchResultEntity> call = service.getUserSearchResult(page, searchWord);

        call.enqueue(new Callback<SearchResultEntity>() {
            @Override
            public void onResponse(Response<SearchResultEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    SearchResultEntity searchResultEntity = response.body();
                    BusHolder.get().post(searchResultEntity);

                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else if (response.code() == StatusCode.UnprocessableEntity) {
                    Timber.d("failed to post");

                    ModelErrorEntity error = ApiErrorUtil.parseError(response, retrofit);

                    Timber.d(error.toString());
                    if(error.errors != null) {
                        for (Map.Entry<String, List<String>> e : error.errors.entrySet()) {
                            switch (e.getKey()) {
                                default:
                                    //sendErrorToGetUser();
                                    break;
                            }
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

    public void getPickup(){
        Call<PickupEntity> call = service.getPickup();

        call.enqueue(new Callback<PickupEntity>() {
            @Override
            public void onResponse(Response<PickupEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    PickupEntity pickupEntity = response.body();
                    BusHolder.get().post(pickupEntity);

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
                Timber.d("timer get failed");
            }
        });
    }
}
