package vip.smartfamily.vfs.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "connection_info")
class SmbConInfo(
        var name: String,
        var ip: String,
        var user: String,
        var paw: String,
        var path: String
) {
    @PrimaryKey
    var guid = UUID.randomUUID().toString()
}