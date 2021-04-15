package vip.smartfamily.vfs.ui.smb_file

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Gravity
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
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.*
import vip.smartfamily.vfs.R
import vip.smartfamily.vfs.data.smb.SmbFileInfo
import vip.smartfamily.vfs.data.smb.SmbFileTree
import vip.smartfamily.vfs.db.entity.SmbConInfo
import vip.smartfamily.vfs.db.repository.SmbConRepository
import vip.smartfamily.vfs.ui.smb_file.fragment.inter.TopClickListener

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
                Log.e("点击", "点击详情$position")
                val addConView = LayoutInflater.from(itemView.context).inflate(R.layout.dialog_file_info, null)
                val dialog = Dialog(itemView.context, R.style.style_dialog)
                dialog.setContentView(addConView)
                dialog.window?.setGravity(Gravity.BOTTOM)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.window?.setWindowAnimations(R.style.dialog_animation)
                dialog.show()
                //val att = dialog.window?.attributes
                //att?.x = 0
                //att?.y = 0
                //att?.width = itemView.context.resources.getDimension(R.dimen.dp_360).toInt()
                //att?.height = itemView.context.resources.getDimension(R.dimen.dp_360).toInt()
                //dialog.window?.attributes = att

                //val layoutParams = addConView.layoutParams
                //layoutParams.height = itemView.context.resources.getDimension(R.dimen.dp_1).toInt()
                //layoutParams.width = itemView.context.resources.getDimension(R.dimen.dp_1).toInt()
                //addConView.layoutParams = layoutParams

                /*val smbConInfo = SmbConRepository.getInstance().getData(smbFileInfo.smbConInfo.ip, smbFileInfo.smbConInfo.path)
                smbConInfo?.let { smbInfo ->
                    val layoutInflater = LayoutInflater.from(itemView.context)
                    val addConView = layoutInflater.inflate(R.layout.dialog_add_con, null)
                    val dialogBuilder = AlertDialog.Builder(itemView.context)
                    dialogBuilder.setView(addConView)
                    val dialog = dialogBuilder.create()
                    dialog.setCancelable(false)
                    // 背景透明
                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.show()

                    val ipEditText = addConView.findViewById<TextInputEditText>(R.id.tiet_dia_ip)
                    ipEditText.setText(smbInfo.ip)
                    val ipTextLayout = addConView.findViewById<TextInputLayout>(R.id.til_dia_ip)

                    val userEditText = addConView.findViewById<TextInputEditText>(R.id.tiet_dia_user)
                    userEditText.setText(smbInfo.user)
                    val userTextLayout = addConView.findViewById<TextInputLayout>(R.id.til_dia_user)

                    val pawEditText = addConView.findViewById<TextInputEditText>(R.id.tiet_dia_paw)
                    pawEditText.setText(smbInfo.paw)
                    val pawTextLayout = addConView.findViewById<TextInputLayout>(R.id.til_dia_paw)

                    val pathEditText = addConView.findViewById<TextInputEditText>(R.id.tiet_dia_path)
                    pathEditText.setText(smbInfo.path)
                    val pathTextLayout = addConView.findViewById<TextInputLayout>(R.id.til_dia_path)

                    val certainButton = addConView.findViewById<TextView>(R.id.tv_dia_certain)
                    certainButton.setOnClickListener {
                        certainButton.isEnabled = false
                        var b = true
                        if (ipEditText.text.toString().isEmpty()) {
                            ipTextLayout.error = itemView.context.resources.getString(R.string.ip_not_null)
                            b = false
                        } else {
                            ipTextLayout.error = null
                        }

                        if (userEditText.text.toString().isEmpty()) {
                            userTextLayout.error = itemView.context.resources.getString(R.string.user_not_null)
                            b = false
                        } else {
                            userTextLayout.error = null
                        }

                        if (pawEditText.text.toString().isEmpty()) {
                            pawTextLayout.error = itemView.context.resources.getString(R.string.paw_not_null)
                            b = false
                        } else {
                            pawTextLayout.error = null
                        }

                        if (pathEditText.text.toString().isEmpty()) {
                            pathTextLayout.error = itemView.context.resources.getString(R.string.path_not_null)
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
                            dialog.dismiss()
                        }
                    }

                    addConView.findViewById<TextView>(R.id.tv_dia_cancel).setOnClickListener {
                        dialog.dismiss()
                    }
                }*/
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
