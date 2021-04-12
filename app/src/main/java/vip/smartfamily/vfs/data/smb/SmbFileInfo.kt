package vip.smartfamily.vfs.data.smb

import com.hierynomus.smbj.share.DiskShare
import vip.smartfamily.vfs.db.entity.SmbConInfo

class SmbFileInfo(
        var diskShare: DiskShare?,
        var smbConInfo: SmbConInfo,
        var fileTrees: ArrayList<SmbFileTree>?
) {
}