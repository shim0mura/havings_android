package work.t_s.shim0mura.havings.presenter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.util.HashMap;
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
import work.t_s.shim0mura.havings.model.entity.CommentEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageEntity;
import work.t_s.shim0mura.havings.model.entity.ModelErrorEntity;
import work.t_s.shim0mura.havings.model.entity.TimerEntity;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.util.ApiErrorUtil;

/**
 * Created by shim0mura on 2016/01/26.
 */
public class CommentPresenter {

    public static final String ITEM_ID_COMMENT_BELONGS = "ItemIdCommentBelongs";
    public static final String ITEM_NAME_COMMENT_BELONGS = "ItemNameCommentBelongs";
    public static final String COMMENT_POST_HASH_KEY = "comment";
    public static final int ERROR_TO_GET_COMMENTS = 100;
    public static final int ERROR_TO_POST_COMMENT = 200;
    public static final int ERROR_TO_DELETE_COMMENT = 300;
    public static final int ERROR_EMPTY_COMMENT = 400;

    private Activity activity;
    private ApiService service;

    public CommentPresenter(Context c){
        activity = (Activity)c;
        service = ApiServiceManager.getService(activity);
    }

    public void getComments(int itemId){
        Call<List<CommentEntity>> call = service.getComments(itemId);

        call.enqueue(new Callback<List<CommentEntity>>() {
            @Override
            public void onResponse(Response<List<CommentEntity>> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    List<CommentEntity> comments = response.body();
                    BusHolder.get().post(comments);
                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else {
                    Timber.d("failed to post");

                    ModelErrorEntity error = ApiErrorUtil.parseError(response, retrofit);

                    BusHolder.get().post(new SetErrorEvent(ERROR_TO_GET_COMMENTS, error));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("user", "get failed");
            }
        });
    }

    public void postComment(int itemId, String commentContent){
        HashMap<String, CommentEntity> hashItem = new HashMap<String, CommentEntity>();

        CommentEntity comment = new CommentEntity();
        comment.content = commentContent;
        if(isValidCommentToPost(comment)){
            hashItem.put(COMMENT_POST_HASH_KEY, comment);
        }else{
            return;
        }

        Call<CommentEntity> call = service.postComment(itemId, hashItem);

        call.enqueue(new Callback<CommentEntity>() {
            @Override
            public void onResponse(Response<CommentEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    CommentEntity comment = response.body();
                    BusHolder.get().post(comment);
                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else {
                    Timber.d("failed to post comment");

                    ModelErrorEntity error = ApiErrorUtil.parseError(response, retrofit);

                    BusHolder.get().post(new SetErrorEvent(ERROR_TO_POST_COMMENT, error));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("user", "get failed");
            }
        });
    }

    public void deleteComment(int itemId, int commentId){
        Call<CommentEntity> call = service.deleteComment(itemId, commentId);

        call.enqueue(new Callback<CommentEntity>() {
            @Override
            public void onResponse(Response<CommentEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    CommentEntity comment = response.body();
                    BusHolder.get().post(comment);
                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else {
                    Timber.d("failed to post comment");

                    ModelErrorEntity error = ApiErrorUtil.parseError(response, retrofit);

                    BusHolder.get().post(new SetErrorEvent(ERROR_TO_DELETE_COMMENT, error));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("user", "get failed");
            }
        });
    }

    private boolean isValidCommentToPost(CommentEntity commentEntity){
        if(commentEntity.content.isEmpty()){
            sendErrorToCommentContent();
            return false;
        }else{
            return true;
        }
    }

    private void sendErrorToCommentContent(){
        BusHolder.get().post(new SetErrorEvent(ERROR_EMPTY_COMMENT, new ModelErrorEntity()));
    }
}
