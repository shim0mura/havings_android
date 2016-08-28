package work.t_s.shim0mura.havings.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import io.realm.Realm;
import io.realm.RealmResults;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.DefaultTag;
import work.t_s.shim0mura.havings.model.realm.TagEntity;

/**
 * Created by shim0mura on 2015/12/30.
 */
public class ListSelectAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    public static final String TAG_ID_KEY = "tagIdKey";
    public static final String TAG_KIND_KEY = "tagKindKey";
    public static final String TAG_NAME = "searchTag";
    public static final String TAG_SUBTEXT_KEY = "tagSubtextKey";

    private final int subtextCount = 3;

    private LayoutInflater layoutInflater;
    private int layoutResource;
    private Context context;

    private ArrayList<HashMap<String, String>> tagHash = new ArrayList<>();
    private ArrayList<Integer> kindIds = new ArrayList<>();

    public ListSelectAdapter(Context c){
        layoutInflater = LayoutInflater.from(c);
        context = c;

        Realm realm = Realm.getInstance(DefaultTag.getRealmConfig(c));

        RealmResults<TagEntity> tags = realm.where(TagEntity.class).equalTo("tagType", 3).equalTo("parentId", 0).equalTo("isDeleted", false).findAll();

        for(TagEntity t: tags){
            kindIds.add(t.getId());
            getTagRecursive(realm, t, t.getId(), 0);
        }

    }

    private void getTagRecursive(Realm realm, TagEntity parentTag, int kindId, int nest){
        RealmResults<TagEntity> childTag = realm.where(TagEntity.class).equalTo("parentId", parentTag.getId()).findAll();

        StringBuilder subtext = new StringBuilder();
        int tagCount = (childTag.size() > subtextCount) ? subtextCount : childTag.size();

        for(int i = 0; i < tagCount; i++){
            TagEntity t = childTag.get(i);
            subtext.append(t.getName());
            if(i != tagCount -1){
                subtext.append(", ");
            }else{
                subtext.append("など");
            }
        }

        HashMap<String, String> hash = new HashMap<String,String>();
        hash.put(TAG_ID_KEY, String.valueOf(parentTag.getId()));
        hash.put(TAG_KIND_KEY, String.valueOf(kindId));
        hash.put(TAG_NAME, parentTag.getName());
        hash.put(TAG_SUBTEXT_KEY, subtext.toString());

        tagHash.add(hash);

        for(TagEntity t: childTag){
            if(t.getTagType() == 3 && nest < 1) {
                getTagRecursive(realm, t, kindId, nest + 1);
            }
        }
    }

    public ArrayList<Integer> getKindIds(){
        return kindIds;
    }

    @Override
    public int getCount() {
        return tagHash.size();
    }

    @Override
    public Object getItem(int position) {
        return tagHash.get(position);
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
            convertView = layoutInflater.inflate(R.layout.test_list_item, parent, false);
            holder.itemText = (TextView) convertView.findViewById(R.id.item_text);
            holder.itemSubText = (TextView) convertView.findViewById(R.id.item_sub_text);

            convertView.setTag(R.id.TAG_ITEM_HOLDER, holder);
        } else {
            holder = (ViewHolder) convertView.getTag(R.id.TAG_ITEM_HOLDER);
        }

        HashMap<String, String> tag = tagHash.get(position);

        holder.itemText.setText(tag.get(TAG_NAME));
        holder.itemSubText.setText(tag.get(TAG_SUBTEXT_KEY));
        convertView.setTag(R.id.TAG_ITEM_ID, Integer.valueOf(tag.get(TAG_ID_KEY)));
        convertView.setTag(R.id.TAG_ITEM_NAME, tag.get(TAG_NAME));

        return convertView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = layoutInflater.inflate(R.layout.stickylistview_header, parent, false);
            holder.headerText = (TextView) convertView.findViewById(R.id.header);
            holder.headerSubText = (TextView) convertView.findViewById(R.id.subheader);

            convertView.setTag(R.id.TAG_HEADER_HOLDER, holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag(R.id.TAG_HEADER_HOLDER);
        }
        holder.headerText.setText(tagHash.get(position).get(TAG_NAME));
        holder.headerSubText.setText(tagHash.get(position).get(TAG_SUBTEXT_KEY));

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        //return the first character of the country as ID because this is what headers are based upon
        //return countries.get(position).subSequence(5,6).charAt(0);
        HashMap<String, String> hash = tagHash.get(position);
        int id = 0;
        try {
            id = Integer.parseInt(hash.get(TAG_KIND_KEY));
        }catch(Exception e){
            id = 0;
        }
        return id;
    }

    class HeaderViewHolder {
        TextView headerText;
        TextView headerSubText;
    }

    class ViewHolder {
        TextView itemText;
        TextView itemSubText;
    }
}
