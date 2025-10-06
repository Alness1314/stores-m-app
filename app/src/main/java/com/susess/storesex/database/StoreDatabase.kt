package com.susess.storesex.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.susess.storesex.dao.StoreDao
import com.susess.storesex.models.store.StoreEntity

@Database(entities = [StoreEntity::class], version = 2, exportSchema = false)
abstract class StoreDatabase: RoomDatabase() {
    abstract fun storeDao(): StoreDao
}