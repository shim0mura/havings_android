package work.t_s.shim0mura.havings.presenter;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import timber.log.Timber;
import work.t_s.shim0mura.havings.ItemFormActivity;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.Item;
import work.t_s.shim0mura.havings.model.StatusCode;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageEntity;
import work.t_s.shim0mura.havings.model.entity.ModelErrorEntity;
import work.t_s.shim0mura.havings.model.entity.TagEntity;
import work.t_s.shim0mura.havings.model.entity.UserListEntity;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.model.realm.Tag;
import work.t_s.shim0mura.havings.util.ApiErrorUtil;
import work.t_s.shim0mura.havings.util.ViewUtil;
import work.t_s.shim0mura.havings.view.ListSelectAdapter;

/**
 * Created by shim0mura on 2015/12/18.
 */
public class FormPresenter {
    Activity activity;
    Item item;
    static ApiService service;

    public static final String FAB_TYPE_ADD_ITEM = "addItem";
    public static final String FAB_TYPE_EDIT_ITEM = "editItem";

    private static final String ITEM_POST_HASH_KEY = "item";

    protected String TAG = "FormPresenter: ";

    public FormPresenter(Context c){
        activity = (Activity)c;
        service = ApiServiceManager.getService(activity);
        item = new Item();
    }

    public List<TagEntity> getTagEntities(){
        Realm realm = Realm.getInstance(activity);
        RealmResults<Tag> result = realm.where(Tag.class).equalTo("isDeleted", false).findAll();

        List<TagEntity> tags = new ArrayList<TagEntity>();

        for(Tag t: result){
            TagEntity tagEntity = new TagEntity();

            tagEntity.id = t.getId();
            tagEntity.name = t.getName();
            tagEntity.yomiJp = t.getYomiJp();
            tagEntity.yomiRoma = t.getYomiRoma();
            tagEntity.priority = t.getPriority();
            tagEntity.tagType = t.getTagType();
            tags.add(tagEntity);
        }

        return tags;
    }

    public void setItemImageDateListener(final ImageView image, final TextView imageDate, final Calendar currentCalendar){
        final Date currentDate = new Date(currentCalendar.getTimeInMillis());
        imageDate.setText(ViewUtil.dateToString(currentDate, true));
        image.setTag(R.id.IMAGE_DATE, currentDate);

        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener(){
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                GregorianCalendar d = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                Date date = new Date(d.getTimeInMillis());
                imageDate.setText(ViewUtil.dateToString(date, true));
                image.setTag(R.id.IMAGE_DATE, date);
                image.setTag(R.id.IMAGE_METADATA_CHANGED, true);

            }
        };

        imageDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = new GregorianCalendar();
                DatePickerDialog datePicker = new DatePickerDialog(activity, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                datePicker.getDatePicker().setMaxDate(cal.getTimeInMillis());
                datePicker.show();
            }
        });
    }

    public void setItemImageMemoListener(final ImageView image, final TextView imageMemo){
        imageMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editor = new EditText(activity);

                InputFilter[] inputFilter = new InputFilter[1];
                inputFilter[0] = new InputFilter.LengthFilter(ItemImageEntity.MAX_MEMO_SIZE);
                editor.setFilters(inputFilter);

                if(!imageMemo.getText().equals(activity.getString(R.string.prompt_image_memo))){
                    editor.setText(imageMemo.getText());
                }else if(imageMemo.getText().toString().isEmpty()){
                    editor.setText(activity.getString(R.string.prompt_image_memo));
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(R.string.prompt_image_memo)
                        .setView(editor);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        imageMemo.setText(editor.getText());
                        image.setTag(R.id.IMAGE_MEMO, editor.getText());
                        image.setTag(R.id.IMAGE_METADATA_CHANGED, true);
                    }
                });
                builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.create().show();
            }
        });
    }

    public void setItemImageDirectDeleteListener(final ViewGroup imageContainer, final View pictureView, ImageView delete){
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageContainer.removeView(pictureView);
            }
        });
    }

    public void setItemImageDeleteListener(final ImageView image, final View pictureView, ImageView delete){
        final ViewGroup cancelDelete = (ViewGroup)pictureView.findViewById(R.id.cancel_delete);
        final ViewGroup metaData = (ViewGroup)pictureView.findViewById(R.id.image_meta_data);
        final ImageView deleteIcon = (ImageView)pictureView.findViewById(R.id.delete_icon);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelDelete.setVisibility(View.VISIBLE);
                metaData.setVisibility(View.GONE);
                deleteIcon.setVisibility(View.VISIBLE);

                image.setTag(R.id.IS_DELETING_IMAGE, true);
            }
        });
        cancelDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelDelete.setVisibility(View.GONE);
                metaData.setVisibility(View.VISIBLE);
                deleteIcon.setVisibility(View.GONE);

                image.setTag(R.id.IS_DELETING_IMAGE, false);
            }
        });
    }

    public List<ItemImageEntity> constructAddingImage(List<ImageView> images){

        List<ItemImageEntity> itemImageEntities = new ArrayList<ItemImageEntity>();

        // uriの存在チェック、複数画像のセット
        for(ImageView imageView : images){

            Uri u = (Uri)imageView.getTag(R.id.ADDED_IMAGE_URI);
            if(u == null){
                continue;
            }

            File f = new File(u.getPath());
            if(f.exists()){
                Log.d("fileexist", f.getPath());
            }else{
                String fileName = System.currentTimeMillis() + "";
                f = new File(activity.getCacheDir(), fileName);
                try {
                    Log.d("filename", fileName);
                    Log.d("create_file", String.valueOf(f.createNewFile()));
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

            Log.d(TAG, u.getPath());
            Log.d(TAG, ViewUtil.getMimeType(u, activity));

            //RequestBody req = RequestBody.create(MediaType.parse(ViewUtil.getMimeType(u, activity)), f);
            ItemImageEntity itemImage = new ItemImageEntity();
            itemImage.imageData = sb.toString();
            itemImage.date = (Date)imageView.getTag(R.id.IMAGE_DATE);
            if(imageView.getTag(R.id.IMAGE_MEMO) != null){
                itemImage.memo = imageView.getTag(R.id.IMAGE_MEMO).toString();
            }else{
                itemImage.memo = "";
            }
            itemImageEntities.add(itemImage);
        }

        return itemImageEntities;
    }

    public void attemptToCreateItem(ItemEntity itemEntity){
        HashMap<String, ItemEntity> hashItem = new HashMap<String, ItemEntity>();

        if(isValidItemToCreate(itemEntity)){
            hashItem.put(ITEM_POST_HASH_KEY, itemEntity);
        }else{
            return;
        }

        Call<ItemEntity> call = service.createItem(hashItem);
        call.enqueue(getCallbackOfPostItem());
    }

    public void attemptToUpdateItem(ItemEntity itemEntity){
        HashMap<String, ItemEntity> hashItem = new HashMap<String, ItemEntity>();

        if(isValidItemToEdit(itemEntity)){
            hashItem.put(ITEM_POST_HASH_KEY, itemEntity);
        }else{
            return;
        }

        Call<ItemEntity> call = service.updateItem(itemEntity.id, hashItem);
        call.enqueue(getCallbackOfPostItem());
    }

    public void attemptToUpdateItemImage(ItemEntity itemEntity){
        HashMap<String, ItemEntity> hashItem = new HashMap<String, ItemEntity>();
        hashItem.put(ITEM_POST_HASH_KEY, itemEntity);

        Call<ItemEntity> call = service.updateItem(itemEntity.id, hashItem);
        call.enqueue(getCallbackOfPostItem());
    }

    public void attemptToDumpItem(ItemEntity itemEntity){
        HashMap<String, ItemEntity> hashItem = new HashMap<String, ItemEntity>();
        hashItem.put(ITEM_POST_HASH_KEY, itemEntity);

        Call<ItemEntity> call = service.dumpItem(itemEntity.id, hashItem);
        call.enqueue(getCallbackOfPostItem());
    }

    public void attemptToDeleteItem(ItemEntity itemEntity){
        //HashMap<String, ItemEntity> hashItem = new HashMap<String, ItemEntity>();
        //hashItem.put(ITEM_POST_HASH_KEY, itemEntity);

        Call<ItemEntity> call = service.deleteItem(itemEntity.id, itemEntity.fellowIds);
        call.enqueue(getCallbackOfPostItem());
    }

    private Callback<ItemEntity> getCallbackOfPostItem(){
        return new Callback<ItemEntity>() {
            @Override
            public void onResponse(Response<ItemEntity> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    ItemEntity item = response.body();
                    BusHolder.get().post(item);
                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else if (response.code() == StatusCode.UnprocessableEntity) {
                    Timber.d("failed to post");

                    ModelErrorEntity error = ApiErrorUtil.parseError(response, retrofit);

                    for(Map.Entry<String, List<String>> e: error.errors.entrySet()){
                        switch(e.getKey()){
                            case "image":
                                sendErrorToImage();
                                break;
                            case "name":
                                sendErrorToListName();
                                break;
                            default:
                                break;
                        }
                    }
                } else {

                }
            }

            @Override
            public void onFailure(Throwable t) {
                Timber.d("failed to post item");
                t.printStackTrace();
            }
        };
    }

    public void getUserListTree(){
        Call<UserListEntity[]> call = service.getUserList();

        call.enqueue(new Callback<UserListEntity[]>() {
            @Override
            public void onResponse(Response<UserListEntity[]> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    UserListEntity[] list = response.body();

                    BusHolder.get().post(list);
                } else if (response.code() == StatusCode.Unauthorized) {
                    Log.d("failed to authorize", "401 failed to authorize");
                } else {
                    Timber.d("something is wrong in userlisttree");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Timber.d("failed to get userlist");
                t.printStackTrace();
            }
        });
    }

    public boolean isValidItemToCreate(ItemEntity itemEntity){
        if(itemEntity.isList){
            if(isValidName(itemEntity) && isImageExist(itemEntity)){
                return true;
            }else{
                return false;
            }
        }else{
            if(isValidName(itemEntity)){
                return true;
            }else{
                return false;
            }
        }
    }

    public boolean isValidItemToEdit(ItemEntity itemEntity){
        if(isValidName(itemEntity)){
            return true;
        }else{
            return false;
        }
    }

    private boolean isValidName(ItemEntity itemEntity){
        if(item.isValidName(itemEntity.name)){
            return true;
        }else{
            sendErrorToListName();
            return false;
        }
    }

    private boolean isImageExist(ItemEntity itemEntity){
        if(itemEntity.imageDataForPost.size() > 0){
            return true;
        }else{
            return false;
        }
    }


    private void sendErrorToListName(){
        BusHolder.get().post(new SetErrorEvent(getResourceIdByName("comp")));
    }

    private void sendErrorToImage(){
        BusHolder.get().post(new SetErrorEvent(getResourceIdByName("validate_image")));
    }

    protected int getResourceIdByName(String id){
        Resources res = activity.getResources();

        return res.getIdentifier(id, "id", activity.getPackageName());
    }

    public static class ListSelectPagerAdapter extends PagerAdapter {

        private Activity activity;
        private View loader;

        public ListSelectPagerAdapter(Activity a){
            activity = a;
            loader = View.inflate(a, R.layout.loading, null);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //return super.getPageTitle(position);
            return position + "";
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==((View)object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            View view;
            if(position == 0) {
                view = activity.getLayoutInflater().inflate(R.layout.category_list_tab, container, false);

                final ExpandableStickyListHeadersListView listView = (se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView)view.findViewById(R.id.list);

                ListSelectAdapter adapter = new ListSelectAdapter(activity);
                listView.setAdapter(adapter);
                listView.setOnHeaderClickListener(new StickyListHeadersListView.OnHeaderClickListener() {
                    @Override
                    public void onHeaderClick(StickyListHeadersListView l, View header, int itemPosition, long headerId, boolean currentlySticky) {
                        if (listView.isHeaderCollapsed(headerId)) {
                            listView.expand(headerId);
                        } else {
                            listView.collapse(headerId);
                        }
                    }
                });

                for(int i: adapter.getKindIds()){
                    listView.collapse(i);
                }

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Timber.d("selected item %s", view.getTag(R.id.TAG_ITEM_ID));
                        Intent data = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putInt(ItemFormActivity.LIST_NAME_TAG_ID_KEY, (int)view.getTag(R.id.TAG_ITEM_ID));
                        data.putExtras(bundle);

                        activity.setResult(Activity.RESULT_OK, data);
                        activity.finish();
                    }
                });
            }else {
                view = activity.getLayoutInflater().inflate(R.layout.item_list_tab, container, false);
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
