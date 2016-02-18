package work.t_s.shim0mura.havings.presenter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.GeneralResult;
import work.t_s.shim0mura.havings.model.StatusCode;
import work.t_s.shim0mura.havings.model.entity.ModelErrorEntity;
import work.t_s.shim0mura.havings.model.entity.ResultEntity;
import work.t_s.shim0mura.havings.model.entity.UserEntity;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.util.ApiErrorUtil;

/**
 * Created by shim0mura on 2016/01/24.
 */
public class UserListPresenter {

    public static final String USER_LIST_TYPE = "UserListType";
    public static final String RELATED_ID = "RelatedId";

    public static final int ITEM_FAVORITE_USER_LIST = 10;
    public static final int ITEM_IMAGE_FAVORITE_USER_LIST = 20;
    public static final int FOLLOWING_USER_LIST = 30;
    public static final int FOLLOWED_USER_LIST = 40;

    private Activity activity;
    static ApiService service;

    public UserListPresenter(Context c){
        activity = (Activity)c;
        service = ApiServiceManager.getService(activity);
    }

    public void getUsers(int listTypeId, int relatedId){
        Call<List<UserEntity>> call;
        switch (listTypeId){
            case ITEM_FAVORITE_USER_LIST:
                call = service.getItemFavoritedUsers(relatedId);
                break;
            case ITEM_IMAGE_FAVORITE_USER_LIST:
                call = service.getItemImageFavoritedUsers(relatedId);
                break;
            case FOLLOWING_USER_LIST:
                call = service.getFollowingUsers(relatedId);
                break;
            case FOLLOWED_USER_LIST:
                call = service.getFollowedUsers(relatedId);
                break;
            default:
                Timber.w("Unexpected listTypeId specified to get users... listTypeId: %s", listTypeId);
                return;
        }
        call.enqueue(getCallbackOfSuccessToPostItem(GeneralResult.RESULT_GET_USERS));
    }

    private Callback<List<UserEntity>> getCallbackOfSuccessToPostItem(final int resultType){
        return new Callback<List<UserEntity>>() {
            @Override
            public void onResponse(Response<List<UserEntity>> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    List<UserEntity> users = response.body();
                    //BusHolder.get().post(Arrays.asList(users));
                    BusHolder.get().post(users);


                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else if (response.code() == StatusCode.UnprocessableEntity) {
                    Timber.d("failed to get users");

                    ModelErrorEntity error = ApiErrorUtil.parseError(response, retrofit);

                    BusHolder.get().post(new SetErrorEvent(resultType, error));

                } else {

                }
            }

            @Override
            public void onFailure(Throwable t) {
                Timber.d("failed to get user");
                t.printStackTrace();
            }
        };
    }

}
