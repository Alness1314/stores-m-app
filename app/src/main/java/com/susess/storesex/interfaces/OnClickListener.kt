package com.susess.storesex.interfaces


interface OnClickListener<T> {
    fun onClick(item: T)
    fun onFavorite(item: T)
    fun onDelete(item: T)
}