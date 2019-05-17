package pl.qwisdom.simpleradio.view.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class ViewPagerAdapter(manager: FragmentManager)
    : FragmentStatePagerAdapter(manager) {

    private val fragments = ArrayList<Fragment>()

    override fun getItem(p0: Int): Fragment = fragments[p0]

    override fun getCount(): Int = fragments.size

    fun addFragment(fragment: Fragment) = fragments.add(fragment)
}