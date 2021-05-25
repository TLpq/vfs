package vip.smartfamily.vfs.ui.smb_file.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vip.smartfamily.vfs.R
import vip.smartfamily.vfs.entity.LocalFolder

public class LocalFolderRecyclerAdapter(
        private val localFolderList: ArrayList<LocalFolder>
) : RecyclerView.Adapter<LocalFolderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            LocalFolderViewHolder(LayoutInflater.from(parent.context).inflate(
                    R.layout.item_dialog_folder_list, parent, false))

    override fun onBindViewHolder(holder: LocalFolderViewHolder, position: Int) {
        val localFolder = localFolderList[position]
        holder.folderNameTextView.text = localFolder.name
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