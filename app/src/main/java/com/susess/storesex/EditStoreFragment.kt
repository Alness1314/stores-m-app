package com.susess.storesex

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import com.susess.storesex.databinding.FragmentEditStoreBinding
import com.susess.storesex.models.store.StoreEntity
import java.util.concurrent.LinkedBlockingQueue


class EditStoreFragment : Fragment() {
    private var _binding: FragmentEditStoreBinding? = null
    private val binding get() = _binding!!
    var activity: MainActivity? = null
    private var isEditMode: Boolean = false
    private var storeEntity: StoreEntity? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditStoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun findStore(id: Long) {
        val queue = LinkedBlockingQueue<StoreEntity>()
        Thread {
            storeEntity = StoresExApp.database.storeDao().findOne(id)
            queue.add(storeEntity)
        }.start()
        queue.take()?.let {
            setUIStore(it)
        }
    }

    private fun setUIStore(store: StoreEntity) {
        with(binding){
            inputName.setText(store.name)
            inputPhone.setText(store.phone)
            inputWebSite.setText(store.webSite)
            inputUrlImg.setText(store.photoUrl)
            loadPhoto(store.photoUrl!!)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = arguments?.getLong(getString(R.string.arg_id), 0)

        if(id != null && id != 0L){
            isEditMode = true
            findStore(id)
        }else {
            isEditMode = false
            storeEntity = StoreEntity(name="", phone ="", webSite = "", photoUrl="")
        }

        activity = requireActivity() as MainActivity

        activity?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.edit_store_title_add)
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_save, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_save -> {
                        if(storeEntity!=null && validateFields()){
                            val queue = LinkedBlockingQueue<StoreEntity>()
                            mapStore()
                            Thread {
                                if(isEditMode) StoresExApp.database.storeDao().update(storeEntity!!)
                                else storeEntity!!.id = StoresExApp.database.storeDao().save(storeEntity!!)
                                queue.add(storeEntity)
                            }.start()
                            with(queue.take()){
                                var message = ""
                                if(!isEditMode){
                                    message = "Tienda creada"
                                    activity?.addStore(storeEntity!!)
                                    parentFragmentManager.popBackStack()
                                }else{
                                    activity?.updateStore(storeEntity!!)
                                    message = "Tienda actualizada"
                                }
                                Snackbar.make(binding.root,
                                    message,
                                    Snackbar.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        true
                    }
                    android.R.id.home -> {
                        parentFragmentManager.popBackStack()
                        true
                    }
                    else -> false
                }
            }


        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.inputUrlImg.addTextChangedListener {
            loadPhoto(binding.inputUrlImg.text.toString())
        }
    }

    private fun validateFields(): Boolean {
       var isValid = true
        if(binding.inputUrlImg.text.toString().trim().isEmpty()){
            binding.inputLayoutUrlImg.error = getString(R.string.helper_required)
            binding.inputUrlImg.requestFocus()
            isValid = false
        }
        if(binding.inputWebSite.text.toString().trim().isEmpty()){
            binding.inputLayoutWebSite.error = getString(R.string.helper_required)
            binding.inputWebSite.requestFocus()
            isValid = false
        }
        if(binding.inputPhone.text.toString().trim().isEmpty()){
            binding.inputLayoutPhone.error = getString(R.string.helper_required)
            binding.inputPhone.requestFocus()
            isValid = false
        }
        if(binding.inputName.text.toString().trim().isEmpty()){
            binding.inputLayoutName.error = getString(R.string.helper_required)
            binding.inputName.requestFocus()
            isValid = false
        }
        return isValid
    }

    private fun loadPhoto(url: String){
        Glide.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .into(binding.imagePhoto)
    }

    private fun mapStore() {
        with(storeEntity!!){
            name = binding.inputName.text.toString().trim()
            phone = binding.inputPhone.text.toString().trim()
            webSite = binding.inputWebSite.text.toString().trim()
            photoUrl = binding.inputUrlImg.text.toString().trim()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.supportActionBar?.apply{
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowHomeEnabled(false)
            title = getString(R.string.app_name)
        }
        activity?.hideFab(false)
        _binding = null
    }


}