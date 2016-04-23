package work.t_s.shim0mura.havings.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.ShareToClipboardActivity;

/**
 * Created by shim0mura on 2016/04/23.
 */
public class Share {

    public static final String TMP_IMAGE_DIR = "work.t_s.havings";
    public static final String TMP_IMAGE_NAME = "temporary_file.jpg";

    public static void startIntent(Context context, String link, String text, ImageView imageView){
        List<Intent> shareIntentList = new ArrayList<Intent>();

        if(text == null){
            text = " ";
        }

        imageView.setDrawingCacheEnabled(true);
        Bitmap bitmap = imageView.getDrawingCache();

        Drawable drawable = imageView.getDrawable();
        if(drawable != null) {
            Timber.d("drawable_is_not_null");
            bitmap = ((GlideBitmapDrawable) imageView.getDrawable().getCurrent()).getBitmap();
        }

        // http://android-note.open-memo.net/sub/system__share_with_sns.html
        List<ResolveInfo> resolveInfoList;
        if(drawable != null) {
            resolveInfoList = context.getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_SEND).setType("image/*"), 0);
        }else{
            resolveInfoList = context.getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_SEND).setType("text/plain"), 0);
        }

        File f = null;

        String filePath = Environment.getExternalStorageDirectory() + File.separator + TMP_IMAGE_DIR + File.separator + TMP_IMAGE_NAME;
        byte[] bitmapdata = null;

        if(drawable != null && bitmap != null) {
            try {
                // http://stackoverflow.com/questions/7661875/how-to-use-share-image-using-sharing-intent-to-share-images-in-android
                // http://nobuo-create.net/sdcard-2/
                // cacheDirだとこのアプリ以外からは画像が見えないので、他のアプリからも見れる場所に保存する
                // Exterenalとはいうものの、実際にはSDカードの中には入ってないらしい
                // このアプリ用のディレクトリを掘って、その中に画像入れるほうが良さそう
                File dir = new File(Environment.getExternalStorageDirectory() + File.separator + TMP_IMAGE_DIR);
                f = new File(filePath);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                f.createNewFile();

            } catch (IOException e) {
                // Error while creating file
                Timber.d("f_is_null");
                f = null;
            }

            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                bitmapdata = bos.toByteArray();

                FileOutputStream fos = new FileOutputStream(f);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
                bitmapdata = null;
            }
        }

        //SNSアプリの一覧
        for(ResolveInfo info : resolveInfoList){
            String packageName = info.activityInfo.packageName.toLowerCase();

            if(packageName.contains("twitter") && info.activityInfo.name.contains("com.twitter.android.composer.ComposerActivity")){
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                if(drawable == null || bitmapdata == null){
                    shareIntent.setType("text/plain");
                }else{
                    shareIntent.setType("image/*");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filePath));
                }
                shareIntent.putExtra(Intent.EXTRA_TEXT, text + " " + link);
                shareIntent.setPackage(info.activityInfo.packageName);
                shareIntent.setClassName(
                        info.activityInfo.packageName,
                        info.activityInfo.name);
                shareIntentList.add(shareIntent);

            }else if(packageName.contains("facebook")){
                // http://primevision.hatenablog.com/entry/2014/11/20/121013
                // 写真かテキストしか送れないので、リンクを送るようにする
                Intent shareIntent = new Intent(Intent.ACTION_SEND).setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, link);
                shareIntent.setPackage(info.activityInfo.packageName);
                shareIntentList.add(shareIntent);

            }else {
                // instagramはテキストを入れれない（公式アナウンスあり）
                // 多分API経由でやらないとダメ

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                if(drawable == null || bitmapdata == null){
                    shareIntent.setType("text/plain");
                }else{
                    shareIntent.setType("image/*");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filePath));
                }

                shareIntent.putExtra(Intent.EXTRA_TEXT, text + " " + link);
                shareIntent.setPackage(info.activityInfo.packageName);
                shareIntentList.add(shareIntent);
            }
        }


        // 追加するコピー機能のActivityのIntent
        Intent clipIntent = new Intent(context, ShareToClipboardActivity.class);
        clipIntent.putExtra(ShareToClipboardActivity.KEY_CLIP_TEXT, link);
        LabeledIntent labeldIntent = new LabeledIntent(clipIntent, context.getPackageName(), context.getString(R.string.prompt_copy_url_link), R.drawable.ic_link_white_36dp);

        shareIntentList.add(labeldIntent);

        Timber.d("size_share %s " ,shareIntentList.size());

        if(!shareIntentList.isEmpty()){
            Intent chooserIntent = Intent.createChooser(shareIntentList.remove(0), context.getString(R.string.prompt_share));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, shareIntentList.toArray(new Parcelable[]{}));
            context.startActivity(chooserIntent);
        }
    }

}
