package vip.smartfamily.vfs.ui.smb_file

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.share.DiskShare
import kotlinx.coroutines.*
import vip.smartfamily.vfs.R
import vip.smartfamily.vfs.data.smb.SmbFileInfo
import vip.smartfamily.vfs.data.smb.SmbFileTree
import vip.smartfamily.vfs.db.entity.SmbConInfo
import vip.smartfamily.vfs.db.repository.SmbConRepository
import vip.smartfamily.vfs.ui.smb_file.fragment.inter.TopClickListener
import vip.smartfamily.vfs.ui.smb_file.my_view.DialogFileChoice

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

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val smbFileInfo = smbFileList[position]
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
                            for (fileInfo in share.list("", "*")) {
                                if ("." != fileInfo.fileName && ".." != fileInfo.fileName) {
                                    smbFileInfo.fileTrees
                                            ?: synchronized(this@FolderRecyclerAdapter) {
                                                smbFileInfo.fileTrees = ArrayList()
                                            }
                                    val smbFileTree = SmbFileTree("", fileInfo, null)
                                    smbFileInfo.fileTrees?.add(smbFileTree)
                                }
                            }
                        }

                        withContext(Dispatchers.Main) {
                            iconView.setOnClickListener {
                                topClickListener.onClickDisk(smbFileInfo.diskShare!!, smbFileInfo.fileTrees!!)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            statusView.setBackgroundResource(R.drawable.ic_folder_status_false)
                        }
                    }
                }
            }

            detailsView.visibility = View.VISIBLE
            detailsView.setOnClickListener {
                val dialogFileChoice = DialogFileChoice(itemView.context, DialogFileChoice.VFS_DISK, smbFileInfo)
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

    /**
     * 修改连接
     * [smbFileInfo] smb文件信息
     */
    private fun initReconDialog(context: Context, smbFileInfo: SmbFileInfo) {
        val smbConInfo = SmbConRepository.getInstance().getData(smbFileInfo.smbConInfo.ip, smbFileInfo.smbConInfo.path)
        smbConInfo?.let { smbInfo ->
            val layoutInflater = LayoutInflater.from(context)
            val reconView = layoutInflater.inflate(R.layout.dialog_add_con, null)
            val dialogBuilder = AlertDialog.Builder(context)
            dialogBuilder.setView(reconView)
            val reconDialog = dialogBuilder.create()
            reconDialog.setCancelable(false)
            // 背景透明
            reconDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            reconDialog.show()

            val ipEditText = reconView.findViewById<TextInputEditText>(R.id.tiet_dia_ip)
            ipEditText.setText(smbInfo.ip)
            val ipTextLayout = reconView.findViewById<TextInputLayout>(R.id.til_dia_ip)

            val userEditText = reconView.findViewById<TextInputEditText>(R.id.tiet_dia_user)
            userEditText.setText(smbInfo.user)
            val userTextLayout = reconView.findViewById<TextInputLayout>(R.id.til_dia_user)

            val pawEditText = reconView.findViewById<TextInputEditText>(R.id.tiet_dia_paw)
            pawEditText.setText(smbInfo.paw)
            val pawTextLayout = reconView.findViewById<TextInputLayout>(R.id.til_dia_paw)

            val pathEditText = reconView.findViewById<TextInputEditText>(R.id.tiet_dia_path)
            pathEditText.setText(smbInfo.path)
            val pathTextLayout = reconView.findViewById<TextInputLayout>(R.id.til_dia_path)

            val certainButton = reconView.findViewById<TextView>(R.id.tv_dia_certain)
            certainButton.setOnClickListener {
                certainButton.isEnabled = false
                var b = true
                if (ipEditText.text.toString().isEmpty()) {
                    ipTextLayout.error = context.resources.getString(R.string.ip_not_null)
                    b = false
                } else {
                    ipTextLayout.error = null
                }

                if (userEditText.text.toString().isEmpty()) {
                    userTextLayout.error = context.resources.getString(R.string.user_not_null)
                    b = false
                } else {
                    userTextLayout.error = null
                }

                if (pawEditText.text.toString().isEmpty()) {
                    pawTextLayout.error = context.resources.getString(R.string.paw_not_null)
                    b = false
                } else {
                    pawTextLayout.error = null
                }

                if (pathEditText.text.toString().isEmpty()) {
                    pathTextLayout.error = context.resources.getString(R.string.path_not_null)
                    b = false
                } else {
                    pathTextLayout.error = null
                }
                if (b) {
                    smbInfo.path = pathEditText.text.toString()
                    smbInfo.ip = ipEditText.text.toString()
                    smbInfo.user = userEditText.text.toString()
                    smbInfo.paw = pawEditText.text.toString()
                    smbInfo.path = pathEditText.text.toString()
                    SmbConRepository.getInstance().upData(smbInfo)
                    reconDialog.dismiss()
                }
            }

            reconView.findViewById<TextView>(R.id.tv_dia_cancel).setOnClickListener {
                reconDialog.dismiss()
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
