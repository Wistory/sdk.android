package ru.vvdev.wistory.internal.presentation.items

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import java.util.ArrayList

internal class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private var fragmentList = ArrayList<Fragment>()

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getCount(): Int {
        return fragmentList.size
    }

    fun updateFragment(position: Int, fragment: Fragment) {
        fragmentList[position] = fragment
    }

    fun addFragments(fragment: Fragment) {
        fragmentList.add(fragment)
    }
}
