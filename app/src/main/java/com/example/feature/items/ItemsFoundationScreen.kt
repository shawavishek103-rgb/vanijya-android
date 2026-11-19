package com.example.feature.items

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warehouse
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.core.database.CompanyEntity
import com.example.core.database.HsnMasterEntity
import com.example.core.database.ItemBatchEntity
import com.example.core.database.ItemBundleEntity
import com.example.core.database.ItemCategoryEntity
import com.example.core.database.ItemEntity
import com.example.core.database.ItemSerialEntity
import com.example.core.database.ItemUnitEntity
import com.example.core.database.ItemVariantEntity
import com.example.core.database.StockAdjustmentEntity
import com.example.core.database.StockLedgerEntity
import com.example.core.database.StockTransferEntity
import com.example.core.database.UnitConversionEntity
import com.example.core.database.WarehouseEntity
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
import com.example.core.designsystem.vanijyaSwitchColors
import com.example.core.designsystem.vanijyaTextFieldColors
import com.example.core.model.BarcodeAndImageHelper
import com.example.core.model.BundledHsnDirectoryLoader
import com.example.core.model.ItemImportPreviewRow
import com.example.core.model.Money
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
import java.util.UUID
import java.util.concurrent.Executors

enum class ItemsLaunchAction {
    NONE,
    ADD_ITEM,
    STOCK_ADJUSTMENT,
    SCAN_BARCODE,
    IMPORT_EXPORT
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemsFoundationScreen(
    hsnMasterList: List<HsnMasterEntity> = emptyList(),
    hideAmounts: Boolean = false,
    decimals: Int = 2,
    activeCompany: CompanyEntity? = null,
    itemsList: List<ItemEntity> = emptyList(),
    categoriesList: List<ItemCategoryEntity> = emptyList(),
    unitsList: List<ItemUnitEntity> = emptyList(),
    conversionsList: List<UnitConversionEntity> = emptyList(),
    batchesList: List<ItemBatchEntity> = emptyList(),
    serialsList: List<ItemSerialEntity> = emptyList(),
    variantsList: List<ItemVariantEntity> = emptyList(),
    warehousesList: List<WarehouseEntity> = emptyList(),
    stockLedgerList: List<StockLedgerEntity> = emptyList(),
    bundlesList: List<ItemBundleEntity> = emptyList(),
    initialSegmentIndex: Int = 0,
    launchAction: ItemsLaunchAction = ItemsLaunchAction.NONE,
    onConsumeLaunchAction: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    onFullScreenEditorOpenChange: (Boolean) -> Unit = {},
    reselectTrigger: Int = 0,
    onSaveItem: (
        ItemEntity,
        ItemBatchEntity?,
        List<ItemVariantEntity>,
        List<ItemSerialEntity>,
        List<ItemBundleEntity>
    ) -> Unit = { _, _, _, _, _ -> },
    onRecordStockAdjustment: (StockAdjustmentEntity) -> Unit = {},
    onRecordStockTransfer: (StockTransferEntity) -> Unit = {},
    onBulkUpdatePricesAndTax: (List<String>, Int, Int?, (Int) -> Unit) -> Unit = { _, _, _, cb -> cb(0) },
    onBulkImportItems: (List<ItemImportPreviewRow>, (Int) -> Unit) -> Unit = { _, cb -> cb(0) },
    onSaveCategory: (String, String) -> Unit = { _, _ -> },
    onSaveWarehouse: (String, String, String, String, String) -> Unit = { _, _, _, _, _ -> }
) {
    val context = LocalContext.current
    val glass = LocalVanijyaGlass.current
    val companyId = activeCompany?.id ?: "default_company"
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH) }
    val listState = rememberLazyListState()

    val fallbackItems = remember {
        val now = System.currentTimeMillis()
        mutableStateListOf(
            ItemEntity(
                id = "item_1", companyId = companyId, itemType = "PRODUCT",
                name = "Royal Basmati Rice (25 Kg Bag)", itemCode = "SKU-RICE-25",
                hsnSacCode = "1006", categoryName = "Kirana & Staples", brand = "Royal Harvest",
                primaryUnit = "BAG", secondaryUnit = "KG", conversionRate = 25,
                salePricePaise = 2_450_00L, purchasePricePaise = 2_100_00L,
                wholesalePricePaise = 2_320_00L, mrpPaise = 2_600_00L, taxRateBps = 500,
                openingStockQty = 40L, currentStockQty = 40L, minStockAlertQty = 10L,
                warehouseName = "Main Godown", barcode = "8901006000012",
                createdAt = now, updatedAt = now
            ),
            ItemEntity(
                id = "item_2", companyId = companyId, itemType = "PRODUCT",
                name = "Cold Pressed Mustard Oil (1L Bottle)", itemCode = "SKU-OIL-1L",
                hsnSacCode = "1514", categoryName = "Edible Oils", brand = "Kisan Pure",
                primaryUnit = "PCS", secondaryUnit = "BOX", conversionRate = 12,
                salePricePaise = 195_00L, purchasePricePaise = 160_00L,
                wholesalePricePaise = 182_00L, mrpPaise = 210_00L, taxRateBps = 500,
                openingStockQty = 4L, currentStockQty = 4L, minStockAlertQty = 10L,
                warehouseName = "Main Godown", barcode = "8901514000029",
                createdAt = now, updatedAt = now
            )
        )
    }
    val effectiveItems = if (itemsList.isNotEmpty()) itemsList else fallbackItems

    // Ensure bundled HSN/SAC directory is always available even if DB hasn't emitted yet
    val effectiveHsnList = remember(hsnMasterList, companyId) {
        if (hsnMasterList.isNotEmpty()) hsnMasterList
        else BundledHsnDirectoryLoader.loadFromAssets(context, companyId)
    }

    val segments = remember { listOf("Items", "Categories", "Low Stock", "Expiring") }
    var selectedSegment by rememberSaveable { mutableIntStateOf(initialSegmentIndex.coerceIn(0, 3)) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedWarehouseFilter by rememberSaveable { mutableStateOf("All Godowns") }
    var selectedCategoryFilter by rememberSaveable { mutableStateOf("All") }

    var editingItem by remember { mutableStateOf<ItemEntity?>(null) }
    var showItemFormSheet by rememberSaveable { mutableStateOf(false) }
    var activeDetailItemId by rememberSaveable { mutableStateOf<String?>(null) }
    var showItemValuationSubScreen by rememberSaveable { mutableStateOf(false) }
    val activeDetailItem = remember(effectiveItems, activeDetailItemId) {
        effectiveItems.firstOrNull { it.id == activeDetailItemId }
    }
    var showStockAdjustmentSheet by rememberSaveable { mutableStateOf(false) }
    var showGodownTransferSheet by rememberSaveable { mutableStateOf(false) }
    var showBarcodeScannerSheet by rememberSaveable { mutableStateOf(false) }
    var showBulkPriceTaxSheet by rememberSaveable { mutableStateOf(false) }
    var showBulkImportExportSheet by rememberSaveable { mutableStateOf(false) }
    var showHsnDirectorySheet by rememberSaveable { mutableStateOf(false) }
    var showAddCategoryOrGodownSheet by rememberSaveable { mutableStateOf(false) }
    var scannedBarcodeValue by rememberSaveable { mutableStateOf("") }
    var feedbackBanner by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(showItemFormSheet) {
        onFullScreenEditorOpenChange(showItemFormSheet)
    }

    DisposableEffect(Unit) {
        onDispose {
            onFullScreenEditorOpenChange(false)
        }
    }

    LaunchedEffect(reselectTrigger) {
        if (reselectTrigger > 0) {
            showItemValuationSubScreen = false
            activeDetailItemId = null
            showItemFormSheet = false
            listState.scrollToItem(0)
        }
    }

    LaunchedEffect(launchAction) {
        when (launchAction) {
            ItemsLaunchAction.ADD_ITEM -> {
                editingItem = null
                showItemFormSheet = true
                onConsumeLaunchAction()
            }
            ItemsLaunchAction.STOCK_ADJUSTMENT -> {
                showStockAdjustmentSheet = true
                onConsumeLaunchAction()
            }
            ItemsLaunchAction.SCAN_BARCODE -> {
                showBarcodeScannerSheet = true
                onConsumeLaunchAction()
            }
            ItemsLaunchAction.IMPORT_EXPORT -> {
                showBulkImportExportSheet = true
                onConsumeLaunchAction()
            }
            ItemsLaunchAction.NONE -> Unit
        }
    }

    val stockMetrics = remember(effectiveItems, batchesList) {
        StockLedgerCalculator.computeRealTimeStockMetrics(effectiveItems, batchesList)
    }

    val warehouseNames = remember(warehousesList, effectiveItems) {
        val fromWh = warehousesList.map { it.name }
        val fromItems = effectiveItems.map { it.warehouseName }.filter { it.isNotBlank() }
        listOf("All Godowns") + (fromWh + fromItems + listOf("Main Godown", "Retail Counter", "Cold Storage Annex")).distinct()
    }

    val filteredItems = remember(effectiveItems, searchQuery, selectedWarehouseFilter, selectedCategoryFilter) {
        effectiveItems.filter { item ->
            val matchesGodown = selectedWarehouseFilter == "All Godowns" ||
                item.warehouseName.equals(selectedWarehouseFilter, ignoreCase = true)
            val matchesCategory = selectedCategoryFilter == "All" ||
                item.categoryName.equals(selectedCategoryFilter, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true) ||
                item.itemCode.contains(searchQuery, ignoreCase = true) ||
                item.hsnSacCode.contains(searchQuery, ignoreCase = true) ||
                item.barcode.contains(searchQuery, ignoreCase = true) ||
                item.brand.contains(searchQuery, ignoreCase = true)
            matchesGodown && matchesCategory && matchesQuery
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("items_stock_hub_screen")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("items_lazy_column"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 180.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Header with Back Button + Action Icons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (onBack != null) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(glass.pillFill)
                                    .border(1.dp, glass.borderGradientStart, CircleShape)
                                    .testTag("items_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Items & Stock Master",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Products, Services, HSN/SAC (${effectiveHsnList.size}), Batches, Godowns & Barcodes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    var showItemsHeaderMenu by remember { mutableStateOf(false) }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showBarcodeScannerSheet = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(glass.pillFill)
                                .border(1.dp, glass.borderGradientStart, CircleShape)
                                .testTag("items_scan_barcode_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan Barcode / QR",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { showStockAdjustmentSheet = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(glass.pillFill)
                                .border(1.dp, glass.borderGradientStart, CircleShape)
                                .testTag("items_stock_adjustment_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Stock Adjustment",
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Box {
                            IconButton(
                                onClick = { showItemsHeaderMenu = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(glass.pillFill)
                                    .border(1.dp, glass.borderGradientStart, CircleShape)
                                    .testTag("items_header_more_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Stock Actions",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showItemsHeaderMenu,
                                onDismissRequest = { showItemsHeaderMenu = false },
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Transfer Between Godowns", color = MaterialTheme.colorScheme.onSurface) },
                                    leadingIcon = { Icon(Icons.Default.CompareArrows, contentDescription = null, tint = PurchaseBlue) },
                                    onClick = {
                                        showItemsHeaderMenu = false
                                        showGodownTransferSheet = true
                                    },
                                    modifier = Modifier.testTag("items_godown_transfer_btn")
                                )
                                DropdownMenuItem(
                                    text = { Text("Bulk Price / Tax Update", color = MaterialTheme.colorScheme.onSurface) },
                                    leadingIcon = { Icon(Icons.Default.PriceChange, contentDescription = null, tint = VioletPrimary) },
                                    onClick = {
                                        showItemsHeaderMenu = false
                                        showBulkPriceTaxSheet = true
                                    },
                                    modifier = Modifier.testTag("items_bulk_price_btn")
                                )
                                DropdownMenuItem(
                                    text = { Text("Import / Export Excel & PDF", color = MaterialTheme.colorScheme.onSurface) },
                                    leadingIcon = { Icon(Icons.Default.FileUpload, contentDescription = null, tint = EmeraldPrimary) },
                                    onClick = {
                                        showItemsHeaderMenu = false
                                        showBulkImportExportSheet = true
                                    },
                                    modifier = Modifier.testTag("items_import_export_btn")
                                )
                            }
                        }
                    }
                }
            }

            // Real-Time Stock Value, Low-Stock Count & Expiring Count Header Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassCard(
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("items_total_stock_value_card"),
                        cornerRadius = 22.dp,
                        accentTint = EmeraldPrimary,
                        contentPadding = PaddingValues(14.dp),
                        onClick = { selectedSegment = 0 }
                    ) {
                        Text(
                            text = "Stock Value (Cost)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        AmountText(
                            amount = stockMetrics.totalStockValue,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldPrimary,
                            masked = hideAmounts,
                            decimals = decimals,
                            animateCountUp = true
                        )
                        Text(
                            text = "${stockMetrics.totalProductCount} Products • ${stockMetrics.totalServiceCount} Services",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    GlassCard(
                        modifier = Modifier
                            .weight(0.85f)
                            .testTag("items_low_stock_header_card"),
                        cornerRadius = 22.dp,
                        accentTint = GoldPrimary,
                        contentPadding = PaddingValues(14.dp),
                        onClick = { selectedSegment = 2 }
                    ) {
                        Text(
                            text = "Low Stock",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldPrimary
                        )
                        Text(
                            text = "${stockMetrics.lowStockItems.size} SKUs",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = GoldPrimary
                        )
                        Text(
                            text = "Below min alert",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    GlassCard(
                        modifier = Modifier
                            .weight(0.85f)
                            .testTag("items_expiring_header_card"),
                        cornerRadius = 22.dp,
                        accentTint = AmberSale,
                        contentPadding = PaddingValues(14.dp),
                        onClick = { selectedSegment = 3 }
                    ) {
                        Text(
                            text = "Expiring",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberSale
                        )
                        Text(
                            text = "${stockMetrics.expiringBatches.size} Batches",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = AmberSale
                        )
                        Text(
                            text = "Within 30 days",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Segmented Tabs: Items, Categories, Low Stock, Expiring
            item {
                SegmentedTabs(
                    tabs = segments,
                    selectedIndex = selectedSegment,
                    onTabSelected = { selectedSegment = it },
                    scrollable = false,
                    testTagPrefix = "items_segment"
                )
            }

            // Search Bar + Godown Filter + HSN Directory Button
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search item name, SKU, HSN/SAC, or barcode") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search Items")
                    },
                    trailingIcon = {
                        IconButton(onClick = { showBarcodeScannerSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan Barcode",
                                tint = EmeraldPrimary
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("items_search_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    warehouseNames.forEach { wh ->
                        FilterChip(
                            selected = selectedWarehouseFilter == wh,
                            onClick = { selectedWarehouseFilter = wh },
                            label = { Text(wh) },
                            colors = vanijyaFilterChipColors()
                        )
                    }
                    FilterChip(
                        selected = false,
                        onClick = { showHsnDirectorySheet = true },
                        label = { Text("HSN/SAC Directory (${effectiveHsnList.size})") },
                        colors = vanijyaFilterChipColors()
                    )
                    FilterChip(
                        selected = false,
                        onClick = { showAddCategoryOrGodownSheet = true },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Category / Godown",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("Category / Godown") },
                        colors = vanijyaFilterChipColors()
                    )
                }
            }

            if (!feedbackBanner.isNullOrBlank()) {
                item {
                    Surface(
                        color = EmeraldPrimary.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { feedbackBanner = null }
                            .testTag("items_feedback_banner")
                    ) {
                        Text(
                            text = feedbackBanner!!,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // ================================================================
            // SEGMENT 0: ITEMS MASTER LIST (Products, Services, Bundles)
            // ================================================================
            if (selectedSegment == 0) {
                if (filteredItems.isEmpty()) {
                    item {
                        VanijyaEmptyState(
                            icon = Icons.Default.Inventory2,
                            title = "No matching items in stock master",
                            description = "Add products or services with HSN/SAC codes, unit conversions (1 Box = 12 Pcs), batches, and godowns.",
                            actionLabel = "Add Item",
                            onActionClick = {
                                editingItem = null
                                showItemFormSheet = true
                            }
                        )
                    }
                } else {
                    itemsIndexed(filteredItems, key = { _, it -> it.id }) { idx, item ->
                        val isLow = item.itemType == "PRODUCT" && item.currentStockQty <= item.minStockAlertQty
                        val itemStockValue = Money(item.currentStockQty.coerceAtLeast(0L) * item.purchasePricePaise)
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("item_card_$idx"),
                            cornerRadius = 22.dp,
                            contentPadding = PaddingValues(16.dp),
                            onClick = { activeDetailItemId = item.id }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = "${item.itemCode.ifBlank { "SKU" }} • ${if (item.itemType == "SERVICE") "SAC" else "HSN"} ${item.hsnSacCode.ifBlank { "-" }} • GST ${item.taxRateBps / 100}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${item.categoryName} • ${item.warehouseName}" +
                                            if (item.secondaryUnit.isNotBlank() && item.conversionRate > 1) {
                                                " • 1 ${item.secondaryUnit} = ${item.conversionRate} ${item.primaryUnit}"
                                            } else "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    AmountText(
                                        amount = Money(item.salePricePaise),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = EmeraldPrimary,
                                        masked = hideAmounts,
                                        decimals = decimals
                                    )
                                    if (item.itemType == "SERVICE") {
                                        Text(
                                            text = "Service (SAC)",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = VioletPrimary
                                        )
                                    } else {
                                        Text(
                                            text = "Stock: ${item.currentStockQty} ${item.primaryUnit}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isLow) CoralError else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = glass.subtleDivider)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (item.itemType == "PRODUCT") {
                                        Text(
                                            text = "Purchase: ${Money(item.purchasePricePaise).formatIndian(masked = hideAmounts)} • Value: ${itemStockValue.formatIndian(masked = hideAmounts)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        Text(
                                            text = item.description.ifBlank { "On-site service contract" },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (isLow) {
                                        Surface(
                                            color = CoralError.copy(alpha = 0.16f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "Low Stock (≤${item.minStockAlertQty})",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = CoralError,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    Surface(
                                        color = glass.pillFill,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                editingItem = item
                                                showItemFormSheet = true
                                            }
                                    ) {
                                        Text(
                                            text = "Edit",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bundles / Kits section if available
                if (bundlesList.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Item Bundles & Combo Kits (${bundlesList.size})")
                    }
                    items(bundlesList, key = { it.id }) { bundle ->
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 18.dp,
                            accentTint = VioletPrimary,
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = bundle.bundleName,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Includes ${bundle.componentQty}x ${bundle.componentItemName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                AmountText(
                                    amount = Money(bundle.bundleSalePricePaise),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = VioletPrimary,
                                    masked = hideAmounts,
                                    decimals = decimals
                                )
                            }
                        }
                    }
                }
            }

            // ================================================================
            // SEGMENT 1: CATEGORIES & GODOWNS
            // ================================================================
            if (selectedSegment == 1) {
                item {
                    SectionHeader(
                        title = "Product & Service Categories",
                        actionText = "New Category",
                        actionIcon = Icons.Default.Add,
                        onActionClick = { showAddCategoryOrGodownSheet = true }
                    )
                }
                val categoryGroups = effectiveItems.groupBy { it.categoryName.ifBlank { "General" } }
                items(categoryGroups.entries.toList(), key = { "cat_${it.key}" }) { (catName, catItems) ->
                    val catValue = Money(
                        catItems.filter { it.itemType == "PRODUCT" }
                            .sumOf { it.currentStockQty.coerceAtLeast(0L) * it.purchasePricePaise }
                    )
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp,
                        accentTint = VioletPrimary,
                        contentPadding = PaddingValues(16.dp),
                        onClick = {
                            selectedCategoryFilter = catName
                            selectedSegment = 0
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = catName,
                                    tint = VioletPrimary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = catName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${catItems.size} items in category",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            AmountText(
                                amount = catValue,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldPrimary,
                                masked = hideAmounts,
                                decimals = decimals
                            )
                        }
                    }
                }

                item {
                    SectionHeader(
                        title = "Warehouses & Godowns (${warehousesList.size})",
                        actionText = "Transfer Stock",
                        onActionClick = { showGodownTransferSheet = true }
                    )
                }
                items(warehousesList, key = { it.id }) { wh ->
                    val whItems = itemsList.filter { it.warehouseId == wh.id || it.warehouseName == wh.name }
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp,
                        accentTint = PurchaseBlue,
                        contentPadding = PaddingValues(16.dp),
                        onClick = {
                            selectedWarehouseFilter = wh.name
                            selectedSegment = 0
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warehouse,
                                    contentDescription = wh.name,
                                    tint = PurchaseBlue
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "${wh.name} (${wh.code})",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${wh.address} • In-charge: ${wh.inchargeName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "${whItems.size} SKUs",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = PurchaseBlue
                            )
                        }
                    }
                }
            }

            // ================================================================
            // SEGMENT 2: LOW STOCK ALERTS
            // ================================================================
            if (selectedSegment == 2) {
                if (stockMetrics.lowStockItems.isEmpty()) {
                    item {
                        VanijyaEmptyState(
                            icon = Icons.Default.Inventory2,
                            title = "All SKUs are above reorder level",
                            description = "Items whose current stock drops at or below their minimum-stock alert threshold appear here automatically."
                        )
                    }
                } else {
                    items(stockMetrics.lowStockItems, key = { "low_${it.id}" }) { lowItem ->
                        val shortfall = (lowItem.minStockAlertQty - lowItem.currentStockQty).coerceAtLeast(1L)
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("low_stock_item_${lowItem.id}"),
                            cornerRadius = 22.dp,
                            accentTint = GoldPrimary,
                            contentPadding = PaddingValues(16.dp),
                            onClick = { activeDetailItemId = lowItem.id }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.WarningAmber,
                                            contentDescription = "Low Stock",
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = lowItem.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "${lowItem.warehouseName} • Rack: ${lowItem.rackLocation.ifBlank { "-" }} • Min Alert: ${lowItem.minStockAlertQty} ${lowItem.primaryUnit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${lowItem.currentStockQty} ${lowItem.primaryUnit}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = CoralError
                                    )
                                    Text(
                                        text = "Short by $shortfall ${lowItem.primaryUnit}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ================================================================
            // SEGMENT 3: EXPIRING BATCHES (WITHIN 30 DAYS)
            // ================================================================
            if (selectedSegment == 3) {
                if (stockMetrics.expiringBatches.isEmpty()) {
                    item {
                        VanijyaEmptyState(
                            icon = Icons.Default.EventBusy,
                            title = "No batches expiring within 30 days",
                            description = "Batch-tracked FMCG, Pharma, and Dairy items nearing their expiry date appear here."
                        )
                    }
                } else {
                    items(stockMetrics.expiringBatches, key = { "exp_${it.id}" }) { batch ->
                        val daysLeft = ((batch.expiryDate - System.currentTimeMillis()) / 86_400_000L).coerceAtLeast(0L)
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("expiring_batch_${batch.id}"),
                            cornerRadius = 22.dp,
                            accentTint = AmberSale,
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = batch.itemName.ifBlank { "Batch ${batch.batchNumber}" },
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Batch: ${batch.batchNumber} • ${batch.warehouseName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Mfg: ${dateFormatter.format(Date(batch.mfgDate))} • Exp: ${dateFormatter.format(Date(batch.expiryDate))}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AmberSale
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${batch.availableQty} units",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = AmberSale
                                    )
                                    Text(
                                        text = "Expires in ${daysLeft}d",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = CoralError
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating "Add Item" Button
        VanijyaExtendedFab(
            text = "Add Item",
            onClick = {
                editingItem = null
                showItemFormSheet = true
            },
            icon = Icons.Default.Add,
            containerColor = EmeraldPrimary,
            contentColor = MaterialTheme.colorScheme.surface,
            testTag = "items_fab_add_item",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 104.dp)
        )
    }

    // ========================================================================
    // SHEET 1: ADD / EDIT ITEM MASTER FORM
    // ========================================================================
    if (showItemFormSheet) {
        val existing = editingItem
        val itemId = remember(existing) { existing?.id ?: UUID.randomUUID().toString() }

        var itemTypeDraft by rememberSaveable(existing) { mutableStateOf(existing?.itemType ?: "PRODUCT") }
        var nameDraft by rememberSaveable(existing) { mutableStateOf(existing?.name ?: "") }
        var codeDraft by rememberSaveable(existing) { mutableStateOf(existing?.itemCode ?: "SKU-${(100..999).random()}") }
        var hsnDraft by rememberSaveable(existing) { mutableStateOf(existing?.hsnSacCode ?: "1006") }
        var categoryDraft by rememberSaveable(existing) { mutableStateOf(existing?.categoryName ?: "Kirana & Staples") }
        var brandDraft by rememberSaveable(existing) { mutableStateOf(existing?.brand ?: "") }
        var primaryUnitDraft by rememberSaveable(existing) { mutableStateOf(existing?.primaryUnit ?: "PCS") }
        var secondaryUnitDraft by rememberSaveable(existing) { mutableStateOf(existing?.secondaryUnit ?: "BOX") }
        var conversionRateDraft by rememberSaveable(existing) { mutableStateOf((existing?.conversionRate ?: 12).toString()) }
        var salePriceDraft by rememberSaveable(existing) {
            mutableStateOf(if (existing != null) (existing.salePricePaise / 100L).toString() else "")
        }
        var purchasePriceDraft by rememberSaveable(existing) {
            mutableStateOf(if (existing != null) (existing.purchasePricePaise / 100L).toString() else "")
        }
        var wholesalePriceDraft by rememberSaveable(existing) {
            mutableStateOf(if (existing != null) (existing.wholesalePricePaise / 100L).toString() else "")
        }
        var mrpDraft by rememberSaveable(existing) {
            mutableStateOf(if (existing != null) (existing.mrpPaise / 100L).toString() else "")
        }
        var taxRateBpsDraft by rememberSaveable(existing) { mutableIntStateOf(existing?.taxRateBps ?: 1800) }
        var taxInclusiveDraft by rememberSaveable(existing) { mutableStateOf(existing?.taxInclusive ?: false) }
        var cessBpsDraft by rememberSaveable(existing) { mutableStateOf((existing?.cessBps?.div(100) ?: 0).toString()) }
        var openingStockDraft by rememberSaveable(existing) { mutableStateOf((existing?.openingStockQty ?: 25L).toString()) }
        var minStockAlertDraft by rememberSaveable(existing) { mutableStateOf((existing?.minStockAlertQty ?: 5L).toString()) }
        var rackDraft by rememberSaveable(existing) { mutableStateOf(existing?.rackLocation ?: "Aisle A • Rack 01") }
        var barcodeDraft by rememberSaveable(existing, scannedBarcodeValue) {
            mutableStateOf(scannedBarcodeValue.ifBlank { existing?.barcode ?: "8901234567890" })
        }
        var warehouseNameDraft by rememberSaveable(existing) { mutableStateOf(existing?.warehouseName ?: "Main Godown") }
        var imageUriDraft by rememberSaveable(existing) { mutableStateOf(existing?.imageUri ?: "") }
        var descriptionDraft by rememberSaveable(existing) { mutableStateOf(existing?.description ?: "") }
        var trackBatchDraft by rememberSaveable(existing) { mutableStateOf(existing?.trackBatch ?: false) }
        var batchNoDraft by rememberSaveable(existing) { mutableStateOf("B-2026-01") }
        var batchExpiryDaysDraft by rememberSaveable(existing) { mutableStateOf("180") }
        var trackSerialDraft by rememberSaveable(existing) { mutableStateOf(existing?.trackSerial ?: false) }
        var serialInputDraft by rememberSaveable(existing) { mutableStateOf("") }
        var variantInputDraft by rememberSaveable(existing) { mutableStateOf("") }
        var formError by remember { mutableStateOf<String?>(null) }

        val itemImagePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                imageUriDraft = BarcodeAndImageHelper.compressAndSaveLocalItemImage(context, uri)
            }
        }

        val barcodeBitmap = remember(barcodeDraft) {
            BarcodeAndImageHelper.generateBarcodeBitmap(barcodeDraft)
        }

        GlassFullScreenEditor(
            onBack = { showItemFormSheet = false },
            title = if (existing == null) "Add Product / Service Item" else "Edit ${existing.name}",
            subtitle = "Stored locally with HSN/SAC, unit conversions, batch/serial tracking & stock ledger",
            screenTestTag = "item_full_screen_editor",
            backButtonTestTag = "item_form_back_btn"
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = itemTypeDraft == "PRODUCT",
                    onClick = { itemTypeDraft = "PRODUCT" },
                    label = { Text("Product (Stock Tracked)") },
                    colors = vanijyaFilterChipColors()
                )
                FilterChip(
                    selected = itemTypeDraft == "SERVICE",
                    onClick = { itemTypeDraft = "SERVICE" },
                    label = { Text("Service (SAC)") },
                    colors = vanijyaFilterChipColors()
                )
            }

            OutlinedTextField(
                value = nameDraft,
                onValueChange = {
                    nameDraft = it
                    formError = null
                },
                label = { Text("Item / Service Name *") },
                singleLine = true,
                isError = formError != null,
                colors = vanijyaTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("item_form_name_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = codeDraft,
                    onValueChange = { codeDraft = it.uppercase() },
                    label = { Text("Item / SKU Code") },
                    singleLine = true,
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = hsnDraft,
                    onValueChange = { raw ->
                        hsnDraft = raw
                        effectiveHsnList.firstOrNull { it.hsnCode == raw }?.let { matched ->
                            taxRateBpsDraft = matched.defaultTaxRateBps
                        }
                    },
                    label = { Text(if (itemTypeDraft == "SERVICE") "SAC Code" else "HSN Code") },
                    trailingIcon = {
                        IconButton(onClick = { showHsnDirectorySheet = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search HSN Directory")
                        }
                    },
                    singleLine = true,
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("item_form_hsn_input")
                )
            }

            // Units & Unit Conversion (e.g., 1 BOX = 12 PCS)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = primaryUnitDraft,
                    onValueChange = { primaryUnitDraft = it.uppercase().take(6) },
                    label = { Text("Primary Unit") },
                    singleLine = true,
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = secondaryUnitDraft,
                    onValueChange = { secondaryUnitDraft = it.uppercase().take(6) },
                    label = { Text("Secondary Unit") },
                    singleLine = true,
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = conversionRateDraft,
                    onValueChange = { conversionRateDraft = it.filter(Char::isDigit).take(4) },
                    label = { Text("1 Sec = N Pri") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
            }

            // Pricing: Sale, Purchase, Wholesale, MRP (integer paise via Money)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = salePriceDraft,
                    onValueChange = { salePriceDraft = it.filter { ch -> ch.isDigit() || ch == '.' }.take(9) },
                    label = { Text("Sale Price (₹) *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("item_form_sale_price_input")
                )
                OutlinedTextField(
                    value = purchasePriceDraft,
                    onValueChange = { purchasePriceDraft = it.filter { ch -> ch.isDigit() || ch == '.' }.take(9) },
                    label = { Text("Purchase Price (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("item_form_purchase_price_input")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = wholesalePriceDraft,
                    onValueChange = { wholesalePriceDraft = it.filter { ch -> ch.isDigit() || ch == '.' }.take(9) },
                    label = { Text("Wholesale Price (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = mrpDraft,
                    onValueChange = { mrpDraft = it.filter { ch -> ch.isDigit() || ch == '.' }.take(9) },
                    label = { Text("MRP (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
            }

            // GST Slab + Tax Inclusive Toggle + Cess
            Text(
                text = "GST Slab & Tax Mode",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    0 to "0%",
                    25 to "0.25%",
                    300 to "3%",
                    500 to "5%",
                    1200 to "12%",
                    1800 to "18%",
                    2800 to "28%",
                    4000 to "40%"
                ).forEach { (bps, label) ->
                    FilterChip(
                        selected = taxRateBpsDraft == bps,
                        onClick = { taxRateBpsDraft = bps },
                        label = { Text("GST $label") },
                        colors = vanijyaFilterChipColors()
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (taxInclusiveDraft) "Tax Inclusive Price" else "Tax Exclusive Price",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = taxInclusiveDraft,
                        onCheckedChange = { taxInclusiveDraft = it },
                        colors = vanijyaSwitchColors()
                    )
                }
                OutlinedTextField(
                    value = cessBpsDraft,
                    onValueChange = { cessBpsDraft = it.filter(Char::isDigit).take(2) },
                    label = { Text("Cess %") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.width(110.dp)
                )
            }

            if (itemTypeDraft == "PRODUCT") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = openingStockDraft,
                        onValueChange = { openingStockDraft = it.filter(Char::isDigit).take(7) },
                        label = { Text("Opening Stock Qty") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = vanijyaTextFieldColors(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("item_form_opening_stock_input")
                    )
                    OutlinedTextField(
                        value = minStockAlertDraft,
                        onValueChange = { minStockAlertDraft = it.filter(Char::isDigit).take(6) },
                        label = { Text("Min Stock Alert") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = vanijyaTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = rackDraft,
                        onValueChange = { rackDraft = it },
                        label = { Text("Rack / Shelf Location") },
                        singleLine = true,
                        colors = vanijyaTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = brandDraft,
                        onValueChange = { brandDraft = it },
                        label = { Text("Brand") },
                        singleLine = true,
                        colors = vanijyaTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Batch & Serial/IMEI Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = trackBatchDraft,
                        onClick = { trackBatchDraft = !trackBatchDraft },
                        label = { Text("Batch & Expiry Tracking") },
                        colors = vanijyaFilterChipColors()
                    )
                    FilterChip(
                        selected = trackSerialDraft,
                        onClick = { trackSerialDraft = !trackSerialDraft },
                        label = { Text("Serial / IMEI Tracking") },
                        colors = vanijyaFilterChipColors()
                    )
                }

                if (trackBatchDraft) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = batchNoDraft,
                            onValueChange = { batchNoDraft = it },
                            label = { Text("Initial Batch Number") },
                            singleLine = true,
                            colors = vanijyaTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = batchExpiryDaysDraft,
                            onValueChange = { batchExpiryDaysDraft = it.filter(Char::isDigit).take(4) },
                            label = { Text("Expires in (Days)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = vanijyaTextFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (trackSerialDraft) {
                    OutlinedTextField(
                        value = serialInputDraft,
                        onValueChange = { serialInputDraft = it },
                        label = { Text("Serial / IMEI Numbers (comma-separated)") },
                        placeholder = { Text("IMEI-001, IMEI-002") },
                        singleLine = true,
                        colors = vanijyaTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = variantInputDraft,
                    onValueChange = { variantInputDraft = it },
                    label = { Text("Optional Variant (e.g. Size: XL • Color: Navy)") },
                    singleLine = true,
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Barcode / QR + Generated Barcode Preview + Compressed Local Photo Picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = barcodeDraft,
                    onValueChange = { barcodeDraft = it },
                    label = { Text("Barcode / EAN / QR") },
                    trailingIcon = {
                        IconButton(onClick = { showBarcodeScannerSheet = true }) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan CameraX")
                        }
                    },
                    singleLine = true,
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                VanijyaButton(
                    text = if (imageUriDraft.isBlank()) "Photo" else "Photo ✓",
                    icon = Icons.Default.Image,
                    isOutlined = true,
                    onClick = {
                        itemImagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            }

            // Offline Generated Barcode Preview Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = barcodeBitmap.asImageBitmap(),
                    contentDescription = "Generated Code-128 Barcode for $barcodeDraft",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                )
            }

            if (formError != null) {
                Text(
                    text = formError!!,
                    style = MaterialTheme.typography.labelMedium,
                    color = CoralError
                )
            }

            VanijyaButton(
                text = "Save Item to Stock Master",
                onClick = {
                    if (nameDraft.isBlank()) {
                        formError = "Item / Service Name is required"
                    } else {
                        val now = System.currentTimeMillis()
                        val salePaise = runCatching { Money.parseInput(salePriceDraft).paise }.getOrDefault(0L)
                        val purchasePaise = runCatching { Money.parseInput(purchasePriceDraft).paise }.getOrDefault(0L)
                        val wholesalePaise = runCatching { Money.parseInput(wholesalePriceDraft).paise }.getOrDefault(salePaise)
                        val mrpPaise = runCatching { Money.parseInput(mrpDraft).paise }.getOrDefault(salePaise)
                        val openingQty = if (itemTypeDraft == "SERVICE") 0L else (openingStockDraft.toLongOrNull() ?: 0L)
                        val minAlertQty = if (itemTypeDraft == "SERVICE") 0L else (minStockAlertDraft.toLongOrNull() ?: 5L)

                        val newItem = ItemEntity(
                            id = itemId,
                            companyId = companyId,
                            itemType = itemTypeDraft,
                            name = nameDraft.trim(),
                            itemCode = codeDraft.trim(),
                            hsnSacCode = hsnDraft.trim(),
                            categoryId = "${companyId}_cat_${categoryDraft.lowercase().take(4)}",
                            categoryName = categoryDraft,
                            brand = brandDraft.trim(),
                            primaryUnit = primaryUnitDraft.ifBlank { "PCS" },
                            secondaryUnit = secondaryUnitDraft,
                            conversionRate = conversionRateDraft.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                            salePricePaise = salePaise,
                            purchasePricePaise = purchasePaise,
                            wholesalePricePaise = wholesalePaise,
                            mrpPaise = mrpPaise,
                            taxRateBps = taxRateBpsDraft,
                            taxInclusive = taxInclusiveDraft,
                            cessBps = (cessBpsDraft.toIntOrNull() ?: 0) * 100,
                            openingStockQty = openingQty,
                            openingStockDate = now,
                            currentStockQty = existing?.currentStockQty ?: openingQty,
                            minStockAlertQty = minAlertQty,
                            rackLocation = rackDraft.trim(),
                            imageUri = imageUriDraft,
                            description = descriptionDraft.trim(),
                            barcode = barcodeDraft.trim(),
                            trackBatch = trackBatchDraft,
                            trackSerial = trackSerialDraft,
                            warehouseId = "${companyId}_wh_main",
                            warehouseName = warehouseNameDraft,
                            createdAt = existing?.createdAt ?: now,
                            updatedAt = now
                        )

                        val batch = if (trackBatchDraft && itemTypeDraft == "PRODUCT") {
                            val expDays = batchExpiryDaysDraft.toLongOrNull() ?: 180L
                            ItemBatchEntity(
                                id = "${itemId}_batch_init",
                                companyId = companyId,
                                itemId = itemId,
                                itemName = newItem.name,
                                warehouseId = newItem.warehouseId,
                                warehouseName = newItem.warehouseName,
                                batchNumber = batchNoDraft.ifBlank { "B-01" },
                                mfgDate = now,
                                expiryDate = now + expDays * 86_400_000L,
                                openingQty = openingQty,
                                availableQty = openingQty,
                                purchasePricePaise = purchasePaise,
                                salePricePaise = salePaise,
                                mrpPaise = mrpPaise,
                                createdAt = now,
                                updatedAt = now
                            )
                        } else null

                        val variants = if (variantInputDraft.isNotBlank()) {
                            listOf(
                                ItemVariantEntity(
                                    id = UUID.randomUUID().toString(),
                                    companyId = companyId,
                                    itemId = itemId,
                                    variantName = variantInputDraft.trim(),
                                    skuCode = "${newItem.itemCode}-V1",
                                    barcode = newItem.barcode,
                                    salePricePaise = salePaise,
                                    purchasePricePaise = purchasePaise,
                                    stockQty = openingQty,
                                    createdAt = now,
                                    updatedAt = now
                                )
                            )
                        } else emptyList()

                        val serials = if (trackSerialDraft && serialInputDraft.isNotBlank()) {
                            serialInputDraft.split(",").mapNotNull { s ->
                                val clean = s.trim()
                                if (clean.isEmpty()) null
                                else ItemSerialEntity(
                                    id = UUID.randomUUID().toString(),
                                    companyId = companyId,
                                    itemId = itemId,
                                    warehouseId = newItem.warehouseId,
                                    serialOrImei = clean,
                                    status = "IN_STOCK",
                                    warrantyMonths = 12,
                                    createdAt = now,
                                    updatedAt = now
                                )
                            }
                        } else emptyList()

                        onSaveItem(newItem, batch, variants, serials, emptyList())
                        val idx = fallbackItems.indexOfFirst { it.id == newItem.id }
                        if (idx >= 0) fallbackItems[idx] = newItem else fallbackItems.add(newItem)
                        feedbackBanner = "✓ Saved '${newItem.name}' to Stock Master"
                        showItemFormSheet = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("item_form_save_btn")
            )
        }
    }

    // ========================================================================
    // SUB-SCREEN 2: ITEM DETAIL, STOCK LEDGER MOVEMENTS, BATCHES, SERIALS & BARCODE
    // ========================================================================
    if (activeDetailItem != null && !showItemValuationSubScreen) {
        val item = effectiveItems.firstOrNull { it.id == activeDetailItem.id } ?: activeDetailItem
        val itemMovements = remember(stockLedgerList, item.id) {
            stockLedgerList.filter { it.itemId == item.id }
        }
        val itemBatchesForThis = remember(batchesList, item.id) {
            batchesList.filter { it.itemId == item.id }
        }
        val itemVariantsForThis = remember(variantsList, item.id) {
            variantsList.filter { it.itemId == item.id }
        }
        val itemSerialsForThis = remember(serialsList, item.id) {
            serialsList.filter { it.itemId == item.id }
        }
        val barcodeBmp = remember(item.barcode) {
            BarcodeAndImageHelper.generateBarcodeBitmap(item.barcode.ifBlank { item.itemCode })
        }

        GlassSubScreen(
            onBack = { activeDetailItemId = null },
            title = item.name,
            subtitle = "${item.itemCode} • HSN/SAC: ${item.hsnSacCode} • GST ${item.taxRateBps / 100}% • ${item.warehouseName}",
            screenTestTag = "item_detail_screen",
            backButtonTestTag = "item_detail_back_btn"
        ) {
            VanijyaButton(
                text = "Stock Valuation & Batch Breakdown",
                icon = Icons.Default.History,
                isOutlined = true,
                onClick = { showItemValuationSubScreen = true },
                modifier = Modifier.fillMaxWidth(),
                testTag = "item_detail_open_valuation_btn"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassCard(
                    modifier = Modifier.weight(1f),
                    cornerRadius = 18.dp,
                    accentTint = EmeraldPrimary,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Text("Sale Price", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    AmountText(
                        amount = Money(item.salePricePaise),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldPrimary,
                        masked = hideAmounts,
                        decimals = decimals
                    )
                    Text(
                        text = "Wholesale: ${Money(item.wholesalePricePaise).formatIndian(masked = hideAmounts)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                GlassCard(
                    modifier = Modifier.weight(1f),
                    cornerRadius = 18.dp,
                    accentTint = PurchaseBlue,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Text("Current Stock", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${item.currentStockQty} ${item.primaryUnit}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Cost: ${Money(item.purchasePricePaise).formatIndian(masked = hideAmounts)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Barcode graphic
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = barcodeBmp.asImageBitmap(),
                    contentDescription = "Barcode ${item.barcode}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                )
            }

            if (itemBatchesForThis.isNotEmpty()) {
                SectionHeader(title = "Batches & Expiry (${itemBatchesForThis.size})")
                itemBatchesForThis.forEach { b ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp,
                        contentPadding = PaddingValues(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Batch ${b.batchNumber} (${b.warehouseName})",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${b.availableQty} avail • Exp: ${dateFormatter.format(Date(b.expiryDate))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberSale
                            )
                        }
                    }
                }
            }

            if (itemVariantsForThis.isNotEmpty()) {
                SectionHeader(title = "Variants (${itemVariantsForThis.size})")
                itemVariantsForThis.forEach { v ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp,
                        contentPadding = PaddingValues(10.dp)
                    ) {
                        Text(
                            text = "${v.variantName} (${v.skuCode}) — Stock: ${v.stockQty}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (itemSerialsForThis.isNotEmpty()) {
                SectionHeader(title = "Serial / IMEI Numbers (${itemSerialsForThis.size})")
                itemSerialsForThis.forEach { sn ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp,
                        contentPadding = PaddingValues(10.dp)
                    ) {
                        Text(
                            text = "${sn.serialOrImei} • ${sn.status} (${sn.warrantyMonths}m warranty)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            SectionHeader(title = "Stock Ledger & Price History (${itemMovements.size})")
            itemMovements.forEach { mov ->
                val isPositive = mov.qtyDelta >= 0L
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
                                text = "${mov.movementType} • ${mov.voucherNumber}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${dateFormatter.format(Date(mov.movementDate))} • ${mov.warehouseName} • ${mov.remarks}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (mov.qtyDelta > 0L) "+${mov.qtyDelta}" else "${mov.qtyDelta}",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (isPositive) EmeraldPrimary else ToPayRed
                            )
                            Text(
                                text = "Closing: ${mov.closingQty}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VanijyaButton(
                    text = "Adjust Stock",
                    icon = Icons.Default.SwapVert,
                    onClick = {
                        activeDetailItemId = null
                        showStockAdjustmentSheet = true
                    },
                    modifier = Modifier.weight(1f)
                )
                VanijyaButton(
                    text = "Edit Item",
                    icon = Icons.Default.Edit,
                    isOutlined = true,
                    onClick = {
                        editingItem = item
                        activeDetailItemId = null
                        showItemFormSheet = true
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (activeDetailItem != null && showItemValuationSubScreen) {
        val item = effectiveItems.firstOrNull { it.id == activeDetailItem.id } ?: activeDetailItem
        val costVal = Money(item.currentStockQty.coerceAtLeast(0L) * item.purchasePricePaise)
        val retailVal = Money(item.currentStockQty.coerceAtLeast(0L) * item.salePricePaise)
        GlassSubScreen(
            onBack = { showItemValuationSubScreen = false },
            title = "Stock Valuation & Batch Breakdown",
            subtitle = "${item.name} • ${item.warehouseName}",
            screenTestTag = "item_valuation_sub_screen",
            backButtonTestTag = "item_valuation_back_btn"
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                accentTint = EmeraldPrimary,
                contentPadding = PaddingValues(16.dp)
            ) {
                Text(
                    text = "Closing Valuation (${item.currentStockQty} ${item.primaryUnit})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("At Purchase Cost", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    AmountText(
                        amount = costVal,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldPrimary,
                        masked = hideAmounts,
                        decimals = decimals
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("At Sale Price", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    AmountText(
                        amount = retailVal,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = PurchaseBlue,
                        masked = hideAmounts,
                        decimals = decimals
                    )
                }
            }
        }
    }

    // ========================================================================
    // SHEET 3: STOCK ADJUSTMENT WITH REASON (WRITES TO stock_adjustment + stock_ledger)
    // ========================================================================
    if (showStockAdjustmentSheet) {
        val productItems = itemsList.filter { it.itemType == "PRODUCT" }
        var selectedItem by remember(productItems) { mutableStateOf(productItems.firstOrNull()) }
        var adjustmentType by remember { mutableStateOf("ADD") } // ADD, REDUCE
        var qtyInput by remember { mutableStateOf("10") }
        var reason by remember { mutableStateOf("AUDIT_CORRECTION") }
        var notes by remember { mutableStateOf("") }

        GlassSheet(
            onDismissRequest = { showStockAdjustmentSheet = false },
            title = "Stock Adjustment",
            subtitle = "Atomically updates Item Stock, Stock Adjustment table, and Stock Ledger in one transaction"
        ) {
            Text(
                text = "Select Item to Adjust",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                productItems.forEach { prod ->
                    FilterChip(
                        selected = selectedItem?.id == prod.id,
                        onClick = { selectedItem = prod },
                        label = { Text("${prod.name.take(20)} (${prod.currentStockQty})") },
                        colors = vanijyaFilterChipColors()
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = adjustmentType == "ADD",
                    onClick = { adjustmentType = "ADD" },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Stock",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("Add Stock (Inward)") },
                    colors = vanijyaFilterChipColors()
                )
                FilterChip(
                    selected = adjustmentType == "REDUCE",
                    onClick = { adjustmentType = "REDUCE" },
                    label = { Text("Reduce Stock (Outward)") },
                    colors = vanijyaFilterChipColors()
                )
            }

            OutlinedTextField(
                value = qtyInput,
                onValueChange = { qtyInput = it.filter(Char::isDigit).take(6) },
                label = { Text("Adjustment Quantity (${selectedItem?.primaryUnit ?: "PCS"})") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = vanijyaTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stock_adj_qty_input")
            )

            Text(
                text = "Adjustment Reason",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("AUDIT_CORRECTION", "DAMAGED", "EXPIRED", "SHRINKAGE", "PROMOTIONAL_SAMPLE").forEach { r ->
                    FilterChip(
                        selected = reason == r,
                        onClick = { reason = r },
                        label = { Text(r.replace("_", " ")) },
                        colors = vanijyaFilterChipColors()
                    )
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Remarks / Voucher Note") },
                singleLine = true,
                colors = vanijyaTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            VanijyaButton(
                text = "Commit Stock Adjustment",
                icon = Icons.Default.SwapVert,
                onClick = {
                    val target = selectedItem
                    val qty = qtyInput.toLongOrNull() ?: 0L
                    if (target != null && qty > 0L) {
                        val now = System.currentTimeMillis()
                        val adj = StockAdjustmentEntity(
                            id = UUID.randomUUID().toString(),
                            companyId = companyId,
                            itemId = target.id,
                            itemName = target.name,
                            warehouseId = target.warehouseId,
                            warehouseName = target.warehouseName,
                            adjustmentType = adjustmentType,
                            qty = qty,
                            reason = reason,
                            unitCostPaise = target.purchasePricePaise,
                            adjustmentDate = now,
                            notes = notes.ifBlank { "Manual stock adjustment" },
                            createdAt = now,
                            updatedAt = now
                        )
                        onRecordStockAdjustment(adj)
                        feedbackBanner = "✓ Adjusted ${target.name} ($adjustmentType $qty ${target.primaryUnit} • $reason)"
                        showStockAdjustmentSheet = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stock_adj_commit_btn")
            )
        }
    }

    // ========================================================================
    // SHEET 4: TRANSFER BETWEEN GODOWNS (WAREHOUSES)
    // ========================================================================
    if (showGodownTransferSheet) {
        val productItems = itemsList.filter { it.itemType == "PRODUCT" }
        var selectedItem by remember(productItems) { mutableStateOf(productItems.firstOrNull()) }
        var fromGodown by remember { mutableStateOf("Main Godown") }
        var toGodown by remember { mutableStateOf("Retail Counter") }
        var transferQty by remember { mutableStateOf("10") }
        var transferNotes by remember { mutableStateOf("") }

        GlassSheet(
            onDismissRequest = { showGodownTransferSheet = false },
            title = "Transfer Stock Between Godowns",
            subtitle = "Records TRANSFER_OUT and TRANSFER_IN stock ledger entries in one transaction"
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                productItems.forEach { prod ->
                    FilterChip(
                        selected = selectedItem?.id == prod.id,
                        onClick = {
                            selectedItem = prod
                            fromGodown = prod.warehouseName
                        },
                        label = { Text(prod.name.take(22)) },
                        colors = vanijyaFilterChipColors()
                    )
                }
            }

            Text("From Godown: $fromGodown → Select Destination Godown:", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Main Godown", "Retail Counter", "Cold Storage Annex").forEach { wh ->
                    FilterChip(
                        selected = toGodown == wh,
                        onClick = { toGodown = wh },
                        label = { Text(wh) },
                        colors = vanijyaFilterChipColors()
                    )
                }
            }

            OutlinedTextField(
                value = transferQty,
                onValueChange = { transferQty = it.filter(Char::isDigit).take(6) },
                label = { Text("Transfer Quantity") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = vanijyaTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = transferNotes,
                onValueChange = { transferNotes = it },
                label = { Text("Challan / Vehicle Note") },
                singleLine = true,
                colors = vanijyaTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            VanijyaButton(
                text = "Transfer Stock Now",
                icon = Icons.Default.CompareArrows,
                onClick = {
                    val target = selectedItem
                    val qty = transferQty.toLongOrNull() ?: 0L
                    if (target != null && qty > 0L && fromGodown != toGodown) {
                        val now = System.currentTimeMillis()
                        onRecordStockTransfer(
                            StockTransferEntity(
                                id = UUID.randomUUID().toString(),
                                companyId = companyId,
                                transferNumber = "TRF-${(1000..9999).random()}",
                                itemId = target.id,
                                itemName = target.name,
                                fromWarehouseId = "${companyId}_wh_from",
                                fromWarehouseName = fromGodown,
                                toWarehouseId = "${companyId}_wh_to",
                                toWarehouseName = toGodown,
                                qty = qty,
                                transferDate = now,
                                notes = transferNotes,
                                createdAt = now,
                                updatedAt = now
                            )
                        )
                        feedbackBanner = "✓ Transferred $qty ${target.primaryUnit} of '${target.name}' to $toGodown"
                        showGodownTransferSheet = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // ========================================================================
    // SHEET 5: CAMERAX BARCODE / QR SCANNER + MANUAL FALLBACK
    // ========================================================================
    if (showBarcodeScannerSheet) {
        var cameraPermissionGranted by remember { mutableStateOf(false) }
        var manualCode by remember { mutableStateOf("8901491101844") }
        val cameraPermLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            cameraPermissionGranted = granted
        }

        GlassSheet(
            onDismissRequest = { showBarcodeScannerSheet = false },
            title = "Barcode & QR Scanner (CameraX)",
            subtitle = "On-device CameraX preview + manual EAN/UPC/QR fallback"
        ) {
            if (!cameraPermissionGranted) {
                VanijyaButton(
                    text = "Grant Camera Permission & Start Live Scanner",
                    icon = Icons.Default.QrCodeScanner,
                    onClick = { cameraPermLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                val lifecycleOwner = LocalLifecycleOwner.current
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(2.dp, EmeraldPrimary, RoundedCornerShape(18.dp))
                ) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                runCatching {
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.surfaceProvider = previewView.surfaceProvider
                                    }
                                    val analysis = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()
                                    analysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                        imageProxy.close()
                                    }
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        analysis
                                    )
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            OutlinedTextField(
                value = manualCode,
                onValueChange = { manualCode = it },
                label = { Text("Enter or Confirm Scanned Barcode / SKU") },
                singleLine = true,
                colors = vanijyaTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("barcode_manual_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VanijyaButton(
                    text = "Find in Stock",
                    icon = Icons.Default.Search,
                    onClick = {
                        searchQuery = manualCode.trim()
                        scannedBarcodeValue = manualCode.trim()
                        showBarcodeScannerSheet = false
                        feedbackBanner = "✓ Filtered stock by barcode $manualCode"
                    },
                    modifier = Modifier.weight(1f)
                )
                VanijyaButton(
                    text = "New Item with Code",
                    icon = Icons.Default.QrCode2,
                    isOutlined = true,
                    onClick = {
                        scannedBarcodeValue = manualCode.trim()
                        editingItem = null
                        showBarcodeScannerSheet = false
                        showItemFormSheet = true
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // ========================================================================
    // SHEET 6: BULK PRICE / TAX UPDATE (WITH PRICE HISTORY IN stock_ledger)
    // ========================================================================
    if (showBulkPriceTaxSheet) {
        var priceDeltaPercent by remember { mutableIntStateOf(5) } // +5%
        var selectedNewTaxBps by remember { mutableStateOf<Int?>(null) }

        GlassSheet(
            onDismissRequest = { showBulkPriceTaxSheet = false },
            title = "Bulk Price & GST Slab Revision",
            subtitle = "Updates filtered items and writes PRICE_REVISION history in stock_ledger"
        ) {
            Text(
                text = "Select Price Revision Percentage (${filteredItems.size} items matched)",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(-10, -5, 0, 5, 10, 15).forEach { pct ->
                    FilterChip(
                        selected = priceDeltaPercent == pct,
                        onClick = { priceDeltaPercent = pct },
                        label = { Text(if (pct >= 0) "+$pct%" else "$pct%") },
                        colors = vanijyaFilterChipColors()
                    )
                }
            }

            Text(
                text = "Optional GST Slab Override",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedNewTaxBps == null,
                    onClick = { selectedNewTaxBps = null },
                    label = { Text("Keep Existing GST") },
                    colors = vanijyaFilterChipColors()
                )
                listOf(500 to "5%", 1200 to "12%", 1800 to "18%", 2800 to "28%").forEach { (bps, lbl) ->
                    FilterChip(
                        selected = selectedNewTaxBps == bps,
                        onClick = { selectedNewTaxBps = bps },
                        label = { Text("Set GST $lbl") },
                        colors = vanijyaFilterChipColors()
                    )
                }
            }

            VanijyaButton(
                text = "Apply Bulk Update to ${filteredItems.size} Items",
                icon = Icons.Default.PriceChange,
                onClick = {
                    val ids = filteredItems.map { it.id }
                    onBulkUpdatePricesAndTax(ids, priceDeltaPercent * 100, selectedNewTaxBps) { count ->
                        feedbackBanner = "✓ Bulk updated $count item(s) and logged price history"
                    }
                    showBulkPriceTaxSheet = false
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // ========================================================================
    // SHEET 7: BULK IMPORT / EXPORT (CSV/EXCEL TEMPLATE, DUPLICATE DETECTION & PDF)
    // ========================================================================
    if (showBulkImportExportSheet) {
        var csvDraft by remember { mutableStateOf(VanijyaCsvPorters.ITEM_CSV_TEMPLATE) }
        val previewRows = remember(csvDraft, itemsList) {
            VanijyaCsvPorters.parseItemCsvPreview(csvDraft, itemsList)
        }

        GlassSheet(
            onDismissRequest = { showBulkImportExportSheet = false },
            title = "Bulk Import / Export Items & Stock",
            subtitle = "Download CSV/Excel template, preview import with duplicate detection, or export PDF"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VanijyaButton(
                    text = "CSV Template",
                    icon = Icons.Default.FileDownload,
                    isOutlined = true,
                    onClick = {
                        VanijyaCsvPorters.exportItemsCsvAndShare(context, itemsList, templateOnly = true)
                        feedbackBanner = "✓ Shared downloadable Excel/CSV item template"
                    },
                    modifier = Modifier.weight(1f)
                )
                VanijyaButton(
                    text = "Export Excel",
                    icon = Icons.Default.FileDownload,
                    isOutlined = true,
                    onClick = {
                        VanijyaCsvPorters.exportItemsCsvAndShare(context, itemsList, templateOnly = false)
                        feedbackBanner = "✓ Exported ${itemsList.size} items to CSV/Excel"
                    },
                    modifier = Modifier.weight(1f)
                )
                VanijyaButton(
                    text = "Stock PDF",
                    icon = Icons.Default.PictureAsPdf,
                    onClick = {
                        VanijyaPdfExporter.exportAndShareStockSummaryPdf(
                            context = context,
                            company = activeCompany,
                            items = itemsList,
                            metrics = stockMetrics
                        )
                        feedbackBanner = "✓ Exported Stock Valuation PDF Report"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = csvDraft,
                onValueChange = { csvDraft = it },
                label = { Text("Paste or Edit Item CSV / Excel Rows") },
                minLines = 3,
                maxLines = 5,
                colors = vanijyaTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            previewRows.forEach { row ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    accentTint = if (row.duplicateReason != null) CoralError else EmeraldPrimary,
                    contentPadding = PaddingValues(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${row.name} (${row.itemCode})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "HSN ${row.hsnSacCode} • 1 ${row.secondaryUnit} = ${row.conversionRate} ${row.primaryUnit} • Stock: ${row.openingStockQty}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = row.salePrice.formatIndian(),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldPrimary
                            )
                            if (row.duplicateReason != null) {
                                Text(
                                    text = row.duplicateReason,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CoralError
                                )
                            }
                        }
                    }
                }
            }

            VanijyaButton(
                text = "Import ${previewRows.count { it.duplicateReason == null }} Unique Items",
                icon = Icons.Default.FileUpload,
                onClick = {
                    onBulkImportItems(previewRows) { count ->
                        feedbackBanner = "✓ Imported $count new items with opening stock ledger entries"
                    }
                    showBulkImportExportSheet = false
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // ========================================================================
    // SHEET 8: SEARCHABLE OFFLINE HSN / SAC DIRECTORY
    // ========================================================================
    if (showHsnDirectorySheet) {
        var hsnQuery by remember { mutableStateOf("") }
        val filteredHsn = remember(effectiveHsnList, hsnQuery) {
            effectiveHsnList.filter {
                hsnQuery.isBlank() ||
                    it.hsnCode.contains(hsnQuery, ignoreCase = true) ||
                    it.description.contains(hsnQuery, ignoreCase = true)
            }
        }

        GlassSheet(
            onDismissRequest = { showHsnDirectorySheet = false },
            title = "Offline HSN & SAC Directory (${effectiveHsnList.size})",
            subtitle = "Bundled in assets/hsn_sac_directory.csv — works 100% offline"
        ) {
            OutlinedTextField(
                value = hsnQuery,
                onValueChange = { hsnQuery = it },
                label = { Text("Search HSN/SAC code or commodity description") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search HSN") },
                singleLine = true,
                colors = vanijyaTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            filteredHsn.take(25).forEach { hsn ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(12.dp),
                    onClick = {
                        showHsnDirectorySheet = false
                        feedbackBanner = "✓ Selected ${if (hsn.isService) "SAC" else "HSN"} ${hsn.hsnCode} (GST ${hsn.defaultTaxRateBps / 100}%)"
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${if (hsn.isService) "SAC" else "HSN"} ${hsn.hsnCode}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = hsn.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "GST ${hsn.defaultTaxRateBps / 100}%",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldPrimary
                        )
                    }
                }
            }
        }
    }

    // ========================================================================
    // SHEET 9: ADD NEW CATEGORY OR GODOWN (WAREHOUSE)
    // ========================================================================
    if (showAddCategoryOrGodownSheet) {
        var newCatName by remember { mutableStateOf("") }
        var newWhName by remember { mutableStateOf("") }
        var newWhCode by remember { mutableStateOf("GDN-03") }
        var newWhAddress by remember { mutableStateOf("") }

        GlassSheet(
            onDismissRequest = { showAddCategoryOrGodownSheet = false },
            title = "Add Category or Godown",
            subtitle = "Create a new item category or physical godown/warehouse"
        ) {
            OutlinedTextField(
                value = newCatName,
                onValueChange = { newCatName = it },
                label = { Text("New Category Name (e.g. Pharmaceuticals)") },
                singleLine = true,
                colors = vanijyaTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            VanijyaButton(
                text = "Save Category",
                icon = Icons.Default.Category,
                onClick = {
                    if (newCatName.isNotBlank()) {
                        onSaveCategory(newCatName, "Custom category")
                        feedbackBanner = "✓ Created category '$newCatName'"
                        showAddCategoryOrGodownSheet = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(color = glass.subtleDivider)

            OutlinedTextField(
                value = newWhName,
                onValueChange = { newWhName = it },
                label = { Text("New Godown / Warehouse Name") },
                singleLine = true,
                colors = vanijyaTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = newWhCode,
                    onValueChange = { newWhCode = it.uppercase() },
                    label = { Text("Godown Code") },
                    singleLine = true,
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = newWhAddress,
                    onValueChange = { newWhAddress = it },
                    label = { Text("Location / Address") },
                    singleLine = true,
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
            }
            VanijyaButton(
                text = "Save Godown",
                icon = Icons.Default.Warehouse,
                isOutlined = true,
                onClick = {
                    if (newWhName.isNotBlank()) {
                        onSaveWarehouse(newWhName, newWhCode, newWhAddress, "Store Keeper", "")
                        feedbackBanner = "✓ Added godown '$newWhName'"
                        showAddCategoryOrGodownSheet = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
