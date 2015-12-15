package work.t_s.shim0mura.havings.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.presenter.StickyScrollPresenter;

/**
 * Created by shim0mura on 2015/11/25.
 */
public class TestFragment extends Fragment {

    private int lastY;
    private StickyScrollPresenter presenter;

    public TestFragment(){}

    /*
    public TestFragment(StickyScrollPresenter p) {
        presenter = p;
    }
    */

    public static TestFragment newInstance(int page, StickyScrollPresenter p) {
        Bundle args = new Bundle();
        args.putInt("page", page);
        TestFragment fragment = new TestFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int page = getArguments().getInt("page", 0);
        View view = inflater.inflate(R.layout.item_list_tab, container, false);
        if(page == 1) {
            final ListView listView = (ListView)view.findViewById(R.id.page_text);
            ArrayList<String> items = new ArrayList<String>();
            for (int i = 1; i <= 30; i++) {
                items.add("Item " + i);
            }
            listView.setAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, items));


            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                    Log.d("position", String.valueOf(position));

                                                }
                                            }
            );

            listView.setOnTouchListener(new StickyScrollPresenter.CustomTouchListener(presenter));

            listView.setOnScrollListener(new AbsListView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    Log.d("schroll state", "changed");
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    Log.d("scroll from listview", String.valueOf(visibleItemCount));
                    Log.d("listview", String.valueOf(listView.getHeight()));
                    //view.setEnabled(false);
                }
            });

        }else{
            //((TextView) view.findViewById(R.id.page_text)).setText("Page " + page);

        }
        return view;
    }
}