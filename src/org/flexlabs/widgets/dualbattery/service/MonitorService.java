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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import org.flexlabs.widgets.dualbattery.BatteryLevel;
import org.flexlabs.widgets.dualbattery.BatteryWidgetUpdater;
import org.flexlabs.widgets.dualbattery.Constants;

public class MonitorService extends Service {
    private static final int FOREGROUND_NOTIFICATION_ID = 2;
    private IntentReceiver batteryReceiver;

    public static void start(Context context) {
        start(context, null);
    }

    public static void start(Context context, int[] widgetIds) {
        Intent intent = new Intent(context, MonitorService.class);
        if (widgetIds != null)
            intent.putExtra(Constants.EXTRA_WIDGET_IDS, widgetIds);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(intent);
        else
            context.startService(intent);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        startForeground(FOREGROUND_NOTIFICATION_ID, NotificationManager.createForegroundNotification(this));
        batteryReceiver = new IntentReceiver(this);
        registerSystemReceiver(new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerSystemReceiver(new IntentFilter(Intent.ACTION_SCREEN_OFF));
        registerSystemReceiver(new IntentFilter(Intent.ACTION_DOCK_EVENT));
        registerSystemReceiver(new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private void registerSystemReceiver(IntentFilter filter) {
        androidx.core.content.ContextCompat.registerReceiver(this, batteryReceiver, filter,
                androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void processStartIntent(Intent intent) {
        if (intent == null)
            return;
        final int[] widgetIds = intent.getIntArrayExtra(Constants.EXTRA_WIDGET_IDS);
        if (widgetIds != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BatteryWidgetUpdater.updateAllWidgets(MonitorService.this, BatteryLevel.getCurrent(), widgetIds);
                    } catch (Exception ignore) {}
                }
            }).start();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        processStartIntent(intent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryReceiver);
    }
}
