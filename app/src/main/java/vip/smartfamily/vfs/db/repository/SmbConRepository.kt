package vip.smartfamily.vfs.db.repository

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import vip.smartfamily.vfs.db.AppDatabase
import vip.smartfamily.vfs.db.dao.SmbConDao
import vip.smartfamily.vfs.db.entity.SmbConInfo

class SmbConRepository private constructor(private val smbConDao: SmbConDao) {

    fun addConInfo(smbConInfo: SmbConInfo) {
        runBlocking {
            val job = GlobalScope.launch {
                smbConDao.addData(smbConInfo)
            }
            job.join()
        }
    }

    fun isExist(ip: String, path: String): Boolean {
        var b = true
        runBlocking {
            val job = GlobalScope.launch {
                b = smbConDao.isExist(ip, path)
            }
            job.join()
        }
        return b
    }

    fun upData(smbConInfo: SmbConInfo) {
        CoroutineScope(Dispatchers.IO).launch {
            smbConDao.upData(smbConInfo)
        }
    }

    fun getLiveData(): Flow<List<SmbConInfo>> {
        var data: Flow<List<SmbConInfo>>
        runBlocking {
            data = withContext(Dispatchers.IO) {
                data = smbConDao.getLiveData()
                data
            }
        }
        return data
    }

    fun getData(ip: String, path: String): SmbConInfo? {
        var smbConInfo: SmbConInfo?
        runBlocking {
            smbConInfo = withContext(Dispatchers.IO) {
                smbConInfo = smbConDao.getData(ip, path)
                smbConInfo
            }
        }
        return smbConInfo
    }

    fun gatAllConList(): ArrayList<SmbConInfo> {
        val list = ArrayList<SmbConInfo>()
        runBlocking {
            val job = GlobalScope.launch {
                list.addAll(smbConDao.getAllData())
            }
            job.join()
        }
        return list
    }

    companion object {
        @Volatile
        private var instance: SmbConRepository? = null

        fun getInstance() = instance ?: synchronized(this) {
            val appDatabase = AppDatabase.getInstance()
            instance ?: SmbConRepository(appDatabase.getSmbConDao()).also {
                instance = it
            }
        }

    }
}