package work.t_s.shim0mura.havings;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

public class ShareToClipboardActivity extends AppCompatActivity {

    public static String KEY_CLIP_TEXT = "clipText";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String clipText = getIntent().getStringExtra(KEY_CLIP_TEXT);
        if (!TextUtils.isEmpty(clipText)) {
            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(clipText);
            } else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("clip_text", clipText);
                clipboard.setPrimaryClip(clip);
            }
            Toast.makeText(this, getString(R.string.prompt_link_copied), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

}
