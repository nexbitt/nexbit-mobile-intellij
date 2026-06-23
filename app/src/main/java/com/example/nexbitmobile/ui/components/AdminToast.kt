package com.example.nexbitmobile.ui.components

import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.example.nexbitmobile.R

object AdminToast {

    fun success(rootView: View, message: String) {
        show(rootView, message, R.color.success)
    }

    fun error(rootView: View, message: String) {
        show(rootView, message, R.color.error_text)
    }

    fun warning(rootView: View, message: String) {
        show(rootView, message, R.color.warning)
    }

    fun info(rootView: View, message: String) {
        show(rootView, message, R.color.primary)
    }

    private fun show(rootView: View, message: String, bgColor: Int) {
        val inflater = LayoutInflater.from(rootView.context)
        val layout = inflater.inflate(R.layout.toast_admin, null) as LinearLayout
        layout.setBackgroundColor(rootView.context.resources.getColor(bgColor, rootView.context.theme))
        layout.findViewById<TextView>(R.id.tvToastAdminMessage).text = message

        val toast = Toast(rootView.context)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }
}
