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
    private static final String ACCESS_KEY = "secret";

    public static void resetApiKey(Context context){
        SharedPreferences preferences = context.getSharedPreferences(API_KEY, Context.MODE_PRIVATE);
        preferences.edit().remove(API_TOKEN);
        preferences.edit().remove(UID);
        preferences.edit().commit();
    }

    //https://github.com/facebook/conceal/issues/90
    //concealは最新バージョンだとcrypto.isAvailable()がうまく行かなくて落ちるので今は使わないで
    //sharedpreferenceのみでいく
    //使う場合の参考
    // http://qiita.com/Rompei/items/c21c543707510720db2d
    public static void storeApiKey(Context context, String token, String uid){
        SharedPreferences preferences = context.getSharedPreferences(API_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor;

        //String accessKey = UUID.randomUUID().toString();
        editor = preferences.edit();
        editor.putString(API_TOKEN, token);
        editor.putString(UID, uid);

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

    public static String[] getApiKey(Context context){
        SharedPreferences preferences = context.getSharedPreferences(API_KEY, Context.MODE_PRIVATE);
        String accessKey = preferences.getString(ACCESS_KEY, null);
        String encryptedToken = preferences.getString(API_TOKEN, null);
        String encryptedUid = preferences.getString(UID, null);

        /*
        byte[] rawEncryptedToken = null;
        String rawDecryptedToken = null;
        byte[] rawEncryptedUid = null;
        String rawDecryptedUid = null;

        if(encryptedToken!=null && encryptedUid!=null && accessKey!=null) {
            rawEncryptedToken = Base64.decode(encryptedToken, Base64.DEFAULT);
            rawEncryptedUid = Base64.decode(encryptedToken, Base64.DEFAULT);


            Crypto crypto = new Crypto(
                    new SharedPrefsBackedKeyChain(context),
                    new SystemNativeCryptoLibrary());
            if (!crypto.isAvailable()) {
                return null;
            }
            try {
                byte[] decryptedToken = crypto.decrypt(rawEncryptedToken, new Entity(accessKey));
                rawDecryptedToken = new String(decryptedToken);
                byte[] decryptedUid = crypto.decrypt(rawEncryptedUid, new Entity(accessKey));
                rawDecryptedUid = new String(decryptedUid);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (CryptoInitializationException e) {
                e.printStackTrace();
            } catch (KeyChainException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new String[]{rawDecryptedToken, rawDecryptedUid};
        */

        return new String[]{encryptedToken, encryptedUid};
    }
}
