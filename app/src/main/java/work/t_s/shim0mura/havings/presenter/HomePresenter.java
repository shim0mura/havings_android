package work.t_s.shim0mura.havings.presenter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import timber.log.Timber;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.view.DashboardTabFragment;
import work.t_s.shim0mura.havings.view.SearchTabFragment;

/**
 * Created by shim0mura on 2016/03/29.
 */
public class HomePresenter {


    public static class HomeTabPagerAdapter extends FragmentPagerAdapter {

        private final String[] tabTitle = {"ホーム", "ソーシャル", "検索", "設定"};
        private Context context;
        private LayoutInflater layoutInflater;

        public HomeTabPagerAdapter(android.support.v4.app.FragmentManager fm, Context c){
            super(fm);
            context = c;
            layoutInflater = LayoutInflater.from(c);
        }

        public String getTabTitle(int position){
            return tabTitle[position];
        }

        public int getTabIcon(int position, boolean isSelected){
            int icon = R.drawable.item_icon_for_tab;

            switch(position){
                case 0:
                    if(isSelected){
                        icon = R.drawable.ic_home_white_36dp;
                    }else {
                        icon = R.drawable.ic_home_black_36dp;
                    }
                    break;
                case 1:
                    if(isSelected){
                        icon = R.drawable.ic_face_white_36dp;
                    }else {
                        icon = R.drawable.ic_face_black_36dp;
                    }
                    break;
                case 2:
                    if(isSelected){
                        icon = R.drawable.ic_search_white_36dp;
                    }else {
                        icon = R.drawable.ic_search_black_36dp;
                    }
                    break;
                case 3:
                    if(isSelected){
                        icon = R.drawable.ic_account_circle_white_36dp;
                    }else {
                        icon = R.drawable.ic_account_circle_black_36dp;
                    }
                    break;
            }

            return icon;
        }


        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            String s = "sssss" + "sss";
            double n = Math.random()*30;
            s += String.valueOf(n);

            if(position == 0) {
                return DashboardTabFragment.newInstance();
            }else {
                return SearchTabFragment.newInstance(s);
                //v = activity.getLayoutInflater().inflate(R.layout.item_list_tab, container, false);
            }
        }

        /*
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==((View)object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            Timber.d("dashboard ,position %s", position);

            View v;
            if(position == 0) {
                v = layoutInflater.inflate(R.layout.fragment_dashboard_tab, null);
            }else if (position == 1){
                v = layoutInflater.inflate(R.layout.page_search, null);
            }else if (position == 2){
                v = layoutInflater.inflate(R.layout.page_search, null);
            }else {
                v = layoutInflater.inflate(R.layout.page_search, null);
                //v = activity.getLayoutInflater().inflate(R.layout.item_list_tab, container, false);
            }

            container.addView(v);
            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
        */
    }
}
