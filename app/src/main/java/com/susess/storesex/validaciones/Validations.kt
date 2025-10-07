package com.susess.storesex.validaciones


fun String.isEmail(): Boolean {
    val regex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
    return !this.matches(regex)
}

fun String.isUrl(): Boolean {
    val regex = Regex("^(https?:\\/\\/)?([^\\s\\/?#]+)(\\/[^\\s?#]*)?(\\?[^\\s#]*)?(#.*)?\$")
    return !this.isEmpty() && this.matches(regex)
}

fun String.isPhone(): Boolean {
    val regex = Regex("^(?:\\+\\d{1,3}\\s)?\\d{6,12}\$")
    return !this.isEmpty() && this.matches(regex)
}

fun String.isVoid(): Boolean{
    return !this.isNullOrEmpty()
}
