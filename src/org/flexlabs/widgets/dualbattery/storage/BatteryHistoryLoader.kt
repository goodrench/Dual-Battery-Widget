package org.flexlabs.widgets.dualbattery.storage

import android.content.Context
import org.flexlabs.widgets.dualbattery.BatteryLevel

object BatteryHistoryLoader {
    @JvmStatic
    fun load(context: Context, days: Int): BatteryHistory {
        val adapter = BatteryLevelAdapter(context)
        adapter.openRead()
        val entries = mutableListOf<BatteryHistoryEntry>()
        try {
            adapter.getRecentEntries(days).use { cursor ->
                while (cursor.moveToNext()) {
                    entries.add(BatteryHistoryEntry(
                        cursor.getLong(BatteryLevelAdapter.ORD_TIME),
                        cursor.getInt(BatteryLevelAdapter.ORD_LEVEL),
                        cursor.getInt(BatteryLevelAdapter.ORD_DOCK_STATUS),
                        cursor.getInt(BatteryLevelAdapter.ORD_DOCK_LEVEL)
                    ))
                }
            }
        } finally {
            adapter.close()
        }
        return HistorySeriesBuilder.build(
            entries,
            BatteryLevel.getCurrent()?.is_dockFriendly() == true,
            System.currentTimeMillis()
        )
    }
}
