package org.flexlabs.widgets.dualbattery.app

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter as ViewPagerFragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.flexlabs.widgets.dualbattery.R

abstract class TabbedFragmentsActivity : AppCompatActivity(), AdapterView.OnItemClickListener {
    private var fragments: List<Fragment> = emptyList()
    private var titles: List<String> = emptyList()
    private var list: ListView? = null
    private var currentTab = -1

    protected fun setupTabs(fragments: List<Fragment>, titles: List<String>) {
        require(fragments.size == titles.size)
        this.fragments = fragments
        this.titles = titles

        val screenLayout = resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        if (screenLayout > Configuration.SCREENLAYOUT_SIZE_LARGE) {
            setContentView(R.layout.preference_list_large)
            list = findViewById<ListView>(android.R.id.list).apply {
                choiceMode = ListView.CHOICE_MODE_SINGLE
                onItemClickListener = this@TabbedFragmentsActivity
                adapter = ArrayAdapter(
                    this@TabbedFragmentsActivity,
                    android.R.layout.simple_list_item_activated_1,
                    titles
                )
            }
            selectTab(0)
        } else {
            setContentView(R.layout.widgetsettings)
            val pager = findViewById<ViewPager2>(R.id.pager)
            pager.adapter = PagerTabAdapter()
            val tabs = findViewById<TabLayout>(R.id.indicator)
            TabLayoutMediator(tabs, pager) { tab, position ->
                tab.text = titles[position]
            }.attach()
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectTab(position)
    }

    private fun selectTab(position: Int) {
        if (currentTab == position) return
        currentTab = position
        supportFragmentManager.beginTransaction()
            .setTransition(androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.prefs, fragments[position])
            .commit()
        list?.setItemChecked(position, true)
    }

    private inner class PagerTabAdapter : ViewPagerFragmentStateAdapter(this) {
        override fun createFragment(position: Int): Fragment = fragments[position]
        override fun getItemCount(): Int = fragments.size
    }
}
