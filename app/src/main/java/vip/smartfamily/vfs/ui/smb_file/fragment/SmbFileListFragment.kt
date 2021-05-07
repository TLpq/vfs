package vip.smartfamily.vfs.ui.smb_file.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hierynomus.smbj.share.DiskShare
import vip.smartfamily.vfs.R
import vip.smartfamily.vfs.data.smb.SmbFileTree
import vip.smartfamily.vfs.ui.smb_file.fragment.inter.TopClickListener

class SmbFileListFragment(
        private val diskShare: DiskShare,
        private val smbFileInfoList: ArrayList<SmbFileTree>,
        private val topClickListener: TopClickListener
) : Fragment() {
    private var fragmentView: View? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView ?: synchronized(this) {
            fragmentView ?: inflater.inflate(R.layout.fragment_file_list, container, false).also {
                fragmentView = it
            }
        }

        return fragmentView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initRecyclerView()
    }

    private fun initRecyclerView() {
        val recyclerView = fragmentView!!.findViewById<RecyclerView>(R.id.rv_fragment_file_list)
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = staggeredGridLayoutManager
        val smbFileListFragment = SmbFileRecycAdapter(activity, diskShare, smbFileInfoList, topClickListener)
        recyclerView.adapter = smbFileListFragment
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }
}