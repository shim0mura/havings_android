package work.t_s.shim0mura.havings.model.di;

import android.content.Context;
import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Collection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import okio.Buffer;
import okio.BufferedSink;
import work.t_s.shim0mura.havings.model.ApiService;

/**
 * Created by shim0mura on 2015/11/05.
 */
public class WebApiImpl implements Api {

    private static final OkHttpClient client = new OkHttpClient();

    public WebApiImpl(Context context){

        // 開発環境下でオレオレ証明書を認証できるよう証明書のチェック無効
        // http://stackoverflow.com/questions/31917988/okhttp-javax-net-ssl-sslpeerunverifiedexception-hostname-domain-com-not-verifie
        client.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

        // 証明書の追加
        // サーバ側の開発用オレオレ証明書のcrtを直接文字列に書きだしてそれを追加してる
        // https://github.com/square/okhttp/blob/master/samples/guide/src/main/java/com/squareup/okhttp/recipes/CustomTrust.java
        SSLContext sslContext = sslContextForTrustedCertificates(trustedCertificatesInputStream());
        client.setSslSocketFactory(sslContext.getSocketFactory());

        client.interceptors().add(new JsonHeaderInterceptor());
    }

    public void test(){
        Log.d("ssss", "dddddd");
    }

    public void execute(final Request request, final Callback callback){
        client.newCall(request).enqueue(callback);
    }

    public OkHttpClient getClient(){
        return client;
    }

    private InputStream trustedCertificatesInputStream() {
        // PEM files for root certificates of Comodo and Entrust. These two CAs are sufficient to view
        // https://publicobject.com (Comodo) and https://squareup.com (Entrust). But they aren't
        // sufficient to connect to most HTTPS sites including https://godaddy.com and https://visa.com.
        // Typically developers will need to get a PEM file from their organization's TLS administrator.
        String developmentCertificationAuthority = ""
                + "-----BEGIN CERTIFICATE-----\n"
                + "MIIDtTCCAp2gAwIBAgIJAKX/x+0+2/cXMA0GCSqGSIb3DQEBBQUAMEUxCzAJBgNV\n"
                + "BAYTAmphMRMwEQYDVQQIEwpTb21lLVN0YXRlMSEwHwYDVQQKExhJbnRlcm5ldCBX\n"
                + "aWRnaXRzIFB0eSBMdGQwHhcNMTUxMTAzMDgxMjQ5WhcNMjUxMDMxMDgxMjQ5WjBF\n"
                + "MQswCQYDVQQGEwJqYTETMBEGA1UECBMKU29tZS1TdGF0ZTEhMB8GA1UEChMYSW50\n"
                + "ZXJuZXQgV2lkZ2l0cyBQdHkgTHRkMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIB\n"
                + "CgKCAQEA2FtRQX1qVPgSs2esFyKABFN0XraActxyycc97l5a/NlB1WuhZvqeJGdp\n"
                + "8kUP4/eKpeuMKl6CwtdwwiI8uH2h+ct2JeBpghHEZybQvxLS0DrrWFBrmJRFWoFw\n"
                + "757rCuRFBBvEKxFqLrorwNtwlLN7OhPepFUKVjCuKDPIrjSyXYhHFaQZ5Gf7Noqs\n"
                + "DjNHUWt5vhK2+O+yYpsx6mwVYuTtok/S+8N/R3Z7OW1qcTlBBr9xQzwrntUlD8Dd\n"
                + "hCYVgguz5/EKMBvGzQC68NNYKKHnovbmj4LlB1djr2aYWl+p8FKx5WHfEA/AQ4fC\n"
                + "S2aVdJ8XBzGlUYuKOwq6uVjt4MW6CwIDAQABo4GnMIGkMB0GA1UdDgQWBBS7Z3Gv\n"
                + "/W1GinoFXtRqmBHvt3XMBDB1BgNVHSMEbjBsgBS7Z3Gv/W1GinoFXtRqmBHvt3XM\n"
                + "BKFJpEcwRTELMAkGA1UEBhMCamExEzARBgNVBAgTClNvbWUtU3RhdGUxITAfBgNV\n"
                + "BAoTGEludGVybmV0IFdpZGdpdHMgUHR5IEx0ZIIJAKX/x+0+2/cXMAwGA1UdEwQF\n"
                + "MAMBAf8wDQYJKoZIhvcNAQEFBQADggEBAIp4CFEWzz464xOvIzhAR7ct5TOiPtXl\n"
                + "wp7uzU+6f64Nn0vcflZmUy2QWc154+aw+WpZXtLATGaGDCYxp8OYPS/ye5uG8Jcp\n"
                + "NbKmDs3gZ3HuQptNpcqtrPKhQer8ljTASua/yBmS0SaaWlOoepTZCqSxan9eEh8p\n"
                + "Hgk62PqLy67nIPEJfZLXMQwZjznBuV3kITJkqcWm4KYeOcQ5/x/TthEySSBLpUt1\n"
                + "D5sSUD0QAoTG7YeMQ0lAu5rKjurjviHSzOSDI1C1PII5RY/FIwL9cMr69Oa8Fr6H\n"
                + "KgAbe13ZLBkIE/ZUFDE2A4YnmXQ+unOJKcswN30n2StR7hJXBBn8n8Q=\n"
                + "-----END CERTIFICATE-----\n";

        return new Buffer()
                .writeUtf8(developmentCertificationAuthority)
                .inputStream();
    }

    public SSLContext sslContextForTrustedCertificates(InputStream in) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(in);
            if (certificates.isEmpty()) {
                throw new IllegalArgumentException("expected non-empty set of trusted certificates");
            }

            // Put the certificates a key store.
            char[] password = "password".toCharArray(); // Any password will work.
            KeyStore keyStore = newEmptyKeyStore(password);
            int index = 0;
            for (Certificate certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificate);
            }

            // Wrap it up in an SSL context.
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, password);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(),
                    new SecureRandom());
            return sslContext;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream in = null; // By convention, 'null' creates an empty key store.
            keyStore.load(in, password);
            return keyStore;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private class JsonHeaderInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();

            Request newRequest = original.newBuilder()
                        .addHeader("Accept", ApiService.JSON.toString())
                        .build();

            return chain.proceed(newRequest);
        }
    }

}
