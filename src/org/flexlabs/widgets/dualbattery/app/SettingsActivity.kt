package org.flexlabs.widgets.dualbattery.app

import android.os.Bundle
import org.flexlabs.widgets.dualbattery.R
import org.flexlabs.widgets.dualbattery.ui.SystemBarInsets

class SettingsActivity : TabbedFragmentsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupTabs(
            listOf(SettingsFragment(), FeedbackFragment(), AboutFragment()),
            listOf(
                getString(R.string.settings),
                getString(R.string.propHeader_Feedback),
                getString(R.string.propHeader_About)
            )
        )
        SystemBarInsets.apply(this)
    }
}
