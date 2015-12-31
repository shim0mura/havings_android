package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import work.t_s.shim0mura.havings.presenter.FormPresenter;

public class ListNameSelectActivity extends AppCompatActivity {

    public static void startActivity(Context context){
        Intent intent = new Intent(context, ListNameSelectActivity.class);
        //intent.putExtra(SERIALIZED_ITEM, i);
        Activity a = (Activity)context;
        a.startActivityForResult(intent, ItemFormActivity.LIST_NAME_CHOOSER_RESULTCODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        PagerAdapter pagerAdapter = new FormPresenter.ListSelectPagerAdapter(this);

        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

    }

}
