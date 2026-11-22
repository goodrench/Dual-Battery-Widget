package org.flexlabs.widgets.dualbattery.ui;

import android.app.Activity;
import android.graphics.Insets;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;

public final class SystemBarInsets {
    private SystemBarInsets() {
    }

    public static void apply(final Activity activity) {
        if (Build.VERSION.SDK_INT < 35)
            return;

        final View content = activity.findViewById(android.R.id.content);
        final View actionBar = activity.findViewById(androidx.appcompat.R.id.action_bar_container);
        content.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                Insets insets = windowInsets.getInsets(
                        WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                int top = actionBar == null ? insets.top : Math.max(insets.top, actionBar.getHeight());
                view.setPadding(insets.left, top, insets.right, insets.bottom);
                return windowInsets;
            }
        });
        if (actionBar != null) {
            actionBar.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int left, int top, int right, int bottom,
                        int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    content.requestApplyInsets();
                }
            });
        }
        content.requestApplyInsets();
    }
}
