package com.example.feature.sales

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.core.database.ChequeEntity
import com.example.core.database.CompanyEntity
import com.example.core.database.ItemBatchEntity
import com.example.core.database.ItemEntity
import com.example.core.database.ItemSerialEntity
import com.example.core.database.NumberSeriesEntity
import com.example.core.database.PartyAddressEntity
import com.example.core.database.PartyCustomFieldEntity
import com.example.core.database.PartyEntity
import com.example.core.database.PaymentAllocationEntity
import com.example.core.database.PaymentEntity
import com.example.core.database.RecurringTemplateEntity
import com.example.core.database.RoundingModeOption
import com.example.core.database.TaxRateEntity
import com.example.core.database.TxnChargeEntity
import com.example.core.database.TxnEntity
import com.example.core.database.TxnLineEntity
import com.example.core.database.TxnLinkEntity
import com.example.core.database.TxnTaxLineEntity
import com.example.core.designsystem.AmountText
import com.example.core.designsystem.DocumentCard
import com.example.core.designsystem.GlassCard
import com.example.core.designsystem.GlassSheet
import com.example.core.designsystem.GlassSubScreen
import com.example.core.designsystem.InvoiceRow
import com.example.core.designsystem.SectionHeader
import com.example.core.designsystem.SegmentedTabs
import com.example.core.designsystem.StatusChip
import com.example.core.designsystem.VanijyaAddButton
import com.example.core.designsystem.VanijyaButton
import com.example.core.designsystem.VanijyaEmptyState
import com.example.core.designsystem.VanijyaExtendedFab
import com.example.core.designsystem.vanijyaFilterChipColors
import com.example.core.designsystem.vanijyaTextFieldColors
import com.example.core.model.CreditLimitCheckResult
import com.example.core.model.GstAdditionalChargeInput
import com.example.core.model.GstCalculationEngine
import com.example.core.model.GstDocumentInput
import com.example.core.model.GstLineInput
import com.example.core.model.GstSupplyType
import com.example.core.model.GstTaxCategory
import com.example.core.model.IndianNumberToWords
import com.example.core.model.Money
import com.example.core.model.PartyLedgerCalculator
import com.example.core.model.SalesDocTypes
import com.example.core.model.Step3APdfAndCsvExporter
import com.example.core.model.Validators
import com.example.ui.theme.AmberSale
import com.example.ui.theme.CoralError
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LocalVanijyaGlass
import com.example.ui.theme.PurchaseBlue
import com.example.ui.theme.ToPayRed
import com.example.ui.theme.VioletPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class BillingLaunchAction {
    NONE,
    NEW_SALE,
    NEW_PURCHASE,
    NEW_QUOTATION,
    NEW_SALE_ORDER,
    NEW_PURCHASE_ORDER,
    NEW_CHALLAN,
    NEW_RETURN,
    NEW_PAYMENT_IN,
    OPEN_CHEQUES,
    OPEN_RECURRING
}

/**
 * Resolves the exact allowed status chips for a document based on its document type:
 * - Sales & Purchases: Paid / Partial / Unpaid / Overdue / Cancelled (or Draft)
 * - Quotations: Open / Accepted / Converted / Expired / Rejected (never Unpaid, Paid, or Balance)
 * - Orders: Pending / Partially fulfilled / Completed (no payment status)
 * - Challans: Pending / Invoiced (no payment status)
 * - Returns: Adjusted / Refunded / Pending
 */
internal fun resolveDocumentDisplayStatuses(
    doc: TxnEntity,
    links: List<TxnLinkEntity>,
    nowMs: Long = System.currentTimeMillis()
): List<String> {
    val outgoingLinks = links.filter { it.sourceTxnId == doc.id }
    val rawStatus = doc.status.trim()
    val isDraft = rawStatus.equals("Draft", ignoreCase = true)
    val isCancelled = rawStatus.equals("Cancelled", ignoreCase = true)

    return when (doc.docType) {
        SalesDocTypes.ESTIMATE, SalesDocTypes.PROFORMA -> {
            when {
                isDraft -> listOf("Draft")
                rawStatus.equals("Rejected", ignoreCase = true) || isCancelled -> listOf("Rejected")
                outgoingLinks.isNotEmpty() || rawStatus.equals("Converted", ignoreCase = true) -> {
                    val primary = if (rawStatus.equals("Accepted", ignoreCase = true)) "Accepted" else "Open"
                    listOf(primary, "Converted")
                }
                rawStatus.equals("Accepted", ignoreCase = true) -> listOf("Accepted")
                rawStatus.equals("Expired", ignoreCase = true) || (doc.dueDate in 1 until nowMs) -> listOf("Expired")
                else -> listOf("Open")
            }
        }
        SalesDocTypes.SALE_ORDER, SalesDocTypes.PURCHASE_ORDER -> {
            when {
                isDraft -> listOf("Draft")
                outgoingLinks.isNotEmpty() ||
                    rawStatus.equals("Completed", ignoreCase = true) ||
                    rawStatus.equals("Converted", ignoreCase = true) -> {
                    if (outgoingLinks.isNotEmpty()) listOf("Completed", "Converted") else listOf("Completed")
                }
                rawStatus.contains("Partial", ignoreCase = true) -> listOf("Partially fulfilled")
                else -> listOf("Pending")
            }
        }
        SalesDocTypes.DELIVERY_CHALLAN -> {
            when {
                isDraft -> listOf("Draft")
                outgoingLinks.isNotEmpty() ||
                    rawStatus.equals("Invoiced", ignoreCase = true) ||
                    rawStatus.equals("Converted", ignoreCase = true) -> listOf("Invoiced")
                else -> listOf("Pending")
            }
        }
        SalesDocTypes.CREDIT_NOTE, SalesDocTypes.DEBIT_NOTE -> {
            when {
                isDraft -> listOf("Draft")
                rawStatus.equals("Refunded", ignoreCase = true) -> listOf("Refunded")
                doc.balanceAmountPaise <= 0L ||
                    rawStatus.equals("Adjusted", ignoreCase = true) ||
                    rawStatus.equals("Paid", ignoreCase = true) -> listOf("Adjusted")
                else -> listOf("Pending")
            }
        }
        else -> {
            // Sales & Purchases
            when {
                isCancelled -> listOf("Cancelled")
                isDraft -> listOf("Draft")
                (doc.balanceAmountPaise <= 0L && doc.grandTotalPaise > 0L) ||
                    rawStatus.equals("Paid", ignoreCase = true) -> listOf("Paid")
                (doc.paidAmountPaise > 0L && doc.balanceAmountPaise > 0L) ||
                    rawStatus.equals("Partial", ignoreCase = true) -> {
                    if (doc.dueDate in 1 until nowMs) listOf("Partial", "Overdue") else listOf("Partial")
                }
                rawStatus.equals("Overdue", ignoreCase = true) ||
                    (doc.dueDate in 1 until nowMs && doc.balanceAmountPaise > 0L) -> listOf("Overdue")
                else -> listOf("Unpaid")
            }
        }
    }
}

internal fun resolvePaymentDisplayStatuses(
    pay: PaymentEntity,
    allocs: List<PaymentAllocationEntity>
): List<String> {
    val raw = pay.status.trim()
    return when {
        raw.equals("BOUNCED", ignoreCase = true) -> listOf("Bounced")
        raw.equals("Refunded", ignoreCase = true) -> listOf("Refunded")
        allocs.isNotEmpty() || pay.unallocatedAdvancePaise == 0L || raw.equals("Adjusted", ignoreCase = true) -> listOf("Adjusted")
        else -> listOf("Pending")
    }
}

/**
 * Resolves the small muted subtitle line under the party name:
 * - Sales & Purchases: "B2B  |  Balance ₹..."
 * - Quotations: "B2B  |  Valid till <date>" (never shows Unpaid, Paid, or Balance)
 * - Orders & Challans: "B2B  |  <delivery/state info>" (no payment status or balance)
 * - Returns: "B2B  |  Sale Return / Purchase Return"
 */
internal fun resolveDocumentSubtitleLine(
    doc: TxnEntity,
    dateFmt: SimpleDateFormat,
    decimals: Int,
    hideAmounts: Boolean
): String {
    val classTag = if (doc.partyGstin.isNotBlank() || doc.classification.equals("B2B", ignoreCase = true)) "B2B" else "B2C"
    return when (doc.docType) {
        SalesDocTypes.ESTIMATE, SalesDocTypes.PROFORMA -> {
            val validDate = if (doc.dueDate > 0L) Date(doc.dueDate) else Date(doc.txnDate + 15L * 86_400_000L)
            "$classTag  |  Valid till ${dateFmt.format(validDate)}"
        }
        SalesDocTypes.SALE_ORDER, SalesDocTypes.PURCHASE_ORDER -> {
            val duePart = if (doc.dueDate > 0L) {
                "Due ${dateFmt.format(Date(doc.dueDate))}"
            } else {
                doc.placeOfSupplyStateName.ifBlank { "Order" }
            }
            "$classTag  |  $duePart"
        }
        SalesDocTypes.DELIVERY_CHALLAN -> {
            "$classTag  |  ${doc.placeOfSupplyStateName.ifBlank { "Dispatch" }}"
        }
        SalesDocTypes.CREDIT_NOTE, SalesDocTypes.DEBIT_NOTE -> {
            val retType = if (doc.docType == SalesDocTypes.CREDIT_NOTE) "Credit Note" else "Debit Note"
            "$classTag  |  $retType"
        }
        else -> {
            // Sales & Purchases show the balance line
            val balFormatted = Money(doc.balanceAmountPaise.coerceAtLeast(0L)).formatIndian(
                decimals = decimals,
                masked = hideAmounts
            )
            "$classTag  |  Balance $balFormatted"
        }
    }
}

/**
 * Resolves the link chip text and target/source txnId to display inside DocumentCard.
 */
internal fun resolveDocumentLinkBadge(
    doc: TxnEntity,
    links: List<TxnLinkEntity>
): Pair<String?, String?> {
    val outgoing = links.firstOrNull { it.sourceTxnId == doc.id }
    if (outgoing != null) {
        return "Converted to ${outgoing.targetDocNumber}" to outgoing.targetTxnId
    }
    val incoming = links.firstOrNull { it.targetTxnId == doc.id }
    if (incoming != null) {
        return "Linked: ${incoming.sourceDocNumber}" to incoming.sourceTxnId
    }
    return null to null
}

internal fun statusFiltersForSegment(segmentName: String): List<String> = when (segmentName) {
    "Sales", "Purchases" -> listOf("Paid", "Partial", "Unpaid", "Overdue", "Cancelled", "Draft")
    "Quotations" -> listOf("Open", "Accepted", "Converted", "Expired", "Rejected")
    "Orders" -> listOf("Pending", "Partially fulfilled", "Completed")
    "Challans" -> listOf("Pending", "Invoiced")
    "Returns", "Payments" -> listOf("Adjusted", "Refunded", "Pending")
    else -> listOf("Paid", "Partial", "Unpaid", "Overdue", "Cancelled")
}

internal fun dateBucketLabel(timestampMs: Long, nowMs: Long): String {
    val diffMs = (nowMs - timestampMs).coerceAtLeast(0L)
    val oneDayMs = 86_400_000L
    return when {
        diffMs < oneDayMs -> "Today"
        diffMs < 2L * oneDayMs -> "Yesterday"
        diffMs < 7L * oneDayMs -> "This Week"
        else -> "Earlier"
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SalesFoundationScreen(
    invoicePrefix: String,
    hideAmounts: Boolean = false,
    decimals: Int = 2,
    initialSegmentIndex: Int = 0,
    launchAction: BillingLaunchAction = BillingLaunchAction.NONE,
    activeCompany: CompanyEntity? = null,
    roundingMode: RoundingModeOption = RoundingModeOption.NEAREST,
    taxRates: List<TaxRateEntity> = emptyList(),
    numberSeries: List<NumberSeriesEntity> = emptyList(),
    parties: List<PartyEntity> = emptyList(),
    items: List<ItemEntity> = emptyList(),
    itemBatches: List<ItemBatchEntity> = emptyList(),
    itemSerials: List<ItemSerialEntity> = emptyList(),
    transactions: List<TxnEntity> = emptyList(),
    txnLines: List<TxnLineEntity> = emptyList(),
    txnCharges: List<TxnChargeEntity> = emptyList(),
    txnTaxLines: List<TxnTaxLineEntity> = emptyList(),
    txnLinks: List<TxnLinkEntity> = emptyList(),
    payments: List<PaymentEntity> = emptyList(),
    paymentAllocations: List<PaymentAllocationEntity> = emptyList(),
    cheques: List<ChequeEntity> = emptyList(),
    recurringTemplates: List<RecurringTemplateEntity> = emptyList(),
    onBuilderOpenChange: (Boolean) -> Unit = {},
    onSaveOrUpdateDocument: (
        header: TxnEntity,
        lines: List<TxnLineEntity>,
        charges: List<TxnChargeEntity>,
        taxLines: List<TxnTaxLineEntity>,
        sourceTxnIdForConversion: String?,
        conversionRelationship: String?,
        immediatePaymentReceivedPaise: Long,
        immediatePaymentModesJson: String
    ) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onCancelDocument: (txnId: String, reason: String) -> Unit = { _, _ -> },
    onConvertDocument: (sourceTxnId: String, targetDocType: String) -> Unit = { _, _ -> },
    onRecordPayment: (PaymentEntity, List<PaymentAllocationEntity>, ChequeEntity?) -> Unit = { _, _, _ -> },
    onUpdateChequeStatus: (chequeId: String, newStatus: String, bounceChargesPaise: Long, bounceReason: String) -> Unit = { _, _, _, _ -> },
    onSaveRecurringTemplate: (RecurringTemplateEntity) -> Unit = {},
    onRunRecurringNow: (templateId: String) -> Unit = {},
    onQuickAddParty: (PartyEntity, List<PartyAddressEntity>, List<PartyCustomFieldEntity>) -> Unit = { _, _, _ -> },
    onConsumeLaunchAction: () -> Unit = {},
    reselectTrigger: Int = 0
) {
    val context = LocalContext.current
    val glass = LocalVanijyaGlass.current
    val listState = rememberLazyListState()
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    // 7 Billing Segments: Sales, Purchases, Quotations, Orders, Challans, Returns, Payments
    val segments = remember {
        listOf("Sales", "Purchases", "Quotations", "Orders", "Challans", "Returns", "Payments")
    }
    var selectedSegment by rememberSaveable {
        mutableIntStateOf(initialSegmentIndex.coerceIn(0, segments.lastIndex))
    }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedStatusFilter by rememberSaveable { mutableStateOf("All") }
    var selectedPartyFilterId by rememberSaveable { mutableStateOf("ALL") }
    var selectedDateFilter by rememberSaveable { mutableStateOf("All") }
    var activeBanner by rememberSaveable { mutableStateOf<String?>(null) }

    // Builder & Sub-Screen States
    var showDocBuilderSheet by rememberSaveable { mutableStateOf(false) }
    var editingTxnId by rememberSaveable { mutableStateOf<String?>(null) }
    var builderInitialDocType by rememberSaveable { mutableStateOf(SalesDocTypes.TAX_INVOICE) }
    var showPaymentBuilderSheet by rememberSaveable { mutableStateOf(false) }
    var showChequeRegisterSheet by rememberSaveable { mutableStateOf(false) }
    var showRecurringSheet by rememberSaveable { mutableStateOf(false) }
    var selectedDetailTxnId by rememberSaveable { mutableStateOf<String?>(null) }
    var showInvoiceAuditSubScreen by rememberSaveable { mutableStateOf(false) }
    var cancellingTxnId by rememberSaveable { mutableStateOf<String?>(null) }
    var deletingTxnId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(showDocBuilderSheet) {
        onBuilderOpenChange(showDocBuilderSheet)
    }

    DisposableEffect(Unit) {
        onDispose {
            onBuilderOpenChange(false)
        }
    }

    LaunchedEffect(reselectTrigger) {
        if (reselectTrigger > 0) {
            showInvoiceAuditSubScreen = false
            selectedDetailTxnId = null
            showDocBuilderSheet = false
            showPaymentBuilderSheet = false
            showChequeRegisterSheet = false
            showRecurringSheet = false
            listState.scrollToItem(0)
        }
    }

    // Handle LaunchAction from Dashboard Quick Actions / More Sheet
    LaunchedEffect(launchAction) {
        when (launchAction) {
            BillingLaunchAction.NEW_SALE -> {
                selectedSegment = segments.indexOf("Sales")
                editingTxnId = null
                builderInitialDocType = SalesDocTypes.TAX_INVOICE
                showDocBuilderSheet = true
            }
            BillingLaunchAction.NEW_PURCHASE -> {
                selectedSegment = segments.indexOf("Purchases")
                editingTxnId = null
                builderInitialDocType = SalesDocTypes.PURCHASE_BILL
                showDocBuilderSheet = true
            }
            BillingLaunchAction.NEW_QUOTATION -> {
                selectedSegment = segments.indexOf("Quotations")
                editingTxnId = null
                builderInitialDocType = SalesDocTypes.ESTIMATE
                showDocBuilderSheet = true
            }
            BillingLaunchAction.NEW_SALE_ORDER -> {
                selectedSegment = segments.indexOf("Orders")
                editingTxnId = null
                builderInitialDocType = SalesDocTypes.SALE_ORDER
                showDocBuilderSheet = true
            }
            BillingLaunchAction.NEW_PURCHASE_ORDER -> {
                selectedSegment = segments.indexOf("Orders")
                editingTxnId = null
                builderInitialDocType = SalesDocTypes.PURCHASE_ORDER
                showDocBuilderSheet = true
            }
            BillingLaunchAction.NEW_CHALLAN -> {
                selectedSegment = segments.indexOf("Challans")
                editingTxnId = null
                builderInitialDocType = SalesDocTypes.DELIVERY_CHALLAN
                showDocBuilderSheet = true
            }
            BillingLaunchAction.NEW_RETURN -> {
                selectedSegment = segments.indexOf("Returns")
                editingTxnId = null
                builderInitialDocType = SalesDocTypes.CREDIT_NOTE
                showDocBuilderSheet = true
            }
            BillingLaunchAction.NEW_PAYMENT_IN -> {
                selectedSegment = segments.indexOf("Payments")
                showPaymentBuilderSheet = true
            }
            BillingLaunchAction.OPEN_CHEQUES -> {
                selectedSegment = segments.indexOf("Payments")
                showChequeRegisterSheet = true
            }
            BillingLaunchAction.OPEN_RECURRING -> {
                selectedSegment = segments.indexOf("Sales")
                showRecurringSheet = true
            }
            BillingLaunchAction.NONE -> Unit
        }
        if (launchAction != BillingLaunchAction.NONE) {
            onConsumeLaunchAction()
        }
    }

    // Fallback in-memory preview list when rendered in standalone preview/test without Room seed
    val localFallbackDocs = remember(invoicePrefix) {
        mutableStateListOf(
            TxnEntity(
                id = "fallback_1",
                companyId = "local",
                docType = SalesDocTypes.TAX_INVOICE,
                docNumber = "${invoicePrefix}0001",
                txnDate = System.currentTimeMillis(),
                dueDate = System.currentTimeMillis() + 15 * 86_400_000L,
                partyName = "Mehta Kirana & Traders",
                partyGstin = "27AAPFM3456D1Z9",
                placeOfSupplyStateCode = "27",
                placeOfSupplyStateName = "Maharashtra",
                classification = "B2B",
                taxableSubtotalPaise = 28_500_00L,
                cgstTotalPaise = 712_50L,
                sgstTotalPaise = 712_50L,
                totalTaxPaise = 1_425_00L,
                grandTotalPaise = 29_925_00L,
                paidAmountPaise = 29_925_00L,
                balanceAmountPaise = 0L,
                totalCostPaise = 24_000_00L,
                grossProfitPaise = 4_500_00L,
                status = "Paid",
                amountInWords = IndianNumberToWords.toCurrencyWords(Money(29_925_00L))
            ),
            TxnEntity(
                id = "fallback_2",
                companyId = "local",
                docType = SalesDocTypes.TAX_INVOICE,
                docNumber = "${invoicePrefix}0002",
                txnDate = System.currentTimeMillis() - 86_400_000L,
                dueDate = System.currentTimeMillis() + 10 * 86_400_000L,
                partyName = "Shree Balaji Distributors",
                partyGstin = "27AABCS9876G1Z3",
                placeOfSupplyStateCode = "27",
                placeOfSupplyStateName = "Maharashtra",
                classification = "B2B",
                taxableSubtotalPaise = 38_214_28L,
                cgstTotalPaise = 2_292_86L,
                sgstTotalPaise = 2_292_86L,
                totalTaxPaise = 4_585_72L,
                grandTotalPaise = 42_800_00L,
                paidAmountPaise = 20_000_00L,
                balanceAmountPaise = 22_800_00L,
                totalCostPaise = 34_200_00L,
                grossProfitPaise = 4_014_28L,
                status = "Partial",
                amountInWords = IndianNumberToWords.toCurrencyWords(Money(42_800_00L))
            ),
            TxnEntity(
                id = "fallback_purchase_1",
                companyId = "local",
                docType = SalesDocTypes.PURCHASE_BILL,
                docNumber = "PUR/26-27/0001",
                txnDate = System.currentTimeMillis() - 86_400_000L,
                dueDate = System.currentTimeMillis() + 14 * 86_400_000L,
                partyName = "Annapurna Agro Mills Pvt Ltd",
                partyGstin = "27AABCA1122K1Z5",
                placeOfSupplyStateCode = "27",
                placeOfSupplyStateName = "Maharashtra",
                classification = "B2B",
                taxableSubtotalPaise = 50_000_00L,
                cgstTotalPaise = 1_250_00L,
                sgstTotalPaise = 1_250_00L,
                totalTaxPaise = 2_500_00L,
                grandTotalPaise = 52_500_00L,
                paidAmountPaise = 12_500_00L,
                balanceAmountPaise = 40_000_00L,
                status = "Partial",
                amountInWords = IndianNumberToWords.toCurrencyWords(Money(52_500_00L))
            ),
            TxnEntity(
                id = "fallback_3",
                companyId = "local",
                docType = SalesDocTypes.ESTIMATE,
                docNumber = "EST/26-27/0001",
                txnDate = System.currentTimeMillis() - 2 * 86_400_000L,
                dueDate = System.currentTimeMillis() + 7 * 86_400_000L,
                partyName = "Apex Packaging Industries",
                partyGstin = "27AADCK9012C1Z9",
                placeOfSupplyStateCode = "27",
                placeOfSupplyStateName = "Maharashtra",
                classification = "B2B",
                taxableSubtotalPaise = 32_000_00L,
                cgstTotalPaise = 1_920_00L,
                sgstTotalPaise = 1_920_00L,
                totalTaxPaise = 3_840_00L,
                grandTotalPaise = 35_840_00L,
                paidAmountPaise = 0L,
                balanceAmountPaise = 35_840_00L,
                status = "Open",
                amountInWords = IndianNumberToWords.toCurrencyWords(Money(35_840_00L))
            ),
            TxnEntity(
                id = "fallback_order_1",
                companyId = "local",
                docType = SalesDocTypes.SALE_ORDER,
                docNumber = "SO/26-27/0001",
                txnDate = System.currentTimeMillis() - 2 * 86_400_000L,
                dueDate = System.currentTimeMillis() + 5 * 86_400_000L,
                partyName = "Kaveri Supermart",
                partyGstin = "27AADCK9012C1Z9",
                placeOfSupplyStateCode = "27",
                placeOfSupplyStateName = "Maharashtra",
                classification = "B2B",
                taxableSubtotalPaise = 18_000_00L,
                cgstTotalPaise = 1_620_00L,
                sgstTotalPaise = 1_620_00L,
                totalTaxPaise = 3_240_00L,
                grandTotalPaise = 21_240_00L,
                paidAmountPaise = 0L,
                balanceAmountPaise = 21_240_00L,
                status = "Pending",
                amountInWords = IndianNumberToWords.toCurrencyWords(Money(21_240_00L))
            ),
            TxnEntity(
                id = "fallback_challan_1",
                companyId = "local",
                docType = SalesDocTypes.DELIVERY_CHALLAN,
                docNumber = "DC/26-27/0001",
                txnDate = System.currentTimeMillis() - 3 * 86_400_000L,
                dueDate = System.currentTimeMillis() + 3 * 86_400_000L,
                partyName = "Patel Hardware & Electricals",
                partyGstin = "27AAJCP5566M1Z1",
                placeOfSupplyStateCode = "27",
                placeOfSupplyStateName = "Maharashtra",
                classification = "B2B",
                taxableSubtotalPaise = 12_500_00L,
                cgstTotalPaise = 1_125_00L,
                sgstTotalPaise = 1_125_00L,
                totalTaxPaise = 2_250_00L,
                grandTotalPaise = 14_750_00L,
                paidAmountPaise = 0L,
                balanceAmountPaise = 0L,
                status = "Pending",
                amountInWords = IndianNumberToWords.toCurrencyWords(Money(14_750_00L))
            ),
            TxnEntity(
                id = "fallback_return_1",
                companyId = "local",
                docType = SalesDocTypes.CREDIT_NOTE,
                docNumber = "CN/26-27/0001",
                txnDate = System.currentTimeMillis() - 4 * 86_400_000L,
                dueDate = 0L,
                partyName = "Mehta Kirana & Traders",
                partyGstin = "27AAPFM3456D1Z9",
                placeOfSupplyStateCode = "27",
                placeOfSupplyStateName = "Maharashtra",
                classification = "B2B",
                taxableSubtotalPaise = 2_850_00L,
                cgstTotalPaise = 71_25L,
                sgstTotalPaise = 71_25L,
                totalTaxPaise = 142_50L,
                grandTotalPaise = 2_992_50L,
                paidAmountPaise = 2_992_50L,
                balanceAmountPaise = 0L,
                status = "Adjusted",
                amountInWords = IndianNumberToWords.toCurrencyWords(Money(2_992_50L))
            )
        )
    }

    val localFallbackPayments = remember {
        mutableStateListOf(
            PaymentEntity(
                id = "fallback_pay_1",
                companyId = "local",
                paymentNumber = "RCPT/26-27/0001",
                paymentType = "PAYMENT_IN",
                paymentDate = System.currentTimeMillis(),
                partyId = "local_party_1",
                partyName = "Mehta Kirana & Traders",
                totalAmountPaise = 29_925_00L,
                discountPaise = 0L,
                unallocatedAdvancePaise = 0L,
                primaryMode = "UPI",
                modesSplitJson = "UPI:2992500",
                referenceNo = "UPI-4321987654",
                status = "Adjusted",
                notes = "Bill-to-bill settlement"
            )
        )
    }

    val effectiveTxns = if (transactions.isNotEmpty()) transactions else localFallbackDocs
    val effectivePayments = if (payments.isNotEmpty()) payments else localFallbackPayments
    val currentSegmentName = segments[selectedSegment.coerceIn(0, segments.lastIndex)]
    val nowMs = System.currentTimeMillis()

    // Reset status filter to All if the user switches segments and the old status filter doesn't apply
    val currentSegmentStatusFilters = remember(currentSegmentName) {
        statusFiltersForSegment(currentSegmentName)
    }
    LaunchedEffect(currentSegmentName) {
        if (selectedStatusFilter != "All" && selectedStatusFilter !in currentSegmentStatusFilters) {
            selectedStatusFilter = "All"
        }
    }

    val dateFilterCutoffMs = when (selectedDateFilter) {
        "Today" -> nowMs - 86_400_000L
        "This week", "This Week" -> nowMs - 7L * 86_400_000L
        "This month", "This Month" -> nowMs - 31L * 86_400_000L
        else -> 0L // All / This FY
    }

    val filteredTxns = remember(
        effectiveTxns,
        txnLinks,
        currentSegmentName,
        selectedStatusFilter,
        selectedPartyFilterId,
        selectedDateFilter,
        searchQuery
    ) {
        effectiveTxns.filter { txn ->
            val matchesSegment = when (currentSegmentName) {
                "Sales" -> txn.docType in setOf(SalesDocTypes.TAX_INVOICE, SalesDocTypes.BILL_OF_SUPPLY, SalesDocTypes.CASH_SALE)
                "Purchases" -> txn.docType == SalesDocTypes.PURCHASE_BILL
                else -> SalesDocTypes.segmentForDocType(txn.docType) == currentSegmentName
            }
            val displayStatuses = resolveDocumentDisplayStatuses(txn, txnLinks, nowMs)
            val matchesStatus = selectedStatusFilter == "All" ||
                displayStatuses.any { it.equals(selectedStatusFilter, ignoreCase = true) } ||
                txn.status.equals(selectedStatusFilter, ignoreCase = true)

            !txn.isDeleted &&
                matchesSegment &&
                txn.txnDate >= dateFilterCutoffMs &&
                (selectedPartyFilterId == "ALL" || txn.partyId == selectedPartyFilterId) &&
                matchesStatus &&
                (searchQuery.isBlank() ||
                    txn.docNumber.contains(searchQuery, ignoreCase = true) ||
                    txn.partyName.contains(searchQuery, ignoreCase = true) ||
                    txn.partyGstin.contains(searchQuery, ignoreCase = true))
        }
    }

    val filteredPayments = remember(
        effectivePayments,
        paymentAllocations,
        selectedStatusFilter,
        selectedPartyFilterId,
        selectedDateFilter,
        searchQuery
    ) {
        effectivePayments.filter { pay ->
            val allocs = paymentAllocations.filter { it.paymentId == pay.id }
            val payStatuses = resolvePaymentDisplayStatuses(pay, allocs)
            val matchesStatus = selectedStatusFilter == "All" ||
                payStatuses.any { it.equals(selectedStatusFilter, ignoreCase = true) } ||
                pay.status.equals(selectedStatusFilter, ignoreCase = true)

            !pay.isDeleted &&
                pay.paymentDate >= dateFilterCutoffMs &&
                (selectedPartyFilterId == "ALL" || pay.partyId == selectedPartyFilterId) &&
                matchesStatus &&
                (searchQuery.isBlank() ||
                    pay.paymentNumber.contains(searchQuery, ignoreCase = true) ||
                    pay.partyName.contains(searchQuery, ignoreCase = true) ||
                    pay.referenceNo.contains(searchQuery, ignoreCase = true))
        }
    }

    val segmentTotal = if (currentSegmentName == "Payments") {
        Money(filteredPayments.filter { it.status != "BOUNCED" }.sumOf { it.totalAmountPaise })
    } else {
        Money(filteredTxns.filter { it.status != "Cancelled" }.sumOf { it.grandTotalPaise })
    }
    val paidTotal = if (currentSegmentName == "Payments") {
        segmentTotal
    } else {
        Money(filteredTxns.filter { it.status != "Cancelled" }.sumOf { it.paidAmountPaise })
    }
    val unpaidTotal = if (currentSegmentName == "Payments") {
        Money(filteredPayments.sumOf { it.unallocatedAdvancePaise })
    } else {
        Money(filteredTxns.filter { it.status != "Cancelled" }.sumOf { it.balanceAmountPaise })
    }

    val newDocLabel = when (currentSegmentName) {
        "Sales" -> "Sale Invoice"
        "Purchases" -> "Purchase Bill"
        "Quotations" -> "Quotation"
        "Orders" -> "Order"
        "Challans" -> "Challan"
        "Returns" -> "Return"
        else -> "Payment"
    }

    val openBuilderForCurrentSegment = {
        if (currentSegmentName == "Payments") {
            showPaymentBuilderSheet = true
        } else {
            editingTxnId = null
            builderInitialDocType = when (currentSegmentName) {
                "Sales" -> if (activeCompany?.gstMode == "COMPOSITION") SalesDocTypes.BILL_OF_SUPPLY else SalesDocTypes.TAX_INVOICE
                "Purchases" -> SalesDocTypes.PURCHASE_BILL
                "Quotations" -> SalesDocTypes.ESTIMATE
                "Orders" -> SalesDocTypes.SALE_ORDER
                "Challans" -> SalesDocTypes.DELIVERY_CHALLAN
                "Returns" -> SalesDocTypes.CREDIT_NOTE
                else -> SalesDocTypes.TAX_INVOICE
            }
            showDocBuilderSheet = true
        }
    }

    // Collapse the floating "New ..." FAB to an icon when scrolling down the list
    val isFabExpanded by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 90
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("billing_hub_screen")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("sales_billing_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 196.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Header + Quick Tool Pills (Cheques, Recurring, Export CSV)
            item(key = "billing_top_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Billing & GST Vouchers",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val nextInvNo = numberSeries.firstOrNull { it.docType == "SALE_INVOICE" }?.let {
                            "${it.prefix}${it.nextNumber.toString().padStart(it.paddingDigits, '0')}"
                        } ?: "${invoicePrefix}0001"
                        Text(
                            text = "Next: $nextInvNo • ${activeCompany?.gstMode ?: "REGULAR_GST"} • Offline GST Engine",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val inHandCheques = cheques.count { !it.isDeleted && (it.status == "IN_HAND" || it.status == "DEPOSITED") }
                    val activeRecCount = recurringTemplates.count { !it.isDeleted && it.isActive }
                    FilterChip(
                        selected = false,
                        onClick = { showPaymentBuilderSheet = true },
                        label = { Text("Payment-In") },
                        leadingIcon = {
                            Icon(Icons.Default.Add, contentDescription = "Payment-In", modifier = Modifier.size(16.dp))
                        },
                        colors = vanijyaFilterChipColors(),
                        modifier = Modifier.testTag("billing_chip_payment_in")
                    )
                    FilterChip(
                        selected = false,
                        onClick = { showChequeRegisterSheet = true },
                        label = { Text("Cheques ($inHandCheques)") },
                        leadingIcon = {
                            Icon(Icons.Default.AccountBalance, contentDescription = "Cheques", modifier = Modifier.size(16.dp))
                        },
                        colors = vanijyaFilterChipColors(),
                        modifier = Modifier.testTag("billing_chip_cheques")
                    )
                    FilterChip(
                        selected = false,
                        onClick = { showRecurringSheet = true },
                        label = { Text("Recurring ($activeRecCount)") },
                        leadingIcon = {
                            Icon(Icons.Default.Autorenew, contentDescription = "Recurring", modifier = Modifier.size(16.dp))
                        },
                        colors = vanijyaFilterChipColors(),
                        modifier = Modifier.testTag("billing_chip_recurring")
                    )
                    FilterChip(
                        selected = false,
                        onClick = {
                            Step3APdfAndCsvExporter.exportSalesCsvAndShare(context, effectiveTxns)
                            activeBanner = "✓ Exported Sales & GST Register CSV"
                        },
                        label = { Text("Export CSV") },
                        colors = vanijyaFilterChipColors()
                    )
                }
            }

            // 2. Segmented Tabs: Sales, Purchases, Quotations, Orders, Challans, Returns, Payments
            item(key = "billing_segmented_tabs") {
                SegmentedTabs(
                    tabs = segments,
                    selectedIndex = selectedSegment,
                    onTabSelected = { selectedSegment = it },
                    testTagPrefix = "billing_segment"
                )
            }

            // 3. Sticky Search Bar + Horizontally Scrollable Filter Chips (All, Today, This week, This month + status filters)
            stickyHeader(key = "billing_sticky_search_filters") {
                Surface(
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search invoice #, party, or GSTIN") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = "Search Billing")
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            colors = vanijyaTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("billing_search_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Date & Status Quick Filter Chips
                            val datePresets = listOf("All", "Today", "This week", "This month")
                            datePresets.forEach { preset ->
                                val isSelected = if (preset == "All") {
                                    selectedDateFilter == "All" && selectedStatusFilter == "All"
                                } else {
                                    selectedDateFilter.equals(preset, ignoreCase = true)
                                }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (preset == "All") {
                                            selectedDateFilter = "All"
                                            selectedStatusFilter = "All"
                                        } else {
                                            selectedDateFilter = if (selectedDateFilter.equals(preset, ignoreCase = true)) "All" else preset
                                        }
                                    },
                                    label = { Text(preset, maxLines = 1, softWrap = false) },
                                    colors = vanijyaFilterChipColors()
                                )
                            }

                            val activeParties = parties.filter { !it.isDeleted }
                            if (activeParties.isNotEmpty()) {
                                val selectedPartyName = activeParties.firstOrNull { it.id == selectedPartyFilterId }?.name ?: "All Parties"
                                FilterChip(
                                    selected = selectedPartyFilterId != "ALL",
                                    onClick = {
                                        val ids = listOf("ALL") + activeParties.map { it.id }
                                        val nextIdx = (ids.indexOf(selectedPartyFilterId) + 1) % ids.size
                                        selectedPartyFilterId = ids[nextIdx]
                                    },
                                    label = { Text("Party: ${selectedPartyName.take(14)}", maxLines = 1, softWrap = false) },
                                    colors = vanijyaFilterChipColors()
                                )
                            }

                            currentSegmentStatusFilters.forEach { status ->
                                FilterChip(
                                    selected = selectedStatusFilter.equals(status, ignoreCase = true),
                                    onClick = {
                                        selectedStatusFilter = if (selectedStatusFilter.equals(status, ignoreCase = true)) {
                                            "All"
                                        } else {
                                            status
                                        }
                                    },
                                    label = { Text(status, maxLines = 1, softWrap = false) },
                                    colors = vanijyaFilterChipColors()
                                )
                            }
                        }
                    }
                }
            }

            // 4. Glass Totals Header ("12 documents  |  ₹1,23,456")
            item(key = "billing_totals_header_item") {
                val itemCount = if (currentSegmentName == "Payments") filteredPayments.size else filteredTxns.size
                val docCountWord = if (itemCount == 1) "1 document" else "$itemCount documents"
                val formattedTotalInline = segmentTotal.formatIndian(decimals = decimals, masked = hideAmounts)
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("billing_totals_header"),
                    cornerRadius = 24.dp,
                    accentTint = EmeraldPrimary,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "$docCountWord  |  $formattedTotalInline",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFeatureSettings = "tnum"
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            AmountText(
                                amount = segmentTotal,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                masked = hideAmounts,
                                decimals = decimals,
                                animateCountUp = true
                            )
                            if (currentSegmentName == "Sales") {
                                val totalTaxPaise = filteredTxns.filter { it.status != "Cancelled" }.sumOf { it.totalTaxPaise }
                                val totalProfitPaise = filteredTxns.filter { it.status != "Cancelled" }.sumOf { it.grossProfitPaise }
                                Text(
                                    text = "GST: ${Money(totalTaxPaise).formatIndian(decimals = 0)} • Profit: ${Money(totalProfitPaise).formatIndian(decimals = 0)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldPrimary
                                )
                            }
                        }
                        if (currentSegmentName in setOf("Sales", "Purchases", "Payments")) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (currentSegmentName == "Payments") "Total Received" else "Settled / Paid",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldPrimary
                                )
                                AmountText(
                                    amount = paidTotal,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldPrimary,
                                    masked = hideAmounts,
                                    decimals = decimals
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (currentSegmentName == "Payments") "Unallocated Advance" else "Balance Due",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberSale
                                )
                                AmountText(
                                    amount = unpaidTotal,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = AmberSale,
                                    masked = hideAmounts,
                                    decimals = decimals
                                )
                            }
                        }
                    }
                }
            }

            if (!activeBanner.isNullOrBlank()) {
                item(key = "billing_active_banner") {
                    Surface(
                        color = EmeraldPrimary.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeBanner = null }
                    ) {
                        Text(
                            text = activeBanner!!,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // 5. Document or Payment List Cards with Date Section Headers ("Today", "Yesterday", "This Week", "Earlier")
            if (currentSegmentName == "Payments") {
                if (filteredPayments.isEmpty()) {
                    item(key = "empty_payments") {
                        VanijyaEmptyState(
                            icon = Icons.Default.PointOfSale,
                            title = "No Payment Receipts found",
                            description = "Record multi-mode customer receipts (Cash, UPI, Bank, Cheque) with bill-to-bill invoice settlement.",
                            actionLabel = "New Payment",
                            onActionClick = { showPaymentBuilderSheet = true }
                        )
                    }
                } else {
                    val paymentBuckets = filteredPayments.groupBy { dateBucketLabel(it.paymentDate, nowMs) }
                    var paymentGlobalIndex = 0
                    paymentBuckets.forEach { (bucketName, bucketPayments) ->
                        item(key = "pay_date_header_$bucketName") {
                            Text(
                                text = bucketName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }
                        items(bucketPayments, key = { it.id }) { pay ->
                            val currentIdx = paymentGlobalIndex++
                            val allocsForPay = paymentAllocations.filter { it.paymentId == pay.id }
                            val payStatuses = resolvePaymentDisplayStatuses(pay, allocsForPay)
                            val linkedInvoicesText = if (allocsForPay.isNotEmpty()) {
                                "Linked: ${allocsForPay.joinToString(", ") { it.txnDocNumber }}"
                            } else {
                                null
                            }
                            val firstLinkedTxnId = allocsForPay.firstOrNull()?.txnId
                            val subtitle = buildString {
                                append(pay.primaryMode)
                                if (pay.referenceNo.isNotBlank()) {
                                    append("  |  Ref: ${pay.referenceNo}")
                                }
                                if (pay.unallocatedAdvancePaise > 0L) {
                                    append("  |  Advance ${Money(pay.unallocatedAdvancePaise).formatIndian(decimals = decimals, masked = hideAmounts)}")
                                }
                            }
                            DocumentCard(
                                docNumber = pay.paymentNumber.ifBlank { "RCPT/26-27/0001" },
                                docTypeLabel = if (pay.paymentType == "PAYMENT_OUT") "Payment-Out" else "Payment-In",
                                dateText = dateFmt.format(Date(pay.paymentDate)),
                                partyName = pay.partyName,
                                subtitleLine = subtitle,
                                amount = Money(pay.totalAmountPaise),
                                statuses = payStatuses,
                                isPositiveAmount = pay.status != "BOUNCED",
                                masked = hideAmounts,
                                decimals = decimals,
                                linkedBadgeText = linkedInvoicesText,
                                onLinkedBadgeClick = if (firstLinkedTxnId != null) {
                                    { selectedDetailTxnId = firstLinkedTxnId }
                                } else null,
                                primaryActionLabel = if (pay.unallocatedAdvancePaise > 0L) "Receive Payment" else null,
                                onPrimaryActionClick = if (pay.unallocatedAdvancePaise > 0L) {
                                    { showPaymentBuilderSheet = true }
                                } else null,
                                onSharePdfClick = {
                                    activeBanner = "✓ Shared Receipt PDF for ${pay.paymentNumber}"
                                },
                                onPrintClick = {
                                    activeBanner = "✓ Sent Receipt ${pay.paymentNumber} to printer"
                                },
                                onDownloadPdfClick = {
                                    activeBanner = "✓ Downloaded Receipt PDF for ${pay.paymentNumber}"
                                },
                                onClick = {
                                    if (firstLinkedTxnId != null) {
                                        selectedDetailTxnId = firstLinkedTxnId
                                    } else {
                                        showPaymentBuilderSheet = true
                                    }
                                },
                                modifier = Modifier.testTag("invoice_card_$currentIdx")
                            )
                        }
                    }
                }
            } else {
                if (filteredTxns.isEmpty()) {
                    item(key = "empty_txns_$currentSegmentName") {
                        VanijyaEmptyState(
                            icon = Icons.Default.PointOfSale,
                            title = "No $currentSegmentName found",
                            description = "Tap 'New $newDocLabel' to create an offline GST voucher with HSN summary & automatic Indian amount-in-words.",
                            actionLabel = "New $newDocLabel",
                            onActionClick = openBuilderForCurrentSegment
                        )
                    }
                } else {
                    val txnBuckets = filteredTxns.groupBy { dateBucketLabel(it.txnDate, nowMs) }
                    var txnGlobalIndex = 0
                    txnBuckets.forEach { (bucketName, bucketDocs) ->
                        item(key = "txn_date_header_${currentSegmentName}_$bucketName") {
                            Text(
                                text = bucketName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }
                        items(bucketDocs, key = { it.id }) { doc ->
                            val currentIdx = txnGlobalIndex++
                            val docLinks = txnLinks.filter { it.sourceTxnId == doc.id || it.targetTxnId == doc.id }
                            val displayStatuses = resolveDocumentDisplayStatuses(doc, docLinks, nowMs)
                            val subtitleLine = resolveDocumentSubtitleLine(doc, dateFmt, decimals, hideAmounts)
                            val (linkBadgeText, linkedTxnId) = resolveDocumentLinkBadge(doc, docLinks)
                            val nextConversionType = SalesDocTypes.nextConversionTarget(doc.docType)
                            val isCancelled = doc.status.equals("Cancelled", ignoreCase = true)

                            // Determine the single primary action button for the card's bottom right
                            val primaryActionPair: Pair<String?, (() -> Unit)?> = when {
                                isCancelled -> null to null
                                doc.docType in setOf(SalesDocTypes.ESTIMATE, SalesDocTypes.PROFORMA) -> {
                                    "Convert to Sale" to {
                                        val target = nextConversionType ?: SalesDocTypes.TAX_INVOICE
                                        onConvertDocument(doc.id, target)
                                        activeBanner = "✓ Converted ${doc.docNumber} → ${SalesDocTypes.displayLabel(target)}"
                                    }
                                }
                                doc.docType == SalesDocTypes.SALE_ORDER || doc.docType == SalesDocTypes.DELIVERY_CHALLAN -> {
                                    "Convert to Sale" to {
                                        onConvertDocument(doc.id, SalesDocTypes.TAX_INVOICE)
                                        activeBanner = "✓ Converted ${doc.docNumber} → Tax Invoice"
                                    }
                                }
                                doc.docType == SalesDocTypes.PURCHASE_ORDER -> {
                                    "Convert to Purchase" to {
                                        onConvertDocument(doc.id, SalesDocTypes.PURCHASE_BILL)
                                        activeBanner = "✓ Converted ${doc.docNumber} → Purchase Bill"
                                    }
                                }
                                doc.docType == SalesDocTypes.PURCHASE_BILL -> {
                                    if (doc.balanceAmountPaise > 0L) {
                                        "Pay" to { showPaymentBuilderSheet = true }
                                    } else {
                                        null to null
                                    }
                                }
                                doc.docType in setOf(SalesDocTypes.TAX_INVOICE, SalesDocTypes.BILL_OF_SUPPLY, SalesDocTypes.CASH_SALE) -> {
                                    if (doc.balanceAmountPaise > 0L) {
                                        "Receive Payment" to { showPaymentBuilderSheet = true }
                                    } else if (nextConversionType != null) {
                                        "Convert to Sale" to {
                                            onConvertDocument(doc.id, nextConversionType)
                                            activeBanner = "✓ Converted ${doc.docNumber} → ${SalesDocTypes.displayLabel(nextConversionType)}"
                                        }
                                    } else {
                                        null to null
                                    }
                                }
                                else -> null to null
                            }

                            val exportOrSharePdf: () -> Unit = {
                                val linesForDoc = txnLines.filter { it.txnId == doc.id }
                                val chargesForDoc = txnCharges.filter { it.txnId == doc.id }
                                val taxesForDoc = txnTaxLines.filter { it.txnId == doc.id }
                                Step3APdfAndCsvExporter.exportAndShareSalesVoucherPdf(
                                    context = context,
                                    company = activeCompany,
                                    txn = doc,
                                    lines = linesForDoc,
                                    charges = chargesForDoc,
                                    taxLines = taxesForDoc
                                )
                                activeBanner = "✓ Generated A4 GST PDF for ${doc.docNumber} (${doc.partyName})"
                            }

                            DocumentCard(
                                docNumber = doc.docNumber,
                                docTypeLabel = SalesDocTypes.displayLabel(doc.docType),
                                dateText = dateFmt.format(Date(doc.txnDate)),
                                partyName = doc.partyName,
                                subtitleLine = subtitleLine,
                                amount = Money(doc.grandTotalPaise),
                                statuses = displayStatuses,
                                isPositiveAmount = doc.docType !in setOf(SalesDocTypes.CREDIT_NOTE, SalesDocTypes.PURCHASE_BILL),
                                masked = hideAmounts,
                                decimals = decimals,
                                linkedBadgeText = linkBadgeText,
                                onLinkedBadgeClick = if (linkedTxnId != null) {
                                    {
                                        showInvoiceAuditSubScreen = false
                                        selectedDetailTxnId = linkedTxnId
                                    }
                                } else null,
                                primaryActionLabel = primaryActionPair.first,
                                onPrimaryActionClick = primaryActionPair.second,
                                onSharePdfClick = exportOrSharePdf,
                                onEditClick = if (!isCancelled) {
                                    {
                                        editingTxnId = doc.id
                                        builderInitialDocType = doc.docType
                                        showDocBuilderSheet = true
                                    }
                                } else null,
                                onDuplicateClick = {
                                    val copyDoc = doc.copy(
                                        id = UUID.randomUUID().toString(),
                                        docNumber = "${doc.docNumber}-COPY",
                                        txnDate = System.currentTimeMillis()
                                    )
                                    localFallbackDocs.add(0, copyDoc)
                                    activeBanner = "✓ Duplicated ${doc.docNumber} as ${copyDoc.docNumber}"
                                },
                                onPrintClick = exportOrSharePdf,
                                onDownloadPdfClick = exportOrSharePdf,
                                convertMenuLabel = if (!isCancelled && nextConversionType != null) {
                                    "Convert to ${SalesDocTypes.displayLabel(nextConversionType)}"
                                } else null,
                                onConvertMenuClick = if (!isCancelled && nextConversionType != null) {
                                    {
                                        onConvertDocument(doc.id, nextConversionType)
                                        activeBanner = "✓ Converted ${doc.docNumber} → ${SalesDocTypes.displayLabel(nextConversionType)}"
                                    }
                                } else null,
                                onCancelClick = if (!isCancelled) {
                                    { cancellingTxnId = doc.id }
                                } else null,
                                onDeleteClick = {
                                    deletingTxnId = doc.id
                                },
                                onClick = {
                                    showInvoiceAuditSubScreen = false
                                    selectedDetailTxnId = doc.id
                                },
                                modifier = Modifier.testTag("invoice_card_$currentIdx")
                            )
                        }
                    }
                }
            }
        }

        // Floating "New ..." Button creating the right document type for the current segment, collapsing to icon on scroll down
        if (!showDocBuilderSheet) {
            VanijyaExtendedFab(
                text = "New $newDocLabel",
                onClick = openBuilderForCurrentSegment,
                icon = Icons.Default.Add,
                containerColor = VioletPrimary,
                contentColor = Color.White,
                expanded = isFabExpanded,
                testTag = "billing_fab_new_doc",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 104.dp)
            )
        }
    }

    // ============================================================================
    // FULL-SCREEN REDESIGNED BILLING PAGE (Sale, Purchase, Quotation, Order, Challan, Return)
    // ============================================================================
    if (showDocBuilderSheet) {
        val coroutineScope = rememberCoroutineScope()
        val builderLazyListState = rememberLazyListState()

        val editingTxn = remember(editingTxnId, effectiveTxns) {
            effectiveTxns.firstOrNull { it.id == editingTxnId }
        }
        val existingLines = remember(editingTxnId, txnLines) {
            if (editingTxnId != null) txnLines.filter { it.txnId == editingTxnId } else emptyList()
        }
        val existingCharges = remember(editingTxnId, txnCharges) {
            if (editingTxnId != null) txnCharges.filter { it.txnId == editingTxnId } else emptyList()
        }

        val activeParties = remember(parties) { parties.filter { !it.isDeleted } }
        val activeItems = remember(items) { items.filter { !it.isDeleted } }

        var selectedDocType by remember(editingTxnId, builderInitialDocType) {
            mutableStateOf(editingTxn?.docType ?: builderInitialDocType)
        }
        var isCashSaleToggle by remember(editingTxnId, selectedDocType) {
            mutableStateOf(selectedDocType == SalesDocTypes.CASH_SALE)
        }
        var docPrefix by remember(editingTxnId, selectedDocType) {
            val defaultPref = editingTxn?.docNumber?.takeWhile { !it.isDigit() }?.ifBlank { null }
                ?: if (selectedDocType == SalesDocTypes.TAX_INVOICE) invoicePrefix else SalesDocTypes.defaultPrefix(selectedDocType)
            mutableStateOf(defaultPref)
        }
        var docSequenceNumber by remember(editingTxnId, selectedDocType, effectiveTxns.size) {
            val existingDigits = editingTxn?.docNumber?.takeLastWhile { it.isDigit() }?.toIntOrNull()
            mutableIntStateOf(existingDigits ?: (effectiveTxns.size + 1))
        }
        var txnDateMillis by remember(editingTxnId) {
            mutableStateOf(editingTxn?.txnDate ?: System.currentTimeMillis())
        }
        var dueDaysOffset by remember(editingTxnId, selectedDocType) {
            val defaultDays = when (selectedDocType) {
                SalesDocTypes.SALE_ORDER, SalesDocTypes.PURCHASE_ORDER -> 7
                else -> 15
            }
            mutableIntStateOf(defaultDays)
        }

        var selectedPartyId by remember(editingTxnId) {
            mutableStateOf(editingTxn?.partyId ?: activeParties.firstOrNull()?.id ?: "")
        }
        var customPartyName by remember(editingTxnId) {
            mutableStateOf(
                editingTxn?.partyName
                    ?: activeParties.firstOrNull()?.name
                    ?: "Rajesh Traders"
            )
        }
        var customPartyGstin by remember(editingTxnId) {
            mutableStateOf(
                editingTxn?.partyGstin
                    ?: activeParties.firstOrNull()?.gstin
                    ?: "27AABCU9603R1ZM"
            )
        }
        var placeOfSupplyStateCode by remember(editingTxnId) {
            mutableStateOf(
                editingTxn?.placeOfSupplyStateCode
                    ?: activeParties.firstOrNull()?.stateCode
                    ?: activeCompany?.stateCode
                    ?: "27"
            )
        }
        var paymentTerms by remember(editingTxnId) { mutableStateOf(editingTxn?.paymentTerms ?: "15 Days Credit") }
        var isReverseCharge by remember(editingTxnId) { mutableStateOf(editingTxn?.isReverseCharge ?: false) }
        var isExportOrSez by remember(editingTxnId) { mutableStateOf(editingTxn?.supplyType ?: "AUTO") }
        var invoiceDiscountRupeesText by remember(editingTxnId) {
            mutableStateOf(if ((editingTxn?.invoiceDiscountPaise ?: 0L) > 0L) (editingTxn!!.invoiceDiscountPaise / 100L).toString() else "")
        }
        var freightChargeRupeesText by remember(editingTxnId) {
            val firstChg = existingCharges.firstOrNull()
            mutableStateOf(if (firstChg != null) (firstChg.amountPaise / 100L).toString() else "")
        }
        var tcsPercentText by remember(editingTxnId) {
            mutableStateOf(if ((editingTxn?.tcsBps ?: 0) > 0) (editingTxn!!.tcsBps / 100.0).toString() else "")
        }
        var tdsPercentText by remember(editingTxnId) {
            mutableStateOf(if ((editingTxn?.tdsBps ?: 0) > 0) (editingTxn!!.tdsBps / 100.0).toString() else "")
        }
        var selectedRoundingMode by remember(editingTxnId) {
            mutableStateOf(
                editingTxn?.roundingMode?.let {
                    runCatching { RoundingModeOption.valueOf(it) }.getOrNull()
                } ?: roundingMode
            )
        }

        // Variant-specific state (Purchase, Returns, Challan)
        var supplierBillNo by rememberSaveable(editingTxnId) { mutableStateOf("") }
        var supplierBillDateText by rememberSaveable(editingTxnId) { mutableStateOf(dateFmt.format(Date())) }
        var itcEligible by rememberSaveable(editingTxnId) { mutableStateOf(true) }
        var returnOriginalInvoiceNo by rememberSaveable(editingTxnId) {
            mutableStateOf(effectiveTxns.firstOrNull()?.docNumber ?: "INV/26-27/0001")
        }
        var returnSettlementMode by rememberSaveable(editingTxnId) { mutableStateOf("ADJUST") }
        var challanHidePrices by rememberSaveable(editingTxnId) { mutableStateOf(false) }

        // More Details (collapsed by default)
        var moreDetailsExpanded by rememberSaveable { mutableStateOf(false) }
        var notesDraft by remember(editingTxnId) { mutableStateOf(editingTxn?.notes ?: "") }
        var termsDraft by remember(editingTxnId) {
            mutableStateOf(editingTxn?.termsText ?: "Goods once sold will not be taken back. Subject to local jurisdiction.")
        }
        var transportVehicleNo by rememberSaveable(editingTxnId) { mutableStateOf("") }
        var eWayBillNo by rememberSaveable(editingTxnId) { mutableStateOf("") }
        var attachmentRefNote by rememberSaveable(editingTxnId) { mutableStateOf("") }
        var includeAuthorisedSignatory by rememberSaveable(editingTxnId) { mutableStateOf(true) }

        // Payment state — default mode is Cash (never Bank by default)
        var lastUsedPaymentMode by rememberSaveable { mutableStateOf("Cash") }
        var receivedPaymentMode by remember(editingTxnId) { mutableStateOf(lastUsedPaymentMode) }
        var receivedNowRupeesText by remember(editingTxnId, selectedDocType) {
            mutableStateOf(if (selectedDocType == SalesDocTypes.CASH_SALE) "FULL" else "")
        }

        // Top bar & sheets state
        var showTopThreeDotMenu by remember { mutableStateOf(false) }
        var showQuickAddPartyDialog by remember { mutableStateOf(false) }
        var showBarcodeQuickScanDialog by remember { mutableStateOf(false) }
        var showUnsavedChangesDialog by remember { mutableStateOf(false) }
        var hasUnsavedChanges by remember { mutableStateOf(false) }

        // Line item editor / context menu / undo delete states
        var editingLineIndex by remember { mutableIntStateOf(-1) }
        var showLineEditorDialog by remember { mutableStateOf(false) }
        var longPressedLineIndex by remember { mutableIntStateOf(-1) }
        var recentlyDeletedLine by remember { mutableStateOf<Pair<Int, BillingDraftLineItem>?>(null) }

        // Inline validation states
        var validationError by remember { mutableStateOf<String?>(null) }
        var pendingNegativeStockWarnings by remember { mutableStateOf<List<String>>(emptyList()) }
        var pendingSaveModeForStockWarning by remember { mutableStateOf("SAVE") }

        val draftLines = remember(editingTxnId) {
            val list = mutableStateListOf<BillingDraftLineItem>()
            if (existingLines.isNotEmpty()) {
                existingLines.forEach { ln ->
                    list.add(
                        BillingDraftLineItem(
                            id = ln.id,
                            itemId = ln.itemId,
                            itemName = ln.itemName,
                            itemCode = ln.itemCode,
                            hsnSacCode = ln.hsnSacCode,
                            unit = ln.unit,
                            qty = ln.qty,
                            freeQty = ln.freeQty,
                            unitPricePaise = ln.unitPricePaise,
                            purchaseCostPaise = ln.purchaseCostPaise,
                            discountPercent = ln.discountBps / 100,
                            taxRateBps = ln.taxRateBps,
                            cessBps = ln.cessBps,
                            taxInclusive = ln.taxInclusive,
                            batchId = ln.batchId,
                            batchNumber = ln.batchNumber,
                            serialNumbersCsv = ln.serialNumbersCsv
                        )
                    )
                }
            } else {
                val firstItem = activeItems.firstOrNull()
                if (firstItem != null) {
                    val firstBatch = itemBatches.firstOrNull { it.itemId == firstItem.id && !it.isDeleted }
                    list.add(
                        BillingDraftLineItem(
                            itemId = firstItem.id,
                            itemName = firstItem.name,
                            itemCode = firstItem.itemCode,
                            hsnSacCode = firstItem.hsnSacCode,
                            unit = firstItem.primaryUnit,
                            qty = 2L,
                            freeQty = 0L,
                            unitPricePaise = if (isPurchaseVariant(selectedDocType)) firstItem.purchasePricePaise else firstItem.salePricePaise,
                            purchaseCostPaise = firstItem.purchasePricePaise,
                            taxRateBps = firstItem.taxRateBps,
                            cessBps = firstItem.cessBps,
                            taxInclusive = firstItem.taxInclusive,
                            batchId = firstBatch?.id ?: "",
                            batchNumber = firstBatch?.batchNumber ?: ""
                        )
                    )
                } else {
                    list.add(
                        BillingDraftLineItem(
                            itemId = "",
                            itemName = "India Gate Classic Basmati Rice (25kg Bag)",
                            itemCode = "SKU-RICE-25",
                            hsnSacCode = "1006",
                            unit = "BAG",
                            qty = 2L,
                            unitPricePaise = 2_850_00L,
                            purchaseCostPaise = 2_400_00L,
                            taxRateBps = 500
                        )
                    )
                }
            }
            list
        }

        // Scroll to top when validation error triggers
        LaunchedEffect(validationError) {
            if (!validationError.isNullOrBlank()) {
                coroutineScope.launch {
                    builderLazyListState.animateScrollToItem(0)
                }
            }
        }

        val supplierStateCode = activeCompany?.stateCode?.ifBlank { "27" } ?: "27"
        val isComposition = activeCompany?.gstMode == "COMPOSITION" || selectedDocType == SalesDocTypes.BILL_OF_SUPPLY
        val forcedSupplyType = when (isExportOrSez) {
            "EXPORT_WITHOUT_TAX" -> GstSupplyType.EXPORT_WITHOUT_TAX
            "SEZ_WITH_TAX" -> GstSupplyType.SEZ_WITH_TAX
            else -> null
        }

        val gstInput = remember(
            supplierStateCode,
            placeOfSupplyStateCode,
            customPartyGstin,
            isComposition,
            isReverseCharge,
            forcedSupplyType,
            draftLines.toList(),
            invoiceDiscountRupeesText,
            freightChargeRupeesText,
            tcsPercentText,
            tdsPercentText,
            selectedRoundingMode
        ) {
            val invDiscPaise = (invoiceDiscountRupeesText.toLongOrNull() ?: 0L) * 100L
            val freightPaise = (freightChargeRupeesText.toLongOrNull() ?: 0L) * 100L
            val tcsBps = ((tcsPercentText.toDoubleOrNull() ?: 0.0) * 100).toInt()
            val tdsBps = ((tdsPercentText.toDoubleOrNull() ?: 0.0) * 100).toInt()

            GstDocumentInput(
                supplierStateCode = supplierStateCode,
                placeOfSupplyStateCode = placeOfSupplyStateCode,
                partyGstin = customPartyGstin,
                isCompositionScheme = isComposition,
                isReverseCharge = isReverseCharge,
                forcedSupplyType = forcedSupplyType,
                lines = draftLines.map { dl ->
                    GstLineInput(
                        lineId = dl.id,
                        itemId = dl.itemId,
                        itemName = dl.itemName,
                        hsnSacCode = dl.hsnSacCode,
                        unit = dl.unit,
                        qty = dl.qty,
                        freeQty = dl.freeQty,
                        unitPricePaise = dl.unitPricePaise,
                        purchaseCostPaise = dl.purchaseCostPaise,
                        taxInclusive = dl.taxInclusive,
                        discountBps = dl.effectiveDiscountBps(),
                        taxCategory = dl.taxCategory,
                        taxRateBps = dl.taxRateBps,
                        cessBps = dl.cessBps
                    )
                },
                invoiceDiscountPaise = invDiscPaise,
                additionalCharges = if (freightPaise > 0L) {
                    listOf(
                        GstAdditionalChargeInput(
                            id = "freight_1",
                            label = "Freight & Forwarding",
                            amountPaise = freightPaise,
                            taxRateBps = 500
                        )
                    )
                } else {
                    emptyList()
                },
                tcsBps = tcsBps,
                tdsBps = tdsBps,
                roundingMode = selectedRoundingMode
            )
        }

        val gstResult = remember(gstInput) { GstCalculationEngine.calculate(gstInput) }

        // Group taxes by GST slab rate for BillSummaryCard
        val taxSlabBreakup = remember(gstResult) {
            gstResult.lines
                .groupBy { it.taxRateBps }
                .map { (rateBps, linesForSlab) ->
                    TaxSlabBreakupRow(
                        taxRateBps = rateBps,
                        taxablePaise = linesForSlab.sumOf { it.netTaxablePaise },
                        cgstPaise = linesForSlab.sumOf { it.cgstPaise },
                        sgstPaise = linesForSlab.sumOf { it.sgstPaise },
                        igstPaise = linesForSlab.sumOf { it.igstPaise },
                        cessPaise = linesForSlab.sumOf { it.cessPaise }
                    )
                }
                .sortedBy { it.taxRateBps }
        }

        val selectedPartyEntity = activeParties.firstOrNull { it.id == selectedPartyId }
        val creditEvaluation = remember(selectedPartyEntity, gstResult.grandTotalPaise, selectedDocType) {
            if (selectedPartyEntity != null &&
                SalesDocTypes.affectsPartyLedger(selectedDocType) &&
                !SalesDocTypes.isCreditToPartyLedger(selectedDocType)
            ) {
                PartyLedgerCalculator.evaluateCreditLimitWarning(
                    party = selectedPartyEntity,
                    newInvoiceAmount = Money(gstResult.grandTotalPaise)
                )
            } else null
        }

        val currentReceivedPaise = remember(receivedNowRupeesText, gstResult.grandTotalPaise, selectedDocType, isCashSaleToggle) {
            when {
                !shouldShowPaymentCard(selectedDocType) -> 0L
                selectedDocType == SalesDocTypes.CASH_SALE || isCashSaleToggle || receivedNowRupeesText == "FULL" ->
                    gstResult.grandTotalPaise
                else ->
                    ((receivedNowRupeesText.toLongOrNull() ?: 0L) * 100L).coerceAtMost(gstResult.grandTotalPaise)
            }
        }
        val currentBalanceDuePaise = (gstResult.grandTotalPaise - currentReceivedPaise).coerceAtLeast(0L)

        // Shared validation + save handler supporting Save, Save & New, Save & Share, Save as Draft
        val executeSave: (String, Boolean) -> Unit = remember(
            selectedDocType,
            isCashSaleToggle,
            docPrefix,
            docSequenceNumber,
            txnDateMillis,
            dueDaysOffset,
            selectedPartyId,
            customPartyName,
            customPartyGstin,
            placeOfSupplyStateCode,
            isReverseCharge,
            isComposition,
            gstResult,
            gstInput,
            selectedRoundingMode,
            receivedNowRupeesText,
            receivedPaymentMode,
            paymentTerms,
            notesDraft,
            termsDraft,
            supplierBillNo,
            itcEligible,
            returnOriginalInvoiceNo,
            returnSettlementMode,
            transportVehicleNo,
            eWayBillNo,
            editingTxn,
            activeCompany,
            activeItems,
            transactions
        ) {
            saveLambda@{ saveMode: String, ignoreNegativeStock: Boolean ->
                validationError = null

                // 1. Validate Party Missing (except Cash Sale or Draft)
                val isCashDoc = selectedDocType == SalesDocTypes.CASH_SALE || isCashSaleToggle
                if (saveMode != "SAVE_AS_DRAFT" && !isCashDoc && customPartyName.trim().isBlank()) {
                    validationError = "Party is required for ${SalesDocTypes.displayLabel(selectedDocType)}. Please select or enter a customer/supplier name."
                    return@saveLambda
                }

                // 2. Validate No Items
                if (draftLines.isEmpty()) {
                    validationError = "No items added. Please add at least one item line to save this bill."
                    return@saveLambda
                }

                // 3. Validate Quantity Zero
                val zeroQtyLine = draftLines.firstOrNull { it.qty <= 0L }
                if (zeroQtyLine != null) {
                    validationError = "Quantity must be greater than zero for '${zeroQtyLine.itemName}'."
                    return@saveLambda
                }

                // 4. Negative Stock Warning check for outward stock documents
                if (!ignoreNegativeStock &&
                    saveMode != "SAVE_AS_DRAFT" &&
                    SalesDocTypes.affectsStock(selectedDocType) &&
                    !SalesDocTypes.isInwardStock(selectedDocType)
                ) {
                    val warnings = draftLines.mapNotNull { dl ->
                        val item = activeItems.firstOrNull { it.id == dl.itemId }
                        if (item != null && item.itemType == "PRODUCT") {
                            val totalOutQty = dl.qty + dl.freeQty
                            if (totalOutQty > item.currentStockQty) {
                                "${item.name}: Requested $totalOutQty ${dl.unit}, Available ${item.currentStockQty} ${item.primaryUnit}"
                            } else null
                        } else null
                    }
                    if (warnings.isNotEmpty()) {
                        pendingNegativeStockWarnings = warnings
                        pendingSaveModeForStockWarning = saveMode
                        return@saveLambda
                    }
                }
                pendingNegativeStockWarnings = emptyList()

                val now = System.currentTimeMillis()
                val txnId = editingTxn?.id ?: UUID.randomUUID().toString()
                val companyId = activeCompany?.id ?: "local"
                val receivedPaise = if (saveMode == "SAVE_AS_DRAFT") 0L else currentReceivedPaise
                val balancePaise = (gstResult.grandTotalPaise - receivedPaise).coerceAtLeast(0L)
                val computedStatus = when {
                    saveMode == "SAVE_AS_DRAFT" -> "Draft"
                    selectedDocType == SalesDocTypes.CREDIT_NOTE && returnSettlementMode == "ADJUST" -> "Paid"
                    receivedPaise >= gstResult.grandTotalPaise && gstResult.grandTotalPaise > 0L -> "Paid"
                    receivedPaise > 0L -> "Partial"
                    else -> "Unpaid"
                }
                val posStateName = Validators.indianStateMap[placeOfSupplyStateCode] ?: "Maharashtra"
                val formattedCustomDocNo = "${docPrefix.ifBlank { SalesDocTypes.defaultPrefix(selectedDocType) }}${docSequenceNumber.toString().padStart(4, '0')}"
                val finalDocNo = editingTxn?.docNumber?.takeIf { it.isNotBlank() }
                    ?: if (transactions.isEmpty()) formattedCustomDocNo else ""

                val combinedNotes = buildString {
                    if (notesDraft.isNotBlank()) append(notesDraft.trim())
                    if (isPurchaseVariant(selectedDocType) && supplierBillNo.isNotBlank()) {
                        if (isNotEmpty()) append(" • ")
                        append("Supplier Bill: $supplierBillNo ($supplierBillDateText) • ITC: ${if (itcEligible) "Eligible" else "Blocked"}")
                    }
                    if (isReturnVariant(selectedDocType) && returnOriginalInvoiceNo.isNotBlank()) {
                        if (isNotEmpty()) append(" • ")
                        append("Against: $returnOriginalInvoiceNo ($returnSettlementMode)")
                    }
                    if (transportVehicleNo.isNotBlank() || eWayBillNo.isNotBlank()) {
                        if (isNotEmpty()) append(" • ")
                        append("Vehicle: $transportVehicleNo E-Way: $eWayBillNo")
                    }
                }

                val header = TxnEntity(
                    id = txnId,
                    companyId = companyId,
                    docType = if (isCashSaleToggle && selectedDocType == SalesDocTypes.TAX_INVOICE) SalesDocTypes.CASH_SALE else selectedDocType,
                    docNumber = finalDocNo,
                    txnDate = txnDateMillis,
                    dueDate = txnDateMillis + dueDaysOffset.toLong() * 86_400_000L,
                    partyId = selectedPartyId,
                    partyName = customPartyName.trim().ifBlank { "Cash / Walk-in Customer" },
                    partyGstin = customPartyGstin.trim(),
                    partyPhone = selectedPartyEntity?.phone ?: "",
                    placeOfSupplyStateCode = placeOfSupplyStateCode,
                    placeOfSupplyStateName = posStateName,
                    supplyType = gstResult.supplyType.name,
                    classification = gstResult.classification.name,
                    isReverseCharge = isReverseCharge,
                    isComposition = isComposition,
                    grossSubtotalPaise = gstResult.grossSubtotalPaise,
                    lineDiscountTotalPaise = gstResult.lineDiscountTotalPaise,
                    invoiceDiscountPaise = gstResult.invoiceDiscountPaise,
                    taxableSubtotalPaise = gstResult.taxableSubtotalPaise,
                    cgstTotalPaise = gstResult.cgstTotalPaise,
                    sgstTotalPaise = gstResult.sgstTotalPaise,
                    igstTotalPaise = gstResult.igstTotalPaise,
                    cessTotalPaise = gstResult.cessTotalPaise,
                    totalTaxPaise = gstResult.totalTaxPaise,
                    additionalChargesTotalPaise = gstResult.additionalChargesSubtotalPaise,
                    tcsBps = gstInput.tcsBps,
                    tcsPaise = gstResult.tcsPaise,
                    tdsBps = gstInput.tdsBps,
                    tdsPaise = gstResult.tdsPaise,
                    roundOffPaise = gstResult.roundOffPaise,
                    roundingMode = selectedRoundingMode.name,
                    grandTotalPaise = gstResult.grandTotalPaise,
                    paidAmountPaise = receivedPaise,
                    balanceAmountPaise = balancePaise,
                    totalCostPaise = gstResult.totalCostPaise,
                    grossProfitPaise = if (selectedDocType == SalesDocTypes.CREDIT_NOTE) -gstResult.grossProfitPaise else gstResult.grossProfitPaise,
                    status = computedStatus,
                    paymentTerms = paymentTerms,
                    notes = combinedNotes,
                    termsText = termsDraft,
                    amountInWords = gstResult.amountInWords,
                    warehouseId = "${companyId}_wh_main",
                    warehouseName = "Main Godown",
                    createdAt = editingTxn?.createdAt ?: now,
                    updatedAt = now
                )

                val lineEntities = gstResult.lines.mapIndexed { i, cl ->
                    val draft = draftLines.getOrNull(i)
                    TxnLineEntity(
                        id = UUID.randomUUID().toString(),
                        companyId = companyId,
                        txnId = txnId,
                        lineIndex = i,
                        itemId = cl.itemId,
                        itemName = cl.itemName,
                        itemCode = draft?.itemCode ?: "",
                        hsnSacCode = cl.hsnSacCode,
                        batchId = draft?.batchId ?: "",
                        batchNumber = draft?.batchNumber ?: "",
                        serialNumbersCsv = draft?.serialNumbersCsv ?: "",
                        qty = cl.qty,
                        freeQty = cl.freeQty,
                        unit = cl.unit,
                        unitPricePaise = cl.unitPricePaise,
                        purchaseCostPaise = cl.purchaseCostPaise,
                        taxInclusive = cl.taxInclusive,
                        discountBps = draft?.effectiveDiscountBps() ?: 0,
                        discountPaise = cl.lineDiscountPaise + cl.allocatedInvoiceDiscountPaise,
                        taxCategory = cl.taxCategory.name,
                        taxRateBps = cl.taxRateBps,
                        cessBps = cl.cessBps,
                        taxablePaise = cl.netTaxablePaise,
                        cgstPaise = cl.cgstPaise,
                        sgstPaise = cl.sgstPaise,
                        igstPaise = cl.igstPaise,
                        cessPaise = cl.cessPaise,
                        lineTotalPaise = cl.lineTotalPaise,
                        createdAt = now,
                        updatedAt = now
                    )
                }

                val chargeEntities = gstResult.charges.map { ch ->
                    TxnChargeEntity(
                        id = UUID.randomUUID().toString(),
                        companyId = companyId,
                        txnId = txnId,
                        label = ch.label,
                        amountPaise = ch.amountPaise,
                        taxRateBps = ch.taxRateBps,
                        cgstPaise = ch.cgstPaise,
                        sgstPaise = ch.sgstPaise,
                        igstPaise = ch.igstPaise,
                        totalChargePaise = ch.totalWithTaxPaise,
                        createdAt = now,
                        updatedAt = now
                    )
                }

                val taxEntities = gstResult.hsnSummary.map { hsn ->
                    TxnTaxLineEntity(
                        id = UUID.randomUUID().toString(),
                        companyId = companyId,
                        txnId = txnId,
                        hsnSacCode = hsn.hsnSacCode,
                        description = hsn.description,
                        uqc = hsn.uqc,
                        totalQty = hsn.totalQty,
                        taxablePaise = hsn.taxablePaise,
                        taxRateBps = hsn.taxRateBps,
                        cgstPaise = hsn.cgstPaise,
                        sgstPaise = hsn.sgstPaise,
                        igstPaise = hsn.igstPaise,
                        cessPaise = hsn.cessPaise,
                        totalTaxPaise = hsn.totalTaxPaise,
                        createdAt = now,
                        updatedAt = now
                    )
                }

                lastUsedPaymentMode = receivedPaymentMode

                if (transactions.isEmpty()) {
                    val existingIdx = localFallbackDocs.indexOfFirst { it.id == header.id }
                    if (existingIdx >= 0) {
                        localFallbackDocs[existingIdx] = header
                    } else {
                        localFallbackDocs.add(0, header)
                    }
                } else {
                    onSaveOrUpdateDocument(
                        header,
                        lineEntities,
                        chargeEntities,
                        taxEntities,
                        null,
                        null,
                        receivedPaise,
                        receivedPaymentMode.uppercase(Locale.getDefault())
                    )
                }

                if (saveMode == "SAVE_AND_SHARE") {
                    val shareTxn = header.copy(docNumber = header.docNumber.ifBlank { formattedCustomDocNo })
                    Step3APdfAndCsvExporter.exportAndShareSalesVoucherPdf(
                        context = context,
                        company = activeCompany,
                        txn = shareTxn,
                        lines = lineEntities,
                        charges = chargeEntities,
                        taxLines = taxEntities
                    )
                }

                selectedSegment = if (header.docType == SalesDocTypes.PURCHASE_BILL) {
                    segments.indexOf("Purchases").coerceAtLeast(0)
                } else {
                    segments.indexOf(SalesDocTypes.segmentForDocType(header.docType)).coerceAtLeast(0)
                }
                hasUnsavedChanges = false

                if (saveMode == "SAVE_AND_NEW") {
                    editingTxnId = null
                    invoiceDiscountRupeesText = ""
                    freightChargeRupeesText = ""
                    notesDraft = ""
                    docSequenceNumber += 1
                    activeBanner = "✓ Saved ${SalesDocTypes.displayLabel(header.docType)} (${Money(gstResult.grandTotalPaise).formatIndian()}) • Ready for next voucher"
                } else {
                    showDocBuilderSheet = false
                    editingTxnId = null
                    activeBanner = when (saveMode) {
                        "SAVE_AS_DRAFT" -> "✓ Saved Draft ${SalesDocTypes.displayLabel(header.docType)} (${Money(gstResult.grandTotalPaise).formatIndian()})"
                        "SAVE_AND_SHARE" -> "✓ Saved & Shared ${SalesDocTypes.displayLabel(header.docType)} (${Money(gstResult.grandTotalPaise).formatIndian()})"
                        else -> "✓ Saved ${SalesDocTypes.displayLabel(header.docType)} (${Money(gstResult.grandTotalPaise).formatIndian()}) — ${draftLines.size} item(s)"
                    }
                }
            }
        }

        val handleAttemptClose: () -> Unit = {
            if (hasUnsavedChanges) {
                showUnsavedChangesDialog = true
            } else {
                showDocBuilderSheet = false
                editingTxnId = null
            }
        }

        BackHandler(enabled = showDocBuilderSheet) {
            handleAttemptClose()
        }

        Surface(
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxSize()
                .testTag("shared_voucher_builder_screen")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .navigationBarsPadding()
            ) {
                // ====================================================================
                // 1. TOP BAR: [X]  New Sale (or New Purchase / New Quotation...)  [Draft] [Save] [...]
                // ====================================================================
                SectionCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    cornerRadius = 20.dp,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = handleAttemptClose,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("builder_close_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Billing Builder",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = billingScreenTitle(selectedDocType, editingTxn?.docNumber),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("builder_screen_title")
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // [Draft] Chip
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = GoldPrimary.copy(alpha = 0.18f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { executeSave("SAVE_AS_DRAFT", true) }
                                    .testTag("builder_draft_chip")
                            ) {
                                Text(
                                    text = "Draft",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GoldPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }

                            // Top Bar Save Icon
                            IconButton(
                                onClick = { executeSave("SAVE", false) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("builder_topbar_save_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Save Voucher",
                                    tint = EmeraldPrimary
                                )
                            }

                            // Three-dot Menu (Save as draft, Save and New, Save and Share PDF, Cancel)
                            Box {
                                IconButton(
                                    onClick = { showTopThreeDotMenu = true },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("builder_top_more_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More billing actions",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                DropdownMenu(
                                    expanded = showTopThreeDotMenu,
                                    onDismissRequest = { showTopThreeDotMenu = false },
                                    containerColor = MaterialTheme.colorScheme.surface
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Save as draft", color = MaterialTheme.colorScheme.onSurface) },
                                        leadingIcon = { Icon(Icons.Default.Save, contentDescription = null, tint = GoldPrimary) },
                                        onClick = {
                                            showTopThreeDotMenu = false
                                            executeSave("SAVE_AS_DRAFT", true)
                                        },
                                        modifier = Modifier.testTag("top_menu_save_draft")
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Save and New", color = MaterialTheme.colorScheme.onSurface) },
                                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = EmeraldPrimary) },
                                        onClick = {
                                            showTopThreeDotMenu = false
                                            executeSave("SAVE_AND_NEW", false)
                                        },
                                        modifier = Modifier.testTag("top_menu_save_new")
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Save and Share PDF", color = MaterialTheme.colorScheme.onSurface) },
                                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = PurchaseBlue) },
                                        onClick = {
                                            showTopThreeDotMenu = false
                                            executeSave("SAVE_AND_SHARE", false)
                                        },
                                        modifier = Modifier.testTag("top_menu_save_share")
                                    )
                                    HorizontalDivider(color = glass.subtleDivider)
                                    DropdownMenuItem(
                                        text = { Text("Cancel", color = CoralError) },
                                        leadingIcon = { Icon(Icons.Default.Cancel, contentDescription = null, tint = CoralError) },
                                        onClick = {
                                            showTopThreeDotMenu = false
                                            handleAttemptClose()
                                        },
                                        modifier = Modifier.testTag("top_menu_cancel")
                                    )
                                }
                            }
                        }
                    }
                }

                // ====================================================================
                // 2. ONE SCROLLING COLUMN OF CARDS (12dp gaps, 16dp screen margins, 24dp radius)
                // ====================================================================
                LazyColumn(
                    state = builderLazyListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("builder_lazy_column"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Inline Validation Error Banner (auto-scrolled to top)
                    if (!validationError.isNullOrBlank()) {
                        item {
                            SectionCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("builder_validation_error_banner"),
                                cornerRadius = 18.dp,
                                accentBorderColor = CoralError,
                                contentPadding = PaddingValues(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = "Validation Error",
                                        tint = CoralError
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = validationError!!,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = CoralError,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // Negative Stock Warning Banner
                    if (pendingNegativeStockWarnings.isNotEmpty()) {
                        item {
                            SectionCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("builder_negative_stock_warning"),
                                cornerRadius = 18.dp,
                                accentBorderColor = AmberSale,
                                contentPadding = PaddingValues(14.dp)
                            ) {
                                Text(
                                    text = "⚠ Negative Stock Warning",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = AmberSale
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                pendingNegativeStockWarnings.forEach { warn ->
                                    Text(
                                        text = "• $warn",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    VanijyaButton(
                                        text = "Edit Qty",
                                        isOutlined = true,
                                        onClick = { pendingNegativeStockWarnings = emptyList() },
                                        modifier = Modifier.weight(1f)
                                    )
                                    VanijyaButton(
                                        text = "Save Anyway",
                                        containerColor = AmberSale,
                                        onClick = { executeSave(pendingSaveModeForStockWarning, true) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("builder_confirm_negative_stock_btn")
                                    )
                                }
                            }
                        }
                    }

                    // A. Document Type Selector Chips (Sale | Purchase | Quotation | Sale Order | Purchase Order | Challan | Sale Return | Purchase Return)
                    item {
                        DocTypeChips(
                            selectedDocType = selectedDocType,
                            onSelectDocType = { newType ->
                                selectedDocType = newType
                                isCashSaleToggle = (newType == SalesDocTypes.CASH_SALE)
                                docPrefix = if (newType == SalesDocTypes.TAX_INVOICE) invoicePrefix else SalesDocTypes.defaultPrefix(newType)
                                validationError = null
                                hasUnsavedChanges = true
                            }
                        )
                    }

                    // B. Document Number Chip ("INV-0001 [pencil]") + Date + Due/Valid/Delivery
                    item {
                        DocNumberChip(
                            docType = selectedDocType,
                            docPrefix = docPrefix,
                            docSequenceNumber = docSequenceNumber,
                            txnDateMillis = txnDateMillis,
                            dueDaysOffset = dueDaysOffset,
                            onUpdatePrefixAndSequence = { newPref, newSeq ->
                                docPrefix = newPref
                                docSequenceNumber = newSeq
                                hasUnsavedChanges = true
                            },
                            onUpdateDueDaysOffset = { newDays ->
                                dueDaysOffset = newDays
                                paymentTerms = "$newDays Days Credit"
                                hasUnsavedChanges = true
                            }
                        )
                    }

                    // C. PARTY CARD (Customer / Supplier + Search + Recent Chips + Place of Supply + Variants)
                    item {
                        PartyCard(
                            docType = selectedDocType,
                            parties = activeParties,
                            selectedPartyId = selectedPartyId,
                            partyNameInput = customPartyName,
                            partyGstinInput = customPartyGstin,
                            supplierStateCode = supplierStateCode,
                            placeOfSupplyStateCode = placeOfSupplyStateCode,
                            isReverseCharge = isReverseCharge,
                            supplyTypeOverride = isExportOrSez,
                            isCashSaleToggle = isCashSaleToggle,
                            creditEvaluation = creditEvaluation,
                            supplierBillNo = supplierBillNo,
                            supplierBillDateText = supplierBillDateText,
                            itcEligible = itcEligible,
                            returnOriginalInvoiceNo = returnOriginalInvoiceNo,
                            returnSettlementMode = returnSettlementMode,
                            challanHidePrices = challanHidePrices,
                            recentInvoicesForReturn = effectiveTxns,
                            hasValidationError = validationError?.contains("Party", ignoreCase = true) == true,
                            hideAmounts = hideAmounts,
                            decimals = decimals,
                            onPartySelected = { party ->
                                selectedPartyId = party.id
                                customPartyName = party.name
                                customPartyGstin = party.gstin
                                placeOfSupplyStateCode = party.stateCode.ifBlank { supplierStateCode }
                                dueDaysOffset = party.creditDays
                                paymentTerms = "${party.creditDays} Days Credit"
                                validationError = null
                                hasUnsavedChanges = true
                            },
                            onPartyNameChange = {
                                customPartyName = it
                                validationError = null
                                hasUnsavedChanges = true
                            },
                            onPartyGstinChange = {
                                val up = it.uppercase().take(15)
                                customPartyGstin = up
                                if (up.length >= 2 && Validators.indianStateMap.containsKey(up.take(2))) {
                                    placeOfSupplyStateCode = up.take(2)
                                }
                                hasUnsavedChanges = true
                            },
                            onPlaceOfSupplyChange = { stateCode, supplyOverride, rcm ->
                                placeOfSupplyStateCode = stateCode
                                isExportOrSez = supplyOverride
                                isReverseCharge = rcm
                                hasUnsavedChanges = true
                            },
                            onToggleCashSale = { enabled ->
                                isCashSaleToggle = enabled
                                if (enabled) {
                                    receivedNowRupeesText = "FULL"
                                    validationError = null
                                }
                                hasUnsavedChanges = true
                            },
                            onOpenNewPartyDialog = { showQuickAddPartyDialog = true },
                            onSupplierBillNoChange = {
                                supplierBillNo = it
                                hasUnsavedChanges = true
                            },
                            onSupplierBillDateChange = {
                                supplierBillDateText = it
                                hasUnsavedChanges = true
                            },
                            onItcEligibleChange = {
                                itcEligible = it
                                hasUnsavedChanges = true
                            },
                            onReturnOriginalInvoiceChange = {
                                returnOriginalInvoiceNo = it
                                hasUnsavedChanges = true
                            },
                            onReturnSettlementModeChange = {
                                returnSettlementMode = it
                                hasUnsavedChanges = true
                            },
                            onChallanHidePricesChange = {
                                challanHidePrices = it
                                hasUnsavedChanges = true
                            }
                        )
                    }

                    // D. ITEMS SECTION HEADER: ITEMS (N) + [+ Add item]
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                                .testTag("builder_items_summary_header"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ITEMS (${draftLines.size})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            VanijyaAddButton(
                                text = "Add item",
                                icon = Icons.Default.Add,
                                isTextButton = true,
                                onClick = {
                                    editingLineIndex = -1
                                    showLineEditorDialog = true
                                },
                                testTag = "builder_header_add_item_btn"
                            )
                        }
                    }

                    // Undo banner when an item line was deleted
                    if (recentlyDeletedLine != null) {
                        item {
                            val (deletedIdx, deletedItem) = recentlyDeletedLine!!
                            SectionCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("undo_delete_line_banner"),
                                cornerRadius = 16.dp,
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Removed '${deletedItem.itemName}'",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(
                                        onClick = {
                                            val insertAt = deletedIdx.coerceIn(0, draftLines.size)
                                            draftLines.add(insertAt, deletedItem)
                                            recentlyDeletedLine = null
                                        },
                                        modifier = Modifier.testTag("undo_delete_line_btn")
                                    ) {
                                        Icon(Icons.Default.Undo, contentDescription = "Undo delete", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Undo", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // E. ITEM LINE CARDS (Compact card with [- qty +] stepper, [edit], [bin], swipe-to-delete, reorder)
                    itemsIndexed(
                        items = draftLines,
                        key = { _, line -> line.id }
                    ) { idx, line ->
                        val calcLine = gstResult.lines.getOrNull(idx)
                        ItemLineCard(
                            index = idx,
                            totalLinesCount = draftLines.size,
                            line = line,
                            calculatedLine = calcLine,
                            hidePrices = challanHidePrices && selectedDocType == SalesDocTypes.DELIVERY_CHALLAN,
                            hideAmounts = hideAmounts,
                            decimals = decimals,
                            onQuantityChange = { newQty ->
                                if (idx in draftLines.indices) {
                                    draftLines[idx] = draftLines[idx].copy(qty = newQty.coerceAtLeast(1L))
                                    hasUnsavedChanges = true
                                }
                            },
                            onEditClick = {
                                editingLineIndex = idx
                                showLineEditorDialog = true
                            },
                            onDeleteClick = {
                                if (idx in draftLines.indices) {
                                    val removed = draftLines.removeAt(idx)
                                    recentlyDeletedLine = idx to removed
                                    hasUnsavedChanges = true
                                }
                            },
                            onMoveUp = {
                                if (idx > 0 && idx in draftLines.indices) {
                                    val item = draftLines.removeAt(idx)
                                    draftLines.add(idx - 1, item)
                                    hasUnsavedChanges = true
                                }
                            },
                            onMoveDown = {
                                if (idx < draftLines.lastIndex && idx in draftLines.indices) {
                                    val item = draftLines.removeAt(idx)
                                    draftLines.add(idx + 1, item)
                                    hasUnsavedChanges = true
                                }
                            },
                            onLongClick = {
                                longPressedLineIndex = idx
                            }
                        )
                    }

                    // F. [ + Add another item ] & [ Scan barcode ] + Quick Catalog Chips
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            VanijyaAddButton(
                                text = "Add another item",
                                icon = Icons.Default.Add,
                                isOutlined = true,
                                onClick = {
                                    editingLineIndex = -1
                                    showLineEditorDialog = true
                                },
                                modifier = Modifier.weight(1.3f),
                                testTag = "builder_add_item_btn"
                            )
                            VanijyaButton(
                                text = "Scan barcode",
                                icon = Icons.Default.QrCodeScanner,
                                isOutlined = true,
                                onClick = { showBarcodeQuickScanDialog = true },
                                modifier = Modifier.weight(1f),
                                testTag = "builder_scan_barcode_btn"
                            )
                        }

                        if (activeItems.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Quick add:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                activeItems.take(6).forEach { item ->
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            val batch = itemBatches.firstOrNull { it.itemId == item.id && !it.isDeleted }
                                            val defaultRate = if (isPurchaseVariant(selectedDocType)) {
                                                item.purchasePricePaise
                                            } else {
                                                item.salePricePaise
                                            }
                                            draftLines.add(
                                                BillingDraftLineItem(
                                                    itemId = item.id,
                                                    itemName = item.name,
                                                    itemCode = item.itemCode,
                                                    hsnSacCode = item.hsnSacCode,
                                                    unit = item.primaryUnit,
                                                    qty = 1L,
                                                    unitPricePaise = defaultRate,
                                                    purchaseCostPaise = item.purchasePricePaise,
                                                    taxRateBps = item.taxRateBps,
                                                    cessBps = item.cessBps,
                                                    taxInclusive = item.taxInclusive,
                                                    batchId = batch?.id ?: "",
                                                    batchNumber = batch?.batchNumber ?: ""
                                                )
                                            )
                                            validationError = null
                                            hasUnsavedChanges = true
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        },
                                        label = { Text(item.name.take(18), maxLines = 1, fontSize = 12.sp) },
                                        colors = vanijyaFilterChipColors()
                                    )
                                }
                            }
                        }
                    }

                    // G. BILL SUMMARY CARD (Subtotal, Discount in emerald, Taxable, Tax breakup, Other charges, Round off, 28sp Gradient Grand Total, Words)
                    item {
                        BillSummaryCard(
                            grossSubtotalPaise = gstResult.grossSubtotalPaise,
                            totalDiscountPaise = gstResult.lineDiscountTotalPaise + gstResult.invoiceDiscountPaise,
                            taxableAmountPaise = gstResult.taxableSubtotalPaise,
                            cgstTotalPaise = gstResult.cgstTotalPaise,
                            sgstTotalPaise = gstResult.sgstTotalPaise,
                            igstTotalPaise = gstResult.igstTotalPaise,
                            cessTotalPaise = gstResult.cessTotalPaise,
                            otherChargesPaise = gstResult.additionalChargesSubtotalPaise,
                            roundOffPaise = gstResult.roundOffPaise,
                            grandTotalPaise = gstResult.grandTotalPaise,
                            amountInWords = gstResult.amountInWords,
                            supplyType = gstResult.supplyType,
                            taxSlabBreakup = taxSlabBreakup,
                            isEditable = true,
                            invoiceDiscountInputText = invoiceDiscountRupeesText,
                            freightChargeInputText = freightChargeRupeesText,
                            onInvoiceDiscountChange = {
                                invoiceDiscountRupeesText = it
                                hasUnsavedChanges = true
                            },
                            onFreightChargeChange = {
                                freightChargeRupeesText = it
                                hasUnsavedChanges = true
                            },
                            hideAmounts = hideAmounts,
                            decimals = decimals
                        )
                    }

                    // H. PAYMENT CARD (Hidden for Quotation, Order, Challan; Cash default; Coral/Emerald Balance Due)
                    if (shouldShowPaymentCard(selectedDocType)) {
                        item {
                            PaymentCard(
                                docType = selectedDocType,
                                grandTotalPaise = gstResult.grandTotalPaise,
                                amountInputText = receivedNowRupeesText,
                                selectedPaymentMode = receivedPaymentMode,
                                hideAmounts = hideAmounts,
                                decimals = decimals,
                                onAmountInputChange = {
                                    receivedNowRupeesText = it
                                    hasUnsavedChanges = true
                                },
                                onSelectPaymentStatusChip = { status ->
                                    receivedNowRupeesText = when (status) {
                                        "Paid" -> "FULL"
                                        "Partial" -> ((gstResult.grandTotalPaise / 200L).coerceAtLeast(1L)).toString()
                                        else -> ""
                                    }
                                    hasUnsavedChanges = true
                                },
                                onSelectPaymentMode = { mode ->
                                    receivedPaymentMode = mode
                                    lastUsedPaymentMode = mode
                                    hasUnsavedChanges = true
                                }
                            )
                        }
                    }

                    // I. MORE DETAILS (Collapsed by default: Notes, Terms, Attachments, Signature, Transport/Vehicle)
                    item {
                        SectionCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("more_details_card"),
                            sectionTitle = "More Details",
                            headerTrailing = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { moreDetailsExpanded = !moreDetailsExpanded }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .testTag("more_details_toggle_btn")
                                ) {
                                    Text(
                                        text = if (moreDetailsExpanded) "Collapse" else "Notes, Terms, Transport & Sign",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Icon(
                                        imageVector = if (moreDetailsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle More Details",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        ) {
                            if (!moreDetailsExpanded) {
                                Text(
                                    text = "Tap to edit invoice notes, terms & conditions, vehicle/E-Way bill number, attachments, and signatory.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            AnimatedVisibility(visible = moreDetailsExpanded) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = notesDraft,
                                        onValueChange = {
                                            notesDraft = it
                                            hasUnsavedChanges = true
                                        },
                                        label = { Text("Customer / Voucher Notes", fontSize = 12.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = vanijyaTextFieldColors(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("more_details_notes_input")
                                    )
                                    OutlinedTextField(
                                        value = termsDraft,
                                        onValueChange = {
                                            termsDraft = it
                                            hasUnsavedChanges = true
                                        },
                                        label = { Text("Terms & Conditions", fontSize = 12.sp) },
                                        minLines = 2,
                                        maxLines = 3,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = vanijyaTextFieldColors(),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = transportVehicleNo,
                                            onValueChange = {
                                                transportVehicleNo = it.uppercase().take(15)
                                                hasUnsavedChanges = true
                                            },
                                            label = { Text("Vehicle No.", fontSize = 12.sp) },
                                            placeholder = { Text("MH12AB1234") },
                                            singleLine = true,
                                            shape = RoundedCornerShape(14.dp),
                                            colors = vanijyaTextFieldColors(),
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = eWayBillNo,
                                            onValueChange = {
                                                eWayBillNo = it.filter(Char::isDigit).take(12)
                                                hasUnsavedChanges = true
                                            },
                                            label = { Text("E-Way Bill No.", fontSize = 12.sp) },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = vanijyaTextFieldColors(),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    OutlinedTextField(
                                        value = attachmentRefNote,
                                        onValueChange = {
                                            attachmentRefNote = it
                                            hasUnsavedChanges = true
                                        },
                                        label = { Text("Attachment / PO Reference", fontSize = 12.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        colors = vanijyaTextFieldColors(),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Include Authorised Signatory on PDF",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Switch(
                                            checked = includeAuthorisedSignatory,
                                            onCheckedChange = { includeAuthorisedSignatory = it }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ====================================================================
                // 3. STICKY BOTTOM BAR: Total / Balance Left + [ Save & Generate Invoice (₹...) ] [v] Right
                // ====================================================================
                StickyTotalBar(
                    docType = selectedDocType,
                    grandTotalPaise = gstResult.grandTotalPaise,
                    balanceDuePaise = currentBalanceDuePaise,
                    hideAmounts = hideAmounts,
                    decimals = decimals,
                    onPrimarySave = { executeSave("SAVE", false) },
                    onSaveAndShare = { executeSave("SAVE_AND_SHARE", false) },
                    onSaveAndNew = { executeSave("SAVE_AND_NEW", false) },
                    onSaveAsDraft = { executeSave("SAVE_AS_DRAFT", true) },
                    onCancel = handleAttemptClose
                )
            }
        }

        // ====================================================================
        // ITEM EDIT / ADD BOTTOM SHEET (ItemEditSheet)
        // ====================================================================
        if (showLineEditorDialog) {
            val isEditingExistingLine = editingLineIndex in draftLines.indices
            val currentLine = if (isEditingExistingLine) draftLines[editingLineIndex] else null
            ItemEditSheet(
                isEditingExistingLine = isEditingExistingLine,
                editingLineIndex = editingLineIndex,
                initialLine = currentLine,
                docType = selectedDocType,
                catalogItems = activeItems,
                itemBatches = itemBatches,
                taxRates = taxRates,
                onDismiss = { showLineEditorDialog = false },
                onSaveLine = { updatedItem ->
                    if (isEditingExistingLine) {
                        draftLines[editingLineIndex] = updatedItem
                    } else {
                        draftLines.add(updatedItem)
                    }
                    validationError = null
                    hasUnsavedChanges = true
                    showLineEditorDialog = false
                },
                onDeleteLine = if (isEditingExistingLine) {
                    {
                        if (editingLineIndex in draftLines.indices) {
                            val removed = draftLines.removeAt(editingLineIndex)
                            recentlyDeletedLine = editingLineIndex to removed
                            hasUnsavedChanges = true
                        }
                    }
                } else null
            )
        }

        // ====================================================================
        // QUICK BARCODE / SKU SCANNER & LOOKUP SHEET
        // ====================================================================
        if (showBarcodeQuickScanDialog) {
            var scannedCode by remember { mutableStateOf("") }
            GlassSheet(
                onDismissRequest = { showBarcodeQuickScanDialog = false },
                title = "Scan or Enter Barcode / SKU",
                subtitle = "Matches item barcode or SKU code in Stock Master and adds it to the bill"
            ) {
                OutlinedTextField(
                    value = scannedCode,
                    onValueChange = { scannedCode = it },
                    label = { Text("Barcode / EAN / SKU Code") },
                    placeholder = { Text("e.g. 8901491101844 or SKU-RICE-25") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("barcode_scan_sheet_input")
                )
                VanijyaButton(
                    text = "Add Matched Item",
                    icon = Icons.Default.Add,
                    onClick = {
                        val matched = activeItems.firstOrNull {
                            it.barcode.equals(scannedCode.trim(), ignoreCase = true) ||
                                it.itemCode.equals(scannedCode.trim(), ignoreCase = true) ||
                                it.name.contains(scannedCode.trim(), ignoreCase = true)
                        } ?: activeItems.firstOrNull()
                        if (matched != null) {
                            val defaultRate = if (isPurchaseVariant(selectedDocType)) matched.purchasePricePaise else matched.salePricePaise
                            draftLines.add(
                                BillingDraftLineItem(
                                    itemId = matched.id,
                                    itemName = matched.name,
                                    itemCode = matched.itemCode,
                                    hsnSacCode = matched.hsnSacCode,
                                    unit = matched.primaryUnit,
                                    qty = 1L,
                                    unitPricePaise = defaultRate,
                                    purchaseCostPaise = matched.purchasePricePaise,
                                    taxRateBps = matched.taxRateBps,
                                    cessBps = matched.cessBps,
                                    taxInclusive = matched.taxInclusive
                                )
                            )
                        } else {
                            draftLines.add(
                                BillingDraftLineItem(
                                    itemId = "",
                                    itemName = "Scanned Item (${scannedCode.ifBlank { "BARCODE-1" }})",
                                    itemCode = scannedCode.ifBlank { "BARCODE-1" },
                                    hsnSacCode = "1006",
                                    unit = "PCS",
                                    qty = 1L,
                                    unitPricePaise = 250_00L,
                                    purchaseCostPaise = 200_00L,
                                    taxRateBps = 1800
                                )
                            )
                        }
                        hasUnsavedChanges = true
                        showBarcodeQuickScanDialog = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("barcode_scan_add_btn")
                )
            }
        }

        // ====================================================================
        // UNSAVED CHANGES GUARD DIALOG ("Save draft or discard?")
        // ====================================================================
        if (showUnsavedChangesDialog) {
            AlertDialog(
                onDismissRequest = { showUnsavedChangesDialog = false },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                title = {
                    Text(
                        text = "Save draft or discard?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Text(
                        text = "You have unsaved changes on this ${SalesDocTypes.displayLabel(selectedDocType)}. Would you like to save it as a draft or discard your edits?",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showUnsavedChangesDialog = false
                            executeSave("SAVE_AS_DRAFT", true)
                        },
                        modifier = Modifier.testTag("unsaved_save_draft_btn")
                    ) {
                        Text("Save as Draft", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(
                            onClick = { showUnsavedChangesDialog = false },
                            modifier = Modifier.testTag("unsaved_keep_editing_btn")
                        ) {
                            Text("Keep Editing")
                        }
                        TextButton(
                            onClick = {
                                showUnsavedChangesDialog = false
                                hasUnsavedChanges = false
                                showDocBuilderSheet = false
                                editingTxnId = null
                            },
                            modifier = Modifier.testTag("unsaved_discard_btn")
                        ) {
                            Text("Discard", color = CoralError, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                modifier = Modifier.testTag("unsaved_changes_dialog")
            )
        }

        // Long-Press Line Action Sheet (Duplicate, Move Up, Move Down, Delete)
        if (longPressedLineIndex in draftLines.indices) {
            val targetIdx = longPressedLineIndex
            val targetLine = draftLines[targetIdx]
            AlertDialog(
                onDismissRequest = { longPressedLineIndex = -1 },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                title = { Text("Line #${targetIdx + 1}: ${targetLine.itemName}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        VanijyaButton(
                            text = "Duplicate Line",
                            icon = Icons.Default.ContentCopy,
                            isOutlined = true,
                            onClick = {
                                draftLines.add(
                                    targetIdx + 1,
                                    targetLine.copy(id = UUID.randomUUID().toString())
                                )
                                hasUnsavedChanges = true
                                longPressedLineIndex = -1
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "line_action_duplicate"
                        )
                        if (targetIdx > 0) {
                            VanijyaButton(
                                text = "Move Up ↑",
                                icon = Icons.Default.ArrowUpward,
                                isOutlined = true,
                                onClick = {
                                    val item = draftLines.removeAt(targetIdx)
                                    draftLines.add(targetIdx - 1, item)
                                    hasUnsavedChanges = true
                                    longPressedLineIndex = -1
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "line_action_move_up"
                            )
                        }
                        if (targetIdx < draftLines.lastIndex) {
                            VanijyaButton(
                                text = "Move Down ↓",
                                icon = Icons.Default.ArrowDownward,
                                isOutlined = true,
                                onClick = {
                                    val item = draftLines.removeAt(targetIdx)
                                    draftLines.add(targetIdx + 1, item)
                                    hasUnsavedChanges = true
                                    longPressedLineIndex = -1
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "line_action_move_down"
                            )
                        }
                        VanijyaButton(
                            text = "Delete Line",
                            icon = Icons.Default.Delete,
                            containerColor = CoralError,
                            onClick = {
                                val removed = draftLines.removeAt(targetIdx)
                                recentlyDeletedLine = targetIdx to removed
                                hasUnsavedChanges = true
                                longPressedLineIndex = -1
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "line_action_delete"
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { longPressedLineIndex = -1 }) {
                        Text("Close")
                    }
                }
            )
        }

        if (showQuickAddPartyDialog) {
            var qName by remember { mutableStateOf("") }
            var qPhone by remember { mutableStateOf("") }
            var qGstin by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showQuickAddPartyDialog = false },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                title = { Text("Quick Add Party", color = MaterialTheme.colorScheme.onSurface) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = qName,
                            onValueChange = { qName = it },
                            label = { Text("Customer / Supplier Name") },
                            singleLine = true,
                            colors = vanijyaTextFieldColors(),
                            modifier = Modifier.testTag("quick_add_party_name_input")
                        )
                        OutlinedTextField(
                            value = qPhone,
                            onValueChange = { qPhone = it.filter(Char::isDigit).take(10) },
                            label = { Text("Phone (10 digits)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = vanijyaTextFieldColors()
                        )
                        OutlinedTextField(
                            value = qGstin,
                            onValueChange = { qGstin = it.uppercase().take(15) },
                            label = { Text("GSTIN (Optional)") },
                            singleLine = true,
                            colors = vanijyaTextFieldColors()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (qName.isNotBlank()) {
                                val now = System.currentTimeMillis()
                                val companyId = activeCompany?.id ?: "local"
                                val newPartyId = UUID.randomUUID().toString()
                                val stateCode = if (qGstin.length >= 2) qGstin.take(2) else (activeCompany?.stateCode ?: "27")
                                val stateName = Validators.indianStateMap[stateCode] ?: "Maharashtra"
                                val newParty = PartyEntity(
                                    id = newPartyId,
                                    companyId = companyId,
                                    name = qName.trim(),
                                    partyType = if (isPurchaseVariant(selectedDocType)) "SUPPLIER" else "CUSTOMER",
                                    phone = qPhone,
                                    gstin = qGstin,
                                    pan = Validators.extractPanFromGstin(qGstin) ?: "",
                                    stateCode = stateCode,
                                    stateName = stateName,
                                    groupId = "${companyId}_grp_retail",
                                    groupName = "Retail Customers",
                                    createdAt = now,
                                    updatedAt = now
                                )
                                onQuickAddParty(newParty, emptyList(), emptyList())
                                selectedPartyId = newPartyId
                                customPartyName = newParty.name
                                customPartyGstin = newParty.gstin
                                placeOfSupplyStateCode = stateCode
                                hasUnsavedChanges = true
                                showQuickAddPartyDialog = false
                            }
                        },
                        modifier = Modifier.testTag("quick_add_party_save_btn")
                    ) {
                        Text("Save & Select", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showQuickAddPartyDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    // ============================================================================
    // INLINE SUB-SCREEN 1 & 2: DOCUMENT DETAIL (Level 1, using BillSummaryCard) & HSN/AUDIT TRAIL (Level 2)
    // ============================================================================
    val detailTxn = remember(selectedDetailTxnId, effectiveTxns) {
        effectiveTxns.firstOrNull { it.id == selectedDetailTxnId }
    }
    if (detailTxn != null) {
        val docLines = txnLines.filter { it.txnId == detailTxn.id }
        val docCharges = txnCharges.filter { it.txnId == detailTxn.id }
        val docTaxes = txnTaxLines.filter { it.txnId == detailTxn.id }
        val docLinks = txnLinks.filter { it.sourceTxnId == detailTxn.id || it.targetTxnId == detailTxn.id }
        val nextConversion = SalesDocTypes.nextConversionTarget(detailTxn.docType)
        var showSharePreviewSheet by remember { mutableStateOf(false) }

        val detailSupplyType = remember(detailTxn.supplyType) {
            runCatching { GstSupplyType.valueOf(detailTxn.supplyType) }.getOrDefault(GstSupplyType.INTRA_STATE)
        }
        val detailSlabBreakup = remember(docTaxes, detailTxn) {
            if (docTaxes.isNotEmpty()) {
                docTaxes.groupBy { it.taxRateBps }.map { (rateBps, rows) ->
                    TaxSlabBreakupRow(
                        taxRateBps = rateBps,
                        taxablePaise = rows.sumOf { it.taxablePaise },
                        cgstPaise = rows.sumOf { it.cgstPaise },
                        sgstPaise = rows.sumOf { it.sgstPaise },
                        igstPaise = rows.sumOf { it.igstPaise },
                        cessPaise = rows.sumOf { it.cessPaise }
                    )
                }.sortedBy { it.taxRateBps }
            } else {
                listOf(
                    TaxSlabBreakupRow(
                        taxRateBps = 500,
                        taxablePaise = detailTxn.taxableSubtotalPaise,
                        cgstPaise = detailTxn.cgstTotalPaise,
                        sgstPaise = detailTxn.sgstTotalPaise,
                        igstPaise = detailTxn.igstTotalPaise,
                        cessPaise = detailTxn.cessTotalPaise
                    )
                )
            }
        }

        if (showInvoiceAuditSubScreen) {
            // Level 2 Deep Screen: HSN Tax Audit & Conversion Chain
            GlassSubScreen(
                onDismissRequest = { showInvoiceAuditSubScreen = false },
                title = "HSN & Conversion Audit • ${detailTxn.docNumber}",
                subtitle = "${detailTxn.partyName} • Supply: ${detailTxn.supplyType} • Classification: ${detailTxn.classification}",
                screenTestTag = "invoice_audit_sub_screen",
                backButtonTestTag = "invoice_audit_back_btn"
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    accentTint = VioletPrimary,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Text(
                        text = "Statutory GST & Place of Supply Audit",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "POS: ${detailTxn.placeOfSupplyStateCode}-${detailTxn.placeOfSupplyStateName} • Reverse Charge: ${if (detailTxn.isReverseCharge) "Yes" else "No"} • Rounding: ${detailTxn.roundingMode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Taxable Value: ${Money(detailTxn.taxableSubtotalPaise).formatIndian()} • Total GST: ${Money(detailTxn.totalTaxPaise).formatIndian()}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = EmeraldPrimary
                    )
                }

                SectionHeader(title = "HSN / SAC Summary Table")
                if (docTaxes.isNotEmpty()) {
                    docTaxes.forEach { tl ->
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 16.dp,
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Text(
                                text = "HSN ${tl.hsnSacCode} (${tl.taxRateBps / 100.0}%)",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Taxable ${Money(tl.taxablePaise).formatIndian()} | CGST ${Money(tl.cgstPaise).formatIndian()} | SGST ${Money(tl.sgstPaise).formatIndian()} | IGST ${Money(tl.igstPaise).formatIndian()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp,
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Text(
                            text = "HSN 1006 • Taxable: ${Money(detailTxn.taxableSubtotalPaise).formatIndian()} • CGST: ${Money(detailTxn.cgstTotalPaise).formatIndian()} • SGST: ${Money(detailTxn.sgstTotalPaise).formatIndian()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                SectionHeader(title = "Document Conversion Chain (txn_link)")
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    accentTint = GoldPrimary,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    if (docLinks.isNotEmpty()) {
                        docLinks.forEach { link ->
                            Text(
                                text = "• ${link.sourceDocNumber} (${link.sourceDocType}) → ${link.targetDocNumber} (${link.targetDocType}) [${link.relationship}]",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Text(
                            text = "Primary origin voucher (${detailTxn.docNumber}). Ready for downstream conversion or settlement.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Level 1 Deep Screen: Invoice Detail (Reuses BillSummaryCard)
            GlassSubScreen(
                onDismissRequest = { selectedDetailTxnId = null },
                title = "${detailTxn.docNumber} • ${SalesDocTypes.displayLabel(detailTxn.docType)}",
                subtitle = "${detailTxn.partyName} • ${detailTxn.classification} (${detailTxn.supplyType}) • Status: ${detailTxn.status}",
                screenTestTag = "invoice_detail_screen",
                backButtonTestTag = "invoice_detail_back_btn"
            ) {
                // Button at top of detail screen to open Level 2 Deep Screen (HSN & Conversion Audit)
                VanijyaButton(
                    text = "HSN & Conversion Audit Trail",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    isOutlined = true,
                    onClick = { showInvoiceAuditSubScreen = true },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "invoice_detail_open_audit_btn"
                )

                if (detailTxn.status == "Cancelled" && detailTxn.cancelReason.isNotBlank()) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp,
                        accentTint = CoralError,
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Text(
                            text = "Cancelled (Number Preserved): ${detailTxn.cancelReason}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = CoralError
                        )
                    }
                }

                // Conversion Trace (`txn_link`)
                if (docLinks.isNotEmpty()) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 18.dp,
                        accentTint = GoldPrimary,
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Text(
                            text = "Document Conversion Chain (txn_link)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = GoldPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        docLinks.forEach { link ->
                            Text(
                                text = "• ${link.sourceDocNumber} (${link.sourceDocType}) → ${link.targetDocNumber} (${link.targetDocType}) [${link.relationship}]",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Line Items
                if (docLines.isNotEmpty()) {
                    SectionCard(
                        sectionTitle = "Items (${docLines.size})",
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        docLines.forEachIndexed { idx, ln ->
                            if (idx > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = glass.subtleDivider)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ln.itemName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "HSN ${ln.hsnSacCode} | ${ln.qty}${if (ln.freeQty > 0L) "+${ln.freeQty}F" else ""} ${ln.unit} × ${Money(ln.unitPricePaise).formatIndian()} | GST ${formatGstSlabRate(ln.taxRateBps)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 12.sp,
                                            fontFeatureSettings = "tnum"
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                AmountText(
                                    amount = Money(ln.lineTotalPaise),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    masked = hideAmounts,
                                    decimals = decimals
                                )
                            }
                        }
                    }
                }

                // Reusable BillSummaryCard on Invoice Detail Screen
                BillSummaryCard(
                    grossSubtotalPaise = detailTxn.grossSubtotalPaise.takeIf { it > 0L } ?: detailTxn.taxableSubtotalPaise,
                    totalDiscountPaise = detailTxn.lineDiscountTotalPaise + detailTxn.invoiceDiscountPaise,
                    taxableAmountPaise = detailTxn.taxableSubtotalPaise,
                    cgstTotalPaise = detailTxn.cgstTotalPaise,
                    sgstTotalPaise = detailTxn.sgstTotalPaise,
                    igstTotalPaise = detailTxn.igstTotalPaise,
                    cessTotalPaise = detailTxn.cessTotalPaise,
                    otherChargesPaise = detailTxn.additionalChargesTotalPaise,
                    roundOffPaise = detailTxn.roundOffPaise,
                    grandTotalPaise = detailTxn.grandTotalPaise,
                    amountInWords = detailTxn.amountInWords,
                    supplyType = detailSupplyType,
                    taxSlabBreakup = detailSlabBreakup,
                    isEditable = false,
                    hideAmounts = hideAmounts,
                    decimals = decimals
                )

                // One-Tap Conversion + Share/PDF Preview + Edit Actions
                if (nextConversion != null && detailTxn.status != "Cancelled") {
                    VanijyaButton(
                        text = "One-Tap Convert to ${SalesDocTypes.displayLabel(nextConversion)}",
                        icon = Icons.Default.SwapHoriz,
                        onClick = {
                            onConvertDocument(detailTxn.id, nextConversion)
                            selectedDetailTxnId = null
                            activeBanner = "✓ Converted ${detailTxn.docNumber} to ${SalesDocTypes.displayLabel(nextConversion)} with txn_link trace"
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VanijyaButton(
                        text = "Preview & Share PDF",
                        icon = Icons.Default.PictureAsPdf,
                        onClick = { showSharePreviewSheet = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("invoice_detail_share_preview_btn")
                    )
                    if (detailTxn.status != "Cancelled") {
                        VanijyaButton(
                            text = "Edit",
                            icon = Icons.Default.Edit,
                            isOutlined = true,
                            onClick = {
                                val idToEdit = detailTxn.id
                                val dt = detailTxn.docType
                                selectedDetailTxnId = null
                                editingTxnId = idToEdit
                                builderInitialDocType = dt
                                showDocBuilderSheet = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Share & PDF Preview Sheet (Reuses BillSummaryCard)
        if (showSharePreviewSheet) {
            GlassSheet(
                onDismissRequest = { showSharePreviewSheet = false },
                title = "PDF & Share Preview • ${detailTxn.docNumber}",
                subtitle = "${detailTxn.partyName} • ${SalesDocTypes.displayLabel(detailTxn.docType)}"
            ) {
                BillSummaryCard(
                    grossSubtotalPaise = detailTxn.grossSubtotalPaise.takeIf { it > 0L } ?: detailTxn.taxableSubtotalPaise,
                    totalDiscountPaise = detailTxn.lineDiscountTotalPaise + detailTxn.invoiceDiscountPaise,
                    taxableAmountPaise = detailTxn.taxableSubtotalPaise,
                    cgstTotalPaise = detailTxn.cgstTotalPaise,
                    sgstTotalPaise = detailTxn.sgstTotalPaise,
                    igstTotalPaise = detailTxn.igstTotalPaise,
                    cessTotalPaise = detailTxn.cessTotalPaise,
                    otherChargesPaise = detailTxn.additionalChargesTotalPaise,
                    roundOffPaise = detailTxn.roundOffPaise,
                    grandTotalPaise = detailTxn.grandTotalPaise,
                    amountInWords = detailTxn.amountInWords,
                    supplyType = detailSupplyType,
                    taxSlabBreakup = detailSlabBreakup,
                    isEditable = false,
                    hideAmounts = hideAmounts,
                    decimals = decimals
                )
                VanijyaButton(
                    text = "Generate & Share A4 PDF",
                    icon = Icons.Default.Share,
                    onClick = {
                        Step3APdfAndCsvExporter.exportAndShareSalesVoucherPdf(
                            context = context,
                            company = activeCompany,
                            txn = detailTxn,
                            lines = docLines,
                            charges = docCharges,
                            taxLines = docTaxes
                        )
                        showSharePreviewSheet = false
                        selectedDetailTxnId = null
                        activeBanner = "✓ Generated & shared A4 PDF for ${detailTxn.docNumber}"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_share_pdf_btn")
                )
            }
        }
    }

    // ============================================================================
    // DIALOG 3: CANCEL DOCUMENT WITH MANDATORY REASON (Keeps Document Number)
    // ============================================================================
    if (cancellingTxnId != null) {
        val targetTxn = effectiveTxns.firstOrNull { it.id == cancellingTxnId }
        var cancelReason by remember { mutableStateOf("Customer order cancelled / duplicate entry") }
        AlertDialog(
            onDismissRequest = { cancellingTxnId = null },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Text(
                    text = "Cancel ${targetTxn?.docNumber ?: "Document"}?",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Cancelling keeps ${targetTxn?.docNumber} in the gap-free register, reverses stock and party ledger in one @Transaction, and records the reason in audit_log.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Cancellation Reason") },
                        singleLine = true,
                        colors = vanijyaTextFieldColors()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = cancellingTxnId!!
                        onCancelDocument(id, cancelReason)
                        cancellingTxnId = null
                        activeBanner = "✓ Cancelled ${targetTxn?.docNumber ?: ""} (Stock & Khata reversed)"
                    }
                ) {
                    Text("Confirm Cancel", color = CoralError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { cancellingTxnId = null }) {
                    Text("Keep Active")
                }
            }
        )
    }

    if (deletingTxnId != null) {
        val targetTxn = effectiveTxns.firstOrNull { it.id == deletingTxnId }
        AlertDialog(
            onDismissRequest = { deletingTxnId = null },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Text(
                    text = "Delete ${targetTxn?.docNumber ?: "Document"}?",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "This will remove ${targetTxn?.docNumber ?: "this document"} from active billing lists and reverse any stock or party ledger impact.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = deletingTxnId!!
                        localFallbackDocs.removeAll { it.id == id }
                        onCancelDocument(id, "Deleted by user")
                        deletingTxnId = null
                        activeBanner = "✓ Deleted ${targetTxn?.docNumber ?: ""}"
                    }
                ) {
                    Text("Delete", color = CoralError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingTxnId = null }) {
                    Text("Keep")
                }
            }
        )
    }

    // ============================================================================
    // SHEET 4: PAYMENT-IN BUILDER (Multi-mode, Bill-to-Bill Settlement, Cheque & Advance)
    // ============================================================================
    if (showPaymentBuilderSheet) {
        val activeParties = remember(parties) { parties.filter { !it.isDeleted } }
        var selectedPartyId by remember { mutableStateOf(activeParties.firstOrNull()?.id ?: "") }
        val selectedParty = activeParties.firstOrNull { it.id == selectedPartyId }

        val openPartyInvoices = remember(selectedPartyId, effectiveTxns) {
            effectiveTxns.filter {
                !it.isDeleted &&
                    it.partyId == selectedPartyId &&
                    it.balanceAmountPaise > 0L &&
                    it.status != "Cancelled" &&
                    SalesDocTypes.affectsPartyLedger(it.docType)
            }
        }

        val selectedInvoiceIds = remember(selectedPartyId) {
            mutableStateMapOf<String, Boolean>().apply {
                openPartyInvoices.forEach { put(it.id, true) }
            }
        }

        val defaultAllocatedRupees = openPartyInvoices
            .filter { selectedInvoiceIds[it.id] == true }
            .sumOf { it.balanceAmountPaise } / 100L

        var amountRupeesText by remember(selectedPartyId, defaultAllocatedRupees) {
            mutableStateOf(if (defaultAllocatedRupees > 0L) defaultAllocatedRupees.toString() else "10000")
        }
        var discountRupeesText by remember { mutableStateOf("") }
        var paymentMode by remember { mutableStateOf("UPI") }
        var splitSecondMode by remember { mutableStateOf("CASH") }
        var referenceNo by remember { mutableStateOf("") }
        var chequeNumber by remember { mutableStateOf("") }
        var chequeBank by remember { mutableStateOf("HDFC Bank") }

        GlassSheet(
            onDismissRequest = { showPaymentBuilderSheet = false },
            title = "Record Payment-In (Receipt)",
            subtitle = "Multi-mode receipt with bill-to-bill invoice allocation & automatic advance balance"
        ) {
            if (activeParties.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    activeParties.forEach { party ->
                        FilterChip(
                            selected = selectedPartyId == party.id,
                            onClick = { selectedPartyId = party.id },
                            label = { Text(party.name) },
                            colors = vanijyaFilterChipColors()
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amountRupeesText,
                    onValueChange = { amountRupeesText = it.filter(Char::isDigit).take(9) },
                    label = { Text("Amount Received (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = discountRupeesText,
                    onValueChange = { discountRupeesText = it.filter(Char::isDigit).take(7) },
                    label = { Text("Settlement Disc (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Payment Mode (Supports Split Modes)",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("UPI", "CASH", "BANK_NEFT", "CHEQUE", "SPLIT").forEach { mode ->
                    FilterChip(
                        selected = paymentMode == mode,
                        onClick = { paymentMode = mode },
                        label = { Text(mode) },
                        colors = vanijyaFilterChipColors()
                    )
                }
            }

            if (paymentMode == "CHEQUE") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = chequeNumber,
                        onValueChange = { chequeNumber = it.filter(Char::isDigit).take(8) },
                        label = { Text("Cheque No (6 digits)") },
                        singleLine = true,
                        colors = vanijyaTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = chequeBank,
                        onValueChange = { chequeBank = it },
                        label = { Text("Drawee Bank") },
                        singleLine = true,
                        colors = vanijyaTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                OutlinedTextField(
                    value = referenceNo,
                    onValueChange = { referenceNo = it },
                    label = { Text("UTR / UPI Ref / Split Note") },
                    singleLine = true,
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Bill-to-Bill Invoice Settlement List
            if (openPartyInvoices.isNotEmpty()) {
                Text(
                    text = "Bill-to-Bill Invoice Settlement (${openPartyInvoices.size} Open Bills)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldPrimary
                )
                openPartyInvoices.forEach { inv ->
                    val checked = selectedInvoiceIds[inv.id] == true
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedInvoiceIds[inv.id] = !checked }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { selectedInvoiceIds[inv.id] = it }
                            )
                            Column {
                                Text(
                                    text = inv.docNumber,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Total: ${Money(inv.grandTotalPaise).formatIndian()} • Due: ${Money(inv.balanceAmountPaise).formatIndian()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        AmountText(
                            amount = Money(inv.balanceAmountPaise),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = AmberSale,
                            masked = hideAmounts,
                            decimals = decimals
                        )
                    }
                }
            }

            VanijyaButton(
                text = "Save Payment-In & Settle Selected Bills",
                onClick = {
                    val totalPaise = (amountRupeesText.toLongOrNull() ?: 0L) * 100L
                    val discPaise = (discountRupeesText.toLongOrNull() ?: 0L) * 100L
                    if (totalPaise > 0L) {
                        val now = System.currentTimeMillis()
                        val companyId = activeCompany?.id ?: "local"
                        val payId = UUID.randomUUID().toString()
                        val chqId = if (paymentMode == "CHEQUE") UUID.randomUUID().toString() else ""

                        var remainingPool = totalPaise + discPaise
                        val allocations = mutableListOf<PaymentAllocationEntity>()
                        var discountRemaining = discPaise

                        openPartyInvoices.filter { selectedInvoiceIds[it.id] == true }.forEach { inv ->
                            if (remainingPool > 0L) {
                                val settle = minOf(remainingPool, inv.balanceAmountPaise)
                                val discOnThis = minOf(discountRemaining, settle)
                                val cashOnThis = settle - discOnThis
                                allocations.add(
                                    PaymentAllocationEntity(
                                        id = UUID.randomUUID().toString(),
                                        companyId = companyId,
                                        paymentId = payId,
                                        txnId = inv.id,
                                        txnDocNumber = inv.docNumber,
                                        allocatedAmountPaise = cashOnThis,
                                        discountAllowedPaise = discOnThis,
                                        createdAt = now,
                                        updatedAt = now
                                    )
                                )
                                remainingPool -= settle
                                discountRemaining -= discOnThis
                            }
                        }

                        val advancePaise = remainingPool.coerceAtLeast(0L)
                        val modesJson = if (paymentMode == "SPLIT") {
                            "UPI:${totalPaise / 2}|$splitSecondMode:${totalPaise - totalPaise / 2}"
                        } else {
                            "$paymentMode:$totalPaise"
                        }

                        val payment = PaymentEntity(
                            id = payId,
                            companyId = companyId,
                            paymentNumber = "",
                            paymentType = "PAYMENT_IN",
                            paymentDate = now,
                            partyId = selectedParty?.id ?: "",
                            partyName = selectedParty?.name ?: "Walk-in Party",
                            totalAmountPaise = totalPaise,
                            discountPaise = discPaise,
                            unallocatedAdvancePaise = advancePaise,
                            primaryMode = paymentMode,
                            modesSplitJson = modesJson,
                            referenceNo = referenceNo.ifBlank { chequeNumber },
                            chequeId = chqId,
                            status = "Paid",
                            notes = if (advancePaise > 0L) "Includes ${Money(advancePaise).formatIndian()} advance" else "Bill-to-bill settlement",
                            createdAt = now,
                            updatedAt = now
                        )

                        val chequeEntity = if (paymentMode == "CHEQUE") {
                            ChequeEntity(
                                id = chqId,
                                companyId = companyId,
                                paymentId = payId,
                                partyId = selectedParty?.id ?: "",
                                partyName = selectedParty?.name ?: "Walk-in Party",
                                chequeType = "RECEIVABLE",
                                chequeNumber = chequeNumber.ifBlank { "901244" },
                                bankName = chequeBank.ifBlank { "HDFC Bank" },
                                chequeDate = now,
                                dueDate = now + 2 * 86_400_000L,
                                amountPaise = totalPaise,
                                status = "IN_HAND",
                                createdAt = now,
                                updatedAt = now
                            )
                        } else null

                        onRecordPayment(payment, allocations, chequeEntity)
                        showPaymentBuilderSheet = false
                        selectedSegment = 5
                        activeBanner = "✓ Recorded Payment-In (${Money(totalPaise).formatIndian()}) • Settled ${allocations.size} bill(s)"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // ============================================================================
    // SHEET 5: CHEQUE REGISTER & BOUNCED-CHEQUE HANDLING
    // ============================================================================
    if (showChequeRegisterSheet) {
        var bouncePenaltyRupees by remember { mutableStateOf("250") }
        GlassSheet(
            onDismissRequest = { showChequeRegisterSheet = false },
            title = "Cheque Register & Bounce Handling",
            subtitle = "Track PDC / In-Hand cheques, deposit, clear, or mark bounced (reverses invoice & debits party)"
        ) {
            OutlinedTextField(
                value = bouncePenaltyRupees,
                onValueChange = { bouncePenaltyRupees = it.filter(Char::isDigit).take(5) },
                label = { Text("Default Cheque Bounce Bank Charge (₹)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = vanijyaTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            if (cheques.isEmpty()) {
                Text(
                    text = "No cheques recorded yet. Record a Payment-In with mode CHEQUE to track it here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                cheques.filter { !it.isDeleted }.forEach { chq ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 18.dp,
                        accentTint = when (chq.status) {
                            "BOUNCED" -> CoralError
                            "CLEARED" -> EmeraldPrimary
                            else -> PurchaseBlue
                        },
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Chq #${chq.chequeNumber} • ${chq.bankName}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${chq.partyName} • Due: ${dateFmt.format(Date(chq.dueDate))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (chq.status == "BOUNCED") {
                                    Text(
                                        text = "BOUNCED: ${chq.bounceReason} (+${Money(chq.bounceChargesPaise).formatIndian()} penalty)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = CoralError
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                AmountText(
                                    amount = Money(chq.amountPaise),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (chq.status == "BOUNCED") CoralError else EmeraldPrimary,
                                    masked = hideAmounts,
                                    decimals = decimals
                                )
                                Text(
                                    text = chq.status,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = when (chq.status) {
                                        "BOUNCED" -> CoralError
                                        "CLEARED" -> EmeraldPrimary
                                        else -> GoldPrimary
                                    }
                                )
                            }
                        }
                        if (chq.status != "BOUNCED" && chq.status != "CLEARED") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (chq.status == "IN_HAND") {
                                    Surface(
                                        color = PurchaseBlue.copy(alpha = 0.16f),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                onUpdateChequeStatus(chq.id, "DEPOSITED", 0L, "")
                                                activeBanner = "✓ Marked Cheque #${chq.chequeNumber} as DEPOSITED"
                                            }
                                    ) {
                                        Text(
                                            text = "Mark Deposited",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = PurchaseBlue,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                                Surface(
                                    color = EmeraldPrimary.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            onUpdateChequeStatus(chq.id, "CLEARED", 0L, "")
                                            activeBanner = "✓ Marked Cheque #${chq.chequeNumber} as CLEARED"
                                        }
                                ) {
                                    Text(
                                        text = "Mark Cleared",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = EmeraldPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                                Surface(
                                    color = CoralError.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            val penaltyPaise = (bouncePenaltyRupees.toLongOrNull() ?: 250L) * 100L
                                            onUpdateChequeStatus(chq.id, "BOUNCED", penaltyPaise, "Insufficient Funds")
                                            activeBanner = "✓ Cheque #${chq.chequeNumber} marked BOUNCED — Invoice settlement reversed & party debited"
                                        }
                                        .testTag("bounce_cheque_btn_${chq.chequeNumber}")
                                ) {
                                    Text(
                                        text = "Mark Bounced",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = CoralError,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ============================================================================
    // SHEET 6: RECURRING INVOICES SCHEDULER & LOCAL NOTIFICATION RUNNER
    // ============================================================================
    if (showRecurringSheet) {
        var newRecTitle by remember { mutableStateOf("") }
        var newRecAmountRupees by remember { mutableStateOf("4500") }
        var newRecFrequency by remember { mutableStateOf("MONTHLY") }

        GlassSheet(
            onDismissRequest = { showRecurringSheet = false },
            title = "Recurring Invoices & Scheduler",
            subtitle = "Auto-generate standing GST invoices & trigger local Android notifications offline"
        ) {
            recurringTemplates.filter { !it.isDeleted }.forEach { tpl ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    accentTint = VioletPrimary,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tpl.templateTitle,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${tpl.partyName} • ${tpl.frequency} • Next: ${dateFmt.format(Date(tpl.nextRunDate))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            AmountText(
                                amount = Money(tpl.estimatedGrandTotalPaise),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldPrimary,
                                masked = hideAmounts,
                                decimals = decimals
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = EmeraldPrimary.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        onRunRecurringNow(tpl.id)
                                        showRecurringSheet = false
                                        activeBanner = "✓ Generated Recurring Invoice for '${tpl.templateTitle}' & posted notification"
                                    }
                                    .testTag("run_recurring_btn_${tpl.id}")
                            ) {
                                Text(
                                    text = "Run Now",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = glass.subtleDivider)
            Text(
                text = "Add Standing Recurring Schedule",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newRecTitle,
                    onValueChange = { newRecTitle = it },
                    label = { Text("Schedule Title") },
                    singleLine = true,
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(1.2f)
                )
                OutlinedTextField(
                    value = newRecAmountRupees,
                    onValueChange = { newRecAmountRupees = it.filter(Char::isDigit).take(7) },
                    label = { Text("Base Rate ₹") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(0.8f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("WEEKLY", "MONTHLY", "QUARTERLY", "YEARLY").forEach { freq ->
                    FilterChip(
                        selected = newRecFrequency == freq,
                        onClick = { newRecFrequency = freq },
                        label = { Text(freq) },
                        colors = vanijyaFilterChipColors()
                    )
                }
            }
            VanijyaAddButton(
                text = "Save Recurring Schedule",
                icon = Icons.Default.Add,
                isOutlined = false,
                onClick = {
                    if (newRecTitle.isNotBlank()) {
                        val now = System.currentTimeMillis()
                        val companyId = activeCompany?.id ?: "local"
                        val firstParty = parties.firstOrNull { !it.isDeleted }
                        val ratePaise = (newRecAmountRupees.toLongOrNull() ?: 4500L) * 100L
                        val estTotal = ratePaise + (ratePaise * 1800L) / 10_000L
                        val days = when (newRecFrequency) {
                            "WEEKLY" -> 7
                            "QUARTERLY" -> 90
                            "YEARLY" -> 365
                            else -> 30
                        }
                        onSaveRecurringTemplate(
                            RecurringTemplateEntity(
                                id = UUID.randomUUID().toString(),
                                companyId = companyId,
                                templateTitle = newRecTitle.trim(),
                                docType = SalesDocTypes.TAX_INVOICE,
                                partyId = firstParty?.id ?: "",
                                partyName = firstParty?.name ?: "Mehta Kirana & Traders",
                                partyGstin = firstParty?.gstin ?: "27AAPFM3456D1Z9",
                                placeOfSupplyStateCode = firstParty?.stateCode ?: "27",
                                placeOfSupplyStateName = firstParty?.stateName ?: "Maharashtra",
                                frequency = newRecFrequency,
                                intervalDays = days,
                                nextRunDate = now + days * 86_400_000L,
                                defaultItemName = newRecTitle.trim(),
                                defaultHsnCode = "9983",
                                defaultQty = 1L,
                                defaultUnit = "PCS",
                                defaultRatePaise = ratePaise,
                                defaultTaxRateBps = 1800,
                                estimatedGrandTotalPaise = estTotal,
                                autoNotify = true,
                                isActive = true,
                                createdAt = now,
                                updatedAt = now
                            )
                        )
                        newRecTitle = ""
                        activeBanner = "✓ Saved recurring invoice schedule"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
