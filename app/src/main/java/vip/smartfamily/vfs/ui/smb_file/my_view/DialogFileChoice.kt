package vip.smartfamily.vfs.ui.smb_file.my_view

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vip.smartfamily.vfs.R
import vip.smartfamily.vfs.data.smb.SmbFileInfo
import vip.smartfamily.vfs.data.smb.SmbFileTree

/**
 * 下载文件夹选择器
 */

class DialogFileChoice : Dialog, View.OnClickListener {

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

    //constructor(context: Context, themeResId: Int) : super(context, themeResId)
    //constructor(context: Context, cancelable: Boolean, cancelListener: DialogInterface.OnCancelListener?) : super(context, cancelable, cancelListener)

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
                smbFileInfo != null -> {
                    nameTextView.text = smbFileInfo!!.smbConInfo.name
                    GlobalScope.launch(Dispatchers.IO) {
                        smbFileInfo!!.diskShare?.shareInformation?.let {
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
                smbFileTree != null -> {
                    nameTextView.text = smbFileTree!!.fileInfo!!.fileName
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
                }
                R.id.cl_dia_file_rename -> {
                }
                R.id.cl_dia_file_download -> {

                }
            }
        }
    }

    companion object {
        const val VFS_DISK = "VFS_DISK"
        const val VFS_FILE = "VFS_FILE"
    }
}