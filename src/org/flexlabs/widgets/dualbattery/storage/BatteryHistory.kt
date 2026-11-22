package org.flexlabs.widgets.dualbattery.storage

data class ChartPoint(val time: Long, val level: Int)

data class BatteryHistory(
    val main: List<ChartPoint>,
    val dock: List<ChartPoint>
)

data class BatteryHistoryEntry(
    val time: Long,
    val level: Int,
    val dockStatus: Int,
    val dockLevel: Int
)

object HistorySeriesBuilder {
    @JvmStatic
    fun build(
        entries: List<BatteryHistoryEntry>,
        dockSupported: Boolean,
        now: Long
    ): BatteryHistory {
        val main = mutableListOf<ChartPoint>()
        val dock = mutableListOf<ChartPoint>()
        var oldLevel = -1
        var oldDockLevel = -1
        var time = now
        var mainSkipped = false
        var dockSkipped = false
        var dockConnected = false

        for (entry in entries.sortedBy { it.time }) {
            time = entry.time
            mainSkipped = entry.level == oldLevel
            if (!mainSkipped) {
                main.add(ChartPoint(entry.time, entry.level))
                oldLevel = entry.level
            }

            val connected = dockSupported && entry.dockStatus > 1
            if (connected && (!dockConnected || entry.dockLevel != oldDockLevel)) {
                dock.add(ChartPoint(entry.time, entry.dockLevel))
                oldDockLevel = entry.dockLevel
                dockSkipped = false
            } else {
                dockSkipped = connected
            }
            dockConnected = connected
        }
        if (mainSkipped) main.add(ChartPoint(time, oldLevel))
        if (dockSkipped) dock.add(ChartPoint(time, oldDockLevel))
        return BatteryHistory(main, dock)
    }
}
