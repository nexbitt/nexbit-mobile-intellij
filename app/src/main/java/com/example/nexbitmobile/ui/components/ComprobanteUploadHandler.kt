package com.example.nexbitmobile.ui.components

import android.net.Uri
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class ComprobanteUploadHandler(private val activity: AppCompatActivity) {

    var currentSheetUri: Uri? = null
    var currentSheetFileNameView: TextView? = null

    private val comprobanteLauncher = activity.registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            currentSheetUri = uri
            currentSheetFileNameView?.let {
                it.text = uri.lastPathSegment ?: "Comprobante seleccionado"
                it.visibility = android.view.View.VISIBLE
            }
        }
    }

    fun launchPicker() {
        try {
            comprobanteLauncher.launch("image/*")
        } catch (_: Exception) {
            android.widget.Toast.makeText(activity, "No hay una app para seleccionar archivos", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun reset() {
        currentSheetUri = null
        currentSheetFileNameView?.let {
            it.visibility = android.view.View.GONE
        }
    }
}
