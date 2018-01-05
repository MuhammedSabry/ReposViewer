package com.sabry.muhammed.reposviewer.utility;

import android.content.Context;
import android.widget.Toast;

public class SingleToast {

    private static Toast mToast;

    public static void show(Context context) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(context, "No connection detected!", Toast.LENGTH_SHORT);
        mToast.show();
    }
}