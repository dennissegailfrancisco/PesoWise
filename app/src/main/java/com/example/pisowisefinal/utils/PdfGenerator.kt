package com.example.pisowisefinal.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.pisowisefinal.models.Expense
import java.io.File
import java.io.IOException

object PdfGenerator {
    private const val PAGE_WIDTH = 600
    private const val PAGE_HEIGHT = 1000

    fun generateTransactionPdf(context: Context, transactions: List<Expense>, fileName: String) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawPdfContent(canvas, transactions, fileName)

        document.finishPage(page)

        val fileUri = savePdf(context, document, fileName)
        document.close()
        fileUri?.let { openPdf(context, it) }
    }

    private fun drawPdfContent(canvas: Canvas, transactions: List<Expense>, fileName: String) {
        var yPosition = 50

        val titlePaint = createPaint(size = 22f, color = Color.BLUE, isBold = true)
        val headerPaint = createPaint(size = 18f, color = Color.BLACK, isBold = true)
        val textPaint = createPaint(size = 14f, color = Color.BLACK)

        val title = "Transaction Report - ${fileName.removePrefix("transactions_").removeSuffix(".pdf")}"
        canvas.drawText(title, 120f, yPosition.toFloat(), titlePaint)
        yPosition += 50

        canvas.drawText("Type", 30f, yPosition.toFloat(), headerPaint)
        canvas.drawText("Amount", 130f, yPosition.toFloat(), headerPaint)
        canvas.drawText("Category", 250f, yPosition.toFloat(), headerPaint)
        canvas.drawText("Date", 370f, yPosition.toFloat(), headerPaint)
        yPosition += 30

        transactions.forEach { transaction ->
            canvas.drawText(transaction.transactionType, 30f, yPosition.toFloat(), textPaint)
            canvas.drawText("₱${String.format("%.2f", transaction.amount)}", 130f, yPosition.toFloat(), textPaint)
            canvas.drawText(transaction.category, 250f, yPosition.toFloat(), textPaint)
            canvas.drawText(transaction.date, 370f, yPosition.toFloat(), textPaint)
            yPosition += 25

            canvas.drawText("Title: ${transaction.title}", 30f, yPosition.toFloat(), textPaint)
            yPosition += 20
            canvas.drawText("Message: ${transaction.message}", 30f, yPosition.toFloat(), textPaint)
            yPosition += 30
        }
    }

    private fun savePdf(context: Context, document: PdfDocument, fileName: String): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)?.also { uri ->
                    resolver.openOutputStream(uri)?.use { document.writeTo(it) }
                    return uri
                }
            } else {
                val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(directory, fileName)
                file.outputStream().use { document.writeTo(it) }
                Uri.fromFile(file)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun openPdf(context: Context, fileUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createPaint(size: Float, color: Int, isBold: Boolean = false): Paint {
        return Paint().apply {
            textSize = size
            this.color = color
            typeface = if (isBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
    }
}
