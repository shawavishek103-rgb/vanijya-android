package com.example.feature.parties

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.database.CompanyEntity
import com.example.core.database.PartyAddressEntity
import com.example.core.database.PartyCustomFieldEntity
import com.example.core.database.PartyEntity
import com.example.core.database.PartyGroupEntity
import com.example.core.database.PartyLedgerEntryEntity
import com.example.core.database.PriceListEntity
import com.example.core.designsystem.AmountText
import com.example.core.designsystem.GlassCard
import com.example.core.designsystem.GlassFullScreenEditor
import com.example.core.designsystem.GlassSheet
import com.example.core.designsystem.GlassSubScreen
import com.example.core.designsystem.MiniTrendLine
import com.example.core.designsystem.SectionHeader
import com.example.core.designsystem.SegmentedTabs
import com.example.core.designsystem.VanijyaButton
import com.example.core.designsystem.VanijyaEmptyState
import com.example.core.designsystem.VanijyaExtendedFab
import com.example.core.designsystem.vanijyaFilterChipColors
import com.example.core.designsystem.vanijyaTextFieldColors
import com.example.core.model.CreditLimitCheckResult
import com.example.core.model.Money
import com.example.core.model.PartyImportPreviewRow
import com.example.core.model.PartyLedgerCalculator
import com.example.core.model.Validators
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
import java.util.UUID
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PartiesFoundationScreen(
    hideAmounts: Boolean = false,
    decimals: Int = 2,
    activeCompany: CompanyEntity? = null,
    partiesList: List<PartyEntity> = emptyList(),
    partyAddresses: List<PartyAddressEntity> = emptyList(),
    partyGroups: List<PartyGroupEntity> = emptyList(),
    partyCustomFields: List<PartyCustomFieldEntity> = emptyList(),
    partyLedgerEntries: List<PartyLedgerEntryEntity> = emptyList(),
    priceLists: List<PriceListEntity> = emptyList(),
    openAddPartyOnLaunch: Boolean = false,
    onConsumeOpenAddParty: () -> Unit = {},
    onFullScreenEditorOpenChange: (Boolean) -> Unit = {},
    reselectTrigger: Int = 0,
    onSaveParty: (PartyEntity, List<PartyAddressEntity>, List<PartyCustomFieldEntity>) -> Unit = { _, _, _ -> },
    onRecordLedgerEntry: (PartyLedgerEntryEntity) -> Unit = {},
    onDeleteParties: (List<String>) -> Unit = {},
    onBulkImportParties: (List<PartyImportPreviewRow>, (Int) -> Unit) -> Unit = { _, cb -> cb(0) }
) {
    val context = LocalContext.current
    val glass = LocalVanijyaGlass.current
    val companyId = activeCompany?.id ?: "default_company"

    // Fallback in-memory list when rendered standalone in unit tests without Room
    val fallbackParties = remember {
        val now = System.currentTimeMillis()
        mutableStateListOf(
            PartyEntity(
                id = "1", companyId = companyId, name = "Apex Packaging Industries",
                partyType = "SUPPLIER", phone = "9820112233", gstin = "27AAACA1234A1Z5",
                pan = "AAACA1234A", stateCode = "27", stateName = "Maharashtra",
                groupName = "FMCG & Raw Suppliers", openingBalancePaise = 24_600_00L,
                isToCollect = false, currentBalancePaise = -24_600_00L, creditLimitPaise = 1_50_000_00L,
                creditDays = 30, createdAt = now - 52 * 86_400_000L, updatedAt = now
            ),
            PartyEntity(
                id = "2", companyId = companyId, name = "Hindustan Agro Supply Co.",
                partyType = "SUPPLIER", phone = "9820445566", gstin = "27AABCH5678B1Z2",
                pan = "AABCH5678B", stateCode = "27", stateName = "Maharashtra",
                groupName = "FMCG & Raw Suppliers", openingBalancePaise = 51_000_00L,
                isToCollect = false, currentBalancePaise = -51_000_00L, creditLimitPaise = 2_00_000_00L,
                creditDays = 45, createdAt = now - 22 * 86_400_000L, updatedAt = now
            ),
            PartyEntity(
                id = "3", companyId = companyId, name = "Kaveri Supermart",
                partyType = "CUSTOMER", phone = "9811223344", gstin = "27AADCK9012C1Z9",
                pan = "AADCK9012C", stateCode = "27", stateName = "Maharashtra",
                groupName = "Retail Customers", openingBalancePaise = 18_900_00L,
                isToCollect = true, currentBalancePaise = 18_900_00L, creditLimitPaise = 50_000_00L,
                creditDays = 15, createdAt = now - 98 * 86_400_000L, updatedAt = now
            ),
            PartyEntity(
                id = "4", companyId = companyId, name = "Mehta Kirana & Traders",
                partyType = "CUSTOMER", phone = "9876501234", gstin = "27AAPFM3456D1Z9",
                pan = "AAPFM3456D", stateCode = "27", stateName = "Maharashtra",
                groupName = "Wholesale Buyers", openingBalancePaise = 64_500_00L,
                isToCollect = true, currentBalancePaise = 64_500_00L, creditLimitPaise = 75_000_00L,
                creditDays = 15, createdAt = now - 18 * 86_400_000L, updatedAt = now
            ),
            PartyEntity(
                id = "5", companyId = companyId, name = "Narmada Logistics",
                partyType = "SUPPLIER", phone = "9898776655", gstin = "24AABCN7890E1Z4",
                pan = "AABCN7890E", stateCode = "24", stateName = "Gujarat",
                groupName = "Transport & Logistics", openingBalancePaise = 11_900_00L,
                isToCollect = false, currentBalancePaise = -11_900_00L, creditLimitPaise = 1_00_000_00L,
                creditDays = 15, createdAt = now - 12 * 86_400_000L, updatedAt = now
            ),
            PartyEntity(
                id = "6", companyId = companyId, name = "Patel Hardware Mart",
                partyType = "BOTH", phone = "9765432109", gstin = "27AAJFP4321F1Z8",
                pan = "AAJFP4321F", stateCode = "27", stateName = "Maharashtra",
                groupName = "Retail Customers", openingBalancePaise = 28_200_00L,
                isToCollect = true, currentBalancePaise = 28_200_00L, creditLimitPaise = 50_000_00L,
                creditDays = 30, createdAt = now - 72 * 86_400_000L, updatedAt = now
            ),
            PartyEntity(
                id = "7", companyId = companyId, name = "Shree Balaji Distributors",
                partyType = "CUSTOMER", phone = "9823098765", gstin = "27AABCS9876G1Z3",
                pan = "AABCS9876G", stateCode = "27", stateName = "Maharashtra",
                groupName = "Wholesale Buyers", openingBalancePaise = 42_800_00L,
                isToCollect = true, currentBalancePaise = 42_800_00L, creditLimitPaise = 40_000_00L,
                creditDays = 30, createdAt = now - 45 * 86_400_000L, updatedAt = now
            )
        )
    }

    val effectiveParties = if (partiesList.isNotEmpty()) partiesList else fallbackParties

    val tabs = remember { listOf("Customers", "Suppliers", "All") }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedGroupFilter by rememberSaveable { mutableStateOf("All Groups") }
    var selectedBalanceFilter by rememberSaveable { mutableStateOf("All") } // All, To Collect, To Pay, Over Limit
    var isMultiSelectMode by rememberSaveable { mutableStateOf(false) }
    val selectedPartyIds = remember { mutableStateListOf<String>() }

    val listState = rememberLazyListState()
    var editingParty by remember { mutableStateOf<PartyEntity?>(null) }
    var showPartyFormSheet by rememberSaveable { mutableStateOf(false) }
    var activeDetailPartyId by rememberSaveable { mutableStateOf<String?>(null) }
    var showPartyDeepAgingScreen by rememberSaveable { mutableStateOf(false) }
    val activeDetailParty = remember(effectiveParties, activeDetailPartyId) {
        effectiveParties.firstOrNull { it.id == activeDetailPartyId }
    }
    var activeReminderParty by remember { mutableStateOf<PartyEntity?>(null) }
    var showAgingOverviewSheet by rememberSaveable { mutableStateOf(false) }
    var showCsvImportExportSheet by rememberSaveable { mutableStateOf(false) }
    var showContactsImportSheet by rememberSaveable { mutableStateOf(false) }
    var actionFeedback by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(showPartyFormSheet) {
        onFullScreenEditorOpenChange(showPartyFormSheet)
    }

    DisposableEffect(Unit) {
        onDispose {
            onFullScreenEditorOpenChange(false)
        }
    }

    LaunchedEffect(reselectTrigger) {
        if (reselectTrigger > 0) {
            showPartyDeepAgingScreen = false
            activeDetailPartyId = null
            showPartyFormSheet = false
            listState.scrollToItem(0)
        }
    }

    LaunchedEffect(openAddPartyOnLaunch) {
        if (openAddPartyOnLaunch) {
            editingParty = null
            showPartyFormSheet = true
            onConsumeOpenAddParty()
        }
    }

    // Contact Picker + Optional READ_CONTACTS permission launcher
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri: Uri? ->
        if (contactUri != null) {
            runCatching {
                context.contentResolver.query(contactUri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        val name = if (nameIdx >= 0) cursor.getString(nameIdx) ?: "" else ""
                        if (name.isNotBlank()) {
                            editingParty = PartyEntity(
                                id = UUID.randomUUID().toString(),
                                companyId = companyId,
                                name = name,
                                partyType = "CUSTOMER",
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                            showPartyFormSheet = true
                            actionFeedback = "✓ Imported '$name' from phone contacts"
                        }
                    }
                }
            }.onFailure {
                showContactsImportSheet = true
            }
        }
    }

    val readContactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            runCatching { contactPickerLauncher.launch(null) }
                .onFailure { showContactsImportSheet = true }
        } else {
            showContactsImportSheet = true
        }
    }

    val groupNames = remember(partyGroups, effectiveParties) {
        val fromEntities = partyGroups.map { it.name }
        val fromParties = effectiveParties.map { it.groupName }.filter { it.isNotBlank() }
        (listOf("All Groups") + (fromEntities + fromParties).distinct())
    }

    val filteredParties = effectiveParties
        .filter { party ->
            val matchesRole = when (selectedTab) {
                0 -> party.partyType == "CUSTOMER" || party.partyType == "BOTH"
                1 -> party.partyType == "SUPPLIER" || party.partyType == "BOTH"
                else -> true
            }
            val matchesGroup = selectedGroupFilter == "All Groups" ||
                party.groupName.equals(selectedGroupFilter, ignoreCase = true)
            val matchesBalance = when (selectedBalanceFilter) {
                "To Collect" -> party.currentBalancePaise > 0L
                "To Pay" -> party.currentBalancePaise < 0L
                "Over Credit Limit" -> party.creditLimitPaise > 0L && party.currentBalancePaise > party.creditLimitPaise
                else -> true
            }
            val matchesQuery = searchQuery.isBlank() ||
                party.name.contains(searchQuery, ignoreCase = true) ||
                party.phone.contains(searchQuery) ||
                party.gstin.contains(searchQuery, ignoreCase = true) ||
                party.groupName.contains(searchQuery, ignoreCase = true)
            matchesRole && matchesGroup && matchesBalance && matchesQuery
        }
        .sortedBy { it.name.uppercase() }

    val totalToCollect = Money(
        effectiveParties.filter { it.currentBalancePaise > 0L }.sumOf { it.currentBalancePaise }
    )
    val totalToPay = Money(
        effectiveParties.filter { it.currentBalancePaise < 0L }.sumOf { abs(it.currentBalancePaise) }
    )

    // Compute overall aging across all parties
    val overallAging = remember(partyLedgerEntries, effectiveParties) {
        val entriesToUse = if (partyLedgerEntries.isNotEmpty()) {
            partyLedgerEntries
        } else {
            effectiveParties.map { p ->
                PartyLedgerEntryEntity(
                    id = "${p.id}_open",
                    companyId = p.companyId,
                    partyId = p.id,
                    entryDate = p.createdAt,
                    voucherType = "OPENING_BALANCE",
                    voucherNumber = "OPEN-BAL",
                    debitPaise = if (p.currentBalancePaise > 0L) p.currentBalancePaise else 0L,
                    creditPaise = if (p.currentBalancePaise < 0L) abs(p.currentBalancePaise) else 0L,
                    runningBalancePaise = p.currentBalancePaise,
                    createdAt = p.createdAt,
                    updatedAt = p.updatedAt
                )
            }
        }
        PartyLedgerCalculator.computeAgingBuckets(entriesToUse)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("parties_hub_screen")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("parties_lazy_column"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 180.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Parties & Khata Ledgers",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Alphabetical Customer & Supplier masters, GSTIN validator, Aging & PDF Statements",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    var showHeaderMoreMenu by remember { mutableStateOf(false) }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showContactsImportSheet = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(glass.pillFill)
                                .border(1.dp, glass.borderGradientStart, CircleShape)
                                .testTag("parties_import_contacts_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContactPhone,
                                contentDescription = "Import from Contacts",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { showAgingOverviewSheet = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(glass.pillFill)
                                .border(1.dp, glass.borderGradientStart, CircleShape)
                                .testTag("parties_aging_analysis_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Receivable / Payable Aging (0-30/60/90+)",
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Box {
                            IconButton(
                                onClick = { showHeaderMoreMenu = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isMultiSelectMode) VioletPrimary.copy(alpha = 0.2f) else glass.pillFill)
                                    .border(1.dp, if (isMultiSelectMode) VioletPrimary else glass.borderGradientStart, CircleShape)
                                    .testTag("parties_header_more_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Party Actions",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showHeaderMoreMenu,
                                onDismissRequest = { showHeaderMoreMenu = false },
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Import / Export CSV & Excel", color = MaterialTheme.colorScheme.onSurface) },
                                    leadingIcon = { Icon(Icons.Default.FileUpload, contentDescription = null, tint = PurchaseBlue) },
                                    onClick = {
                                        showHeaderMoreMenu = false
                                        showCsvImportExportSheet = true
                                    },
                                    modifier = Modifier.testTag("parties_csv_import_export_btn")
                                )
                                DropdownMenuItem(
                                    text = { Text("Aging Analysis (0-30/60/90+)", color = MaterialTheme.colorScheme.onSurface) },
                                    leadingIcon = { Icon(Icons.Default.History, contentDescription = null, tint = GoldPrimary) },
                                    onClick = {
                                        showHeaderMoreMenu = false
                                        showAgingOverviewSheet = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (isMultiSelectMode) "Exit Multi-Select Mode" else "Multi-Select Parties",
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    leadingIcon = { Icon(Icons.Default.Checklist, contentDescription = null, tint = VioletPrimary) },
                                    onClick = {
                                        showHeaderMoreMenu = false
                                        isMultiSelectMode = !isMultiSelectMode
                                        if (!isMultiSelectMode) selectedPartyIds.clear()
                                    },
                                    modifier = Modifier.testTag("parties_multiselect_toggle_btn")
                                )
                            }
                        }
                    }
                }
            }

            // Summary Header: To Collect vs To Pay
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        cornerRadius = 22.dp,
                        accentTint = EmeraldPrimary,
                        contentPadding = PaddingValues(14.dp),
                        onClick = {
                            selectedBalanceFilter = if (selectedBalanceFilter == "To Collect") "All" else "To Collect"
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "To Collect",
                                style = MaterialTheme.typography.labelMedium,
                                color = EmeraldPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            MiniTrendLine(isPositive = true, color = EmeraldPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        AmountText(
                            amount = totalToCollect,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldPrimary,
                            masked = hideAmounts,
                            decimals = decimals,
                            animateCountUp = true
                        )
                    }
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        cornerRadius = 22.dp,
                        accentTint = ToPayRed,
                        contentPadding = PaddingValues(14.dp),
                        onClick = {
                            selectedBalanceFilter = if (selectedBalanceFilter == "To Pay") "All" else "To Pay"
                        }
                    ) {
                        Text(
                            text = "To Pay",
                            style = MaterialTheme.typography.labelMedium,
                            color = ToPayRed
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AmountText(
                            amount = totalToPay,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = ToPayRed,
                            masked = hideAmounts,
                            decimals = decimals,
                            animateCountUp = true
                        )
                    }
                }
            }

            // Segmented Customers / Suppliers / All
            item {
                SegmentedTabs(
                    tabs = tabs,
                    selectedIndex = selectedTab,
                    onTabSelected = { selectedTab = it },
                    scrollable = false,
                    testTagPrefix = "parties_segment"
                )
            }

            // Search Bar + Group & Balance Filter Chips
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search party name, phone, GSTIN, or group") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search Parties")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("parties_search_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "To Collect", "To Pay", "Over Credit Limit").forEach { bf ->
                        FilterChip(
                            selected = selectedBalanceFilter == bf,
                            onClick = { selectedBalanceFilter = bf },
                            label = { Text(bf) },
                            colors = vanijyaFilterChipColors()
                        )
                    }
                    groupNames.forEach { grp ->
                        if (grp != "All Groups") {
                            FilterChip(
                                selected = selectedGroupFilter == grp,
                                onClick = {
                                    selectedGroupFilter = if (selectedGroupFilter == grp) "All Groups" else grp
                                },
                                label = { Text(grp) },
                                colors = vanijyaFilterChipColors()
                            )
                        }
                    }
                }
            }

            // Multi-select bulk action bar
            if (isMultiSelectMode) {
                item {
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
                            Text(
                                text = "${selectedPartyIds.size} selected",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(
                                    onClick = {
                                        if (selectedPartyIds.size == filteredParties.size) {
                                            selectedPartyIds.clear()
                                        } else {
                                            selectedPartyIds.clear()
                                            selectedPartyIds.addAll(filteredParties.map { it.id })
                                        }
                                    }
                                ) {
                                    Text("Select All", color = MaterialTheme.colorScheme.primary)
                                }
                                TextButton(
                                    onClick = {
                                        if (selectedPartyIds.isNotEmpty()) {
                                            val toDelete = selectedPartyIds.toList()
                                            onDeleteParties(toDelete)
                                            fallbackParties.removeAll { it.id in toDelete }
                                            actionFeedback = "✓ Archived ${toDelete.size} selected party record(s)"
                                            selectedPartyIds.clear()
                                            isMultiSelectMode = false
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete Selected",
                                        tint = CoralError,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Archive", color = CoralError)
                                }
                            }
                        }
                    }
                }
            }

            if (!actionFeedback.isNullOrBlank()) {
                item {
                    Surface(
                        color = EmeraldPrimary.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { actionFeedback = null }
                            .testTag("parties_feedback_banner")
                    ) {
                        Text(
                            text = actionFeedback!!,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Alphabetical Party List with Quick Call / WhatsApp / Reminder / PDF Statement
            if (filteredParties.isEmpty()) {
                item {
                    VanijyaEmptyState(
                        icon = Icons.Default.People,
                        title = "No matching parties",
                        description = "Add customers or suppliers to track 'To Collect' and 'To Pay' balances.",
                        actionLabel = "Add Party",
                        actionIcon = Icons.Default.PersonAdd,
                        onActionClick = {
                            editingParty = null
                            showPartyFormSheet = true
                        }
                    )
                }
            } else {
                itemsIndexed(filteredParties, key = { _, it -> it.id }) { idx, party ->
                    val isReceivable = party.currentBalancePaise >= 0L
                    val accent = if (isReceivable) EmeraldPrimary else ToPayRed
                    val isOverCreditLimit = party.creditLimitPaise > 0L && party.currentBalancePaise > party.creditLimitPaise
                    val isSelected = party.id in selectedPartyIds

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("party_card_$idx"),
                        cornerRadius = 24.dp,
                        contentPadding = PaddingValues(16.dp),
                        onClick = {
                            if (isMultiSelectMode) {
                                if (isSelected) selectedPartyIds.remove(party.id)
                                else selectedPartyIds.add(party.id)
                            } else {
                                activeDetailPartyId = party.id
                            }
                        }
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
                                if (isMultiSelectMode) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                        contentDescription = "Select ${party.name}",
                                        tint = if (isSelected) VioletPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .padding(end = 10.dp)
                                            .size(22.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(accent.copy(alpha = 0.18f))
                                        .border(1.dp, accent.copy(alpha = 0.45f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = party.name.take(1).uppercase(),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = accent
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = party.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = "${party.phone.ifBlank { "No phone" }} • GST: ${party.gstin.ifBlank { "URD" }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${party.groupName} • ${party.stateName} (${party.creditDays}d credit)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                AmountText(
                                    amount = Money(abs(party.currentBalancePaise)),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = accent,
                                    masked = hideAmounts,
                                    decimals = decimals
                                )
                                Text(
                                    text = if (isReceivable) "To Collect" else "To Pay",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = accent
                                )
                                if (isOverCreditLimit) {
                                    Surface(
                                        color = CoralError.copy(alpha = 0.16f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Text(
                                            text = "Over Limit",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = CoralError,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = glass.subtleDivider)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick Call / Reminder / PDF Statement + More Menu (at most 3 visible buttons, 48dp touch targets)
                        var showPartyMoreMenu by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        runCatching {
                                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${party.phone}")).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(dialIntent)
                                        }
                                        actionFeedback = "Dialing ${party.name} (${party.phone})..."
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Call ${party.name}",
                                        tint = PurchaseBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { activeReminderParty = party },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = "Payment Reminder Templates for ${party.name}",
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = glass.pillFill,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 40.dp)
                                        .border(1.dp, glass.borderGradientStart, RoundedCornerShape(12.dp))
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            val entries = partyLedgerEntries.filter { it.partyId == party.id }
                                            val aging = PartyLedgerCalculator.computeAgingBuckets(entries)
                                            VanijyaPdfExporter.exportAndSharePartyStatementPdf(
                                                context = context,
                                                company = activeCompany,
                                                party = party,
                                                entries = entries,
                                                aging = aging,
                                                dateRangeLabel = "All Time"
                                            )
                                            actionFeedback = "✓ Exported Party Statement PDF for ${party.name}"
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = "PDF Statement",
                                            tint = CoralError,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "PDF",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Box {
                                    IconButton(
                                        onClick = { showPartyMoreMenu = true },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "More actions for ${party.name}",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showPartyMoreMenu,
                                        onDismissRequest = { showPartyMoreMenu = false },
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("View Khata Ledger", color = MaterialTheme.colorScheme.onSurface) },
                                            onClick = {
                                                showPartyMoreMenu = false
                                                activeDetailPartyId = party.id
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Edit Party", color = MaterialTheme.colorScheme.onSurface) },
                                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = PurchaseBlue) },
                                            onClick = {
                                                showPartyMoreMenu = false
                                                editingParty = party
                                                showPartyFormSheet = true
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("WhatsApp / SMS Reminder", color = MaterialTheme.colorScheme.onSurface) },
                                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = EmeraldPrimary) },
                                            onClick = {
                                                showPartyMoreMenu = false
                                                activeReminderParty = party
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating "Add Party" Button
        VanijyaExtendedFab(
            text = "Add Party",
            onClick = {
                editingParty = null
                showPartyFormSheet = true
            },
            icon = Icons.Default.PersonAdd,
            containerColor = EmeraldPrimary,
            contentColor = MaterialTheme.colorScheme.surface,
            testTag = "parties_fab_add_party",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 104.dp)
        )
    }

    // ========================================================================
    // SHEET 1: ADD / EDIT PARTY FULL FORM (WITH GSTIN CHECKSUM, ADDRESSES, CUSTOM FIELDS)
    // ========================================================================
    if (showPartyFormSheet) {
        val existing = editingParty
        val partyId = remember(existing) { existing?.id ?: UUID.randomUUID().toString() }

        var nameDraft by rememberSaveable(existing) { mutableStateOf(existing?.name ?: "") }
        var partyTypeDraft by rememberSaveable(existing) { mutableStateOf(existing?.partyType ?: "CUSTOMER") }
        var phoneDraft by rememberSaveable(existing) { mutableStateOf(existing?.phone ?: "") }
        var altPhoneDraft by rememberSaveable(existing) { mutableStateOf(existing?.altPhone ?: "") }
        var emailDraft by rememberSaveable(existing) { mutableStateOf(existing?.email ?: "") }
        var gstinDraft by rememberSaveable(existing) { mutableStateOf(existing?.gstin ?: "") }
        var panDraft by rememberSaveable(existing) { mutableStateOf(existing?.pan ?: "") }
        var stateCodeDraft by rememberSaveable(existing) { mutableStateOf(existing?.stateCode ?: "27") }
        var stateNameDraft by rememberSaveable(existing) { mutableStateOf(existing?.stateName ?: "Maharashtra") }
        var groupNameDraft by rememberSaveable(existing) { mutableStateOf(existing?.groupName ?: "Wholesale Buyers") }
        var openingBalanceDraft by rememberSaveable(existing) {
            mutableStateOf(
                if (existing != null && existing.openingBalancePaise > 0L) (existing.openingBalancePaise / 100L).toString() else ""
            )
        }
        var isToCollectDraft by rememberSaveable(existing) { mutableStateOf(existing?.isToCollect ?: true) }
        var creditLimitDraft by rememberSaveable(existing) {
            mutableStateOf(
                if (existing != null && existing.creditLimitPaise > 0L) (existing.creditLimitPaise / 100L).toString() else "50000"
            )
        }
        var creditDaysDraft by rememberSaveable(existing) { mutableStateOf((existing?.creditDays ?: 30).toString()) }
        var priceListNameDraft by rememberSaveable(existing) { mutableStateOf(existing?.priceListName ?: "Standard Retail") }
        var billingAddressDraft by rememberSaveable(existing) {
            val bill = partyAddresses.firstOrNull { it.partyId == existing?.id && it.addressType == "BILLING" }
            mutableStateOf(bill?.line1 ?: "")
        }
        var shippingAddressDraft by rememberSaveable(existing) {
            val ship = partyAddresses.firstOrNull { it.partyId == existing?.id && it.addressType == "SHIPPING" }
            mutableStateOf(ship?.line1 ?: "")
        }
        var cityDraft by rememberSaveable(existing) {
            val bill = partyAddresses.firstOrNull { it.partyId == existing?.id }
            mutableStateOf(bill?.city ?: "Pune")
        }
        var pincodeDraft by rememberSaveable(existing) {
            val bill = partyAddresses.firstOrNull { it.partyId == existing?.id }
            mutableStateOf(bill?.pincode ?: "411001")
        }
        var customFieldKeyDraft by rememberSaveable(existing) {
            val cf = partyCustomFields.firstOrNull { it.partyId == existing?.id }
            mutableStateOf(cf?.fieldKey ?: "FSSAI / Drug License")
        }
        var customFieldValDraft by rememberSaveable(existing) {
            val cf = partyCustomFields.firstOrNull { it.partyId == existing?.id }
            mutableStateOf(cf?.fieldValue ?: "")
        }
        var birthdayDraft by rememberSaveable(existing) { mutableStateOf(existing?.birthday ?: "") }
        var anniversaryDraft by rememberSaveable(existing) { mutableStateOf(existing?.anniversary ?: "") }
        var notesDraft by rememberSaveable(existing) { mutableStateOf(existing?.notes ?: "") }
        var formError by remember { mutableStateOf<String?>(null) }

        val gstinFormatCheck = remember(gstinDraft) {
            Validators.validateGstin(gstinDraft, verifyChecksumStrictly = false)
        }
        val gstinStrictChecksum = remember(gstinDraft) {
            Validators.validateGstin(gstinDraft, verifyChecksumStrictly = true)
        }

        GlassFullScreenEditor(
            onBack = { showPartyFormSheet = false },
            title = if (existing == null) "Add Customer / Supplier" else "Edit ${existing.name}",
            subtitle = "Stored locally inside your encrypted SQLCipher party, party_address & party_ledger_entry tables",
            screenTestTag = "party_full_screen_editor",
            backButtonTestTag = "party_form_back_btn"
        ) {
            // Party Type: CUSTOMER / SUPPLIER / BOTH
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "CUSTOMER" to "Customer (To Collect)",
                    "SUPPLIER" to "Supplier (To Pay)",
                    "BOTH" to "Both"
                ).forEach { (code, label) ->
                    FilterChip(
                        selected = partyTypeDraft == code,
                        onClick = {
                            partyTypeDraft = code
                            isToCollectDraft = code != "SUPPLIER"
                        },
                        label = { Text(label) },
                        colors = vanijyaFilterChipColors()
                    )
                }
            }

            OutlinedTextField(
                value = nameDraft,
                onValueChange = {
                    nameDraft = it
                    formError = null
                },
                label = { Text("Party / Firm Name *") },
                singleLine = true,
                isError = formError != null,
                colors = vanijyaTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("party_form_name_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = phoneDraft,
                    onValueChange = { phoneDraft = it.filter(Char::isDigit).take(10) },
                    label = { Text("Primary Mobile") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = altPhoneDraft,
                    onValueChange = { altPhoneDraft = it.filter(Char::isDigit).take(10) },
                    label = { Text("Alt Phone / WhatsApp") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
            }

            // GSTIN with offline format, Mod-36 checksum validation & automatic State + PAN detection
            OutlinedTextField(
                value = gstinDraft,
                onValueChange = { raw ->
                    val upper = raw.uppercase().take(15)
                    gstinDraft = upper
                    Validators.extractPanFromGstin(upper)?.let { panDraft = it }
                    Validators.extractStateFromGstin(upper)?.let { st ->
                        stateCodeDraft = st.code
                        stateNameDraft = st.name
                    }
                },
                label = { Text("GSTIN (Optional)") },
                placeholder = { Text("27AAPFU0939F1ZV") },
                singleLine = true,
                isError = gstinFormatCheck is Validators.ValidationResult.Invalid,
                supportingText = {
                    when {
                        gstinFormatCheck is Validators.ValidationResult.Invalid ->
                            Text(gstinFormatCheck.reason, color = CoralError)
                        gstinDraft.length == 15 && gstinStrictChecksum is Validators.ValidationResult.Valid ->
                            Text("✓ Valid GSTIN + Checksum • State: $stateCodeDraft ($stateNameDraft) • PAN: $panDraft", color = EmeraldPrimary)
                        gstinDraft.length == 15 && gstinStrictChecksum is Validators.ValidationResult.Invalid ->
                            Text("✓ State: $stateCodeDraft ($stateNameDraft) • Note: ${gstinStrictChecksum.reason}", color = GoldPrimary)
                        else ->
                            Text("Auto-detects State Code & 10-char PAN offline", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                colors = vanijyaTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("party_form_gstin_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = panDraft,
                    onValueChange = { panDraft = it.uppercase().take(10) },
                    label = { Text("PAN") },
                    singleLine = true,
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = "$stateCodeDraft - $stateNameDraft",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Detected State") },
                    singleLine = true,
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
            }

            // Opening Balance (To Collect / To Pay) + Credit Limit + Credit Days
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = openingBalanceDraft,
                    onValueChange = { openingBalanceDraft = it.filter { ch -> ch.isDigit() || ch == '.' }.take(10) },
                    label = { Text("Opening Balance (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("party_form_opening_balance")
                )
                FilterChip(
                    selected = isToCollectDraft,
                    onClick = { isToCollectDraft = true },
                    label = { Text("To Collect") },
                    colors = vanijyaFilterChipColors()
                )
                FilterChip(
                    selected = !isToCollectDraft,
                    onClick = { isToCollectDraft = false },
                    label = { Text("To Pay") },
                    colors = vanijyaFilterChipColors()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = creditLimitDraft,
                    onValueChange = { creditLimitDraft = it.filter(Char::isDigit).take(9) },
                    label = { Text("Credit Limit (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = creditDaysDraft,
                    onValueChange = { creditDaysDraft = it.filter(Char::isDigit).take(3) },
                    label = { Text("Credit Days") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
            }

            // Group & Price List Selection Chips
            Text(
                text = "Party Group & Price Tier",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Retail Customers", "Wholesale Buyers", "FMCG & Raw Suppliers", "Transport & Logistics").forEach { grp ->
                    FilterChip(
                        selected = groupNameDraft == grp,
                        onClick = { groupNameDraft = grp },
                        label = { Text(grp) },
                        colors = vanijyaFilterChipColors()
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tiers = if (priceLists.isNotEmpty()) priceLists.map { it.name }
                else listOf("Standard Retail", "Wholesale Tier-1", "Distributor VIP")
                tiers.forEach { pl ->
                    FilterChip(
                        selected = priceListNameDraft == pl,
                        onClick = { priceListNameDraft = pl },
                        label = { Text("Price: $pl") },
                        colors = vanijyaFilterChipColors()
                    )
                }
            }

            // Multiple Addresses (Billing & Shipping)
            OutlinedTextField(
                value = billingAddressDraft,
                onValueChange = { billingAddressDraft = it },
                label = { Text("Billing Address") },
                singleLine = true,
                colors = vanijyaTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = shippingAddressDraft,
                onValueChange = { shippingAddressDraft = it },
                label = { Text("Shipping / Godown Delivery Address") },
                singleLine = true,
                colors = vanijyaTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = cityDraft,
                    onValueChange = { cityDraft = it },
                    label = { Text("City") },
                    singleLine = true,
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = pincodeDraft,
                    onValueChange = { pincodeDraft = it.filter(Char::isDigit).take(6) },
                    label = { Text("Pincode") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
            }

            // Custom Field + Birthday / Anniversary + Notes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = customFieldKeyDraft,
                    onValueChange = { customFieldKeyDraft = it },
                    label = { Text("Custom Field Label") },
                    singleLine = true,
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = customFieldValDraft,
                    onValueChange = { customFieldValDraft = it },
                    label = { Text("Custom Field Value") },
                    singleLine = true,
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = birthdayDraft,
                    onValueChange = { birthdayDraft = it },
                    label = { Text("Birthday (e.g. 15 Aug)") },
                    singleLine = true,
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = anniversaryDraft,
                    onValueChange = { anniversaryDraft = it },
                    label = { Text("Anniversary") },
                    singleLine = true,
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = notesDraft,
                onValueChange = { notesDraft = it },
                label = { Text("Party Notes / Payment Terms") },
                singleLine = true,
                colors = vanijyaTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            if (formError != null) {
                Text(
                    text = formError!!,
                    style = MaterialTheme.typography.labelMedium,
                    color = CoralError
                )
            }

            VanijyaButton(
                text = "Save Party",
                onClick = {
                    if (nameDraft.isBlank()) {
                        formError = "Party / Firm Name is required"
                    } else if (gstinFormatCheck is Validators.ValidationResult.Invalid) {
                        formError = gstinFormatCheck.reason
                    } else {
                        val now = System.currentTimeMillis()
                        val openingMoney = runCatching { Money.parseInput(openingBalanceDraft) }.getOrDefault(Money.ZERO)
                        val limitMoney = runCatching { Money.parseInput(creditLimitDraft) }.getOrDefault(Money.ZERO)
                        val signedOpening = if (isToCollectDraft) openingMoney.paise else -openingMoney.paise

                        val newParty = PartyEntity(
                            id = partyId,
                            companyId = companyId,
                            name = nameDraft.trim(),
                            partyType = partyTypeDraft,
                            phone = phoneDraft.ifBlank { "9800000000" },
                            altPhone = altPhoneDraft,
                            email = emailDraft.trim(),
                            gstin = gstinDraft.trim().uppercase(),
                            pan = panDraft.trim().uppercase(),
                            stateCode = stateCodeDraft,
                            stateName = stateNameDraft,
                            groupId = "${companyId}_grp_${groupNameDraft.lowercase().take(6)}",
                            groupName = groupNameDraft,
                            openingBalancePaise = openingMoney.paise,
                            isToCollect = isToCollectDraft,
                            currentBalancePaise = existing?.currentBalancePaise ?: signedOpening,
                            creditLimitPaise = limitMoney.paise,
                            creditDays = creditDaysDraft.toIntOrNull() ?: 30,
                            priceListId = "${companyId}_pl_${priceListNameDraft.lowercase().take(6)}",
                            priceListName = priceListNameDraft,
                            notes = notesDraft.trim(),
                            birthday = birthdayDraft.trim(),
                            anniversary = anniversaryDraft.trim(),
                            createdAt = existing?.createdAt ?: now,
                            updatedAt = now
                        )

                        val addrList = buildList {
                            if (billingAddressDraft.isNotBlank()) {
                                add(
                                    PartyAddressEntity(
                                        id = "${partyId}_bill",
                                        companyId = companyId,
                                        partyId = partyId,
                                        addressType = "BILLING",
                                        label = "Billing Office",
                                        line1 = billingAddressDraft.trim(),
                                        city = cityDraft.trim(),
                                        stateCode = stateCodeDraft,
                                        stateName = stateNameDraft,
                                        pincode = pincodeDraft.trim(),
                                        isDefault = true,
                                        createdAt = now,
                                        updatedAt = now
                                    )
                                )
                            }
                            if (shippingAddressDraft.isNotBlank()) {
                                add(
                                    PartyAddressEntity(
                                        id = "${partyId}_ship",
                                        companyId = companyId,
                                        partyId = partyId,
                                        addressType = "SHIPPING",
                                        label = "Shipping Gate",
                                        line1 = shippingAddressDraft.trim(),
                                        city = cityDraft.trim(),
                                        stateCode = stateCodeDraft,
                                        stateName = stateNameDraft,
                                        pincode = pincodeDraft.trim(),
                                        isDefault = false,
                                        createdAt = now,
                                        updatedAt = now
                                    )
                                )
                            }
                        }

                        val customFields = if (customFieldValDraft.isNotBlank()) {
                            listOf(
                                PartyCustomFieldEntity(
                                    id = "${partyId}_cf1",
                                    companyId = companyId,
                                    partyId = partyId,
                                    fieldKey = customFieldKeyDraft.ifBlank { "Custom Field" },
                                    fieldValue = customFieldValDraft.trim(),
                                    createdAt = now,
                                    updatedAt = now
                                )
                            )
                        } else emptyList()

                        onSaveParty(newParty, addrList, customFields)
                        val idx = fallbackParties.indexOfFirst { it.id == newParty.id }
                        if (idx >= 0) fallbackParties[idx] = newParty else fallbackParties.add(newParty)
                        actionFeedback = "✓ Saved party master '${newParty.name}'"
                        showPartyFormSheet = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("party_form_save_btn")
            )
        }
    }

    // ========================================================================
    // SUB-SCREEN 2: PARTY DETAIL, RUNNING LEDGER STATEMENT, AGING & CREDIT-LIMIT HOOK
    // ========================================================================
    if (activeDetailParty != null && !showPartyDeepAgingScreen) {
        val party = effectiveParties.firstOrNull { it.id == activeDetailParty.id } ?: activeDetailParty
        val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH) }
        var dateFilter by remember { mutableStateOf("All Time") }
        var testInvoiceAmountInput by remember { mutableStateOf("15000") }
        var voucherTypeDraft by remember { mutableStateOf("SALE_INVOICE") }
        var voucherAmountDraft by remember { mutableStateOf("") }

        val rawEntries = remember(partyLedgerEntries, party) {
            val dbEntries = partyLedgerEntries.filter { it.partyId == party.id }
            if (dbEntries.isNotEmpty()) {
                PartyLedgerCalculator.computeRunningBalances(dbEntries)
            } else {
                listOf(
                    PartyLedgerEntryEntity(
                        id = "${party.id}_opening",
                        companyId = party.companyId,
                        partyId = party.id,
                        entryDate = party.createdAt,
                        voucherType = "OPENING_BALANCE",
                        voucherNumber = "OPEN-BAL",
                        debitPaise = if (party.currentBalancePaise >= 0L) party.currentBalancePaise else 0L,
                        creditPaise = if (party.currentBalancePaise < 0L) abs(party.currentBalancePaise) else 0L,
                        runningBalancePaise = party.currentBalancePaise,
                        notes = "Opening ledger balance",
                        createdAt = party.createdAt,
                        updatedAt = party.updatedAt
                    )
                )
            }
        }

        val now = System.currentTimeMillis()
        val filteredEntries = remember(rawEntries, dateFilter) {
            val cutoff = when (dateFilter) {
                "Last 30 Days" -> now - 30L * 86_400_000L
                "Last 90 Days" -> now - 90L * 86_400_000L
                else -> 0L
            }
            rawEntries.filter { it.entryDate >= cutoff }
        }

        val partyAging = remember(rawEntries) {
            PartyLedgerCalculator.computeAgingBuckets(rawEntries, now)
        }

        val creditLimitResult = remember(party, testInvoiceAmountInput) {
            val simulatedBill = runCatching { Money.parseInput(testInvoiceAmountInput) }.getOrDefault(Money.ZERO)
            PartyLedgerCalculator.evaluateCreditLimitWarning(party, simulatedBill)
        }

        val addressesForParty = remember(partyAddresses, party.id) {
            partyAddresses.filter { it.partyId == party.id }
        }

        GlassSubScreen(
            onBack = { activeDetailPartyId = null },
            title = "${party.name} (${party.partyType})",
            subtitle = "GSTIN: ${party.gstin.ifBlank { "URD" }} • State: ${party.stateCode}-${party.stateName} • Price Tier: ${party.priceListName}",
            screenTestTag = "party_detail_screen",
            backButtonTestTag = "party_detail_back_btn"
        ) {
            VanijyaButton(
                text = "Aging & Credit-Limit Analysis",
                icon = Icons.Default.History,
                isOutlined = true,
                onClick = { showPartyDeepAgingScreen = true },
                modifier = Modifier.fillMaxWidth(),
                testTag = "party_detail_open_aging_btn"
            )
            // Closing Balance & Credit Limit Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                accentTint = if (party.currentBalancePaise >= 0L) EmeraldPrimary else ToPayRed,
                contentPadding = PaddingValues(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (party.currentBalancePaise >= 0L) "Closing Receivable (To Collect)" else "Closing Payable (To Pay)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        AmountText(
                            amount = Money(abs(party.currentBalancePaise)),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (party.currentBalancePaise >= 0L) EmeraldPrimary else ToPayRed,
                            masked = hideAmounts,
                            decimals = decimals
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Credit Limit (${party.creditDays}d)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        AmountText(
                            amount = Money(party.creditLimitPaise),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            masked = hideAmounts,
                            decimals = decimals
                        )
                    }
                }
            }

            // Aging Analysis (0-30, 31-60, 61-90, 90+)
            SectionHeader(title = "Aging Analysis (0-30 / 31-60 / 61-90 / 90+ Days)")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("0-30d", partyAging.bucket0To30, EmeraldPrimary),
                    Triple("31-60d", partyAging.bucket31To60, GoldPrimary),
                    Triple("61-90d", partyAging.bucket61To90, AmberSale),
                    Triple("90+d", partyAging.bucket90Plus, CoralError)
                ).forEach { (bucketLabel, amount, tint) ->
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        cornerRadius = 16.dp,
                        accentTint = tint,
                        contentPadding = PaddingValues(10.dp)
                    ) {
                        Text(
                            text = bucketLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = tint
                        )
                        AmountText(
                            amount = amount,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            masked = hideAmounts,
                            decimals = 0
                        )
                    }
                }
            }

            // Credit-Limit Warning Hook for Billing
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                accentTint = if (creditLimitResult is CreditLimitCheckResult.Exceeded) CoralError else EmeraldPrimary,
                contentPadding = PaddingValues(12.dp)
            ) {
                Text(
                    text = "Billing Credit-Limit Warning Hook",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = testInvoiceAmountInput,
                    onValueChange = { testInvoiceAmountInput = it.filter(Char::isDigit).take(8) },
                    label = { Text("Simulate New Sale Invoice Amount (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                when (creditLimitResult) {
                    is CreditLimitCheckResult.Exceeded -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = "Credit Limit Exceeded",
                                tint = CoralError,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Credit Limit Warning: Projected receivable ${creditLimitResult.projectedReceivable.formatIndian()} exceeds limit ${creditLimitResult.creditLimit.formatIndian()} by ${creditLimitResult.exceededBy.formatIndian()}!",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = CoralError
                            )
                        }
                    }
                    is CreditLimitCheckResult.WithinLimit -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Within Limit",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Within Credit Limit: Projected ${creditLimitResult.projectedReceivable.formatIndian()} / Limit ${creditLimitResult.creditLimit.formatIndian()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldPrimary
                            )
                        }
                    }
                }
            }

            // Addresses if present
            if (addressesForParty.isNotEmpty()) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    addressesForParty.forEach { addr ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = addr.addressType,
                                tint = PurchaseBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${addr.addressType}: ${addr.line1}, ${addr.city} - ${addr.pincode}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Date Filter + Running Balance Ledger Table
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All Time", "Last 30 Days", "Last 90 Days").forEach { df ->
                    FilterChip(
                        selected = dateFilter == df,
                        onClick = { dateFilter = df },
                        label = { Text(df) },
                        colors = vanijyaFilterChipColors()
                    )
                }
            }

            filteredEntries.forEach { entry ->
                val isDebit = entry.debitPaise >= entry.creditPaise
                val runSign = if (entry.runningBalancePaise >= 0L) "Dr (Collect)" else "Cr (Pay)"
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${entry.voucherType} • ${entry.voucherNumber}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${dateFormatter.format(Date(entry.entryDate))} • ${entry.notes}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isDebit) "+ ${Money(entry.debitPaise).formatIndian()}" else "- ${Money(entry.creditPaise).formatIndian()}",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (isDebit) EmeraldPrimary else ToPayRed
                            )
                            Text(
                                text = "Bal: ${Money(abs(entry.runningBalancePaise)).formatIndian()} $runSign",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Quick Add Ledger Transaction Row
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                contentPadding = PaddingValues(12.dp)
            ) {
                Text(
                    text = "Record Quick Khata Entry",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("SALE_INVOICE", "PAYMENT_IN", "PURCHASE_BILL", "PAYMENT_OUT").forEach { vt ->
                        FilterChip(
                            selected = voucherTypeDraft == vt,
                            onClick = { voucherTypeDraft = vt },
                            label = { Text(vt.replace("_", " ")) },
                            colors = vanijyaFilterChipColors()
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = voucherAmountDraft,
                        onValueChange = { voucherAmountDraft = it.filter(Char::isDigit).take(8) },
                        label = { Text("Amount (₹)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = vanijyaTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    VanijyaButton(
                        text = "Add Entry",
                        icon = Icons.Default.Add,
                        onClick = {
                            val amt = runCatching { Money.parseInput(voucherAmountDraft) }.getOrDefault(Money.ZERO)
                            if (amt.paise > 0L) {
                                val isDebitEntry = voucherTypeDraft in setOf("SALE_INVOICE", "PAYMENT_OUT", "DEBIT_NOTE")
                                val newEntry = PartyLedgerEntryEntity(
                                    id = UUID.randomUUID().toString(),
                                    companyId = party.companyId,
                                    partyId = party.id,
                                    entryDate = System.currentTimeMillis(),
                                    voucherType = voucherTypeDraft,
                                    voucherNumber = "VCH-${(100..999).random()}",
                                    debitPaise = if (isDebitEntry) amt.paise else 0L,
                                    creditPaise = if (!isDebitEntry) amt.paise else 0L,
                                    notes = "Manual Khata entry",
                                    createdAt = System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                )
                                onRecordLedgerEntry(newEntry)
                                voucherAmountDraft = ""
                                actionFeedback = "✓ Added ${voucherTypeDraft.replace("_", " ")} (${amt.formatIndian()}) to ${party.name}"
                            }
                        }
                    )
                }
            }

            // Export PDF Statement & Payment Reminder Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VanijyaButton(
                    text = "PDF Statement",
                    icon = Icons.Default.PictureAsPdf,
                    onClick = {
                        VanijyaPdfExporter.exportAndSharePartyStatementPdf(
                            context = context,
                            company = activeCompany,
                            party = party,
                            entries = filteredEntries,
                            aging = partyAging,
                            dateRangeLabel = dateFilter
                        )
                        actionFeedback = "✓ Generated & opened Share Sheet for ${party.name} PDF Statement"
                    },
                    modifier = Modifier.weight(1f)
                )
                VanijyaButton(
                    text = "Send Reminder",
                    icon = Icons.Default.NotificationsActive,
                    isOutlined = true,
                    onClick = {
                        activeReminderParty = party
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (activeDetailParty != null && showPartyDeepAgingScreen) {
        val party = effectiveParties.firstOrNull { it.id == activeDetailParty.id } ?: activeDetailParty
        val rawEntries = remember(partyLedgerEntries, party) {
            val dbEntries = partyLedgerEntries.filter { it.partyId == party.id }
            if (dbEntries.isNotEmpty()) PartyLedgerCalculator.computeRunningBalances(dbEntries)
            else emptyList()
        }
        val partyAging = remember(rawEntries) {
            PartyLedgerCalculator.computeAgingBuckets(rawEntries)
        }
        GlassSubScreen(
            onBack = { showPartyDeepAgingScreen = false },
            title = "Aging & Credit-Limit Analysis",
            subtitle = "${party.name} • GSTIN: ${party.gstin.ifBlank { "URD" }}",
            screenTestTag = "party_aging_sub_screen",
            backButtonTestTag = "party_aging_back_btn"
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                accentTint = GoldPrimary,
                contentPadding = PaddingValues(16.dp)
            ) {
                Text(
                    text = "Receivable / Payable Aging Breakdown",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                listOf(
                    Triple("0 - 30 Days", partyAging.bucket0To30, EmeraldPrimary),
                    Triple("31 - 60 Days", partyAging.bucket31To60, GoldPrimary),
                    Triple("61 - 90 Days", partyAging.bucket61To90, AmberSale),
                    Triple("90+ Days", partyAging.bucket90Plus, CoralError)
                ).forEach { (label, amt, tint) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        AmountText(
                            amount = amt,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = tint,
                            masked = hideAmounts,
                            decimals = decimals
                        )
                    }
                }
            }
        }
    }

    // ========================================================================
    // SHEET 3: OFFLINE PAYMENT REMINDER TEMPLATES (OPENED VIA SHARE SHEET)
    // ========================================================================
    if (activeReminderParty != null) {
        val party = activeReminderParty!!
        var style by remember { mutableStateOf("GENTLE") }
        val firmName = activeCompany?.businessName?.ifBlank { "Shree Ganesh Enterprises" } ?: "Shree Ganesh Enterprises"
        val upiId = activeCompany?.upiId?.ifBlank { "ganeshtraders@okicici" } ?: "ganeshtraders@okicici"
        val previewText = remember(party, style, firmName, upiId) {
            PartyLedgerCalculator.buildPaymentReminderMessage(party, firmName, upiId, style)
        }

        GlassSheet(
            onDismissRequest = { activeReminderParty = null },
            title = "Payment Reminder Templates",
            subtitle = "Opens Android Share Sheet (WhatsApp / SMS / Email) — Vanijya stays 100% offline"
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "GENTLE" to "Polite / Gentle",
                    "STANDARD" to "Standard Due",
                    "URGENT" to "Overdue / Urgent",
                    "HINDI" to "हिन्दी (Hindi)"
                ).forEach { (code, label) ->
                    FilterChip(
                        selected = style == code,
                        onClick = { style = code },
                        label = { Text(label) },
                        colors = vanijyaFilterChipColors()
                    )
                }
            }

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                accentTint = EmeraldPrimary,
                contentPadding = PaddingValues(14.dp)
            ) {
                Text(
                    text = previewText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            VanijyaButton(
                text = "Share via WhatsApp / SMS / Email",
                icon = Icons.AutoMirrored.Filled.Chat,
                onClick = {
                    VanijyaPdfExporter.sharePlainTextViaChooser(
                        context = context,
                        text = previewText,
                        chooserTitle = "Send Payment Reminder to ${party.name}"
                    )
                    actionFeedback = "✓ Opened offline reminder share sheet for ${party.name}"
                    activeReminderParty = null
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // ========================================================================
    // SHEET 4: RECEIVABLE / PAYABLE AGING ANALYSIS OVERVIEW (0-30, 31-60, 61-90, 90+)
    // ========================================================================
    if (showAgingOverviewSheet) {
        GlassSheet(
            onDismissRequest = { showAgingOverviewSheet = false },
            title = "Receivable / Payable Aging Analysis",
            subtitle = "0–30, 31–60, 61–90, and 90+ days overdue breakdown across all parties"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("0-30 Days", overallAging.bucket0To30, EmeraldPrimary),
                    Triple("31-60 Days", overallAging.bucket31To60, GoldPrimary),
                    Triple("61-90 Days", overallAging.bucket61To90, AmberSale),
                    Triple("90+ Days", overallAging.bucket90Plus, CoralError)
                ).forEach { (label, amt, tint) ->
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        cornerRadius = 16.dp,
                        accentTint = tint,
                        contentPadding = PaddingValues(10.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = tint
                        )
                        AmountText(
                            amount = amt,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            masked = hideAmounts,
                            decimals = 0
                        )
                    }
                }
            }

            effectiveParties.forEach { p ->
                val ageDays = ((System.currentTimeMillis() - p.createdAt).coerceAtLeast(0L)) / 86_400_000L
                val bucketBadge = when {
                    ageDays <= 30L -> "0-30d" to EmeraldPrimary
                    ageDays <= 60L -> "31-60d" to GoldPrimary
                    ageDays <= 90L -> "61-90d" to AmberSale
                    else -> "90+d" to CoralError
                }
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = p.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Bucket: ${bucketBadge.first} (${ageDays}d) • Credit Days: ${p.creditDays}d",
                                style = MaterialTheme.typography.bodySmall,
                                color = bucketBadge.second
                            )
                        }
                        AmountText(
                            amount = Money(abs(p.currentBalancePaise)),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (p.currentBalancePaise >= 0L) EmeraldPrimary else ToPayRed,
                            masked = hideAmounts,
                            decimals = decimals
                        )
                    }
                }
            }
        }
    }

    // ========================================================================
    // SHEET 5: CSV / EXCEL IMPORT (WITH PREVIEW) & EXPORT
    // ========================================================================
    if (showCsvImportExportSheet) {
        var csvDraft by remember { mutableStateOf(VanijyaCsvPorters.PARTY_CSV_TEMPLATE) }
        val previewRows = remember(csvDraft, effectiveParties) {
            VanijyaCsvPorters.parsePartyCsvPreview(csvDraft, effectiveParties)
        }

        GlassSheet(
            onDismissRequest = { showCsvImportExportSheet = false },
            title = "Import / Export Parties (CSV & Excel)",
            subtitle = "Preview parsed rows, detect duplicates by Name/GSTIN, or export full party ledger"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VanijyaButton(
                    text = "Export Parties CSV",
                    icon = Icons.Default.FileDownload,
                    onClick = {
                        VanijyaCsvPorters.exportPartiesCsvAndShare(context, effectiveParties)
                        actionFeedback = "✓ Exported ${effectiveParties.size} parties to CSV/Excel"
                        showCsvImportExportSheet = false
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = csvDraft,
                onValueChange = { csvDraft = it },
                label = { Text("Paste or Edit CSV / Excel Rows") },
                minLines = 3,
                maxLines = 5,
                colors = vanijyaTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Import Preview (${previewRows.size} rows parsed)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            previewRows.forEach { row ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    accentTint = if (row.isDuplicate) CoralError else EmeraldPrimary,
                    contentPadding = PaddingValues(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${row.name} (${row.partyType})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "GSTIN: ${row.gstin.ifBlank { "URD" }} • State: ${row.stateCode}-${row.stateName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${row.openingBalance.formatIndian()} (${if (row.isToCollect) "Collect" else "Pay"})",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (row.isToCollect) EmeraldPrimary else ToPayRed
                            )
                            if (row.isDuplicate) {
                                Text(
                                    text = "Duplicate (Skipped)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CoralError
                                )
                            }
                        }
                    }
                }
            }

            VanijyaButton(
                text = "Import ${previewRows.count { !it.isDuplicate }} Valid Parties",
                icon = Icons.Default.FileUpload,
                onClick = {
                    onBulkImportParties(previewRows) { count ->
                        actionFeedback = "✓ Imported $count parties from CSV preview"
                    }
                    showCsvImportExportSheet = false
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // ========================================================================
    // SHEET 6: IMPORT FROM PHONE CONTACTS (OPTIONAL PERMISSION + PREVIEW)
    // ========================================================================
    if (showContactsImportSheet) {
        val sampleContacts = remember {
            listOf(
                Triple("Rajeshwari Super Bazar", "9822011223", "Retail Customers"),
                Triple("Omkar Electricals & Hardware", "9823155667", "Wholesale Buyers"),
                Triple("Sahyadri Dairy & Cold Chain", "9764099887", "FMCG & Raw Suppliers")
            )
        }
        GlassSheet(
            onDismissRequest = { showContactsImportSheet = false },
            title = "Import Parties from Phone Contacts",
            subtitle = "Uses optional READ_CONTACTS permission or quick address-book import"
        ) {
            VanijyaButton(
                text = "Open System Contact Picker",
                icon = Icons.Default.ContactPhone,
                onClick = {
                    showContactsImportSheet = false
                    readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Detected Device Business Contacts Preview",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            sampleContacts.forEach { (cName, cPhone, cGroup) ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(12.dp),
                    onClick = {
                        val now = System.currentTimeMillis()
                        val p = PartyEntity(
                            id = UUID.randomUUID().toString(),
                            companyId = companyId,
                            name = cName,
                            partyType = if (cGroup.contains("Supplier")) "SUPPLIER" else "CUSTOMER",
                            phone = cPhone,
                            groupName = cGroup,
                            createdAt = now,
                            updatedAt = now
                        )
                        onSaveParty(p, emptyList(), emptyList())
                        fallbackParties.add(p)
                        actionFeedback = "✓ Imported '$cName' ($cPhone) from contacts"
                        showContactsImportSheet = false
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = cName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$cPhone • $cGroup",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Import",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
