package com.susess.storesex

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditStoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                        val queue = LinkedBlockingQueue<Long>()
                        val store = mapStore()
                        Thread {
                            val id = StoresExApp.database.storeDao().save(store)
                            store.id = id
                            queue.add(id)
                        }.start()
                        with(queue.take()){
                            Snackbar.make(binding.root,
                                "Tienda creada",
                                Snackbar.LENGTH_SHORT)
                                .show()
                            //store.id = queue.take()

                            activity?.addStore(store)
                            parentFragmentManager.popBackStack()
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
            Glide.with(this)
                .load(binding.inputUrlImg.text.toString())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(binding.imagePhoto)
        }
    }

    private fun mapStore(): StoreEntity{
        val name = binding.inputName.text.toString().trim()
        val phone = binding.inputPhone.text.toString().trim()
        val url = binding.inputWebSite.text.toString().trim()
        val photo = binding.inputUrlImg.text.toString().trim()
       return StoreEntity(name =name, phone = phone, webSite = url, photoUrl = photo)
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