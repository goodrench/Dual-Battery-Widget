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

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import org.flexlabs.widgets.dualbattery.BatteryLevel;
import org.flexlabs.widgets.dualbattery.Constants;
import org.flexlabs.widgets.dualbattery.R;
import org.flexlabs.widgets.dualbattery.storage.PreferenceValues;

public class PropertiesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        WidgetActivity activity = (WidgetActivity) requireActivity();
        String preferencesName = Constants.SETTINGS_WIDGET_FILE + activity.appWidgetId;
        getPreferenceManager().setSharedPreferencesName(preferencesName);
        android.content.SharedPreferences preferences = requireContext().getSharedPreferences(
                preferencesName, android.content.Context.MODE_PRIVATE);
        PreferenceValues.migrateToString(preferences, Constants.SETTING_TEXT_POS);
        PreferenceValues.migrateToString(preferences, Constants.SETTING_SHOW_SELECTION);
        PreferenceValues.migrateToString(preferences, Constants.SETTING_TEXT_COLOR);
        PreferenceValues.migrateToString(preferences, Constants.SETTING_MARGIN);
        addPreferencesFromResource(R.xml.widget_properties_general);
        BatteryLevel level = BatteryLevel.getCurrent();
        if (level != null && level.is_dockFriendly()) {
            addPreferencesFromResource(R.xml.widget_properties_dock);
        }
    }
}
