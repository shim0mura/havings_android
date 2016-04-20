package work.t_s.shim0mura.havings.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * Created by shim0mura on 2016/04/20.
 */
public class PermissionUtil {

    public static final int CAMERA_PERMISSION_REQUEST_CODE = 11;
    public static final int WRITE_EXT_STORAGE_PERMISSION_REQUEST_CODE = 12;
    public static final int CAMERA_AND_GALLERY_PERMISSION_REQUEST_CODE = 13;

    public static boolean hasSelfPermission(Activity activity, String permission) {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
    }

}
