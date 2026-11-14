package org.flexlabs.widgets.dualbattery.modern;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class SettingsActivity extends Activity {
    private Spinner devices;
    private Spinner themes;
    private final List<String> addresses = new ArrayList<>();
    private final List<String> deviceLabels = new ArrayList<>();
    private SharedPreferences prefs;

    @Override public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        prefs = getSharedPreferences(ModernWidget.PREFS, MODE_PRIVATE);
        ScrollView scroll = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(36, 40, 36, 36);
        scroll.addView(layout);
        TextView header = text("Dual Battery Widget", 27);
        layout.addView(header);
        TextView description = text("Phone and paired Bluetooth accessory batteries", 15);
        layout.addView(description);
        layout.addView(text("Widget theme", 19));
        themes = new Spinner(this);
        String[] choices = {"System", "Dark", "Light", "Material You"};
        themes.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, choices));
        String current = prefs.getString(ModernWidget.THEME, "SYSTEM");
        int selected = "DARK".equals(current) ? 1 : "LIGHT".equals(current) ? 2 : "MATERIAL".equals(current) ? 3 : 0;
        themes.setSelection(selected);
        layout.addView(themes);
        layout.addView(text("Bluetooth accessory", 19));
        devices = new Spinner(this);
        layout.addView(devices);
        Button grant = new Button(this);
        grant.setText("Allow Bluetooth access / reload devices");
        grant.setOnClickListener(v -> loadDevices(true));
        layout.addView(grant);
        TextView note = text("Only paired devices that report their battery level to Android can display a percentage. Refreshes occur when Android delivers events, on settings changes, and periodically.", 14);
        layout.addView(note);
        Button save = new Button(this);
        save.setText("Save and refresh widget");
        save.setOnClickListener(v -> {
            String[] keys = {"SYSTEM", "DARK", "LIGHT", "MATERIAL"};
            int themeIndex = themes.getSelectedItemPosition();
            int deviceIndex = devices.getSelectedItemPosition();
            prefs.edit().putString(ModernWidget.THEME, keys[Math.max(0, themeIndex)])
                    .putString(ModernWidget.ACCESSORY,
                            deviceIndex >= 0 && deviceIndex < addresses.size() ? addresses.get(deviceIndex) : "")
                    .apply();
            ModernWidget.refreshAll(this);
            setResult(RESULT_OK);
            finish();
        });
        layout.addView(save);
        setContentView(scroll);
        loadDevices(false);
    }

    private TextView text(String label, int size) {
        TextView view = new TextView(this);
        view.setText(label);
        view.setTextSize(size);
        view.setPadding(0, 15, 0, 13);
        return view;
    }

    private void loadDevices(boolean request) {
        if (Build.VERSION.SDK_INT >= 31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            deviceLabels.clear();
            addresses.clear();
            deviceLabels.add("Bluetooth permission needed");
            addresses.add("");
            devices.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, deviceLabels));
            if (request) requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 101);
            return;
        }
        addresses.clear();
        deviceLabels.clear();
        addresses.add("");
        deviceLabels.add("None (phone only)");
        try {
            BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter adapter = manager == null ? null : manager.getAdapter();
            Set<BluetoothDevice> paired = adapter == null ? null : adapter.getBondedDevices();
            if (paired != null) {
                for (BluetoothDevice device : paired) {
                    addresses.add(device.getAddress());
                    String name = device.getName();
                    deviceLabels.add((name == null ? "Bluetooth accessory" : name) + " (" + device.getAddress() + ")");
                }
            }
        } catch (SecurityException ignored) { }
        devices.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, deviceLabels));
        String previous = prefs.getString(ModernWidget.ACCESSORY, "");
        int chosen = addresses.indexOf(previous);
        devices.setSelection(Math.max(0, chosen));
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == 101) loadDevices(false);
    }
}
