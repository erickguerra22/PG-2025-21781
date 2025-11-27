package com.eguerra.ciudadanodigital.ui.util

import android.app.AlertDialog
import android.content.Context
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

fun showConfirmationDialog(
    title: String,
    description: String,
    context: Context,
    hasNegative: Boolean = true,
    confirmText: String = "Sí",
    cancelText: String = "No",
    callback: (Boolean) -> Unit,
) {
    val dialogBuilder = AlertDialog.Builder(context)
    dialogBuilder.setMessage(description).setCancelable(false)
        .setPositiveButton(confirmText) { _, _ ->
            callback(true)
        }
    if (hasNegative) {
        dialogBuilder.setNegativeButton(cancelText) { dialog, _ ->
            dialog.dismiss()
            callback(false)
        }
    }

    val alert = dialogBuilder.create()
    alert.setTitle(title)
    alert.show()
}

suspend fun showConfirmationDialogSuspend(
    title: String,
    description: String,
    context: Context,
    hasNegative: Boolean = true,
    confirmText: String = "Sí",
    cancelText: String = "No",
): Boolean = suspendCancellableCoroutine { continuation ->
    val dialogBuilder = AlertDialog.Builder(context)
    dialogBuilder.setMessage(description).setCancelable(false)
        .setPositiveButton(confirmText) { _, _ ->
            if (continuation.isActive) {
                continuation.resume(true)
            }
        }
    if (hasNegative) {
        dialogBuilder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
            if (continuation.isActive) {
                continuation.resume(false)
            }
        }
    }
    val alert = dialogBuilder.create()
    alert.setTitle(title)

    alert.setOnCancelListener {
        if (continuation.isActive) {
            continuation.resume(false)
        }
    }

    alert.show()
}
