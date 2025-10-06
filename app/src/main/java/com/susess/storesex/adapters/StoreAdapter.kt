package com.susess.storesex.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.susess.storesex.R
import com.susess.storesex.databinding.ItemStoreBinding
import com.susess.storesex.interfaces.OnClickListener
import com.susess.storesex.models.store.StoreEntity

class StoreAdapter(
    private var stores: MutableList<StoreEntity>,
    private var listener: OnClickListener<StoreEntity>
) : RecyclerView.Adapter<StoreAdapter.ViewHolder>() {

    private lateinit var mContext: Context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        mContext = parent.context
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.item_store, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val store = stores.get(position)
        with(holder) {
            setListeners(store)
            binding.textTitle.text = store.name
            binding.checkboxFav.isChecked = store.isFavorite

            Glide.with(mContext)
                .load(store.photoUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(binding.imagePhotoStore)
        }
    }

    override fun getItemCount(): Int = stores.size

    fun add(store: StoreEntity) {
        if(!stores.contains(store)){
            stores.add(store)
            notifyItemInserted(stores.size - 1)
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setStores(stores: MutableList<StoreEntity>) {
        this.stores = stores
        notifyDataSetChanged()
    }

    fun update(storeEntity: StoreEntity) {
        val index = stores.indexOf(storeEntity)
        if(index != -1){
            stores.set(index, storeEntity)
            notifyItemChanged(index)
        }
    }

    fun delete(storeEntity: StoreEntity) {
        val index = stores.indexOf(storeEntity)
        if(index != -1){
            stores.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemStoreBinding.bind(view)

        fun setListeners(store: StoreEntity) {
            with(binding.root){
                setOnClickListener { listener.onClick(store) }
                setOnLongClickListener {
                    listener.onDelete(store)
                    true
                }
            }

            binding.checkboxFav.setOnClickListener {
                listener.onFavorite(store)
            }
        }
    }
}