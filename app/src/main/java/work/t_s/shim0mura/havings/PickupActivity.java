package work.t_s.shim0mura.havings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.entity.PickupEntity;
import work.t_s.shim0mura.havings.model.entity.PopularTagEntity;
import work.t_s.shim0mura.havings.presenter.HomePresenter;
import work.t_s.shim0mura.havings.presenter.SearchPresenter;
import work.t_s.shim0mura.havings.view.ItemListAdapter;

public class PickupActivity extends DrawerActivity {

    public final static int PICKUP_TYPE_TAG = 1;
    public final static int PICKUP_TYPE_LIST = 2;
    public final static int PICKUP_TYPE_NEW = 3;

    private final static String SERIALIZED_PICKUP = "SerializedPickup";
    private final static String SERIALIZED_PICKUP_TYPE = "SerializedPickupType";

    private PickupEntity pickupEntity;
    private int pickupType;
    private View header;
    private View loader;
    private HomePresenter homePresenter;
    private ItemListAdapter adapter;

    @Bind(R.id.pickup_list) ListView pickupList;
    @Bind(R.id.scroll_wrapper) ScrollView scrollWrapper;
    @Bind(R.id.header_wrapper) LinearLayout headerWrapper;
    @Bind(R.id.pickup_wrapper) LinearLayout pickupWrapper;

    public static void startActivity(Context context, PickupEntity pickupEntity, int pickupType){
        Intent intent = new Intent(context, new Object() {}.getClass().getEnclosingClass());
        intent.putExtra(SERIALIZED_PICKUP, pickupEntity);
        intent.putExtra(SERIALIZED_PICKUP_TYPE, pickupType);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        pickupEntity = (PickupEntity)extras.getSerializable(SERIALIZED_PICKUP);
        pickupType = extras.getInt(SERIALIZED_PICKUP_TYPE);

        setContentView(R.layout.activity_pickup);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        header = View.inflate(this, R.layout.partial_basic_header, null);
        TextView pageBreadcrumb = (TextView)header.findViewById(R.id.page_breadcrumb);
        TextView pageTitle = (TextView)header.findViewById(R.id.page_title);
        ImageView pageIcon = (ImageView)header.findViewById(R.id.page_icon);

        switch(pickupType) {
            case PICKUP_TYPE_TAG:
                //pageBreadcrumb.setText(this.getString(R.string.popular_tags));
                pageTitle.setText(this.getString(R.string.popular_tags));
                setTitle(this.getString(R.string.popular_tags));
                pageIcon.setImageResource(R.drawable.ic_label_outline_yellow_600_24dp);
                pickupList.setVisibility(View.GONE);

                headerWrapper.addView(header);
                HomePresenter.setPopularTag(this, pickupWrapper, pickupEntity.popularTag, true);

                break;
            case PICKUP_TYPE_LIST:
                //pageBreadcrumb.setText(this.getString(R.string.popular_list));
                pageTitle.setText(this.getString(R.string.popular_list));
                setTitle(this.getString(R.string.popular_list));
                pageIcon.setImageResource(R.drawable.ic_whatshot_yellow_600_24dp);
                pickupList.setVisibility(View.GONE);

                headerWrapper.addView(header);
                HomePresenter.setPopularList(this, pickupWrapper, pickupEntity.popularList, true);

                break;
            case PICKUP_TYPE_NEW:
                pickupList.addHeaderView(header);
                scrollWrapper.setVisibility(View.GONE);
                break;
        }

        onCreateDrawer(false);

    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
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

}
