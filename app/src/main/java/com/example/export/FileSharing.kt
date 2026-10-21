package com.example.export

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object FileSharing {

    /** Writes bytes to cache/exports and returns the file. */
    fun writeExport(context: Context, fileName: String, bytes: ByteArray): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safe = fileName.replace(Regex("[\\\\/:*?\"<>|]"), "_")
        return File(dir, safe).apply { writeBytes(bytes) }
    }

    /** Opens the Android share sheet (WhatsApp, Gmail, Drive, ...) for a cached export file. */
    fun shareFile(context: Context, file: File, mimeType: String, chooserTitle: String, text: String? = null) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            if (text != null) putExtra(Intent.EXTRA_TEXT, text)
            clipData = ClipData.newRawUri(file.name, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(send, chooserTitle).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(chooser)
    }

    fun shareBytes(context: Context, fileName: String, bytes: ByteArray, mimeType: String, chooserTitle: String) {
        shareFile(context, writeExport(context, fileName, bytes), mimeType, chooserTitle)
    }
}
