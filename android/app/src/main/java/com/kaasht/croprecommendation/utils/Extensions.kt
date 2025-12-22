package com.kaasht.croprecommendation.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

// View Extensions
fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

// Context Extensions
fun Context.toast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

// Fragment Extensions
fun Fragment.showSnackbar(message: String, length: Int = Snackbar.LENGTH_SHORT) {
    view?.let {
        Snackbar.make(it, message, length).show()
    }
}

// String Extensions
fun String.capitalize(): String {
    return this.replaceFirstChar { 
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
    }
}

// Date Extensions
fun Long.toFormattedDate(pattern: String = "MMM dd, yyyy"): String {
    val format = SimpleDateFormat(pattern, Locale.getDefault())
    return format.format(Date(this))
}

fun Long.toFormattedTime(pattern: String = "HH:mm"): String {
    val format = SimpleDateFormat(pattern, Locale.getDefault())
    return format.format(Date(this))
}

// Double Extensions
fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return kotlin.math.round(this * multiplier) / multiplier
}
