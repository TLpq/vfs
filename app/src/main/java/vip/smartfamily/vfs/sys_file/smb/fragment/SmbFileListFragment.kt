package vip.smartfamily.vfs.sys_file.smb.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hierynomus.smbj.share.DiskShare
import vip.smartfamily.vfs.R
import vip.smartfamily.vfs.data.smb.SmbFileTree
import vip.smartfamily.vfs.sys_file.smb.fragment.inter.TopClickListener

/**
 * 非磁盘文件处理模块
 */
class SmbFileListFragment(
        private val diskShare: DiskShare,
        private val smbFileInfoList: ArrayList<SmbFileTree>,
        private val topClickListener: TopClickListener
) : Fragment() {
    private var fragmentView: View? = null

    private lateinit var smbFileListFragment: SmbFileRecyclerAdapter

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
        smbFileListFragment = object : SmbFileRecyclerAdapter(diskShare, smbFileInfoList, topClickListener) {
            override fun onWriteStorage() {
                permissionDetect()
            }
        }
        recyclerView.adapter = smbFileListFragment
    }

    /**
     * 检测是否有用外部存储读取权限
     */
    private fun permissionDetect() {
        context?.let {
            when {
                it.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                    smbFileListFragment.showFolderChoiceDialog()
                }

                showQuest() -> {

                }

                else -> {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 102)
                }
            }
        }
    }

    /**
     * 说明理由
     */
    private fun showQuest(): Boolean {
        return false
    }

    /**
     * 授权回调
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 102) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                smbFileListFragment.showFolderChoiceDialog()
            } else {
                toastInfo(resources.getString(R.string.toast_no_permission))
            }
        }
    }

    private var toast: Toast? = null
    private fun toastInfo(info: String) {
        toast?.cancel()
        toast?.setText(info)

        toast ?: synchronized(this) {
            toast ?: Toast.makeText(context, info, Toast.LENGTH_SHORT).also {
                toast = it
            }
        }

        toast?.show()
    }
}