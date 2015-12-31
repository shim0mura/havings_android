package work.t_s.shim0mura.havings.model;

import android.support.annotation.Nullable;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.util.HashMap;
import java.util.List;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.PartMap;
import retrofit.http.Path;
import retrofit.http.Query;
import work.t_s.shim0mura.havings.model.entity.CountDataEntity;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.TagMigrationEntity;
import work.t_s.shim0mura.havings.model.entity.UserEntity;
import work.t_s.shim0mura.havings.model.entity.UserListEntity;

/**
 * Created by shim0mura on 2015/11/07.
 */
public interface ApiService {

    MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    String BASE_URL = "https://192.168.1.25:9292";

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
    Call<ItemEntity> postItem(
            //@Body ItemEntity item
            @Body HashMap<String, ItemEntity> item
            //@PartMap HashMap<String, RequestBody> fileParams
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
