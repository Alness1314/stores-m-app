package com.susess.storesex.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.susess.storesex.models.store.StoreEntity

@Dao
interface StoreDao {

    @Query("SELECT * FROM stores")
    fun find(): MutableList<StoreEntity>

    @Insert
    fun save(storeEntity: StoreEntity): Long

    @Update
    fun update(storeEntity: StoreEntity)

    @Delete
    fun delete(storeEntity: StoreEntity)
}