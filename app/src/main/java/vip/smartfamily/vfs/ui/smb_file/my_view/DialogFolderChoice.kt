package vip.smartfamily.vfs.ui.smb_file.my_view

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import vip.smartfamily.vfs.R

class DialogFolderChoice(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_folder_choose, null).apply {

        }

        setContentView(view)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}