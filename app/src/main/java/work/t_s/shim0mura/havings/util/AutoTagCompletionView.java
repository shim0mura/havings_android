package work.t_s.shim0mura.havings.util;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tokenautocomplete.TokenCompleteTextView;

import work.t_s.shim0mura.havings.R;
import work.t_s.shim0mura.havings.model.entity.TagEntity;

/**
 * Created by shim0mura on 2016/05/01.
 */
public class AutoTagCompletionView extends TokenCompleteTextView<TagEntity> {
    public AutoTagCompletionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View getViewForObject(TagEntity tag) {

        LayoutInflater l = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        LinearLayout view = (LinearLayout)l.inflate(R.layout.contact_token, (ViewGroup) AutoTagCompletionView.this.getParent(), false);
        ((TextView)view.findViewById(R.id.name)).setText(tag.getName());

        return view;
    }

    @Override
    protected TagEntity defaultObject(String completionText) {
        TagEntity tag = new TagEntity();
        tag.setName(completionText);
        return tag;
    }
}