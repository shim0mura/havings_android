package work.t_s.shim0mura.havings;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.GeneralResult;
import work.t_s.shim0mura.havings.model.entity.ResultEntity;
import work.t_s.shim0mura.havings.model.entity.UserEntity;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.presenter.UserPresenter;
import work.t_s.shim0mura.havings.util.ImageConverter;
import work.t_s.shim0mura.havings.util.PermissionUtil;
import work.t_s.shim0mura.havings.util.ViewUtil;

public class ProfileEditActivity extends AppCompatActivity {

    private static final String SERIALIZED_USER = "SerializedUser";
    private static final String SERIALIZED_USER_ID = "SerializedUserId";
    protected static final int IMAGE_CHOOSER_FROM_CAMERA_RESULTCODE = 200;
    protected static final int IMAGE_CHOOSER_FROM_GALLERY_RESULTCODE = 300;

    private UserPresenter userPresenter;
    private UserEntity userEntity;
    private int userId;
    private TextView changeImagePrompt;
    private ImageView changedProfileImage;
    private Uri pictureUri;

    @Bind(R.id.user_name) EditText userName;
    @Bind(R.id.profile) EditText profile;
    @Bind(R.id.add_profile_image) View addImage;
    @Bind(R.id.exist_profile_image) View existProfileImage;

    public static void startActivity(Context context, UserEntity user){
        Intent intent = new Intent(context, new Object() {}.getClass().getEnclosingClass());
        intent.putExtra(SERIALIZED_USER, user);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int id){
        Intent intent = new Intent(context, new Object() {}.getClass().getEnclosingClass());
        intent.putExtra(SERIALIZED_USER_ID, id);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        setContentView(R.layout.activity_profile_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        userEntity = (UserEntity)extras.getSerializable(SERIALIZED_USER);
        userId = extras.getInt(SERIALIZED_USER_ID, 0);

        userPresenter = new UserPresenter(this);

        if(userEntity == null && userId != 0){
            userPresenter.getUser(userId);
        }else if(userEntity != null){
            setDefaultValue();
        }

        changedProfileImage = (ImageView)addImage.findViewById(R.id.added_image);
        changeImagePrompt = (TextView)addImage.findViewById(R.id.image_prompt);
        changeImagePrompt.setText(R.string.prompt_change_profile_image);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusHolder.get().register(this);
        Timber.d("register observer");
    }

    @Override
    protected void onPause() {
        BusHolder.get().unregister(this);
        Timber.d("unregister observer");
        super.onPause();
    }

    @OnClick(R.id.change_profile)
    public void changeProfile() {
        Timber.d("change profile");

        Uri changedImageUri = (Uri)changedProfileImage.getTag(R.id.ADDED_IMAGE_URI);
        if(changedImageUri != null) {
            userEntity.image = ImageConverter.convertImageToBase64(this, changedImageUri);
        }

        userPresenter.editProfile(userEntity);
    }

    @Subscribe
    public void applySuccessResult(ResultEntity resultEntity){
        Timber.d("success to change");
    }

    @Subscribe
    public void applyGereralError(SetErrorEvent errorEvent){
        switch(errorEvent.resultType){
            default:
                Timber.w("Unexpected ResultCode in Error Returned in changeProfile... code: %s, relatedId: %s", errorEvent.resultType);
                break;
        }
    }

    @Subscribe
    public void getUser(UserEntity user){
        userEntity = user;
        setDefaultValue();
    }

    @OnClick(R.id.add_profile_image)
    public void launchImageChooser(){
        final Activity self = this;
        final String[] imageSources = {"写真を撮る", "フォルダから画像を選択"};
        new AlertDialog.Builder(this)
                .setTitle(this.getString(R.string.prompt_change_profile_image))
                .setItems(imageSources, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which){
                            // https://developer.android.com/intl/zh-tw/training/permissions/requesting.html
                            // http://yuki312.blogspot.jp/2015/05/runtime-permission.html
                            case 0:
                                if (!PermissionUtil.hasSelfPermission(self, Manifest.permission.CAMERA)) {
                                    ActivityCompat.requestPermissions(self, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionUtil.CAMERA_PERMISSION_REQUEST_CODE);
                                } else {
                                    launchImageChooserFromCamera();
                                }
                                break;
                            case 1:
                                if (!PermissionUtil.hasSelfPermission(self, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    ActivityCompat.requestPermissions(self, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionUtil.WRITE_EXT_STORAGE_PERMISSION_REQUEST_CODE);
                                } else {
                                    launchImageChooserFromGallery();
                                }
                                break;
                        }
                    }
                })
                .show();
    }

    private void setDefaultValue(){
        userName.setText(userEntity.name);
        profile.setText(userEntity.description);
        ImageView existImage = (ImageView)existProfileImage.findViewById(R.id.added_image);
        existImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if(userEntity.image != null){
            String thumbnailUrl = ApiService.BASE_URL + userEntity.image;
            Glide.with(this).load(thumbnailUrl).into(existImage);
            TextView existingImagePrompt = (TextView)existProfileImage.findViewById(R.id.image_prompt);
            existingImagePrompt.setText(R.string.prompt_existing_profile_image);
        }else{
            existImage.setImageResource(R.drawable.bg);
        }
    }

    private void launchImageChooserFromCamera(){
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

    private void launchImageChooserFromGallery(){
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

    private void changeProfileImage(Uri imageUri){
        changedProfileImage.setImageURI(imageUri);
        changedProfileImage.setTag(R.id.ADDED_IMAGE_URI, imageUri);
        changedProfileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

        changeImagePrompt.setText(R.string.prompt_changed_profile_image);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                Uri resultFromCamera = (data == null) ? pictureUri : data.getData();

                changeProfileImage(resultFromCamera);

                pictureUri = null;
                break;
            case IMAGE_CHOOSER_FROM_GALLERY_RESULTCODE:
                if (resultCode != RESULT_OK) {
                    return;
                }
                Uri resultFromGallery = (data == null) ? pictureUri : data.getData();

                changeProfileImage(resultFromGallery);

                pictureUri = null;
                break;
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
