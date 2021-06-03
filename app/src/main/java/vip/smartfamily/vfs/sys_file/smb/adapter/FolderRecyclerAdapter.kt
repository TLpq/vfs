package vip.smartfamily.vfs.sys_file.smb.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.share.DiskShare
import kotlinx.coroutines.*
import vip.smartfamily.vfs.R
import vip.smartfamily.vfs.data.smb.SmbFileInfo
import vip.smartfamily.vfs.data.smb.SmbFileTree
import vip.smartfamily.vfs.db.entity.SmbConInfo
import vip.smartfamily.vfs.sys_file.smb.fragment.inter.TopClickListener
import vip.smartfamily.vfs.sys_file.smb.ui.my_view.DialogFileChoice

/**
 * SMB连接列表适配器
 */
class FolderRecyclerAdapter(
        folderList: List<SmbConInfo>,
        private val topClickListener: TopClickListener
) : RecyclerView.Adapter<FolderViewHolder>() {

    private val smbFileList = ArrayList<SmbFileInfo>()

    private val smbConnectList = ArrayList<DiskShare>()

    init {
        for (folder in folderList) {
            val smbFileInfo = SmbFileInfo(null, folder, null)
            this.smbFileList.add(smbFileInfo)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val folderView = LayoutInflater.from(parent.context).inflate(
                R.layout.item_file_list,
                parent,
                false
        )

        return FolderViewHolder(folderView)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val smbFileInfo = smbFileList[position]
        holder.run {
            nameView.text = smbFileInfo.smbConInfo.name
            runBlocking {
                GlobalScope.launch() {
                    try {
                        // 连接SMB
                        val client = SMBClient()
                        val connection = client.connect(smbFileInfo.smbConInfo.ip)
                        val ac = AuthenticationContext(smbFileInfo.smbConInfo.user, smbFileInfo.smbConInfo.paw.toCharArray(), smbFileInfo.smbConInfo.ip)
                        val session = connection.authenticate(ac)
                        val share = session.connectShare(smbFileInfo.smbConInfo.path) as DiskShare
                        smbFileInfo.diskShare = share
                        smbConnectList.add(share)

                        withContext(Dispatchers.Main) {
                            statusView.setBackgroundResource(R.drawable.ic_folder_status_true)
                            iconView.setOnClickListener {
                                runBlocking {
                                    val job = GlobalScope.launch {
                                        smbFileInfo.smbConInfo.run {
                                            smbFileInfo.fileTrees?.clear()
                                            for (fileInfo in share.list("", "*")) {
                                                if ("." != fileInfo.fileName && ".." != fileInfo.fileName) {
                                                    smbFileInfo.fileTrees
                                                            ?: synchronized(FolderRecyclerAdapter::class) {
                                                                smbFileInfo.fileTrees = ArrayList()
                                                            }
                                                    val smbFileTree = SmbFileTree("", fileInfo, null)
                                                    smbFileInfo.fileTrees?.add(smbFileTree)
                                                }
                                            }
                                        }
                                    }
                                    job.join()
                                    // 显示下一级
                                    topClickListener.onClickDisk(smbFileInfo.diskShare!!, smbFileInfo.fileTrees!!)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("onBindViewHolder", "${e.message}")
                    }
                }
            }

            detailsView.visibility = View.VISIBLE
            detailsView.setOnClickListener {
                val dialogFileChoice = object : DialogFileChoice(itemView.context, DialogFileChoice.VFS_DISK, smbFileInfo) {
                    override fun onCheckPermission() {

                    }

                    override fun onDownload(loadPath: String?) {

                    }
                }
                dialogFileChoice.show()
            }
        }
    }

    override fun getItemCount() = smbFileList.size

    fun add(smbConInfo: SmbConInfo) {
        val smbFileInfo = SmbFileInfo(null, smbConInfo, null)
        smbFileList.add(smbFileInfo)
        notifyDataSetChanged()
    }

    fun delete(position: Int) {
        try {
            smbFileList.removeAt(position)
        } catch (e: Exception) {
        }
        notifyDataSetChanged()
    }

    fun clear() {
        for (connectInfo in smbConnectList){
            try {
                connectInfo.close()
            } catch (e: Exception) {
            }
        }
    }
}

class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var nameView: TextView
    var statusView: View
    var detailsView: View
    var iconView: ImageView

    init {
        itemView.run {
            nameView = findViewById(R.id.tv_item_folder_name)
            statusView = findViewById(R.id.v_item_status)
            detailsView = findViewById(R.id.v_item_details)
            iconView = findViewById(R.id.iv_item_folder)
        }
    }
}
