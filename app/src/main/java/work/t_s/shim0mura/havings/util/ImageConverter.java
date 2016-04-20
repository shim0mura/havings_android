package work.t_s.shim0mura.havings.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import timber.log.Timber;
import work.t_s.shim0mura.havings.R;

/**
 * Created by shim0mura on 2016/04/18.
 */
public class ImageConverter {

    public static String convertImageToBase64(Activity activity, Uri u){
        File f = new File(u.getPath());
        if(f.exists()){
            Timber.d("fileexist %s", f.getPath());
        }else{
            String fileName = System.currentTimeMillis() + "";
            f = new File(activity.getCacheDir(), fileName);
            try {
                Timber.d("image: filename %s", fileName);
                Timber.d("image: create_file %s", String.valueOf(f.createNewFile()));
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        byte[] bitmapdata;
        try{
            InputStream is = activity.getContentResolver().openInputStream(u);

            Bitmap bitmap = BitmapFactory.decodeStream(is);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bitmapdata = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        }catch(Exception e){
            e.printStackTrace();
            bitmapdata = null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("data:");
        sb.append(ViewUtil.getMimeType(u, activity));
        sb.append(";base64,");
        String base64Img = Base64.encodeToString(bitmapdata, Base64.NO_WRAP);
        sb.append(base64Img);

        return sb.toString();
    }

}
