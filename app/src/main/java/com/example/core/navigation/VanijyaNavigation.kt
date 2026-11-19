package com.example.core.navigation

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.core.database.AppPreferencesState
import com.example.core.database.AppUserEntity
import com.example.core.database.AuditLogEntity
import com.example.core.database.AutoLockTimeout
import com.example.core.database.CompanyEntity
import com.example.core.database.HsnMasterEntity
import com.example.core.database.ItemBatchEntity
import com.example.core.database.ItemBundleEntity
import com.example.core.database.ItemCategoryEntity
import com.example.core.database.ItemEntity
import com.example.core.database.ItemSerialEntity
import com.example.core.database.ItemUnitEntity
import com.example.core.database.ItemVariantEntity
import com.example.core.database.PartyAddressEntity
import com.example.core.database.PartyCustomFieldEntity
import com.example.core.database.PartyEntity
import com.example.core.database.PartyGroupEntity
import com.example.core.database.PartyLedgerEntryEntity
import com.example.core.database.ChequeEntity
import com.example.core.database.NumberSeriesEntity
import com.example.core.database.PaymentAllocationEntity
import com.example.core.database.PaymentEntity
import com.example.core.database.PriceListEntity
import com.example.core.database.RecurringTemplateEntity
import com.example.core.database.RolePermissionEntity
import com.example.core.database.RoundingModeOption
import com.example.core.database.StockAdjustmentEntity
import com.example.core.database.StockLedgerEntity
import com.example.core.database.StockTransferEntity
import com.example.core.database.TaxRateEntity
import com.example.core.database.ThemeMode
import com.example.core.database.TxnChargeEntity
import com.example.core.database.TxnEntity
import com.example.core.database.TxnLineEntity
import com.example.core.database.TxnLinkEntity
import com.example.core.database.TxnTaxLineEntity
import com.example.core.database.UnitConversionEntity
import com.example.core.database.WarehouseEntity
import com.example.core.designsystem.FloatingGlassNavBar
import com.example.core.designsystem.GlassBackgroundBox
import com.example.core.designsystem.GlassCard
import com.example.core.designsystem.GlassNavDestination
import com.example.core.designsystem.GlassSheet
import com.example.core.designsystem.SectionHeader
import com.example.core.designsystem.VanijyaButton
import com.example.core.designsystem.VanijyaVectorLogoMark
import com.example.core.designsystem.vanijyaTextFieldColors
import com.example.core.model.AppRole
import com.example.core.model.ItemImportPreviewRow
import com.example.core.model.Money
import com.example.core.model.PartyImportPreviewRow
import com.example.core.model.StockLedgerCalculator
import com.example.feature.home.HomeDashboardScreen
import com.example.feature.items.ItemsFoundationScreen
import com.example.feature.items.ItemsLaunchAction
import com.example.feature.parties.PartiesFoundationScreen
import com.example.feature.reports.ReportsFoundationScreen
import com.example.feature.sales.BillingLaunchAction
import com.example.feature.sales.SalesFoundationScreen
import com.example.feature.settings.SettingsScreen
import com.example.feature.staff.StaffFoundationScreen
import com.example.ui.theme.AmberSale
import com.example.ui.theme.CoralError
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IndigoDeep
import com.example.ui.theme.LocalVanijyaGlass
import com.example.ui.theme.PurchaseBlue
import com.example.ui.theme.VioletPrimary

data class MoreMenuItem(
    val route: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

private fun NavGraphBuilder.composableRoute(
    route: String,
    content: @Composable () -> Unit
) {
    composable(route = route) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VanijyaMainShell(
    prefs: AppPreferencesState,
    companies: List<CompanyEntity>,
    activeCompany: CompanyEntity?,
    users: List<AppUserEntity>,
    taxRates: List<TaxRateEntity>,
    rolePermissions: List<RolePermissionEntity>,
    hsnMasterList: List<HsnMasterEntity>,
    recentAuditLogs: List<AuditLogEntity>,
    onToggleHideAmounts: () -> Unit,
    onLockNow: () -> Unit,
    onSwitchCompany: (CompanyEntity) -> Unit,
    onCreateAdditionalCompany: (String, String) -> Unit,
    onSwitchUserByPin: (String, (Boolean, String) -> Unit) -> Unit,
    onSaveBusinessProfile: (CompanyEntity) -> Unit,
    onSaveInvoiceSettings: (String, Long, Int) -> Unit,
    onUpdateRoundingMode: (RoundingModeOption) -> Unit,
    onUpdateDecimals: (Int) -> Unit,
    onUpdateDateFormat: (String) -> Unit,
    onUpdateTheme: (ThemeMode) -> Unit,
    onUpdateLanguage: (String) -> Unit,
    onUpdateAutoLock: (AutoLockTimeout) -> Unit,
    onUpdateHideAmounts: (Boolean) -> Unit,
    onUpdateBlockScreenshots: (Boolean) -> Unit,
    onUpdateBiometricEnabled: (Boolean) -> Unit,
    onChangePin: (String, String, (Boolean, String) -> Unit) -> Unit,
    onAddLocalUser: (String, AppRole, String, (Boolean, String) -> Unit) -> Unit,
    onUpdateTaxRate: (TaxRateEntity) -> Unit,
    onUpdateRolePermission: (RolePermissionEntity) -> Unit,
    onVerifyPin: (String, (Boolean) -> Unit) -> Unit = { pin, cb -> cb(pin.length == 6) },
    onVerifyPinAndResetApp: (String, (Boolean) -> Unit) -> Unit,
    onBiometricResetApp: () -> Unit,
    parties: List<PartyEntity> = emptyList(),
    partyAddresses: List<PartyAddressEntity> = emptyList(),
    partyGroups: List<PartyGroupEntity> = emptyList(),
    partyCustomFields: List<PartyCustomFieldEntity> = emptyList(),
    partyLedgerEntries: List<PartyLedgerEntryEntity> = emptyList(),
    items: List<ItemEntity> = emptyList(),
    itemCategories: List<ItemCategoryEntity> = emptyList(),
    itemUnits: List<ItemUnitEntity> = emptyList(),
    unitConversions: List<UnitConversionEntity> = emptyList(),
    itemBatches: List<ItemBatchEntity> = emptyList(),
    itemSerials: List<ItemSerialEntity> = emptyList(),
    itemVariants: List<ItemVariantEntity> = emptyList(),
    warehouses: List<WarehouseEntity> = emptyList(),
    stockLedger: List<StockLedgerEntity> = emptyList(),
    itemBundles: List<ItemBundleEntity> = emptyList(),
    priceLists: List<PriceListEntity> = emptyList(),
    onSaveParty: (PartyEntity, List<PartyAddressEntity>, List<PartyCustomFieldEntity>) -> Unit = { _, _, _ -> },
    onRecordPartyLedgerMovement: (PartyLedgerEntryEntity) -> Unit = {},
    onDeleteParties: (List<String>) -> Unit = {},
    onBulkImportParties: (List<PartyImportPreviewRow>, (Int) -> Unit) -> Unit = { _, cb -> cb(0) },
    onSaveItem: (ItemEntity, ItemBatchEntity?, List<ItemVariantEntity>, List<ItemSerialEntity>, List<ItemBundleEntity>) -> Unit = { _, _, _, _, _ -> },
    onRecordStockAdjustment: (StockAdjustmentEntity) -> Unit = {},
    onRecordStockTransfer: (StockTransferEntity) -> Unit = {},
    onBulkUpdateItemPricesAndTax: (List<String>, Int, Int?, (Int) -> Unit) -> Unit = { _, _, _, cb -> cb(0) },
    onBulkImportItems: (List<ItemImportPreviewRow>, (Int) -> Unit) -> Unit = { _, cb -> cb(0) },
    onSaveItemCategory: (String, String) -> Unit = { _, _ -> },
    onSaveWarehouse: (String, String, String, String, String) -> Unit = { _, _, _, _, _ -> },
    numberSeries: List<NumberSeriesEntity> = emptyList(),
    transactions: List<TxnEntity> = emptyList(),
    txnLines: List<TxnLineEntity> = emptyList(),
    txnCharges: List<TxnChargeEntity> = emptyList(),
    txnTaxLines: List<TxnTaxLineEntity> = emptyList(),
    txnLinks: List<TxnLinkEntity> = emptyList(),
    payments: List<PaymentEntity> = emptyList(),
    paymentAllocations: List<PaymentAllocationEntity> = emptyList(),
    cheques: List<ChequeEntity> = emptyList(),
    recurringTemplates: List<RecurringTemplateEntity> = emptyList(),
    onSaveOrUpdateDocument: (
        TxnEntity,
        List<TxnLineEntity>,
        List<TxnChargeEntity>,
        List<TxnTaxLineEntity>,
        String?,
        String?,
        Long,
        String
    ) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onCancelDocument: (String, String) -> Unit = { _, _ -> },
    onConvertDocument: (String, String) -> Unit = { _, _ -> },
    onRecordPayment: (PaymentEntity, List<PaymentAllocationEntity>, ChequeEntity?) -> Unit = { _, _, _ -> },
    onUpdateChequeStatus: (String, String, Long, String) -> Unit = { _, _, _, _ -> },
    onSaveRecurringTemplate: (RecurringTemplateEntity) -> Unit = {},
    onRunRecurringNow: (String) -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
    val view = LocalView.current
    val glass = LocalVanijyaGlass.current

    // Shared Top-Bar Sheets & Dialogs across all 5 main tabs
    var showNotificationsSheet by rememberSaveable { mutableStateOf(false) }
    var showProfileAvatarSheet by rememberSaveable { mutableStateOf(false) }
    var showSwitchFirmSheet by rememberSaveable { mutableStateOf(false) }
    var showAddBusinessDialog by rememberSaveable { mutableStateOf(false) }
    var showSwitchUserPinDialog by rememberSaveable { mutableStateOf(false) }
    var showHelpSheet by rememberSaveable { mutableStateOf(false) }
    var showAboutSheet by rememberSaveable { mutableStateOf(false) }

    // Full-screen editor visibility flags (hide bottom bar & tablet rail when any of the 4 full-screen editors is open)
    var isBillingBuilderOpen by rememberSaveable { mutableStateOf(false) }
    var isPartyFormOpen by rememberSaveable { mutableStateOf(false) }
    var isItemFormOpen by rememberSaveable { mutableStateOf(false) }
    var isPayrollRunOpen by rememberSaveable { mutableStateOf(false) }
    val isAnyFullScreenEditorOpen = isBillingBuilderOpen || isPartyFormOpen || isItemFormOpen || isPayrollRunOpen

    // Per-tab reselect counters (tapping active tab scrolls to top & resets deep sub-screens)
    var homeReselectTrigger by rememberSaveable { mutableIntStateOf(0) }
    var billingReselectTrigger by rememberSaveable { mutableIntStateOf(0) }
    var partiesReselectTrigger by rememberSaveable { mutableIntStateOf(0) }
    var staffReselectTrigger by rememberSaveable { mutableIntStateOf(0) }
    var stockReselectTrigger by rememberSaveable { mutableIntStateOf(0) }

    // Pending launch actions when navigating via quick-action shortcuts
    var pendingBillingSegment by rememberSaveable { mutableIntStateOf(0) }
    var pendingBillingActionName by rememberSaveable { mutableStateOf(BillingLaunchAction.NONE.name) }
    var pendingAddParty by rememberSaveable { mutableStateOf(false) }
    var pendingStockActionName by rememberSaveable { mutableStateOf(ItemsLaunchAction.NONE.name) }

    val billingRoutes = remember {
        setOf(
            "billing",
            "sales",
            "purchases",
            "billing_new_sale",
            "billing_new_purchase",
            "billing_new_quotation",
            "billing_sale_order",
            "billing_purchase_order",
            "billing_challan",
            "billing_return",
            "billing_payment_in",
            "billing_cheques",
            "billing_recurring"
        )
    }

    val stockRoutes = remember {
        setOf(
            "stock",
            "items",
            "items_add",
            "items_adjust",
            "items_scan",
            "import_export"
        )
    }

    val overlayRoutes = remember {
        setOf("reports", "settings", "more", "cash", "expenses", "gst", "backup", "daybook")
    }

    val mainTabRoutes = remember {
        billingRoutes + stockRoutes + setOf("home", "parties", "parties_add", "staff", "payroll", "reports")
    }

    val closeAllTopBarOverlays: () -> Unit = {
        showNotificationsSheet = false
        showProfileAvatarSheet = false
        showSwitchFirmSheet = false
        showAddBusinessDialog = false
        showSwitchUserPinDialog = false
        showHelpSheet = false
        showAboutSheet = false
    }

    val navigateToTab: (String) -> Unit = { target ->
        closeAllTopBarOverlays()
        val activeCanonicalTab = when (currentRoute) {
            "home", "dashboard" -> "home"
            in billingRoutes -> "billing"
            "parties", "parties_add" -> "parties"
            "staff", "payroll" -> "staff"
            in stockRoutes -> "stock"
            else -> currentRoute
        }

        if (target == "home") {
            // Tapping Dashboard from ANY screen (including deep screens in Billing, Parties, Staff, Stock, Reports, Settings)
            // ALWAYS returns to the Dashboard root and scrolls/resets it.
            isBillingBuilderOpen = false
            isPartyFormOpen = false
            isItemFormOpen = false
            isPayrollRunOpen = false
            homeReselectTrigger++
            if (currentRoute != "home") {
                if (currentRoute in overlayRoutes) {
                    navController.popBackStack("home", inclusive = false)
                } else {
                    navController.navigate("home") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        } else if (target == activeCanonicalTab) {
            // Tapping the active tab again scrolls that tab to the top and closes any open sub-screen
            when (target) {
                "billing" -> billingReselectTrigger++
                "parties" -> partiesReselectTrigger++
                "staff" -> staffReselectTrigger++
                "stock" -> stockReselectTrigger++
            }
        } else {
            // If currently on an overlay route (like reports or settings), pop it before switching tabs
            // so the overlay is not trapped inside the home tab's saved state.
            if (currentRoute in overlayRoutes) {
                navController.popBackStack("home", inclusive = false)
            }
            navController.navigate(target) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val navigateToRoute: (String) -> Unit = { rawTarget ->
        closeAllTopBarOverlays()
        when (rawTarget) {
            "home", "dashboard" -> navigateToTab("home")
            "billing", "sales" -> {
                pendingBillingSegment = 0
                pendingBillingActionName = BillingLaunchAction.NONE.name
                navigateToTab("billing")
            }
            "purchases" -> {
                pendingBillingSegment = 1
                pendingBillingActionName = BillingLaunchAction.NONE.name
                navigateToTab("billing")
            }
            "billing_new_purchase" -> {
                pendingBillingSegment = 1
                pendingBillingActionName = BillingLaunchAction.NEW_PURCHASE.name
                navigateToTab("billing")
            }
            "billing_new_sale" -> {
                pendingBillingSegment = 0
                pendingBillingActionName = BillingLaunchAction.NEW_SALE.name
                navigateToTab("billing")
            }
            "billing_new_quotation" -> {
                pendingBillingSegment = 2
                pendingBillingActionName = BillingLaunchAction.NEW_QUOTATION.name
                navigateToTab("billing")
            }
            "billing_sale_order" -> {
                pendingBillingSegment = 3
                pendingBillingActionName = BillingLaunchAction.NEW_SALE_ORDER.name
                navigateToTab("billing")
            }
            "billing_purchase_order" -> {
                pendingBillingSegment = 3
                pendingBillingActionName = BillingLaunchAction.NEW_PURCHASE_ORDER.name
                navigateToTab("billing")
            }
            "billing_challan" -> {
                pendingBillingSegment = 4
                pendingBillingActionName = BillingLaunchAction.NEW_CHALLAN.name
                navigateToTab("billing")
            }
            "billing_return" -> {
                pendingBillingSegment = 5
                pendingBillingActionName = BillingLaunchAction.NEW_RETURN.name
                navigateToTab("billing")
            }
            "billing_payment_in" -> {
                pendingBillingSegment = 6
                pendingBillingActionName = BillingLaunchAction.NEW_PAYMENT_IN.name
                navigateToTab("billing")
            }
            "billing_cheques" -> {
                pendingBillingSegment = 6
                pendingBillingActionName = BillingLaunchAction.OPEN_CHEQUES.name
                navigateToTab("billing")
            }
            "billing_recurring" -> {
                pendingBillingSegment = 0
                pendingBillingActionName = BillingLaunchAction.OPEN_RECURRING.name
                navigateToTab("billing")
            }
            "parties" -> {
                pendingAddParty = false
                navigateToTab("parties")
            }
            "parties_add" -> {
                pendingAddParty = true
                navigateToTab("parties")
            }
            "staff", "payroll" -> navigateToTab("staff")
            "stock", "items" -> {
                pendingStockActionName = ItemsLaunchAction.NONE.name
                navigateToTab("stock")
            }
            "items_add" -> {
                pendingStockActionName = ItemsLaunchAction.ADD_ITEM.name
                navigateToTab("stock")
            }
            "items_adjust" -> {
                pendingStockActionName = ItemsLaunchAction.STOCK_ADJUSTMENT.name
                navigateToTab("stock")
            }
            "items_scan" -> {
                pendingStockActionName = ItemsLaunchAction.SCAN_BARCODE.name
                navigateToTab("stock")
            }
            "import_export" -> {
                pendingStockActionName = ItemsLaunchAction.IMPORT_EXPORT.name
                navigateToTab("stock")
            }
            else -> {
                if (rawTarget != currentRoute) {
                    navController.navigate(rawTarget) {
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    val navigateBack: () -> Unit = {
        if (!navController.popBackStack()) {
            navController.navigate("home") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    if (currentRoute != "home") {
        BackHandler {
            navigateBack()
        }
    }

    val dashboardLabel = stringResource(R.string.nav_dashboard)
    val billingLabel = stringResource(R.string.nav_billing)
    val partiesLabel = stringResource(R.string.nav_parties)
    val staffLabel = stringResource(R.string.nav_staff)
    val stockLabel = stringResource(R.string.nav_stock)

    // 5 Bottom Navigation Tabs: 1. Dashboard, 2. Billing, 3. Parties, 4. Staff, 5. Stock
    val navDestinations = remember(dashboardLabel, billingLabel, partiesLabel, staffLabel, stockLabel) {
        listOf(
            GlassNavDestination(
                route = "home",
                label = dashboardLabel,
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                testTag = "bottom_nav_home",
                matchRoutes = setOf("home", "dashboard")
            ),
            GlassNavDestination(
                route = "billing",
                label = billingLabel,
                selectedIcon = Icons.AutoMirrored.Filled.ReceiptLong,
                unselectedIcon = Icons.AutoMirrored.Outlined.ReceiptLong,
                testTag = "bottom_nav_billing",
                matchRoutes = billingRoutes
            ),
            GlassNavDestination(
                route = "parties",
                label = partiesLabel,
                selectedIcon = Icons.Filled.People,
                unselectedIcon = Icons.Outlined.People,
                testTag = "bottom_nav_parties",
                matchRoutes = setOf("parties", "parties_add")
            ),
            GlassNavDestination(
                route = "staff",
                label = staffLabel,
                selectedIcon = Icons.Filled.Badge,
                unselectedIcon = Icons.Outlined.Badge,
                testTag = "bottom_nav_staff",
                matchRoutes = setOf("staff", "payroll")
            ),
            GlassNavDestination(
                route = "stock",
                label = stockLabel,
                selectedIcon = Icons.Filled.Inventory2,
                unselectedIcon = Icons.Outlined.Inventory2,
                testTag = "bottom_nav_stock",
                matchRoutes = stockRoutes
            )
        )
    }

    val currentTopBarTitle = when (currentRoute) {
        "home", "dashboard" -> stringResource(R.string.nav_dashboard)
        in billingRoutes -> stringResource(R.string.nav_billing)
        "parties", "parties_add" -> stringResource(R.string.nav_parties)
        "staff", "payroll" -> stringResource(R.string.staff_title)
        in stockRoutes -> stringResource(R.string.nav_stock)
        "reports" -> stringResource(R.string.nav_reports)
        else -> stringResource(R.string.app_name)
    }

    GlassBackgroundBox(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isTabletOrExpanded = maxWidth >= 600.dp

            Row(modifier = Modifier.fillMaxSize()) {
                if (isTabletOrExpanded && !isAnyFullScreenEditorOpen) {
                    NavigationRail(
                        containerColor = glass.cardFillElevated,
                        modifier = Modifier
                            .fillMaxHeight()
                            .border(
                                width = 1.dp,
                                color = glass.borderGradientStart,
                                shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                            )
                            .testTag("tablet_navigation_rail")
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        navDestinations.forEach { dest ->
                            val selected = currentRoute in dest.matchRoutes
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    navigateToTab(dest.route)
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label) },
                                alwaysShowLabel = true,
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Every main tab has a top app bar with: screen title, chart icon (on Dashboard), gear icon (Settings), notification bell, and profile avatar
                        if (currentRoute in mainTabRoutes && !isAnyFullScreenEditorOpen) {
                            VanijyaMainTopBar(
                                title = currentTopBarTitle,
                                showBrandMark = currentRoute == "home",
                                hideAmounts = prefs.hideAmounts,
                                activeUserName = prefs.activeUserName,
                                onToggleHideAmounts = onToggleHideAmounts,
                                onOpenReports = { navigateToRoute("reports") },
                                onOpenSettings = { navigateToRoute("settings") },
                                onOpenNotifications = { showNotificationsSheet = true },
                                onOpenAvatarSheet = { showProfileAvatarSheet = true }
                            )
                        }

                        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                            NavHost(
                                navController = navController,
                                startDestination = "home",
                                modifier = Modifier.fillMaxSize()
                            ) {
                                composableRoute("home") {
                                    HomeDashboardScreen(
                                        prefs = prefs,
                                        companies = companies,
                                        activeCompany = activeCompany,
                                        recentAuditLogs = recentAuditLogs,
                                        onToggleHideAmounts = onToggleHideAmounts,
                                        onLockNow = onLockNow,
                                        onSwitchCompany = onSwitchCompany,
                                        onCreateAdditionalCompany = onCreateAdditionalCompany,
                                        onSwitchUserByPin = onSwitchUserByPin,
                                        onNavigateToRoute = { navigateToRoute(it) },
                                        parties = parties,
                                        items = items,
                                        itemBatches = itemBatches,
                                        transactions = transactions,
                                        payments = payments,
                                        cheques = cheques,
                                        reselectTrigger = homeReselectTrigger
                                    )
                                }
                                val renderBillingScreen: @Composable (Int, BillingLaunchAction) -> Unit = { segIdx, action ->
                                    SalesFoundationScreen(
                                        invoicePrefix = prefs.invoicePrefix,
                                        hideAmounts = prefs.hideAmounts,
                                        decimals = prefs.decimalPlaces,
                                        initialSegmentIndex = segIdx,
                                        launchAction = action,
                                        onConsumeLaunchAction = {
                                            pendingBillingActionName = BillingLaunchAction.NONE.name
                                        },
                                        reselectTrigger = billingReselectTrigger,
                                        activeCompany = activeCompany,
                                        roundingMode = prefs.roundingMode,
                                        taxRates = taxRates,
                                        numberSeries = numberSeries,
                                        parties = parties,
                                        items = items,
                                        itemBatches = itemBatches,
                                        itemSerials = itemSerials,
                                        transactions = transactions,
                                        txnLines = txnLines,
                                        txnCharges = txnCharges,
                                        txnTaxLines = txnTaxLines,
                                        txnLinks = txnLinks,
                                        payments = payments,
                                        paymentAllocations = paymentAllocations,
                                        cheques = cheques,
                                        recurringTemplates = recurringTemplates,
                                        onBuilderOpenChange = { isBillingBuilderOpen = it },
                                        onSaveOrUpdateDocument = onSaveOrUpdateDocument,
                                        onCancelDocument = onCancelDocument,
                                        onConvertDocument = onConvertDocument,
                                        onRecordPayment = onRecordPayment,
                                        onUpdateChequeStatus = onUpdateChequeStatus,
                                        onSaveRecurringTemplate = onSaveRecurringTemplate,
                                        onRunRecurringNow = onRunRecurringNow,
                                        onQuickAddParty = onSaveParty
                                    )
                                }
                                composableRoute("billing") {
                                    val launchAct = runCatching {
                                        BillingLaunchAction.valueOf(pendingBillingActionName)
                                    }.getOrDefault(BillingLaunchAction.NONE)
                                    renderBillingScreen(pendingBillingSegment, launchAct)
                                }
                                composableRoute("sales") { renderBillingScreen(0, BillingLaunchAction.NONE) }
                                composableRoute("purchases") { renderBillingScreen(1, BillingLaunchAction.NONE) }
                                composableRoute("billing_new_sale") { renderBillingScreen(0, BillingLaunchAction.NEW_SALE) }
                                composableRoute("billing_new_purchase") { renderBillingScreen(1, BillingLaunchAction.NEW_PURCHASE) }
                                composableRoute("billing_new_quotation") { renderBillingScreen(2, BillingLaunchAction.NEW_QUOTATION) }
                                composableRoute("billing_sale_order") { renderBillingScreen(3, BillingLaunchAction.NEW_SALE_ORDER) }
                                composableRoute("billing_purchase_order") { renderBillingScreen(3, BillingLaunchAction.NEW_PURCHASE_ORDER) }
                                composableRoute("billing_challan") { renderBillingScreen(4, BillingLaunchAction.NEW_CHALLAN) }
                                composableRoute("billing_return") { renderBillingScreen(5, BillingLaunchAction.NEW_RETURN) }
                                composableRoute("billing_payment_in") { renderBillingScreen(6, BillingLaunchAction.NEW_PAYMENT_IN) }
                                composableRoute("billing_cheques") { renderBillingScreen(6, BillingLaunchAction.OPEN_CHEQUES) }
                                composableRoute("billing_recurring") { renderBillingScreen(0, BillingLaunchAction.OPEN_RECURRING) }
                                composableRoute("parties") {
                                    PartiesFoundationScreen(
                                        hideAmounts = prefs.hideAmounts,
                                        decimals = prefs.decimalPlaces,
                                        activeCompany = activeCompany,
                                        partiesList = parties,
                                        partyAddresses = partyAddresses,
                                        partyGroups = partyGroups,
                                        partyCustomFields = partyCustomFields,
                                        partyLedgerEntries = partyLedgerEntries,
                                        priceLists = priceLists,
                                        openAddPartyOnLaunch = pendingAddParty,
                                        onConsumeOpenAddParty = { pendingAddParty = false },
                                        onFullScreenEditorOpenChange = { isPartyFormOpen = it },
                                        reselectTrigger = partiesReselectTrigger,
                                        onSaveParty = onSaveParty,
                                        onRecordLedgerEntry = onRecordPartyLedgerMovement,
                                        onDeleteParties = onDeleteParties,
                                        onBulkImportParties = onBulkImportParties
                                    )
                                }
                                composableRoute("parties_add") {
                                    PartiesFoundationScreen(
                                        hideAmounts = prefs.hideAmounts,
                                        decimals = prefs.decimalPlaces,
                                        activeCompany = activeCompany,
                                        partiesList = parties,
                                        partyAddresses = partyAddresses,
                                        partyGroups = partyGroups,
                                        partyCustomFields = partyCustomFields,
                                        partyLedgerEntries = partyLedgerEntries,
                                        priceLists = priceLists,
                                        openAddPartyOnLaunch = true,
                                        onConsumeOpenAddParty = { pendingAddParty = false },
                                        onFullScreenEditorOpenChange = { isPartyFormOpen = it },
                                        reselectTrigger = partiesReselectTrigger,
                                        onSaveParty = onSaveParty,
                                        onRecordLedgerEntry = onRecordPartyLedgerMovement,
                                        onDeleteParties = onDeleteParties,
                                        onBulkImportParties = onBulkImportParties
                                    )
                                }
                                composableRoute("staff") {
                                    StaffFoundationScreen(
                                        hideAmounts = prefs.hideAmounts,
                                        decimals = prefs.decimalPlaces,
                                        onFullScreenEditorOpenChange = { isPayrollRunOpen = it },
                                        reselectTrigger = staffReselectTrigger
                                    )
                                }
                                composableRoute("payroll") {
                                    StaffFoundationScreen(
                                        hideAmounts = prefs.hideAmounts,
                                        decimals = prefs.decimalPlaces,
                                        initialTabIndex = 2,
                                        onFullScreenEditorOpenChange = { isPayrollRunOpen = it },
                                        reselectTrigger = staffReselectTrigger
                                    )
                                }
                                val renderStockScreen: @Composable (ItemsLaunchAction, (() -> Unit)?) -> Unit = { action, backCb ->
                                    ItemsFoundationScreen(
                                        hsnMasterList = hsnMasterList,
                                        hideAmounts = prefs.hideAmounts,
                                        decimals = prefs.decimalPlaces,
                                        activeCompany = activeCompany,
                                        itemsList = items,
                                        categoriesList = itemCategories,
                                        unitsList = itemUnits,
                                        conversionsList = unitConversions,
                                        batchesList = itemBatches,
                                        serialsList = itemSerials,
                                        variantsList = itemVariants,
                                        warehousesList = warehouses,
                                        stockLedgerList = stockLedger,
                                        bundlesList = itemBundles,
                                        launchAction = action,
                                        onConsumeLaunchAction = {
                                            pendingStockActionName = ItemsLaunchAction.NONE.name
                                        },
                                        onBack = backCb,
                                        onFullScreenEditorOpenChange = { isItemFormOpen = it },
                                        reselectTrigger = stockReselectTrigger,
                                        onSaveItem = onSaveItem,
                                        onRecordStockAdjustment = onRecordStockAdjustment,
                                        onRecordStockTransfer = onRecordStockTransfer,
                                        onBulkUpdatePricesAndTax = onBulkUpdateItemPricesAndTax,
                                        onBulkImportItems = onBulkImportItems,
                                        onSaveCategory = onSaveItemCategory,
                                        onSaveWarehouse = onSaveWarehouse
                                    )
                                }
                                composableRoute("stock") {
                                    val launchAct = runCatching {
                                        ItemsLaunchAction.valueOf(pendingStockActionName)
                                    }.getOrDefault(ItemsLaunchAction.NONE)
                                    renderStockScreen(launchAct, null)
                                }
                                composableRoute("items") { renderStockScreen(ItemsLaunchAction.NONE, navigateBack) }
                                composableRoute("items_add") { renderStockScreen(ItemsLaunchAction.ADD_ITEM, navigateBack) }
                                composableRoute("items_adjust") { renderStockScreen(ItemsLaunchAction.STOCK_ADJUSTMENT, navigateBack) }
                                composableRoute("items_scan") { renderStockScreen(ItemsLaunchAction.SCAN_BARCODE, navigateBack) }
                                composableRoute("import_export") { renderStockScreen(ItemsLaunchAction.IMPORT_EXPORT, navigateBack) }
                                composableRoute("reports") {
                                    ReportsFoundationScreen(
                                        auditLogs = recentAuditLogs,
                                        activeCompany = activeCompany,
                                        parties = parties,
                                        partyLedgerEntries = partyLedgerEntries,
                                        items = items,
                                        itemBatches = itemBatches,
                                        transactions = transactions,
                                        hideAmounts = prefs.hideAmounts,
                                        decimals = prefs.decimalPlaces,
                                        onBack = navigateBack
                                    )
                                }
                                composableRoute("more") {
                                    MoreHubScreen(onNavigate = { route -> navigateToRoute(route) })
                                }
                                composableRoute("settings") {
                                    SettingsScreen(
                                        prefs = prefs,
                                        companies = companies,
                                        activeCompany = activeCompany,
                                        users = users,
                                        taxRates = taxRates,
                                        rolePermissions = rolePermissions,
                                        onBack = navigateBack,
                                        onUpdateTheme = onUpdateTheme,
                                        onUpdateLanguage = onUpdateLanguage,
                                        onSaveBusinessProfile = onSaveBusinessProfile,
                                        onSaveInvoiceSettings = onSaveInvoiceSettings,
                                        onUpdateRoundingMode = onUpdateRoundingMode,
                                        onUpdateDecimals = onUpdateDecimals,
                                        onUpdateDateFormat = onUpdateDateFormat,
                                        onChangePin = onChangePin,
                                        onUpdateBiometricEnabled = onUpdateBiometricEnabled,
                                        onUpdateAutoLock = onUpdateAutoLock,
                                        onUpdateHideAmounts = onUpdateHideAmounts,
                                        onUpdateBlockScreenshots = onUpdateBlockScreenshots,
                                        onAddLocalUser = onAddLocalUser,
                                        onUpdateRolePermission = onUpdateRolePermission,
                                        onUpdateTaxRate = onUpdateTaxRate,
                                        onSwitchCompany = onSwitchCompany,
                                        onCreateAdditionalCompany = onCreateAdditionalCompany,
                                        onVerifyPin = onVerifyPin,
                                        onVerifyPinAndResetApp = onVerifyPinAndResetApp,
                                        onBiometricResetApp = onBiometricResetApp
                                    )
                                }
                                composableRoute("cash") {
                                    ComingSoonModuleScreen(
                                        moduleName = stringResource(R.string.more_cash_bank),
                                        moduleDetails = "Cash in Hand ledger, Bank Accounts, Payment-In, Payment-Out, Cheques & offline UPI QR code generator.",
                                        icon = Icons.Default.AccountBalanceWallet,
                                        onBack = navigateBack
                                    )
                                }
                                composableRoute("expenses") {
                                    ComingSoonModuleScreen(
                                        moduleName = stringResource(R.string.more_expenses),
                                        moduleDetails = "Direct & Indirect Business Expense Vouchers with GST and non-GST categories.",
                                        icon = Icons.Default.Receipt,
                                        onBack = navigateBack
                                    )
                                }
                                composableRoute("gst") {
                                    ComingSoonModuleScreen(
                                        moduleName = stringResource(R.string.more_gst),
                                        moduleDetails = "GSTR-1, GSTR-3B, HSN/SAC Tax Summary, and ${taxRates.size} editable Indian GST slabs (0% to 40% + Cess).",
                                        icon = Icons.Default.Percent,
                                        onBack = navigateBack
                                    )
                                }
                                composableRoute("backup") {
                                    ComingSoonModuleScreen(
                                        moduleName = "Backup and Restore",
                                        moduleDetails = "Single-file encrypted .vbk archive backup and full offline restore on a fresh install.",
                                        icon = Icons.Default.Backup,
                                        onBack = navigateBack
                                    )
                                }
                                composableRoute("daybook") {
                                    ComingSoonModuleScreen(
                                        moduleName = "Day Book",
                                        moduleDetails = "Chronological daily voucher register across Sales, Purchases, Receipts, Payments, and Expenses.",
                                        icon = Icons.Default.MenuBook,
                                        onBack = navigateBack
                                    )
                                }
                            }
                        }
                    }

                    // Floating Pill-Shaped Glass Bottom Navigation Bar on compact phones (hidden on full-screen editors)
                    if (!isTabletOrExpanded && !isAnyFullScreenEditorOpen) {
                        FloatingGlassNavBar(
                            destinations = navDestinations,
                            currentRoute = currentRoute,
                            onSelectRoute = { route -> navigateToTab(route) },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }

    // TOP-BAR SHEET 1: Notifications Bell Sheet
    if (showNotificationsSheet) {
        val stockMetrics = remember(items, itemBatches) {
            StockLedgerCalculator.computeRealTimeStockMetrics(items, itemBatches)
        }
        val lowStockCount = if (items.isNotEmpty()) stockMetrics.lowStockItems.size else 3
        val expiringCount = if (items.isNotEmpty() || itemBatches.isNotEmpty()) stockMetrics.expiringBatches.size else 2
        val topDueParty = remember(parties) {
            parties.filter { !it.isDeleted && it.currentBalancePaise > 0L }
                .maxByOrNull { it.currentBalancePaise }
        }
        val dueDescription = if (topDueParty != null) {
            "${Money(topDueParty.currentBalancePaise).formatIndian(decimals = prefs.decimalPlaces)} due from ${topDueParty.name}"
        } else {
            "₹42,800 overdue from Shree Balaji Distributors"
        }
        val lowStockNames = stockMetrics.lowStockItems.take(2).joinToString(", ") { it.name }
        val lowStockDescription = if (lowStockNames.isNotBlank()) {
            "$lowStockCount SKUs below reorder level ($lowStockNames)"
        } else {
            "$lowStockCount SKUs below reorder level in Main Godown"
        }
        val expiringNames = stockMetrics.expiringBatches.take(2).joinToString(", ") { it.batchNumber }
        val expiringDescription = if (expiringNames.isNotBlank()) {
            "$expiringCount batches expiring soon ($expiringNames)"
        } else {
            "$expiringCount FMCG batches expiring within 60 days"
        }
        GlassSheet(
            onDismissRequest = { showNotificationsSheet = false },
            title = "Notifications & Reminders",
            subtitle = "100% on-device alerts computed from your local SQLCipher database"
        ) {
            val notifications = listOf(
                Triple("Due Payments", dueDescription, EmeraldPrimary to "parties"),
                Triple("Low Stock Alert", lowStockDescription, GoldPrimary to "stock"),
                Triple("Batch Expiry Warning", expiringDescription, AmberSale to "stock"),
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
                        navigateToRoute(route)
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

    // TOP-BAR SHEET 2: Profile Avatar Glass Sheet
    // Includes: profile and business name, Switch Firm, Switch User, Settings, Backup and Restore, Lock app now, Help, About
    if (showProfileAvatarSheet) {
        val firmName = activeCompany?.businessName?.ifBlank { "A.K. Enterprises" } ?: "A.K. Enterprises"
        GlassSheet(
            onDismissRequest = { showProfileAvatarSheet = false },
            title = "${prefs.activeUserName} (${prefs.activeUserRole.displayName})",
            subtitle = "$firmName • ${activeCompany?.stateName ?: "Maharashtra"}"
        ) {
            val avatarActions = listOf(
                Triple(stringResource(R.string.avatar_switch_firm), Icons.Default.Business, "switch_firm"),
                Triple(stringResource(R.string.avatar_switch_user), Icons.Default.SwitchAccount, "switch_user"),
                Triple(stringResource(R.string.nav_reports), Icons.Default.Assessment, "reports"),
                Triple(stringResource(R.string.avatar_settings), Icons.Default.Settings, "settings"),
                Triple(stringResource(R.string.avatar_backup_restore), Icons.Default.Backup, "backup"),
                Triple(stringResource(R.string.avatar_lock_now), Icons.Default.Lock, "lock"),
                Triple(stringResource(R.string.avatar_help), Icons.AutoMirrored.Filled.HelpOutline, "help"),
                Triple(stringResource(R.string.avatar_about), Icons.Default.Info, "about")
            )

            avatarActions.forEach { (label, icon, actionKey) ->
                val isLock = actionKey == "lock"
                val accent = if (isLock) CoralError else MaterialTheme.colorScheme.primary
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("avatar_menu_$actionKey"),
                    cornerRadius = 18.dp,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    onClick = {
                        showProfileAvatarSheet = false
                        when (actionKey) {
                            "switch_firm" -> showSwitchFirmSheet = true
                            "switch_user" -> showSwitchUserPinDialog = true
                            "reports" -> navigateToRoute("reports")
                            "settings" -> navigateToRoute("settings")
                            "backup" -> navigateToRoute("backup")
                            "lock" -> onLockNow()
                            "help" -> showHelpSheet = true
                            "about" -> showAboutSheet = true
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isLock) CoralError else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = label,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }

    // TOP-BAR SHEET 3: Switch Firm Glass Sheet
    if (showSwitchFirmSheet) {
        GlassSheet(
            onDismissRequest = { showSwitchFirmSheet = false },
            title = stringResource(R.string.avatar_switch_firm),
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
                        showSwitchFirmSheet = false
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
                    showSwitchFirmSheet = false
                    showAddBusinessDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // TOP-BAR SHEET 4: Help Sheet
    if (showHelpSheet) {
        GlassSheet(
            onDismissRequest = { showHelpSheet = false },
            title = "Vanijya Offline Help & Guide",
            subtitle = "100% On-Device Business Management"
        ) {
            GlassCard(cornerRadius = 20.dp) {
                Text(
                    text = "Quick Navigation Guide",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Bottom Bar: Switch between Dashboard, Billing, Parties, Staff, and Stock.\n" +
                        "• Reports: Open from the Dashboard top bar chart icon, Dashboard All Features tile, or Avatar menu.\n" +
                        "• Top Bar Gear Icon: Open full Settings from any tab.\n" +
                        "• Role Switching: Use 6-digit PINs (Owner 123456, Manager 222222, Sales 333333, Accountant 444444, Viewer 555555).\n" +
                        "• Hide Amounts: Toggle the eye icon in the top bar or in Settings > Security to mask amounts across all tabs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // TOP-BAR SHEET 5: About Sheet
    if (showAboutSheet) {
        GlassSheet(
            onDismissRequest = { showAboutSheet = false },
            title = "About Vanijya v1.0.0",
            subtitle = stringResource(R.string.settings_works_offline)
        ) {
            GlassCard(cornerRadius = 20.dp) {
                Text(
                    text = stringResource(R.string.badge_offline_encrypted),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.settings_privacy_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            VanijyaButton(
                text = "Open Full Settings",
                icon = Icons.Default.Settings,
                onClick = {
                    showAboutSheet = false
                    navigateToRoute("settings")
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

    if (showSwitchUserPinDialog) {
        var rolePin by remember { mutableStateOf("") }
        var feedback by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showSwitchUserPinDialog = false },
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
                                showSwitchUserPinDialog = false
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
                TextButton(onClick = { showSwitchUserPinDialog = false }) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}

/**
 * Shared Top App Bar rendered across all 5 main tabs (Dashboard, Billing, Parties, Staff, Reports).
 * Contains: screen title (plus brand mark on Dashboard), gear icon (Settings), notification bell, and profile avatar.
 */
@Composable
private fun VanijyaMainTopBar(
    title: String,
    showBrandMark: Boolean,
    hideAmounts: Boolean,
    activeUserName: String,
    onToggleHideAmounts: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenAvatarSheet: () -> Unit
) {
    val glass = LocalVanijyaGlass.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("vanijya_main_top_bar"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (showBrandMark) {
                VanijyaVectorLogoMark(size = 36.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                VanijyaVectorLogoMark(size = 32.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hide / Show Amounts quick button
            IconButton(
                onClick = onToggleHideAmounts,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(glass.pillFill)
                    .border(1.dp, glass.borderGradientStart, CircleShape)
                    .testTag("toggle_hide_amounts_btn")
            ) {
                Icon(
                    imageVector = if (hideAmounts) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (hideAmounts) "Show Amounts" else "Hide Amounts",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Dashboard Top Bar Chart Icon for Reports
            if (showBrandMark) {
                IconButton(
                    onClick = onOpenReports,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(glass.pillFill)
                        .border(1.dp, glass.borderGradientStart, CircleShape)
                        .testTag("dashboard_top_bar_reports_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = stringResource(R.string.nav_reports),
                        tint = VioletPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Settings Gear Icon (opens Settings from every main tab)
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(glass.pillFill)
                    .border(1.dp, glass.borderGradientStart, CircleShape)
                    .testTag("home_top_bar_settings_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.nav_settings),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Notification Bell (due payments, low stock, expiry, backup reminder)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(glass.pillFill)
                    .border(1.dp, glass.borderGradientStart, CircleShape)
                    .clickable(onClick = onOpenNotifications)
                    .testTag("dashboard_notification_bell"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications & Alerts",
                    tint = GoldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CoralError)
                )
            }

            // Profile Avatar (opens glass sheet with profile & business name, Switch Firm, Switch User, Settings, Backup & Restore, Lock app now, Help, About)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(VioletPrimary, EmeraldPrimary)
                        )
                    )
                    .border(1.dp, glass.borderGradientStart, CircleShape)
                    .clickable(onClick = onOpenAvatarSheet)
                    .testTag("dashboard_profile_avatar"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activeUserName.take(2).uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Clean glassmorphic "Coming Soon" screen with module name, icon, details, and both a top-bar Back icon
 * and a primary Back button so unbuilt modules never crash and always navigate back cleanly.
 */
@Composable
fun ComingSoonModuleScreen(
    moduleName: String,
    moduleDetails: String,
    icon: ImageVector,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("coming_soon_screen_$moduleName")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("coming_soon_top_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = moduleName,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 28.dp,
            elevated = true,
            accentTint = VioletPrimary,
            contentPadding = PaddingValues(28.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = moduleName,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    color = GoldPrimary.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.coming_soon_badge),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = GoldPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = moduleName,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = moduleDetails,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                VanijyaButton(
                    text = stringResource(R.string.action_back),
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = onBack,
                    testTag = "coming_soon_back_button"
                )
            }
        }
    }
}

@Composable
private fun MoreHubScreen(
    onNavigate: (String) -> Unit
) {
    val modules = listOf(
        MoreMenuItem("sales", stringResource(R.string.more_sales), "Tax Invoices, Estimates, Delivery Challans & Credit Notes", Icons.Default.PointOfSale),
        MoreMenuItem("purchases", stringResource(R.string.more_purchases), "Purchase Bills, Purchase Orders & Debit Notes", Icons.Default.ShoppingCart),
        MoreMenuItem("cash", stringResource(R.string.more_cash_bank), "Cash in Hand, Bank Accounts, UPI & Cheques", Icons.Default.AccountBalanceWallet),
        MoreMenuItem("expenses", stringResource(R.string.more_expenses), "Direct & Indirect Business Expense Vouchers", Icons.Default.Receipt),
        MoreMenuItem("staff", stringResource(R.string.more_staff_payroll), "Daily Attendance, Advances, Salary & PDF Payslips", Icons.Default.Badge),
        MoreMenuItem("gst", stringResource(R.string.more_gst), "Tax Slabs (0% to 40% + Cess), HSN Summary & GSTR", Icons.Default.Percent),
        MoreMenuItem("reports", stringResource(R.string.more_reports), "Daybook, P&L, Balance Sheet, Party Statements & Audit Log", Icons.Default.Assessment),
        MoreMenuItem("backup", stringResource(R.string.more_backup), "Single-file AES-256 Encrypted Archive (.vbk) & Restore", Icons.Default.Backup),
        MoreMenuItem("settings", stringResource(R.string.more_settings), "Appearance, Language, Business Profile, Invoicing, Security & Users", Icons.Default.Settings)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("more_hub_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SectionHeader(title = stringResource(R.string.nav_more))
        }
        items(modules, key = { it.route }) { item ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("more_item_${item.route}"),
                cornerRadius = 22.dp,
                contentPadding = PaddingValues(16.dp),
                onClick = { onNavigate(item.route) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            IndigoDeep.copy(alpha = 0.25f),
                                            VioletPrimary.copy(alpha = 0.18f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Open ${item.title}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
