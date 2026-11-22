package org.flexlabs.widgets.dualbattery.widgetsettings

import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.view.MenuItem
import org.flexlabs.widgets.dualbattery.BatteryWidgetUpdater
import org.flexlabs.widgets.dualbattery.R
import org.flexlabs.widgets.dualbattery.app.AboutFragment
import org.flexlabs.widgets.dualbattery.app.FeedbackFragment
import org.flexlabs.widgets.dualbattery.app.TabbedFragmentsActivity
import org.flexlabs.widgets.dualbattery.service.MonitorService
import org.flexlabs.widgets.dualbattery.ui.SystemBarInsets

class WidgetActivity : TabbedFragmentsActivity() {
    @JvmField
    var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    private lateinit var batteryInfoFragment: BatteryInfoFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MonitorService.start(this)
        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        batteryInfoFragment = BatteryInfoFragment()
        setupTabs(
            listOf(batteryInfoFragment, PropertiesFragment(), FeedbackFragment(), AboutFragment()),
            listOf(
                getString(R.string.propHeader_BatteryInfo),
                getString(R.string.propHeader_Properties),
                getString(R.string.propHeader_Feedback),
                getString(R.string.propHeader_About)
            )
        )
        SystemBarInsets.apply(this)

        if (WidgetSettingsContainer.getUpgradeSwappedSingle(this, appWidgetId)) {
            AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.alert_just_swapped)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    override fun onPause() {
        super.onPause()
        BatteryWidgetUpdater.updateWidget(this, appWidgetId)
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        batteryInfoFragment.onMenuItemSelected(item) || super.onOptionsItemSelected(item)
}
