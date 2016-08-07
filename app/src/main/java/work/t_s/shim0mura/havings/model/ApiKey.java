package work.t_s.shim0mura.havings.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.util.SystemNativeCryptoLibrary;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * Created by shim0mura on 2015/11/10.
 */
public class ApiKey {

    private static final String API_KEY = "ApiKey";
    private static final String API_TOKEN = "access-token";
    private static final String UID = "uid";
    private static final String USER_ID = "userId";
    private static final String DEVICE_TOKEN = "deviceToken";
    private static final String ACCESS_KEY = "secret";

    private static ApiKey apiKey;
    private Context context;
    private String token;
    private String uid;
    private int userId;
    private String deviceToken;

    private ApiKey(Context c){
        context = c;

        SharedPreferences preferences = context.getSharedPreferences(API_KEY, Context.MODE_PRIVATE);
        String accessKey = preferences.getString(ACCESS_KEY, null);
        token = preferences.getString(API_TOKEN, null);
        uid = preferences.getString(UID, null);
        userId = preferences.getInt(USER_ID, 0);
        deviceToken = preferences.getString(DEVICE_TOKEN, null);
    }

    public String getToken(){
        return token;
    }
    public String getUid(){
        return uid;
    }
    public int getUserId() { return userId; }
    public String getDeviceToken() {
        return deviceToken;
    }

    public static synchronized ApiKey getSingleton(Context context){
        if(apiKey == null){
            apiKey = new ApiKey(context);
        }

        return apiKey;
    }

    public void clearApiKey(){
        token = null;
        uid = null;
        userId = 0;

        SharedPreferences preferences = context.getSharedPreferences(API_KEY, Context.MODE_PRIVATE);
        //preferences.edit().remove(API_TOKEN);
        //preferences.edit().remove(UID);
        //preferences.edit().commit();
        preferences.edit().clear().commit();
    }

    public void updateDeviceToken(String token){
        SharedPreferences preferences = context.getSharedPreferences(API_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor;

        editor = preferences.edit();
        editor.putString(DEVICE_TOKEN, token);

        deviceToken = token;

        editor.apply();
    }

    //https://github.com/facebook/conceal/issues/90
    //concealは最新バージョンだとcrypto.isAvailable()がうまく行かなくて落ちるので今は使わないで
    //sharedpreferenceのみでいく
    //使う場合の参考
    // http://qiita.com/Rompei/items/c21c543707510720db2d
    public void storeApiKey(String rawToken, String rawUid, int rawUserId){
        SharedPreferences preferences = context.getSharedPreferences(API_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor;

        //String accessKey = UUID.randomUUID().toString();
        editor = preferences.edit();
        editor.putString(API_TOKEN, rawToken);
        editor.putString(UID, rawUid);
        editor.putInt(USER_ID, rawUserId);

        token = rawToken;
        uid = rawUid;
        userId = rawUserId;
        //userId = rawUserId;

        /*
        Crypto crypto = new Crypto(
                new SharedPrefsBackedKeyChain(context),
                new SystemNativeCryptoLibrary());
        if(!crypto.isAvailable()){
            return;
        }
        try {
            byte[] encryptedToken = crypto.encrypt(token.getBytes(), new Entity(accessKey));
            byte[] encryptedUid = crypto.encrypt(uid.getBytes(), new Entity(accessKey));

            editor.putString(ACCESS_KEY, accessKey);
            editor.putString(API_TOKEN, Base64.encodeToString(encryptedToken, Base64.DEFAULT));
            editor.putString(UID, Base64.encodeToString(encryptedUid, Base64.DEFAULT));
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }catch (CryptoInitializationException e){
            e.printStackTrace();
        }catch(KeyChainException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        */
        editor.apply();
    }
}
