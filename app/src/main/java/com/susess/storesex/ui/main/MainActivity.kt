package com.susess.storesex.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.susess.storesex.R
import com.susess.storesex.ui.store.StoreFragment
import com.susess.storesex.StoresExApp
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

    //MVVM
    private lateinit var mMainViewModel: MainViewModel

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

        setupViewModel()

        setupRecyclerView()
        setupListeners()
    }

    private fun setupViewModel() {
        mMainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        mMainViewModel.stores.observe(this) { stores ->
            mAdapter.setStores(stores.toMutableList())
        }
    }

    private fun setupRecyclerView() {
        mAdapter = StoreAdapter(mutableListOf(), this)
        mGridLayout = GridLayoutManager(this, resources.getInteger(R.integer.main_colums))
        //findStores()

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
        val fragment = StoreFragment()

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

    /*private fun findStores(){
        val queue = LinkedBlockingQueue<MutableList<StoreEntity>>()
        Thread {
            val stores = StoresExApp.Companion.database.storeDao().find()
            queue.add(stores)
        }.start()

        mAdapter.setStores(queue.take())
    }*/

    override fun onClick(item: StoreEntity) {
        val args = Bundle()
        args.putLong(getString(R.string.arg_id), item.id)
        launchEditFragment(args)
    }

    override fun onFavorite(item: StoreEntity) {
        item.isFavorite = !item.isFavorite
        val queue = LinkedBlockingQueue<StoreEntity>()
        Thread {
            StoresExApp.Companion.database.storeDao().update(item)
            queue.add(item)
        }.start()
        updateStore(queue.take())
    }

    override fun onDelete(item: StoreEntity) {
        val items = resources.getStringArray(R.array.array_options_items)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_options_title)
            .setItems(items) { dialogInterface, i ->
                when (i) {
                    0 -> confirmDelete(item)
                    1 -> dial(item.phone!!)
                    2 -> goToWebSite(item.webSite!!)
                    else -> Toast.makeText(
                        this,
                        resources.getString(R.string.toast_message_option_default),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .show()
    }

    private fun dial(phone: String){
        val callIntent = Intent().apply {
            action = Intent.ACTION_DIAL
            data = "tel:$phone".toUri()
        }
        startIntent(callIntent)
    }

    private fun goToWebSite(webSite: String){
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = webSite.toUri()
        }
        startIntent(intent)
    }

    private fun startIntent(intent: Intent){
        if(intent.resolveActivity(packageManager) != null)
            startActivity(intent)
        else
            Toast.makeText(this,
                resources.getString(R.string.toast_message_option_default),
                Toast.LENGTH_SHORT).show()
    }

    private fun confirmDelete(item: StoreEntity){
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_delete_title)
            .setPositiveButton(R.string.dialog_delete_confirm) { dialogInterface, i ->
                val queue = LinkedBlockingQueue<StoreEntity>()
                Thread {
                    StoresExApp.Companion.database.storeDao().delete(item)
                    queue.add(item)
                }.start()
                mAdapter.delete(queue.take())
            }
            .setNegativeButton(R.string.dialog_delete_cancel, null)
            .show()
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