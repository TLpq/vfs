package vip.smartfamily.vfs.sys_file.smb.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerFragmentStateAdapter(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
        private val fragmentList: ArrayList<Fragment>
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount() = fragmentList.size

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    fun addFragment(fragment: Fragment) {
        fragmentList.add(fragment)
        notifyDataSetChanged()
    }

    fun delFragment(index: Int) {
        if (index < itemCount) {
            fragmentList.removeAt(index)
            notifyDataSetChanged()
        }
    }
}