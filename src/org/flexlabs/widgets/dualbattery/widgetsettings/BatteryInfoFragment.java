/*
 * Copyright 2012 Artiom Chilaru (http://flexlabs.org)
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

package org.flexlabs.widgets.dualbattery.widgetsettings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import org.flexlabs.widgets.dualbattery.BatteryLevel;
import org.flexlabs.widgets.dualbattery.Constants;
import org.flexlabs.widgets.dualbattery.R;
import org.flexlabs.widgets.dualbattery.storage.BatteryHistoryLoader;
import org.flexlabs.widgets.dualbattery.ui.BatteryHistoryChart;

import java.text.DateFormat;
import java.text.NumberFormat;

public class BatteryInfoFragment extends Fragment {
    private IntentFilter mIntentFilter;
    private TextView mStatus, mLevel, mScale;
    private TextView mHealth;
    private TextView mVoltage;
    private TextView mTemperature;
    private TextView mTechnology;
    private TextView mDockStatus;
    private TextView mDockLevel;
    private TextView mDockLastConnected;
    private TextView mLastCharged;
    private TableRow mRowDockLevel, mRowDockStatus, mRowDockLastConnected;
    private BatteryHistoryChart mChartView;
    private boolean tempUnitsC;
    private int temperature, appWidgetId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        WidgetActivity activity = (WidgetActivity) requireActivity();
        appWidgetId = activity.appWidgetId;
        tempUnitsC = WidgetSettingsContainer.getTempUnits(activity, appWidgetId);
        activity.invalidateOptionsMenu();

        View view = inflater.inflate(R.layout.battery_info_table, container, false);
        mStatus = (TextView) view.findViewById(R.id.status);
        mLevel = (TextView) view.findViewById(R.id.level);
        mScale = (TextView) view.findViewById(R.id.scale);
        mHealth = (TextView) view.findViewById(R.id.health);
        mVoltage = (TextView) view.findViewById(R.id.voltage);
        mTemperature = (TextView) view.findViewById(R.id.temperature);
        mTechnology = (TextView) view.findViewById(R.id.technology);
        mDockStatus = (TextView) view.findViewById(R.id.dock_status);
        mDockLevel = (TextView) view.findViewById(R.id.dock_level);
        mDockLastConnected = (TextView) view.findViewById(R.id.dock_last_connected);
        mLastCharged = (TextView) view.findViewById(R.id.last_charged);
        mRowDockLevel = (TableRow) view.findViewById(R.id.row_dock_level);
        mRowDockStatus = (TableRow) view.findViewById(R.id.row_dock_status);
        mRowDockLastConnected = (TableRow) view.findViewById(R.id.row_dock_lastConnected);
        mChartView = (BatteryHistoryChart) view.findViewById(R.id.chart);

        view.findViewById(R.id.batterySummary).setOnClickListener(batterySummaryListener);
        mIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        requireActivity().unregisterReceiver(batteryReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            requireActivity().registerReceiver(batteryReceiver, mIntentFilter, Context.RECEIVER_EXPORTED);
        else
            requireActivity().registerReceiver(batteryReceiver, mIntentFilter);
        mChartView.setData(BatteryHistoryLoader.load(requireContext(), 7), 7);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.widget_batteryinfo, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.temperature).setTitle(getMenuTitle());
    }

    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) return;

            mLevel.setText(formatInteger(intent.getIntExtra("level", 0)));
            mScale.setText(formatInteger(intent.getIntExtra("scale", 0)));
            int voltage = intent.getIntExtra("voltage", 0);
            int voltageRes = voltage > 1000
                    ? R.string.battery_info_voltage_units_mV
                    : R.string.battery_info_voltage_units_V;
            mVoltage.setText(getString(R.string.battery_info_voltage_value,
                    formatInteger(voltage), context.getString(voltageRes)));
            temperature = intent.getIntExtra("temperature", 0);
            updateTemperature();
            mTechnology.setText(intent.getStringExtra("technology"));

            int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
            switch (status) {
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    String statusString = context.getString(R.string.battery_info_status_charging);
                    int plugType = intent.getIntExtra("plugged", 0);
                    if (plugType > 0) {
                        statusString = statusString + " " + context.getString(
                                (plugType == BatteryManager.BATTERY_PLUGGED_AC)
                                        ? R.string.battery_info_status_charging_ac
                                        : R.string.battery_info_status_charging_usb);
                    }
                    mStatus.setText(statusString);
                    break;
                case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    mStatus.setText(R.string.battery_info_status_discharging);
                    break;
                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                    mStatus.setText(R.string.battery_info_status_not_charging);
                    break;
                case BatteryManager.BATTERY_STATUS_FULL:
                    mStatus.setText(R.string.battery_info_status_full);
                    break;
                default:
                    mStatus.setText(R.string.unknown);
                    break;
            }

            switch (intent.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN)) {
                case BatteryManager.BATTERY_HEALTH_GOOD:
                    mHealth.setText(R.string.battery_info_health_good);
                    break;
                case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                    mHealth.setText(R.string.battery_info_health_overheat);
                    break;
                case BatteryManager.BATTERY_HEALTH_DEAD:
                    mHealth.setText(R.string.battery_info_health_dead);
                    break;
                case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                    mHealth.setText(R.string.battery_info_health_over_voltage);
                    break;
                case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                    mHealth.setText(R.string.battery_info_health_unspecified_failure);
                    break;
                default:
                    mHealth.setText(R.string.unknown);
                    break;
            }

            String lastCharged;
            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                lastCharged = "--";
            } else if (BatteryLevel.lastCharged == null) {
                lastCharged = context.getString(R.string.unknown);
            } else {
                lastCharged = DateFormat.getDateTimeInstance().format(BatteryLevel.lastCharged);
            }
            mLastCharged.setText(lastCharged);

            if (intent.hasExtra("dock_status")) {
                mRowDockLevel.setVisibility(View.VISIBLE);
                mDockLevel.setText(formatInteger(intent.getIntExtra("dock_level", 0)));

                mRowDockStatus.setVisibility(View.VISIBLE);
                int dockStatus = intent.getIntExtra("dock_status", Constants.DOCK_STATE_UNKNOWN);
                switch (dockStatus) {
                    case Constants.DOCK_STATE_UNDOCKED:
                        mDockStatus.setText(R.string.battery_info_dock_status_undocked);
                        break;
                    case Constants.DOCK_STATE_DOCKED:
                        mDockStatus.setText(R.string.battery_info_dock_status_docked);
                        break;
                    case Constants.DOCK_STATE_CHARGING:
                        mDockStatus.setText(R.string.battery_info_dock_status_charging);
                        break;
                    case Constants.DOCK_STATE_DISCHARGING:
                        mDockStatus.setText(R.string.battery_info_dock_status_discharging);
                        break;
                    default:
                        mDockStatus.setText(R.string.unknown);
                        break;
                }

                mRowDockLastConnected.setVisibility(View.VISIBLE);
                String dockLastConnected;
                if (dockStatus >= Constants.DOCK_STATE_CHARGING) {
                    dockLastConnected = "--";
                } else if (BatteryLevel.dockLastConnected == null) {
                    dockLastConnected = context.getString(R.string.unknown);
                } else {
                    dockLastConnected = DateFormat.getDateTimeInstance().format(BatteryLevel.dockLastConnected);
                }
                mDockLastConnected.setText(dockLastConnected);
            }
        }
    };

    public final View.OnClickListener batterySummaryListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent("android.intent.action.POWER_USAGE_SUMMARY"));
        }
    };

    private String formatInteger(int value) {
        return NumberFormat.getIntegerInstance().format(value);
    }

    private String formatTenths(int value) {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMinimumFractionDigits(1);
        format.setMaximumFractionDigits(1);
        return format.format(value / 10.0);
    }

    public int getMenuTitle() {
        return tempUnitsC
                ? R.string.battery_info_temperature_units_c
                : R.string.battery_info_temperature_units_f;
    }

    public void updateTemperature() {
        int tempVal = temperature;
        if (!tempUnitsC)
            tempVal = tempVal * 9 / 5 + 320;
        mTemperature.setText(getString(R.string.battery_info_temperature_value,
                formatTenths(tempVal), getString(getMenuTitle())));
    }

    public boolean onMenuItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.temperature) {
            tempUnitsC = !tempUnitsC;
            requireActivity().invalidateOptionsMenu();
            updateTemperature();
            WidgetSettingsContainer.setTempUnits(requireContext(), appWidgetId, tempUnitsC);
            return true;
        }
        return false;
    }
}
