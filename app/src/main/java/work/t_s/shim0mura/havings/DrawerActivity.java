package work.t_s.shim0mura.havings;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;
import work.t_s.shim0mura.havings.model.ApiKey;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.ApiServiceManager;

public class DrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected void onCreateDrawer(boolean showIcon) {

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        ActionBarDrawerToggle toggle;
        if(showIcon){
            toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.prompt_home, R.string.prompt_close);
        }else{
            toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.prompt_home, R.string.prompt_close);
            toggle.setDrawerIndicatorEnabled(false);
        }
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);

        setUserInfo(navigationView);
        navigationView.setNavigationItemSelectedListener(this);
    }

    protected void setUserInfo(NavigationView navigationView){
        TextView userName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.drawer_user_name);
        TextView itemCount = (TextView) navigationView.getHeaderView(0).findViewById(R.id.drawer_item_count);
        CircleImageView userThumbnail = (CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.drawer_user_thumbnail);

        ApiKey apiKey = ApiKey.getSingleton(this);
        String name = apiKey.getUserName();
        String thumbnail = apiKey.getUserThumbnail();
        int count = apiKey.getItemCount();

        if(name != null && userName != null){
            userName.setText(name);
            itemCount.setText(getString(R.string.prompt_item_having, count));
        }

        if(thumbnail != null && userThumbnail != null){
            userThumbnail.setVisibility(View.VISIBLE);
            String thumbnailUrl = ApiServiceManager.getSingleton(this).getApiUrl() + thumbnail;
            Glide.with(this).load(thumbnailUrl).into(userThumbnail);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.menu_home:
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                this.startActivity(intent);
                break;
            case R.id.menu_user:
                int userId = ApiKey.getSingleton(this).getUserId();
                UserActivity.startActivity(this, userId);
                break;
            case R.id.menu_setting:
                Intent settingIntent = new Intent(this, SettingActivity.class);
                this.startActivity(settingIntent);
            case R.id.menu_logout:
                ApiServiceManager asm = ApiServiceManager.getSingleton(this);
                asm.clearApiKey();
                Intent registerIntent = new Intent(this, RegisterActivity.class);
                startActivity(registerIntent);
                this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }
}
