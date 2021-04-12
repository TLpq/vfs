package vip.smartfamily.vfs.data.smb

import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation

class SmbFileTree(
        val path: String,
        val fileInfo: FileIdBothDirectoryInformation?,
        val subtree: ArrayList<SmbFileTree>?
)