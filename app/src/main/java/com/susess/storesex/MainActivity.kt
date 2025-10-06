package com.susess.storesex

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.susess.storesex.adapters.StoreAdapter
import com.susess.storesex.databinding.ActivityMainBinding
import com.susess.storesex.interfaces.MainAux
import com.susess.storesex.interfaces.OnClickListener
import com.susess.storesex.models.store.StoreEntity
import java.util.concurrent.LinkedBlockingQueue

class MainActivity : AppCompatActivity(), OnClickListener<StoreEntity>, MainAux {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mAdapter: StoreAdapter
    private lateinit var mGridLayout: GridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // registrar toolbar como ActionBar
        setSupportActionBar(binding.toolbarHeader)

        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        mAdapter = StoreAdapter(mutableListOf(), this)
        mGridLayout = GridLayoutManager(this, 2)
        findStores()

        binding.recyclerViewStores.apply {
            setHasFixedSize(true)
            layoutManager = mGridLayout
            adapter = mAdapter
        }
    }

    private fun setupListeners(){
        binding.floatButtonAction.setOnClickListener {
            launchEditFragment()
        }
    }

    private fun launchEditFragment(args: Bundle? = null) {
        val fragment = EditStoreFragment()

        if(args!=null){
            fragment.arguments = args
        }

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.add(R.id.main, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

        hideFab(true)
    }

    private fun findStores(){
        val queue = LinkedBlockingQueue<MutableList<StoreEntity>>()
        Thread {
            val stores = StoresExApp.database.storeDao().find()
            queue.add(stores)
        }.start()

        mAdapter.setStores(queue.take())
    }

    override fun onClick(item: StoreEntity) {
        val args = Bundle()
        args.putLong(getString(R.string.arg_id), item.id)
        launchEditFragment(args)
    }

    override fun onFavorite(item: StoreEntity) {
        item.isFavorite = !item.isFavorite
        Log.i("OnFavorite CLick", item.toString())
        val queue = LinkedBlockingQueue<StoreEntity>()
        Thread {
            StoresExApp.database.storeDao().update(item)
            queue.add(item)
        }.start()
        mAdapter.update(queue.take())
    }

    override fun onDelete(item: StoreEntity) {
        val queue = LinkedBlockingQueue<StoreEntity>()
        Thread {
            StoresExApp.database.storeDao().delete(item)
            queue.add(item)
        }.start()
        mAdapter.delete(queue.take())
    }

    override fun hideFab(hide: Boolean) {
        if(hide){
            binding.floatButtonAction.hide()
        }else{
            binding.floatButtonAction.show()
        }
    }

    override fun addStore(storeEntity: StoreEntity) {
        mAdapter.add(storeEntity)
    }

    override fun updateStore(storeEntity: StoreEntity) {
        mAdapter.update(storeEntity)
    }
}




