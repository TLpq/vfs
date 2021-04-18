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
import vip.smartfamily.vfs.R

/**
 * 下载文件夹选择器
 */

abstract class DialogFileChoice : Dialog, View.OnClickListener {

    private var code = ""

    constructor(context: Context) : super(context)
    constructor(context: Context, code: String) : super(context, R.style.style_dialog) {
        this.code = code
    }

    constructor(context: Context, themeResId: Int) : super(context, themeResId)
    constructor(context: Context, cancelable: Boolean, cancelListener: DialogInterface.OnCancelListener?) : super(context, cancelable, cancelListener)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_file_info, null).apply {
            val reconView = findViewById<ConstraintLayout>(R.id.cl_dia_file_recon)
            val renameView = findViewById<ConstraintLayout>(R.id.cl_dia_file_rename)
            val downloadView = findViewById<ConstraintLayout>(R.id.cl_dia_file_download)
            reconView.setOnClickListener(this@DialogFileChoice)
            renameView.setOnClickListener(this@DialogFileChoice)

            when (code) {
                VFS_DISK -> {
                    downloadView.isEnabled = false
                }
                VFS_FILE -> {
                    downloadView.setOnClickListener(this@DialogFileChoice)
                }
            }

            findViewById<TextView>(R.id.tv_dia_file_name).text = getName()
            setMsg(findViewById(R.id.tv_dia_file_date))
        }

        setContentView(view)

        window?.setGravity(Gravity.BOTTOM)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setWindowAnimations(R.style.dialog_animation)
    }

    abstract fun getName(): String
    abstract fun setMsg(textView: TextView)

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