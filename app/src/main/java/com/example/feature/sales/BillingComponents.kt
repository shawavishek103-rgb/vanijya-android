@file:OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)

package com.example.feature.sales

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.ItemBatchEntity
import com.example.core.database.ItemEntity
import com.example.core.database.PartyEntity
import com.example.core.database.TaxRateEntity
import com.example.core.database.TxnEntity
import com.example.core.designsystem.AmountText
import com.example.core.designsystem.GlassSheet
import com.example.core.designsystem.VanijyaAddButton
import com.example.core.designsystem.VanijyaButton
import com.example.core.designsystem.vanijyaFilterChipColors
import com.example.core.designsystem.vanijyaTextFieldColors
import com.example.core.model.CreditLimitCheckResult
import com.example.core.model.GstCalculatedLine
import com.example.core.model.GstCalculationResult
import com.example.core.model.GstSupplyType
import com.example.core.model.GstTaxCategory
import com.example.core.model.Money
import com.example.core.model.SalesDocTypes
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
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Line item draft model for the unified Vanijya Billing Builder.
 * All currency values are stored as integer paise (Long) and rates as basis points (Int).
 */
data class BillingDraftLineItem(
    val id: String = UUID.randomUUID().toString(),
    val itemId: String,
    val itemName: String,
    val itemCode: String,
    val hsnSacCode: String,
    val unit: String,
    val qty: Long,
    val freeQty: Long = 0L,
    val unitPricePaise: Long,
    val purchaseCostPaise: Long,
    val discountPercent: Int = 0,
    val discountAmountPaise: Long = 0L,
    val isDiscountInRupees: Boolean = false,
    val taxRateBps: Int = 1800,
    val cessBps: Int = 0,
    val taxInclusive: Boolean = false,
    val taxCategory: GstTaxCategory = GstTaxCategory.TAXABLE,
    val batchId: String = "",
    val batchNumber: String = "",
    val serialNumbersCsv: String = ""
) {
    /**
     * Effective discount basis points (100 bps = 1%) for GstCalculationEngine.
     */
    fun effectiveDiscountBps(): Int {
        if (!isDiscountInRupees) return (discountPercent * 100).coerceIn(0, 10_000)
        val grossPaise = (qty * unitPricePaise).coerceAtLeast(1L)
        return ((discountAmountPaise.toDouble() / grossPaise.toDouble()) * 10_000.0)
            .roundToLong()
            .toInt()
            .coerceIn(0, 10_000)
    }
}

/**
 * Tax rate slab group for the expandable "Tax breakup" in BillSummaryCard.
 */
data class TaxSlabBreakupRow(
    val taxRateBps: Int,
    val taxablePaise: Long,
    val cgstPaise: Long,
    val sgstPaise: Long,
    val igstPaise: Long,
    val cessPaise: Long
)

/**
 * Helper to format GST rate without trailing ".0" when whole (e.g., 1800 bps -> "18%", 250 bps -> "2.5%").
 */
fun formatGstSlabRate(bps: Int): String {
    val whole = bps / 100
    val rem = bps % 100
    return if (rem == 0) "$whole%" else "${bps / 100.0}%"
}

/**
 * Helper to format screen title ("New Sale", "New Purchase", "New Quotation", etc.) by document type.
 */
fun billingScreenTitle(docType: String, editingDocNumber: String? = null): String {
    if (!editingDocNumber.isNullOrBlank()) return "Edit $editingDocNumber"
    return when (docType) {
        SalesDocTypes.TAX_INVOICE -> "New Sale"
        SalesDocTypes.PURCHASE_BILL -> "New Purchase"
        SalesDocTypes.ESTIMATE, SalesDocTypes.PROFORMA -> "New Quotation"
        SalesDocTypes.SALE_ORDER -> "New Sale Order"
        SalesDocTypes.PURCHASE_ORDER -> "New Purchase Order"
        SalesDocTypes.DELIVERY_CHALLAN -> "New Delivery Challan"
        SalesDocTypes.CREDIT_NOTE -> "New Sale Return"
        SalesDocTypes.DEBIT_NOTE -> "New Purchase Return"
        SalesDocTypes.CASH_SALE -> "New Cash Sale"
        SalesDocTypes.BILL_OF_SUPPLY -> "New Bill of Supply"
        else -> "New ${SalesDocTypes.displayLabel(docType)}"
    }
}

/**
 * Helper to format primary sticky save button CTA label with live Grand Total.
 */
fun stickySaveButtonLabel(docType: String, formattedTotal: String): String = when (docType) {
    SalesDocTypes.TAX_INVOICE, SalesDocTypes.CASH_SALE, SalesDocTypes.BILL_OF_SUPPLY ->
        "Save & Generate Invoice ($formattedTotal)"
    SalesDocTypes.PURCHASE_BILL ->
        "Save Purchase Bill ($formattedTotal)"
    SalesDocTypes.ESTIMATE, SalesDocTypes.PROFORMA ->
        "Save Quotation ($formattedTotal)"
    SalesDocTypes.SALE_ORDER ->
        "Save Sale Order ($formattedTotal)"
    SalesDocTypes.PURCHASE_ORDER ->
        "Save Purchase Order ($formattedTotal)"
    SalesDocTypes.DELIVERY_CHALLAN ->
        "Save Delivery Challan ($formattedTotal)"
    SalesDocTypes.CREDIT_NOTE ->
        "Save Sale Return ($formattedTotal)"
    SalesDocTypes.DEBIT_NOTE ->
        "Save Purchase Return ($formattedTotal)"
    else ->
        "Save Voucher ($formattedTotal)"
}

fun isPurchaseVariant(docType: String): Boolean =
    docType in setOf(SalesDocTypes.PURCHASE_BILL, SalesDocTypes.PURCHASE_ORDER, SalesDocTypes.DEBIT_NOTE)

fun isReturnVariant(docType: String): Boolean =
    docType in setOf(SalesDocTypes.CREDIT_NOTE, SalesDocTypes.DEBIT_NOTE)

fun shouldShowPaymentCard(docType: String): Boolean =
    docType !in setOf(
        SalesDocTypes.ESTIMATE,
        SalesDocTypes.PROFORMA,
        SalesDocTypes.SALE_ORDER,
        SalesDocTypes.PURCHASE_ORDER,
        SalesDocTypes.DELIVERY_CHALLAN
    )

// ============================================================================
// 1. REUSABLE COMPONENT: SectionCard (24dp radius, ~88% opaque fill, 12sp uppercase title)
// ============================================================================
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    sectionTitle: String? = null,
    cornerRadius: Dp = 24.dp,
    accentBorderColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    headerTrailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val glass = LocalVanijyaGlass.current
    val shape = RoundedCornerShape(cornerRadius)
    val cardFill = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
    val borderColor = accentBorderColor?.copy(alpha = 0.45f) ?: glass.borderGradientStart

    Surface(
        color = cardFill,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = shape,
        tonalElevation = 4.dp,
        shadowElevation = 2.dp,
        modifier = modifier
            .clip(shape)
            .border(width = 1.dp, color = borderColor, shape = shape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
        ) {
            if (!sectionTitle.isNullOrBlank() || headerTrailing != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!sectionTitle.isNullOrBlank()) {
                        Text(
                            text = sectionTitle.uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    headerTrailing?.invoke()
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
            content()
        }
    }
}

// ============================================================================
// 2. REUSABLE COMPONENT: DocTypeChips (Equal outlined chips, selected filled lavender)
// ============================================================================
@Composable
fun DocTypeChips(
    selectedDocType: String,
    onSelectDocType: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val docTypes = remember {
        listOf(
            SalesDocTypes.TAX_INVOICE to "Sale",
            SalesDocTypes.PURCHASE_BILL to "Purchase",
            SalesDocTypes.ESTIMATE to "Quotation",
            SalesDocTypes.SALE_ORDER to "Sale Order",
            SalesDocTypes.PURCHASE_ORDER to "Purchase Order",
            SalesDocTypes.DELIVERY_CHALLAN to "Challan",
            SalesDocTypes.CREDIT_NOTE to "Sale Return",
            SalesDocTypes.DEBIT_NOTE to "Purchase Return",
            SalesDocTypes.CASH_SALE to "Cash Sale",
            SalesDocTypes.BILL_OF_SUPPLY to "Bill of Supply"
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .testTag("doc_type_chips_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        docTypes.forEach { (docType, shortLabel) ->
            val selected = selectedDocType == docType
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (selected) {
                    VioletPrimary.copy(alpha = 0.22f)
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                },
                border = BorderStroke(
                    width = if (selected) 1.5.dp else 1.dp,
                    color = if (selected) VioletPrimary else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .defaultMinSize(minWidth = 96.dp, minHeight = 48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelectDocType(docType) }
                    .testTag("doc_type_chip_$docType")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = shortLabel,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ============================================================================
// 3. REUSABLE COMPONENT: DocNumberChip (Single chip "INV-0001 [pencil]" + Date + Due/Valid/Delivery)
// ============================================================================
@Composable
fun DocNumberChip(
    docType: String,
    docPrefix: String,
    docSequenceNumber: Int,
    txnDateMillis: Long,
    dueDaysOffset: Int,
    onUpdatePrefixAndSequence: (newPrefix: String, newSeq: Int) -> Unit,
    onUpdateDueDaysOffset: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    var showEditNumberSheet by rememberSaveable { mutableStateOf(false) }
    var showDueDaysSheet by rememberSaveable { mutableStateOf(false) }

    val formattedDocNumber = remember(docPrefix, docSequenceNumber) {
        val cleanPrefix = docPrefix.ifBlank { SalesDocTypes.defaultPrefix(docType) }
        "$cleanPrefix${docSequenceNumber.toString().padStart(4, '0')}"
    }

    val docNoTitle = when (docType) {
        SalesDocTypes.ESTIMATE, SalesDocTypes.PROFORMA -> "Quote No"
        SalesDocTypes.SALE_ORDER, SalesDocTypes.PURCHASE_ORDER -> "Order No"
        SalesDocTypes.DELIVERY_CHALLAN -> "Challan No"
        SalesDocTypes.PURCHASE_BILL -> "Purchase No"
        SalesDocTypes.CREDIT_NOTE, SalesDocTypes.DEBIT_NOTE -> "Note No"
        else -> "Invoice No"
    }

    val dueLabelPrefix = when (docType) {
        SalesDocTypes.ESTIMATE, SalesDocTypes.PROFORMA -> "Valid till"
        SalesDocTypes.SALE_ORDER, SalesDocTypes.PURCHASE_ORDER -> "Delivery"
        else -> "Due"
    }

    SectionCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Document Number Chip with Pencil Icon
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showEditNumberSheet = true }
                    .testTag("doc_number_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column {
                        Text(
                            text = docNoTitle,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formattedDocNumber,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Document Number Series",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // 2. Date Chip with Calendar Icon
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showDueDaysSheet = true }
                    .testTag("doc_date_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Voucher Date",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Column {
                        Text(
                            text = "Date",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dateFmt.format(Date(txnDateMillis)),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 3. Due / Valid Till / Delivery Days Chip
            if (docType != SalesDocTypes.DELIVERY_CHALLAN && docType != SalesDocTypes.CASH_SALE) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showDueDaysSheet = true }
                        .testTag("doc_due_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Column {
                            Text(
                                text = dueLabelPrefix,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (dueDaysOffset == 0) "Same day" else "+$dueDaysOffset days",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFeatureSettings = "tnum"
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Change $dueLabelPrefix days",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Bottom Sheet to Edit Prefix & Next Number with Live Preview
    if (showEditNumberSheet) {
        var prefixDraft by remember(docPrefix) { mutableStateOf(docPrefix) }
        var seqTextDraft by remember(docSequenceNumber) { mutableStateOf(docSequenceNumber.toString()) }
        val previewSeq = seqTextDraft.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val previewFull = "${prefixDraft.ifBlank { SalesDocTypes.defaultPrefix(docType) }}${previewSeq.toString().padStart(4, '0')}"

        GlassSheet(
            onDismissRequest = { showEditNumberSheet = false },
            title = "Edit $docNoTitle Series",
            subtitle = "Gap-free financial year numbering • Preview updates live below"
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("doc_number_preview_banner")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Next Number Preview",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = previewFull,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum"
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = prefixDraft,
                    onValueChange = { prefixDraft = it.uppercase().take(14) },
                    label = { Text("Prefix (e.g. INV-)") },
                    singleLine = true,
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("doc_prefix_sheet_input")
                )
                OutlinedTextField(
                    value = seqTextDraft,
                    onValueChange = { seqTextDraft = it.filter(Char::isDigit).take(6) },
                    label = { Text("Sequence No.") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("doc_seq_sheet_input")
                )
            }

            VanijyaButton(
                text = "Apply $previewFull",
                icon = Icons.Default.CheckCircle,
                onClick = {
                    onUpdatePrefixAndSequence(
                        prefixDraft.ifBlank { SalesDocTypes.defaultPrefix(docType) },
                        previewSeq
                    )
                    showEditNumberSheet = false
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "doc_number_apply_btn"
            )
        }
    }

    // Bottom Sheet to select Due / Valid till / Delivery days
    if (showDueDaysSheet) {
        GlassSheet(
            onDismissRequest = { showDueDaysSheet = false },
            title = "Select $dueLabelPrefix Period",
            subtitle = "Automatically computes due/validity date from voucher date"
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0, 7, 15, 30, 45, 60, 90).forEach { days ->
                    FilterChip(
                        selected = dueDaysOffset == days,
                        onClick = {
                            onUpdateDueDaysOffset(days)
                            showDueDaysSheet = false
                        },
                        label = { Text(if (days == 0) "Same Day (0d)" else "+$days Days") },
                        colors = vanijyaFilterChipColors()
                    )
                }
            }
        }
    }
}

// ============================================================================
// 4. REUSABLE COMPONENT: PartyCard (Search, Recent Chips, + New Party, Selected Details, POS Badge, Variants)
// ============================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PartyCard(
    docType: String,
    parties: List<PartyEntity>,
    selectedPartyId: String,
    partyNameInput: String,
    partyGstinInput: String,
    supplierStateCode: String,
    placeOfSupplyStateCode: String,
    isReverseCharge: Boolean,
    supplyTypeOverride: String,
    isCashSaleToggle: Boolean,
    creditEvaluation: CreditLimitCheckResult?,
    // Variant fields
    supplierBillNo: String,
    supplierBillDateText: String,
    itcEligible: Boolean,
    returnOriginalInvoiceNo: String,
    returnSettlementMode: String, // "ADJUST" or "REFUND"
    challanHidePrices: Boolean,
    recentInvoicesForReturn: List<TxnEntity>,
    hasValidationError: Boolean,
    hideAmounts: Boolean,
    decimals: Int,
    onPartySelected: (PartyEntity) -> Unit,
    onPartyNameChange: (String) -> Unit,
    onPartyGstinChange: (String) -> Unit,
    onPlaceOfSupplyChange: (stateCode: String, supplyOverride: String, rcm: Boolean) -> Unit,
    onToggleCashSale: (Boolean) -> Unit,
    onOpenNewPartyDialog: () -> Unit,
    onSupplierBillNoChange: (String) -> Unit,
    onSupplierBillDateChange: (String) -> Unit,
    onItcEligibleChange: (Boolean) -> Unit,
    onReturnOriginalInvoiceChange: (String) -> Unit,
    onReturnSettlementModeChange: (String) -> Unit,
    onChallanHidePricesChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isPurchase = isPurchaseVariant(docType)
    val isReturn = isReturnVariant(docType)
    val partyRoleLabel = if (isPurchase) "Supplier" else "Customer"
    val selectedParty = remember(parties, selectedPartyId) {
        parties.firstOrNull { it.id == selectedPartyId }
    }
    var showPosSelectorSheet by rememberSaveable { mutableStateOf(false) }

    val isIntraState = remember(supplierStateCode, placeOfSupplyStateCode, supplyTypeOverride) {
        supplyTypeOverride == "AUTO" && supplierStateCode == placeOfSupplyStateCode
    }
    val posStateName = Validators.indianStateMap[placeOfSupplyStateCode] ?: "Maharashtra"
    val posBadgeLabel = when {
        supplyTypeOverride == "EXPORT_WITHOUT_TAX" -> "Export (0% LUT)"
        supplyTypeOverride == "SEZ_WITH_TAX" -> "SEZ Supply (IGST)"
        isIntraState -> "Intra-state (CGST + SGST)"
        else -> "Inter-state (IGST)"
    }

    val filteredPartySuggestions = remember(parties, partyNameInput) {
        if (partyNameInput.isBlank()) {
            parties.take(6)
        } else {
            parties.filter {
                it.name.contains(partyNameInput, ignoreCase = true) ||
                    it.phone.contains(partyNameInput, ignoreCase = true) ||
                    it.gstin.contains(partyNameInput, ignoreCase = true)
            }.take(6)
        }
    }

    SectionCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("billing_party_card"),
        sectionTitle = "$partyRoleLabel Details",
        accentBorderColor = if (hasValidationError) CoralError else null,
        headerTrailing = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (docType == SalesDocTypes.TAX_INVOICE || docType == SalesDocTypes.CASH_SALE) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onToggleCashSale(!isCashSaleToggle) }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("cash_sale_switch")
                    ) {
                        Text(
                            text = "Cash sale",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = isCashSaleToggle,
                            onCheckedChange = onToggleCashSale,
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
                VanijyaAddButton(
                    text = "New $partyRoleLabel",
                    icon = Icons.Default.PersonAdd,
                    isTextButton = true,
                    onClick = onOpenNewPartyDialog,
                    testTag = "party_card_new_party_btn"
                )
            }
        }
    ) {
        // 1. Search / Select Party Input + Optional GSTIN Field
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = partyNameInput,
                onValueChange = onPartyNameChange,
                label = {
                    Text(
                        text = if (isCashSaleToggle || docType == SalesDocTypes.CASH_SALE) {
                            "Select $partyRoleLabel (Optional for Cash Sale)"
                        } else {
                            "Select $partyRoleLabel *"
                        },
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search $partyRoleLabel"
                    )
                },
                singleLine = true,
                isError = hasValidationError,
                shape = RoundedCornerShape(14.dp),
                colors = vanijyaTextFieldColors(),
                modifier = Modifier
                    .weight(1.25f)
                    .testTag("builder_party_name_input")
            )
            OutlinedTextField(
                value = partyGstinInput,
                onValueChange = onPartyGstinChange,
                label = { Text("GSTIN", fontSize = 12.sp) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = vanijyaTextFieldColors(),
                modifier = Modifier
                    .weight(0.85f)
                    .testTag("builder_party_gstin_input")
            )
        }

        // 2. Recent / Matching Party Chips for 1-Tap Selection
        if (filteredPartySuggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                filteredPartySuggestions.forEach { party ->
                    val isSelected = selectedPartyId == party.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPartySelected(party) },
                        label = {
                            Text(
                                text = party.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 12.sp
                            )
                        },
                        colors = vanijyaFilterChipColors(),
                        modifier = Modifier.testTag("recent_party_chip_${party.id}")
                    )
                }
            }
        }

        // 3. Selected Party Details Box (Phone | GSTIN | State | Outstanding Balance)
        if (selectedParty != null || partyNameInput.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("selected_party_summary_box")
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = buildString {
                                append(partyNameInput.ifBlank { selectedParty?.name ?: "Cash Customer" })
                                val ph = selectedParty?.phone.orEmpty()
                                if (ph.isNotBlank()) append("  |  +91 $ph")
                                val gst = partyGstinInput.ifBlank { selectedParty?.gstin.orEmpty() }
                                if (gst.isNotBlank()) append("  |  GSTIN $gst")
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (selectedParty != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "State: ${selectedParty.stateCode}-${selectedParty.stateName}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (selectedParty.currentBalancePaise >= 0L) "Receivable: " else "Payable: ",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                AmountText(
                                    amount = Money(abs(selectedParty.currentBalancePaise)),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (selectedParty.currentBalancePaise >= 0L) EmeraldPrimary else CoralError,
                                    masked = hideAmounts,
                                    decimals = decimals
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Place of Supply Row with Intra-state (CGST+SGST) / Inter-state (IGST) Badge & [Change]
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Place of supply: $placeOfSupplyStateCode-$posStateName",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = if (isIntraState) EmeraldPrimary.copy(alpha = 0.16f) else PurchaseBlue.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = posBadgeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isIntraState) EmeraldPrimary else PurchaseBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    if (isReverseCharge) {
                        Surface(
                            color = GoldPrimary.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "RCM Enabled",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GoldPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
            TextButton(
                onClick = { showPosSelectorSheet = true },
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("pos_change_btn")
            ) {
                Text(
                    text = "Change",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Credit limit warning banner
        if (creditEvaluation is CreditLimitCheckResult.Exceeded) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = CoralError.copy(alpha = 0.14f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⚠ Credit limit exceeded by ${creditEvaluation.exceededBy.formatIndian()} (Limit: ${creditEvaluation.creditLimit.formatIndian()})",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = CoralError,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        // 5. VARIANT FIELDS:
        // (a) Purchase Variant: Supplier Bill No, Supplier Bill Date, and ITC Eligible Toggle
        if (isPurchase) {
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = supplierBillNo,
                    onValueChange = onSupplierBillNoChange,
                    label = { Text("Supplier Bill No.", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("supplier_bill_no_input")
                )
                OutlinedTextField(
                    value = supplierBillDateText,
                    onValueChange = onSupplierBillDateChange,
                    label = { Text("Supplier Bill Date", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("supplier_bill_date_input")
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onItcEligibleChange(!itcEligible) }
                    .padding(vertical = 4.dp)
                    .testTag("itc_eligible_switch"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ITC Eligible (Input Tax Credit)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (itcEligible) "Eligible for GSTR-3B Input Tax Credit claim" else "Ineligible / Blocked ITC under Sec 17(5)",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = itcEligible,
                    onCheckedChange = onItcEligibleChange
                )
            }
        }

        // (b) Return Variant (Sale Return / Purchase Return): Original Invoice Link & Refund/Adjust Choice
        if (isReturn) {
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = returnOriginalInvoiceNo,
                onValueChange = onReturnOriginalInvoiceChange,
                label = { Text("Original Invoice / Bill No. Link", fontSize = 12.sp) },
                placeholder = { Text("e.g. INV/26-27/0001") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = vanijyaTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("return_orig_invoice_input")
            )
            if (recentInvoicesForReturn.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    recentInvoicesForReturn.take(5).forEach { inv ->
                        FilterChip(
                            selected = returnOriginalInvoiceNo == inv.docNumber,
                            onClick = { onReturnOriginalInvoiceChange(inv.docNumber) },
                            label = { Text("Link ${inv.docNumber}", fontSize = 12.sp) },
                            colors = vanijyaFilterChipColors()
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Return Settlement",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = returnSettlementMode == "ADJUST",
                        onClick = { onReturnSettlementModeChange("ADJUST") },
                        label = { Text("Adjust in Ledger") },
                        colors = vanijyaFilterChipColors(),
                        modifier = Modifier.testTag("return_settlement_mode_chip_adjust")
                    )
                    FilterChip(
                        selected = returnSettlementMode == "REFUND",
                        onClick = { onReturnSettlementModeChange("REFUND") },
                        label = { Text("Immediate Refund") },
                        colors = vanijyaFilterChipColors(),
                        modifier = Modifier.testTag("return_settlement_mode_chip_refund")
                    )
                }
            }
        }

        // (c) Delivery Challan Variant: Hide Prices Toggle
        if (docType == SalesDocTypes.DELIVERY_CHALLAN) {
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onChallanHidePricesChange(!challanHidePrices) }
                    .padding(vertical = 4.dp)
                    .testTag("challan_hide_prices_switch"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hide item rates & values on Challan",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Shows only quantities & units for godown/transport movement",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = challanHidePrices,
                    onCheckedChange = onChallanHidePricesChange
                )
            }
        }
    }

    // Place of Supply & GST Supply Mode Selector Sheet
    if (showPosSelectorSheet) {
        GlassSheet(
            onDismissRequest = { showPosSelectorSheet = false },
            title = "Place of Supply & Tax Regime",
            subtitle = "Same state as firm ($supplierStateCode) charges CGST + SGST; different state charges IGST"
        ) {
            Text(
                text = "Select State / UT",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "27" to "27 - Maharashtra",
                    "19" to "19 - West Bengal",
                    "24" to "24 - Gujarat",
                    "29" to "29 - Karnataka",
                    "07" to "07 - Delhi",
                    "33" to "33 - Tamil Nadu",
                    "09" to "09 - Uttar Pradesh",
                    "08" to "08 - Rajasthan",
                    "36" to "36 - Telangana"
                ).forEach { (code, label) ->
                    val isIntra = code == supplierStateCode
                    FilterChip(
                        selected = placeOfSupplyStateCode == code && supplyTypeOverride == "AUTO",
                        onClick = {
                            onPlaceOfSupplyChange(code, "AUTO", isReverseCharge)
                            showPosSelectorSheet = false
                        },
                        label = {
                            Text("$label (${if (isIntra) "Intra: CGST+SGST" else "Inter: IGST"})")
                        },
                        colors = vanijyaFilterChipColors()
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = supplyTypeOverride == "EXPORT_WITHOUT_TAX",
                    onClick = {
                        val next = if (supplyTypeOverride == "EXPORT_WITHOUT_TAX") "AUTO" else "EXPORT_WITHOUT_TAX"
                        onPlaceOfSupplyChange(placeOfSupplyStateCode, next, isReverseCharge)
                    },
                    label = { Text("Export (0% LUT)") },
                    colors = vanijyaFilterChipColors()
                )
                FilterChip(
                    selected = isReverseCharge,
                    onClick = {
                        onPlaceOfSupplyChange(placeOfSupplyStateCode, supplyTypeOverride, !isReverseCharge)
                    },
                    label = { Text("Reverse Charge (RCM)") },
                    colors = vanijyaFilterChipColors()
                )
            }
        }
    }
}

// ============================================================================
// 5. REUSABLE COMPONENT: ItemLineCard (Compact card, [- qty +] stepper, edit, bin, swipe-to-delete, reorder)
// ============================================================================
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ItemLineCard(
    index: Int,
    totalLinesCount: Int,
    line: BillingDraftLineItem,
    calculatedLine: GstCalculatedLine?,
    hidePrices: Boolean = false,
    hideAmounts: Boolean = false,
    decimals: Int = 2,
    onQuantityChange: (Long) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalVanijyaGlass.current
    val lineTotalPaise = calculatedLine?.lineTotalPaise ?: (line.qty * line.unitPricePaise)
    val rateFormatted = Money(line.unitPricePaise).formatIndian(decimals = 2, includeSymbol = true)
    val discountLabel = if (line.isDiscountInRupees && line.discountAmountPaise > 0L) {
        "Disc ${Money(line.discountAmountPaise).formatIndian(decimals = 0)}"
    } else {
        "Disc ${line.discountPercent}%"
    }
    val gstLabel = "GST ${formatGstSlabRate(line.taxRateBps)}"

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDeleteClick()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CoralError.copy(alpha = 0.22f))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete item line",
                        tint = CoralError
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Remove",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = CoralError
                    )
                }
            }
        },
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 3.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, glass.borderGradientStart, RoundedCornerShape(20.dp))
                .combinedClickable(
                    onClick = onEditClick,
                    onLongClick = onLongClick
                )
                .testTag("builder_line_card_$index")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Top Row: Bold 16sp Item Name (Left) + Tabular Line Total (Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = line.itemName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (hidePrices) {
                        Text(
                            text = "${line.qty} ${line.unit}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        AmountText(
                            amount = Money(lineTotalPaise),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            masked = hideAmounts,
                            decimals = decimals
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Middle Row: HSN 1234 | 2 PCS x ₹100.00 | Disc 0% | GST 18%
                Text(
                    text = buildString {
                        append("HSN ${line.hsnSacCode.ifBlank { "-" }}")
                        append("  |  ${line.qty} ${line.unit}")
                        if (line.freeQty > 0L) append(" (+${line.freeQty} Free)")
                        if (!hidePrices) {
                            append(" × $rateFormatted")
                            append("  |  $discountLabel")
                            append("  |  $gstLabel")
                        }
                        if (line.batchNumber.isNotBlank()) {
                            append("  |  Batch: ${line.batchNumber}")
                        }
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Row: Quick Quantity Stepper [- 2 +] on Left, Reorder + [Edit] + [Bin] on Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Stepper: [-] qty [+]
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (line.qty > 1L) onQuantityChange(line.qty - 1L)
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("line_qty_minus_$index")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease quantity for ${line.itemName}",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = line.qty.toString(),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFeatureSettings = "tnum"
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .testTag("line_qty_text_$index")
                            )
                            IconButton(
                                onClick = { onQuantityChange(line.qty + 1L) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("line_qty_plus_$index")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase quantity for ${line.itemName}",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Right Actions: Reorder Up/Down + Edit + Bin
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (totalLinesCount > 1 && index > 0) {
                            IconButton(
                                onClick = onMoveUp,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("line_move_up_$index")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Move line ${index + 1} up",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        if (totalLinesCount > 1 && index < totalLinesCount - 1) {
                            IconButton(
                                onClick = onMoveDown,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("line_move_down_$index")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Move line ${index + 1} down",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("line_edit_btn_$index")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit ${line.itemName}",
                                tint = PurchaseBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("line_delete_btn_$index")
                                .testTag("builder_line_more_$index")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete ${line.itemName}",
                                tint = CoralError,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 6. REUSABLE COMPONENT: ItemEditSheet (Searchable picker + Rate, Qty, Unit, %/₹ Discount, GST 0-40%, Live Preview)
// ============================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemEditSheet(
    isEditingExistingLine: Boolean,
    editingLineIndex: Int,
    initialLine: BillingDraftLineItem?,
    docType: String,
    catalogItems: List<ItemEntity>,
    itemBatches: List<ItemBatchEntity>,
    taxRates: List<TaxRateEntity>,
    onDismiss: () -> Unit,
    onSaveLine: (BillingDraftLineItem) -> Unit,
    onDeleteLine: (() -> Unit)? = null
) {
    val isPurchase = isPurchaseVariant(docType)
    val defaultCatalogItem = catalogItems.firstOrNull()

    var catalogSearchQuery by remember { mutableStateOf("") }
    var lineItemId by remember(editingLineIndex, initialLine) {
        mutableStateOf(initialLine?.itemId ?: defaultCatalogItem?.id ?: "")
    }
    var lineItemName by remember(editingLineIndex, initialLine) {
        mutableStateOf(initialLine?.itemName ?: defaultCatalogItem?.name ?: "Standard GST Item")
    }
    var lineItemCode by remember(editingLineIndex, initialLine) {
        mutableStateOf(initialLine?.itemCode ?: defaultCatalogItem?.itemCode ?: "")
    }
    var lineHsn by remember(editingLineIndex, initialLine) {
        mutableStateOf(initialLine?.hsnSacCode ?: defaultCatalogItem?.hsnSacCode ?: "1006")
    }
    var lineUnit by remember(editingLineIndex, initialLine) {
        mutableStateOf(initialLine?.unit ?: defaultCatalogItem?.primaryUnit ?: "PCS")
    }
    var lineQtyText by remember(editingLineIndex, initialLine) {
        mutableStateOf((initialLine?.qty ?: 1L).toString())
    }
    var lineFreeQtyText by remember(editingLineIndex, initialLine) {
        mutableStateOf((initialLine?.freeQty ?: 0L).toString())
    }
    var lineRateRupeesText by remember(editingLineIndex, initialLine) {
        val defaultPricePaise = initialLine?.unitPricePaise
            ?: if (isPurchase) {
                defaultCatalogItem?.purchasePricePaise ?: 100_00L
            } else {
                defaultCatalogItem?.salePricePaise ?: 100_00L
            }
        val rupees = defaultPricePaise / 100L
        val paiseRem = defaultPricePaise % 100L
        mutableStateOf(if (paiseRem == 0L) rupees.toString() else "${rupees}.${paiseRem.toString().padStart(2, '0')}")
    }
    var linePurchaseCostPaise by remember(editingLineIndex, initialLine) {
        mutableStateOf(initialLine?.purchaseCostPaise ?: defaultCatalogItem?.purchasePricePaise ?: 80_00L)
    }
    var isDiscountInRupees by remember(editingLineIndex, initialLine) {
        mutableStateOf(initialLine?.isDiscountInRupees ?: false)
    }
    var lineDiscountInputText by remember(editingLineIndex, initialLine) {
        val initVal = if (initialLine?.isDiscountInRupees == true) {
            (initialLine.discountAmountPaise / 100L).toString()
        } else {
            (initialLine?.discountPercent ?: 0).toString()
        }
        mutableStateOf(initVal)
    }
    var lineTaxRateBps by remember(editingLineIndex, initialLine) {
        mutableIntStateOf(initialLine?.taxRateBps ?: defaultCatalogItem?.taxRateBps ?: 1800)
    }
    var lineTaxInclusive by remember(editingLineIndex, initialLine) {
        mutableStateOf(initialLine?.taxInclusive ?: defaultCatalogItem?.taxInclusive ?: false)
    }
    var lineBatchNo by remember(editingLineIndex, initialLine) {
        mutableStateOf(initialLine?.batchNumber ?: "")
    }
    var lineSerialCsv by remember(editingLineIndex, initialLine) {
        mutableStateOf(initialLine?.serialNumbersCsv ?: "")
    }

    val filteredCatalog = remember(catalogItems, catalogSearchQuery) {
        if (catalogSearchQuery.isBlank()) {
            catalogItems.take(8)
        } else {
            catalogItems.filter {
                it.name.contains(catalogSearchQuery, ignoreCase = true) ||
                    it.itemCode.contains(catalogSearchQuery, ignoreCase = true) ||
                    it.barcode.contains(catalogSearchQuery, ignoreCase = true) ||
                    it.hsnSacCode.contains(catalogSearchQuery, ignoreCase = true)
            }.take(8)
        }
    }

    // Live Line Total Calculation Preview
    val previewQty = lineQtyText.toLongOrNull()?.coerceAtLeast(0L) ?: 0L
    val previewRatePaise = remember(lineRateRupeesText) {
        runCatching { Money.parseInput(lineRateRupeesText).paise }.getOrDefault(0L)
    }
    val previewGrossPaise = previewQty * previewRatePaise
    val previewDiscPaise = remember(previewGrossPaise, isDiscountInRupees, lineDiscountInputText) {
        if (isDiscountInRupees) {
            ((lineDiscountInputText.toLongOrNull() ?: 0L) * 100L).coerceAtMost(previewGrossPaise)
        } else {
            val pct = (lineDiscountInputText.toIntOrNull() ?: 0).coerceIn(0, 99)
            (previewGrossPaise * pct) / 100L
        }
    }
    val previewAfterDiscPaise = (previewGrossPaise - previewDiscPaise).coerceAtLeast(0L)
    val previewTaxablePaise = if (lineTaxInclusive && lineTaxRateBps > 0) {
        ((previewAfterDiscPaise * 10_000.0) / (10_000 + lineTaxRateBps)).roundToLong()
    } else {
        previewAfterDiscPaise
    }
    val previewTaxPaise = if (lineTaxInclusive && lineTaxRateBps > 0) {
        (previewAfterDiscPaise - previewTaxablePaise).coerceAtLeast(0L)
    } else {
        ((previewTaxablePaise * lineTaxRateBps.toDouble()) / 10_000.0).roundToLong()
    }
    val previewLineTotalPaise = previewTaxablePaise + previewTaxPaise

    GlassSheet(
        onDismissRequest = onDismiss,
        title = if (isEditingExistingLine) "Edit Item Line #${editingLineIndex + 1}" else "Add Item to Bill",
        subtitle = "Search item catalog by name/code/barcode or enter a custom line item"
    ) {
        // 1. Searchable Item Picker
        if (catalogItems.isNotEmpty()) {
            OutlinedTextField(
                value = catalogSearchQuery,
                onValueChange = { catalogSearchQuery = it },
                label = { Text("Search item by name, SKU code, HSN or barcode", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search catalog") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = vanijyaTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("item_picker_search_input")
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filteredCatalog.forEach { catItem ->
                    FilterChip(
                        selected = lineItemId == catItem.id,
                        onClick = {
                            lineItemId = catItem.id
                            lineItemName = catItem.name
                            lineItemCode = catItem.itemCode
                            lineHsn = catItem.hsnSacCode
                            lineUnit = catItem.primaryUnit
                            val pricePaise = if (isPurchase) catItem.purchasePricePaise else catItem.salePricePaise
                            lineRateRupeesText = (pricePaise / 100L).toString()
                            linePurchaseCostPaise = catItem.purchasePricePaise
                            lineTaxRateBps = catItem.taxRateBps
                            lineTaxInclusive = catItem.taxInclusive
                            val batch = itemBatches.firstOrNull { it.itemId == catItem.id && !it.isDeleted }
                            lineBatchNo = batch?.batchNumber ?: ""
                        },
                        label = {
                            Text(
                                text = "${catItem.name} (₹${catItem.salePricePaise / 100L})",
                                fontSize = 12.sp
                            )
                        },
                        colors = vanijyaFilterChipColors(),
                        modifier = Modifier.testTag("catalog_item_chip_${catItem.id}")
                    )
                }
            }
        }

        // 2. Item Name & HSN
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = lineItemName,
                onValueChange = { lineItemName = it },
                label = { Text("Item / Service Name", fontSize = 12.sp) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = vanijyaTextFieldColors(),
                modifier = Modifier
                    .weight(1.4f)
                    .testTag("line_editor_name_input")
            )
            OutlinedTextField(
                value = lineHsn,
                onValueChange = { lineHsn = it.take(8) },
                label = { Text("HSN/SAC", fontSize = 12.sp) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = vanijyaTextFieldColors(),
                modifier = Modifier
                    .weight(0.7f)
                    .testTag("line_editor_hsn_input")
            )
        }

        // 3. Quantity, Free Qty & Unit Dropdown Chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = lineQtyText,
                onValueChange = { lineQtyText = it.filter(Char::isDigit).take(6) },
                label = { Text("Quantity", fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                colors = vanijyaTextFieldColors(),
                modifier = Modifier
                    .weight(1f)
                    .testTag("line_editor_qty_input")
            )
            OutlinedTextField(
                value = lineRateRupeesText,
                onValueChange = { input ->
                    lineRateRupeesText = input.filter { it.isDigit() || it == '.' }.take(10)
                },
                label = { Text("Rate (₹)", fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(14.dp),
                colors = vanijyaTextFieldColors(),
                modifier = Modifier
                    .weight(1f)
                    .testTag("line_editor_rate_input")
            )
            OutlinedTextField(
                value = lineUnit,
                onValueChange = { lineUnit = it.uppercase().take(8) },
                label = { Text("Unit", fontSize = 12.sp) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = vanijyaTextFieldColors(),
                modifier = Modifier
                    .weight(0.75f)
                    .testTag("line_editor_unit_input")
            )
        }

        // Quick Unit selector chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Unit:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            listOf("PCS", "BAG", "BOX", "KG", "LTR", "MTR", "NOS").forEach { u ->
                FilterChip(
                    selected = lineUnit == u,
                    onClick = { lineUnit = u },
                    label = { Text(u, fontSize = 11.sp) },
                    colors = vanijyaFilterChipColors()
                )
            }
        }

        // 4. Discount with (% or ₹) Toggle + Tax Inclusive/Exclusive Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = lineDiscountInputText,
                onValueChange = { lineDiscountInputText = it.filter(Char::isDigit).take(6) },
                label = {
                    Text(
                        text = if (isDiscountInRupees) "Discount (₹)" else "Discount (%)",
                        fontSize = 12.sp
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                colors = vanijyaTextFieldColors(),
                modifier = Modifier
                    .weight(1f)
                    .testTag("line_editor_disc_input")
            )
            FilterChip(
                selected = !isDiscountInRupees,
                onClick = { isDiscountInRupees = false },
                label = { Text("%") },
                colors = vanijyaFilterChipColors(),
                modifier = Modifier.testTag("discount_mode_pct_chip")
            )
            FilterChip(
                selected = isDiscountInRupees,
                onClick = { isDiscountInRupees = true },
                label = { Text("₹") },
                colors = vanijyaFilterChipColors(),
                modifier = Modifier.testTag("discount_mode_amt_chip")
            )
            FilterChip(
                selected = lineTaxInclusive,
                onClick = { lineTaxInclusive = !lineTaxInclusive },
                label = { Text(if (lineTaxInclusive) "Tax Incl." else "Tax Excl.") },
                colors = vanijyaFilterChipColors(),
                modifier = Modifier.testTag("line_tax_inclusive_switch")
            )
        }

        // 5. GST Rate Chips (0%, 5%, 12%, 18%, 28%, 40% from editable tax table)
        Text(
            text = "GST Slab Rate",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val slabList = remember(taxRates) {
            val base = listOf(0, 500, 1200, 1800, 2800, 4000)
            val fromDb = taxRates.map { it.rateBps }
            (base + fromDb).distinct().sorted()
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            slabList.forEach { bps ->
                FilterChip(
                    selected = lineTaxRateBps == bps,
                    onClick = { lineTaxRateBps = bps },
                    label = { Text("GST ${formatGstSlabRate(bps)}", fontSize = 12.sp) },
                    colors = vanijyaFilterChipColors(),
                    modifier = Modifier.testTag("gst_rate_chip_$bps")
                )
            }
        }

        // Optional Batch / Serial Row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = lineBatchNo,
                onValueChange = { lineBatchNo = it },
                label = { Text("Batch No. (Optional)", fontSize = 12.sp) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = vanijyaTextFieldColors(),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = lineSerialCsv,
                onValueChange = { lineSerialCsv = it },
                label = { Text("Serial / IMEI (Optional)", fontSize = 12.sp) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = vanijyaTextFieldColors(),
                modifier = Modifier.weight(1f)
            )
        }

        // 6. Live Line-Total Preview Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("line_editor_live_preview_card")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Live Line Preview",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Taxable ${Money(previewTaxablePaise).formatIndian()} + GST ${Money(previewTaxPaise).formatIndian()}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            fontFeatureSettings = "tnum"
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = Money(previewLineTotalPaise).formatIndian(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 7. Save & Delete Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isEditingExistingLine && onDeleteLine != null) {
                VanijyaButton(
                    text = "Delete",
                    icon = Icons.Default.Delete,
                    isOutlined = true,
                    onClick = {
                        onDeleteLine()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    testTag = "line_editor_delete_btn"
                )
            } else {
                VanijyaButton(
                    text = "Cancel",
                    isOutlined = true,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
            }
            VanijyaButton(
                text = if (isEditingExistingLine) "Save Line" else "Add to Bill",
                icon = Icons.Default.CheckCircle,
                onClick = {
                    val parsedQty = lineQtyText.toLongOrNull() ?: 0L
                    val parsedFree = lineFreeQtyText.toLongOrNull() ?: 0L
                    val parsedDiscPct = if (!isDiscountInRupees) {
                        (lineDiscountInputText.toIntOrNull() ?: 0).coerceIn(0, 99)
                    } else 0
                    val parsedDiscPaise = if (isDiscountInRupees) {
                        (lineDiscountInputText.toLongOrNull() ?: 0L) * 100L
                    } else 0L
                    val batch = itemBatches.firstOrNull { it.itemId == lineItemId && !it.isDeleted }
                    onSaveLine(
                        BillingDraftLineItem(
                            id = initialLine?.id ?: UUID.randomUUID().toString(),
                            itemId = lineItemId,
                            itemName = lineItemName.trim().ifBlank { "Item" },
                            itemCode = lineItemCode,
                            hsnSacCode = lineHsn.ifBlank { "1006" },
                            unit = lineUnit.ifBlank { "PCS" },
                            qty = parsedQty,
                            freeQty = parsedFree,
                            unitPricePaise = previewRatePaise,
                            purchaseCostPaise = linePurchaseCostPaise,
                            discountPercent = parsedDiscPct,
                            discountAmountPaise = parsedDiscPaise,
                            isDiscountInRupees = isDiscountInRupees,
                            taxRateBps = lineTaxRateBps,
                            taxInclusive = lineTaxInclusive,
                            batchId = batch?.id ?: "",
                            batchNumber = lineBatchNo.ifBlank { batch?.batchNumber.orEmpty() },
                            serialNumbersCsv = lineSerialCsv
                        )
                    )
                },
                modifier = Modifier.weight(1.2f),
                testTag = "line_editor_confirm_btn"
            )
        }
    }
}

// ============================================================================
// 7. REUSABLE COMPONENT: BillSummaryCard (Subtotal, Discount in emerald, Taxable, Tax breakup by slab, Charges, Round-off, 28sp Gradient Grand Total, Words)
// ============================================================================
@Composable
fun BillSummaryCard(
    grossSubtotalPaise: Long,
    totalDiscountPaise: Long,
    taxableAmountPaise: Long,
    cgstTotalPaise: Long,
    sgstTotalPaise: Long,
    igstTotalPaise: Long,
    cessTotalPaise: Long,
    otherChargesPaise: Long,
    roundOffPaise: Long,
    grandTotalPaise: Long,
    amountInWords: String,
    supplyType: GstSupplyType = GstSupplyType.INTRA_STATE,
    taxSlabBreakup: List<TaxSlabBreakupRow> = emptyList(),
    isEditable: Boolean = false,
    invoiceDiscountInputText: String = "",
    freightChargeInputText: String = "",
    onInvoiceDiscountChange: (String) -> Unit = {},
    onFreightChargeChange: (String) -> Unit = {},
    hideAmounts: Boolean = false,
    decimals: Int = 2,
    modifier: Modifier = Modifier
) {
    var taxBreakupExpanded by rememberSaveable { mutableStateOf(false) }
    var showDiscountInputRow by rememberSaveable { mutableStateOf(invoiceDiscountInputText.isNotBlank()) }
    var showChargesInputRow by rememberSaveable { mutableStateOf(freightChargeInputText.isNotBlank()) }

    val primarySlabRateBps = taxSlabBreakup.firstOrNull()?.taxRateBps ?: 1800
    val halfSlabLabel = formatGstSlabRate(primarySlabRateBps / 2)
    val fullSlabLabel = formatGstSlabRate(primarySlabRateBps)
    val hasMultipleSlabs = taxSlabBreakup.size > 1

    SectionCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("builder_collapsible_totals_panel"),
        sectionTitle = "Bill Summary",
        headerTrailing = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { taxBreakupExpanded = !taxBreakupExpanded }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("builder_toggle_totals_btn")
            ) {
                Text(
                    text = if (taxBreakupExpanded) "Hide tax breakup" else "Tax breakup",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (taxBreakupExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle Tax Breakup",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    ) {
        // 1. Subtotal Row (Normal weight)
        SummaryRowItem(
            label = "Subtotal",
            amountPaise = grossSubtotalPaise,
            hideAmounts = hideAmounts,
            decimals = decimals,
            testTag = "bill_summary_subtotal_row"
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Discount Row (Emerald with minus sign, dimmed when zero, [+ add discount] button)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (totalDiscountPaise == 0L) 0.62f else 1f)
                .testTag("bill_summary_discount_row"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Discount",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isEditable) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldPrimary.copy(alpha = 0.14f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showDiscountInputRow = !showDiscountInputRow }
                            .testTag("bill_summary_add_discount_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = if (totalDiscountPaise > 0L) Icons.Default.Edit else Icons.Default.Add,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (totalDiscountPaise > 0L) "Edit" else "Add discount",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = EmeraldPrimary
                            )
                        }
                    }
                }
            }
            Text(
                text = if (hideAmounts) "- ₹••••" else "- ${Money(totalDiscountPaise).formatIndian(decimals = decimals)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFeatureSettings = "tnum"
                ),
                color = EmeraldPrimary
            )
        }

        AnimatedVisibility(visible = isEditable && showDiscountInputRow) {
            PaddingBox(top = 6.dp) {
                OutlinedTextField(
                    value = invoiceDiscountInputText,
                    onValueChange = { onInvoiceDiscountChange(it.filter(Char::isDigit).take(7)) },
                    label = { Text("Bill Discount (₹)", fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bill_summary_discount_input")
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 3. Taxable Amount Row (Normal weight)
        SummaryRowItem(
            label = "Taxable amount",
            amountPaise = taxableAmountPaise,
            hideAmounts = hideAmounts,
            decimals = decimals,
            testTag = "bill_summary_taxable_row"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 4. TAX Mini-Heading + Tax Rows (CGST/SGST or IGST) + Expandable Per-Rate Breakup
        Text(
            text = "TAX",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(4.dp))

        if (supplyType == GstSupplyType.INTRA_STATE) {
            if (hasMultipleSlabs) {
                taxSlabBreakup.forEach { slab ->
                    val halfLabel = formatGstSlabRate(slab.taxRateBps / 2)
                    SummaryRowItem(
                        label = "CGST $halfLabel",
                        amountPaise = slab.cgstPaise,
                        isLighterTone = true,
                        hideAmounts = hideAmounts,
                        decimals = decimals
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    SummaryRowItem(
                        label = "SGST $halfLabel",
                        amountPaise = slab.sgstPaise,
                        isLighterTone = true,
                        hideAmounts = hideAmounts,
                        decimals = decimals
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            } else {
                SummaryRowItem(
                    label = "CGST $halfSlabLabel",
                    amountPaise = cgstTotalPaise,
                    isLighterTone = true,
                    hideAmounts = hideAmounts,
                    decimals = decimals,
                    testTag = "bill_summary_cgst_row"
                )
                Spacer(modifier = Modifier.height(4.dp))
                SummaryRowItem(
                    label = "SGST $halfSlabLabel",
                    amountPaise = sgstTotalPaise,
                    isLighterTone = true,
                    hideAmounts = hideAmounts,
                    decimals = decimals,
                    testTag = "bill_summary_sgst_row"
                )
            }
        } else {
            if (hasMultipleSlabs) {
                taxSlabBreakup.forEach { slab ->
                    val rateLabel = formatGstSlabRate(slab.taxRateBps)
                    SummaryRowItem(
                        label = "IGST $rateLabel",
                        amountPaise = slab.igstPaise,
                        isLighterTone = true,
                        hideAmounts = hideAmounts,
                        decimals = decimals
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            } else {
                SummaryRowItem(
                    label = "IGST $fullSlabLabel",
                    amountPaise = igstTotalPaise,
                    isLighterTone = true,
                    hideAmounts = hideAmounts,
                    decimals = decimals,
                    testTag = "bill_summary_igst_row"
                )
            }
        }

        if (cessTotalPaise > 0L) {
            Spacer(modifier = Modifier.height(4.dp))
            SummaryRowItem(
                label = "Compensation Cess",
                amountPaise = cessTotalPaise,
                isLighterTone = true,
                hideAmounts = hideAmounts,
                decimals = decimals
            )
        }

        // Expandable Per-Rate GST Breakup Table
        AnimatedVisibility(visible = taxBreakupExpanded) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .testTag("bill_summary_tax_breakup_box")
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "GST Slab-wise Tax Breakup",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (taxSlabBreakup.isEmpty()) {
                        Text(
                            text = "CGST: ${Money(cgstTotalPaise).formatIndian()}  |  SGST: ${Money(sgstTotalPaise).formatIndian()}  |  IGST: ${Money(igstTotalPaise).formatIndian()}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontFeatureSettings = "tnum"
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        taxSlabBreakup.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "GST ${formatGstSlabRate(row.taxRateBps)} (Taxable ${Money(row.taxablePaise).formatIndian()})",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (supplyType == GstSupplyType.INTRA_STATE) {
                                        "CGST ${Money(row.cgstPaise).formatIndian()} + SGST ${Money(row.sgstPaise).formatIndian()}"
                                    } else {
                                        "IGST ${Money(row.igstPaise).formatIndian()}"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFeatureSettings = "tnum"
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 5. Other Charges (freight etc.) Row (dimmed when zero, [+ add] button)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (otherChargesPaise == 0L) 0.62f else 1f)
                .testTag("bill_summary_charges_row"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Other charges (freight etc.)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isEditable) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showChargesInputRow = !showChargesInputRow }
                            .testTag("bill_summary_add_charge_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = if (otherChargesPaise > 0L) Icons.Default.Edit else Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (otherChargesPaise > 0L) "Edit" else "Add",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            AmountText(
                amount = Money(otherChargesPaise),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontFeatureSettings = "tnum"
                ),
                color = MaterialTheme.colorScheme.onSurface,
                masked = hideAmounts,
                decimals = decimals
            )
        }

        AnimatedVisibility(visible = isEditable && showChargesInputRow) {
            PaddingBox(top = 6.dp) {
                OutlinedTextField(
                    value = freightChargeInputText,
                    onValueChange = { onFreightChargeChange(it.filter(Char::isDigit).take(7)) },
                    label = { Text("Freight / Packing Charges (₹)", fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bill_summary_freight_input")
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 6. Round Off Row (dimmed when zero)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (roundOffPaise == 0L) 0.55f else 1f)
                .testTag("bill_summary_round_off_row"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Round off",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val signPrefix = if (roundOffPaise > 0L) "+" else if (roundOffPaise < 0L) "-" else ""
            Text(
                text = if (hideAmounts) "₹••••" else "$signPrefix${Money(abs(roundOffPaise)).formatIndian(decimals = decimals)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontFeatureSettings = "tnum"
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(12.dp))

        // 7. GRAND TOTAL on a Primary-Gradient Pill with 28sp Bold White Text
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(VioletPrimary, PurchaseBlue)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .testTag("bill_summary_grand_total_pill")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GRAND TOTAL",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = Color.White.copy(alpha = 0.92f)
                )
                Text(
                    text = if (hideAmounts) "₹ ••••••" else Money(grandTotalPaise).formatIndian(decimals = decimals),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum"
                    ),
                    color = Color.White,
                    modifier = Modifier.testTag("bill_summary_grand_total_text")
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 8. Amount in Words (Small, Muted)
        Text(
            text = amountInWords,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("bill_summary_amount_in_words")
        )
    }
}

@Composable
private fun SummaryRowItem(
    label: String,
    amountPaise: Long,
    isLighterTone: Boolean = false,
    hideAmounts: Boolean = false,
    decimals: Int = 2,
    testTag: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = if (isLighterTone) 12.sp else 13.sp
            ),
            color = if (isLighterTone) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        AmountText(
            amount = Money(amountPaise),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = if (isLighterTone) 14.sp else 16.sp,
                fontWeight = if (isLighterTone) FontWeight.Normal else FontWeight.Medium,
                fontFeatureSettings = "tnum"
            ),
            color = if (isLighterTone) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            masked = hideAmounts,
            decimals = decimals
        )
    }
}

@Composable
private fun PaddingBox(
    top: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.padding(top = top)) {
        content()
    }
}

// ============================================================================
// 8. REUSABLE COMPONENT: PaymentCard (Hidden for Quotation/Order/Challan; Cash default; Coral/Emerald Balance Due)
// ============================================================================
@Composable
fun PaymentCard(
    docType: String,
    grandTotalPaise: Long,
    amountInputText: String,
    selectedPaymentMode: String,
    hideAmounts: Boolean = false,
    decimals: Int = 2,
    onAmountInputChange: (String) -> Unit,
    onSelectPaymentStatusChip: (String) -> Unit, // "Paid", "Partial", "Unpaid"
    onSelectPaymentMode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!shouldShowPaymentCard(docType)) return

    val isPurchase = isPurchaseVariant(docType)
    val amountLabel = if (isPurchase) "Amount paid (₹)" else "Amount received (₹)"

    val enteredPaise = remember(amountInputText, grandTotalPaise) {
        when (amountInputText) {
            "FULL" -> grandTotalPaise
            "" -> 0L
            else -> ((amountInputText.toLongOrNull() ?: 0L) * 100L).coerceAtMost(grandTotalPaise)
        }
    }
    val balanceDuePaise = (grandTotalPaise - enteredPaise).coerceAtLeast(0L)
    val autoStatus = when {
        enteredPaise >= grandTotalPaise && grandTotalPaise > 0L -> "Paid"
        enteredPaise > 0L -> "Partial"
        else -> "Unpaid"
    }

    SectionCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("billing_payment_card"),
        sectionTitle = "Payment"
    ) {
        // 1. Status Chips: [ Paid ] [ Partial ] [ Unpaid ] (Auto-selected from amount)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Paid", "Partial", "Unpaid").forEach { status ->
                val selected = autoStatus == status
                FilterChip(
                    selected = selected,
                    onClick = { onSelectPaymentStatusChip(status) },
                    label = {
                        Text(
                            text = status,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (status) {
                            "Paid" -> EmeraldPrimary.copy(alpha = 0.22f)
                            "Partial" -> GoldPrimary.copy(alpha = 0.22f)
                            else -> CoralError.copy(alpha = 0.22f)
                        },
                        selectedLabelColor = when (status) {
                            "Paid" -> EmeraldPrimary
                            "Partial" -> GoldPrimary
                            else -> CoralError
                        }
                    ),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("payment_status_chip_${status.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Amount Received / Amount Paid Input + Payment Mode Chips ([Cash] [UPI] [Card] [Bank] [Cheque])
        OutlinedTextField(
            value = if (amountInputText == "FULL") (grandTotalPaise / 100L).toString() else amountInputText,
            onValueChange = { onAmountInputChange(it.filter(Char::isDigit).take(9)) },
            label = { Text(amountLabel, fontSize = 12.sp) },
            placeholder = { Text("0") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(14.dp),
            colors = vanijyaTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("payment_amount_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Default mode is Cash (never Bank by default)
            listOf("Cash", "UPI", "Card", "Bank", "Cheque").forEach { mode ->
                val selected = selectedPaymentMode.equals(mode, ignoreCase = true)
                FilterChip(
                    selected = selected,
                    onClick = { onSelectPaymentMode(mode) },
                    label = { Text(mode, fontSize = 12.sp) },
                    colors = vanijyaFilterChipColors(),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("payment_mode_chip_${mode.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        Spacer(modifier = Modifier.height(10.dp))

        // 3. Balance Due Row (Coral if > 0, Emerald when Paid / 0)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("payment_balance_due_row"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Balance due",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AmountText(
                amount = Money(balanceDuePaise),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum"
                ),
                color = if (balanceDuePaise > 0L) CoralError else EmeraldPrimary,
                masked = hideAmounts,
                decimals = decimals
            )
        }
    }
}

// ============================================================================
// 9. REUSABLE COMPONENT: StickyTotalBar (Fixed bottom bar with Total/Balance left, Primary CTA + [v] right)
// ============================================================================
@Composable
fun StickyTotalBar(
    docType: String,
    grandTotalPaise: Long,
    balanceDuePaise: Long,
    hideAmounts: Boolean = false,
    decimals: Int = 2,
    onPrimarySave: () -> Unit,
    onSaveAndShare: () -> Unit,
    onSaveAndNew: () -> Unit,
    onSaveAsDraft: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalVanijyaGlass.current
    var showDropdown by remember { mutableStateOf(false) }
    val formattedTotal = if (hideAmounts) "₹••••" else Money(grandTotalPaise).formatIndian(decimals = decimals)
    val ctaText = stickySaveButtonLabel(docType, formattedTotal)

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 10.dp,
        shadowElevation = 14.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = glass.borderGradientStart,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .testTag("builder_bottom_action_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Total & Balance Due in Tabular Numerals
            Column(modifier = Modifier.weight(0.95f)) {
                Text(
                    text = "Total $formattedTotal",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (shouldShowPaymentCard(docType)) {
                    val formattedBalance = if (hideAmounts) "₹••••" else Money(balanceDuePaise).formatIndian(decimals = decimals)
                    Text(
                        text = "Balance $formattedBalance",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFeatureSettings = "tnum"
                        ),
                        color = if (balanceDuePaise > 0L) CoralError else EmeraldPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = SalesDocTypes.displayLabel(docType),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right: Primary Save CTA Button + [v] Dropdown Menu Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onPrimarySave,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VioletPrimary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("builder_save_btn")
                ) {
                    Text(
                        text = ctaText,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum"
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box {
                    OutlinedButton(
                        onClick = { showDropdown = true },
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("builder_more_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "More save options",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false },
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        DropdownMenuItem(
                            text = { Text("Save and Share PDF", color = MaterialTheme.colorScheme.onSurface) },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = PurchaseBlue) },
                            onClick = {
                                showDropdown = false
                                onSaveAndShare()
                            },
                            modifier = Modifier.testTag("builder_menu_save_and_share")
                        )
                        DropdownMenuItem(
                            text = { Text("Save and New", color = MaterialTheme.colorScheme.onSurface) },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = EmeraldPrimary) },
                            onClick = {
                                showDropdown = false
                                onSaveAndNew()
                            },
                            modifier = Modifier.testTag("builder_menu_save_and_new")
                        )
                        DropdownMenuItem(
                            text = { Text("Save as Draft", color = MaterialTheme.colorScheme.onSurface) },
                            leadingIcon = { Icon(Icons.Default.Save, contentDescription = null, tint = GoldPrimary) },
                            onClick = {
                                showDropdown = false
                                onSaveAsDraft()
                            },
                            modifier = Modifier.testTag("builder_menu_save_as_draft")
                        )
                        HorizontalDivider(color = glass.subtleDivider)
                        DropdownMenuItem(
                            text = { Text("Cancel", color = CoralError) },
                            leadingIcon = { Icon(Icons.Default.Cancel, contentDescription = null, tint = CoralError) },
                            onClick = {
                                showDropdown = false
                                onCancel()
                            },
                            modifier = Modifier.testTag("builder_menu_cancel")
                        )
                    }
                }
            }
        }
    }
}
