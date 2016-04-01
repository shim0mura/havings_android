package work.t_s.shim0mura.havings.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lecho.lib.hellocharts.view.PieChartView;
import timber.log.Timber;
import work.t_s.shim0mura.havings.ItemActivity;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.RegisterActivity;
import work.t_s.shim0mura.havings.UserActivity;
import work.t_s.shim0mura.havings.model.ApiServiceManager;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.entity.ItemPercentageEntity;
import work.t_s.shim0mura.havings.model.event.GenericEvent;
import work.t_s.shim0mura.havings.model.event.ItemPercentageGraphEvent;
import work.t_s.shim0mura.havings.presenter.UserPresenter;

/**
 * Created by shim0mura on 2016/03/30.
 */
public class DashboardTabFragment extends Fragment {

    private static final String BUNDLE_ARG = "BundleArg";

    @Bind(R.id.swipe) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.total_item_percentage_wrapper) LinearLayout graphDetailWrapper;
    @Bind(R.id.pie_chart) PieChartView pieChartView;

    private UserPresenter userPresenter;
    private ArrayList<ItemPercentageEntity> itemPercentageEntityArrayList;

    public DashboardTabFragment(){}

    public static DashboardTabFragment newInstance() {
        DashboardTabFragment fragment = new DashboardTabFragment();
        Bundle args = new Bundle();
        //args.putString(BUNDLE_ARG, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
        */
        userPresenter = new UserPresenter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_dashboard_tab, container, false);
        ButterKnife.bind(this, view);

        if(itemPercentageEntityArrayList == null){
            userPresenter.getItemPercentage();
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Timber.d("swipe end");
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 3000);
            }
        });
        swipeRefreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        BusHolder.get().register(this);
        Timber.d("register observer from dashboard fragment");
    }

    @Override
    public void onPause() {
        BusHolder.get().unregister(this);
        Timber.d("unregister observer from dashboard fragment");
        super.onPause();
    }

    @Subscribe
    public void renderPieChart(ItemPercentageGraphEvent graphEvent){
        Timber.d("render chart");
        itemPercentageEntityArrayList = graphEvent.itemPercentageEntities;

        GraphRenderer.renderPieChart(getActivity(), pieChartView, graphDetailWrapper, itemPercentageEntityArrayList);
    }

    @Subscribe
    public void renderPieChartDetail(GenericEvent genericEvent){
        ItemPercentageEntity categoryParent = itemPercentageEntityArrayList.get(genericEvent.result);

        if(categoryParent == null){
            return;
        }

        GraphRenderer.renderPieChartDetail(getActivity(), graphDetailWrapper, categoryParent);
    }

    @OnClick(R.id.logout)
    public void logout(View v){
        Log.d("ssss", "logout");
        ApiServiceManager asm = ApiServiceManager.getSingleton(getActivity());
        asm.clearApiKey();
        Intent intent = new Intent(getActivity(), RegisterActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    @OnClick(R.id.item)
    public void navigateToItem(View v){
        ItemActivity.startActivity(getActivity(), 2);

        //UserActivity.startActivity(this, 10);

        //Intent intent = new Intent(this, UserActivity.class);
        //intent.putExtra("itemId", 2);
        //startActivity(intent);
    }

    @OnClick(R.id.user_10)
    public void navigateToUser(View v){
        UserActivity.startActivity(getActivity(), 10);
    }

    @OnClick(R.id.user_6)
    public void navigateToUser6(View v){
        UserActivity.startActivity(getActivity(), 6);
    }
}
