package com.example.nexbitmobile.ui.components

import android.app.AlertDialog
import android.content.Context

object CustomDialog {

    fun confirm(
        context: Context,
        title: String,
        message: String,
        confirmText: String = "Confirmar",
        cancelText: String = "Cancelar",
        onConfirm: () -> Unit,
        onCancel: (() -> Unit)? = null
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(confirmText) { _, _ -> onConfirm() }
            .setNegativeButton(cancelText) { _, _ -> onCancel?.invoke() }
            .show()
    }

    fun info(
        context: Context,
        title: String,
        message: String,
        buttonText: String = "Aceptar"
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(buttonText, null)
            .show()
    }

    fun options(
        context: Context,
        title: String,
        items: Array<String>,
        onItemClick: (Int) -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setItems(items) { _, which -> onItemClick(which) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    fun customView(
        context: Context,
        title: String,
        view: android.view.View,
        confirmText: String = "Aceptar",
        cancelText: String = "Cancelar",
        onConfirm: () -> Unit,
        onCancel: (() -> Unit)? = null
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(view)
            .setPositiveButton(confirmText) { _, _ -> onConfirm() }
            .setNegativeButton(cancelText) { _, _ -> onCancel?.invoke() }
            .show()
    }
}
