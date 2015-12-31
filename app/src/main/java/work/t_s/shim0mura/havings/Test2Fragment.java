package work.t_s.shim0mura.havings;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

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
        View view = inflater.inflate(R.layout.category_list_tab, container, false);
        if(page == 1) {
            final ExpandableStickyListHeadersListView listView = (ExpandableStickyListHeadersListView)view.findViewById(R.id.list);
            ArrayList<String> items = new ArrayList<String>();
            for (int i = 1; i <= 100; i++) {
                items.add("Item " + i);
            }
            //listView.setAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, items));
            listView.setAdapter(new MyAdapter(getContext()));
            listView.setOnHeaderClickListener(new StickyListHeadersListView.OnHeaderClickListener() {
                @Override
                public void onHeaderClick(StickyListHeadersListView l, View header, int itemPosition, long headerId, boolean currentlySticky) {
                    if(listView.isHeaderCollapsed(headerId)){
                        listView.expand(headerId);
                    }else {
                        listView.collapse(headerId);
                    }
                }
            });

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

    public class MyAdapter extends BaseAdapter implements StickyListHeadersAdapter {

        private ArrayList<String> countries;
        private LayoutInflater inflater;

        public MyAdapter(Context context) {
            inflater = LayoutInflater.from(context);
            //countries = context.getResources().getStringArray(R.array.countries);
            countries = new ArrayList<String>();
            for (int i = 1; i <= 100; i++) {
                countries.add("Item " + i);
            }

        }

        @Override
        public int getCount() {
            return countries.size();
        }

        @Override
        public Object getItem(int position) {
            return countries.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.test_list_item, parent, false);
                holder.text = (TextView) convertView.findViewById(R.id.text);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.text.setText(countries.get(position));

            return convertView;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            if (convertView == null) {
                holder = new HeaderViewHolder();
                convertView = inflater.inflate(R.layout.stickylistview_header, parent, false);
                holder.text = (TextView) convertView.findViewById(R.id.header);
                convertView.setTag(holder);
            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }
            //set header text as first char in name
            String headerText = "" + countries.get(position);
            holder.text.setText(headerText);
            return convertView;
        }

        @Override
        public long getHeaderId(int position) {
            //return the first character of the country as ID because this is what headers are based upon
            return countries.get(position).subSequence(5,6).charAt(0);
        }

        class HeaderViewHolder {
            TextView text;
        }

        class ViewHolder {
            TextView text;
        }

    }
}
