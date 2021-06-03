package vip.smartfamily.vfs.sys_file.smb.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vip.smartfamily.vfs.R
import vip.smartfamily.vfs.entity.LocalFolder
import java.io.File
import java.io.FilenameFilter

class LocalFolderRecyclerAdapter(
        private val textView: TextView,
        private val localFolderList: ArrayList<LocalFolder>
) : RecyclerView.Adapter<LocalFolderViewHolder>() {
    var path: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            LocalFolderViewHolder(LayoutInflater.from(parent.context).inflate(
                    R.layout.item_dialog_folder_list, parent, false))

    override fun onBindViewHolder(holder: LocalFolderViewHolder, position: Int) {
        val localFolder = localFolderList[position]
        holder.folderNameTextView.text = localFolder.name

        holder.itemView.setOnClickListener {
            path = localFolder.path
            textView.text = localFolder.name
            val file = File(localFolder.path)
            if (file.exists()) {
                val list = file.listFiles(DirFiler())
                list?.let { fileList ->
                    localFolderList.clear()
                    for (fileInfo in fileList) {
                        localFolderList.add(LocalFolder(fileInfo.name, fileInfo.path))
                    }
                }
            }

            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = localFolderList.size
}

class LocalFolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var folderNameTextView: TextView

    init {
        itemView.run {
            folderNameTextView = findViewById(R.id.tv_item_folder_name)
        }
    }
}

private class DirFiler : FilenameFilter {
    override fun accept(p0: File?, p1: String?): Boolean {
        p0?.let { file ->
            p1?.let { name ->
                return file.isDirectory && !name.contains(".")
            }
        }
        return p0?.isDirectory ?: false
    }
}