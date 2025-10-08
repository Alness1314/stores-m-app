package com.susess.storesex.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.susess.storesex.StoresExApp
import com.susess.storesex.models.store.StoreEntity
import java.util.concurrent.LinkedBlockingQueue

class MainViewModel: ViewModel() {
    private val _stores = MutableLiveData<List<StoreEntity>>()
    val stores: LiveData<List<StoreEntity>> = _stores

    init {
        findStores()
    }

    private fun setStores(stores: MutableList<StoreEntity>){
        _stores.postValue(stores)
    }

    private fun findStores(){
        val queue = LinkedBlockingQueue<MutableList<StoreEntity>>()
        Thread {
            val stores = StoresExApp.database.storeDao().find()
            queue.add(stores)
        }.start()
        setStores(queue.take())
    }




}