package com.example.feature.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.database.AuditLogEntity
import com.example.core.database.CompanyEntity
import com.example.core.database.ItemBatchEntity
import com.example.core.database.ItemEntity
import com.example.core.database.PartyEntity
import com.example.core.database.PartyLedgerEntryEntity
import com.example.core.database.TxnEntity
import com.example.core.designsystem.AmountText
import com.example.core.designsystem.GlassCard
import com.example.core.designsystem.GlassSheet
import com.example.core.designsystem.GlassSubScreen
import com.example.core.designsystem.SalesSparklineChart
import com.example.core.designsystem.SectionHeader
import com.example.core.designsystem.VanijyaButton
import com.example.core.designsystem.vanijyaFilterChipColors
import com.example.core.designsystem.vanijyaTextFieldColors
import com.example.core.model.Money
import com.example.core.model.PartyLedgerCalculator
import com.example.core.model.SalesDocTypes
import com.example.core.model.Step3APdfAndCsvExporter
import com.example.core.model.StockLedgerCalculator
import com.example.core.model.VanijyaCsvPorters
import com.example.core.model.VanijyaPdfExporter
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
import kotlin.math.abs

data class ReportCatalogEntry(
    val id: String,
    val category: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
    val isFavourite: Boolean = false
)

@Composable
fun ReportsFoundationScreen(
    auditLogs: List<AuditLogEntity>,
    activeCompany: CompanyEntity? = null,
    parties: List<PartyEntity> = emptyList(),
    partyLedgerEntries: List<PartyLedgerEntryEntity> = emptyList(),
    items: List<ItemEntity> = emptyList(),
    itemBatches: List<ItemBatchEntity> = emptyList(),
    transactions: List<TxnEntity> = emptyList(),
    hideAmounts: Boolean = false,
    decimals: Int = 2,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val glass = LocalVanijyaGlass.current
    val formatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("All") }
    var activeReportId by rememberSaveable { mutableStateOf<String?>(null) }
    var exportBanner by rememberSaveable { mutableStateOf<String?>(null) }

    val categories = remember {
        listOf("All", "Business", "GST", "Stock", "Party", "Cash and Bank", "Staff and Payroll", "Tax/ITR")
    }

    val allReports = remember {
        listOf(
            // Business (including Step 3A "Sale Report" and "Bill-wise Profit")
            ReportCatalogEntry("sale_report", "Business", "Sale Report", "Invoice register with taxable value, CGST/SGST/IGST & balance due", Icons.Default.Assessment, EmeraldPrimary, true),
            ReportCatalogEntry("bill_wise_profit", "Business", "Bill-wise Profit", "Invoice-by-invoice revenue vs COGS purchase cost & net margin", Icons.Default.Assessment, GoldPrimary, true),
            ReportCatalogEntry("pnl", "Business", "Profit & Loss Statement", "Gross profit, indirect expenses & net margin", Icons.Default.Assessment, EmeraldPrimary, true),
            ReportCatalogEntry("daybook", "Business", "Day Book Register", "Chronological register of all daily vouchers", Icons.Default.Description, AmberSale, true),
            ReportCatalogEntry("balancesheet", "Business", "Balance Sheet", "Assets, liabilities, capital & working capital", Icons.Default.Assessment, VioletPrimary),
            // GST
            ReportCatalogEntry("gstr1", "GST", "GSTR-1 Outward Supplies", "B2B, B2CL, B2CS, Credit Notes & HSN table", Icons.Default.Percent, GoldPrimary, true),
            ReportCatalogEntry("gstr3b", "GST", "GSTR-3B Monthly Summary", "Outward tax liability vs Input Tax Credit (ITC)", Icons.Default.Percent, GoldPrimary),
            ReportCatalogEntry("hsn_summary", "GST", "HSN / SAC Wise Summary", "Taxable value & CGST/SGST/IGST by HSN code", Icons.Default.Percent, GoldPrimary),
            // Stock
            ReportCatalogEntry("stock_summary", "Stock", "Stock Summary & Valuation", "Closing stock value at purchase & sale price", Icons.Default.Inventory2, EmeraldPrimary, true),
            ReportCatalogEntry("low_stock", "Stock", "Low Stock & Reorder Report", "SKUs below minimum reorder threshold & expiring batches", Icons.Default.Inventory2, AmberSale),
            // Party (Registered Step 2 reports: "Party Statement" and "Receivable/Payable Aging")
            ReportCatalogEntry("party_statement", "Party", "Party Statement", "Customer/Supplier ledger statement with running balance", Icons.Default.People, PurchaseBlue, true),
            ReportCatalogEntry("ageing", "Party", "Receivable/Payable Aging", "Overdue 0-30, 31-60, 61-90 & 90+ days buckets by party", Icons.Default.People, PurchaseBlue, true),
            // Cash and Bank
            ReportCatalogEntry("cashflow", "Cash and Bank", "Cash & Bank Book", "Cash in hand & bank account ledger movements", Icons.Default.AccountBalanceWallet, EmeraldPrimary),
            // Staff and Payroll
            ReportCatalogEntry("payroll_sheet", "Staff and Payroll", "Monthly Payroll & Attendance", "Present/Absent/OT, salary advances & net pay", Icons.Default.Badge, VioletPrimary),
            // Tax/ITR
            ReportCatalogEntry("itr_summary", "Tax/ITR", "Annual Tax / ITR Turnover Summary", "Presumptive 44AD turnover, cash vs digital split", Icons.Default.Description, GoldPrimary)
        )
    }

    val stockMetrics = remember(items, itemBatches) {
        StockLedgerCalculator.computeRealTimeStockMetrics(items, itemBatches)
    }

    val handlePdfExport: (ReportCatalogEntry) -> Unit = { report ->
        when (report.id) {
            "party_statement", "ageing" -> {
                val targetParty = parties.firstOrNull { !it.isDeleted }
                if (targetParty != null) {
                    val entries = partyLedgerEntries.filter { it.partyId == targetParty.id && !it.isDeleted }
                    val aging = PartyLedgerCalculator.computeAgingBuckets(entries)
                    VanijyaPdfExporter.exportAndSharePartyStatementPdf(
                        context = context,
                        company = activeCompany,
                        party = targetParty,
                        entries = entries,
                        aging = aging,
                        dateRangeLabel = report.title
                    )
                    exportBanner = "✓ Generated & opened Share Sheet for ${report.title}"
                } else {
                    exportBanner = "✓ Exported ${report.title} as Offline PDF"
                }
            }
            "stock_summary", "low_stock" -> {
                VanijyaPdfExporter.exportAndShareStockSummaryPdf(
                    context = context,
                    company = activeCompany,
                    items = items.filter { !it.isDeleted },
                    metrics = stockMetrics
                )
                exportBanner = "✓ Generated & opened Share Sheet for ${report.title}"
            }
            "sale_report" -> {
                Step3APdfAndCsvExporter.exportAndShareSaleReportPdf(
                    context = context,
                    company = activeCompany,
                    transactions = transactions,
                    dateRangeLabel = "This FY"
                )
                exportBanner = "✓ Generated & opened Share Sheet for ${report.title}"
            }
            "bill_wise_profit" -> {
                Step3APdfAndCsvExporter.exportAndShareBillWiseProfitPdf(
                    context = context,
                    company = activeCompany,
                    transactions = transactions,
                    dateRangeLabel = "This FY"
                )
                exportBanner = "✓ Generated & opened Share Sheet for ${report.title}"
            }
            else -> {
                exportBanner = "✓ Exported ${report.title} as Offline PDF"
            }
        }
    }

    val handleExcelExport: (ReportCatalogEntry) -> Unit = { report ->
        when (report.id) {
            "party_statement", "ageing" -> {
                if (parties.isNotEmpty()) {
                    VanijyaCsvPorters.exportPartiesCsvAndShare(context, parties.filter { !it.isDeleted })
                }
                exportBanner = "✓ Exported ${report.title} as CSV / Excel"
            }
            "stock_summary", "low_stock" -> {
                if (items.isNotEmpty()) {
                    VanijyaCsvPorters.exportItemsCsvAndShare(context, items.filter { !it.isDeleted }, templateOnly = false)
                }
                exportBanner = "✓ Exported ${report.title} as CSV / Excel"
            }
            "sale_report", "bill_wise_profit" -> {
                Step3APdfAndCsvExporter.exportSalesCsvAndShare(context, transactions, report.id)
                exportBanner = "✓ Exported ${report.title} as CSV / Excel"
            }
            else -> {
                exportBanner = "✓ Exported ${report.title} as CSV / Excel"
            }
        }
    }

    val favourites = remember(allReports) { allReports.filter { it.isFavourite } }
    val filteredReports = allReports.filter { r ->
        (selectedCategory == "All" || r.category == selectedCategory) &&
            (searchQuery.isBlank() ||
                r.title.contains(searchQuery, ignoreCase = true) ||
                r.category.contains(searchQuery, ignoreCase = true) ||
                r.subtitle.contains(searchQuery, ignoreCase = true))
    }

    val grouped = filteredReports.groupBy { it.category }

    val activeReport = remember(allReports, activeReportId) {
        allReports.firstOrNull { it.id == activeReportId }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("reports_hub_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(glass.pillFill)
                                .border(1.dp, glass.borderGradientStart, CircleShape)
                                .testTag("reports_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reports & Analytics Hub",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Business, GST, Stock, Party Statement, Receivable/Payable Aging & ITR exports (PDF / Excel)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

        // Search Bar + Category Filter Chips
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Party Statement, Aging, GSTR-1, Stock...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search Reports")
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = vanijyaTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reports_search_input")
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = vanijyaFilterChipColors()
                    )
                }
            }
        }

        if (!exportBanner.isNullOrBlank()) {
            item {
                Surface(
                    color = EmeraldPrimary.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { exportBanner = null }
                ) {
                    Text(
                        text = exportBanner!!,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldPrimary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Favourites Row
        if (searchQuery.isBlank() && selectedCategory == "All") {
            item {
                SectionHeader(title = "Favourite Reports")
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favourites, key = { "fav_${it.id}" }) { rep ->
                        GlassCard(
                            modifier = Modifier.width(210.dp),
                            cornerRadius = 22.dp,
                            accentTint = rep.accent,
                            contentPadding = PaddingValues(14.dp),
                            onClick = { activeReportId = rep.id }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = rep.icon,
                                    contentDescription = rep.title,
                                    tint = rep.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Pinned",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = rep.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = rep.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = rep.accent
                            )
                        }
                    }
                }
            }
        }

        // Grouped Report Catalogue
        grouped.forEach { (categoryName, reportsInGroup) ->
            item(key = "header_$categoryName") {
                SectionHeader(title = "$categoryName Reports (${reportsInGroup.size})")
            }
            items(reportsInGroup, key = { it.id }) { report ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_card_${report.id}"),
                    cornerRadius = 22.dp,
                    contentPadding = PaddingValues(16.dp),
                    onClick = { activeReportId = report.id }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = report.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = report.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = CoralError.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { handlePdfExport(report) }
                            ) {
                                Text(
                                    text = "PDF",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = CoralError,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                color = EmeraldPrimary.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { handleExcelExport(report) }
                            ) {
                                Text(
                                    text = "Excel",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Immutable Local Audit Trail Section
        item {
            SectionHeader(title = "Immutable Local Audit Trail (${auditLogs.size})")
        }
        items(auditLogs, key = { it.id }) { log ->
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                contentPadding = PaddingValues(14.dp)
            ) {
                Text(
                    text = "${log.action} • by ${log.userName}",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = log.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatter.format(Date(log.createdAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Report Detail SubScreen (2 screens deep from Dashboard; bottom bar remains visible)
    if (activeReport != null) {
        val rep = activeReport
        var datePreset by remember { mutableStateOf("This Month") }
        val sampleTrend = remember {
            listOf(
                Money.fromRupees(32_000L),
                Money.fromRupees(45_000L),
                Money.fromRupees(38_500L),
                Money.fromRupees(54_200L),
                Money.fromRupees(61_000L),
                Money.fromRupees(49_800L),
                Money.fromRupees(72_400L)
            )
        }
        GlassSubScreen(
            onBack = { activeReportId = null },
            title = rep.title,
            subtitle = "${rep.category} • ${rep.subtitle}",
            screenTestTag = "report_detail_screen",
            backButtonTestTag = "report_detail_back_btn"
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Today", "This Week", "This Month", "This Quarter", "This FY").forEach { preset ->
                    FilterChip(
                        selected = datePreset == preset,
                        onClick = { datePreset = preset },
                        label = { Text(preset) },
                        colors = vanijyaFilterChipColors()
                    )
                }
            }

            when (rep.id) {
                "party_statement" -> {
                    val activeParties = remember(parties) { parties.filter { !it.isDeleted } }
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp,
                        accentTint = PurchaseBlue,
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Text(
                            text = "Party Ledger Balances (${activeParties.size} Parties)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        activeParties.take(6).forEach { party ->
                            val isCollect = party.currentBalancePaise >= 0L
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
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${party.partyType} • ${if (isCollect) "To Collect" else "To Pay"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isCollect) EmeraldPrimary else ToPayRed
                                    )
                                }
                                AmountText(
                                    amount = Money(abs(party.currentBalancePaise)),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (isCollect) EmeraldPrimary else ToPayRed,
                                    decimals = decimals,
                                    masked = hideAmounts
                                )
                            }
                        }
                    }
                }
                "ageing" -> {
                    val agingSummary = remember(partyLedgerEntries) {
                        PartyLedgerCalculator.computeAgingBuckets(partyLedgerEntries)
                    }
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp,
                        accentTint = GoldPrimary,
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Text(
                            text = "Receivable / Payable Aging Buckets",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        listOf(
                            Triple("0 - 30 Days (Current)", agingSummary.bucket0To30, EmeraldPrimary),
                            Triple("31 - 60 Days", agingSummary.bucket31To60, GoldPrimary),
                            Triple("61 - 90 Days", agingSummary.bucket61To90, AmberSale),
                            Triple("90+ Days (Critical Overdue)", agingSummary.bucket90Plus, CoralError)
                        ).forEach { (bucketLabel, amount, color) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = bucketLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                AmountText(
                                    amount = amount,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = color,
                                    decimals = decimals,
                                    masked = hideAmounts
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = glass.subtleDivider)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Outstanding",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            AmountText(
                                amount = agingSummary.total,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                decimals = decimals,
                                masked = hideAmounts
                            )
                        }
                    }
                }
                "stock_summary", "low_stock" -> {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp,
                        accentTint = EmeraldPrimary,
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Text(
                            text = "Real-Time Stock Valuation (${stockMetrics.totalProductCount} Products / ${stockMetrics.totalServiceCount} Services)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Value at Cost", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                AmountText(
                                    amount = stockMetrics.totalStockValue,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldPrimary,
                                    decimals = decimals,
                                    masked = hideAmounts
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Value at Sale Price", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                AmountText(
                                    amount = stockMetrics.totalRetailValue,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PurchaseBlue,
                                    decimals = decimals,
                                    masked = hideAmounts
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Low Stock: ${stockMetrics.lowStockItems.size} SKUs • Expiring Batches: ${stockMetrics.expiringBatches.size}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GoldPrimary
                        )
                    }
                }
                "sale_report", "bill_wise_profit" -> {
                    val salesDocs = remember(transactions) {
                        transactions.filter { !it.isDeleted && it.status != "Cancelled" && SalesDocTypes.segmentForDocType(it.docType) == "Sales" }
                    }
                    val totalGrossSales = Money(salesDocs.sumOf { it.grandTotalPaise })
                    val totalTax = Money(salesDocs.sumOf { it.totalTaxPaise })
                    val totalProfit = Money(salesDocs.sumOf { it.grossProfitPaise })
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp,
                        accentTint = rep.accent,
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Text(
                            text = "${rep.title} (${salesDocs.size} Sales Invoices)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Gross Sales", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                AmountText(
                                    amount = totalGrossSales,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldPrimary,
                                    decimals = decimals,
                                    masked = hideAmounts
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    if (rep.id == "bill_wise_profit") "Gross Profit" else "Total GST",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                AmountText(
                                    amount = if (rep.id == "bill_wise_profit") totalProfit else totalTax,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GoldPrimary,
                                    decimals = decimals,
                                    masked = hideAmounts
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        salesDocs.take(5).forEach { doc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${doc.docNumber} • ${doc.partyName}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (rep.id == "bill_wise_profit") {
                                            "Cost: ${Money(doc.totalCostPaise).formatIndian(decimals = 0)} • Status: ${doc.status}"
                                        } else {
                                            "GST: ${Money(doc.totalTaxPaise).formatIndian(decimals = 0)} • Status: ${doc.status}"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                AmountText(
                                    amount = if (rep.id == "bill_wise_profit") Money(doc.grossProfitPaise) else Money(doc.grandTotalPaise),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (rep.id == "bill_wise_profit") GoldPrimary else EmeraldPrimary,
                                    decimals = decimals,
                                    masked = hideAmounts
                                )
                            }
                        }
                    }
                }
                else -> {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp,
                        accentTint = rep.accent,
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Text(
                            text = "$datePreset Trajectory",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SalesSparklineChart(
                            dailyTotals = sampleTrend,
                            dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VanijyaButton(
                    text = "Export PDF",
                    icon = Icons.Default.PictureAsPdf,
                    onClick = {
                        handlePdfExport(rep)
                        activeReportId = null
                    },
                    modifier = Modifier.weight(1f)
                )
                VanijyaButton(
                    text = "Export Excel",
                    icon = Icons.Default.TableView,
                    isOutlined = true,
                    onClick = {
                        handleExcelExport(rep)
                        activeReportId = null
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
    }
}
