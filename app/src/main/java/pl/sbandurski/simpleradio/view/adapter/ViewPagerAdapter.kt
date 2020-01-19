package pl.sbandurski.simpleradio.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class ViewPagerAdapter(manager: FragmentManager)
    : FragmentStatePagerAdapter(manager) {

    private val fragments = ArrayList<Fragment>()

    override fun getItem(p0: Int): Fragment = fragments[p0]

    override fun getCount(): Int = fragments.size

    fun addFragment(fragment: Fragment) = fragments.add(fragment)
}