package work.t_s.shim0mura.havings;

/**
 * Created by shim0mura on 2016/01/04.
 */

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.tokenautocomplete.FilteredArrayAdapter;
import com.wefika.flowlayout.FlowLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.DefaultTag;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.TagEntity;
import work.t_s.shim0mura.havings.model.entity.UserListEntity;
import work.t_s.shim0mura.havings.model.event.AlertEvent;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.presenter.FormPresenter;
import work.t_s.shim0mura.havings.util.PermissionUtil;
import work.t_s.shim0mura.havings.util.SpaceTokenizer;
import work.t_s.shim0mura.havings.util.TagCompletionView;
import work.t_s.shim0mura.havings.view.FellowSelectExpandableListAdapter;

abstract public class ItemFormBaseActivity extends AppCompatActivity {

    protected static final String SERIALIZED_ITEM = "SerializedItem";
    protected static final String AS_LIST = "AsList";
    public static final String LIST_NAME_TAG_ID_KEY = "ListNameTagIdKey";
    public static final int LIST_NAME_CHOOSER_RESULTCODE = 100;
    protected static final int IMAGE_CHOOSER_FROM_CAMERA_RESULTCODE = 200;
    protected static final int IMAGE_CHOOSER_FROM_GALLERY_RESULTCODE = 300;

    protected FormPresenter formPresenter;
    protected ItemEntity item;
    protected Uri pictureUri;
    protected List<TagEntity> tags = new ArrayList<>();
    protected Boolean asList;
    protected String itemTypeString;
    protected ProgressDialog progressDialog;

    // data-bindingで@Nullableを消したい...
    @Nullable @Bind(R.id.new_image_container) FlowLayout newImageContainer;
    //@Nullable @Bind(R.id.exist_image_container) FlowLayout existImageContainer;
    @Nullable @Bind(R.id.comp) TextView itemName;
    @Nullable @Bind(R.id.item_type_icon) ImageView itemTypeIcon;
    @Nullable @Bind(R.id.searchView) TagCompletionView itemTag;
    @Nullable @Bind(R.id.description) TextView description;
    @Nullable @Bind(R.id.list_spinner) Spinner listSpinner;
    //@Nullable @Bind(R.id.private_type) Spinner privateTypeSpinner;
    //@Nullable @Bind(R.id.specify_list_name_from_tags) LinearLayout inputListNameByTag;
    //@Nullable @Bind(R.id.specify_list_name_by_input) LinearLayout inputListNameByInput;
    @Nullable @Bind(R.id.private_type_main_text) TextView privateTypeMainText;
    @Nullable @Bind(R.id.private_type_detail_text) TextView privateTypeDetailText;
    @Nullable @Bind(R.id.private_type_switch) CompoundButton privateTypeSwitch;

    @Nullable @Bind(R.id.validate_image) LinearLayout imageWarning;
    @Nullable @Bind(R.id.validate_image_below) LinearLayout imageWarningBelow;
    @Nullable @Bind(R.id.item_count_wrapper) LinearLayout itemCountWrapper;
    @Nullable @Bind(R.id.item_count_changer) LinearLayout itemCountChanger;
    @Nullable @Bind(R.id.item_count) TextView itemCount;

    @Nullable @Bind(R.id.add_image_prompt) TextView addImagePrompt;
    @Nullable @Bind(R.id.list_name_prompt) TextView listNamePrompt;

    @Nullable @Bind(R.id.garbage_reason) TextView garbageReason;
    @Nullable @Bind(R.id.fellow_ids) ExpandableListView fellowIds;

    @Nullable @Bind(R.id.garbage_reason_text) EditText garbageReasonEdit;
    @Nullable @Bind(R.id.as_garbage_wrapper) LinearLayout asGarbageWrapper;
    @Nullable @Bind(R.id.as_garbage_switch) Switch asGarbageSwitch;
    @Nullable @Bind(R.id.garbage_reason_wrapper) LinearLayout garbageReasonWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        asList = extras.getBoolean(AS_LIST);
        item = (ItemEntity)extras.getSerializable(SERIALIZED_ITEM);
        formPresenter = new FormPresenter(this);


    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    protected void constructForm(){
        //setPrivateTypeSpinner();

        DefaultTag defaultTag = DefaultTag.getSingleton(this);
        //tags.addAll(formPresenter.getTagEntities());
        tags.addAll(defaultTag.getTagEntities());

        setNameAdapter();
        setTagAdapter();

        itemTypeString = (item.isList) ? getString(R.string.list) : getString(R.string.item);

        privateTypeMainText.setText(getString(R.string.prompt_private_type_main, itemTypeString));
        privateTypeDetailText.setText(getString(R.string.prompt_private_type_detail_public, itemTypeString));
        privateTypeSwitch.setChecked(true);

        if(item.isList){
            itemCountWrapper.setVisibility(View.GONE);
        }else{
            //showItemNameInputView();
            itemTypeIcon.setImageResource(R.drawable.item_icon_for_tab);
            listNamePrompt.setText(getText(R.string.prompt_item_name));
            itemName.setHint(R.string.hint_to_item_name);
            if(addImagePrompt != null) {
                addImagePrompt.setText(getText(R.string.prompt_optional_image));
            }
            description.setHint(R.string.hint_to_description_of_item);
        }

        privateTypeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                UserListEntity selectedList = (UserListEntity) listSpinner.getSelectedItem();
                Boolean parentPrivate = (selectedList != null && selectedList.privateType > 0) ? true : false;
                changePrivateType(!isChecked, parentPrivate);
            }
        });

        garbageReasonWrapper.setVisibility(View.GONE);
        asGarbageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    garbageReasonWrapper.setVisibility(View.VISIBLE);
                }else{
                    garbageReasonWrapper.setVisibility(View.GONE);
                }
            }
        });

    }

    protected void setDefaultValue(){
        itemName.setText(item.name);
        description.setText(item.description);

        for(String tag : item.tags){
            Timber.d(tag);
            TagEntity t = new TagEntity();
            t.name = tag;
            itemTag.addObject(t);
        }

        if(!item.isList){
            itemCount.setText(String.valueOf(item.count));
        }

        if(item.privateType > 0){
            privateTypeSwitch.setChecked(false);
            changePrivateType((item.privateType > 0), false);
        }

        asGarbageSwitch.setChecked(item.isGarbage);
        EditText t = (EditText)findViewById(R.id.garbage_reason_text);
        t.setText(item.garbageReason);
    }

    protected void changePrivateType(Boolean isPrivate, Boolean isParentPrivate){
        if(isParentPrivate) {
            privateTypeDetailText.setText(getString(R.string.prompt_private_type_detail_private_by_parent));
        }else if(isPrivate) {
            privateTypeDetailText.setText(getString(R.string.prompt_private_type_detail_private, itemTypeString));
        }else{
            privateTypeDetailText.setText(getString(R.string.prompt_private_type_detail_public, itemTypeString));

        }

    }

    protected void showItemCountChanger(){
        final Dialog d = new Dialog(this);
        d.setTitle(R.string.prompt_item_count);
        d.setContentView(R.layout.dialog_item_count);
        Button b1 = (Button) d.findViewById(R.id.button1);
        Button b2 = (Button) d.findViewById(R.id.button2);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        np.setMaxValue(900);
        np.setMinValue(1);
        np.setValue(item.count);
        np.setWrapSelectorWheel(false);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemCount.setText(String.valueOf(np.getValue()));
                item.count = np.getValue();
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }

    /*
    protected void setPrivateTypeSpinner(){
        ArrayAdapter<Item.PrivateType> privateTypeSpinnerAdapter = new ArrayAdapter<Item.PrivateType>(this, android.R.layout.simple_spinner_item);
        privateTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for(Item.PrivateType type : Item.getPrivateTypeObj(this)) {
            privateTypeSpinnerAdapter.add(type);
        }

        privateTypeSpinner.setAdapter(privateTypeSpinnerAdapter);
        if (item.privateType != 0) {
            privateTypeSpinner.setSelection(item.privateType);
        }
    }
    */

    protected void setNameAdapter(){
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
                TagEntity tag = new TagEntity();
                TextView selected = (TextView) view;
                tag.name = selected.getText().toString();
                itemTag.addObject(tag);
            }
        });
    }

    protected void setTagAdapter(){
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

    protected void setFellowAdapter(String explanation, String subExplanation, String buttonText){

        final FellowSelectExpandableListAdapter adapter = new FellowSelectExpandableListAdapter(this, item.owningItems, explanation, subExplanation);
        final Button postItem = (Button)findViewById(R.id.post_item);

        if(fellowIds != null && item.isList){
            final View footer = getLayoutInflater().inflate(R.layout.partial_fellow_item_select_footer, null);
            Button b = (Button)footer.findViewById(R.id.sub_post_item);
            b.setText(buttonText);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    postItem();
                }
            });

            LinearLayout allCheckButtons = (LinearLayout)footer.findViewById(R.id.all_check_buttons);

            fellowIds.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                    Timber.d("child clicked %s", childPosition);
                    CheckBox checkBox = (CheckBox) v.findViewById(R.id.fellow_item);
                    if(checkBox.isChecked()){
                        adapter.deselectItem(groupPosition, childPosition);
                    }else{
                        adapter.selectItem(groupPosition, childPosition);
                    }
                    checkBox.setChecked(!checkBox.isChecked());

                    return false;
                }
            });
            allCheckButtons.findViewById(R.id.check_all).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.selectAll();
                }
            });
            allCheckButtons.findViewById(R.id.deselect_all).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.deselectAll();
                }
            });
            fellowIds.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
                @Override
                public void onGroupCollapse(int groupPosition) {
                    adapter.hideSubText();
                    fellowIds.removeFooterView(footer);
                    postItem.setVisibility(View.VISIBLE);
                }
            });
            fellowIds.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
                @Override
                public void onGroupExpand(int groupPosition) {
                    adapter.showSubText();
                    fellowIds.addFooterView(footer);
                    fellowIds.requestFocus();
                    InputMethodManager inputMethodMgr = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodMgr.hideSoftInputFromWindow(fellowIds.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    postItem.setVisibility(View.GONE);
                }
            });
            fellowIds.setAdapter(adapter);
        }

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

    abstract public void subscribeSetListUserOwning(UserListEntity[] list);

    public void setListUserOwning(UserListEntity[] list){

        int defaultPos = 0;
        int listSize = list.length;
        int compareTo = item.listId;
        int tmpSelfPosition = 0;

        for(int i = 0; i < listSize; i++){
            if (list[i].id == compareTo) {
                defaultPos = i;
            }
            if (item.isList != null && item.isList && item.id == list[i].id){
                tmpSelfPosition = i;
            }
        }

        final int selfPosition = tmpSelfPosition;
        final UserListEntity selfList = list[selfPosition];

        ArrayAdapter<UserListEntity> spinnerAdapter = new ArrayAdapter<UserListEntity>(this, android.R.layout.simple_spinner_item){
            @Override
            public boolean isEnabled(int position) {
                UserListEntity u = getItem(position);
                if(selfPosition != 0 && u.id == selfList.id){
                    Timber.d("selected self");
                    return false;
                }else{
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                UserListEntity u = getItem(position);

                View v = super.getDropDownView(position, convertView, parent);
                TextView t = (TextView)v;
                if(selfPosition != 0 && u.id == selfList.id){
                    t.setTextColor(ContextCompat.getColor(getContext(), R.color.unable));
                }else{
                    t.setTextColor(ContextCompat.getColor(getContext(), R.color.primaryText));
                }
                return t;
            }

        };
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        for(int i = 0; i < listSize; i++) {
            spinnerAdapter.add(list[i]);
        }

        listSpinner.setAdapter(spinnerAdapter);
        if (defaultPos != 0) {
            listSpinner.setSelection(defaultPos);
            UserListEntity selectedList = (UserListEntity) listSpinner.getSelectedItem();
            Boolean parentPrivate = (selectedList != null && selectedList.privateType > 0) ? true : false;
            if(parentPrivate){
                privateTypeSwitch.setChecked(false);
                privateTypeSwitch.setEnabled(false);
                changePrivateType(false, true);
            }
        }

        listSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView t = (TextView) view;
                String listName = t.getText().toString();
                t.setText(listName.trim());

                UserListEntity selectedList = (UserListEntity) listSpinner.getSelectedItem();
                Boolean parentPrivate = (selectedList != null && selectedList.privateType > 0) ? true : false;
                if(parentPrivate){
                    privateTypeSwitch.setChecked(false);
                    privateTypeSwitch.setEnabled(false);
                    changePrivateType(false, true);
                }else{
                    privateTypeSwitch.setEnabled(true);
                    changePrivateType(!privateTypeSwitch.isChecked(), false);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Subscribe
    abstract public void subscribeAlert(AlertEvent event);

    public void showAlert(AlertEvent event){
        new AlertDialog.Builder(this)
                .setTitle(event.title)
                .setMessage(event.message)
                .setPositiveButton("OK", null)
                .show();
    }

    @Subscribe
    abstract public void subscribeSetValidateError(SetErrorEvent event);

    public void setValidateError(SetErrorEvent event){
        switch(event.resourceId){
            case R.id.comp:
                //showListNameInputView();
                if(item.isList) {
                    itemName.setError(getResources().getString(R.string.error_list_name_required));
                }else{
                    itemName.setError(getResources().getString(R.string.error_item_name_required));
                }
                itemName.requestFocus();
                break;
            case R.id.validate_image:
                imageWarning.setVisibility(View.VISIBLE);
                imageWarningBelow.setVisibility(View.VISIBLE);
                break;
        }
    }

    abstract public void successToPost(ItemEntity itemEntity);

    /*
    @Nullable @OnClick(R.id.specify_list_name_from_tags)
    public void navigateToListNameSelecter(){
        //ListNameSelectActivity.startActivity(this);
    }

    @Nullable @OnClick(R.id.specify_list_name_by_input)
    public void focusToListNameInput(){
        showListNameInputView();
        itemName.requestFocus();
        MultiAutoCompleteTextView nameEditor = (MultiAutoCompleteTextView) findViewById(R.id.comp);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(nameEditor, InputMethodManager.SHOW_IMPLICIT);
    }
    */

    @Nullable @OnClick(R.id.add_image_from_camera)
    public void startImageChooserFromCamera(){
        if (!PermissionUtil.hasSelfPermission(this, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionUtil.CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            launchImageChooserFromCamera();
        }
    }

    @Nullable @OnClick(R.id.add_image_from_gallery)
    public void startImageChooserFromGallery(){
        if (!PermissionUtil.hasSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionUtil.WRITE_EXT_STORAGE_PERMISSION_REQUEST_CODE);
        } else {
            launchImageChooserFromGallery();
        }
    }

    @Nullable @OnClick(R.id.item_count_changer)
    public void itemCountChange(){
        showItemCountChanger();
    }

    @Nullable @OnClick(R.id.count_help)
    public void showCountHelp(){
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.prompt_item_count))
                .setMessage(getString(R.string.prompt_count_help))
                .setPositiveButton("OK", null)
                .show();
    }

    @Nullable @OnClick(R.id.tag_help)
    public void showTagHelp(View view){
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.prompt_tag))
                .setMessage(getString(R.string.prompt_tag_help))
                .setPositiveButton("OK", null)
                .show();
    }

    @Nullable @OnClick(R.id.belong_list_help)
    public void showBelongListHelp(View view){
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.prompt_add_to_list))
                .setMessage(getString(R.string.prompt_belong_list_help))
                .setPositiveButton("OK", null)
                .show();
    }

    @Nullable @OnClick(R.id.garbage_help)
    public void showGarbageHelp(View view){
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.prompt_as_garbage))
                .setMessage(getString(R.string.prompt_garbage_help))
                .setPositiveButton("OK", null)
                .show();
    }

    @OnClick(R.id.post_item)
    abstract public void postItem();

    public void constructItem(){
        item.name = itemName.getText().toString();
        StringBuilder sb = new StringBuilder();

        ArrayList<String> tags = new ArrayList<>();
        //tagの存在チェック
        for (Object token: itemTag.getObjects()) {
            sb.append(token.toString());
            sb.append(",");
            tags.add(token.toString());
        }
        if(sb.length() > 1){
            sb.deleteCharAt(sb.length() - 1);
        }
        item.tags = tags;
        item.tagList = sb.toString();

        item.description = description.getText().toString();

        UserListEntity selectedList = (UserListEntity) listSpinner.getSelectedItem();
        item.listId = selectedList.id;

        //Item.PrivateType selectedPrivateType = (Item.PrivateType)privateTypeSpinner.getSelectedItem();
        int privateType = privateTypeSwitch.isChecked() ? 0 : 3;
        item.privateType = privateType;

        item.isGarbage = asGarbageSwitch.isChecked();
        item.garbageReason = garbageReasonEdit.getText().toString();
    }

    protected List<ImageView> getAddedImageViews(){
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

    /*
    protected Map<Integer, Map<String, String>> getChangedImageMetadata(){
        Map<Integer, Map<String, String>> changedImageMetadata = new HashMap<>();

        int childs = existImageContainer.getChildCount();
        for(int i = 0; i < childs; i++){
            View parent = existImageContainer.getChildAt(i);
            ImageView imageView = (ImageView)parent.findViewById(R.id.added_image);

            if(imageView == null) {
                continue;
            }

            Object isAddedImage = imageView.getTag(R.id.IS_ADDED_IMAGE_FROM_FORM);
            Object isDataChanged = imageView.getTag(R.id.IMAGE_METADATA_CHANGED);

            // 削除するつもりの画像はわざわざ含める必要ないのでパスする
            Object isDeletingImage = imageView.getTag(R.id.IS_DELETING_IMAGE);
            boolean isGoingToDelete = (isDeletingImage != null && (boolean)isDeletingImage);

            if(isAddedImage != null && !(boolean)isAddedImage && isDataChanged != null && (boolean)isDataChanged && !isGoingToDelete){
                int id = (int)imageView.getTag(R.id.EDIT_IMAGE_ID);
                Date d = (Date)imageView.getTag(R.id.IMAGE_DATE);
                String timestamp = String.valueOf(d.getTime()/1000);
                Object memoObj = imageView.getTag(R.id.IMAGE_MEMO);
                String memo = ((memoObj != null) ? memoObj.toString() : "");
                Map<String, String> hash = new HashMap<>();
                Timber.d("changing image id %s", id);
                Timber.d("changing image timestamp %s", timestamp);
                Timber.d("changing image memo %s", memo);

                hash.put("timestamp", timestamp);
                hash.put("memo", memo);
                changedImageMetadata.put(id, hash);
            }
        }

        return changedImageMetadata;
    }

    protected List<Integer> getDeletingImage(){
        List<Integer> imageIds = new ArrayList<Integer>();

        int childs = existImageContainer.getChildCount();
        for(int i = 0; i < childs; i++){
            View parent = existImageContainer.getChildAt(i);
            ImageView imageView = (ImageView)parent.findViewById(R.id.added_image);

            if(imageView == null) {
                continue;
            }

            Object isAddedImage = imageView.getTag(R.id.IS_ADDED_IMAGE_FROM_FORM);
            Object isDeletingImage = imageView.getTag(R.id.IS_DELETING_IMAGE);

            if(isAddedImage != null && !(boolean)isAddedImage && isDeletingImage != null && (boolean)isDeletingImage){
                int id = (int)imageView.getTag(R.id.EDIT_IMAGE_ID);
                imageIds.add(id);
            }
        }

        return imageIds;
    }
    */

    protected void clearWarning(){
        if(imageWarning != null) {
            imageWarning.setVisibility(View.GONE);
        }
        if(imageWarningBelow != null){
            imageWarningBelow.setVisibility(View.GONE);
        }
    }

    protected void launchImageChooserFromCamera(){
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

    protected void launchImageChooserFromGallery(){
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

    /*
    protected void showListNameInputView(){
        inputListNameByInput.setVisibility(View.GONE);
        itemName.setVisibility(View.VISIBLE);
    }
    */

    /*
    protected void showItemNameInputView(){
        showListNameInputView();
        inputListNameByTag.setVisibility(View.GONE);
    }
    */

    /*
    protected void addListName(int id){
        Timber.d("selected tag_id %s", id);
        Realm realm = Realm.getInstance(this);
        TagEntity result = realm.where(TagEntity.class).equalTo("id", id).findFirst();

        showListNameInputView();

        itemName.append(result.getName() + " ");
        TagEntity tagEntity = new TagEntity();
        tagEntity.name = result.getName();
        itemTag.addObject(tagEntity);
    }*/

    protected void addNewPicture(Uri imageUri){
        View pictureView = getLayoutInflater().inflate(R.layout.partial_added_image_in_form, newImageContainer, false);
        ImageView image = (ImageView) pictureView.findViewById(R.id.added_image);
        image.setImageURI(imageUri);
        image.setTag(R.id.IS_ADDED_IMAGE_FROM_FORM, true);
        image.setTag(R.id.ADDED_IMAGE_URI, imageUri);

        TextView imageDate = (TextView)pictureView.findViewById(R.id.image_date);
        final Calendar currentCalendar = new GregorianCalendar();
        formPresenter.setItemImageDateListener(image, imageDate, currentCalendar);

        final TextView imageMemo = (TextView)pictureView.findViewById(R.id.image_memo);
        formPresenter.setItemImageMemoListener(image, imageMemo);

        ImageView delete = (ImageView)pictureView.findViewById(R.id.delete_image);
        formPresenter.setItemImageDirectDeleteListener(newImageContainer, pictureView, delete);

        int index = newImageContainer.getChildCount();
        newImageContainer.addView(pictureView, index);
    }

    /*
    protected void addExistPicture(){
        if(item.itemImages.images.size() < 1){
            return;
        }

        existImageContainer.removeViewAt(0);

        for(ItemImageEntity imageEntity : item.itemImages.images){
            View pictureView = getLayoutInflater().inflate(R.layout.partial_added_image_in_form, existImageContainer, false);
            ImageView image = (ImageView) pictureView.findViewById(R.id.added_image);
            String imageUrl = ApiService.BASE_URL + imageEntity.url;
            Glide.with(this).load(imageUrl).into(image);
            image.setTag(R.id.IS_ADDED_IMAGE_FROM_FORM, false);
            image.setTag(R.id.IS_DELETING_IMAGE, false);
            image.setTag(R.id.IMAGE_METADATA_CHANGED, false);
            image.setTag(R.id.EDIT_IMAGE_ID, imageEntity.id);

            TextView imageDate = (TextView)pictureView.findViewById(R.id.image_date);
            final Calendar currentCalendar = new GregorianCalendar();
            currentCalendar.setTime(imageEntity.addedDate);
            formPresenter.setItemImageDateListener(image, imageDate, currentCalendar);

            final TextView imageMemo = (TextView)pictureView.findViewById(R.id.image_memo);
            String memo = ((imageEntity.memo == null || imageEntity.memo.isEmpty()) ? getString(R.string.prompt_image_memo) : imageEntity.memo);
            imageMemo.setText(memo);
            formPresenter.setItemImageMemoListener(image, imageMemo);

            ImageView delete = (ImageView)pictureView.findViewById(R.id.delete_image);
            formPresenter.setItemImageDeleteListener(image, pictureView, delete);

            existImageContainer.addView(pictureView);
        }
    }
    */

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

                addNewPicture(resultFromCamera);

                pictureUri = null;
                break;
            case IMAGE_CHOOSER_FROM_GALLERY_RESULTCODE:
                if (resultCode != RESULT_OK) {
                    return;
                }
                Uri resultFromGallery = (data == null) ? pictureUri : data.getData();

                addNewPicture(resultFromGallery);

                pictureUri = null;
                break;
            case LIST_NAME_CHOOSER_RESULTCODE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();

                    //addListName(bundle.getInt(LIST_NAME_TAG_ID_KEY));
                }
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
