package work.t_s.shim0mura.havings.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.otto.Subscribe;
import com.tokenautocomplete.FilteredArrayAdapter;
import com.wefika.flowlayout.FlowLayout;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;
import work.t_s.shim0mura.havings.PickupActivity;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.SearchTagResultActivity;
import work.t_s.shim0mura.havings.model.ApiService;
import work.t_s.shim0mura.havings.model.BusHolder;
import work.t_s.shim0mura.havings.model.DefaultTag;
import work.t_s.shim0mura.havings.model.entity.ItemEntity;
import work.t_s.shim0mura.havings.model.entity.PickupEntity;
import work.t_s.shim0mura.havings.model.entity.PopularTagEntity;
import work.t_s.shim0mura.havings.model.entity.TagEntity;
import work.t_s.shim0mura.havings.presenter.HomePresenter;
import work.t_s.shim0mura.havings.presenter.ItemPresenter;
import work.t_s.shim0mura.havings.presenter.SearchPresenter;
import work.t_s.shim0mura.havings.util.SpaceTokenizer;
import work.t_s.shim0mura.havings.util.ViewUtil;

public class SearchTabFragment extends Fragment {

    @Bind(R.id.search) MultiAutoCompleteTextView searchTag;
    @Bind(R.id.popular_tag_wrapper) LinearLayout popularTagWrapper;
    @Bind(R.id.popular_list_wrapper) LinearLayout popularListWrapper;
    @Bind(R.id.view_more_popular_tag) LinearLayout viewMorePopularTag;
    @Bind(R.id.view_more_popular_list) LinearLayout viewMorePopularList;

    private SearchPresenter searchPresenter;
    private List<TagEntity> tagEntities;
    private PickupEntity pickupEntity;
    private LayoutInflater layoutInflater;

    public static SearchTabFragment newInstance() {
        SearchTabFragment fragment = new SearchTabFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public SearchTabFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DefaultTag defaultTag = DefaultTag.getSingleton(getActivity());
        tagEntities = defaultTag.getTagEntities();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_tab, container, false);
        ButterKnife.bind(this, view);

        searchPresenter = new SearchPresenter(getActivity());

        // SearchViewをつかってActionBarを入れ替えることも出来そうだし
        // そのほうがカッコ良さそうだけど、面倒だしMultiAutoCompが使えるか分からないし時間かかりそうなので
        // 今のところはちょっとかっこ悪いけど検索ボックスをFragmentの中に表示する
        setTagAdapter();
        searchTag.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String tags = v.getText().toString();
                    SearchTagResultActivity.startActivity(getActivity(), tags);
                    return true;
                }
                return false;
            }
        });

        //if(pickupEntity == null) {
            searchPresenter.getPickup();
        //}
        layoutInflater = LayoutInflater.from(getActivity());

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
        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Timber.d("unregister observer from dashboard fragment");
        super.onPause();
    }

    @Subscribe
    public void renderPickup(PickupEntity pickup){
        pickupEntity = pickup;

        if(pickupEntity.popularTag != null) {
            HomePresenter.setPopularTag(getContext(), popularTagWrapper, pickupEntity.popularTag, false);
            viewMorePopularTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PickupActivity.startActivity(getContext(), pickupEntity, PickupActivity.PICKUP_TYPE_TAG);
                }
            });
        }

        if(pickupEntity.popularList != null){
            HomePresenter.setPopularList(getContext(), popularListWrapper, pickupEntity.popularList, false);
            viewMorePopularList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PickupActivity.startActivity(getContext(), pickupEntity, PickupActivity.PICKUP_TYPE_LIST);
                }
            });
        }
    }

    private void setTagAdapter(){
        ArrayAdapter<TagEntity> adapter = new FilteredArrayAdapter<TagEntity>(getActivity(), android.R.layout.simple_list_item_1, tagEntities) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {

                    LayoutInflater l = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                    convertView = l.inflate(android.R.layout.simple_list_item_1, parent, false);
                }

                TagEntity p = getItem(position);

                ((TextView)convertView).setText(p.getName());

                return convertView;
            }

            @Override
            protected boolean keepObject(TagEntity tag, String mask) {
                mask = mask.toLowerCase();
                return tag.getName().toLowerCase().startsWith(mask) || tag.getYomiJp().startsWith(mask) || tag.getYomiRoma().toLowerCase().startsWith(mask);
            }
        };

        searchTag.setAdapter(adapter);
        searchTag.setTokenizer(new SpaceTokenizer());
        searchTag.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TagEntity tag = new TagEntity();
                TextView selected = (TextView) view;
                tag.name = selected.getText().toString();
            }
        });
    }


}
