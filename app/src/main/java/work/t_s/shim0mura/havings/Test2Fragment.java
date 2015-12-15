package work.t_s.shim0mura.havings;

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

/**
 * Created by shim0mura on 2015/11/26.
 */
public class Test2Fragment extends Fragment {

    public Test2Fragment() {
    }

    public static Test2Fragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt("page", page);
        Test2Fragment fragment = new Test2Fragment();
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
            for (int i = 1; i <= 100; i++) {
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


            /*
            listView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d("touch", String.valueOf(event.getAction() == MotionEvent.ACTION_MOVE));
                    //Log.d("super touch", String.valueOf(v.onTouchEvent(event)));

                    if(MotionEvent.ACTION_MOVE == event.getAction()){
                        Log.d("action type", "move");
                        Log.d("point move", String.valueOf(lastY - (int) event.getY()));
                    }
                    if(MotionEvent.ACTION_DOWN == event.getAction()){
                        Log.d("action type", "down");
                        lastY = (int)event.getY();
                        Log.d("point", String.valueOf(lastY));
                    }

                    //return (event.getAction() == MotionEvent.ACTION_MOVE);
                    //return v.onTouchEvent(event);
                    return true;
                }
            });
            */


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
