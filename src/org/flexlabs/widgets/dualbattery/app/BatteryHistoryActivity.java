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

package org.flexlabs.widgets.dualbattery.app;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayout;
import org.flexlabs.widgets.dualbattery.R;
import org.flexlabs.widgets.dualbattery.service.MonitorService;
import org.flexlabs.widgets.dualbattery.storage.BatteryHistory;
import org.flexlabs.widgets.dualbattery.storage.BatteryHistoryLoader;
import org.flexlabs.widgets.dualbattery.ui.BatteryHistoryChart;
import org.flexlabs.widgets.dualbattery.ui.SystemBarInsets;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BatteryHistoryActivity extends AppCompatActivity {
    private final ExecutorService historyExecutor = Executors.newSingleThreadExecutor();
    private BatteryHistoryChart chart;
    private int days = 3;
    private int defaultDays;
    private int historyLoadGeneration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MonitorService.start(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { android.Manifest.permission.POST_NOTIFICATIONS }, 0);
        }

        setContentView(R.layout.battery_history);
        chart = (BatteryHistoryChart) findViewById(R.id.chart);
        defaultDays = new SettingsContainer(this).getDefaultDaysTab();

        TabLayout tabs = (TabLayout) findViewById(R.id.history_tabs);
        addTab(tabs, 3, R.string.sett_defaultDaysTab_3);
        addTab(tabs, 7, R.string.sett_defaultDaysTab_7);
        addTab(tabs, 30, R.string.sett_defaultDaysTab_30);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                days = (Integer) tab.getTag();
                loadHistory();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
        for (int i = 0; i < tabs.getTabCount(); i++) {
            TabLayout.Tab tab = tabs.getTabAt(i);
            if ((Integer) tab.getTag() == defaultDays) {
                tabs.selectTab(tab);
                break;
            }
        }
        SystemBarInsets.apply(this);
    }

    private void addTab(TabLayout tabs, int days, int label) {
        tabs.addTab(tabs.newTab().setText(label).setTag(days));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        final int requestedDays = days;
        final int generation = ++historyLoadGeneration;
        historyExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final BatteryHistory history = BatteryHistoryLoader.load(getApplicationContext(), requestedDays);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isDestroyed() || generation != historyLoadGeneration)
                            return;
                        chart.setData(history, requestedDays);
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        historyLoadGeneration++;
        historyExecutor.shutdownNow();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
