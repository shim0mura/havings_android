package work.t_s.shim0mura.havings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tokenautocomplete.FilteredArrayAdapter;
import com.tokenautocomplete.TokenCompleteTextView;

import work.t_s.shim0mura.havings.model.entity.TagEntity;
import work.t_s.shim0mura.havings.util.AutoTagCompletionView;

public class TokenAutoCompActivity extends AppCompatActivity {

    private AutoTagCompletionView completionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_auto_comp);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TagEntity t1 = new TagEntity("アんどろいｄ");
        TagEntity t2 = new TagEntity("レイルズ");
        t2.yomiRoma = "reiruzu";
        TagEntity t3 = new TagEntity("ルビイ");



        TagEntity tags[] = new TagEntity[]{
                t1, t2, t3
        };

        //ArrayAdapter<TagEntity> adapter = new ArrayAdapter<TagEntity>(this, android.R.layout.simple_list_item_1, tags);

        ArrayAdapter<TagEntity> adapter = new FilteredArrayAdapter<TagEntity>(this, android.R.layout.simple_list_item_1, tags) {

            @Override
            protected boolean keepObject(TagEntity tag, String mask) {
                mask = mask.toLowerCase();
                return tag.getName().toLowerCase().startsWith(mask) || tag.getYomiJp().startsWith(mask) || tag.getYomiRoma().toLowerCase().startsWith(mask);
            }
        };

        completionView = (AutoTagCompletionView)findViewById(R.id.tag_comp);
        completionView.setThreshold(1);
        completionView.setAdapter(adapter);
    }


}