package com.kaasht.croprecommendation.utils

import android.content.Context
import android.os.Environment
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.TextAlignment
import com.kaasht.croprecommendation.data.model.PredictionEntity
import com.opencsv.CSVWriter
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportHelper @Inject constructor(
    private val context: Context
) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    /**
     * Export predictions to CSV file
     */
    fun exportToCsv(predictions: List<PredictionEntity>): Result<String> {
        return try {
            val fileName = "kaasht_predictions_${dateFormat.format(Date())}.csv"
            val file = File(getExportDirectory(), fileName)
            
            val writer = CSVWriter(FileWriter(file))
            
            // Write header
            writer.writeNext(arrayOf(
                "Date",
                "Crop",
                "Confidence",
                "District",
                "Temperature",
                "Humidity",
                "Rainfall",
                "Nitrogen",
                "Phosphorus",
                "Potassium",
                "pH"
            ))
            
            // Write data
            predictions.forEach { prediction ->
                writer.writeNext(arrayOf(
                    displayDateFormat.format(Date(prediction.timestamp)),
                    prediction.predictedCrop,
                    String.format("%.2f", prediction.confidence),
                    prediction.district,
                    prediction.temperature.toString(),
                    prediction.humidity.toString(),
                    prediction.rainfall.toString(),
                    prediction.nitrogen.toString(),
                    prediction.phosphorus.toString(),
                    prediction.potassium.toString(),
                    prediction.ph.toString()
                ))
            }
            
            writer.close()
            Timber.d("CSV export successful: ${file.absolutePath}")
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Timber.e(e, "CSV export failed")
            Result.failure(e)
        }
    }
    
    /**
     * Export predictions to PDF file
     */
    fun exportToPdf(predictions: List<PredictionEntity>): Result<String> {
        return try {
            val fileName = "kaasht_predictions_${dateFormat.format(Date())}.pdf"
            val file = File(getExportDirectory(), fileName)
            
            val pdfWriter = PdfWriter(file)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)
            
            // Add title
            document.add(
                Paragraph("Kaasht - Crop Prediction History")
                    .setFontSize(20f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
            )
            
            document.add(
                Paragraph("Generated on: ${displayDateFormat.format(Date())}")
                    .setFontSize(10f)
                    .setTextAlignment(TextAlignment.CENTER)
            )
            
            document.add(Paragraph("\n"))
            
            // Summary statistics
            addSummarySection(document, predictions)
            
            document.add(Paragraph("\n"))
            
            // Detailed predictions table
            addPredictionsTable(document, predictions)
            
            document.close()
            Timber.d("PDF export successful: ${file.absolutePath}")
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Timber.e(e, "PDF export failed")
            Result.failure(e)
        }
    }
    
    private fun addSummarySection(document: Document, predictions: List<PredictionEntity>) {
        document.add(
            Paragraph("Summary Statistics")
                .setFontSize(16f)
                .setBold()
        )
        
        val totalPredictions = predictions.size
        val avgConfidence = predictions.map { it.confidence }.average()
        val mostPredicted = predictions
            .groupingBy { it.predictedCrop }
            .eachCount()
            .maxByOrNull { it.value }
        
        val summaryTable = Table(2).apply {
            addCell(createCell("Total Predictions:", true))
            addCell(createCell(totalPredictions.toString(), false))
            
            addCell(createCell("Average Confidence:", true))
            addCell(createCell("${String.format("%.1f", avgConfidence)}%", false))
            
            addCell(createCell("Most Predicted Crop:", true))
            addCell(createCell(mostPredicted?.key?.capitalize() ?: "N/A", false))
            
            addCell(createCell("Unique Crops:", true))
            addCell(createCell(predictions.map { it.predictedCrop }.distinct().size.toString(), false))
        }
        
        document.add(summaryTable)
    }
    
    private fun addPredictionsTable(document: Document, predictions: List<PredictionEntity>) {
        document.add(
            Paragraph("Detailed Predictions")
                .setFontSize(16f)
                .setBold()
        )
        
        val table = Table(floatArrayOf(2f, 2f, 1.5f, 1.5f, 1f, 1f, 1f))
        
        // Header
        table.addHeaderCell(createHeaderCell("Date"))
        table.addHeaderCell(createHeaderCell("Crop"))
        table.addHeaderCell(createHeaderCell("Confidence"))
        table.addHeaderCell(createHeaderCell("District"))
        table.addHeaderCell(createHeaderCell("Temp (°C)"))
        table.addHeaderCell(createHeaderCell("Humidity (%)"))
        table.addHeaderCell(createHeaderCell("pH"))
        
        // Data rows
        predictions.take(50).forEach { prediction -> // Limit to 50 for PDF size
            table.addCell(createCell(displayDateFormat.format(Date(prediction.timestamp)), false))
            table.addCell(createCell(prediction.predictedCrop.capitalize(), false))
            table.addCell(createCell("${String.format("%.1f", prediction.confidence)}%", false))
            table.addCell(createCell(prediction.district.capitalize(), false))
            table.addCell(createCell(prediction.temperature.toInt().toString(), false))
            table.addCell(createCell(prediction.humidity.toInt().toString(), false))
            table.addCell(createCell(String.format("%.1f", prediction.ph), false))
        }
        
        document.add(table)
        
        if (predictions.size > 50) {
            document.add(
                Paragraph("\nShowing first 50 predictions only. Total: ${predictions.size}")
                    .setFontSize(10f)
                    .setItalic()
            )
        }
    }
    
    private fun createHeaderCell(text: String): Cell {
        return Cell()
            .add(Paragraph(text))
            .setBold()
            .setBackgroundColor(ColorConstants.LIGHT_GRAY)
            .setTextAlignment(TextAlignment.CENTER)
    }
    
    private fun createCell(text: String, isBold: Boolean): Cell {
        val paragraph = Paragraph(text)
        if (isBold) paragraph.setBold()
        return Cell().add(paragraph)
    }
    
    private fun getExportDirectory(): File {
        val exportDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "Kaasht"
        )
        
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        
        return exportDir
    }
}
