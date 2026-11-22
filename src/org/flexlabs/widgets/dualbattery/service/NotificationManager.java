/*
 * Copyright 2011 Artiom Chilaru (http://flexlabs.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.flexlabs.widgets.dualbattery.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import org.flexlabs.widgets.dualbattery.BatteryLevel;
import org.flexlabs.widgets.dualbattery.Constants;
import org.flexlabs.widgets.dualbattery.R;
import org.flexlabs.widgets.dualbattery.app.BatteryHistoryActivity;
import org.flexlabs.widgets.dualbattery.app.SettingsContainer;

public class NotificationManager implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int NOTIFICATION_DOCK = 1;
    private static final String CHANNEL_ID = "battery_monitor";

    private android.app.NotificationManager mNotificationManager;
    private Context mContext;
    private boolean enabled;

    public NotificationManager(Context context) {
        mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mContext = context;
        createNotificationChannel(context);

        enabled = new SettingsContainer(context, this).isShowNotificationIcon();
    }

    static Notification createForegroundNotification(Context context) {
        createNotificationChannel(context);
        return createNotification(context, R.drawable.stat_sys_battery, "Monitoring battery levels", 0);
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, context.getString(R.string.app_name), android.app.NotificationManager.IMPORTANCE_LOW);
            channel.setShowBadge(false);
            android.app.NotificationManager manager =
                    (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
    }

    private static Notification createNotification(Context context, int icon, CharSequence text, int level) {
        Intent intent = new Intent(context, BatteryHistoryActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Notification.Builder(context, CHANNEL_ID)
                    .setSmallIcon(icon, level)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(text)
                    .setContentIntent(contentIntent)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .build();
        }
        return new androidx.core.app.NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(icon, level)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(text)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .getNotification();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Constants.SETTING_NOTIFICATION_ICON.equals(key)) {
            enabled = new SettingsContainer(mContext).isShowNotificationIcon();
            BatteryLevel level = BatteryLevel.getCurrent();
            if (enabled && level != null && level.get_dock_level() != null) {
                update(level.get_dock_level(), level.get_dock_status() == Constants.DOCK_STATE_CHARGING);
            } else {
                hide();
            }
        }
    }

    public void update(int dockLevel, boolean charging) {
        if (!enabled) {
            return;
        }

        int icon = charging
                ? R.drawable.stat_sys_battery_charge
                : R.drawable.stat_sys_battery;
        Notification notification = createNotification(
                mContext, icon, "Dock battery level: " + dockLevel + "%", dockLevel);
        mNotificationManager.notify(NOTIFICATION_DOCK, notification);
    }

    public void hide() {
        mNotificationManager.cancel(NOTIFICATION_DOCK);
    }
}
