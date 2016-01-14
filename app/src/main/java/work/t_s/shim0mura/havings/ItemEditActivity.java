package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageEntity;
import work.t_s.shim0mura.havings.model.entity.UserListEntity;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;

public class ItemEditActivity extends ItemFormBaseActivity {

    public static void startActivity(Context context, ItemEntity i, boolean asList){
        Intent intent = new Intent(context, new Object(){ }.getClass().getEnclosingClass());
        intent.putExtra(SERIALIZED_ITEM, i);
        intent.putExtra(AS_LIST, asList);
        Activity a = (Activity)context;
        a.startActivityForResult(intent, ItemActivity.ITEM_UPDATED_RESULTCODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        constructForm();

        setDefaultValue();

        showListNameInputView();

        formPresenter.getUserListTree();
    }

    @OnClick(R.id.post_item)
    public void postItem(){
        clearWarning();
        constructItem();

        formPresenter.attemptToUpdateItem(item);
        item.imageDataForPost = new ArrayList<ItemImageEntity>();
    }

    @Subscribe
    @Override
    public void successToPost(ItemEntity itemEntity){
        Intent data = getIntent();
        Bundle extras = new Bundle();
        extras.putSerializable(ItemActivity.UPDATED_ITEM, item);
        data.putExtras(extras);
        setResult(Activity.RESULT_OK, data);

        finish();
    }


    @Subscribe
    @Override
    public void subscribeSetListUserOwning(UserListEntity[] list){
        setListUserOwning(list);
    };


    @Subscribe
    @Override
    public void subscribeSetValidateError(SetErrorEvent event) {
        setValidateError(event);
    }
}

/*
public class ItemEditActivity extends AppCompatActivity {

    private static final String SERIALIZED_ITEM = "SerializedItem";
    public static final String LIST_NAME_TAG_ID_KEY = "ListNameTagIdKey";
    public static final int LIST_NAME_CHOOSER_RESULTCODE = 100;
    private static final int IMAGE_CHOOSER_FROM_CAMERA_RESULTCODE = 200;
    private static final int IMAGE_CHOOSER_FROM_GALLERY_RESULTCODE = 300;
    private static final String TAG = "ItemFormActivity:";

    private FormPresenter formPresenter;
    private ItemEntity item;
    private Uri pictureUri;
    private List<TagEntity> tags = new ArrayList<>();
    private Calendar currentCalendar = new GregorianCalendar();

    @Nullable @Bind(R.id.new_image_container) FlowLayout imageContainer;
    @Bind(R.id.comp) TextView itemName;
    @Bind(R.id.searchView) TagCompletionView itemTag;
    @Bind(R.id.description) TextView description;
    @Bind(R.id.list_spinner) Spinner listSpinner;
    @Bind(R.id.private_type) Spinner privateTypeSpinner;
    @Bind(R.id.list_name_prompt) TextView listNamePrompt;
    @Bind(R.id.specify_list_name_by_input) LinearLayout inputListNameByInput;
    //@Bind(R.id.add_image_from_camera) FrameLayout imageAdderFromCamera;
    //@Bind(R.id.add_image_from_gallery) FrameLayout imageAdderFromGallery;
    @Nullable @Bind(R.id.validate_image) LinearLayout imageWarning;
    @Nullable @Bind(R.id.validate_image_below) LinearLayout imageWarningBelow;


    public static void startActivity(Context context, ItemEntity i){
        Intent intent = new Intent(context, ItemEditActivity.class);
        intent.putExtra(SERIALIZED_ITEM, i);
        Activity a = (Activity)context;
        a.startActivityForResult(intent, ItemActivity.ITEM_CREATED_RESULTCODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        item = (ItemEntity)extras.getSerializable(SERIALIZED_ITEM);

        setContentView(R.layout.activity_item_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        formPresenter = new FormPresenter(this);

        ButterKnife.bind(this);

        formPresenter.getUserListTree();

        setPrivateTypeSpinner();

        tags.addAll(formPresenter.getTagEntities());

        setNameAdapter();
        setTagAdapter();

        setDefault();

        showListNameInputView();


        //imageAdderFromCamera.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        launchImageChooserFromCamera();
        //    }
        //});

        //imageAdderFromGallery.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        launchImageChooserFromGallery();
        //    }
        //});


    }

    private void setDefault(){
        itemName.setText(item.name);
        description.setText(item.description);

        for(String tag : item.tags){
            Timber.d(tag);
            TagEntity t = new TagEntity();
            t.name = tag;
            itemTag.addObject(t);
        }
    }

    private void setPrivateTypeSpinner(){
        ArrayAdapter<Item.PrivateType> privateTypeSpinnerAdapter = new ArrayAdapter<Item.PrivateType>(this, android.R.layout.simple_spinner_item);
        privateTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for(Item.PrivateType type : Item.getPrivateTypeObj(this)) {
            privateTypeSpinnerAdapter.add(type);
        }

        privateTypeSpinner.setAdapter(privateTypeSpinnerAdapter);
        privateTypeSpinner.setSelection(item.privateType);
    }

    private void setNameAdapter(){
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
                TextView selected = (TextView) view;
                tag.name = selected.getText().toString();
                itemTag.addObject(tag);
            }
        });
    }

    private void setTagAdapter(){
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

        listSpinner.setAdapter(spinnerAdapter);
        if (defaultPos != 0) {
            listSpinner.setSelection(defaultPos);
        }

        listSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

    @Subscribe
    public void setValidateError(SetErrorEvent event){
        switch(event.resourceId){
            case R.id.comp:
                showListNameInputView();
                itemName.setError(getResources().getString(R.string.error_list_name_required));
                itemName.requestFocus();
                break;
            case R.id.validate_image:
                imageWarning.setVisibility(View.VISIBLE);
                imageWarningBelow.setVisibility(View.VISIBLE);
                break;
        }
    }

    @OnClick(R.id.test_button)
    public void testClick(){
        successToPost(item);
    }

    @Subscribe
    public void successToPost(ItemEntity itemEntity){

        //Intent data = new Intent();
        //Bundle bundle = new Bundle();
        //bundle.putInt(ItemActivity.LIST_NAME_TAG_ID_KEY, itemEntity);
        //data.putExtras(bundle);

        //setResult(Activity.RESULT_OK, data);

        finish();
    }

    @OnClick(R.id.specify_list_name_from_tags)
    public void navigateToListNameSelecter(){
        ListNameSelectActivity.startActivity(this);
    }

    @OnClick(R.id.specify_list_name_by_input)
    public void focusToListNameInput(){
        showListNameInputView();
        itemName.requestFocus();
        MultiAutoCompleteTextView nameEditor = (MultiAutoCompleteTextView) findViewById(R.id.comp);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(nameEditor, InputMethodManager.SHOW_IMPLICIT);
    }

    @OnClick(R.id.post_item)
    public void postItem(){
        clearWarning();
        constructItem();


        //List<ItemImageEntity> addedImages = formPresenter.constructAddingImage(getAddedImageViews());
        //item.imageDataForPost = new ArrayList<ItemImageEntity>();
        //item.imageDataForPost.addAll(addedImages);


        formPresenter.attemptToUpdateItem(item);
    }

    public void constructItem(){
        item.name = itemName.getText().toString();
        StringBuilder sb = new StringBuilder();

        //tagの存在チェック
        for (Object token: itemTag.getObjects()) {
            sb.append(token.toString());
            sb.append(",");
        }
        if(sb.length() > 1){
            sb.deleteCharAt(sb.length() - 1);
        }
        item.tagList = sb.toString();

        item.description = description.getText().toString();

        UserListEntity selectedList = (UserListEntity) listSpinner.getSelectedItem();
        item.listId = selectedList.id;

        Item.PrivateType selectedPrivateType = (Item.PrivateType)privateTypeSpinner.getSelectedItem();
        item.privateType = selectedPrivateType.getTypeId();
    }

    private void clearWarning(){
        if(imageWarning != null) {
            imageWarning.setVisibility(View.GONE);
        }
        if(imageWarningBelow != null){
            imageWarningBelow.setVisibility(View.GONE);
        }
    }


    private List<ImageView> getAddedImageViews(){
        List<ImageView> addedImageViews = new ArrayList<ImageView>();
        int childs = newImageContainer.getChildCount();
        for(int i = 0; i < childs; i++){
            View parent = newImageContainer.getChildAt(i);
            ImageView imageView = (ImageView)parent.findViewById(R.id.added_image);

            if(imageView == null) {
                continue;
            }

            Object isAddedImage = imageView.getTag(R.id.IS_ADDED_IMAGE_FROM_FORM);
            if(isAddedImage != null && (boolean)isAddedImage){
                addedImageViews.add(imageView);
            }
        }

        return addedImageViews;
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


    private void showListNameInputView(){
        inputListNameByInput.setVisibility(View.GONE);
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

    private void addPicture(Uri imageUri){
        final View pictureView = getLayoutInflater().inflate(R.layout.partial_added_image_in_form, imageContainer, false);
        final ImageView image = (ImageView) pictureView.findViewById(R.id.added_image);
        image.setImageURI(imageUri);
        image.setTag(R.id.IS_ADDED_IMAGE_FROM_FORM, true);
        image.setTag(R.id.ADDED_IMAGE_URI, imageUri);

        final Activity act = this;

        final TextView imageDate = (TextView)pictureView.findViewById(R.id.image_date);
        final Date currentDate = new Date(currentCalendar.getTimeInMillis());
        imageDate.setText(ViewUtil.dateToString(currentDate, true));
        image.setTag(R.id.IMAGE_DATE, currentDate);

        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener(){
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                GregorianCalendar d = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                Date date = new Date(d.getTimeInMillis());
                imageDate.setText(ViewUtil.dateToString(date, true));
                image.setTag(R.id.IMAGE_DATE, currentDate);
            }
        };

        imageDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePicker = new DatePickerDialog(act, dateSetListener, currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), currentCalendar.get(Calendar.DAY_OF_MONTH));
                datePicker.show();
            }
        });

        final TextView imageMemo = (TextView)pictureView.findViewById(R.id.image_memo);
        imageMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editor = new EditText(act);
                if(!imageMemo.getText().equals(act.getString(R.string.prompt_image_memo))){
                    editor.setText(imageMemo.getText());
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(act);
                builder.setTitle(R.string.prompt_image_memo)
                        .setView(editor);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        imageMemo.setText(editor.getText());
                        image.setTag(R.id.IMAGE_MEMO, editor.getText());
                    }
                });
                builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.create().show();
            }
        });

        ImageView delete = (ImageView)pictureView.findViewById(R.id.delete_image);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageContainer.removeView(pictureView);
            }
        });

        int index = imageContainer.getChildCount();
        imageContainer.addView(pictureView, index);
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

                addPicture(resultFromCamera);

                pictureUri = null;
                break;
            case IMAGE_CHOOSER_FROM_GALLERY_RESULTCODE:
                Uri resultFromGallery = (data == null) ? pictureUri : data.getData();

                addPicture(resultFromGallery);

                pictureUri = null;
                break;
            case LIST_NAME_CHOOSER_RESULTCODE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();

                    addListName(bundle.getInt(LIST_NAME_TAG_ID_KEY));
                }
                break;
        }

    }

}
*/

