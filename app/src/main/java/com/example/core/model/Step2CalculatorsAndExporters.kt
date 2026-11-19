package com.example.core.model

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.core.database.CompanyEntity
import com.example.core.database.HsnMasterEntity
import com.example.core.database.ItemBatchEntity
import com.example.core.database.ItemEntity
import com.example.core.database.PartyEntity
import com.example.core.database.PartyLedgerEntryEntity
import com.example.core.database.StockLedgerEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.abs

// ============================================================================
// 1. PARTY LEDGER RUNNING BALANCE, AGING ANALYSIS & CREDIT LIMIT HOOK
// ============================================================================

data class AgingBreakdown(
    val bucket0To30: Money = Money.ZERO,
    val bucket31To60: Money = Money.ZERO,
    val bucket61To90: Money = Money.ZERO,
    val bucket90Plus: Money = Money.ZERO
) {
    val total: Money
        get() = bucket0To30 + bucket31To60 + bucket61To90 + bucket90Plus
}

sealed class CreditLimitCheckResult {
    data class WithinLimit(
        val currentReceivable: Money,
        val projectedReceivable: Money,
        val creditLimit: Money
    ) : CreditLimitCheckResult()

    data class Exceeded(
        val partyName: String,
        val currentReceivable: Money,
        val projectedReceivable: Money,
        val creditLimit: Money,
        val exceededBy: Money
    ) : CreditLimitCheckResult()
}

object PartyLedgerCalculator {

    private const val DAY_MS = 86_400_000L

    /**
     * Pure integer-paise calculation of chronological running balances for a party's ledger.
     * Debit (+) increases receivable (To Collect); Credit (-) increases payable (To Pay).
     */
    fun computeRunningBalances(
        entries: List<PartyLedgerEntryEntity>
    ): List<PartyLedgerEntryEntity> {
        val sorted = entries
            .filter { !it.isDeleted }
            .sortedWith(compareBy<PartyLedgerEntryEntity> { it.entryDate }.thenBy { it.createdAt })
        var runningPaise = 0L
        return sorted.map { entry ->
            val netDelta = Math.subtractExact(entry.debitPaise, entry.creditPaise)
            runningPaise = Math.addExact(runningPaise, netDelta)
            entry.copy(runningBalancePaise = runningPaise)
        }
    }

    /**
     * Computes net party balance in integer paise from ledger entries.
     * Positive = To Collect (Receivable), Negative = To Pay (Payable).
     */
    fun computeClosingBalancePaise(entries: List<PartyLedgerEntryEntity>): Long {
        return computeRunningBalances(entries).lastOrNull()?.runningBalancePaise ?: 0L
    }

    /**
     * Computes 0-30, 31-60, 61-90, and 90+ day aging buckets using FIFO settlement
     * of open debits (for receivable parties) or open credits (for payable parties).
     */
    fun computeAgingBuckets(
        entries: List<PartyLedgerEntryEntity>,
        asOfEpochMs: Long = System.currentTimeMillis()
    ): AgingBreakdown {
        val active = entries
            .filter { !it.isDeleted }
            .sortedWith(compareBy<PartyLedgerEntryEntity> { it.entryDate }.thenBy { it.createdAt })
        if (active.isEmpty()) return AgingBreakdown()

        val totalDebit = active.sumOf { it.debitPaise }
        val totalCredit = active.sumOf { it.creditPaise }
        val netPaise = totalDebit - totalCredit
        if (netPaise == 0L) return AgingBreakdown()

        val isNetReceivable = netPaise > 0L
        var remainingOffsetPaise = if (isNetReceivable) totalCredit else totalDebit

        var b0To30 = 0L
        var b31To60 = 0L
        var b61To90 = 0L
        var b90Plus = 0L

        active.forEach { entry ->
            val rawOpen = if (isNetReceivable) entry.debitPaise else entry.creditPaise
            if (rawOpen <= 0L) return@forEach

            val settledPortion = minOf(rawOpen, remainingOffsetPaise)
            remainingOffsetPaise -= settledPortion
            val unsettledPaise = rawOpen - settledPortion
            if (unsettledPaise <= 0L) return@forEach

            val ageDays = ((asOfEpochMs - entry.entryDate).coerceAtLeast(0L)) / DAY_MS
            when {
                ageDays <= 30L -> b0To30 = Math.addExact(b0To30, unsettledPaise)
                ageDays <= 60L -> b31To60 = Math.addExact(b31To60, unsettledPaise)
                ageDays <= 90L -> b61To90 = Math.addExact(b61To90, unsettledPaise)
                else -> b90Plus = Math.addExact(b90Plus, unsettledPaise)
            }
        }

        return AgingBreakdown(
            bucket0To30 = Money(b0To30),
            bucket31To60 = Money(b31To60),
            bucket61To90 = Money(b61To90),
            bucket90Plus = Money(b90Plus)
        )
    }

    /**
     * Credit-limit warning hook for billing: checks if adding `newInvoiceAmount`
     * pushes the customer's receivable balance above `party.creditLimitPaise`.
     */
    fun evaluateCreditLimitWarning(
        party: PartyEntity,
        newInvoiceAmount: Money
    ): CreditLimitCheckResult {
        val currentReceivablePaise = party.currentBalancePaise.coerceAtLeast(0L)
        val projectedPaise = Math.addExact(currentReceivablePaise, newInvoiceAmount.paise.coerceAtLeast(0L))
        val limitPaise = party.creditLimitPaise

        return if (limitPaise > 0L && projectedPaise > limitPaise) {
            CreditLimitCheckResult.Exceeded(
                partyName = party.name,
                currentReceivable = Money(currentReceivablePaise),
                projectedReceivable = Money(projectedPaise),
                creditLimit = Money(limitPaise),
                exceededBy = Money(projectedPaise - limitPaise)
            )
        } else {
            CreditLimitCheckResult.WithinLimit(
                currentReceivable = Money(currentReceivablePaise),
                projectedReceivable = Money(projectedPaise),
                creditLimit = Money(limitPaise)
            )
        }
    }

    /**
     * Generates offline payment-reminder message templates (Gentle, Standard, Urgent, Hindi)
     * ready to be shared through Android's system share sheet (WhatsApp / SMS / Email).
     */
    fun buildPaymentReminderMessage(
        party: PartyEntity,
        companyName: String,
        upiId: String,
        templateStyle: String
    ): String {
        val dueAmount = Money(abs(party.currentBalancePaise)).formatIndian()
        val upiLine = if (upiId.isNotBlank()) "\nUPI ID for instant payment: $upiId" else ""
        return when (templateStyle) {
            "GENTLE" ->
                "Namaste ${party.name},\n\nFriendly reminder from $companyName: your account shows an outstanding balance of $dueAmount (Credit period: ${party.creditDays} days). Kindly arrange settlement at your convenience.$upiLine\n\nThank you for your business!"
            "URGENT" ->
                "URGENT REMINDER — $companyName\n\nDear ${party.name}, an overdue balance of $dueAmount is pending on your ledger. Please clear the dues immediately to avoid credit hold on upcoming dispatches.$upiLine\n\nRegards,\n$companyName"
            "HINDI" ->
                "नमस्ते ${party.name},\n\n$companyName की ओर से विनम्र अनुस्मारक: आपके खाते में $dueAmount की बकाया राशि देय है। कृपया समय पर भुगतान कर सहयोग प्रदान करें।$upiLine\n\nधन्यवाद,\n$companyName"
            else ->
                "Dear ${party.name},\n\nGreetings from $companyName. As per our books, an amount of $dueAmount is due for collection. Please share the payment reference once transferred.$upiLine\n\nWarm regards,\n$companyName"
        }
    }
}

// ============================================================================
// 2. STOCK LEDGER MOVEMENTS & DASHBOARD REAL-TIME VALUATION CALCULATOR
// ============================================================================

data class RealTimeStockMetrics(
    val totalStockValue: Money,
    val totalRetailValue: Money,
    val lowStockItems: List<ItemEntity>,
    val expiringBatches: List<ItemBatchEntity>,
    val totalProductCount: Int,
    val totalServiceCount: Int
)

object StockLedgerCalculator {

    private const val THIRTY_DAYS_MS = 30L * 86_400_000L

    /**
     * Pure integer calculation of chronological stock ledger closing quantities for an item.
     */
    fun computeRunningStockLedger(
        movements: List<StockLedgerEntity>
    ): List<StockLedgerEntity> {
        val sorted = movements
            .filter { !it.isDeleted }
            .sortedWith(compareBy<StockLedgerEntity> { it.movementDate }.thenBy { it.createdAt })
        var runningQty = 0L
        return sorted.map { m ->
            runningQty += m.qtyDelta
            m.copy(closingQty = runningQty)
        }
    }

    /**
     * Computes real-time stock value (at purchase price in integer paise), low-stock items,
     * and batches expiring within 30 days (or already expired) for the Dashboard & Items screen.
     */
    fun computeRealTimeStockMetrics(
        items: List<ItemEntity>,
        batches: List<ItemBatchEntity>,
        asOfEpochMs: Long = System.currentTimeMillis()
    ): RealTimeStockMetrics {
        val activeItems = items.filter { !it.isDeleted }
        val products = activeItems.filter { it.itemType != "SERVICE" }
        val services = activeItems.filter { it.itemType == "SERVICE" }

        var stockCostPaise = 0L
        var stockRetailPaise = 0L
        products.forEach { item ->
            val positiveQty = item.currentStockQty.coerceAtLeast(0L)
            stockCostPaise = Math.addExact(
                stockCostPaise,
                Math.multiplyExact(positiveQty, item.purchasePricePaise)
            )
            stockRetailPaise = Math.addExact(
                stockRetailPaise,
                Math.multiplyExact(positiveQty, item.salePricePaise)
            )
        }

        val lowStock = products.filter { item ->
            item.currentStockQty <= item.minStockAlertQty
        }

        val expiryThreshold = asOfEpochMs + THIRTY_DAYS_MS
        val expiring = batches.filter { batch ->
            !batch.isDeleted &&
                batch.availableQty > 0L &&
                batch.expiryDate in 1L..expiryThreshold
        }.sortedBy { it.expiryDate }

        return RealTimeStockMetrics(
            totalStockValue = Money(stockCostPaise),
            totalRetailValue = Money(stockRetailPaise),
            lowStockItems = lowStock,
            expiringBatches = expiring,
            totalProductCount = products.size,
            totalServiceCount = services.size
        )
    }
}

// ============================================================================
// 3. BUNDLED HSN/SAC ASSET LOADER
// ============================================================================

object BundledHsnDirectoryLoader {

    fun loadFromAssets(context: Context, companyId: String): List<HsnMasterEntity> {
        val now = System.currentTimeMillis()
        return runCatching {
            context.assets.open("hsn_sac_directory.csv").bufferedReader().useLines { lines ->
                lines.drop(1) // Skip CSV header
                    .mapNotNull { line ->
                        val parts = line.split(",")
                        if (parts.size < 4) return@mapNotNull null
                        val code = parts[0].trim()
                        val desc = parts.subList(1, parts.size - 2).joinToString(",").trim()
                        val bps = parts[parts.size - 2].trim().toIntOrNull() ?: 1800
                        val isService = parts.last().trim().equals("true", ignoreCase = true)
                        HsnMasterEntity(
                            id = "${companyId}_hsn_$code",
                            companyId = companyId,
                            hsnCode = code,
                            description = desc,
                            defaultTaxRateBps = bps,
                            isService = isService,
                            createdAt = now,
                            updatedAt = now
                        )
                    }.toList()
            }
        }.getOrElse { emptyList() }
    }
}

// ============================================================================
// 4. OFFLINE PDF STATEMENT & REPORT GENERATOR (Android PdfDocument + Share Sheet)
// ============================================================================

object VanijyaPdfExporter {

    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

    fun exportAndSharePartyStatementPdf(
        context: Context,
        company: CompanyEntity?,
        party: PartyEntity,
        entries: List<PartyLedgerEntryEntity>,
        aging: AgingBreakdown,
        dateRangeLabel: String
    ): Result<File> = runCatching {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 72dpi
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 18f
            color = android.graphics.Color.rgb(27, 27, 47)
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 10f
            color = android.graphics.Color.rgb(74, 83, 110)
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 11f
            color = android.graphics.Color.rgb(91, 63, 217)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textSize = 9.5f
            color = android.graphics.Color.rgb(27, 27, 47)
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(210, 215, 225)
            strokeWidth = 1f
        }

        var y = 40f
        val firmName = company?.businessName?.ifBlank { "Vanijya Business Vault" } ?: "Vanijya Business Vault"
        canvas.drawText("$firmName — PARTY LEDGER STATEMENT", 36f, y, titlePaint)
        y += 16f
        canvas.drawText(
            "GSTIN: ${company?.gstin?.ifBlank { "URD" } ?: "URD"} • State: ${company?.stateName ?: "Maharashtra"} • Period: $dateRangeLabel",
            36f,
            y,
            subtitlePaint
        )
        y += 14f
        canvas.drawLine(36f, y, 559f, y, linePaint)
        y += 18f

        // Party Metadata
        canvas.drawText("Party: ${party.name} (${party.partyType})", 36f, y, headerPaint)
        y += 14f
        canvas.drawText(
            "Phone: ${party.phone.ifBlank { "-" }} • GSTIN: ${party.gstin.ifBlank { "Unregistered" }} • PAN: ${party.pan.ifBlank { "-" }}",
            36f,
            y,
            subtitlePaint
        )
        y += 14f
        val netMoney = Money(abs(party.currentBalancePaise)).formatIndian()
        val netDir = when {
            party.currentBalancePaise > 0L -> "TO COLLECT (Receivable)"
            party.currentBalancePaise < 0L -> "TO PAY (Payable)"
            else -> "SETTLED"
        }
        canvas.drawText(
            "Closing Balance: $netMoney [$netDir] • Credit Limit: ${Money(party.creditLimitPaise).formatIndian()} (${party.creditDays}d)",
            36f,
            y,
            headerPaint
        )
        y += 16f

        // Aging Summary Box
        canvas.drawText(
            "Aging Analysis:  0-30d: ${aging.bucket0To30.formatIndian()}  |  31-60d: ${aging.bucket31To60.formatIndian()}  |  61-90d: ${aging.bucket61To90.formatIndian()}  |  90+d: ${aging.bucket90Plus.formatIndian()}",
            36f,
            y,
            subtitlePaint
        )
        y += 14f
        canvas.drawLine(36f, y, 559f, y, linePaint)
        y += 18f

        // Table Header
        canvas.drawText("DATE", 36f, y, headerPaint)
        canvas.drawText("VOUCHER", 115f, y, headerPaint)
        canvas.drawText("TYPE", 205f, y, headerPaint)
        canvas.drawText("DEBIT (+)", 315f, y, headerPaint)
        canvas.drawText("CREDIT (-)", 400f, y, headerPaint)
        canvas.drawText("BALANCE", 485f, y, headerPaint)
        y += 8f
        canvas.drawLine(36f, y, 559f, y, linePaint)
        y += 16f

        val computed = PartyLedgerCalculator.computeRunningBalances(entries)
        computed.take(28).forEach { entry ->
            val dateStr = dateFormatter.format(Date(entry.entryDate))
            val drStr = if (entry.debitPaise > 0L) Money(entry.debitPaise).formatIndian() else "-"
            val crStr = if (entry.creditPaise > 0L) Money(entry.creditPaise).formatIndian() else "-"
            val balSuffix = if (entry.runningBalancePaise >= 0L) "Dr" else "Cr"
            val balStr = "${Money(abs(entry.runningBalancePaise)).formatIndian()} $balSuffix"

            canvas.drawText(dateStr, 36f, y, bodyPaint)
            canvas.drawText(entry.voucherNumber.take(12), 115f, y, bodyPaint)
            canvas.drawText(entry.voucherType.take(14), 205f, y, bodyPaint)
            canvas.drawText(drStr, 315f, y, bodyPaint)
            canvas.drawText(crStr, 400f, y, bodyPaint)
            canvas.drawText(balStr, 485f, y, bodyPaint)
            y += 15f
        }

        y += 10f
        canvas.drawLine(36f, y, 559f, y, linePaint)
        y += 16f
        canvas.drawText(
            "Generated 100% offline by Vanijya on ${dateFormatter.format(Date())}",
            36f,
            y,
            subtitlePaint
        )

        pdfDocument.finishPage(page)

        val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safePartyName = party.name.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(24)
        val outFile = File(exportsDir, "Party_Statement_${safePartyName}.pdf")
        FileOutputStream(outFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        shareFileViaChooser(
            context = context,
            file = outFile,
            mimeType = "application/pdf",
            chooserTitle = "Share Party Statement PDF (${party.name})",
            subject = "Ledger Statement - ${party.name}"
        )
        outFile
    }

    fun exportAndShareStockSummaryPdf(
        context: Context,
        company: CompanyEntity?,
        items: List<ItemEntity>,
        metrics: RealTimeStockMetrics
    ): Result<File> = runCatching {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 17f
            color = android.graphics.Color.rgb(27, 27, 47)
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 10.5f
            color = android.graphics.Color.rgb(15, 181, 127)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textSize = 9.5f
            color = android.graphics.Color.rgb(27, 27, 47)
        }

        var y = 40f
        val firmName = company?.businessName?.ifBlank { "Vanijya Business Vault" } ?: "Vanijya Business Vault"
        canvas.drawText("$firmName — STOCK VALUATION & LOW-STOCK REPORT", 36f, y, titlePaint)
        y += 18f
        canvas.drawText(
            "Stock Value (Cost): ${metrics.totalStockValue.formatIndian()} • Low Stock SKUs: ${metrics.lowStockItems.size} • Expiring Batches: ${metrics.expiringBatches.size}",
            36f,
            y,
            headerPaint
        )
        y += 22f

        canvas.drawText("ITEM NAME", 36f, y, headerPaint)
        canvas.drawText("HSN", 220f, y, headerPaint)
        canvas.drawText("STOCK QTY", 285f, y, headerPaint)
        canvas.drawText("PURCHASE", 375f, y, headerPaint)
        canvas.drawText("SALE PRICE", 465f, y, headerPaint)
        y += 16f

        items.take(34).forEach { item ->
            canvas.drawText(item.name.take(24), 36f, y, bodyPaint)
            canvas.drawText(item.hsnSacCode.ifBlank { "-" }, 220f, y, bodyPaint)
            canvas.drawText("${item.currentStockQty} ${item.primaryUnit}", 285f, y, bodyPaint)
            canvas.drawText(Money(item.purchasePricePaise).formatIndian(), 375f, y, bodyPaint)
            canvas.drawText(Money(item.salePricePaise).formatIndian(), 465f, y, bodyPaint)
            y += 15f
        }

        pdfDocument.finishPage(page)
        val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val outFile = File(exportsDir, "Vanijya_Stock_Valuation_Report.pdf")
        FileOutputStream(outFile).use { out -> pdfDocument.writeTo(out) }
        pdfDocument.close()

        shareFileViaChooser(
            context = context,
            file = outFile,
            mimeType = "application/pdf",
            chooserTitle = "Share Stock Valuation PDF",
            subject = "Stock Valuation Report"
        )
        outFile
    }

    fun sharePlainTextViaChooser(
        context: Context,
        text: String,
        chooserTitle: String
    ) {
        runCatching {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }
    }

    fun shareFileViaChooser(
        context: Context,
        file: File,
        mimeType: String,
        chooserTitle: String,
        subject: String
    ) {
        runCatching {
            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }
    }
}

// ============================================================================
// 5. CSV / EXCEL IMPORT, EXPORT, TEMPLATES & DUPLICATE DETECTION
// ============================================================================

data class PartyImportPreviewRow(
    val name: String,
    val partyType: String,
    val phone: String,
    val gstin: String,
    val stateCode: String,
    val stateName: String,
    val openingBalance: Money,
    val isToCollect: Boolean,
    val creditLimit: Money,
    val creditDays: Int,
    val isDuplicate: Boolean
)

data class ItemImportPreviewRow(
    val name: String,
    val itemCode: String,
    val hsnSacCode: String,
    val categoryName: String,
    val primaryUnit: String,
    val secondaryUnit: String,
    val conversionRate: Int,
    val salePrice: Money,
    val purchasePrice: Money,
    val wholesalePrice: Money,
    val mrp: Money,
    val taxRateBps: Int,
    val openingStockQty: Long,
    val minStockAlertQty: Long,
    val barcode: String,
    val duplicateReason: String? // null if unique
)

object VanijyaCsvPorters {

    const val PARTY_CSV_TEMPLATE =
        "Name,Type(CUSTOMER/SUPPLIER/BOTH),Phone,GSTIN,OpeningBalanceRupees,Direction(COLLECT/PAY),CreditLimitRupees,CreditDays,Group\n" +
            "Reliance Metro Cash & Carry,SUPPLIER,9820198201,27AAACR5055K1Z7,38500,PAY,200000,30,FMCG Distributors\n" +
            "Annapurna Super Store,CUSTOMER,9876543219,27AADCA2233B1Z5,16400,COLLECT,100000,15,Retail Buyers\n" +
            "Surya Electricals & Hardware,BOTH,9811122233,24AABCS1429B1Z1,9200,COLLECT,75000,30,Wholesale Buyers"

    const val ITEM_CSV_TEMPLATE =
        "Name,Code,HSN,Category,PrimaryUnit,SecondaryUnit,ConversionRate,SalePriceRupees,PurchasePriceRupees,WholesalePriceRupees,MRPRupees,TaxPercent,OpeningStock,MinStockAlert,Barcode\n" +
            "Aashirvaad Shudh Chakki Atta 10kg,SKU-ATTA-10,1101,Kirana & Staples,BAG,BOX,4,465,410,445,495,5,45,10,8901725121242\n" +
            "Havells 9W LED Bulb Cool Day Light,SKU-LED-9W,8539,Electricals,PCS,BOX,12,85,58,74,120,12,120,24,8901234560098\n" +
            "Pidilite Fevicol SH 1kg Jar,SKU-FEV-1KG,3506,Hardware & Adhesives,PCS,BOX,10,270,215,250,295,18,18,6,8901860010258"

    fun parsePartyCsvPreview(
        csvText: String,
        existingParties: List<PartyEntity>
    ): List<PartyImportPreviewRow> {
        val existingNames = existingParties.map { it.name.trim().lowercase() }.toSet()
        val existingGstins = existingParties.mapNotNull { it.gstin.trim().uppercase().takeIf(String::isNotEmpty) }.toSet()

        return csvText.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("Name,", ignoreCase = true) }
            .mapNotNull { line ->
                val cols = line.split(",").map { it.trim() }
                if (cols.isEmpty() || cols[0].isBlank()) return@mapNotNull null
                val name = cols[0]
                val type = cols.getOrNull(1)?.uppercase()?.takeIf { it in setOf("CUSTOMER", "SUPPLIER", "BOTH") } ?: "CUSTOMER"
                val phone = cols.getOrNull(2)?.filter(Char::isDigit)?.take(10) ?: ""
                val gstin = cols.getOrNull(3)?.uppercase()?.take(15) ?: ""
                val openingMoney = runCatching { Money.parseInput(cols.getOrNull(4) ?: "0") }.getOrDefault(Money.ZERO)
                val isCollect = !(cols.getOrNull(5)?.uppercase()?.contains("PAY") ?: (type == "SUPPLIER"))
                val creditLimit = runCatching { Money.parseInput(cols.getOrNull(6) ?: "50000") }.getOrDefault(Money.fromRupees(50_000L))
                val creditDays = cols.getOrNull(7)?.toIntOrNull() ?: 30
                val detectedState = Validators.extractStateFromGstin(gstin)
                val isDup = name.lowercase() in existingNames || (gstin.isNotEmpty() && gstin in existingGstins)

                PartyImportPreviewRow(
                    name = name,
                    partyType = type,
                    phone = phone,
                    gstin = gstin,
                    stateCode = detectedState?.code ?: "27",
                    stateName = detectedState?.name ?: "Maharashtra",
                    openingBalance = openingMoney,
                    isToCollect = isCollect,
                    creditLimit = creditLimit,
                    creditDays = creditDays,
                    isDuplicate = isDup
                )
            }.toList()
    }

    fun parseItemCsvPreview(
        csvText: String,
        existingItems: List<ItemEntity>
    ): List<ItemImportPreviewRow> {
        val byName = existingItems.associateBy { it.name.trim().lowercase() }
        val byCode = existingItems.filter { it.itemCode.isNotBlank() }.associateBy { it.itemCode.trim().uppercase() }
        val byBarcode = existingItems.filter { it.barcode.isNotBlank() }.associateBy { it.barcode.trim() }

        return csvText.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("Name,", ignoreCase = true) }
            .mapNotNull { line ->
                val cols = line.split(",").map { it.trim() }
                if (cols.isEmpty() || cols[0].isBlank()) return@mapNotNull null
                val name = cols[0]
                val code = cols.getOrNull(1)?.uppercase() ?: ""
                val hsn = cols.getOrNull(2) ?: ""
                val cat = cols.getOrNull(3)?.ifBlank { "General" } ?: "General"
                val pUnit = cols.getOrNull(4)?.uppercase()?.ifBlank { "PCS" } ?: "PCS"
                val sUnit = cols.getOrNull(5)?.uppercase() ?: ""
                val conv = cols.getOrNull(6)?.toIntOrNull()?.coerceAtLeast(1) ?: 1
                val sale = runCatching { Money.parseInput(cols.getOrNull(7) ?: "0") }.getOrDefault(Money.ZERO)
                val purchase = runCatching { Money.parseInput(cols.getOrNull(8) ?: "0") }.getOrDefault(Money.ZERO)
                val wholesale = runCatching { Money.parseInput(cols.getOrNull(9) ?: "0") }.getOrDefault(Money.ZERO)
                val mrp = runCatching { Money.parseInput(cols.getOrNull(10) ?: "0") }.getOrDefault(sale)
                val taxPercent = cols.getOrNull(11)?.toIntOrNull() ?: 18
                val openingQty = cols.getOrNull(12)?.toLongOrNull() ?: 0L
                val minAlert = cols.getOrNull(13)?.toLongOrNull() ?: 5L
                val barcode = cols.getOrNull(14) ?: ""

                val dupReason = when {
                    code.isNotEmpty() && code in byCode -> "Duplicate SKU Code ($code)"
                    barcode.isNotEmpty() && barcode in byBarcode -> "Duplicate Barcode ($barcode)"
                    name.lowercase() in byName -> "Duplicate Item Name"
                    else -> null
                }

                ItemImportPreviewRow(
                    name = name,
                    itemCode = code,
                    hsnSacCode = hsn,
                    categoryName = cat,
                    primaryUnit = pUnit,
                    secondaryUnit = sUnit,
                    conversionRate = conv,
                    salePrice = sale,
                    purchasePrice = purchase,
                    wholesalePrice = wholesale,
                    mrp = mrp,
                    taxRateBps = taxPercent * 100,
                    openingStockQty = openingQty,
                    minStockAlertQty = minAlert,
                    barcode = barcode,
                    duplicateReason = dupReason
                )
            }.toList()
    }

    fun exportPartiesCsvAndShare(context: Context, parties: List<PartyEntity>): Result<File> = runCatching {
        val sb = StringBuilder()
        sb.appendLine("Name,Type,Phone,Email,GSTIN,PAN,State,Group,CurrentBalanceRupees,Direction,CreditLimitRupees,CreditDays")
        parties.forEach { p ->
            val balRupees = abs(p.currentBalancePaise) / 100L
            val dir = if (p.currentBalancePaise >= 0L) "TO_COLLECT" else "TO_PAY"
            val limitRupees = p.creditLimitPaise / 100L
            sb.appendLine(
                "${p.name.replace(",", " ")},${p.partyType},${p.phone},${p.email},${p.gstin},${p.pan},${p.stateName},${p.groupName},$balRupees,$dir,$limitRupees,${p.creditDays}"
            )
        }
        val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportsDir, "Vanijya_Parties_Master.csv")
        file.writeText(sb.toString())
        VanijyaPdfExporter.shareFileViaChooser(
            context = context,
            file = file,
            mimeType = "text/csv",
            chooserTitle = "Export Parties CSV / Excel",
            subject = "Vanijya Parties Master CSV"
        )
        file
    }

    fun exportItemsCsvAndShare(
        context: Context,
        items: List<ItemEntity>,
        templateOnly: Boolean = false
    ): Result<File> = runCatching {
        val content = if (templateOnly) {
            ITEM_CSV_TEMPLATE
        } else {
            buildString {
                appendLine("Name,Code,HSN,Category,PrimaryUnit,SecondaryUnit,ConversionRate,SalePriceRupees,PurchasePriceRupees,WholesalePriceRupees,MRPRupees,TaxPercent,CurrentStock,MinStockAlert,Barcode,Godown")
                items.forEach { item ->
                    appendLine(
                        "${item.name.replace(",", " ")},${item.itemCode},${item.hsnSacCode},${item.categoryName},${item.primaryUnit},${item.secondaryUnit},${item.conversionRate},${item.salePricePaise / 100},${item.purchasePricePaise / 100},${item.wholesalePricePaise / 100},${item.mrpPaise / 100},${item.taxRateBps / 100},${item.currentStockQty},${item.minStockAlertQty},${item.barcode},${item.warehouseName}"
                    )
                }
            }
        }
        val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val fileName = if (templateOnly) "Vanijya_Items_Import_Template.csv" else "Vanijya_Items_Stock_Export.csv"
        val file = File(exportsDir, fileName)
        file.writeText(content)
        VanijyaPdfExporter.shareFileViaChooser(
            context = context,
            file = file,
            mimeType = "text/csv",
            chooserTitle = if (templateOnly) "Download Item CSV/Excel Template" else "Export Items & Stock CSV/Excel",
            subject = fileName
        )
        file
    }
}

// ============================================================================
// 6. OFFLINE BARCODE / QR GENERATOR & LOCAL IMAGE COMPRESSOR
// ============================================================================

object BarcodeAndImageHelper {

    /**
     * Generates a deterministic, high-contrast Code-128 style 1D barcode Bitmap offline.
     */
    fun generateBarcodeBitmap(
        codeText: String,
        widthPx: Int = 520,
        heightPx: Int = 140
    ): Bitmap {
        val normalized = codeText.ifBlank { "8901234567890" }
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(27, 27, 47)
            style = Paint.Style.FILL
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(27, 27, 47)
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = 22f
            textAlign = Paint.Align.CENTER
        }

        // Start guard bars
        var x = 24f
        val barBottom = heightPx - 34f
        canvas.drawRect(x, 14f, x + 4f, barBottom, barPaint)
        x += 8f
        canvas.drawRect(x, 14f, x + 2f, barBottom, barPaint)
        x += 6f

        val availableWidth = (widthPx - 64).toFloat()
        val moduleStep = (availableWidth / (normalized.length * 7).coerceAtLeast(1)).coerceAtLeast(2f)

        normalized.forEachIndexed { index, ch ->
            val hash = (ch.code * 31 + index * 17)
            for (bit in 0 until 6) {
                val isDark = ((hash shr bit) and 1) == 1 || (bit == 0)
                val barW = if (((hash shr (bit + 1)) and 1) == 1) moduleStep * 1.35f else moduleStep * 0.85f
                if (isDark && x + barW < widthPx - 28f) {
                    canvas.drawRect(x, 14f, x + barW, barBottom, barPaint)
                }
                x += moduleStep
            }
        }

        // End guard bars
        canvas.drawRect((widthPx - 28).toFloat(), 14f, (widthPx - 24).toFloat(), barBottom, barPaint)
        canvas.drawText(normalized, widthPx / 2f, heightPx - 8f, textPaint)
        return bitmap
    }

    /**
     * Compresses a user-selected image URI locally into internal storage (`filesDir/item_images/`)
     * at 80% JPEG quality so item photos work offline and stay lightweight for `.vbk` backups.
     */
    fun compressAndSaveLocalItemImage(context: Context, sourceUri: Uri): String {
        return runCatching {
            val imagesDir = File(context.filesDir, "item_images").apply { mkdirs() }
            val outFile = File(imagesDir, "item_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                val original = BitmapFactory.decodeStream(input) ?: return sourceUri.toString()
                val maxDim = 720
                val ratio = minOf(
                    maxDim.toFloat() / original.width.coerceAtLeast(1),
                    maxDim.toFloat() / original.height.coerceAtLeast(1),
                    1f
                )
                val targetW = (original.width * ratio).toInt().coerceAtLeast(1)
                val targetH = (original.height * ratio).toInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(original, targetW, targetH, true)
                FileOutputStream(outFile).use { out ->
                    scaled.compress(Bitmap.CompressFormat.JPEG, 80, out)
                }
                outFile.absolutePath
            } ?: sourceUri.toString()
        }.getOrElse { sourceUri.toString() }
    }
}
