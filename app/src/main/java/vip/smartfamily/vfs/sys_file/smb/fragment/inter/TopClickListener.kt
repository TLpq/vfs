package vip.smartfamily.vfs.sys_file.smb.fragment.inter

import com.hierynomus.smbj.share.DiskShare
import vip.smartfamily.vfs.data.smb.SmbFileTree

/**
 * 点击监听
 */
interface TopClickListener {
    /**
     * 点击磁盘
     * [diskShare] 空间连接
     * [smbFileInfoList] 磁盘下的文件
     */
    fun onClickDisk(diskShare: DiskShare, smbFileInfoList: ArrayList<SmbFileTree>)
}