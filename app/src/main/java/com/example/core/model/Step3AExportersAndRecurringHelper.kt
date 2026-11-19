package com.example.core.model

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.example.R
import com.example.core.database.CompanyEntity
import com.example.core.database.TxnChargeEntity
import com.example.core.database.TxnEntity
import com.example.core.database.TxnLineEntity
import com.example.core.database.TxnTaxLineEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Step 3A: Offline A4 PDF & CSV Exporters for Sales Documents, Sale Report, and Bill-wise Profit,
 * plus Local Notification helper for Recurring Invoices.
 * 100% offline — uses Android PdfDocument, FileProvider, and NotificationManager.
 */
object Step3APdfAndCsvExporter {

    fun exportAndShareSalesVoucherPdf(
        context: Context,
        company: CompanyEntity?,
        txn: TxnEntity,
        lines: List<TxnLineEntity>,
        charges: List<TxnChargeEntity>,
        taxLines: List<TxnTaxLineEntity>
    ): File {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(27, 27, 47)
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(80, 84, 110)
            textSize = 9.5f
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(91, 63, 217)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(32, 34, 52)
            textSize = 9f
        }
        val boldBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(20, 22, 38)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(215, 218, 230)
            strokeWidth = 1f
        }

        val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        var y = 36f

        // Firm Header
        val firmName = company?.businessName?.ifBlank { "Vanijya Business Vault" } ?: "Vanijya Business Vault"
        canvas.drawText(firmName, 32f, y, titlePaint)
        val docLabel = SalesDocTypes.displayLabel(txn.docType).uppercase(Locale.getDefault())
        canvas.drawText(docLabel, 390f, y, headerPaint)
        y += 14f

        val firmSub = buildString {
            if (!company?.gstin.isNullOrBlank()) append("GSTIN: ${company?.gstin}  •  ")
            append("State: ${company?.stateCode ?: "27"}-${company?.stateName ?: "Maharashtra"}")
        }
        canvas.drawText(firmSub, 32f, y, subtitlePaint)
        canvas.drawText("No: ${txn.docNumber}", 390f, y, boldBodyPaint)
        y += 13f

        canvas.drawText(
            "Date: ${dateFmt.format(Date(txn.txnDate))}  |  Due: ${dateFmt.format(Date(txn.dueDate))}  |  Mode: ${txn.classification}",
            32f,
            y,
            subtitlePaint
        )
        canvas.drawText("Status: ${txn.status}", 390f, y, subtitlePaint)
        y += 10f
        canvas.drawLine(32f, y, 563f, y, linePaint)
        y += 16f

        // Party Block
        canvas.drawText("Bill To: ${txn.partyName.ifBlank { "Cash / Walk-in Customer" }}", 32f, y, boldBodyPaint)
        canvas.drawText(
            "Place of Supply: ${txn.placeOfSupplyStateCode}-${txn.placeOfSupplyStateName} (${txn.supplyType})",
            310f,
            y,
            bodyPaint
        )
        y += 13f
        if (txn.partyGstin.isNotBlank() || txn.partyPhone.isNotBlank()) {
            canvas.drawText(
                "Party GSTIN: ${txn.partyGstin.ifBlank { "Unregistered" }}  •  Ph: ${txn.partyPhone.ifBlank { "-" }}",
                32f,
                y,
                subtitlePaint
            )
            y += 13f
        }
        if (txn.billingAddress.isNotBlank()) {
            canvas.drawText("Address: ${txn.billingAddress.take(85)}", 32f, y, subtitlePaint)
            y += 13f
        }
        if (txn.isReverseCharge) {
            canvas.drawText("Reverse Charge (RCM): YES — Tax payable by recipient", 32f, y, headerPaint)
            y += 13f
        }

        canvas.drawLine(32f, y, 563f, y, linePaint)
        y += 15f

        // Line Items Header
        canvas.drawText("#", 32f, y, headerPaint)
        canvas.drawText("Item / Description", 50f, y, headerPaint)
        canvas.drawText("HSN", 235f, y, headerPaint)
        canvas.drawText("Qty", 285f, y, headerPaint)
        canvas.drawText("Rate", 335f, y, headerPaint)
        canvas.drawText("Taxable", 405f, y, headerPaint)
        canvas.drawText("GST", 470f, y, headerPaint)
        canvas.drawText("Total", 518f, y, headerPaint)
        y += 6f
        canvas.drawLine(32f, y, 563f, y, linePaint)
        y += 13f

        lines.take(16).forEachIndexed { idx, ln ->
            val freeSuffix = if (ln.freeQty > 0L) "+${ln.freeQty}F" else ""
            val batchSuffix = if (ln.batchNumber.isNotBlank()) " [${ln.batchNumber}]" else ""
            val gstPaise = ln.cgstPaise + ln.sgstPaise + ln.igstPaise + ln.cessPaise
            canvas.drawText("${idx + 1}", 32f, y, bodyPaint)
            canvas.drawText("${ln.itemName.take(26)}$batchSuffix", 50f, y, bodyPaint)
            canvas.drawText(ln.hsnSacCode.ifBlank { "-" }, 235f, y, bodyPaint)
            canvas.drawText("${ln.qty}$freeSuffix ${ln.unit}", 285f, y, bodyPaint)
            canvas.drawText(Money(ln.unitPricePaise).formatIndian(includeSymbol = false), 335f, y, bodyPaint)
            canvas.drawText(Money(ln.taxablePaise).formatIndian(includeSymbol = false), 405f, y, bodyPaint)
            canvas.drawText(Money(gstPaise).formatIndian(includeSymbol = false), 470f, y, bodyPaint)
            canvas.drawText(Money(ln.lineTotalPaise).formatIndian(includeSymbol = false), 518f, y, boldBodyPaint)
            y += 14f
        }

        if (charges.isNotEmpty()) {
            y += 4f
            charges.forEach { ch ->
                canvas.drawText("+ Charge: ${ch.label}", 50f, y, subtitlePaint)
                canvas.drawText(Money(ch.amountPaise).formatIndian(), 505f, y, bodyPaint)
                y += 12f
            }
        }

        y += 4f
        canvas.drawLine(32f, y, 563f, y, linePaint)
        y += 14f

        // Totals Block
        canvas.drawText("Taxable Value: ${Money(txn.taxableSubtotalPaise).formatIndian()}", 32f, y, bodyPaint)
        canvas.drawText(
            "CGST: ${Money(txn.cgstTotalPaise).formatIndian()}  |  SGST: ${Money(txn.sgstTotalPaise).formatIndian()}  |  IGST: ${Money(txn.igstTotalPaise).formatIndian()}",
            210f,
            y,
            bodyPaint
        )
        y += 14f
        if (txn.cessTotalPaise > 0L || txn.tcsPaise > 0L || txn.tdsPaise > 0L || txn.roundOffPaise != 0L) {
            canvas.drawText(
                "Cess: ${Money(txn.cessTotalPaise).formatIndian()}  |  TCS: ${Money(txn.tcsPaise).formatIndian()}  |  TDS: ${Money(txn.tdsPaise).formatIndian()}  |  Round-off: ${txn.roundOffPaise / 100.0}",
                32f,
                y,
                subtitlePaint
            )
            y += 14f
        }

        canvas.drawText("Grand Total: ${Money(txn.grandTotalPaise).formatIndian()}", 32f, y, titlePaint)
        canvas.drawText(
            "Paid: ${Money(txn.paidAmountPaise).formatIndian()}   |   Balance: ${Money(txn.balanceAmountPaise).formatIndian()}",
            310f,
            y,
            boldBodyPaint
        )
        y += 16f

        canvas.drawText("Amount in Words: ${txn.amountInWords}", 32f, y, boldBodyPaint)
        y += 18f

        // HSN Summary Table
        if (taxLines.isNotEmpty() && y < 740f) {
            canvas.drawLine(32f, y, 563f, y, linePaint)
            y += 13f
            canvas.drawText("HSN/SAC Tax Summary", 32f, y, headerPaint)
            y += 13f
            taxLines.take(6).forEach { tl ->
                canvas.drawText(
                    "HSN ${tl.hsnSacCode}: Taxable ${Money(tl.taxablePaise).formatIndian()} | Rate ${tl.taxRateBps / 100.0}% | CGST ${Money(tl.cgstPaise).formatIndian()} | SGST ${Money(tl.sgstPaise).formatIndian()} | IGST ${Money(tl.igstPaise).formatIndian()}",
                    32f,
                    y,
                    subtitlePaint
                )
                y += 12f
            }
        }

        // Footer Bank & Terms
        if (y < 790f) {
            y += 8f
            canvas.drawLine(32f, y, 563f, y, linePaint)
            y += 14f
            if (!company?.bankName.isNullOrBlank() || !company?.upiId.isNullOrBlank()) {
                canvas.drawText(
                    "Bank: ${company?.bankName ?: "-"} A/C ${company?.bankAccountNo ?: "-"} (${company?.bankIfsc ?: "-"})  |  UPI: ${company?.upiId ?: "-"}",
                    32f,
                    y,
                    subtitlePaint
                )
                y += 13f
            }
            if (txn.termsText.isNotBlank()) {
                canvas.drawText("Terms: ${txn.termsText.take(90)}", 32f, y, subtitlePaint)
                y += 13f
            }
            canvas.drawText("Authorised Signatory — $firmName", 380f, y, boldBodyPaint)
        }

        pdf.finishPage(page)

        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safeDocNo = txn.docNumber.replace(Regex("[^A-Za-z0-9_-]"), "_")
        val outFile = File(dir, "Voucher_${safeDocNo}.pdf")
        FileOutputStream(outFile).use { pdf.writeTo(it) }
        pdf.close()

        shareFileSafe(context, outFile, "application/pdf", "Share ${txn.docNumber}")
        return outFile
    }

    fun exportAndShareSaleReportPdf(
        context: Context,
        company: CompanyEntity?,
        transactions: List<TxnEntity>,
        dateRangeLabel: String
    ): File {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(27, 27, 47)
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(35, 38, 58)
            textSize = 9.5f
        }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(20, 22, 38)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val salesDocs = transactions.filter { !it.isDeleted && it.status != "Cancelled" && SalesDocTypes.segmentForDocType(it.docType) == "Sales" }
        val totalSales = Money(salesDocs.sumOf { it.grandTotalPaise })
        val totalTax = Money(salesDocs.sumOf { it.totalTaxPaise })
        val totalBalance = Money(salesDocs.sumOf { it.balanceAmountPaise })
        val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        var y = 40f
        canvas.drawText("${company?.businessName ?: "Vanijya"} — Sale Report ($dateRangeLabel)", 32f, y, titlePaint)
        y += 18f
        canvas.drawText(
            "Invoices: ${salesDocs.size}   |   Gross Sales: ${totalSales.formatIndian()}   |   GST: ${totalTax.formatIndian()}   |   Balance Due: ${totalBalance.formatIndian()}",
            32f,
            y,
            boldPaint
        )
        y += 20f

        salesDocs.take(32).forEach { doc ->
            canvas.drawText(
                "${dateFmt.format(Date(doc.txnDate))}  |  ${doc.docNumber.padEnd(14)}  |  ${doc.partyName.take(22).padEnd(22)}  |  ${Money(doc.grandTotalPaise).formatIndian()}  (${doc.status})",
                32f,
                y,
                bodyPaint
            )
            y += 15f
        }

        pdf.finishPage(page)
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val outFile = File(dir, "Sale_Report_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outFile).use { pdf.writeTo(it) }
        pdf.close()

        shareFileSafe(context, outFile, "application/pdf", "Share Sale Report PDF")
        return outFile
    }

    fun exportAndShareBillWiseProfitPdf(
        context: Context,
        company: CompanyEntity?,
        transactions: List<TxnEntity>,
        dateRangeLabel: String
    ): File {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(27, 27, 47)
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(35, 38, 58)
            textSize = 9.5f
        }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(16, 124, 65)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val salesDocs = transactions.filter { !it.isDeleted && it.status != "Cancelled" && SalesDocTypes.segmentForDocType(it.docType) == "Sales" }
        val totalTaxable = Money(salesDocs.sumOf { it.taxableSubtotalPaise })
        val totalCost = Money(salesDocs.sumOf { it.totalCostPaise })
        val totalProfit = Money(salesDocs.sumOf { it.grossProfitPaise })

        var y = 40f
        canvas.drawText("${company?.businessName ?: "Vanijya"} — Bill-wise Profit ($dateRangeLabel)", 32f, y, titlePaint)
        y += 18f
        canvas.drawText(
            "Net Revenue: ${totalTaxable.formatIndian()}   |   COGS Cost: ${totalCost.formatIndian()}   |   Gross Profit: ${totalProfit.formatIndian()}",
            32f,
            y,
            boldPaint
        )
        y += 20f

        salesDocs.take(32).forEach { doc ->
            canvas.drawText(
                "${doc.docNumber.padEnd(14)} | ${doc.partyName.take(20).padEnd(20)} | Rev: ${Money(doc.taxableSubtotalPaise).formatIndian()} | Cost: ${Money(doc.totalCostPaise).formatIndian()} | Profit: ${Money(doc.grossProfitPaise).formatIndian()}",
                32f,
                y,
                bodyPaint
            )
            y += 15f
        }

        pdf.finishPage(page)
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val outFile = File(dir, "Bill_Wise_Profit_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outFile).use { pdf.writeTo(it) }
        pdf.close()

        shareFileSafe(context, outFile, "application/pdf", "Share Bill-wise Profit PDF")
        return outFile
    }

    fun exportSalesCsvAndShare(
        context: Context,
        transactions: List<TxnEntity>,
        fileNamePrefix: String = "Sales_Register"
    ): File {
        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val sb = StringBuilder()
        sb.appendLine("DocNumber,DocType,Date,PartyName,PartyGSTIN,SupplyType,Classification,TaxableValue,CGST,SGST,IGST,Cess,GrandTotal,Paid,Balance,GrossProfit,Status")
        transactions.filter { !it.isDeleted }.forEach { t ->
            sb.appendLine(
                listOf(
                    escapeCsv(t.docNumber),
                    escapeCsv(t.docType),
                    dateFmt.format(Date(t.txnDate)),
                    escapeCsv(t.partyName),
                    escapeCsv(t.partyGstin),
                    t.supplyType,
                    t.classification,
                    "%.2f".format(Locale.US, t.taxableSubtotalPaise / 100.0),
                    "%.2f".format(Locale.US, t.cgstTotalPaise / 100.0),
                    "%.2f".format(Locale.US, t.sgstTotalPaise / 100.0),
                    "%.2f".format(Locale.US, t.igstTotalPaise / 100.0),
                    "%.2f".format(Locale.US, t.cessTotalPaise / 100.0),
                    "%.2f".format(Locale.US, t.grandTotalPaise / 100.0),
                    "%.2f".format(Locale.US, t.paidAmountPaise / 100.0),
                    "%.2f".format(Locale.US, t.balanceAmountPaise / 100.0),
                    "%.2f".format(Locale.US, t.grossProfitPaise / 100.0),
                    t.status
                ).joinToString(",")
            )
        }
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val outFile = File(dir, "${fileNamePrefix}_${System.currentTimeMillis()}.csv")
        outFile.writeText(sb.toString())
        shareFileSafe(context, outFile, "text/csv", "Share $fileNamePrefix CSV")
        return outFile
    }

    private fun escapeCsv(raw: String): String {
        val cleaned = raw.replace("\"", "\"\"")
        return if (cleaned.contains(",") || cleaned.contains("\n")) "\"$cleaned\"" else cleaned
    }

    private fun shareFileSafe(context: Context, file: File, mimeType: String, chooserTitle: String) {
        runCatching {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(
                Intent.createChooser(intent, chooserTitle).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
    }
}

object RecurringInvoiceNotificationHelper {
    private const val CHANNEL_ID = "vanijya_recurring_invoices"

    fun postRecurringGeneratedNotification(
        context: Context,
        templateTitle: String,
        docNumber: String,
        partyName: String,
        amount: Money
    ) {
        runCatching {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Recurring GST Invoices",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Offline alerts when scheduled recurring invoices are generated"
                }
                manager.createNotificationChannel(channel)
            }
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Recurring Invoice Generated: $docNumber")
                .setContentText("$templateTitle for $partyName (${amount.formatIndian()}) saved to vault.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            manager.notify(docNumber.hashCode(), notification)
        }
    }
}
