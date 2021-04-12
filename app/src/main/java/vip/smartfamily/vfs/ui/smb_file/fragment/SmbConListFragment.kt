package vip.smartfamily.vfs.ui.smb_file.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import vip.smartfamily.vfs.R
import vip.smartfamily.vfs.db.repository.SmbConRepository
import vip.smartfamily.vfs.ui.smb_file.FolderRecyclerAdapter
import vip.smartfamily.vfs.ui.smb_file.fragment.inter.TopClickListener

class SmbConListFragment(
        private val topClickListener: TopClickListener
) : Fragment() {

    private var fragment: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragment?: synchronized(this){
            fragment?:inflater.inflate(R.layout.fragment_file_list, container, false).also {
                fragment = it
                val recyclerView = it.findViewById<RecyclerView>(R.id.rv_fragment_file_list)
                val staggeredGridLayoutManager = StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL)
                recyclerView.layoutManager = staggeredGridLayoutManager
                val folderRecyclerAdapter = FolderRecyclerAdapter(SmbConRepository.getInstance().gatAllConList(), topClickListener)
                recyclerView.adapter = folderRecyclerAdapter
            }
        }

        return fragment
    }
}