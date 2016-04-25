package work.t_s.shim0mura.havings.model;

import android.support.annotation.Nullable;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.util.HashMap;
import java.util.List;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.PartMap;
import retrofit.http.Path;
import retrofit.http.Query;
import work.t_s.shim0mura.havings.model.entity.CommentEntity;
import work.t_s.shim0mura.havings.model.entity.CountDataEntity;
import work.t_s.shim0mura.havings.model.entity.FavoriteItemImageListEntity;
import work.t_s.shim0mura.havings.model.entity.FavoriteItemListEntity;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageListEntity;
import work.t_s.shim0mura.havings.model.entity.ItemPercentageEntity;
import work.t_s.shim0mura.havings.model.entity.NotificationEntity;
import work.t_s.shim0mura.havings.model.entity.PickupEntity;
import work.t_s.shim0mura.havings.model.entity.ResultEntity;
import work.t_s.shim0mura.havings.model.entity.SearchResultEntity;
import work.t_s.shim0mura.havings.model.entity.TagMigrationEntity;
import work.t_s.shim0mura.havings.model.entity.TaskWrapperEntity;
import work.t_s.shim0mura.havings.model.entity.TimelineEntity;
import work.t_s.shim0mura.havings.model.entity.TimerEntity;
import work.t_s.shim0mura.havings.model.entity.UserEntity;
import work.t_s.shim0mura.havings.model.entity.UserListEntity;

/**
 * Created by shim0mura on 2015/11/07.
 */
public interface ApiService {

    MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    String BASE_URL = "https://192.168.1.25:9292";
    String BASE_URL_BY_WEB = "http://192.168.1.25:9292";

    String REGISTER = BASE_URL + "/users";
    String SIGNIN = BASE_URL + "/users/sign_in";
    String SIGNIN_BY_OAUTH = BASE_URL + "/users/auth/";
    String SIGNIN_BY_OAUTH_PARAMS = "?origin=android";

    String ACCESS_TOKEN_HEADER = "X_ACCESS_TOKEN";
    String UID_HEADER = "X_UID";

    @GET("/user/{user_id}")
    Call<UserEntity> getUser(
            @Path("user_id") int user_id
    );

    @GET("/user/self")
    Call<UserEntity> getSelf();

    @GET("/user/{user_id}/item_list")
    Call<ItemEntity> getUserItemList(
            @Path("user_id") int user_id,
            @Query("from") int offset
    );

    @GET("/user/{user_id}/item_images")
    Call<ItemImageListEntity> getUserItemImages(
            @Path("user_id") int user_id,
            @Query("from") int offset
    );

    @GET("/user/{user_id}/favorite_items")
    Call<FavoriteItemListEntity> getFavoriteItemList(
            @Path("user_id") int user_id,
            @Query("from") int offset
    );

    @GET("/user/{user_id}/favorite_images")
    Call<FavoriteItemImageListEntity> getFavoriteItemImages(
            @Path("user_id") int user_id,
            @Query("from") int offset
    );

    @GET("/user/{user_id}/following")
    Call<List<UserEntity>> getFollowingUsers(
            @Path("user_id") int user_id
    );

    @GET("/user/{user_id}/followers")
    Call<List<UserEntity>> getFollowedUsers(
            @Path("user_id") int user_id
    );

    @GET("/user/{user_id}/dump_items")
    Call<ItemEntity> getDumpItemList(
            @Path("user_id") int user_id,
            @Query("from") int offset
    );

    @POST("/user/{user_id}/follow")
    Call<ResultEntity> followUser(
            @Path("user_id") int user_id
    );

    @DELETE("/user/{user_id}/follow")
    Call<ResultEntity> unfollowUser(
            @Path("user_id") int user_id
    );

    @GET("/items/{item_id}")
    Call<ItemEntity> getItem(
            @Path("item_id") int item_id
    );

    @GET("/items/{item_id}/next_items")
    Call<ItemEntity> getNextItem(
            @Path("item_id") int item_id,
            @Query("from") int offset
    );

    @GET("/items/{item_id}/next_images")
    Call<ItemEntity> getNextItemImage(
            @Path("item_id") int item_id,
            @Query("from") int offset
    );

    @GET("/items/{item_id}/showing_events")
    Call<List<CountDataEntity>> getShowingEvent(
            @Path("item_id") int item_id
    );

    @GET("/user/list_tree")
    Call<UserListEntity[]> getUserList();

    //@FormUrlEncoded
    //@Multipart
    @POST("/items.json")
    Call<ItemEntity> createItem(
            //@Body ItemEntity item
            @Body HashMap<String, ItemEntity> item
            //@PartMap HashMap<String, RequestBody> fileParams
    );

    @PUT("/items/{item_id}")
    Call<ItemEntity> updateItem(
            @Path("item_id") int item_id,
            @Body HashMap<String, ItemEntity> item
    );

    @PUT("/items/{item_id}/dump")
    Call<ItemEntity> dumpItem(
            @Path("item_id") int item_id,
            @Body HashMap<String, ItemEntity> item
    );

    @DELETE("/items/{item_id}")
    Call<ItemEntity> deleteItem(
            @Path("item_id") int item_id,
            @Query("fellow_ids[]") List<Integer> fellowIds
    );

    @POST("/items/{item_id}/favorite")
    Call<ResultEntity> favoriteItem(
            @Path("item_id") int item_id
    );

    @DELETE("/items/{item_id}/favorite")
    Call<ResultEntity> unfavoriteItem(
            @Path("item_id") int item_id
    );

    @GET("/items/{item_id}/favorited_users")
    Call<List<UserEntity>> getItemFavoritedUsers(
            @Path("item_id") int item_id
    );

    @GET("/items/{item_id}/image/{image_id}")
    Call<ItemImageEntity> getItemImage(
            @Path("item_id") int item_id,
            @Path("image_id") int image_id
    );

    @POST("/items/image/{image_id}/favorite")
    Call<ResultEntity> favoriteItemImage(
            @Path("image_id") int image_id
    );

    @DELETE("/items/image/{image_id}/favorite")
    Call<ResultEntity> unfavoriteItemImage(
            @Path("image_id") int image_id
    );

    @GET("/items/image/{image_id}/favorited_users")
    Call<List<UserEntity>> getItemImageFavoritedUsers(
            @Path("image_id") int image_id
    );

    @GET("/timers")
    Call<List<TimerEntity>> getAllTimers();

    @POST("/timers")
    Call<TimerEntity> createTimer(
            @Body HashMap<String, TimerEntity> timer
    );

    @PUT("/timers/{timer_id}")
    Call<TimerEntity> updateTimer(
            @Path("timer_id") int timer_id,
            @Body HashMap<String, TimerEntity> timer
    );

    @POST("/timers/{timer_id}/done")
    Call<TimerEntity> doneTimer(
            @Path("timer_id") int timer_id,
            @Body HashMap<String, TimerEntity> timer
    );

    @POST("/timers/{timer_id}/do_later")
    Call<TimerEntity> doLaterTimer(
            @Path("timer_id") int timer_id,
            @Body HashMap<String, TimerEntity> timer
    );

    @POST("/timers/{timer_id}/end")
    Call<TimerEntity> endTimer(
            @Path("timer_id") int timer_id,
            @Body HashMap<String, TimerEntity> timer
    );

    @DELETE("/timers/{timer_id}")
    Call<TimerEntity> deleteTimer(
            @Path("timer_id") int timer_id
    );

    @GET("/items/{item_id}/done_task")
    Call<TaskWrapperEntity> getDoneTasksByList(
            @Path("item_id") int item_id
    );

    @GET("/items/{item_id}/comment")
    Call<List<CommentEntity>> getComments(
            @Path("item_id") int item_id
    );

    @POST("/items/{item_id}/comment")
    Call<CommentEntity> postComment(
            @Path("item_id") int item_id,
            @Body HashMap<String, CommentEntity> comment
    );

    @DELETE("/items/{item_id}/comment/{comment_id}")
    Call<CommentEntity> deleteComment(
            @Path("item_id") int item_id,
            @Path("comment_id") int comment_id
    );

    @GET("/home/graph")
    Call<List<ItemPercentageEntity>> getItemPercentage();

    @GET("/home/timeline")
    Call<TimelineEntity> getTimeline(
            @Query("from") int from
    );

    @GET("/pickup")
    Call<PickupEntity> getPickup();

    @GET("/notification/")
    Call<List<NotificationEntity>> getNotifications();

    @GET("/notification/unread_count/")
    Call<List<NotificationEntity>> getNotificationCount();

    @PUT("/notification/read")
    Call<ResultEntity> readNotifications();

    @PUT("/users")
    Call<ResultEntity> editProfile(
            @Body HashMap<String, UserEntity> user
    );


    @GET("/search/tag/")
    Call<SearchResultEntity> getTagSearchResult(
            @Query("page") int page,
            @Query("tag") String tag
    );

    @GET("/search/user/")
    Call<SearchResultEntity> getUserSearchResult(
            @Query("page") int page,
            @Query("name") String name
    );

    @GET("/tags/default_tag_migration/")
    Call<List<TagMigrationEntity>> getDefaultTag();

    @GET("/tags/tag_migration/{migration_id}")
    Call<List<TagMigrationEntity>> getTagMigration(
            @Path("migration_id") int migration_id
    );

    @GET("/tags/current_migration_version")
    Call<TagMigrationEntity> getTagMigrationVersion();
}
