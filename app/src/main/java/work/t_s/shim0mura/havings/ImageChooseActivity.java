package work.t_s.shim0mura.havings;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ImageChooseActivity extends AppCompatActivity {

    private static final String SERIALIZED_LIST_NAME = "SerializedListName";

    private String listName;

    public static void startActivity(Context context, String listName){
        Intent intent = new Intent(context, new Object() {}.getClass().getEnclosingClass());
        intent.putExtra(SERIALIZED_LIST_NAME, listName);

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_choose);

        Bundle extras = getIntent().getExtras();
        listName = (String) extras.getSerializable(SERIALIZED_LIST_NAME);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.test)
    public void redirectToItem(){
        int id = 2;
        ListNameSelectActivity.startActivity(this);
    }


}
