package vip.smartfamily.vfs.sys_file.smb

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2CreateOptions
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.share.DiskShare
import java.util.*

object TestVFS {
    @JvmStatic
    fun main(args: Array<String>) {
        val client = SMBClient()
        try {
            client.connect("192.168.101.224").use { connection ->
                val ac = AuthenticationContext("SUMBUM", "zdzdzdzd".toCharArray(), "192.168.101.224")
                val session = connection.authenticate(ac)
                try {
                    // Connect to Share
                    val share = session.connectShare("yf") as DiskShare
                    for (f in share.list("", "*")) {
                        if (f.fileAttributes == FileAttributes.FILE_ATTRIBUTE_DIRECTORY.value) {
                            val name = f.fileName
                            if ("." != name && ".." != name) {
                                // 打开文件夹
                                val directory = share.openDirectory(
                                        name,
                                        HashSet(listOf(AccessMask.GENERIC_ALL)),
                                        HashSet(listOf(FileAttributes.FILE_ATTRIBUTE_NORMAL)),
                                        SMB2ShareAccess.ALL,
                                        SMB2CreateDisposition.FILE_OPEN,
                                        HashSet(listOf(SMB2CreateOptions.FILE_DIRECTORY_FILE)))
                                for (w in directory.list()) {
                                    println("File : " + w.fileName)
                                    println("FileType : " + w.fileAttributes)
                                    println("FileShortName : " + w.shortName)
                                    println("=========================================")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}