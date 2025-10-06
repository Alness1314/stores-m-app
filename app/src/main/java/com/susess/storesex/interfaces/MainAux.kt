package com.susess.storesex.interfaces

import com.susess.storesex.models.store.StoreEntity

interface MainAux {
    fun hideFab(hide: Boolean = true)
    fun addStore(storeEntity: StoreEntity)
    fun updateStore(storeEntity: StoreEntity)
}