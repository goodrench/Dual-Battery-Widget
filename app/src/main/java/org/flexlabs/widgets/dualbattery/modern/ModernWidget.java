package org.flexlabs.widgets.dualbattery.modern;

import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.BatteryManager;
import android.os.Build;
import android.widget.RemoteViews;
import android.graphics.Color;

import org.flexlabs.widgets.dualbattery.R;

public final class ModernWidget extends AppWidgetProvider {
    static final String PREFS = "modern_widget_settings";
    static final String ACCESSORY = "accessory_address";
    static final String THEME = "widget_theme";
    static final String REFRESH = "org.flexlabs.widgets.dualbattery.REFRESH";

    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) update(context, manager, id);
    }

    @Override public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent == null || intent.getAction() == null) return;
        String action = intent.getAction();
        if (REFRESH.equals(action) || Intent.ACTION_POWER_CONNECTED.equals(action)
                || Intent.ACTION_POWER_DISCONNECTED.equals(action)
                || Intent.ACTION_BATTERY_LOW.equals(action)
                || Intent.ACTION_BATTERY_OKAY.equals(action)
                || Intent.ACTION_BOOT_COMPLETED.equals(action)
                || BluetoothDevice.ACTION_BATTERY_LEVEL_CHANGED.equals(action)) {
            refreshAll(context);
        }
    }

    static void refreshAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, ModernWidget.class));
        for (int id : ids) update(context, manager, id);
    }

    private static void update(Context context, AppWidgetManager manager, int id) {
        Intent state = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = -1;
        boolean charging = false;
        if (state != null) {
            int raw = state.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = state.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
            if (raw >= 0 && scale > 0) level = Math.max(0, Math.min(100, raw * 100 / scale));
            int status = state.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            charging = status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status == BatteryManager.BATTERY_STATUS_FULL;
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        views.setTextViewText(R.id.phone_level, level < 0 ? "Unavailable" :
                level + "%" + (charging ? "  ⚡" : ""));
        views.setProgressBar(R.id.phone_progress, 100, Math.max(0, level), false);

        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String address = prefs.getString(ACCESSORY, "");
        String label = "Bluetooth device";
        String value = "Select in settings";
        int accessoryLevel = 0;
        if (!address.isEmpty()) {
            value = "Battery unavailable";
            if (Build.VERSION.SDK_INT >= 31 && context.checkSelfPermission(
                    Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                value = "Bluetooth permission needed";
            } else {
                try {
                    BluetoothManager bluetooth = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
                    BluetoothAdapter adapter = bluetooth == null ? null : bluetooth.getAdapter();
                    BluetoothDevice device = adapter == null ? null : adapter.getRemoteDevice(address);
                    if (device != null && adapter.isEnabled()) {
                        label = device.getName() == null ? "Bluetooth device" : device.getName();
                        int reported = device.getBatteryLevel();
                        if (reported >= 0 && reported <= 100) {
                            accessoryLevel = reported;
                            value = reported + "%";
                        }
                    } else {
                        value = "Bluetooth off";
                    }
                } catch (SecurityException | IllegalArgumentException error) {
                    value = "Bluetooth unavailable";
                }
            }
        }
        views.setTextViewText(R.id.accessory_title, label);
        views.setTextViewText(R.id.accessory_level, value);
        views.setProgressBar(R.id.accessory_progress, 100, accessoryLevel, false);

        String theme = prefs.getString(THEME, "SYSTEM");
        boolean dark = (context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if ("DARK".equals(theme)) dark = true;
        if ("LIGHT".equals(theme)) dark = false;
        int main = dark ? Color.WHITE : Color.rgb(30, 37, 52);
        int secondary = dark ? Color.rgb(218, 223, 238) : Color.rgb(66, 73, 91);
        if ("MATERIAL".equals(theme) && Build.VERSION.SDK_INT >= 31) {
            main = context.getColor(android.R.color.system_accent1_100);
            secondary = context.getColor(android.R.color.system_accent2_200);
        }
        // RemoteViews does not inherit the Activity's theme.
        views.setTextColor(R.id.phone_title, secondary);
        views.setTextColor(R.id.accessory_title, secondary);
        views.setTextColor(R.id.phone_level, main);
        views.setTextColor(R.id.accessory_level, main);

        Intent settings = new Intent(context, SettingsActivity.class);
        PendingIntent click = PendingIntent.getActivity(context, 0, settings,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, click);
        manager.updateAppWidget(id, views);
    }
}
