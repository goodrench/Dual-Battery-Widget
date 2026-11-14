package org.flexlabs.widgets.dualbattery.modern;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import org.flexlabs.widgets.dualbattery.R;

/**
 * Optional, user-enabled foreground monitoring for near-immediate battery updates.
 * A normal AppWidgetProvider cannot manifest-register ACTION_BATTERY_CHANGED.
 */
public final class BatteryMonitorService extends Service {
    public static final String ACTION_START = "org.flexlabs.widgets.dualbattery.START_MONITOR";
    private static final String CHANNEL = "battery_monitor";
    private BroadcastReceiver receiver;

    @Override public void onCreate() {
        super.onCreate();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.createNotificationChannel(new NotificationChannel(
                CHANNEL, "Live battery widget updates", NotificationManager.IMPORTANCE_LOW));
        Notification notification = new Notification.Builder(this, CHANNEL)
                .setSmallIcon(R.drawable.ic_battery)
                .setContentTitle("Dual Battery Widget")
                .setContentText("Live battery updates are enabled")
                .setOngoing(true)
                .build();
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(814, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(814, notification);
        }

        receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                ModernWidget.refreshAll(context);
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(android.bluetooth.BluetoothDevice.ACTION_BATTERY_LEVEL_CHANGED);
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(receiver, filter);
        }
        ModernWidget.refreshAll(this);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override public void onDestroy() {
        if (receiver != null) unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
