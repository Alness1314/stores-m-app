package com.susess.storesex.models.store

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    var name: String? = null,
    var phone: String? = null,

    @ColumnInfo(name = "web_site")
    var webSite: String? = null,

    @ColumnInfo(name = "photo_url")
    var photoUrl: String? = null,

    @ColumnInfo(name = "is_favorite")
    var isFavorite: Boolean = false
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StoreEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}