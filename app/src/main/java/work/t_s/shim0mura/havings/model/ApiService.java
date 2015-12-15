package work.t_s.shim0mura.havings.model;

import com.squareup.okhttp.MediaType;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.UserEntity;

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
}
