package com.example.nexbitmobile.ui.components

import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.example.nexbitmobile.R

object CartToast {

    fun show(rootView: View, productName: String, onViewCart: () -> Unit) {
        val inflater = LayoutInflater.from(rootView.context)
        val layout = inflater.inflate(R.layout.toast_cart, null) as LinearLayout
        layout.findViewById<TextView>(R.id.tvCartToastMessage).text = "$productName agregado al carrito"
        layout.findViewById<Button>(R.id.btnViewCartToast).setOnClickListener { onViewCart() }

        val toast = Toast(rootView.context)
        toast.duration = Toast.LENGTH_LONG
        toast.view = layout
        toast.show()
    }
}
