package work.t_s.shim0mura.havings.model;

import com.squareup.okhttp.MediaType;

/**
 * Created by shim0mura on 2015/11/07.
 */
public class ApiRoute {
    private ApiRoute(){}

    public static final String BASE_URL = "https://192.168.1.25:9292/";

    public static final String REGISTER = BASE_URL + "users";
    public static final String SIGNIN = BASE_URL + "users/sign_in";
    public static final String SIGNIN_BY_OAUTH = BASE_URL + "users/auth/";
    public static final String SIGNIN_BY_OAUTH_PARAMS = "?origin=android";

}
