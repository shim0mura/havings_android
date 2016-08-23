package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.squareup.otto.Subscribe;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.ApiKey;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.GeneralResult;
import work.t_s.shim0mura.havings.model.Item;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageEntity;
import work.t_s.shim0mura.havings.model.entity.ResultEntity;
import work.t_s.shim0mura.havings.model.event.AlertEvent;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.presenter.ItemPresenter;
import work.t_s.shim0mura.havings.presenter.UserListPresenter;
import work.t_s.shim0mura.havings.util.Share;
import work.t_s.shim0mura.havings.util.ViewUtil;

public class ImageDetailActivity extends DrawerActivity {

    private static final String SERIALIZED_ITEM = "SerializedItem";
    private static final String SERIALIZED_ITEM_ID = "SerializedItemId";
    private static final String SERIALIZED_ITEM_IMAGE = "SerializedItemImage";
    private static final String SERIALIZED_ITEM_IMAGE_ID = "SerializedItemImageId";

    private static final int MENU_EDIT = 100;
    private static final int MENU_DELETE = 200;

    private int itemId;
    private ItemEntity item;
    private ItemImageEntity itemImageEntity;
    private ItemPresenter itemPresenter;
    private ProgressDialog progressDialog;

    private Date updateDate;
    private String updateMemo;

    private int userId;

    @Bind(R.id.item_name) TextView itemName;
    @Bind(R.id.image_date) TextView imageDate;
    @Bind(R.id.detail_image) ImageView detailImage;
    @Bind(R.id.image_memo) TextView imageMemo;
    @Bind(R.id.image_favorite_count) TextView imageFavoriteCount;
    @Bind(R.id.image_favorite_button) ImageView imageFavoriteButton;
    @Bind(R.id.image_share_button) ImageView imageShareButton;

    public static void startActivity(Context context, ItemEntity item, ItemImageEntity itemImage){
        Intent intent = new Intent(context, new Object() {}.getClass().getEnclosingClass());
        intent.putExtra(SERIALIZED_ITEM, item);
        intent.putExtra(SERIALIZED_ITEM_IMAGE, itemImage);

        context.startActivity(intent);
    }

    public static void startActivity(Context context, ItemEntity item, int itemImageId){
        Intent intent = new Intent(context, ImageDetailActivity.class);
        intent.putExtra(SERIALIZED_ITEM, item);
        intent.putExtra(SERIALIZED_ITEM_IMAGE_ID, itemImageId);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, int itemId, int itemImageId){
        Intent intent = new Intent(context, ImageDetailActivity.class);
        intent.putExtra(SERIALIZED_ITEM_ID, itemId);
        intent.putExtra(SERIALIZED_ITEM_IMAGE_ID, itemImageId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        item = (ItemEntity)extras.getSerializable(SERIALIZED_ITEM);
        itemPresenter = new ItemPresenter(this);

        setContentView(R.layout.activity_image_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        setTitle(null);

        int itemImageId = extras.getInt(SERIALIZED_ITEM_IMAGE_ID, 0);

        if(itemImageId == 0) {
            itemImageEntity = (ItemImageEntity)extras.getSerializable(SERIALIZED_ITEM_IMAGE);
            renderImages();
        }else if(item != null){
            itemPresenter.getItemImage(item.id, itemImageId);
        }else {
            itemId = extras.getInt(SERIALIZED_ITEM_ID, 0);
            if(itemId != 0) {
                itemPresenter.getItemImage(itemId, itemImageId);
            }
        }

        userId = ApiKey.getSingleton(this).getUserId();
        onCreateDrawer(false);

    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(item != null && item.owner.id == userId){
            menu.add(Menu.NONE, MENU_EDIT, Menu.NONE, R.string.prompt_image_detail_edit_data);
            menu.add(Menu.NONE, MENU_DELETE, Menu.NONE, R.string.prompt_image_detail_delete_image);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = true;
        switch (item.getItemId()) {
            default:
                ret = super.onOptionsItemSelected(item);
                break;
            case MENU_EDIT:
                showEditDialog();
                ret = true;
                break;
            case MENU_DELETE:
                showDeleteDialog();
                ret = true;
                break;
        }
        return ret;
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusHolder.get().register(this);
        Timber.d("regist action");
    }

    @Override
    protected void onPause() {
        BusHolder.get().unregister(this);
        Timber.d("unregist action");
        super.onPause();
    }

    @Subscribe
    public void getItemImageEntity(ItemImageEntity itemImage){
        itemImageEntity = itemImage;
        renderImages();
    }

    private void renderImages(){
        String title;
        if(item != null) {
            title = item.name + getString(R.string.postfix_prompt_of_item_image);
        }else if(itemImageEntity.itemName != null){
            title = itemImageEntity.itemName + getString(R.string.postfix_prompt_of_item_image);
        }else {
            title = null;
        }
        setTitle(title);

        if(itemId != 0 && itemImageEntity.itemName != null){
            itemName.setVisibility(View.VISIBLE);
            itemName.setText(itemImageEntity.itemName);
        }

        updateImageData();
        toggleFavoriteState();

        String thumbnail = ApiServiceManager.getSingleton(this).getApiUrl() + itemImageEntity.url;
        Glide.with(this)
                .load(thumbnail)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        Timber.d("failed to get image");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        findViewById(R.id.image_loader).setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(detailImage);
    }

    private void showEditDialog(){
        final Activity act = this;
        final Dialog d = new Dialog(this);
        d.setTitle(getText(R.string.prompt_image_detail_edit_data));
        d.setContentView(R.layout.partial_image_edit_dialog);

        final TextView dateText = (TextView) d.findViewById(R.id.date_text);
        Button cancel = (Button)d.findViewById(R.id.cancel);
        Button post = (Button)d.findViewById(R.id.post);
        final TextView memo = (TextView)d.findViewById(R.id.memo);
        if(itemImageEntity.memo != null){
            memo.setText(itemImageEntity.memo);
        }

        final Calendar currentCalendar = new GregorianCalendar();
        updateDate = new Date(currentCalendar.getTimeInMillis());
        dateText.setText(ViewUtil.dateToString(itemImageEntity.addedDate, true));

        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener(){
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                GregorianCalendar d = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                Date date = new Date(d.getTimeInMillis());
                updateDate = date;
                dateText.setText(ViewUtil.dateToString(date, true));
            }
        };

        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = new GregorianCalendar();
                if(itemImageEntity.addedDate != null){
                    cal.setTime(itemImageEntity.addedDate);
                }
                DatePickerDialog datePicker = new DatePickerDialog(act, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                datePicker.getDatePicker().setMaxDate(currentCalendar.getTimeInMillis());
                datePicker.setTitle(act.getString(R.string.prompt_image_date_select));
                datePicker.show();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = ProgressDialog.show(act, getTitle(), getString(R.string.prompt_sending), true);
                updateMemo = memo.getText().toString();
                itemPresenter.updateImageData(item.id, itemImageEntity.id, updateDate, memo.getText().toString());
                d.dismiss();
            }
        });

        d.show();
    }

    private void showDeleteDialog(){
        final Activity act = this;
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.prompt_image_delete))
                .setMessage(getString(R.string.prompt_image_delete_detail))
                .setPositiveButton(getString(R.string.prompt_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog = ProgressDialog.show(act, getTitle(), getString(R.string.prompt_sending), true);
                        itemPresenter.deleteImage(item.id, itemImageEntity.id);
                    }
                })
                .setNegativeButton(getString(R.string.prompt_cancel), null)
                .show();
    }

    private void updateImageData(){
        imageDate.setText(ViewUtil.dateToString(itemImageEntity.addedDate, true));
        imageFavoriteCount.setText(String.valueOf(itemImageEntity.imageFavoriteCount));
        if(itemImageEntity.memo == null || itemImageEntity.memo.isEmpty()){
            imageMemo.setVisibility(View.GONE);
        }else {
            imageMemo.setText(itemImageEntity.memo);
        }
    }

    @OnClick(R.id.image_favorite_button)
    public void actionFavorite(){
        if(itemImageEntity.isFavorited){
            itemPresenter.unfavoriteItemImage(itemImageEntity.id);
        }else{
            itemPresenter.favoriteItemImage(itemImageEntity.id);
        }
    }

    @OnClick(R.id.image_share_button)
    public void shareThisImage(){
        String itemImageName;
        if(item != null) {
            itemImageName = item.name + getString(R.string.postfix_prompt_of_item_image);
        }else if(itemImageEntity.itemName != null){
            itemImageName = itemImageEntity.itemName + getString(R.string.postfix_prompt_of_item_image);
        }else {
            itemImageName = null;
        }
        Share.startIntent(this, Item.getImagePath(itemImageEntity, this), itemImageName, detailImage);
    }

    @OnClick(R.id.favorite_count_wrapper)
    public void redirectToFavoritedUserList(){
        UserListActivity.startActivity(this, UserListPresenter.ITEM_IMAGE_FAVORITE_USER_LIST, itemImageEntity.id);
    }

    @OnClick(R.id.item_name)
    public void redirectToItem(){
        int id = (itemId != 0 ? itemId : itemImageEntity.itemId);
        ItemActivity.startActivity(this, id);
    }

    @Subscribe
    public void applyGereralResult(ResultEntity resultEntity){
        Timber.d("get general result %s", resultEntity.resultType);
        if(progressDialog != null){
            progressDialog.dismiss();
        }
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.wrapper);

        switch(resultEntity.resultType){
            case GeneralResult.RESULT_FAVORITE_ITEM_IMAGE:
                updateFavoriteProperties(true);
                break;
            case GeneralResult.RESULT_UNFAVORITE_ITEM_IMAGE:
                updateFavoriteProperties(false);
                break;
            case GeneralResult.RESULT_UPDATE_ITEM_IMAGE:
                itemImageEntity.addedDate = updateDate;
                itemImageEntity.memo = updateMemo;
                updateImageData();
                Snackbar.make(coordinatorLayout, getString(R.string.prompt_image_detail_update_sucess), Snackbar.LENGTH_LONG).show();
                break;
            case GeneralResult.RESULT_DELETE_ITEM_IMAGE:
                Snackbar.make(coordinatorLayout, getString(R.string.prompt_image_delete_success), Snackbar.LENGTH_LONG).show();
                break;
            default:
                Timber.w("Unexpected ResultCode Returned... code: %s, relatedId: %s", resultEntity.resultType, resultEntity.relatedId);
                break;
        }
    }

    @Subscribe
    public void applyGereralError(SetErrorEvent errorEvent){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
        switch(errorEvent.resultType){
            case GeneralResult.RESULT_FAVORITE_ITEM_IMAGE:
                Timber.d("failed to favorite item image");
                showAlert(new AlertEvent(AlertEvent.SOMETHING_OCCURED_IN_SERVER));
                break;
            case GeneralResult.RESULT_UNFAVORITE_ITEM_IMAGE:
                Timber.d("failed to unfavorite item image");
                showAlert(new AlertEvent(AlertEvent.SOMETHING_OCCURED_IN_SERVER));
                break;
            case GeneralResult.RESULT_GET_ITEM_IMAGE:
                showAlert(new AlertEvent(AlertEvent.SOMETHING_OCCURED_IN_SERVER));
                break;
            case GeneralResult.RESULT_UPDATE_ITEM_IMAGE:
                showAlert(new AlertEvent(AlertEvent.SOMETHING_OCCURED_IN_SERVER));
                break;
            case GeneralResult.RESULT_DELETE_ITEM_IMAGE:
                showAlert(new AlertEvent(AlertEvent.SOMETHING_OCCURED_IN_SERVER));
                break;
            default:
                Timber.w("Unexpected ResultCode in Error Returned... code: %s, relatedId: %s", errorEvent.resultType);
                break;
        }
    }

    public void showAlert(AlertEvent event){
        new AlertDialog.Builder(this)
                .setTitle(event.title)
                .setMessage(event.message)
                .setPositiveButton("OK", null)
                .show();
    }


    private void updateFavoriteProperties(Boolean isFavorited){
        itemImageEntity.isFavorited = isFavorited;
        int count = Integer.parseInt(imageFavoriteCount.getText().toString());
        if(isFavorited){
            imageFavoriteCount.setText(String.valueOf(count + 1));
        }else{
            count = count - 1;
            if(count < 0){
                count = 0;
            }
            imageFavoriteCount.setText(String.valueOf(count));
        }
        toggleFavoriteState();
    }

    private void toggleFavoriteState(){
        if(itemImageEntity.isFavorited){
            imageFavoriteButton.setImageResource(R.drawable.ic_already_favorite_36dp);
        }else{
            imageFavoriteButton.setImageResource(R.drawable.ic_favorite_border_white_36dp);
        }
    }

}
