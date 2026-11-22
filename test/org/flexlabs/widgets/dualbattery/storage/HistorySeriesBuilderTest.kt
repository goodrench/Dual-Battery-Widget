package org.flexlabs.widgets.dualbattery.storage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistorySeriesBuilderTest {
    @Test
    fun `sorts readings and compresses repeated values`() {
        val history = HistorySeriesBuilder.build(
            listOf(
                BatteryHistoryEntry(3_000, 70, 0, -1),
                BatteryHistoryEntry(1_000, 80, 0, -1),
                BatteryHistoryEntry(2_000, 80, 0, -1)
            ),
            dockSupported = false,
            now = 10_000
        )

        assertEquals(
            listOf(ChartPoint(1_000, 80), ChartPoint(3_000, 70)),
            history.main
        )
        assertTrue(history.dock.isEmpty())
    }

    @Test
    fun `adds a final point after trailing repeated values`() {
        val history = HistorySeriesBuilder.build(
            listOf(
                BatteryHistoryEntry(1_000, 80, 0, -1),
                BatteryHistoryEntry(2_000, 80, 0, -1)
            ),
            dockSupported = false,
            now = 10_000
        )

        assertEquals(
            listOf(ChartPoint(1_000, 80), ChartPoint(2_000, 80)),
            history.main
        )
    }

    @Test
    fun `excludes disconnected dock readings and compresses connected values`() {
        val history = HistorySeriesBuilder.build(
            listOf(
                BatteryHistoryEntry(1_000, 90, 1, 60),
                BatteryHistoryEntry(2_000, 90, 2, 50),
                BatteryHistoryEntry(3_000, 90, 3, 50),
                BatteryHistoryEntry(4_000, 90, 4, 40)
            ),
            dockSupported = true,
            now = 10_000
        )

        assertEquals(
            listOf(ChartPoint(2_000, 50), ChartPoint(4_000, 40)),
            history.dock
        )
    }

    @Test
    fun `adds a dock endpoint after trailing repeated values`() {
        val history = HistorySeriesBuilder.build(
            listOf(
                BatteryHistoryEntry(1_000, 90, 2, 50),
                BatteryHistoryEntry(2_000, 90, 3, 50)
            ),
            dockSupported = true,
            now = 10_000
        )

        assertEquals(
            listOf(ChartPoint(1_000, 50), ChartPoint(2_000, 50)),
            history.dock
        )
    }

    @Test
    fun `returns empty series without readings`() {
        val history = HistorySeriesBuilder.build(emptyList(), true, 10_000)

        assertTrue(history.main.isEmpty())
        assertTrue(history.dock.isEmpty())
    }
}
