package com.example.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface Step2Dao {

    // ========================================================================
    // 1. PARTIES QUERIES & CRUD
    // ========================================================================

    @Query("SELECT * FROM party WHERE company_id = :companyId AND is_deleted = 0 ORDER BY name COLLATE NOCASE ASC")
    fun observeParties(companyId: String): Flow<List<PartyEntity>>

    @Query("SELECT * FROM party WHERE company_id = :companyId AND is_deleted = 0 ORDER BY name COLLATE NOCASE ASC")
    suspend fun getPartiesNow(companyId: String): List<PartyEntity>

    @Query("SELECT * FROM party WHERE id = :partyId AND is_deleted = 0 LIMIT 1")
    suspend fun getPartyById(partyId: String): PartyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertParty(party: PartyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertParties(parties: List<PartyEntity>)

    // Party Addresses
    @Query("SELECT * FROM party_address WHERE company_id = :companyId AND is_deleted = 0 ORDER BY is_default DESC, created_at ASC")
    fun observePartyAddresses(companyId: String): Flow<List<PartyAddressEntity>>

    @Query("SELECT * FROM party_address WHERE party_id = :partyId AND is_deleted = 0 ORDER BY is_default DESC, created_at ASC")
    suspend fun getAddressesForPartyNow(partyId: String): List<PartyAddressEntity>

    @Query("DELETE FROM party_address WHERE party_id = :partyId")
    suspend fun deleteAddressesForParty(partyId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPartyAddresses(addresses: List<PartyAddressEntity>)

    // Party Groups
    @Query("SELECT * FROM party_group WHERE company_id = :companyId AND is_deleted = 0 ORDER BY name COLLATE NOCASE ASC")
    fun observePartyGroups(companyId: String): Flow<List<PartyGroupEntity>>

    @Query("SELECT * FROM party_group WHERE company_id = :companyId AND is_deleted = 0 ORDER BY name COLLATE NOCASE ASC")
    suspend fun getPartyGroupsNow(companyId: String): List<PartyGroupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPartyGroup(group: PartyGroupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPartyGroups(groups: List<PartyGroupEntity>)

    // Party Custom Fields
    @Query("SELECT * FROM party_custom_field WHERE company_id = :companyId AND is_deleted = 0 ORDER BY created_at ASC")
    fun observePartyCustomFields(companyId: String): Flow<List<PartyCustomFieldEntity>>

    @Query("SELECT * FROM party_custom_field WHERE party_id = :partyId AND is_deleted = 0 ORDER BY created_at ASC")
    suspend fun getCustomFieldsForPartyNow(partyId: String): List<PartyCustomFieldEntity>

    @Query("DELETE FROM party_custom_field WHERE party_id = :partyId")
    suspend fun deleteCustomFieldsForParty(partyId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPartyCustomFields(fields: List<PartyCustomFieldEntity>)

    // Party Ledger Entries
    @Query("SELECT * FROM party_ledger_entry WHERE company_id = :companyId AND is_deleted = 0 ORDER BY entry_date ASC, created_at ASC")
    fun observeAllPartyLedgerEntries(companyId: String): Flow<List<PartyLedgerEntryEntity>>

    @Query("SELECT * FROM party_ledger_entry WHERE party_id = :partyId AND is_deleted = 0 ORDER BY entry_date ASC, created_at ASC")
    fun observePartyLedgerEntries(partyId: String): Flow<List<PartyLedgerEntryEntity>>

    @Query("SELECT * FROM party_ledger_entry WHERE party_id = :partyId AND is_deleted = 0 ORDER BY entry_date ASC, created_at ASC")
    suspend fun getPartyLedgerEntriesNow(partyId: String): List<PartyLedgerEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPartyLedgerEntry(entry: PartyLedgerEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPartyLedgerEntries(entries: List<PartyLedgerEntryEntity>)

    /**
     * Multi-table transaction: Saves a Party along with its multiple Billing/Shipping addresses,
     * custom fields, and opening balance ledger entry, then recalculates the chronological
     * running balance and updates `party.current_balance_paise`.
     */
    @Transaction
    suspend fun savePartyWithDetailsInTransaction(
        party: PartyEntity,
        addresses: List<PartyAddressEntity>,
        customFields: List<PartyCustomFieldEntity>
    ) {
        upsertParty(party)
        deleteAddressesForParty(party.id)
        if (addresses.isNotEmpty()) {
            upsertPartyAddresses(addresses)
        }
        deleteCustomFieldsForParty(party.id)
        if (customFields.isNotEmpty()) {
            upsertPartyCustomFields(customFields)
        }

        val existingEntries = getPartyLedgerEntriesNow(party.id)
        val openingId = "${party.id}_opening"
        val existingOpening = existingEntries.firstOrNull {
            it.id == openingId || it.voucherType == "OPENING_BALANCE"
        }

        val debitPaise = if (party.isToCollect) party.openingBalancePaise else 0L
        val creditPaise = if (!party.isToCollect) party.openingBalancePaise else 0L

        if (party.openingBalancePaise > 0L || existingOpening != null) {
            val openingEntry = PartyLedgerEntryEntity(
                id = existingOpening?.id ?: openingId,
                companyId = party.companyId,
                partyId = party.id,
                entryDate = existingOpening?.entryDate ?: (party.createdAt - 86_400_000L),
                voucherType = "OPENING_BALANCE",
                voucherNumber = "OPEN-BAL",
                debitPaise = debitPaise,
                creditPaise = creditPaise,
                runningBalancePaise = debitPaise - creditPaise,
                dueDate = (existingOpening?.entryDate ?: party.createdAt) + party.creditDays * 86_400_000L,
                isSettled = party.openingBalancePaise == 0L,
                notes = "Opening balance (${if (party.isToCollect) "To Collect" else "To Pay"})",
                createdAt = existingOpening?.createdAt ?: party.createdAt,
                updatedAt = party.updatedAt,
                isDeleted = false
            )
            upsertPartyLedgerEntry(openingEntry)
        }

        recalculatePartyRunningBalanceInTransaction(party.id, party.updatedAt)
    }

    /**
     * Multi-table transaction: Appends a ledger entry (Sale, Purchase, Payment In/Out, Credit/Debit Note)
     * and atomically recalculates the running balance across all entries and updates `party.current_balance_paise`.
     */
    @Transaction
    suspend fun recordPartyLedgerMovementInTransaction(entry: PartyLedgerEntryEntity): Long {
        upsertPartyLedgerEntry(entry)
        return recalculatePartyRunningBalanceInTransaction(entry.partyId, entry.updatedAt)
    }

    @Transaction
    suspend fun recalculatePartyRunningBalanceInTransaction(partyId: String, now: Long): Long {
        val entries = getPartyLedgerEntriesNow(partyId)
        var runningPaise = 0L
        val updatedEntries = entries.map { item ->
            runningPaise = Math.addExact(
                runningPaise,
                Math.subtractExact(item.debitPaise, item.creditPaise)
            )
            item.copy(runningBalancePaise = runningPaise, updatedAt = now)
        }
        if (updatedEntries.isNotEmpty()) {
            upsertPartyLedgerEntries(updatedEntries)
        }
        val party = getPartyById(partyId)
        if (party != null) {
            upsertParty(
                party.copy(
                    currentBalancePaise = runningPaise,
                    updatedAt = now
                )
            )
        }
        return runningPaise
    }

    @Transaction
    suspend fun softDeletePartiesInTransaction(partyIds: List<String>, now: Long) {
        partyIds.forEach { partyId ->
            val p = getPartyById(partyId)
            if (p != null) {
                upsertParty(p.copy(isDeleted = true, updatedAt = now))
            }
            val addrs = getAddressesForPartyNow(partyId).map { it.copy(isDeleted = true, updatedAt = now) }
            if (addrs.isNotEmpty()) upsertPartyAddresses(addrs)
            val fields = getCustomFieldsForPartyNow(partyId).map { it.copy(isDeleted = true, updatedAt = now) }
            if (fields.isNotEmpty()) upsertPartyCustomFields(fields)
            val entries = getPartyLedgerEntriesNow(partyId).map { it.copy(isDeleted = true, updatedAt = now) }
            if (entries.isNotEmpty()) upsertPartyLedgerEntries(entries)
        }
    }

    // ========================================================================
    // 2. ITEMS, CATEGORIES, UNITS, BATCHES, SERIALS, VARIANTS, GODOWNS & STOCK
    // ========================================================================

    @Query("SELECT * FROM item WHERE company_id = :companyId AND is_deleted = 0 ORDER BY name COLLATE NOCASE ASC")
    fun observeItems(companyId: String): Flow<List<ItemEntity>>

    @Query("SELECT * FROM item WHERE company_id = :companyId AND is_deleted = 0 ORDER BY name COLLATE NOCASE ASC")
    suspend fun getItemsNow(companyId: String): List<ItemEntity>

    @Query("SELECT * FROM item WHERE id = :itemId AND is_deleted = 0 LIMIT 1")
    suspend fun getItemById(itemId: String): ItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: ItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItems(items: List<ItemEntity>)

    // Categories
    @Query("SELECT * FROM item_category WHERE company_id = :companyId AND is_deleted = 0 ORDER BY name COLLATE NOCASE ASC")
    fun observeItemCategories(companyId: String): Flow<List<ItemCategoryEntity>>

    @Query("SELECT * FROM item_category WHERE company_id = :companyId AND is_deleted = 0 ORDER BY name COLLATE NOCASE ASC")
    suspend fun getItemCategoriesNow(companyId: String): List<ItemCategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItemCategory(category: ItemCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItemCategories(categories: List<ItemCategoryEntity>)

    // Units & Conversions
    @Query("SELECT * FROM item_unit WHERE company_id = :companyId AND is_deleted = 0 ORDER BY short_name ASC")
    fun observeItemUnits(companyId: String): Flow<List<ItemUnitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItemUnits(units: List<ItemUnitEntity>)

    @Query("SELECT * FROM unit_conversion WHERE company_id = :companyId AND is_deleted = 0")
    fun observeUnitConversions(companyId: String): Flow<List<UnitConversionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUnitConversion(conversion: UnitConversionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUnitConversions(conversions: List<UnitConversionEntity>)

    // Batches
    @Query("SELECT * FROM item_batch WHERE company_id = :companyId AND is_deleted = 0 ORDER BY expiry_date ASC")
    fun observeItemBatches(companyId: String): Flow<List<ItemBatchEntity>>

    @Query("SELECT * FROM item_batch WHERE item_id = :itemId AND is_deleted = 0 ORDER BY expiry_date ASC")
    suspend fun getBatchesForItemNow(itemId: String): List<ItemBatchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItemBatch(batch: ItemBatchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItemBatches(batches: List<ItemBatchEntity>)

    // Serials / IMEIs
    @Query("SELECT * FROM item_serial WHERE company_id = :companyId AND is_deleted = 0 ORDER BY created_at DESC")
    fun observeItemSerials(companyId: String): Flow<List<ItemSerialEntity>>

    @Query("SELECT * FROM item_serial WHERE item_id = :itemId AND is_deleted = 0 ORDER BY created_at DESC")
    suspend fun getSerialsForItemNow(itemId: String): List<ItemSerialEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItemSerials(serials: List<ItemSerialEntity>)

    // Variants
    @Query("SELECT * FROM item_variant WHERE company_id = :companyId AND is_deleted = 0 ORDER BY variant_name ASC")
    fun observeItemVariants(companyId: String): Flow<List<ItemVariantEntity>>

    @Query("SELECT * FROM item_variant WHERE item_id = :itemId AND is_deleted = 0 ORDER BY variant_name ASC")
    suspend fun getVariantsForItemNow(itemId: String): List<ItemVariantEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItemVariants(variants: List<ItemVariantEntity>)

    // Warehouses (Godowns)
    @Query("SELECT * FROM warehouse WHERE company_id = :companyId AND is_deleted = 0 ORDER BY is_primary DESC, name ASC")
    fun observeWarehouses(companyId: String): Flow<List<WarehouseEntity>>

    @Query("SELECT * FROM warehouse WHERE company_id = :companyId AND is_deleted = 0 ORDER BY is_primary DESC, name ASC")
    suspend fun getWarehousesNow(companyId: String): List<WarehouseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWarehouse(warehouse: WarehouseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWarehouses(warehouses: List<WarehouseEntity>)

    // Stock Ledger
    @Query("SELECT * FROM stock_ledger WHERE company_id = :companyId AND is_deleted = 0 ORDER BY movement_date DESC, created_at DESC")
    fun observeStockLedger(companyId: String): Flow<List<StockLedgerEntity>>

    @Query("SELECT * FROM stock_ledger WHERE item_id = :itemId AND is_deleted = 0 ORDER BY movement_date ASC, created_at ASC")
    suspend fun getStockLedgerForItemNow(itemId: String): List<StockLedgerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStockLedgerEntry(entry: StockLedgerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStockLedgerEntries(entries: List<StockLedgerEntity>)

    // Stock Adjustments
    @Query("SELECT * FROM stock_adjustment WHERE company_id = :companyId AND is_deleted = 0 ORDER BY adjustment_date DESC")
    fun observeStockAdjustments(companyId: String): Flow<List<StockAdjustmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStockAdjustment(adjustment: StockAdjustmentEntity)

    // Stock Transfers
    @Query("SELECT * FROM stock_transfer WHERE company_id = :companyId AND is_deleted = 0 ORDER BY transfer_date DESC")
    fun observeStockTransfers(companyId: String): Flow<List<StockTransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStockTransfer(transfer: StockTransferEntity)

    // Bundles / Kits
    @Query("SELECT * FROM item_bundle WHERE company_id = :companyId AND is_deleted = 0 ORDER BY bundle_name ASC")
    fun observeItemBundles(companyId: String): Flow<List<ItemBundleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItemBundles(bundles: List<ItemBundleEntity>)

    // Price Lists & Price List Items
    @Query("SELECT * FROM price_list WHERE company_id = :companyId AND is_deleted = 0 ORDER BY is_default DESC, name ASC")
    fun observePriceLists(companyId: String): Flow<List<PriceListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPriceLists(lists: List<PriceListEntity>)

    @Query("SELECT * FROM price_list_item WHERE company_id = :companyId AND is_deleted = 0")
    fun observePriceListItems(companyId: String): Flow<List<PriceListItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPriceListItems(items: List<PriceListItemEntity>)

    /**
     * Multi-table transaction: Saves an Item along with its category, unit conversion,
     * initial batch/variants/serials/bundles, and records the OPENING stock ledger entry
     * (or PRICE_REVISION history if price changed on an existing item).
     */
    @Transaction
    suspend fun saveItemWithDetailsInTransaction(
        item: ItemEntity,
        initialBatch: ItemBatchEntity? = null,
        variants: List<ItemVariantEntity> = emptyList(),
        serials: List<ItemSerialEntity> = emptyList(),
        bundles: List<ItemBundleEntity> = emptyList()
    ) {
        val existingItem = getItemById(item.id)
        upsertItem(item)

        if (item.secondaryUnit.isNotBlank() && item.conversionRate > 1) {
            upsertUnitConversion(
                UnitConversionEntity(
                    id = "${item.companyId}_${item.secondaryUnit}_${item.primaryUnit}",
                    companyId = item.companyId,
                    baseUnit = item.secondaryUnit,
                    secondaryUnit = item.primaryUnit,
                    multiplier = item.conversionRate,
                    createdAt = item.createdAt,
                    updatedAt = item.updatedAt
                )
            )
        }

        if (initialBatch != null) {
            upsertItemBatch(initialBatch)
        }
        if (variants.isNotEmpty()) {
            upsertItemVariants(variants)
        }
        if (serials.isNotEmpty()) {
            upsertItemSerials(serials)
        }
        if (bundles.isNotEmpty()) {
            upsertItemBundles(bundles)
        }

        if (existingItem == null) {
            if (item.itemType == "PRODUCT" && item.openingStockQty != 0L) {
                upsertStockLedgerEntry(
                    StockLedgerEntity(
                        id = "${item.id}_opening_stock",
                        companyId = item.companyId,
                        itemId = item.id,
                        itemName = item.name,
                        warehouseId = item.warehouseId,
                        warehouseName = item.warehouseName,
                        batchId = initialBatch?.id ?: "",
                        movementType = "OPENING",
                        voucherNumber = "OPEN-STK",
                        qtyDelta = item.openingStockQty,
                        closingQty = item.currentStockQty,
                        unitRatePaise = item.purchasePricePaise,
                        movementDate = item.openingStockDate.takeIf { it > 0L } ?: item.createdAt,
                        remarks = "Opening stock (${item.openingStockQty} ${item.primaryUnit})",
                        createdAt = item.createdAt,
                        updatedAt = item.updatedAt
                    )
                )
            }
        } else if (existingItem.salePricePaise != item.salePricePaise ||
            existingItem.purchasePricePaise != item.purchasePricePaise
        ) {
            upsertStockLedgerEntry(
                StockLedgerEntity(
                    id = UUID.randomUUID().toString(),
                    companyId = item.companyId,
                    itemId = item.id,
                    itemName = item.name,
                    warehouseId = item.warehouseId,
                    warehouseName = item.warehouseName,
                    batchId = "",
                    movementType = "PRICE_REVISION",
                    voucherNumber = "PRC-REV",
                    qtyDelta = 0L,
                    closingQty = item.currentStockQty,
                    unitRatePaise = item.salePricePaise,
                    movementDate = item.updatedAt,
                    remarks = "Sale price updated from ₹${existingItem.salePricePaise / 100} to ₹${item.salePricePaise / 100}",
                    createdAt = item.updatedAt,
                    updatedAt = item.updatedAt
                )
            )
        }
    }

    /**
     * Multi-table transaction: Records a Stock Adjustment (ADD or REDUCE) with reason,
     * updates the Item's currentStockQty, and appends a StockLedgerEntity movement row.
     */
    @Transaction
    suspend fun recordStockAdjustmentInTransaction(
        adjustment: StockAdjustmentEntity
    ): Long {
        val item = getItemById(adjustment.itemId) ?: return 0L
        val signedDelta = if (adjustment.adjustmentType == "REDUCE") {
            -adjustment.qty
        } else {
            adjustment.qty
        }
        val newClosingQty = item.currentStockQty + signedDelta
        val now = adjustment.updatedAt

        upsertStockAdjustment(adjustment)
        upsertItem(
            item.copy(
                currentStockQty = newClosingQty,
                updatedAt = now
            )
        )
        upsertStockLedgerEntry(
            StockLedgerEntity(
                id = "${adjustment.id}_ledger",
                companyId = adjustment.companyId,
                itemId = item.id,
                itemName = item.name,
                warehouseId = adjustment.warehouseId,
                warehouseName = adjustment.warehouseName,
                batchId = "",
                movementType = if (adjustment.adjustmentType == "REDUCE") "ADJUSTMENT_REDUCE" else "ADJUSTMENT_ADD",
                voucherNumber = "ADJ-${adjustment.id.takeLast(4).uppercase()}",
                qtyDelta = signedDelta,
                closingQty = newClosingQty,
                unitRatePaise = adjustment.unitCostPaise.takeIf { it > 0L } ?: item.purchasePricePaise,
                movementDate = adjustment.adjustmentDate,
                remarks = "${adjustment.reason}: ${adjustment.notes}".trimEnd(':', ' '),
                createdAt = now,
                updatedAt = now
            )
        )
        return newClosingQty
    }

    /**
     * Multi-table transaction: Transfers stock between two godowns (warehouses) and
     * records both TRANSFER_OUT and TRANSFER_IN entries in `stock_ledger`.
     */
    @Transaction
    suspend fun recordStockTransferInTransaction(
        transfer: StockTransferEntity
    ) {
        val item = getItemById(transfer.itemId) ?: return
        val now = transfer.updatedAt
        upsertStockTransfer(transfer)

        val outEntry = StockLedgerEntity(
            id = "${transfer.id}_out",
            companyId = transfer.companyId,
            itemId = item.id,
            itemName = item.name,
            warehouseId = transfer.fromWarehouseId,
            warehouseName = transfer.fromWarehouseName,
            batchId = "",
            movementType = "TRANSFER_OUT",
            voucherNumber = transfer.transferNumber,
            qtyDelta = -transfer.qty,
            closingQty = item.currentStockQty,
            unitRatePaise = item.purchasePricePaise,
            movementDate = transfer.transferDate,
            remarks = "Transferred to ${transfer.toWarehouseName} (${transfer.notes})".trim(),
            createdAt = now,
            updatedAt = now
        )

        val inEntry = StockLedgerEntity(
            id = "${transfer.id}_in",
            companyId = transfer.companyId,
            itemId = item.id,
            itemName = item.name,
            warehouseId = transfer.toWarehouseId,
            warehouseName = transfer.toWarehouseName,
            batchId = "",
            movementType = "TRANSFER_IN",
            voucherNumber = transfer.transferNumber,
            qtyDelta = transfer.qty,
            closingQty = item.currentStockQty,
            unitRatePaise = item.purchasePricePaise,
            movementDate = transfer.transferDate + 1L,
            remarks = "Received from ${transfer.fromWarehouseName} (${transfer.notes})".trim(),
            createdAt = now + 1L,
            updatedAt = now + 1L
        )

        upsertStockLedgerEntries(listOf(outEntry, inEntry))
        upsertItem(
            item.copy(
                warehouseId = transfer.toWarehouseId,
                warehouseName = transfer.toWarehouseName,
                updatedAt = now
            )
        )
    }

    /**
     * Multi-table transaction: General stock movement (PURCHASE_IN, SALE_OUT, RETURN_IN, RETURN_OUT)
     * updating `item.current_stock_qty` and inserting a `stock_ledger` entry with exact `closing_qty`.
     */
    @Transaction
    suspend fun recordStockMovementInTransaction(
        itemId: String,
        warehouseId: String,
        warehouseName: String,
        batchId: String = "",
        movementType: String,
        voucherNumber: String,
        qtyDelta: Long,
        unitRatePaise: Long,
        movementDate: Long,
        remarks: String
    ): Long {
        val item = getItemById(itemId) ?: return 0L
        val newClosing = item.currentStockQty + qtyDelta
        val now = System.currentTimeMillis()
        upsertItem(
            item.copy(
                currentStockQty = newClosing,
                updatedAt = now
            )
        )
        upsertStockLedgerEntry(
            StockLedgerEntity(
                id = UUID.randomUUID().toString(),
                companyId = item.companyId,
                itemId = item.id,
                itemName = item.name,
                warehouseId = warehouseId.ifBlank { item.warehouseId },
                warehouseName = warehouseName.ifBlank { item.warehouseName },
                batchId = batchId,
                movementType = movementType,
                voucherNumber = voucherNumber,
                qtyDelta = qtyDelta,
                closingQty = newClosing,
                unitRatePaise = unitRatePaise,
                movementDate = movementDate,
                remarks = remarks,
                createdAt = now,
                updatedAt = now
            )
        )
        return newClosing
    }

    /**
     * Multi-table transaction: Bulk updates item prices (by percentage bps or flat paise)
     * and/or GST tax slab, and writes price-history audit entries to `stock_ledger`.
     */
    @Transaction
    suspend fun bulkUpdateItemPricesAndTaxInTransaction(
        companyId: String,
        itemIds: List<String>,
        priceChangeBps: Int,
        newTaxRateBps: Int?,
        now: Long
    ): Int {
        var count = 0
        itemIds.forEach { id ->
            val item = getItemById(id) ?: return@forEach
            val deltaSale = (item.salePricePaise * priceChangeBps.toLong()) / 10_000L
            val newSalePaise = (item.salePricePaise + deltaSale).coerceAtLeast(0L)
            val updatedTaxBps = newTaxRateBps ?: item.taxRateBps
            val updated = item.copy(
                salePricePaise = newSalePaise,
                taxRateBps = updatedTaxBps,
                updatedAt = now
            )
            upsertItem(updated)
            upsertStockLedgerEntry(
                StockLedgerEntity(
                    id = UUID.randomUUID().toString(),
                    companyId = companyId,
                    itemId = item.id,
                    itemName = item.name,
                    warehouseId = item.warehouseId,
                    warehouseName = item.warehouseName,
                    batchId = "",
                    movementType = "PRICE_REVISION",
                    voucherNumber = "BULK-UPD",
                    qtyDelta = 0L,
                    closingQty = item.currentStockQty,
                    unitRatePaise = newSalePaise,
                    movementDate = now,
                    remarks = "Bulk update: Sale ₹${item.salePricePaise / 100} → ₹${newSalePaise / 100}, GST ${updatedTaxBps / 100}%",
                    createdAt = now,
                    updatedAt = now
                )
            )
            count++
        }
        return count
    }

    // Clear all Step 2 tables on full app reset
    @Query("DELETE FROM party")
    suspend fun clearParties()

    @Query("DELETE FROM party_address")
    suspend fun clearPartyAddresses()

    @Query("DELETE FROM party_group")
    suspend fun clearPartyGroups()

    @Query("DELETE FROM party_ledger_entry")
    suspend fun clearPartyLedgerEntries()

    @Query("DELETE FROM party_custom_field")
    suspend fun clearPartyCustomFields()

    @Query("DELETE FROM item")
    suspend fun clearItems()

    @Query("DELETE FROM item_category")
    suspend fun clearItemCategories()

    @Query("DELETE FROM item_unit")
    suspend fun clearItemUnits()

    @Query("DELETE FROM unit_conversion")
    suspend fun clearUnitConversions()

    @Query("DELETE FROM item_batch")
    suspend fun clearItemBatches()

    @Query("DELETE FROM item_serial")
    suspend fun clearItemSerials()

    @Query("DELETE FROM item_variant")
    suspend fun clearItemVariants()

    @Query("DELETE FROM warehouse")
    suspend fun clearWarehouses()

    @Query("DELETE FROM stock_ledger")
    suspend fun clearStockLedger()

    @Query("DELETE FROM stock_adjustment")
    suspend fun clearStockAdjustments()

    @Query("DELETE FROM stock_transfer")
    suspend fun clearStockTransfers()

    @Query("DELETE FROM item_bundle")
    suspend fun clearItemBundles()

    @Query("DELETE FROM price_list")
    suspend fun clearPriceLists()

    @Query("DELETE FROM price_list_item")
    suspend fun clearPriceListItems()
}
