package vip.smartfamily.vfs.ui.smb_file

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
import vip.smartfamily.vfs.ui.smb_file.fragment.inter.TopClickListener
import vip.smartfamily.vfs.ui.smb_file.my_view.DialogFileChoice

/**
 * SMB连接列表适配器
 */
class FolderRecyclerAdapter(
        folderList: List<SmbConInfo>,
        private val topClickListener: TopClickListener
) : RecyclerView.Adapter<FolderViewHolder>() {

    private val smbFileList = ArrayList<SmbFileInfo>()

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

    var index = 0;

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val smbFileInfo = smbFileList[position]
        Log.e("onBindViewHolder","调用onBindViewHolder：${++index}")
        holder.run {
            nameView.text = smbFileInfo.smbConInfo.name
            runBlocking {
                GlobalScope.launch() {
                    try {
                        val client = SMBClient()
                        smbFileInfo.smbConInfo.run {
                            val connection = client.connect(ip)
                            val ac = AuthenticationContext(user, paw.toCharArray(), ip)
                            val session = connection.authenticate(ac)
                            val share = session.connectShare(path) as DiskShare
                            smbFileInfo.diskShare = share
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

                        withContext(Dispatchers.Main) {
                            statusView.setBackgroundResource(R.drawable.ic_folder_status_true)
                            iconView.setOnClickListener {
                                topClickListener.onClickDisk(smbFileInfo.diskShare!!, smbFileInfo.fileTrees!!)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("onBindViewHolder","${e.message}")
                    }
                }
            }

            detailsView.visibility = View.VISIBLE
            detailsView.setOnClickListener {
                val dialogFileChoice = object : DialogFileChoice(itemView.context, DialogFileChoice.VFS_DISK, smbFileInfo) {
                    override fun onDownload() = false
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
