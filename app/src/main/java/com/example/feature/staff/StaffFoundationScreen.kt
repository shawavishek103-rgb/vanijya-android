package com.example.feature.staff

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.designsystem.AmountText
import com.example.core.designsystem.GlassCard
import com.example.core.designsystem.GlassFullScreenEditor
import com.example.core.designsystem.GlassSheet
import com.example.core.designsystem.GlassSubScreen
import com.example.core.designsystem.SectionHeader
import com.example.core.designsystem.SegmentedTabs
import com.example.core.designsystem.VanijyaButton
import com.example.core.designsystem.VanijyaEmptyState
import com.example.core.designsystem.VanijyaExtendedFab
import com.example.core.designsystem.vanijyaFilterChipColors
import com.example.core.designsystem.vanijyaTextFieldColors
import com.example.core.model.Money
import com.example.ui.theme.AmberSale
import com.example.ui.theme.CoralError
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LocalVanijyaGlass
import com.example.ui.theme.PurchaseBlue
import com.example.ui.theme.ToPayRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StaffFoundationScreen(
    hideAmounts: Boolean = false,
    decimals: Int = 2,
    initialTabIndex: Int = 0,
    onFullScreenEditorOpenChange: (Boolean) -> Unit = {},
    reselectTrigger: Int = 0
) {
    val glass = LocalVanijyaGlass.current
    val listState = rememberLazyListState()

    val todayLabel = stringResource(R.string.staff_tab_today)
    val employeesLabel = stringResource(R.string.staff_tab_employees)
    val payrollLabel = stringResource(R.string.staff_tab_payroll)
    val advancesLabel = stringResource(R.string.staff_tab_advances)
    val tabs = remember(todayLabel, employeesLabel, payrollLabel, advancesLabel) {
        listOf(todayLabel, employeesLabel, payrollLabel, advancesLabel)
    }

    var selectedTab by rememberSaveable { mutableIntStateOf(initialTabIndex.coerceIn(0, 3)) }
    var comingSoonBanner by rememberSaveable { mutableStateOf<String?>(null) }

    // Today tab state
    var selectedQuickStatus by rememberSaveable { mutableStateOf("Present") }

    // Employees tab state
    var employeeSearchQuery by rememberSaveable { mutableStateOf("") }
    var selectedSalaryTypeFilter by rememberSaveable { mutableStateOf("All") }
    var showAddEmployeeSheet by rememberSaveable { mutableStateOf(false) }

    // Payroll & Advances state (opens directly with no biometric/PIN re-authentication)
    var selectedPayrollMonth by rememberSaveable { mutableStateOf("Nov 2026") }
    var selectedPayslipFilter by rememberSaveable { mutableStateOf("All") }
    var showPayrollRunEditor by rememberSaveable { mutableStateOf(false) }
    var showStaffRosterDetailScreen by rememberSaveable { mutableStateOf(false) }
    var showStaffSalarySubScreen by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(showPayrollRunEditor) {
        onFullScreenEditorOpenChange(showPayrollRunEditor)
    }

    DisposableEffect(Unit) {
        onDispose {
            onFullScreenEditorOpenChange(false)
        }
    }

    LaunchedEffect(reselectTrigger) {
        if (reselectTrigger > 0) {
            showStaffSalarySubScreen = false
            showStaffRosterDetailScreen = false
            showPayrollRunEditor = false
            listState.scrollToItem(0)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("staff_hub_screen")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 180.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Segmented Tabs: Today, Employees, Payroll, Advances
            item {
                SegmentedTabs(
                    tabs = tabs,
                    selectedIndex = selectedTab,
                    onTabSelected = { selectedTab = it },
                    scrollable = false,
                    testTagPrefix = "staff_segment"
                )
            }

            // Coming Soon / Status Banner
            if (!comingSoonBanner.isNullOrBlank()) {
                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { comingSoonBanner = null }
                            .testTag("staff_coming_soon_banner"),
                        cornerRadius = 18.dp,
                        accentTint = GoldPrimary,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
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
                                Surface(
                                    color = GoldPrimary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.coming_soon_badge),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = GoldPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = comingSoonBanner!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "Dismiss",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            when (selectedTab) {
                // 0. TODAY TAB
                0 -> {
                    item {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("staff_today_summary_card"),
                            cornerRadius = 26.dp,
                            elevated = true,
                            accentTint = EmeraldPrimary
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Today's Attendance Summary",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "13 Nov 2026 • 0 Registered Staff",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    color = GoldPrimary.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.coming_soon_badge),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = GoldPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            VanijyaButton(
                                text = "View Roster & Attendance Details",
                                icon = Icons.Default.Badge,
                                isOutlined = true,
                                onClick = {
                                    showStaffRosterDetailScreen = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "staff_open_roster_detail_btn"
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Present, Absent, Half-day, Leave, Late counts (0 each — no fake data)
                            val statusCounts = listOf(
                                Triple(stringResource(R.string.staff_status_present), 0, EmeraldPrimary),
                                Triple(stringResource(R.string.staff_status_absent), 0, CoralError),
                                Triple(stringResource(R.string.staff_status_half_day), 0, AmberSale),
                                Triple(stringResource(R.string.staff_status_leave), 0, PurchaseBlue),
                                Triple(stringResource(R.string.staff_status_late), 0, GoldPrimary)
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                maxItemsInEachRow = 3
                            ) {
                                statusCounts.forEach { (label, count, color) ->
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(color.copy(alpha = 0.14f))
                                            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                            .padding(vertical = 10.dp, horizontal = 6.dp)
                                            .testTag("attendance_count_${label.lowercase()}"),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = count.toString(),
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = color
                                        )
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = glass.subtleDivider)
                            Spacer(modifier = Modifier.height(12.dp))

                            VanijyaButton(
                                text = stringResource(R.string.staff_mark_all_present),
                                icon = Icons.Default.CheckCircle,
                                onClick = {
                                    comingSoonBanner = "Mark all present is Coming Soon in the Staff & Payroll step."
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "staff_mark_all_present_btn"
                            )
                        }
                    }

                    // Quick status chips to tap + Employee attendance list empty state
                    item {
                        SectionHeader(title = "Quick Attendance Status Chips")
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Present", "Absent", "Half-day", "Leave", "Late").forEach { status ->
                                FilterChip(
                                    selected = selectedQuickStatus == status,
                                    onClick = {
                                        selectedQuickStatus = status
                                        comingSoonBanner = "Quick status '$status' chip ready — employee roster connects in the Staff step (Coming Soon)."
                                    },
                                    label = { Text(status) },
                                    colors = vanijyaFilterChipColors(),
                                    modifier = Modifier.testTag("staff_quick_chip_${status.lowercase()}")
                                )
                            }
                        }
                    }

                    item {
                        VanijyaEmptyState(
                            icon = Icons.Default.Badge,
                            title = "No Employees in Today's Roster",
                            description = "Coming soon: Add employees with monthly, daily, or piece-rate salary structures to mark daily Present, Absent, Half-day, Leave, and Late attendance.",
                            actionLabel = stringResource(R.string.staff_add_employee),
                            onActionClick = {
                                selectedTab = 1
                                showAddEmployeeSheet = true
                            }
                        )
                    }
                }

                // 1. EMPLOYEES TAB
                1 -> {
                    item {
                        OutlinedTextField(
                            value = employeeSearchQuery,
                            onValueChange = { employeeSearchQuery = it },
                            label = { Text("Search employees by name, role, or salary type") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Employees"
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            colors = vanijyaTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("staff_employees_search_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("All", "Monthly Salary", "Daily Wage", "Hourly / Piece-Rate").forEach { type ->
                                FilterChip(
                                    selected = selectedSalaryTypeFilter == type,
                                    onClick = { selectedSalaryTypeFilter = type },
                                    label = { Text(type) },
                                    colors = vanijyaFilterChipColors()
                                )
                            }
                        }
                    }

                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 22.dp,
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Employee Directory Structure",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Supports employee photo, role/designation, salary type (Monthly/Daily), bank/UPI payout details, and joining date.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = GoldPrimary.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.coming_soon_badge),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = GoldPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        VanijyaEmptyState(
                            icon = Icons.Default.PersonAdd,
                            title = "No Employees Registered Yet",
                            description = "Tap 'Add Employee' below to preview the staff profile form (Photo, Role, Salary Type). Full database persistence arrives in the Staff & Payroll step (Coming Soon).",
                            actionLabel = stringResource(R.string.staff_add_employee),
                            actionIcon = Icons.Default.PersonAdd,
                            onActionClick = { showAddEmployeeSheet = true }
                        )
                    }
                }

                // 2. PAYROLL TAB (Opens directly, respects Hide Amounts toggle)
                2 -> {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("Nov 2026", "Oct 2026", "Sep 2026", "Aug 2026").forEach { month ->
                                FilterChip(
                                    selected = selectedPayrollMonth == month,
                                    onClick = { selectedPayrollMonth = month },
                                    label = { Text(month) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = month,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = vanijyaFilterChipColors()
                                )
                            }
                        }
                    }

                    item {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("staff_payroll_summary_card"),
                            cornerRadius = 26.dp,
                            elevated = true,
                            accentTint = EmeraldPrimary
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "$selectedPayrollMonth Payroll Cycle",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "0 Payslips • Attendance-linked monthly payouts",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    color = GoldPrimary.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.coming_soon_badge),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = GoldPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Net Payable",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    AmountText(
                                        amount = Money.ZERO,
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        masked = hideAmounts,
                                        decimals = decimals
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Paid Slips",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldPrimary
                                    )
                                    AmountText(
                                        amount = Money.ZERO,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = EmeraldPrimary,
                                        masked = hideAmounts,
                                        decimals = decimals
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Unpaid Slips",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ToPayRed
                                    )
                                    AmountText(
                                        amount = Money.ZERO,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = ToPayRed,
                                        masked = hideAmounts,
                                        decimals = decimals
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = glass.subtleDivider)
                            Spacer(modifier = Modifier.height(12.dp))

                            VanijyaButton(
                                text = stringResource(R.string.staff_run_payroll),
                                icon = Icons.Default.PlayArrow,
                                onClick = {
                                    showPayrollRunEditor = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "staff_run_payroll_btn"
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("All", "Paid", "Unpaid").forEach { status ->
                                FilterChip(
                                    selected = selectedPayslipFilter == status,
                                    onClick = { selectedPayslipFilter = status },
                                    label = { Text("$status Payslips") },
                                    colors = vanijyaFilterChipColors()
                                )
                            }
                        }
                    }

                    item {
                        VanijyaEmptyState(
                            icon = Icons.Default.Payments,
                            title = "No Payslips for $selectedPayrollMonth",
                            description = "Coming soon: Automatically compute monthly payslips from daily attendance, overtime, and salary advances, and export printable PDF payslips.",
                            actionLabel = stringResource(R.string.staff_run_payroll),
                            onActionClick = {
                                showPayrollRunEditor = true
                            }
                        )
                    }
                }

                // 3. ADVANCES TAB (Opens directly, respects Hide Amounts toggle)
                else -> {
                    item {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("staff_advances_summary_card"),
                            cornerRadius = 26.dp,
                            accentTint = AmberSale
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Staff Advances & Loans",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Remaining Balance across all employees",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    color = GoldPrimary.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.coming_soon_badge),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = GoldPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            AmountText(
                                amount = Money.ZERO,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = AmberSale,
                                masked = hideAmounts,
                                decimals = decimals
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            VanijyaButton(
                                text = "Record Staff Advance (Coming Soon)",
                                icon = Icons.Default.AccountBalanceWallet,
                                isOutlined = true,
                                onClick = {
                                    comingSoonBanner = "Recording staff advances and automatic EMI deductions is Coming Soon."
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    item {
                        VanijyaEmptyState(
                            icon = Icons.Default.AccountBalanceWallet,
                            title = "No Active Advances or Loans",
                            description = "Coming soon: Track salary advances, festival loans, and automatic monthly recovery from staff payslips with full ledger history.",
                            actionLabel = "Record Advance (Coming Soon)",
                            onActionClick = {
                                comingSoonBanner = "Staff advances ledger is Coming Soon in the Staff & Payroll step."
                            }
                        )
                    }
                }
            }
        }

        // Thumb-friendly Floating "Add Employee" Button on Employees Tab
        if (selectedTab == 1) {
            VanijyaExtendedFab(
                text = stringResource(R.string.staff_add_employee),
                onClick = { showAddEmployeeSheet = true },
                icon = Icons.Default.PersonAdd,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                testTag = "staff_fab_add_employee",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 104.dp)
            )
        }
    }

    if (showAddEmployeeSheet) {
        var empName by remember { mutableStateOf("") }
        var empRole by remember { mutableStateOf("") }
        var empSalaryType by remember { mutableStateOf("Monthly Salary") }
        GlassSheet(
            onDismissRequest = { showAddEmployeeSheet = false },
            title = "Add Employee (Coming Soon)",
            subtitle = "Preview of the Staff Profile form • Connects to SQLCipher in the Staff & Payroll step"
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = "Employee Photo",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column {
                    Text(
                        text = "Employee Photo & ID",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Zero-permission Photo Picker • Coming Soon",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldPrimary
                    )
                }
            }
            OutlinedTextField(
                value = empName,
                onValueChange = { empName = it },
                label = { Text("Employee Full Name") },
                singleLine = true,
                colors = vanijyaTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = empRole,
                onValueChange = { empRole = it },
                label = { Text("Designation / Role (e.g. Sales Executive, Cashier)") },
                singleLine = true,
                colors = vanijyaTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Monthly Salary", "Daily Wage", "Hourly / Piece-Rate").forEach { st ->
                    FilterChip(
                        selected = empSalaryType == st,
                        onClick = { empSalaryType = st },
                        label = { Text(st) },
                        colors = vanijyaFilterChipColors()
                    )
                }
            }
            VanijyaButton(
                text = "Save Employee (Coming Soon)",
                onClick = {
                    showAddEmployeeSheet = false
                    comingSoonBanner = "Employee database creation is Coming Soon in the Staff & Payroll step."
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showStaffRosterDetailScreen && !showStaffSalarySubScreen) {
        GlassSubScreen(
            onBack = { showStaffRosterDetailScreen = false },
            title = "Roster & Attendance Details",
            subtitle = "Daily Present, Absent, Half-day, Leave & Overtime Summary",
            screenTestTag = "staff_roster_detail_screen",
            backButtonTestTag = "staff_roster_detail_back_btn"
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp,
                accentTint = EmeraldPrimary,
                contentPadding = PaddingValues(16.dp)
            ) {
                Text(
                    text = "Attendance Roster Register",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Shift attendance, late arrivals, and overtime hours are computed offline.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                VanijyaButton(
                    text = "Salary & Overtime Breakdown",
                    icon = Icons.Default.Payments,
                    isOutlined = true,
                    onClick = { showStaffSalarySubScreen = true },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "staff_detail_open_salary_btn"
                )
            }
        }
    }

    if (showStaffRosterDetailScreen && showStaffSalarySubScreen) {
        GlassSubScreen(
            onBack = { showStaffSalarySubScreen = false },
            title = "Salary & Overtime Breakdown",
            subtitle = "Monthly Base Pay, Overtime Hours & Advance Deductions",
            screenTestTag = "staff_salary_sub_screen",
            backButtonTestTag = "staff_salary_sub_back_btn"
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp,
                accentTint = GoldPrimary,
                contentPadding = PaddingValues(16.dp)
            ) {
                Text(
                    text = "Attendance-Linked Salary Formula",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Net Pay = Earned Days + Overtime − Salary Advance Recovery",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showPayrollRunEditor) {
        GlassFullScreenEditor(
            onBack = { showPayrollRunEditor = false },
            title = "Run Payroll • $selectedPayrollMonth",
            subtitle = "Full-screen payroll worksheet • Computes net payouts from attendance & advances",
            screenTestTag = "payroll_run_full_screen_editor",
            backButtonTestTag = "payroll_run_back_btn"
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp,
                accentTint = EmeraldPrimary,
                contentPadding = PaddingValues(16.dp)
            ) {
                Text(
                    text = "$selectedPayrollMonth Payroll Worksheet",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Review Present/Absent/Half-day counts, overtime, and advance deductions before confirming monthly payslips.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                VanijyaButton(
                    text = "Confirm & Close Payroll Run",
                    icon = Icons.Default.CheckCircle,
                    onClick = {
                        showPayrollRunEditor = false
                        comingSoonBanner = "Payroll run for $selectedPayrollMonth previewed."
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "payroll_run_confirm_btn"
                )
            }
        }
    }
}
