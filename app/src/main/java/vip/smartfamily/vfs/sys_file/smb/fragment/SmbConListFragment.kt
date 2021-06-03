package vip.smartfamily.vfs.sys_file.smb.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import vip.smartfamily.vfs.R
import vip.smartfamily.vfs.db.repository.SmbConRepository
import vip.smartfamily.vfs.sys_file.smb.adapter.FolderRecyclerAdapter
import vip.smartfamily.vfs.sys_file.smb.fragment.inter.TopClickListener

/**
 * ¶à²ãÎÄ¼þ¼ÐÇ¶Ì×ËéÆ¬
 */
class SmbConListFragment(
        private val topClickListener: TopClickListener
) : Fragment() {

    private var fragment: View? = null
    private lateinit var folderRecyclerAdapter: FolderRecyclerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragment ?: synchronized(this) {
            fragment ?: inflater.inflate(R.layout.fragment_file_list, container, false).also {
                fragment = it
                val recyclerView = it.findViewById<RecyclerView>(R.id.rv_fragment_file_list)
                val staggeredGridLayoutManager = StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL)
                recyclerView.layoutManager = staggeredGridLayoutManager
                folderRecyclerAdapter = FolderRecyclerAdapter(SmbConRepository.getInstance().gatAllConList(), topClickListener)
                recyclerView.adapter = folderRecyclerAdapter
            }
        }

        return fragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        folderRecyclerAdapter.clear()
    }
}