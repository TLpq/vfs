package vip.smartfamily.vfs.sys_file.smb.fragment

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.share.Directory
import com.hierynomus.smbj.share.DiskShare
import kotlinx.coroutines.*
import vip.smartfamily.vfs.R
import vip.smartfamily.vfs.data.smb.SmbFileTree
import vip.smartfamily.vfs.sys_file.smb.adapter.FolderViewHolder
import vip.smartfamily.vfs.sys_file.smb.fragment.inter.TopClickListener
import vip.smartfamily.vfs.sys_file.smb.ui.my_view.DialogFileChoice
import java.io.File
import java.io.FileOutputStream
import java.util.*

abstract class SmbFileRecyclerAdapter(
        private val diskShare: DiskShare,
        private val smbFileInfoList: ArrayList<SmbFileTree>,
        private val topClickListener: TopClickListener
) : RecyclerView.Adapter<FolderViewHolder>() {
    private var dialogFileChoice: DialogFileChoice? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val folderView = LayoutInflater.from(parent.context).inflate(
                R.layout.item_file_list,
                parent,
                false
        )
        return FolderViewHolder(folderView)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val smbFileTree = smbFileInfoList[position]
        holder.run {
            statusView.visibility = View.GONE
            smbFileTree.fileInfo?.let {
                val fileName = it.fileName
                val path = if (smbFileTree.path.isBlank()) {
                    it.fileName
                } else {
                    "${smbFileTree.path}\\${it.fileName}"
                }
                nameView.text = it.fileName
                if (it.fileAttributes == FileAttributes.FILE_ATTRIBUTE_DIRECTORY.value) {
                    iconView.setOnClickListener {
                        // ???????????????
                        runBlocking {
                            GlobalScope.launch {
                                try {
                                    val directory: Directory = diskShare.openDirectory(
                                            path,
                                            HashSet(listOf(AccessMask.GENERIC_ALL)),
                                            HashSet(listOf(FileAttributes.FILE_ATTRIBUTE_NORMAL)),
                                            SMB2ShareAccess.ALL,
                                            SMB2CreateDisposition.FILE_OPEN,
                                            HashSet(listOf(SMB2CreateOptions.FILE_DIRECTORY_FILE)))

                                    val fileTrees = ArrayList<SmbFileTree>()
                                    for (fileInfo in directory.list()) {
                                        if ("." != fileInfo.fileName && ".." != fileInfo.fileName) {
                                            fileTrees.add(SmbFileTree(path, fileInfo, null))
                                        }
                                    }
                                    withContext(Dispatchers.Main) {
                                        topClickListener.onClickDisk(diskShare, fileTrees)
                                    }

                                    directory.close()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                } else {
                    val index = it.fileName.lastIndexOf(".")
                    if (index > 0) {
                        when (it.fileName.subSequence(index + 1, it.fileName.length)) {
                            "txt" -> {
                                iconView.setImageResource(R.drawable.ic_file_txt)
                            }
                            "mxml", "xml", "java", "iml", "html", "js", "css" -> {
                                iconView.setImageResource(R.drawable.ic_file_xml)
                            }
                            "7z", "rar", "zip", "JAR", "ARJ", "LZH", "TAR", "GZ", "ACE", "UUE", "BZ2" -> {
                                iconView.setImageResource(R.drawable.ic_file_zip)
                            }
                            "iso" -> {
                                iconView.setImageResource(R.drawable.ic_file_iso)
                            }
                            "png", "bmp", "jpg", "gif" -> {
                                iconView.setImageResource(R.drawable.ic_file_image)
                                iconView.setOnClickListener {
                                    GlobalScope.launch {
                                        withContext(Dispatchers.IO) {
                                            try {
                                                val file = diskShare.openFile(
                                                        path,
                                                        HashSet(listOf(AccessMask.GENERIC_ALL)),
                                                        HashSet(listOf(FileAttributes.FILE_ATTRIBUTE_NORMAL)),
                                                        SMB2ShareAccess.ALL,
                                                        SMB2CreateDisposition.FILE_OPEN,
                                                        HashSet(listOf(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE)))

                                                val bitmap = BitmapFactory.decodeStream(file.inputStream)
                                                // ????????????
                                                bitmap.recycle()
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                }
                            }
                            "mp3", "wma", "amr" -> {
                                iconView.setImageResource(R.drawable.ic_file_music)
                            }
                            "mp4", "3gp", "wmv", "asf", "asx", "rm", "rmvb", "mov", "m4v", "avi", "mkv", "flv", "vob" -> {
                                iconView.setImageResource(R.drawable.ic_file_video)
                            }
                            else -> {
                                iconView.setImageResource(R.drawable.ic_file_unknown)
                            }
                        }
                    } else {
                        iconView.setImageResource(R.drawable.ic_file_unknown)
                    }
                }

                iconView.setOnLongClickListener {
                    dialogFileChoice = object : DialogFileChoice(itemView.context, VFS_FILE, smbFileTree) {
                        override fun onCheckPermission() {
                            onWriteStorage()
                        }

                        override fun onDownload(loadPath: String?) {

                            loadPath?.let { loadFilePath ->
                                val loadFile = File(loadFilePath)
                                if (loadFile.isDirectory) {
                                    runBlocking {
                                        GlobalScope.launch(Dispatchers.IO) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "????????????", Toast.LENGTH_SHORT).show()
                                            }
                                            var fos: FileOutputStream? = null
                                            try {
                                                val file = diskShare.openFile(
                                                        path,
                                                        HashSet(listOf(AccessMask.GENERIC_ALL)),
                                                        HashSet(listOf(FileAttributes.FILE_ATTRIBUTE_NORMAL)),
                                                        SMB2ShareAccess.ALL,
                                                        SMB2CreateDisposition.FILE_OPEN,
                                                        HashSet(listOf(SMB2CreateOptions.FILE_NON_DIRECTORY_FILE)))

                                                fos = FileOutputStream(File(loadFile, fileName))
                                                file.read(fos) { numBytes, totalBytes ->
                                                    // ??????????????????
                                                    Log.e("download","$numBytes/$totalBytes")
                                                }

                                                file.close()
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(context, "????????????", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(context, "????????????", Toast.LENGTH_SHORT).show()
                                                }
                                            } finally {
                                                fos?.let { output ->
                                                    try {
                                                        output.flush()
                                                    } catch (e: Exception) {
                                                    }
                                                    try {
                                                        output.fd.sync()
                                                    } catch (e: Exception) {
                                                    }
                                                    try {
                                                        output.close()
                                                    } catch (e: Exception) {
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            dialogFileChoice?.dismiss()
                        }
                    }
                    dialogFileChoice?.show()

                    true
                }
            }
        }
    }

    /*fun createFile(pickerInitialUri: Uri){
        // ??????????????????PDF???????????????.
        val CREATE_FILE = 1

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "invoice.pdf")

            // ??????????????????????????????????????????????????????????????????????????????????????????????????????URI???
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivityForResult(intent, CREATE_FILE)
    }*/

    fun showFolderChoiceDialog() {
        dialogFileChoice?.showFolderChoiceDialog()
    }

    override fun getItemCount() = smbFileInfoList.size

    abstract fun onWriteStorage()
}