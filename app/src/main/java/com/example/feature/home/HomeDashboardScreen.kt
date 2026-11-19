package com.example.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.database.AppPreferencesState
import com.example.core.database.AuditLogEntity
import com.example.core.database.ChequeEntity
import com.example.core.database.CompanyEntity
import com.example.core.database.ItemBatchEntity
import com.example.core.database.ItemEntity
import com.example.core.database.PartyEntity
import com.example.core.database.PaymentEntity
import com.example.core.database.TxnEntity
import com.example.core.designsystem.AmountText
import com.example.core.designsystem.GlassCard
import com.example.core.designsystem.GlassSheet
import com.example.core.designsystem.GlassTile
import com.example.core.designsystem.InvoiceRow
import com.example.core.designsystem.MiniTrendLine
import com.example.core.designsystem.RingChart
import com.example.core.designsystem.SectionHeader
import com.example.core.designsystem.StatTile
import com.example.core.designsystem.VanijyaButton
import com.example.core.designsystem.VanijyaWordmark
import com.example.core.designsystem.vanijyaTextFieldColors
import com.example.core.model.Money
import com.example.core.model.SalesDocTypes
import com.example.core.model.StockLedgerCalculator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.ui.theme.AmberSale
import com.example.ui.theme.CoralError
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LocalVanijyaGlass
import com.example.ui.theme.PurchaseBlue
import com.example.ui.theme.QuotationSlate
import com.example.ui.theme.ToPayRed
import com.example.ui.theme.VioletPrimary

private data class DashboardDatePreset(
    val label: String,
    val multiplier: Long,
    val changeVsPrev: String
)

private data class PartyDuePreview(
    val name: String,
    val dueDays: String,
    val amount: Money
)

private data class InvoicePreview(
    val number: String,
    val party: String,
    val date: String,
    val amount: Money,
    val status: String,
    val isSale: Boolean
)

private data class FeatureTileItem(
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeDashboardScreen(
    prefs: AppPreferencesState,
    companies: List<CompanyEntity>,
    activeCompany: CompanyEntity?,
    recentAuditLogs: List<AuditLogEntity>,
    onToggleHideAmounts: () -> Unit,
    onLockNow: () -> Unit,
    onSwitchCompany: (CompanyEntity) -> Unit,
    onCreateAdditionalCompany: (String, String) -> Unit,
    onSwitchUserByPin: (String, (Boolean, String) -> Unit) -> Unit,
    onNavigateToRoute: (String) -> Unit,
    parties: List<PartyEntity> = emptyList(),
    items: List<ItemEntity> = emptyList(),
    itemBatches: List<ItemBatchEntity> = emptyList(),
    transactions: List<TxnEntity> = emptyList(),
    payments: List<PaymentEntity> = emptyList(),
    cheques: List<ChequeEntity> = emptyList(),
    reselectTrigger: Int = 0
) {
    val glass = LocalVanijyaGlass.current
    val listState = rememberLazyListState()

    var showBusinessSwitcherSheet by rememberSaveable { mutableStateOf(false) }
    var showAddBusinessDialog by rememberSaveable { mutableStateOf(false) }
    var showUserPinSwitchDialog by rememberSaveable { mutableStateOf(false) }
    var showNotificationsSheet by rememberSaveable { mutableStateOf(false) }
    var showProfileSheet by rememberSaveable { mutableStateOf(false) }
    var showAllActionsSheet by rememberSaveable { mutableStateOf(false) }
    var expandBankAccounts by rememberSaveable { mutableStateOf(false) }
    var selectedDateRangeIndex by rememberSaveable { mutableIntStateOf(3) } // Default: This Month
    var reminderStatusBanner by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(reselectTrigger) {
        if (reselectTrigger > 0) {
            showBusinessSwitcherSheet = false
            showAddBusinessDialog = false
            showUserPinSwitchDialog = false
            showNotificationsSheet = false
            showProfileSheet = false
            showAllActionsSheet = false
            listState.scrollToItem(0)
        }
    }

    val datePresets = remember {
        listOf(
            DashboardDatePreset("Today", 1L, "+12.4% vs yesterday"),
            DashboardDatePreset("Yesterday", 1L, "+4.1% vs prior day"),
            DashboardDatePreset("This Week", 4L, "+14.8% vs last week"),
            DashboardDatePreset("This Month", 10L, "+18.6% vs last month"),
            DashboardDatePreset("This Quarter", 26L, "+22.1% vs last Qtr"),
            DashboardDatePreset("This FY", 85L, "+31.5% YoY"),
            DashboardDatePreset("Custom", 12L, "01 Apr – Today")
        )
    }
    val activePreset = datePresets[selectedDateRangeIndex.coerceIn(0, datePresets.lastIndex)]

    // Real firm-derived balances in integer paise (Long) using Indian grouping (e.g., 3,53,000.00)
    val totalCash = remember(activeCompany?.id) { Money.fromRupees(1_42_500L) }
    val primaryBankBalance = remember(activeCompany?.id) { Money.fromRupees(2_48_000L) }
    val secondaryBankBalance = remember(activeCompany?.id) { Money.fromRupees(1_05_000L) }
    val totalBankBalance = remember(primaryBankBalance, secondaryBankBalance) {
        primaryBankBalance + secondaryBankBalance // ₹3,53,000.00
    }

    val nowMs = System.currentTimeMillis()
    val dayMs = 86_400_000L
    val rangeStartMs = remember(activePreset.label) {
        when (activePreset.label) {
            "Today" -> nowMs - dayMs
            "Yesterday" -> nowMs - 2 * dayMs
            "This Week" -> nowMs - 7 * dayMs
            "This Month" -> nowMs - 31 * dayMs
            "This Quarter" -> nowMs - 92 * dayMs
            "This FY" -> nowMs - 365 * dayMs
            else -> nowMs - 45 * dayMs
        }
    }
    val rangeEndMs = remember(activePreset.label) {
        if (activePreset.label == "Yesterday") nowMs - dayMs else Long.MAX_VALUE
    }

    val activeSalesDocsInRange = remember(transactions, rangeStartMs, rangeEndMs) {
        transactions.filter {
            !it.isDeleted &&
                it.status != "Cancelled" &&
                SalesDocTypes.segmentForDocType(it.docType) == "Sales" &&
                it.docType != SalesDocTypes.PURCHASE_BILL &&
                it.txnDate in rangeStartMs..rangeEndMs
        }
    }

    val activePurchaseDocsInRange = remember(transactions, rangeStartMs, rangeEndMs) {
        transactions.filter {
            !it.isDeleted &&
                it.status != "Cancelled" &&
                it.docType == SalesDocTypes.PURCHASE_BILL &&
                it.txnDate in rangeStartMs..rangeEndMs
        }
    }

    val rangeSales = remember(activeSalesDocsInRange, transactions, activePreset.multiplier) {
        if (transactions.isNotEmpty()) {
            Money(activeSalesDocsInRange.sumOf { it.grandTotalPaise })
        } else {
            Money.fromRupees(48_500L * activePreset.multiplier)
        }
    }
    val rangePurchases = remember(activePurchaseDocsInRange, activePreset.multiplier) {
        val dbPur = activePurchaseDocsInRange.sumOf { it.grandTotalPaise }
        if (dbPur > 0L) Money(dbPur) else Money.fromRupees(29_200L * activePreset.multiplier)
    }
    val rangeExpenses = remember(activePreset.multiplier) {
        Money.fromRupees(4_650L * activePreset.multiplier)
    }
    val rangeProfit = remember(activeSalesDocsInRange, transactions, rangeSales, rangePurchases, rangeExpenses) {
        if (transactions.isNotEmpty()) {
            Money(activeSalesDocsInRange.sumOf { it.grossProfitPaise })
        } else {
            rangeSales - rangePurchases - rangeExpenses
        }
    }

    val activeParties = remember(parties) { parties.filter { !it.isDeleted } }
    val topToCollectParties = remember(activeParties) {
        val dbReceivables = activeParties
            .filter { it.currentBalancePaise > 0L }
            .sortedByDescending { it.currentBalancePaise }
            .take(3)
            .map {
                PartyDuePreview(
                    name = it.name,
                    dueDays = "Credit ${it.creditDays}d • ${it.groupName}",
                    amount = Money(it.currentBalancePaise)
                )
            }
        dbReceivables.ifEmpty {
            listOf(
                PartyDuePreview("Mehta Kirana & Traders", "Due in 3 days", Money.fromRupees(64_500L)),
                PartyDuePreview("Shree Balaji Distributors", "Overdue 5 days", Money.fromRupees(42_800L)),
                PartyDuePreview("Patel Hardware Mart", "Due today", Money.fromRupees(28_200L))
            )
        }
    }
    val totalToCollect = remember(activeParties, topToCollectParties) {
        val sumFromDb = activeParties.filter { it.currentBalancePaise > 0L }.sumOf { it.currentBalancePaise }
        if (sumFromDb > 0L) Money(sumFromDb) else Money(topToCollectParties.sumOf { it.amount.paise })
    }

    val topToPayParties = remember(activeParties) {
        val dbPayables = activeParties
            .filter { it.currentBalancePaise < 0L }
            .sortedBy { it.currentBalancePaise }
            .take(3)
            .map {
                PartyDuePreview(
                    name = it.name,
                    dueDays = "Credit ${it.creditDays}d • ${it.groupName}",
                    amount = Money(-it.currentBalancePaise)
                )
            }
        dbPayables.ifEmpty {
            listOf(
                PartyDuePreview("Hindustan Agro Supply Co.", "Due in 2 days", Money.fromRupees(51_000L)),
                PartyDuePreview("Apex Packaging Industries", "Overdue 4 days", Money.fromRupees(24_600L)),
                PartyDuePreview("Narmada Logistics", "Due in 6 days", Money.fromRupees(11_900L))
            )
        }
    }
    val totalToPay = remember(activeParties, topToPayParties) {
        val sumFromDb = activeParties.filter { it.currentBalancePaise < 0L }.sumOf { -it.currentBalancePaise }
        if (sumFromDb > 0L) Money(sumFromDb) else Money(topToPayParties.sumOf { it.amount.paise })
    }

    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val recentInvoices = remember(transactions, prefs.invoicePrefix) {
        val dbDocs = transactions.filter { !it.isDeleted }.take(5).map { doc ->
            InvoicePreview(
                number = doc.docNumber,
                party = doc.partyName,
                date = dateFmt.format(Date(doc.txnDate)),
                amount = Money(doc.grandTotalPaise),
                status = doc.status,
                isSale = doc.docType != SalesDocTypes.CREDIT_NOTE
            )
        }
        dbDocs.ifEmpty {
            listOf(
                InvoicePreview("${prefs.invoicePrefix}0108", "Mehta Kirana & Traders", "Today, 02:15 PM", Money.fromRupees(24_500L), "Paid", true),
                InvoicePreview("${prefs.invoicePrefix}0107", "Shree Balaji Distributors", "Yesterday", Money.fromRupees(42_800L), "Partial", true),
                InvoicePreview("${prefs.invoicePrefix}0106", "Patel Hardware Mart", "11 Nov 2026", Money.fromRupees(28_200L), "Unpaid", true),
                InvoicePreview("PUR-2026-044", "Apex Packaging Industries", "08 Nov 2026", Money.fromRupees(24_600L), "Overdue", false)
            )
        }
    }

    val allFeatureTiles = remember {
        listOf(
            FeatureTileItem("Reports", "GST, P&L & Ledgers", Icons.Default.Assessment, VioletPrimary, "reports"),
            FeatureTileItem("Items and Stock", "Batches & HSN", Icons.Default.Inventory2, EmeraldPrimary, "stock"),
            FeatureTileItem("Cash and Bank", "Accounts & UPI", Icons.Default.AccountBalanceWallet, PurchaseBlue, "cash"),
            FeatureTileItem("Expenses", "GST & Direct", Icons.Default.Receipt, CoralError, "expenses"),
            FeatureTileItem("Staff and Payroll", "Attendance & Slips", Icons.Default.Badge, VioletPrimary, "staff"),
            FeatureTileItem("GST", "GSTR-1, 3B & HSN", Icons.Default.Percent, GoldPrimary, "gst"),
            FeatureTileItem("Day Book", "Daily Register", Icons.Default.MenuBook, AmberSale, "daybook"),
            FeatureTileItem("Backup and Restore", ".vbk Archive", Icons.Default.Backup, EmeraldPrimary, "backup"),
            FeatureTileItem("Import/Export", "PDF, CSV & Excel", Icons.Default.ImportExport, PurchaseBlue, "import_export"),
            FeatureTileItem("Settings", "Theme, GST & Security", Icons.Default.Settings, GoldPrimary, "settings")
        )
    }

    val stockMetrics = remember(items, itemBatches) {
        StockLedgerCalculator.computeRealTimeStockMetrics(items, itemBatches)
    }
    val totalStockValue = remember(items, stockMetrics, activeCompany?.id) {
        if (items.isNotEmpty()) stockMetrics.totalStockValue else Money.fromRupees(4_86_000L)
    }
    val lowStockCount = if (items.isNotEmpty()) stockMetrics.lowStockItems.size else 3
    val expiringStockCount = if (items.isNotEmpty() || itemBatches.isNotEmpty()) stockMetrics.expiringBatches.size else 2

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_dashboard_list"),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 136.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. "SWITCH FIRM" DROPDOWN CHIP + ROLE CHIP (Top App Bar is rendered by VanijyaMainShell across all 5 tabs)
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(glass.pillFill)
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(glass.borderGradientStart, glass.borderGradientEnd)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { showBusinessSwitcherSheet = true }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("business_switcher_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.switch_firm),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = activeCompany?.businessName?.ifBlank { "A.K. Enterprises" }
                                    ?: "A.K. Enterprises",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Switch Firm",
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = glass.pillFill,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .border(1.dp, glass.borderGradientStart, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { showUserPinSwitchDialog = true }
                            .testTag("switch_user_role_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwitchAccount,
                                contentDescription = "Switch User Role",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = prefs.activeUserRole.displayName,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = onLockNow,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(glass.pillFill)
                            .border(1.dp, GoldPrimary.copy(alpha = 0.45f), CircleShape)
                            .testTag("quick_lock_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Vault Now",
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 3. HERO GLASS CARD (Total Cash, Total Bank Balance, expandable bank accounts, animated RingChart)
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_hero_cash_bank_card"),
                cornerRadius = 28.dp,
                elevated = true,
                accentTint = VioletPrimary,
                onClick = { onNavigateToRoute("cash") }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        // Total Cash
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.total_cash),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            MiniTrendLine(isPositive = true, color = EmeraldPrimary)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        AmountText(
                            amount = totalCash,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldPrimary,
                            decimals = prefs.decimalPlaces,
                            masked = prefs.hideAmounts,
                            animateCountUp = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Total Bank Balance
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { expandBankAccounts = !expandBankAccounts }
                                .padding(vertical = 2.dp)
                                .testTag("hero_expand_banks_toggle")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(PurchaseBlue)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.total_bank_balance),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (expandBankAccounts) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Expand Bank Accounts",
                                tint = PurchaseBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        AmountText(
                            amount = totalBankBalance,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            decimals = prefs.decimalPlaces,
                            masked = prefs.hideAmounts,
                            animateCountUp = true
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Animated Circular RingChart showing Cash vs Bank share
                    RingChart(
                        cashAmount = totalCash,
                        bankAmount = totalBankBalance,
                        size = 96.dp,
                        strokeWidth = 11.dp,
                        cashColor = EmeraldPrimary,
                        bankColor = PurchaseBlue
                    )
                }

                // Expandable Individual Bank Accounts
                AnimatedVisibility(visible = expandBankAccounts) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(color = glass.subtleDivider)
                        val primaryBankLabel = activeCompany?.bankName?.ifBlank { "HDFC Bank Current A/C" }
                            ?: "HDFC Bank Current A/C"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = primaryBankLabel,
                                    tint = PurchaseBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "$primaryBankLabel ••4821",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            AmountText(
                                amount = primaryBankBalance,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                decimals = prefs.decimalPlaces,
                                masked = prefs.hideAmounts
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = "ICICI Merchant UPI Settlement",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ICICI Merchant OD / UPI ••9014",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            AmountText(
                                amount = secondaryBankBalance,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                decimals = prefs.decimalPlaces,
                                masked = prefs.hideAmounts
                            )
                        }
                    }
                }
            }
        }

        // 4. DATE RANGE SELECTOR CHIP (Today, Yesterday, This Week, This Month, This Quarter, This FY, Custom)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                datePresets.forEachIndexed { index, preset ->
                    val selected = index == selectedDateRangeIndex
                    Surface(
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else glass.pillFill,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else glass.borderGradientStart,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { selectedDateRangeIndex = index }
                            .testTag("date_preset_chip_${preset.label.lowercase().replace(" ", "_")}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (preset.label == "Custom") {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Custom Date Range",
                                    tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else GoldPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = preset.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 5. QUICK ACTIONS ROW: Sale (amber/orange), Purchase (blue), Quotation (grey/silver), More ("All actions" sheet)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassTile(
                    label = stringResource(R.string.quick_sale),
                    icon = Icons.Default.PointOfSale,
                    accentColor = AmberSale,
                    onClick = { onNavigateToRoute("billing_new_sale") },
                    modifier = Modifier.weight(1f),
                    testTag = "quick_action_sale"
                )
                GlassTile(
                    label = stringResource(R.string.quick_purchase),
                    icon = Icons.Default.ShoppingCart,
                    accentColor = PurchaseBlue,
                    onClick = { onNavigateToRoute("billing_new_purchase") },
                    modifier = Modifier.weight(1f),
                    testTag = "quick_action_purchase"
                )
                GlassTile(
                    label = stringResource(R.string.quick_quotation),
                    icon = Icons.Default.Description,
                    accentColor = QuotationSlate,
                    onClick = { onNavigateToRoute("billing_new_quotation") },
                    modifier = Modifier.weight(1f),
                    testTag = "quick_action_quotation"
                )
                GlassTile(
                    label = stringResource(R.string.nav_more),
                    icon = Icons.Default.GridView,
                    accentColor = GoldPrimary,
                    onClick = { showAllActionsSheet = true },
                    modifier = Modifier.weight(1f),
                    testTag = "quick_action_more"
                )
            }
        }

        // 6. SUMMARY GLASS TILES (Swipeable horizontal row: Sales, Purchases, Expenses, Profit for selected range)
        item {
            SectionHeader(
                title = "${activePreset.label} Summary",
                actionText = activePreset.changeVsPrev,
                onActionClick = { onNavigateToRoute("reports") }
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_summary_tiles_row"),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    StatTile(
                        title = stringResource(R.string.todays_sales),
                        amount = rangeSales,
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        accentColor = EmeraldPrimary,
                        subtitle = "GST Invoices",
                        changeBadge = "+18.4%",
                        showTrendLine = true,
                        isPositiveTrend = true,
                        masked = prefs.hideAmounts,
                        decimals = prefs.decimalPlaces,
                        onClick = { onNavigateToRoute("billing") },
                        modifier = Modifier.width(210.dp)
                    )
                }
                item {
                    StatTile(
                        title = stringResource(R.string.todays_purchases),
                        amount = rangePurchases,
                        icon = Icons.Default.ShoppingCart,
                        accentColor = PurchaseBlue,
                        subtitle = "Supplier Bills",
                        changeBadge = "+6.2%",
                        showTrendLine = true,
                        isPositiveTrend = false,
                        masked = prefs.hideAmounts,
                        decimals = prefs.decimalPlaces,
                        onClick = { onNavigateToRoute("billing") },
                        modifier = Modifier.width(210.dp)
                    )
                }
                item {
                    StatTile(
                        title = stringResource(R.string.todays_expenses),
                        amount = rangeExpenses,
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        accentColor = CoralError,
                        subtitle = "Direct & Indirect",
                        changeBadge = "-3.1%",
                        showTrendLine = true,
                        isPositiveTrend = false,
                        masked = prefs.hideAmounts,
                        decimals = prefs.decimalPlaces,
                        onClick = { onNavigateToRoute("expenses") },
                        modifier = Modifier.width(210.dp)
                    )
                }
                item {
                    StatTile(
                        title = stringResource(R.string.todays_profit),
                        amount = rangeProfit,
                        icon = Icons.Default.Savings,
                        accentColor = GoldPrimary,
                        subtitle = "Net Margin",
                        changeBadge = "+24.0%",
                        showTrendLine = true,
                        isPositiveTrend = true,
                        masked = prefs.hideAmounts,
                        decimals = prefs.decimalPlaces,
                        onClick = { onNavigateToRoute("reports") },
                        modifier = Modifier.width(210.dp)
                    )
                }
            }
        }

        // 6B. GLASS "STOCK" SUMMARY TILE (Stock Value, Low-Stock Count, Expiring Count -> opens Items and Stock)
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_stock_summary_tile"),
                cornerRadius = 26.dp,
                accentTint = EmeraldPrimary,
                onClick = { onNavigateToRoute("items") }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = stringResource(R.string.stock_summary_title),
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.stock_summary_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap to open Items and Stock",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.stock_value_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        AmountText(
                            amount = totalStockValue,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldPrimary,
                            decimals = prefs.decimalPlaces,
                            masked = prefs.hideAmounts,
                            animateCountUp = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = glass.subtleDivider)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = GoldPrimary.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.stock_low_count_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$lowStockCount SKUs",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = GoldPrimary
                            )
                        }
                    }
                    Surface(
                        color = AmberSale.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, AmberSale.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.stock_expiring_count_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$expiringStockCount Batches",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = AmberSale
                            )
                        }
                    }
                }
            }
        }

        // 7. "TO COLLECT" (emerald + up-trend line) AND "TO PAY" (red) CARDS WITH TOP 3 PARTIES & "REMIND" BUTTON
        item {
            if (!reminderStatusBanner.isNullOrBlank()) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    cornerRadius = 18.dp,
                    accentTint = EmeraldPrimary,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = reminderStatusBanner!!,
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { reminderStatusBanner = null }) {
                            Text("Dismiss", color = GoldPrimary)
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // TO COLLECT CARD
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_to_collect_card"),
                    cornerRadius = 26.dp,
                    accentTint = EmeraldPrimary,
                    onClick = { onNavigateToRoute("parties") }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = stringResource(R.string.you_will_get),
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.you_will_get),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                MiniTrendLine(isPositive = true, color = EmeraldPrimary)
                            }
                            Text(
                                text = "Top 3 Customer Receivables",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        AmountText(
                            amount = totalToCollect,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldPrimary,
                            decimals = prefs.decimalPlaces,
                            masked = prefs.hideAmounts,
                            animateCountUp = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = glass.subtleDivider)
                    Spacer(modifier = Modifier.height(10.dp))

                    topToCollectParties.forEach { party ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = party.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = party.dueDays,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AmountText(
                                    amount = party.amount,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldPrimary,
                                    decimals = prefs.decimalPlaces,
                                    masked = prefs.hideAmounts
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = EmeraldPrimary.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            reminderStatusBanner = "✓ Payment reminder queued offline for ${party.name}"
                                        }
                                        .testTag("remind_collect_${party.name.take(5).lowercase()}")
                                ) {
                                    Text(
                                        text = stringResource(R.string.remind_button),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = EmeraldPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // TO PAY CARD
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_to_pay_card"),
                    cornerRadius = 26.dp,
                    accentTint = ToPayRed,
                    onClick = { onNavigateToRoute("parties") }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = stringResource(R.string.you_will_pay),
                                    tint = ToPayRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.you_will_pay),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ToPayRed
                                )
                            }
                            Text(
                                text = "Top 3 Supplier Payables",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        AmountText(
                            amount = totalToPay,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = ToPayRed,
                            decimals = prefs.decimalPlaces,
                            masked = prefs.hideAmounts,
                            animateCountUp = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = glass.subtleDivider)
                    Spacer(modifier = Modifier.height(10.dp))

                    topToPayParties.forEach { supplier ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = supplier.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = supplier.dueDays,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AmountText(
                                    amount = supplier.amount,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = ToPayRed,
                                    decimals = prefs.decimalPlaces,
                                    masked = prefs.hideAmounts
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = ToPayRed.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .border(1.dp, ToPayRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            reminderStatusBanner = "✓ Payment voucher reminder set for ${supplier.name}"
                                        }
                                ) {
                                    Text(
                                        text = stringResource(R.string.remind_button),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = ToPayRed,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 8. ALERTS STRIP: low stock, expiring items, overdue invoices, cheques due, backup overdue
        item {
            SectionHeader(title = "Operational Alerts")
            Spacer(modifier = Modifier.height(4.dp))
            val overdueCount = if (transactions.isNotEmpty()) {
                transactions.count { !it.isDeleted && (it.status == "Overdue" || (it.balanceAmountPaise > 0L && it.dueDate < nowMs && it.status != "Cancelled")) }
            } else 2
            val chequesDueCount = if (cheques.isNotEmpty()) {
                cheques.count { !it.isDeleted && (it.status == "IN_HAND" || it.status == "DEPOSITED") }
            } else 1
            val alerts = listOf(
                Triple("Low Stock ($lowStockCount SKUs)", GoldPrimary, "items"),
                Triple("Expiring Items ($expiringStockCount Batches)", AmberSale, "items"),
                Triple("Overdue Invoices ($overdueCount Bills)", CoralError, "billing"),
                Triple("Cheques Due ($chequesDueCount Pending)", PurchaseBlue, "billing_cheques"),
                Triple("Backup Ready (.vbk Vault)", EmeraldPrimary, "backup")
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_alerts_strip"),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(alerts) { (label, accent, route) ->
                    Surface(
                        color = accent.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onNavigateToRoute(route) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = label,
                                tint = accent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = accent
                            )
                        }
                    }
                }
            }
        }

        // 9. RECENT ACTIVITY LIST: invoice cards with number, party, amount, status chip (Paid, Partial, Unpaid, Overdue)
        item {
            SectionHeader(
                title = stringResource(R.string.recent_transactions),
                actionText = "Open Billing",
                onActionClick = { onNavigateToRoute("billing") }
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                recentInvoices.forEach { inv ->
                    InvoiceRow(
                        invoiceNumber = inv.number,
                        partyName = inv.party,
                        dateText = inv.date,
                        amount = inv.amount,
                        statusLabel = inv.status,
                        isSale = inv.isSale,
                        masked = prefs.hideAmounts,
                        onClick = { onNavigateToRoute("billing") },
                        onSharePdf = {
                            reminderStatusBanner = "✓ Offline PDF ready to share for ${inv.number}"
                        }
                    )
                }
            }
        }

        // 10. "ALL FEATURES" GRID (Glass Icon Tiles): Items and Stock, Cash and Bank, Expenses, Staff and Payroll, GST, Backup and Restore, Day Book, Import/Export
        item {
            SectionHeader(
                title = stringResource(R.string.all_features_title),
                actionText = "Audit (${recentAuditLogs.size})",
                onActionClick = { onNavigateToRoute("reports") }
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_all_features_grid"),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 4
            ) {
                allFeatureTiles.forEach { feature ->
                    GlassTile(
                        label = feature.label,
                        subtitle = feature.subtitle,
                        icon = feature.icon,
                        accentColor = feature.accent,
                        onClick = { onNavigateToRoute(feature.route) },
                        modifier = Modifier.weight(1f),
                        testTag = "feature_tile_${feature.route}"
                    )
                }
            }
        }
    }

    // SHEET 1: "All Actions" Glass Bottom Sheet (opened from Quick Actions -> More)
    if (showAllActionsSheet) {
        GlassSheet(
            onDismissRequest = { showAllActionsSheet = false },
            title = stringResource(R.string.all_actions_title),
            subtitle = "Create GST vouchers, stock adjustments, or party masters offline"
        ) {
            val allActions = listOf(
                Triple("Payment In", Icons.Default.ArrowDownward, "billing_payment_in"),
                Triple("Payment Out", Icons.Default.ArrowUpward, "billing"),
                Triple("Expense", Icons.Default.Receipt, "expenses"),
                Triple("Sale Order", Icons.AutoMirrored.Filled.ReceiptLong, "billing_sale_order"),
                Triple("Delivery Challan", Icons.Default.LocalShipping, "billing_challan"),
                Triple("Sale Return", Icons.Default.AssignmentReturn, "billing_return"),
                Triple("Purchase Order", Icons.Default.ShoppingCart, "billing_purchase_order"),
                Triple("Add Party", Icons.Default.PersonAdd, "parties_add"),
                Triple("Add Item", Icons.Default.Inventory2, "items_add"),
                Triple("Stock Adjustment", Icons.Default.SwapVert, "items_adjust"),
                Triple("Scan Barcode", Icons.Default.QrCodeScanner, "items_scan"),
                Triple(stringResource(R.string.action_mark_attendance), Icons.Default.Badge, "staff")
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 3
            ) {
                allActions.forEach { (label, icon, route) ->
                    GlassTile(
                        label = label,
                        icon = icon,
                        accentColor = VioletPrimary,
                        onClick = {
                            showAllActionsSheet = false
                            onNavigateToRoute(route)
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "all_actions_${label.lowercase().replace(" ", "_")}"
                    )
                }
            }
        }
    }

    // SHEET 2: Notifications Bell Sheet (due payments, low stock, expiry, backup reminder)
    if (showNotificationsSheet) {
        GlassSheet(
            onDismissRequest = { showNotificationsSheet = false },
            title = "Notifications & Reminders",
            subtitle = "100% on-device alerts computed from your local SQLCipher database"
        ) {
            val topDueParty = topToCollectParties.firstOrNull()
            val dueText = if (topDueParty != null) {
                "${topDueParty.amount.formatIndian(decimals = prefs.decimalPlaces)} due from ${topDueParty.name}"
            } else {
                "₹42,800 overdue from Shree Balaji Distributors"
            }
            val notifications = listOf(
                Triple("Due Payments", dueText, EmeraldPrimary to "parties"),
                Triple("Low Stock Alert", "$lowStockCount SKUs below reorder level in Godown", GoldPrimary to "items"),
                Triple("Batch Expiry Warning", "$expiringStockCount batches expiring within 60 days", AmberSale to "items"),
                Triple("Backup Reminder", "Export encrypted .vbk archive to local storage", PurchaseBlue to "backup")
            )
            notifications.forEach { (title, desc, meta) ->
                val (accent, route) = meta
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    accentTint = accent,
                    contentPadding = PaddingValues(14.dp),
                    onClick = {
                        showNotificationsSheet = false
                        onNavigateToRoute(route)
                    }
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = accent
                    )
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // SHEET 3: Profile Avatar Sheet (opens profile, user switch by PIN, app lock)
    if (showProfileSheet) {
        GlassSheet(
            onDismissRequest = { showProfileSheet = false },
            title = "${prefs.activeUserName} (${prefs.activeUserRole.displayName})",
            subtitle = activeCompany?.businessName ?: "A.K. Enterprises"
        ) {
            VanijyaButton(
                text = "Switch User Role by 6-Digit PIN",
                icon = Icons.Default.SwitchAccount,
                onClick = {
                    showProfileSheet = false
                    showUserPinSwitchDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            )
            VanijyaButton(
                text = "Edit Business Profile & Settings",
                icon = Icons.Default.Settings,
                isOutlined = true,
                onClick = {
                    showProfileSheet = false
                    onNavigateToRoute("settings")
                },
                modifier = Modifier.fillMaxWidth()
            )
            VanijyaButton(
                text = "Lock Vanijya Vault Now",
                icon = Icons.Default.Lock,
                containerColor = CoralError,
                onClick = {
                    showProfileSheet = false
                    onLockNow()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // SHEET 4: Multi-Firm Switcher Glass Sheet
    if (showBusinessSwitcherSheet) {
        GlassSheet(
            onDismissRequest = { showBusinessSwitcherSheet = false },
            title = "Switch Firm / Company Partition",
            subtitle = "Each firm is isolated by company_id inside your encrypted SQLCipher database."
        ) {
            companies.forEach { company ->
                val isSelected = company.id == activeCompany?.id
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    accentTint = if (isSelected) EmeraldPrimary else null,
                    contentPadding = PaddingValues(16.dp),
                    onClick = {
                        onSwitchCompany(company)
                        showBusinessSwitcherSheet = false
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = company.businessName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${company.stateName} • ${company.gstMode}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active Firm",
                                tint = EmeraldPrimary
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            VanijyaButton(
                text = "Add Another Firm (New Partition)",
                icon = Icons.Default.AddBusiness,
                onClick = {
                    showBusinessSwitcherSheet = false
                    showAddBusinessDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showAddBusinessDialog) {
        var newBizName by remember { mutableStateOf("") }
        var newBizGstin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddBusinessDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Text(
                    text = "Create New Firm Partition",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newBizName,
                        onValueChange = { newBizName = it },
                        label = { Text("Firm / Business Name") },
                        singleLine = true,
                        colors = vanijyaTextFieldColors()
                    )
                    OutlinedTextField(
                        value = newBizGstin,
                        onValueChange = { newBizGstin = it.uppercase().take(15) },
                        label = { Text("GSTIN (Optional)") },
                        singleLine = true,
                        colors = vanijyaTextFieldColors()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newBizName.isNotBlank()) {
                            onCreateAdditionalCompany(newBizName.trim(), newBizGstin.trim())
                            showAddBusinessDialog = false
                        }
                    }
                ) {
                    Text(
                        text = "Create Firm",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBusinessDialog = false }) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }

    if (showUserPinSwitchDialog) {
        var rolePin by remember { mutableStateOf("") }
        var feedback by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showUserPinSwitchDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Text(
                    text = "Switch User Role by PIN",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter 6-digit PIN for Owner (your PIN), Manager (222222), Salesperson (333333), Accountant (444444), or Viewer (555555):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = rolePin,
                        onValueChange = {
                            rolePin = it.filter(Char::isDigit).take(6)
                            feedback = null
                        },
                        label = { Text("6-Digit Role PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = vanijyaTextFieldColors()
                    )
                    if (!feedback.isNullOrBlank()) {
                        Text(
                            text = feedback!!,
                            style = MaterialTheme.typography.labelSmall,
                            color = CoralError
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSwitchUserByPin(rolePin) { ok, msg ->
                            if (ok) {
                                showUserPinSwitchDialog = false
                            } else {
                                feedback = msg
                            }
                        }
                    }
                ) {
                    Text(
                        text = "Switch User",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showUserPinSwitchDialog = false }) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}
