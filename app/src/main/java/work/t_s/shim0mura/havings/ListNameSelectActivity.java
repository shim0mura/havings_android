package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import butterknife.ButterKnife;
import butterknife.OnClick;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.presenter.FormPresenter;

public class ListNameSelectActivity extends AppCompatActivity {

    private static final String SERIALIZED_ID = "SerializedListItemID";

    private int listId;

    public static void startActivity(Context context, int id){
        Intent intent = new Intent(context, ListNameSelectActivity.class);
        intent.putExtra(SERIALIZED_ID, id);
        Activity a = (Activity)context;
        //a.startActivityForResult(intent, ItemFormActivity.LIST_NAME_CHOOSER_RESULTCODE);
        a.startActivity(intent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        listId = (int) extras.getSerializable(SERIALIZED_ID);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        PagerAdapter pagerAdapter = new FormPresenter.ListSelectPagerAdapter(this, listId);

        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        setTitle(R.string.prompt_list_name_select);

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(FormPresenter.getCustomTabForListNameSelect(this, i));

        }
        ButterKnife.bind(this);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

}
