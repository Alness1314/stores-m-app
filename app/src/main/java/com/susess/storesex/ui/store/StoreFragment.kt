package com.susess.storesex.ui.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.susess.storesex.R
import com.susess.storesex.StoresExApp
import com.susess.storesex.databinding.FragmentStoreBinding
import com.susess.storesex.models.store.StoreEntity
import com.susess.storesex.ui.main.MainActivity
import com.susess.storesex.validaciones.isPhone
import com.susess.storesex.validaciones.isUrl
import com.susess.storesex.validaciones.isVoid
import java.util.concurrent.LinkedBlockingQueue

class StoreFragment : Fragment() {
    private var _binding: FragmentStoreBinding? = null
    private val binding get() = _binding!!
    var activity: MainActivity? = null
    private var isEditMode: Boolean = false
    private var storeEntity: StoreEntity? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = arguments?.getLong(getString(R.string.arg_id), 0)

        if(id != null && id != 0L){
            isEditMode = true
            findStore(id)
        }else {
            isEditMode = false
            storeEntity = StoreEntity(name = "", phone = "", webSite = "", photoUrl = "")
        }

        activity = requireActivity() as MainActivity

        setupAcctionBar()

        val menuHost: MenuHost = requireActivity()

        setupMenuHost(menuHost)

        binding.inputUrlImg.addTextChangedListener {
            loadPhoto(binding.inputUrlImg.text.toString())
        }

        setupRealTimeValidation()
    }

    private fun setupAcctionBar(){
        activity?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = if(isEditMode) getString(R.string.title_fragment_update_store)
            else getString(R.string.title_fragment_create_store)
        }
    }

    private fun setupMenuHost(menuHost: MenuHost){
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
                                if(isEditMode) StoresExApp.Companion.database.storeDao().update(storeEntity!!)
                                else storeEntity!!.id = StoresExApp.Companion.database.storeDao().save(storeEntity!!)
                                queue.add(storeEntity)
                            }.start()
                            with(queue.take()){
                                var message = ""
                                if(!isEditMode){
                                    message = resources.getString(R.string.snackbar_message_save)
                                    activity?.addStore(storeEntity!!)
                                    parentFragmentManager.popBackStack()
                                }else{
                                    activity?.updateStore(storeEntity!!)
                                    message = resources.getString(R.string.snackbar_message_update)
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
    }

    private fun findStore(id: Long) {
        val queue = LinkedBlockingQueue<StoreEntity>()
        Thread {
            storeEntity = StoresExApp.Companion.database.storeDao().findOne(id)
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

    private fun setupRealTimeValidation() {
        // Lista de campos con su validador
        val validators = listOf(
            Triple(binding.inputLayoutUrlImg, String::isUrl, getString(R.string.helper_error)),
            Triple(binding.inputLayoutWebSite, String::isUrl, getString(R.string.helper_error)),
            Triple(binding.inputLayoutPhone, String::isPhone, getString(R.string.helper_error)),
            Triple(binding.inputLayoutName, String::isVoid, getString(R.string.helper_error))
        )

        validators.forEach { (inputLayout, validator, errorMsg) ->
            inputLayout.editText?.doOnTextChanged { _, _, _, _ ->
                validateField(inputLayout, validator, errorMsg)
            }
        }
    }

    private fun validateField(inputLayout: TextInputLayout,
                              validator: (String) -> Boolean,
                              errorMessage: String): Boolean{
        val text = inputLayout.editText?.text.toString().trim()
        return if(!validator(text)){
            inputLayout.error = errorMessage
            inputLayout.editText?.requestFocus()
            false
        }else {
            inputLayout.error = null
            true
        }
    }

    private fun validateFields(): Boolean {
       var isValid = true

      isValid = validateField(binding.inputLayoutUrlImg,
          String::isUrl,
          getString(R.string.helper_error)) && isValid

        isValid = validateField(binding.inputLayoutWebSite,
            String::isUrl,
            getString(R.string.helper_error)) && isValid

        isValid = validateField(binding.inputLayoutPhone,
            String::isPhone,
            getString(R.string.helper_error)) && isValid

        isValid = validateField(binding.inputLayoutName,
            String::isVoid,
            getString(R.string.helper_error)) && isValid

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