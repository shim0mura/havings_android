package work.t_s.shim0mura.havings;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.UserEntity;
import work.t_s.shim0mura.havings.presenter.StickyScrollPresenter;
import work.t_s.shim0mura.havings.presenter.UserFavoritesPresenter;
import work.t_s.shim0mura.havings.presenter.UserPresenter;

public class UserFavoritesActivity extends AppCompatActivity {

    private static final String SERIALIZED_USER = "SerializedUser";

    @Bind(R.id.tabs) TabLayout tabLayout;
    @Bind(R.id.pager) ViewPager viewPager;
    @Bind(R.id.page_breadcrumb) TextView pageBreadcrumb;

    private StickyScrollPresenter stickyScrollPresenter;
    private UserFavoritesPresenter userFavoritesPresenter;
    private ItemEntity item;
    private UserEntity user;
    private UserFavoritesPresenter.UserFavoritesPagerAdapter pagerAdapter;

    public static void startActivity(Context context, UserEntity user){
        Intent intent = new Intent(context, new Object() {}.getClass().getEnclosingClass());
        intent.putExtra(SERIALIZED_USER, user);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        user = (UserEntity)extras.getSerializable(SERIALIZED_USER);
        item = new ItemEntity();

        setContentView(R.layout.activity_user_favorites);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);
        stickyScrollPresenter = new StickyScrollPresenter(this, StickyScrollPresenter.SCROLL_TYPE_USER);
        userFavoritesPresenter = new UserFavoritesPresenter(this);

        pageBreadcrumb.setText(user.name + "\n> " + this.getString(R.string.prompt_favorite_list));

        pagerAdapter = new UserFavoritesPresenter.UserFavoritesPagerAdapter(this, stickyScrollPresenter, userFavoritesPresenter, user, item);
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            switch(i){
                case 0:
                    tab.setCustomView(userFavoritesPresenter.getTabView(i, user.favoritesCount));
                    break;
                case 1:
                    tab.setCustomView(userFavoritesPresenter.getTabView(i, user.imageFavoritesCount));
                    break;
            }
        }

        pagerAdapter.initialize();

        final View des = findViewById(R.id.desc);

        ViewTreeObserver vto = des.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= 16) {
                    des.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    des.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                Timber.d("desc height aftrer vto %s", des.getHeight());

                stickyScrollPresenter.initializeUser();

            }
        });

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
