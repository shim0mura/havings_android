package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.tokenautocomplete.FilteredArrayAdapter;
import com.wefika.flowlayout.FlowLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.Item;
import work.t_s.shim0mura.havings.model.entity.TagEntity;
import work.t_s.shim0mura.havings.model.event.SetErrorEvent;
import work.t_s.shim0mura.havings.model.realm.Tag;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.ItemImageEntity;
import work.t_s.shim0mura.havings.model.entity.UserListEntity;
import work.t_s.shim0mura.havings.presenter.FormPresenter;
import work.t_s.shim0mura.havings.util.SpaceTokenizer;
import work.t_s.shim0mura.havings.util.TagCompletionView;
import work.t_s.shim0mura.havings.util.ViewUtil;

public class ItemFormActivity extends ItemFormBaseActivity {

    private ItemEntity relatedItem;

    public static void startActivity(Context context, ItemEntity i, boolean asList){
        Intent intent = new Intent(context, new Object(){ }.getClass().getEnclosingClass());
        intent.putExtra(SERIALIZED_ITEM, i);
        intent.putExtra(AS_LIST, asList);
        Activity a = (Activity)context;
        a.startActivityForResult(intent, ItemActivity.ITEM_CREATED_RESULTCODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_form);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        relatedItem = item;
        item = new ItemEntity();
        item.isList = asList;
        item.privateType = relatedItem.privateType;
        item.listId = relatedItem.isList ? relatedItem.id : relatedItem.listId;
        if(!asList){
            item.count = 1;
        }

        ButterKnife.bind(this);

        constructForm();

        formPresenter.getUserListTree();
    }

    @OnClick(R.id.post_item)
    public void postItem(){
        clearWarning();
        constructItem();

        List<ItemImageEntity> addedImages = formPresenter.constructAddingImage(getAddedImageViews());
        item.imageDataForPost = new ArrayList<ItemImageEntity>();
        item.imageDataForPost.addAll(addedImages);

        formPresenter.attemptToCreateItem(item);
        item.imageDataForPost = new ArrayList<ItemImageEntity>();
    }

    @Subscribe
    @Override
    public void successToPost(ItemEntity itemEntity){
        Intent data = getIntent();
        Bundle extras = new Bundle();
        extras.putSerializable(ItemActivity.CREATED_ITEM, itemEntity);
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
