package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.otto.Subscribe;
import com.tokenautocomplete.FilteredArrayAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.entity.TagEntity;
import work.t_s.shim0mura.havings.model.realm.Tag;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageEntity;
import work.t_s.shim0mura.havings.model.entity.UserListEntity;
import work.t_s.shim0mura.havings.presenter.FormPresenter;
import work.t_s.shim0mura.havings.util.SpaceTokenizer;
import work.t_s.shim0mura.havings.util.TagCompletionView;
import work.t_s.shim0mura.havings.util.ViewUtil;

public class ItemFormActivity extends AppCompatActivity {

    private static final String SERIALIZED_ITEM = "SerializedItem";
    public static final String LIST_NAME_TAG_ID_KEY = "ListNameTagIdKey";
    private static final int IMAGE_CHOOSER_RESULTCODE = 100;
    public static final int LIST_NAME_CHOOSER_RESULTCODE = 200;
    private static final String TAG = "ItemFormActivity:";

    private FormPresenter formPresenter;
    private ItemEntity item;
    private Uri pictureUri;
    private List<TagEntity> tags = new ArrayList<>();

    @Bind(R.id.image_view) ImageView itemImage;
    @Bind(R.id.comp) TextView itemName;
    @Bind(R.id.searchView) TagCompletionView itemTag;
    @Bind(R.id.spinner) Spinner spinner;
    @Bind(R.id.list_name_prompt) TextView listNamePrompt;

    public static void startActivity(Context context, @Nullable ItemEntity i){
        Intent intent = new Intent(context, ItemFormActivity.class);
        if(i == null){
            i = new ItemEntity();
        }
        intent.putExtra(SERIALIZED_ITEM, i);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        item = (ItemEntity)extras.getSerializable(SERIALIZED_ITEM);

        setContentView(R.layout.activity_item_form);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        formPresenter = new FormPresenter(this);

        ButterKnife.bind(this);

        formPresenter.getUserListTree();

        getTagList();

        TextView camera = (TextView)findViewById(R.id.start_camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchChooser();
            }
        });

        ArrayAdapter<TagEntity> adapter = new FilteredArrayAdapter<TagEntity>(this, android.R.layout.simple_list_item_1, tags) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {

                    LayoutInflater l = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                    convertView = l.inflate(android.R.layout.simple_list_item_1, parent, false);
                }

                TagEntity p = getItem(position);

                ((TextView)convertView).setText(p.getName());

                return convertView;
            }

            @Override
            protected boolean keepObject(TagEntity tag, String mask) {
                mask = mask.toLowerCase();
                return tag.getName().toLowerCase().startsWith(mask) || tag.getYomiJp().startsWith(mask) || tag.getYomiRoma().toLowerCase().startsWith(mask);
            }
        };

        MultiAutoCompleteTextView textView = (MultiAutoCompleteTextView) findViewById(R.id.comp);
        textView.setAdapter(adapter);
        textView.setTokenizer(new SpaceTokenizer());
        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Timber.d("item clicked!! id: %s", id);
                TagEntity tag = new TagEntity();
                TextView selected = (TextView)view;
                tag.name = selected.getText().toString();
                itemTag.addObject(tag);
            }
        });

        ArrayAdapter<TagEntity> tagAdapter = new FilteredArrayAdapter<TagEntity>(this, android.R.layout.simple_list_item_1, tags) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {

                    LayoutInflater l = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                    convertView = l.inflate(android.R.layout.simple_list_item_1, parent, false);
                }

                TagEntity p = getItem(position);
                ((TextView)convertView).setText(p.getName());

                return convertView;
            }

            @Override
            protected boolean keepObject(TagEntity tag, String mask) {
                mask = mask.toLowerCase();
                return tag.getName().toLowerCase().startsWith(mask) || tag.getYomiJp().startsWith(mask) || tag.getYomiRoma().toLowerCase().startsWith(mask);
            }
        };

        itemTag.setAdapter(tagAdapter);
        char[] splitChar = {',', ';', ' '};
        itemTag.setSplitChar(splitChar);
        itemTag.performBestGuess(false);
    }

    private List<Tag> getTagList(){
        Realm realm = Realm.getInstance(this);
        RealmResults<Tag> result = realm.where(Tag.class).equalTo("isDeleted", false).findAll();

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

        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusHolder.get().register(this);
        Log.d(TAG, "regist action");
    }

    @Override
    protected void onPause() {
        BusHolder.get().unregister(this);
        Log.d(TAG, "unregist action");

        super.onPause();
    }

    @Subscribe
    public void setList(UserListEntity[] list){
        ArrayAdapter<UserListEntity> spinnerAdapter = new ArrayAdapter<UserListEntity>(this, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        int defaultPos = 0;
        int listSize = list.length;
        int compareTo = (item.isList) ? item.id : item.listId;

        for(int i = 0; i < listSize; i++){
            spinnerAdapter.add(list[i]);
            if (list[i].id == compareTo) {
                defaultPos = i;
            }
        }

        spinner.setAdapter(spinnerAdapter);
        if (defaultPos != 0) {
            spinner.setSelection(defaultPos);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView t = (TextView) view;
                String listName = t.getText().toString();
                t.setText(listName.trim());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @OnClick(R.id.specify_list_name_from_tags)
    public void navigateToListNameSelecter(){
        ListNameSelectActivity.startActivity(this);
    }

    @OnClick(R.id.specify_list_name_by_input)
    public void focusToListNameInput(){
        showListNameInputView();
        itemName.requestFocus();
    }

    @OnClick(R.id.post_item)
    public void postItem(){
        constructItem();
        //formPresenter.isValidItem();

        HashMap<String, RequestBody> fileParams = new HashMap<String, RequestBody>();

        // uriの存在チェック、複数画像のセット
        Uri u = (Uri)itemImage.getTag();
        File f = new File(u.getPath());
        if(f.exists()){
            Log.d("fileexist", f.getPath());
        }else{
            String fileName = System.currentTimeMillis() + "";
            f = new File(this.getCacheDir(), fileName);
            try {
                Log.d("filename", fileName);
                Log.d("create_file", String.valueOf(f.createNewFile()));
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        byte[] bitmapdata;
        try{
            InputStream is = getContentResolver().openInputStream(u);

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
        sb.append(ViewUtil.getMimeType(u, this));
        sb.append(";base64,");
        String base64Img = Base64.encodeToString(bitmapdata, Base64.NO_WRAP);
        sb.append(base64Img);

        Log.d(TAG, u.getPath());
        Log.d(TAG, ViewUtil.getMimeType(u, this));

        RequestBody req = RequestBody.create(MediaType.parse(ViewUtil.getMimeType(u, this)), f);
        fileParams.put("aaaa", req);
        ItemImageEntity itemImage = new ItemImageEntity();
        itemImage.imageData = sb.toString();
        item.imageDataForPost = new ArrayList<ItemImageEntity>();
        item.imageDataForPost.add(itemImage);

        HashMap<String, ItemEntity> hashItem = new HashMap<String, ItemEntity>();
        hashItem.put("item", item);
        formPresenter.postItem(hashItem, fileParams);
    }

    public void constructItem(){
        //複数の画像に対応する
        item.name = itemName.getText().toString();
        StringBuilder sb = new StringBuilder();

        //tagの存在チェック
        for (Object token: itemTag.getObjects()) {
            sb.append(token.toString());
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        item.tagList = sb.toString();

        UserListEntity selectedList = (UserListEntity)spinner.getSelectedItem();
        item.listId = selectedList.id;

        Timber.d("listid %s", String.valueOf(item.listId));

        Log.d(TAG, item.name);
        Log.d(TAG, item.tagList);
        Log.d(TAG, itemTag.toString());
        Log.d(TAG + "tagtpe", itemTag.getText().getClass().toString());
    }

    private void launchChooser() {
        // ギャラリーから選択
        Intent i;
        if (Build.VERSION.SDK_INT < 19) {
            i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("image/*");
        } else {
            i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
        }

        // カメラで撮影
        String filename = System.currentTimeMillis() + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        // http://taka-say.hateblo.jp/entry/2015/06/05/010000
        pictureUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent i2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i2.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);

        // ギャラリー選択のIntentでcreateChooser()
        Intent chooserIntent = Intent.createChooser(i, "画像を選択する");
        // EXTRA_INITIAL_INTENTS にカメラ撮影のIntentを追加
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{i2});

        startActivityForResult(chooserIntent, IMAGE_CHOOSER_RESULTCODE);
    }

    private void showListNameInputView(){
        listNamePrompt.setVisibility(View.GONE);
        itemName.setVisibility(View.VISIBLE);
    }

    private void addListName(int id){
        Timber.d("selected tag_id %s", id);
        Realm realm = Realm.getInstance(this);
        Tag result = realm.where(Tag.class).equalTo("id", id).findFirst();

        showListNameInputView();

        itemName.append(result.getName() + " ");
        TagEntity tagEntity = new TagEntity();
        tagEntity.name = result.getName();
        itemTag.addObject(tagEntity);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_CHOOSER_RESULTCODE) {

            if (resultCode != RESULT_OK) {
                if (pictureUri != null) {
                    Timber.d("image delete");
                    getContentResolver().delete(pictureUri, null, null);
                    pictureUri = null;
                }
                return;
            }

            // 画像を取得
            Uri result = (data == null) ? pictureUri : data.getData();

            ImageView iv = (ImageView) findViewById(R.id.image_view);
            iv.setTag(result);
            iv.setImageURI(result);

            pictureUri = null;

        }else if(requestCode == LIST_NAME_CHOOSER_RESULTCODE){
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();

                addListName(bundle.getInt(LIST_NAME_TAG_ID_KEY));
            }
        }
    }

}
