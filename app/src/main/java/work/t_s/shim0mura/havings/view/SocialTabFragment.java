package work.t_s.shim0mura.havings.view;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;
import work.t_s.shim0mura.havings.ItemActivity;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.entity.TimelineEntity;
import work.t_s.shim0mura.havings.presenter.HomePresenter;

public class SocialTabFragment extends Fragment {

    @Bind(R.id.swipe) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.timeline_list) ListView timelineList;

    private View loader;
    private HomePresenter homePresenter;
    private TimelineAdapter adapter;

    public static SocialTabFragment newInstance() {
        SocialTabFragment fragment = new SocialTabFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public SocialTabFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_social_tab, container, false);
        ButterKnife.bind(this, view);

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        loader = layoutInflater.inflate(R.layout.loading, timelineList, false);

        timelineList.addFooterView(loader);
        timelineList.setAdapter(null);

        homePresenter = new HomePresenter(getActivity());
        homePresenter.getTimeline(0);

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
    public void setTimeline(TimelineEntity timelineEntity){
        loader.findViewById(R.id.progress).setVisibility(View.GONE);

        Timber.d("header %s", timelineList.getHeaderViewsCount());

        if(adapter == null){
            initializeAdapter(timelineEntity);
        }else {
            adapter.finishLoadNextItem();
            loader.findViewById(R.id.progress).setVisibility(View.GONE);
            adapter.addItem(timelineEntity);
            adapter.notifyDataSetChanged();
        }
    }

    private void initializeAdapter(TimelineEntity timelineEntity){
        if(timelineEntity.timeline == null || timelineEntity.timeline.isEmpty()){
            View v = View.inflate(getActivity(), R.layout.partial_nothing_text, null);
            TextView text = (TextView)v.findViewById(R.id.nothing);
            text.setText(getString(R.string.prompt_no_timeline));
            timelineList.addFooterView(v);

            return;
        }

        adapter = new TimelineAdapter(getActivity(), R.layout.partial_timeline_content, timelineEntity);
        timelineList.setAdapter(adapter);

        timelineList.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if ((totalItemCount == firstVisibleItem + visibleItemCount) && adapter.hasNextItem()) {
                    if (!adapter.getIsLoadingNextItem()) {
                        adapter.startLoadNextItem();
                        loader.findViewById(R.id.progress).setVisibility(View.VISIBLE);
                        homePresenter.getTimeline(adapter.getLastEventId());
                    }
                }
            }
        });
    }
}
