package vip.smartfamily.vfs.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import vip.smartfamily.vfs.db.entity.SmbConInfo
import vip.smartfamily.vfs.db.repository.SmbConRepository

class SmbFileViewModel : ViewModel() {
    val smbConnect: LiveData<List<SmbConInfo>> =
            SmbConRepository.getInstance().getLiveData().asLiveData()
}