package vip.smartfamily.vfs.ui.smb_file.my_view

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.storage.StorageManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.*
import vip.smartfamily.vfs.R
import vip.smartfamily.vfs.data.smb.SmbFileInfo
import vip.smartfamily.vfs.data.smb.SmbFileTree
import vip.smartfamily.vfs.db.entity.SmbConInfo
import vip.smartfamily.vfs.db.repository.SmbConRepository
import vip.smartfamily.vfs.entity.LocalFolder
import vip.smartfamily.vfs.ui.smb_file.adapter.LocalFolderRecyclerAdapter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 下载文件夹选择器
 */

abstract class DialogFileChoice : Dialog, View.OnClickListener {

    private var code = ""
    private var smbFileInfo: SmbFileInfo? = null
    private var smbFileTree: SmbFileTree? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, code: String, smbFileInfo: SmbFileInfo) : super(context, R.style.style_dialog) {
        this.code = code
        this.smbFileInfo = smbFileInfo
    }

    constructor(context: Context, code: String, smbFileTree: SmbFileTree) : super(context, R.style.style_dialog) {
        this.code = code
        this.smbFileTree = smbFileTree
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_file_info, null).apply {
            val reconView = findViewById<ConstraintLayout>(R.id.cl_dia_file_recon)
            val renameView = findViewById<ConstraintLayout>(R.id.cl_dia_file_rename)
            val downloadView = findViewById<ConstraintLayout>(R.id.cl_dia_file_download)
            reconView.setOnClickListener(this@DialogFileChoice)
            renameView.setOnClickListener(this@DialogFileChoice)

            val nameTextView = findViewById<TextView>(R.id.tv_dia_file_name)
            val msgTextView = findViewById<TextView>(R.id.tv_dia_file_date)

            when {
                // 磁盘文件
                smbFileInfo != null -> {
                    nameTextView.text = smbFileInfo!!.smbConInfo.name
                    GlobalScope.launch(Dispatchers.IO) {
                        smbFileInfo?.diskShare?.shareInformation?.let {
                            var total = it.totalSpace
                            var totalUnit = "B"
                            var free = it.callerFreeSpace
                            var freeUnit = "B"

                            asd@ while (total >= 1024) {
                                total /= 1024
                                when (totalUnit) {
                                    "B" -> {
                                        totalUnit = "KB"
                                    }
                                    "KB" -> {
                                        totalUnit = "MB"
                                    }
                                    "MB" -> {
                                        totalUnit = "GB"
                                    }
                                    "GB" -> {
                                        totalUnit = "TB"
                                        break@asd
                                    }
                                }
                            }

                            asd@ while (free >= 1024) {
                                free /= 1024
                                when (freeUnit) {
                                    "B" -> {
                                        freeUnit = "KB"
                                    }
                                    "KB" -> {
                                        freeUnit = "MB"
                                    }
                                    "MB" -> {
                                        freeUnit = "GB"
                                    }
                                    "GB" -> {
                                        freeUnit = "TB"
                                        break@asd
                                    }
                                }
                            }
                            withContext(Dispatchers.Main) {
                                msgTextView.text = "$free $freeUnit 可用，共 $total $totalUnit"
                            }
                        }
                    }
                }
                // 普通文件
                smbFileTree != null -> {
                    findViewById<TextView>(R.id.tv_dia_file_look).text = context.resources.getString(R.string.dialog_look)
                    findViewById<ConstraintLayout>(R.id.cl_dia_file_download).apply {
                        setBackgroundResource(R.drawable.ic_vfs_look)
                    }
                    smbFileTree?.fileInfo?.let {
                        nameTextView.text = it.fileName
                        val fileDateModel = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
                        msgTextView.text = fileDateModel.format(it.changeTime.toDate())
                    }
                }
            }

            when (code) {
                VFS_DISK -> {
                    downloadView.isEnabled = false
                }
                VFS_FILE -> {
                    downloadView.setOnClickListener(this@DialogFileChoice)
                }
            }

            //setMsg(findViewById(R.id.tv_dia_file_date))
        }

        setContentView(view)

        window?.setGravity(Gravity.BOTTOM)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setWindowAnimations(R.style.dialog_animation)
    }

    /*abstract fun setMsg(textView: TextView)*/

    override fun onClick(v: View?) {
        v?.let {
            when (it.id) {
                R.id.cl_dia_file_recon -> {
                    //onButton2()
                    smbFileInfo?.let { info ->
                        initReconDialog(context, info)
                    }
                }
                R.id.cl_dia_file_rename -> {
                    showRenameDialog()
                }
                R.id.cl_dia_file_download -> {
                    onDownload()
                }
                else -> {
                }
            }
        }
    }

    /**
     * 显示选择本地文件夹Dialog
     */
    fun showFolderChoiceDialog() {
        val folderList = ArrayList<LocalFolder>()

        val server = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

        val list = server.storageVolumes

        if (!list.isNullOrEmpty()) {
            for (storageVolume in list) {
                storageVolume.getDescription(context)?.let { name ->
                    storageVolume.directory?.let { path ->
                        folderList.add(LocalFolder(name, path))
                    }
                }
            }
        }

        if (folderList.isNotEmpty()) {
            val dialog = Dialog(context)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_folder_choose, null).apply {
                val recyclerview = findViewById<RecyclerView>(R.id.rc_dia_folder)
                val adapter = LocalFolderRecyclerAdapter(folderList)
                recyclerview.adapter = adapter
                val staggeredGridLayoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
                recyclerview.layoutManager = staggeredGridLayoutManager

                findViewById<TextView>(R.id.tv_dia_folder_certain).setOnClickListener {
                    dialog.dismiss()
                }

                findViewById<TextView>(R.id.tv_dia_folder_cancel).setOnClickListener {
                    dialog.dismiss()
                }
            }

            dialog.setCancelable(false)
            dialog.setContentView(view)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        } else {
            Toast.makeText(context, "无可用存储", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 显示重命名Dialog
     */
    private fun showRenameDialog() {
        var smbConInfo: SmbConInfo? = null
        val dialog = Dialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_text, null).apply {
            val editText = findViewById<TextInputEditText>(R.id.tiet_dia_rename)

            findViewById<TextView>(R.id.tv_dia_rename_certain).setOnClickListener {
                smbConInfo?.let {
                    val nowName = editText.text.toString()
                    if (nowName.isNotBlank()) {
                        it.name = nowName
                        SmbConRepository.getInstance().upData(it)
                    }
                }
                dialog.dismiss()
            }
            findViewById<TextView>(R.id.tv_dia_rename_cancel).setOnClickListener {
                dialog.dismiss()
            }

            runBlocking {
                if (code == VFS_DISK) {
                    GlobalScope.launch {
                        smbConInfo = SmbConRepository.getInstance().getData(
                                smbFileInfo?.smbConInfo!!.ip,
                                smbFileInfo?.smbConInfo!!.path)

                        withContext(Dispatchers.Main) {
                            editText.setText(smbConInfo?.name)
                        }
                    }
                }
            }
        }

        dialog.setCancelable(false)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    /**
     * 修改连接
     * [smbFileInfo] smb文件信息
     */
    private fun initReconDialog(context: Context, smbFileInfo: SmbFileInfo) {
        val smbConInfo = SmbConRepository.getInstance().getData(smbFileInfo.smbConInfo.ip, smbFileInfo.smbConInfo.path)
        smbConInfo?.let { smbInfo ->
            val layoutInflater = LayoutInflater.from(context)
            val reconView = layoutInflater.inflate(R.layout.dialog_add_con, null).apply {
                findViewById<TextView>(R.id.tv_d_add_title).text = resources.getString(R.string.add_recon)
            }
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

    abstract fun onDownload()

    companion object {
        const val VFS_DISK = "VFS_DISK"
        const val VFS_FILE = "VFS_FILE"
    }
}