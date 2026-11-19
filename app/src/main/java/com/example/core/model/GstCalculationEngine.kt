package com.example.core.model

import com.example.core.database.RoundingModeOption
import kotlin.math.abs

/**
 * Canonical Sales & Billing document types for Step 3A.
 */
object SalesDocTypes {
    const val TAX_INVOICE = "TAX_INVOICE"
    const val BILL_OF_SUPPLY = "BILL_OF_SUPPLY"
    const val CASH_SALE = "CASH_SALE"
    const val PURCHASE_BILL = "PURCHASE_BILL"
    const val PURCHASE_ORDER = "PURCHASE_ORDER"
    const val SALE_ORDER = "SALE_ORDER"
    const val ESTIMATE = "ESTIMATE"
    const val PROFORMA = "PROFORMA"
    const val DELIVERY_CHALLAN = "DELIVERY_CHALLAN"
    const val CREDIT_NOTE = "CREDIT_NOTE"
    const val DEBIT_NOTE = "DEBIT_NOTE"
    const val PAYMENT_IN = "PAYMENT_IN"

    fun displayLabel(docType: String): String = when (docType) {
        TAX_INVOICE -> "Tax Invoice"
        BILL_OF_SUPPLY -> "Bill of Supply"
        CASH_SALE -> "Retail / Cash Sale"
        PURCHASE_BILL -> "Purchase Bill"
        PURCHASE_ORDER -> "Purchase Order"
        SALE_ORDER -> "Sale Order"
        ESTIMATE -> "Estimate / Quotation"
        PROFORMA -> "Proforma Invoice"
        DELIVERY_CHALLAN -> "Delivery Challan"
        CREDIT_NOTE -> "Sale Return / Credit Note"
        DEBIT_NOTE -> "Debit Note"
        PAYMENT_IN -> "Payment-In"
        else -> docType.replace('_', ' ')
    }

    fun segmentForDocType(docType: String): String = when (docType) {
        TAX_INVOICE, BILL_OF_SUPPLY, CASH_SALE, PURCHASE_BILL -> "Sales"
        ESTIMATE, PROFORMA -> "Quotations"
        SALE_ORDER, PURCHASE_ORDER -> "Orders"
        DELIVERY_CHALLAN -> "Challans"
        CREDIT_NOTE, DEBIT_NOTE -> "Returns"
        PAYMENT_IN -> "Payments"
        else -> "Sales"
    }

    fun seriesDocTypeKey(docType: String): String = when (docType) {
        TAX_INVOICE, BILL_OF_SUPPLY, CASH_SALE -> "SALE_INVOICE"
        PURCHASE_BILL -> "PURCHASE_BILL"
        PURCHASE_ORDER -> "PURCHASE_ORDER"
        ESTIMATE, PROFORMA -> "ESTIMATE"
        SALE_ORDER -> "SALE_ORDER"
        DELIVERY_CHALLAN -> "DELIVERY_CHALLAN"
        CREDIT_NOTE -> "CREDIT_NOTE"
        DEBIT_NOTE -> "DEBIT_NOTE"
        PAYMENT_IN -> "PAYMENT_IN"
        else -> docType
    }

    fun defaultPrefix(docType: String): String = when (docType) {
        TAX_INVOICE -> "INV/26-27/"
        BILL_OF_SUPPLY -> "BOS/26-27/"
        CASH_SALE -> "CSH/26-27/"
        PURCHASE_BILL -> "PUR/26-27/"
        PURCHASE_ORDER -> "PO/26-27/"
        ESTIMATE -> "EST/26-27/"
        PROFORMA -> "PI/26-27/"
        SALE_ORDER -> "SO/26-27/"
        DELIVERY_CHALLAN -> "DC/26-27/"
        CREDIT_NOTE -> "CN/26-27/"
        DEBIT_NOTE -> "DN/26-27/"
        PAYMENT_IN -> "RCPT/26-27/"
        else -> "DOC/26-27/"
    }

    fun affectsStock(docType: String): Boolean =
        docType in setOf(TAX_INVOICE, BILL_OF_SUPPLY, CASH_SALE, PURCHASE_BILL, DELIVERY_CHALLAN, CREDIT_NOTE)

    fun isInwardStock(docType: String): Boolean =
        docType in setOf(CREDIT_NOTE, PURCHASE_BILL)

    fun affectsPartyLedger(docType: String): Boolean =
        docType in setOf(TAX_INVOICE, BILL_OF_SUPPLY, CASH_SALE, PURCHASE_BILL, CREDIT_NOTE, DEBIT_NOTE)

    fun isCreditToPartyLedger(docType: String): Boolean =
        docType in setOf(CREDIT_NOTE, PURCHASE_BILL)

    fun nextConversionTarget(docType: String): String? = when (docType) {
        ESTIMATE, PROFORMA -> SALE_ORDER
        SALE_ORDER -> TAX_INVOICE
        PURCHASE_ORDER -> PURCHASE_BILL
        DELIVERY_CHALLAN -> TAX_INVOICE
        TAX_INVOICE, BILL_OF_SUPPLY, CASH_SALE -> CREDIT_NOTE
        else -> null
    }
}

enum class GstSupplyType {
    INTRA_STATE,
    INTER_STATE,
    EXPORT_WITH_TAX,
    EXPORT_WITHOUT_TAX,
    SEZ_WITH_TAX,
    SEZ_WITHOUT_TAX
}

enum class GstTaxCategory {
    TAXABLE,
    NIL_RATED,
    EXEMPT,
    NON_GST
}

enum class GstInvoiceClassification {
    B2B,
    B2CL,
    B2CS,
    EXPORT,
    SEZ,
    COMPOSITION,
    NIL_EXEMPT
}

data class GstLineInput(
    val lineId: String = "",
    val itemId: String = "",
    val itemName: String,
    val hsnSacCode: String = "",
    val unit: String = "PCS",
    val qty: Long,
    val freeQty: Long = 0L,
    val unitPricePaise: Long,
    val purchaseCostPaise: Long = 0L,
    val taxInclusive: Boolean = false,
    val discountBps: Int = 0,
    val flatDiscountPaise: Long = 0L,
    val taxCategory: GstTaxCategory = GstTaxCategory.TAXABLE,
    val taxRateBps: Int = 1800,
    val cessBps: Int = 0
)

data class GstAdditionalChargeInput(
    val id: String = "",
    val label: String,
    val amountPaise: Long,
    val taxRateBps: Int = 0
)

data class GstDocumentInput(
    val supplierStateCode: String = "27",
    val placeOfSupplyStateCode: String = "27",
    val partyGstin: String = "",
    val isCompositionScheme: Boolean = false,
    val isReverseCharge: Boolean = false,
    val forcedSupplyType: GstSupplyType? = null,
    val lines: List<GstLineInput>,
    val invoiceDiscountBps: Int = 0,
    val invoiceDiscountPaise: Long = 0L,
    val additionalCharges: List<GstAdditionalChargeInput> = emptyList(),
    val tcsBps: Int = 0,
    val tdsBps: Int = 0,
    val roundingMode: RoundingModeOption = RoundingModeOption.NEAREST,
    val roundingEnabled: Boolean = true
)

data class GstCalculatedLine(
    val lineId: String,
    val itemId: String,
    val itemName: String,
    val hsnSacCode: String,
    val unit: String,
    val qty: Long,
    val freeQty: Long,
    val unitPricePaise: Long,
    val purchaseCostPaise: Long,
    val taxInclusive: Boolean,
    val taxCategory: GstTaxCategory,
    val taxRateBps: Int,
    val cessBps: Int,
    val grossAmountPaise: Long,
    val lineDiscountPaise: Long,
    val allocatedInvoiceDiscountPaise: Long,
    val netTaxablePaise: Long,
    val cgstPaise: Long,
    val sgstPaise: Long,
    val igstPaise: Long,
    val cessPaise: Long,
    val totalTaxPaise: Long,
    val lineTotalPaise: Long,
    val lineCostPaise: Long,
    val lineProfitPaise: Long
)

data class GstCalculatedCharge(
    val id: String,
    val label: String,
    val amountPaise: Long,
    val taxRateBps: Int,
    val cgstPaise: Long,
    val sgstPaise: Long,
    val igstPaise: Long,
    val totalWithTaxPaise: Long
)

data class HsnTaxSummaryRow(
    val hsnSacCode: String,
    val description: String,
    val uqc: String,
    val totalQty: Long,
    val taxablePaise: Long,
    val taxRateBps: Int,
    val cgstPaise: Long,
    val sgstPaise: Long,
    val igstPaise: Long,
    val cessPaise: Long,
    val totalTaxPaise: Long
)

data class GstCalculationResult(
    val supplyType: GstSupplyType,
    val classification: GstInvoiceClassification,
    val isCompositionScheme: Boolean,
    val isReverseCharge: Boolean,
    val lines: List<GstCalculatedLine>,
    val charges: List<GstCalculatedCharge>,
    val hsnSummary: List<HsnTaxSummaryRow>,
    val grossSubtotalPaise: Long,
    val lineDiscountTotalPaise: Long,
    val invoiceDiscountPaise: Long,
    val additionalChargesSubtotalPaise: Long,
    val taxableSubtotalPaise: Long,
    val cgstTotalPaise: Long,
    val sgstTotalPaise: Long,
    val igstTotalPaise: Long,
    val cessTotalPaise: Long,
    val totalTaxPaise: Long,
    val tcsPaise: Long,
    val tdsPaise: Long,
    val preRoundGrandTotalPaise: Long,
    val roundOffPaise: Long,
    val grandTotalPaise: Long,
    val totalCostPaise: Long,
    val grossProfitPaise: Long,
    val amountInWords: String
)

/**
 * Pure-Kotlin offline GST Calculation Engine for Vanijya.
 * All intermediate and final calculations use integer paise (`Long`) and basis points (`100 bps = 1%`).
 */
object GstCalculationEngine {

    const val B2CL_THRESHOLD_PAISE: Long = 2_50_000_00L // ₹2,50,000.00

    fun resolveSupplyType(
        supplierStateCode: String,
        placeOfSupplyStateCode: String,
        forcedSupplyType: GstSupplyType? = null
    ): GstSupplyType {
        if (forcedSupplyType != null) return forcedSupplyType
        val sup = supplierStateCode.trim().padStart(2, '0')
        val pos = placeOfSupplyStateCode.trim().padStart(2, '0')
        return if (sup == pos || placeOfSupplyStateCode.isBlank()) {
            GstSupplyType.INTRA_STATE
        } else {
            GstSupplyType.INTER_STATE
        }
    }

    fun calculate(input: GstDocumentInput): GstCalculationResult {
        val isComposition = input.isCompositionScheme
        val supplyType = resolveSupplyType(
            supplierStateCode = input.supplierStateCode,
            placeOfSupplyStateCode = input.placeOfSupplyStateCode,
            forcedSupplyType = input.forcedSupplyType
        )
        val isIntraState = supplyType == GstSupplyType.INTRA_STATE
        val isZeroRatedExportOrSez = supplyType == GstSupplyType.EXPORT_WITHOUT_TAX ||
            supplyType == GstSupplyType.SEZ_WITHOUT_TAX

        data class PreDiscountLine(
            val line: GstLineInput,
            val grossPaise: Long,
            val lineDiscountPaise: Long,
            val netAfterLineDiscountPaise: Long,
            val effectiveTaxRateBps: Int,
            val effectiveCessBps: Int,
            val preInvoiceDiscountTaxablePaise: Long
        )

        val preLines = input.lines.map { line ->
            val safeQty = line.qty.coerceAtLeast(0L)
            val safeRate = line.unitPricePaise.coerceAtLeast(0L)
            val grossPaise = safeQty * safeRate

            val rawLineDiscount = when {
                line.flatDiscountPaise > 0L -> line.flatDiscountPaise
                line.discountBps > 0 -> divideRoundHalfUp(grossPaise * line.discountBps.toLong(), 10_000L)
                else -> 0L
            }.coerceIn(0L, grossPaise)

            val netAfterLineDiscount = (grossPaise - rawLineDiscount).coerceAtLeast(0L)
            val isTaxExemptLine = isComposition ||
                isZeroRatedExportOrSez ||
                line.taxCategory != GstTaxCategory.TAXABLE

            val effectiveTaxRateBps = if (isTaxExemptLine) 0 else line.taxRateBps.coerceAtLeast(0)
            val effectiveCessBps = if (isTaxExemptLine) 0 else line.cessBps.coerceAtLeast(0)
            val combinedRateBps = effectiveTaxRateBps + effectiveCessBps

            val preTaxable = if (line.taxInclusive && combinedRateBps > 0) {
                divideRoundHalfUp(netAfterLineDiscount * 10_000L, 10_000L + combinedRateBps.toLong())
            } else {
                netAfterLineDiscount
            }

            PreDiscountLine(
                line = line,
                grossPaise = grossPaise,
                lineDiscountPaise = rawLineDiscount,
                netAfterLineDiscountPaise = netAfterLineDiscount,
                effectiveTaxRateBps = effectiveTaxRateBps,
                effectiveCessBps = effectiveCessBps,
                preInvoiceDiscountTaxablePaise = preTaxable
            )
        }

        val totalPreInvoiceTaxablePaise = preLines.sumOf { it.preInvoiceDiscountTaxablePaise }
        val totalInvoiceDiscountPaise = when {
            input.invoiceDiscountPaise > 0L ->
                input.invoiceDiscountPaise.coerceAtMost(totalPreInvoiceTaxablePaise)
            input.invoiceDiscountBps > 0 ->
                divideRoundHalfUp(
                    totalPreInvoiceTaxablePaise * input.invoiceDiscountBps.coerceIn(0, 10_000).toLong(),
                    10_000L
                ).coerceAtMost(totalPreInvoiceTaxablePaise)
            else -> 0L
        }

        var remainingInvoiceDiscount = totalInvoiceDiscountPaise
        val calculatedLines = preLines.mapIndexed { idx, pre ->
            val invoiceDiscountShare = if (totalInvoiceDiscountPaise == 0L || totalPreInvoiceTaxablePaise == 0L) {
                0L
            } else if (idx == preLines.lastIndex) {
                remainingInvoiceDiscount.coerceIn(0L, pre.preInvoiceDiscountTaxablePaise)
            } else {
                val share = divideRoundHalfUp(
                    totalInvoiceDiscountPaise * pre.preInvoiceDiscountTaxablePaise,
                    totalPreInvoiceTaxablePaise
                ).coerceIn(0L, pre.preInvoiceDiscountTaxablePaise)
                remainingInvoiceDiscount = (remainingInvoiceDiscount - share).coerceAtLeast(0L)
                share
            }

            val finalTaxablePaise = (pre.preInvoiceDiscountTaxablePaise - invoiceDiscountShare).coerceAtLeast(0L)
            val rateBps = pre.effectiveTaxRateBps
            val cessBps = pre.effectiveCessBps

            val cgstPaise: Long
            val sgstPaise: Long
            val igstPaise: Long

            if (isIntraState) {
                val cgstRateBps = rateBps / 2
                val sgstRateBps = rateBps - cgstRateBps
                if (pre.line.taxInclusive && invoiceDiscountShare == 0L && cessBps == 0 && rateBps > 0) {
                    val exactTotalTax = (pre.netAfterLineDiscountPaise - finalTaxablePaise).coerceAtLeast(0L)
                    cgstPaise = exactTotalTax / 2L
                    sgstPaise = exactTotalTax - cgstPaise
                    igstPaise = 0L
                } else {
                    cgstPaise = divideRoundHalfUp(finalTaxablePaise * cgstRateBps.toLong(), 10_000L)
                    sgstPaise = divideRoundHalfUp(finalTaxablePaise * sgstRateBps.toLong(), 10_000L)
                    igstPaise = 0L
                }
            } else {
                cgstPaise = 0L
                sgstPaise = 0L
                igstPaise = if (pre.line.taxInclusive && invoiceDiscountShare == 0L && cessBps == 0 && rateBps > 0) {
                    (pre.netAfterLineDiscountPaise - finalTaxablePaise).coerceAtLeast(0L)
                } else {
                    divideRoundHalfUp(finalTaxablePaise * rateBps.toLong(), 10_000L)
                }
            }

            val cessPaise = if (cessBps > 0) {
                divideRoundHalfUp(finalTaxablePaise * cessBps.toLong(), 10_000L)
            } else {
                0L
            }

            val totalLineTaxPaise = cgstPaise + sgstPaise + igstPaise + cessPaise
            val collectibleTaxPaise = if (input.isReverseCharge) 0L else totalLineTaxPaise
            val lineTotalPaise = finalTaxablePaise + collectibleTaxPaise

            val totalOutwardUnits = pre.line.qty.coerceAtLeast(0L) + pre.line.freeQty.coerceAtLeast(0L)
            val lineCostPaise = totalOutwardUnits * pre.line.purchaseCostPaise.coerceAtLeast(0L)
            val lineProfitPaise = finalTaxablePaise - lineCostPaise

            GstCalculatedLine(
                lineId = pre.line.lineId,
                itemId = pre.line.itemId,
                itemName = pre.line.itemName,
                hsnSacCode = pre.line.hsnSacCode,
                unit = pre.line.unit,
                qty = pre.line.qty,
                freeQty = pre.line.freeQty,
                unitPricePaise = pre.line.unitPricePaise,
                purchaseCostPaise = pre.line.purchaseCostPaise,
                taxInclusive = pre.line.taxInclusive,
                taxCategory = pre.line.taxCategory,
                taxRateBps = rateBps,
                cessBps = cessBps,
                grossAmountPaise = pre.grossPaise,
                lineDiscountPaise = pre.lineDiscountPaise,
                allocatedInvoiceDiscountPaise = invoiceDiscountShare,
                netTaxablePaise = finalTaxablePaise,
                cgstPaise = cgstPaise,
                sgstPaise = sgstPaise,
                igstPaise = igstPaise,
                cessPaise = cessPaise,
                totalTaxPaise = totalLineTaxPaise,
                lineTotalPaise = lineTotalPaise,
                lineCostPaise = lineCostPaise,
                lineProfitPaise = lineProfitPaise
            )
        }

        val calculatedCharges = input.additionalCharges.map { chg ->
            val basePaise = chg.amountPaise.coerceAtLeast(0L)
            val rateBps = if (isComposition || isZeroRatedExportOrSez) 0 else chg.taxRateBps.coerceAtLeast(0)
            val cgstPaise: Long
            val sgstPaise: Long
            val igstPaise: Long
            if (isIntraState) {
                val halfBps = rateBps / 2
                cgstPaise = divideRoundHalfUp(basePaise * halfBps.toLong(), 10_000L)
                sgstPaise = divideRoundHalfUp(basePaise * (rateBps - halfBps).toLong(), 10_000L)
                igstPaise = 0L
            } else {
                cgstPaise = 0L
                sgstPaise = 0L
                igstPaise = divideRoundHalfUp(basePaise * rateBps.toLong(), 10_000L)
            }
            val totalChargeTax = cgstPaise + sgstPaise + igstPaise
            val collectibleChargeTax = if (input.isReverseCharge) 0L else totalChargeTax
            GstCalculatedCharge(
                id = chg.id,
                label = chg.label,
                amountPaise = basePaise,
                taxRateBps = rateBps,
                cgstPaise = cgstPaise,
                sgstPaise = sgstPaise,
                igstPaise = igstPaise,
                totalWithTaxPaise = basePaise + collectibleChargeTax
            )
        }

        val hsnSummaryFromLines = calculatedLines
            .groupBy {
                Triple(
                    it.hsnSacCode.trim().ifBlank { "UNSPECIFIED" },
                    it.taxRateBps,
                    it.taxCategory
                )
            }
            .map { (key, rows) ->
                val (hsnCode, rateBps, _) = key
                val first = rows.first()
                HsnTaxSummaryRow(
                    hsnSacCode = hsnCode,
                    description = rows.map { it.itemName }.distinct().joinToString(", ").take(80),
                    uqc = first.unit.ifBlank { "PCS" },
                    totalQty = rows.sumOf { it.qty + it.freeQty },
                    taxablePaise = rows.sumOf { it.netTaxablePaise },
                    taxRateBps = rateBps,
                    cgstPaise = rows.sumOf { it.cgstPaise },
                    sgstPaise = rows.sumOf { it.sgstPaise },
                    igstPaise = rows.sumOf { it.igstPaise },
                    cessPaise = rows.sumOf { it.cessPaise },
                    totalTaxPaise = rows.sumOf { it.totalTaxPaise }
                )
            }

        val hsnSummaryFromCharges = calculatedCharges
            .filter { it.amountPaise > 0L }
            .map { chg ->
                HsnTaxSummaryRow(
                    hsnSacCode = "9965",
                    description = chg.label,
                    uqc = "OTH",
                    totalQty = 1L,
                    taxablePaise = chg.amountPaise,
                    taxRateBps = chg.taxRateBps,
                    cgstPaise = chg.cgstPaise,
                    sgstPaise = chg.sgstPaise,
                    igstPaise = chg.igstPaise,
                    cessPaise = 0L,
                    totalTaxPaise = chg.cgstPaise + chg.sgstPaise + chg.igstPaise
                )
            }

        val hsnSummary = hsnSummaryFromLines + hsnSummaryFromCharges

        val grossSubtotalPaise = calculatedLines.sumOf { it.grossAmountPaise }
        val lineDiscountTotalPaise = calculatedLines.sumOf { it.lineDiscountPaise }
        val linesTaxablePaise = calculatedLines.sumOf { it.netTaxablePaise }
        val additionalChargesSubtotalPaise = calculatedCharges.sumOf { it.amountPaise }
        val taxableSubtotalPaise = linesTaxablePaise + additionalChargesSubtotalPaise

        val cgstTotalPaise = calculatedLines.sumOf { it.cgstPaise } + calculatedCharges.sumOf { it.cgstPaise }
        val sgstTotalPaise = calculatedLines.sumOf { it.sgstPaise } + calculatedCharges.sumOf { it.sgstPaise }
        val igstTotalPaise = calculatedLines.sumOf { it.igstPaise } + calculatedCharges.sumOf { it.igstPaise }
        val cessTotalPaise = calculatedLines.sumOf { it.cessPaise }
        val totalTaxPaise = cgstTotalPaise + sgstTotalPaise + igstTotalPaise + cessTotalPaise

        val collectibleTaxPaise = if (input.isReverseCharge) 0L else totalTaxPaise
        val basePlusTaxPaise = taxableSubtotalPaise + collectibleTaxPaise

        val tcsPaise = if (input.tcsBps > 0) {
            divideRoundHalfUp(basePlusTaxPaise * input.tcsBps.toLong(), 10_000L)
        } else {
            0L
        }

        val tdsPaise = if (input.tdsBps > 0) {
            divideRoundHalfUp(taxableSubtotalPaise * input.tdsBps.toLong(), 10_000L)
        } else {
            0L
        }

        val preRoundGrandTotalPaise = basePlusTaxPaise + tcsPaise
        val (grandTotalPaise, roundOffPaise) = applyRounding(
            amountPaise = preRoundGrandTotalPaise,
            mode = input.roundingMode,
            enabled = input.roundingEnabled
        )

        val totalCostPaise = calculatedLines.sumOf { it.lineCostPaise }
        val grossProfitPaise = taxableSubtotalPaise - totalCostPaise

        val classification = classifySupply(
            isComposition = isComposition,
            supplyType = supplyType,
            partyGstin = input.partyGstin,
            grandTotalPaise = grandTotalPaise,
            lines = input.lines
        )

        val amountInWords = IndianNumberToWords.toCurrencyWords(Money(grandTotalPaise))

        return GstCalculationResult(
            supplyType = supplyType,
            classification = classification,
            isCompositionScheme = isComposition,
            isReverseCharge = input.isReverseCharge,
            lines = calculatedLines,
            charges = calculatedCharges,
            hsnSummary = hsnSummary,
            grossSubtotalPaise = grossSubtotalPaise,
            lineDiscountTotalPaise = lineDiscountTotalPaise,
            invoiceDiscountPaise = totalInvoiceDiscountPaise,
            additionalChargesSubtotalPaise = additionalChargesSubtotalPaise,
            taxableSubtotalPaise = taxableSubtotalPaise,
            cgstTotalPaise = cgstTotalPaise,
            sgstTotalPaise = sgstTotalPaise,
            igstTotalPaise = igstTotalPaise,
            cessTotalPaise = cessTotalPaise,
            totalTaxPaise = totalTaxPaise,
            tcsPaise = tcsPaise,
            tdsPaise = tdsPaise,
            preRoundGrandTotalPaise = preRoundGrandTotalPaise,
            roundOffPaise = roundOffPaise,
            grandTotalPaise = grandTotalPaise,
            totalCostPaise = totalCostPaise,
            grossProfitPaise = grossProfitPaise,
            amountInWords = amountInWords
        )
    }

    fun classifySupply(
        isComposition: Boolean,
        supplyType: GstSupplyType,
        partyGstin: String,
        grandTotalPaise: Long,
        lines: List<GstLineInput>
    ): GstInvoiceClassification {
        if (isComposition) return GstInvoiceClassification.COMPOSITION
        if (supplyType == GstSupplyType.EXPORT_WITH_TAX || supplyType == GstSupplyType.EXPORT_WITHOUT_TAX) {
            return GstInvoiceClassification.EXPORT
        }
        if (supplyType == GstSupplyType.SEZ_WITH_TAX || supplyType == GstSupplyType.SEZ_WITHOUT_TAX) {
            return GstInvoiceClassification.SEZ
        }
        if (lines.isNotEmpty() && lines.all { it.taxCategory != GstTaxCategory.TAXABLE }) {
            return GstInvoiceClassification.NIL_EXEMPT
        }
        if (partyGstin.trim().length >= 15) {
            return GstInvoiceClassification.B2B
        }
        return if (supplyType == GstSupplyType.INTER_STATE && grandTotalPaise > B2CL_THRESHOLD_PAISE) {
            GstInvoiceClassification.B2CL
        } else {
            GstInvoiceClassification.B2CS
        }
    }

    /**
     * Applies rupee rounding (`100 paise = ₹1.00`) and returns `(roundedGrandTotalPaise, roundOffDeltaPaise)`.
     */
    fun applyRounding(
        amountPaise: Long,
        mode: RoundingModeOption,
        enabled: Boolean = true
    ): Pair<Long, Long> {
        if (!enabled) return amountPaise to 0L
        val sign = if (amountPaise < 0L) -1L else 1L
        val absPaise = abs(amountPaise)
        val rupeesPart = absPaise / 100L
        val remainder = absPaise % 100L
        if (remainder == 0L) return amountPaise to 0L

        val roundedRupees = when (mode) {
            RoundingModeOption.NEAREST -> if (remainder >= 50L) rupeesPart + 1L else rupeesPart
            RoundingModeOption.UP -> rupeesPart + 1L
            RoundingModeOption.DOWN -> rupeesPart
        }
        val roundedPaise = sign * roundedRupees * 100L
        return roundedPaise to (roundedPaise - amountPaise)
    }

    private fun divideRoundHalfUp(numerator: Long, denominator: Long): Long {
        if (denominator == 0L) return 0L
        return (numerator + (denominator / 2L)) / denominator
    }
}
