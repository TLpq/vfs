package vip.smartfamily.vfs.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import vip.smartfamily.vfs.db.entity.SmbConInfo

/**
 * 查询连接
 */
@Dao
interface SmbConDao {
    @Insert
    fun addData(smbConInfo: SmbConInfo)

    @Update
    fun upData(smbConInfo: SmbConInfo)

    @Query("SELECT * FROM connection_info")
    fun getAllData(): List<SmbConInfo>

    @Query("SELECT * FROM connection_info WHERE guid=:guid")
    fun getData(guid: String): SmbConInfo?

    @Query("SELECT * FROM connection_info WHERE ip=:ip AND path=:path")
    fun getData(ip: String, path: String): SmbConInfo

    @Transaction
    @Query("SELECT * FROM connection_info")
    fun getLiveData(): Flow<List<SmbConInfo>>

    @Query("DELETE FROM connection_info WHERE guid=:guid")
    fun delete(guid: String)

    @Query("SELECT * FROM connection_info WHERE ip=:ip AND path=:path")
    fun isExist(ip: String, path: String): Boolean

    @Query("DELETE FROM connection_info")
    fun clear()
}