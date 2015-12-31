package work.t_s.shim0mura.havings.util;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tokenautocomplete.TokenCompleteTextView;

import java.util.List;

import timber.log.Timber;
import work.t_s.shim0mura.havings.ItemFormActivity;
import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.entity.TagEntity;
import work.t_s.shim0mura.havings.model.realm.Tag;

/**
 * Created by shim0mura on 2015/12/18.
 */

public class TagCompletionView extends TokenCompleteTextView<TagEntity> {
    public TagCompletionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View getViewForObject(TagEntity tag) {

        LayoutInflater l = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        LinearLayout view = (LinearLayout)l.inflate(R.layout.contact_token, (ViewGroup)TagCompletionView.this.getParent(), false);
        ((TextView)view.findViewById(R.id.name)).setText(tag.getName());

        return view;
    }

    @Override
    public List<TagEntity> getObjects() {
        List<TagEntity> a = super.getObjects();
        //Log.d("tagView,getObj", a.toString());
        return a;
    }

    @Override
    protected TagEntity defaultObject(String completionText) {
        //Stupid simple example of guessing if we have an email or not
        /*
        int index = completionText.indexOf('@');
        if (index == -1) {
            return new Person(completionText, completionText.replace(" ", "") + "@example.com");
        } else {
            return new Person(completionText.substring(0, index), completionText);
        }*/
        Log.d("tagView,defaultObj", completionText);
        TagEntity tag = new TagEntity();
        tag.setName(completionText);
        return tag;
    }
}
