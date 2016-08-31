package work.t_s.shim0mura.havings.presenter;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import com.tokenautocomplete.FilteredArrayAdapter;

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
import work.t_s.shim0mura.havings.ImageChooseActivity;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.DefaultTag;
import work.t_s.shim0mura.havings.model.Item;
import work.t_s.shim0mura.havings.model.StatusCode;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageEntity;
import work.t_s.shim0mura.havings.model.entity.ModelErrorEntity;
import work.t_s.shim0mura.havings.model.entity.UserListEntity;
import work.t_s.shim0mura.havings.model.event.AlertEvent;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.model.realm.TagEntity;
import work.t_s.shim0mura.havings.util.ApiErrorUtil;
import work.t_s.shim0mura.havings.util.SpaceTokenizer;
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

    public static final String ITEM_POST_HASH_KEY = "item";

    protected String TAG = "FormPresenter: ";

    public FormPresenter(Context c){
        activity = (Activity)c;
        service = ApiServiceManager.getService(activity);
        item = new Item();
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
                datePicker.setTitle(activity.getString(R.string.prompt_image_date_select));
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

                BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
                decodeOptions.inJustDecodeBounds = true;

                BitmapFactory.decodeStream(is, null, decodeOptions);

                int actualWidth = decodeOptions.outWidth;
                int actualHeight = decodeOptions.outHeight;

                int desiredWidth = getResizedDimension(800, 800,
                        actualWidth, actualHeight);
                int desiredHeight = getResizedDimension(800, 800,
                        actualHeight, actualWidth);
                decodeOptions.inJustDecodeBounds = false;

                decodeOptions.inSampleSize =
                        findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);
                Bitmap bitmap;
                is = activity.getContentResolver().openInputStream(u);

                Bitmap tempBitmap = BitmapFactory.decodeStream(is, null, decodeOptions);
                is.close();
                if (tempBitmap != null && (tempBitmap.getWidth() > desiredWidth ||
                        tempBitmap.getHeight() > desiredHeight)) {
                    bitmap = Bitmap.createScaledBitmap(tempBitmap,
                            desiredWidth, desiredHeight, true);
                    tempBitmap.recycle();
                } else {
                    bitmap = tempBitmap;
                }
                Timber.d("bitmap_ %s %s %s", bitmap.toString(), bitmap.getWidth(), bitmap.getHeight());

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

    private int getResizedDimension(int maxPrimary, int maxSecondary, int actualPrimary,
                                    int actualSecondary) {
        if ((maxPrimary == 0) && (maxSecondary == 0)) {
            return actualPrimary;
        }

        if (maxPrimary == 0) {
            double ratio = (double) maxSecondary / (double) actualSecondary;
            return (int) (actualPrimary * ratio);
        }

        if (maxSecondary == 0) {
            return maxPrimary;
        }

        double ratio = (double) actualSecondary / (double) actualPrimary;
        int resized = maxPrimary;

        if ((resized * ratio) < maxSecondary) {
            resized = (int) (maxSecondary / ratio);
        }
        return resized;
    }
    static int findBestSampleSize(
            int actualWidth, int actualHeight, int desiredWidth, int desiredHeight) {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float n = 1.0f;
        while ((n * 2) <= ratio) {
            n *= 2;
        }
        return (int) n;
    }


    public void attemptToCreateItem(ItemEntity itemEntity){
        HashMap<String, ItemEntity> hashItem = new HashMap<String, ItemEntity>();

        if(isValidItemToCreate(itemEntity)){
            hashItem.put(ITEM_POST_HASH_KEY, itemEntity);
        }else{
            if(!isValidName(itemEntity)){
                sendErrorToListName();
            }
            if(itemEntity.isList && !isImageExist(itemEntity)){
                sendErrorToImage();
            }
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
            sendErrorToListName();
            return;
        }

        Call<ItemEntity> call = service.updateItem(itemEntity.id, hashItem);
        call.enqueue(getCallbackOfPostItem());
    }

    public void attemptToUpdateItemImage(ItemEntity itemEntity){
        HashMap<String, ItemEntity> hashItem = new HashMap<String, ItemEntity>();
        hashItem.put(ITEM_POST_HASH_KEY, itemEntity);

        if(!isImageExist(itemEntity)){
            sendErrorToImage();
        }

        Call<ItemEntity> call = service.addImage(itemEntity.id, hashItem);
        call.enqueue(new Callback<ItemEntity>() {
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
                    BusHolder.get().post(new AlertEvent(AlertEvent.SOMETHING_OCCURED_IN_SERVER));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Timber.d("failed to post item");
                t.printStackTrace();
                BusHolder.get().post(new AlertEvent(AlertEvent.SOMETHING_OCCURED_IN_SERVER));

            }
        });
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
                    BusHolder.get().post(new AlertEvent(AlertEvent.SOMETHING_OCCURED_IN_SERVER));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Timber.d("failed to post item");
                t.printStackTrace();
                BusHolder.get().post(new AlertEvent(AlertEvent.SOMETHING_OCCURED_IN_SERVER));

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

    public static View getCustomTabForListNameSelect(Activity activity, int position){
        View tab = activity.getLayoutInflater().inflate(R.layout.partial_list_select_tab_header, null);
        ImageView iconTypeImage = (ImageView) tab.findViewById(R.id.tab_icon);
        TextView imageTab = (TextView) tab.findViewById(R.id.tab_name);
        switch(position) {
            case 0:
                iconTypeImage.setImageResource(R.drawable.place_white);
                imageTab.setText(R.string.prompt_list_type_place);
                break;
            case 1:
                iconTypeImage.setImageResource(R.drawable.closet_white);
                imageTab.setText(R.string.prompt_list_type_closet);
                break;
            case 2:
                iconTypeImage.setImageResource(R.drawable.category_white);
                imageTab.setText(R.string.prompt_list_type_category);
                break;
            case 3:
                iconTypeImage.setImageResource(R.drawable.ic_mode_edit_white_24dp);
                imageTab.setText(R.string.prompt_list_type_input);
                break;
        }

        return tab;
    }

    public static class ListSelectPagerAdapter extends PagerAdapter {

        private Activity activity;
        private int listId = 0;

        private ArrayList<String> tagStringByPlace = new ArrayList<>();
        private ArrayList<String> tagStringByCloset = new ArrayList<>();
        private ArrayList<work.t_s.shim0mura.havings.model.entity.TagEntity> tagEntities = new ArrayList<>();

        private ArrayList<String> inputTags = new ArrayList<>();
        private MultiAutoCompleteTextView textView;

        public ListSelectPagerAdapter(Activity a, int id) {
            activity = a;
            listId = id;


            Realm realm = Realm.getInstance(DefaultTag.getRealmConfig(activity));

            RealmResults<TagEntity> tagsByPlace = realm.where(TagEntity.class).equalTo("tagType", DefaultTag.TAG_TYPE_PLACE).equalTo("isDeleted", false).findAll();
            for(TagEntity t: tagsByPlace){
                tagStringByPlace.add(t.getName());
            }
            RealmResults<TagEntity> tagsByCloset = realm.where(TagEntity.class).equalTo("tagType", DefaultTag.TAG_TYPE_CLOSET).equalTo("isDeleted", false).findAll();
            for(TagEntity t: tagsByCloset){
                tagStringByCloset.add(t.getName());
            }
            tagEntities = new ArrayList<>(DefaultTag.getSingleton(activity).getTagEntities());

        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            View view;
            switch(position){
                case 0:
                    view = instantiateTab(container, position);
                    break;
                case 1:
                    view = instantiateTab(container, position);
                    break;
                case 2:
                    view = instantiateCategoryTab(container);
                    break;
                case 3:
                    view = instantiateInputTab(container);
                    break;
                default:
                    view = activity.getLayoutInflater().inflate(R.layout.item_list_tab, container, false);

            }

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        private View instantiateCategoryTab(ViewGroup container) {
            View view = activity.getLayoutInflater().inflate(R.layout.category_list_tab, container, false);

            final ExpandableStickyListHeadersListView listView = (se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView) view.findViewById(R.id.list);

            ListSelectAdapter adapter = new ListSelectAdapter(activity);
            listView.setAdapter(adapter);
            listView.setOnHeaderClickListener(new StickyListHeadersListView.OnHeaderClickListener() {
                @Override
                public void onHeaderClick(StickyListHeadersListView l, View header, int itemPosition, long headerId, boolean currentlySticky) {
                    ImageView arrow = (ImageView) header.findViewById(R.id.arrow);

                    if (listView.isHeaderCollapsed(headerId)) {
                        arrow.setImageResource(R.drawable.ic_keyboard_arrow_down_black_18dp);
                        listView.expand(headerId);
                    } else {
                        arrow.setImageResource(R.drawable.ic_keyboard_arrow_right_black_18dp);
                        listView.collapse(headerId);
                    }
                }
            });

            for (int i : adapter.getKindIds()) {
                listView.collapse(i);
            }

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Timber.d("selected item %s", view.getTag(R.id.TAG_ITEM_ID));
                    /*
                    Intent data = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putInt(ItemFormActivity.LIST_NAME_TAG_ID_KEY, (int) view.getTag(R.id.TAG_ITEM_ID));
                    data.putExtras(bundle);
                    */
                    String selected = (String) view.getTag(R.id.TAG_ITEM_NAME);
                    inputTags.add(selected);

                    ImageChooseActivity.startActivity(activity, selected, inputTags, listId);

                    //convertView.setTag(R.id.TAG_ITEM_NAME, tag.get(TAG_NAME));
                    //activity.setResult(Activity.RESULT_OK, data);
                    //activity.finish();
                }
            });

            return view;
        }

        private View instantiateTab(ViewGroup container, final int tabPosition) {
            View view = activity.getLayoutInflater().inflate(R.layout.partial_list_name_by_place_tab, container, false);

            final ListView listView = (ListView)view.findViewById(R.id.list_name);

            listView.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, (tabPosition == 0 ? tagStringByPlace : tagStringByCloset)));

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Timber.d("selected item %s", view.getTag(R.id.TAG_ITEM_ID));

                    String selected = (tabPosition == 0 ? tagStringByPlace.get(position) : tagStringByCloset.get(position));
                    inputTags.add(selected);

                    ImageChooseActivity.startActivity(activity, selected, inputTags, listId);

                }
            });

            return view;
        }

        private View instantiateInputTab(ViewGroup container){
            View view = activity.getLayoutInflater().inflate(R.layout.partial_list_name_input_tab, container, false);

            textView = (MultiAutoCompleteTextView) view.findViewById(R.id.comp);

            ArrayAdapter<work.t_s.shim0mura.havings.model.entity.TagEntity> adapter = new FilteredArrayAdapter<work.t_s.shim0mura.havings.model.entity.TagEntity>(activity, android.R.layout.simple_list_item_1, tagEntities) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {
                        LayoutInflater l = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                        convertView = l.inflate(android.R.layout.simple_list_item_1, parent, false);
                    }

                    work.t_s.shim0mura.havings.model.entity.TagEntity p = getItem(position);
                    ((TextView)convertView).setText(p.getName());
                    return convertView;
                }

                @Override
                protected boolean keepObject(work.t_s.shim0mura.havings.model.entity.TagEntity tag, String mask) {
                    mask = mask.toLowerCase();
                    return tag.getName().toLowerCase().startsWith(mask) || tag.getYomiJp().startsWith(mask) || tag.getYomiRoma().toLowerCase().startsWith(mask);
                }
            };

            textView.setAdapter(adapter);
            textView.setTokenizer(new SpaceTokenizer());
            textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Timber.d("item clicked!! id: %s", id);
                    TextView selected = (TextView) view;
                    inputTags.add(selected.getText().toString());
                }
            });

            view.findViewById(R.id.list_name_select_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Timber.d("click");
                    ImageChooseActivity.startActivity(activity, textView.getText().toString(), inputTags, listId);
                }
            });
            return view;
        }

    }
}
