package work.t_s.shim0mura.havings;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.util.PermissionUtil;

public class ImageChooseActivity extends AppCompatActivity {

    private static final String SERIALIZED_LIST_NAME = "SerializedListName";
    private static final String SERIALIZED_LIST_ID = "SerializedListID";
    private static final String SERIALIZED_TAGS = "SerializedListTags";

    private static final int IMAGE_CHOOSER_FROM_CAMERA_RESULTCODE = 200;
    private static final int IMAGE_CHOOSER_FROM_GALLERY_RESULTCODE = 300;

    private Uri pictureUri;

    private String listName;
    private int listId;
    private String[] tags;

    public static void startActivity(Context context, String listName, List<String> tags, int listId){
        Intent intent = new Intent(context, new Object() {}.getClass().getEnclosingClass());
        intent.putExtra(SERIALIZED_LIST_NAME, listName);
        intent.putExtra(SERIALIZED_LIST_ID, listId);
        intent.putExtra(SERIALIZED_TAGS, tags.toArray(new String[0]));

        Timber.d(listName);

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_choose);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        listName = (String) extras.getSerializable(SERIALIZED_LIST_NAME);
        listId = (int) extras.getSerializable(SERIALIZED_LIST_ID);
        tags = (String[]) extras.getSerializable(SERIALIZED_TAGS);

        setTitle(R.string.prompt_choose_image_for_list);

        ButterKnife.bind(this);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @OnClick(R.id.add_image_from_camera)
    public void launchImageChooserFromCamera(){
        String filename = System.currentTimeMillis() + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        // http://taka-say.hateblo.jp/entry/2015/06/05/010000
        pictureUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
        startActivityForResult(intent, IMAGE_CHOOSER_FROM_CAMERA_RESULTCODE);
    }

    @OnClick(R.id.add_image_from_gallery)
    public void launchImageChooserFromGallery(){
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
        }
        startActivityForResult(intent, IMAGE_CHOOSER_FROM_GALLERY_RESULTCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri result;
        switch(requestCode){
            case IMAGE_CHOOSER_FROM_CAMERA_RESULTCODE:
                if (resultCode != RESULT_OK) {
                    if (pictureUri != null) {
                        Timber.d("image delete");
                        getContentResolver().delete(pictureUri, null, null);
                        pictureUri = null;
                    }
                    return;
                }

                // 画像を取得
                //Uri resultFromCamera = (data == null) ? pictureUri : data.getData();

                result = (data == null) ? pictureUri : data.getData();
                //addNewPicture(resultFromCamera);
                Timber.d("image get!!!");
                Timber.d(result.toString());
                ItemFormActivity.startActivityToCreateList(this, listId, listName, tags, result);

                pictureUri = null;
                break;
            case IMAGE_CHOOSER_FROM_GALLERY_RESULTCODE:
                if (resultCode != RESULT_OK) {
                    return;
                }
                //Uri resultFromGallery = (data == null) ? pictureUri : data.getData();
                result = (data == null) ? pictureUri : data.getData();
                //addNewPicture(resultFromGallery);

                Timber.d("image get!!!");
                Timber.d(result.toString());
                ItemFormActivity.startActivityToCreateList(this, listId, listName, tags, result);

                pictureUri = null;
                break;
            default:
                pictureUri = null;
                result = null;
        }



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermissionUtil.CAMERA_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchImageChooserFromCamera();
                } else {
                }
                break;

            case PermissionUtil.WRITE_EXT_STORAGE_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchImageChooserFromGallery();
                } else {
                }
                break;
        }
    }

}
