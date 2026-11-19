package com.example.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// ============================================================================
// STEP 2A: PARTIES MASTER TABLES (party, party_address, party_group,
//          party_ledger_entry, party_custom_field)
// All monetary values are integer paise (Long) — never floating point.
// ============================================================================

@Entity(
    tableName = "party",
    indices = [
        Index("company_id"),
        Index("party_type"),
        Index("group_id"),
        Index("gstin"),
        Index("is_deleted")
    ]
)
data class PartyEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "party_type") val partyType: String = "CUSTOMER", // CUSTOMER, SUPPLIER, BOTH
    @ColumnInfo(name = "phone") val phone: String = "",
    @ColumnInfo(name = "alt_phone") val altPhone: String = "",
    @ColumnInfo(name = "email") val email: String = "",
    @ColumnInfo(name = "gstin") val gstin: String = "",
    @ColumnInfo(name = "pan") val pan: String = "",
    @ColumnInfo(name = "state_code") val stateCode: String = "27",
    @ColumnInfo(name = "state_name") val stateName: String = "Maharashtra",
    @ColumnInfo(name = "group_id") val groupId: String = "",
    @ColumnInfo(name = "group_name") val groupName: String = "General",
    @ColumnInfo(name = "opening_balance_paise") val openingBalancePaise: Long = 0L,
    @ColumnInfo(name = "is_to_collect") val isToCollect: Boolean = true, // true = To Collect (Dr), false = To Pay (Cr)
    /**
     * Net signed balance in integer paise:
     * > 0L => To Collect (Receivable from Party)
     * < 0L => To Pay (Payable to Party)
     * == 0L => Settled
     */
    @ColumnInfo(name = "current_balance_paise") val currentBalancePaise: Long = 0L,
    @ColumnInfo(name = "credit_limit_paise") val creditLimitPaise: Long = 0L,
    @ColumnInfo(name = "credit_days") val creditDays: Int = 30,
    @ColumnInfo(name = "price_list_id") val priceListId: String = "",
    @ColumnInfo(name = "price_list_name") val priceListName: String = "Standard Retail",
    @ColumnInfo(name = "notes") val notes: String = "",
    @ColumnInfo(name = "birthday") val birthday: String = "",
    @ColumnInfo(name = "anniversary") val anniversary: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "party_address",
    indices = [Index("company_id"), Index("party_id"), Index("is_deleted")]
)
data class PartyAddressEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "party_id") val partyId: String,
    @ColumnInfo(name = "address_type") val addressType: String = "BILLING", // BILLING, SHIPPING
    @ColumnInfo(name = "label") val label: String = "Primary Address",
    @ColumnInfo(name = "line1") val line1: String = "",
    @ColumnInfo(name = "city") val city: String = "",
    @ColumnInfo(name = "state_code") val stateCode: String = "27",
    @ColumnInfo(name = "state_name") val stateName: String = "Maharashtra",
    @ColumnInfo(name = "pincode") val pincode: String = "",
    @ColumnInfo(name = "is_default") val isDefault: Boolean = true,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "party_group",
    indices = [Index("company_id"), Index("is_deleted")]
)
data class PartyGroupEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "discount_bps") val discountBps: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

/**
 * Party ledger entry tracking chronological debit/credit movements and running balance.
 * Convention:
 * - debit_paise (+): Increases amount Receivable from Party (e.g. Sale Invoice, Opening To-Collect, Payment Out to Supplier)
 * - credit_paise (-): Increases amount Payable to Party / Reduces Receivable (e.g. Purchase Bill, Opening To-Pay, Payment In from Customer)
 * - running_balance_paise: Cumulative (sum of debits - sum of credits) up to and including this entry.
 */
@Entity(
    tableName = "party_ledger_entry",
    indices = [
        Index("company_id"),
        Index("party_id"),
        Index("entry_date"),
        Index("is_deleted")
    ]
)
data class PartyLedgerEntryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "party_id") val partyId: String,
    @ColumnInfo(name = "entry_date") val entryDate: Long,
    @ColumnInfo(name = "voucher_type") val voucherType: String, // OPENING_BALANCE, SALE_INVOICE, PURCHASE_BILL, PAYMENT_IN, PAYMENT_OUT, CREDIT_NOTE, DEBIT_NOTE
    @ColumnInfo(name = "voucher_number") val voucherNumber: String,
    @ColumnInfo(name = "debit_paise") val debitPaise: Long = 0L,
    @ColumnInfo(name = "credit_paise") val creditPaise: Long = 0L,
    @ColumnInfo(name = "running_balance_paise") val runningBalancePaise: Long = 0L,
    @ColumnInfo(name = "due_date") val dueDate: Long = 0L,
    @ColumnInfo(name = "is_settled") val isSettled: Boolean = false,
    @ColumnInfo(name = "notes") val notes: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "party_custom_field",
    indices = [Index("company_id"), Index("party_id"), Index("is_deleted")]
)
data class PartyCustomFieldEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "party_id") val partyId: String,
    @ColumnInfo(name = "field_key") val fieldKey: String,
    @ColumnInfo(name = "field_value") val fieldValue: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

// ============================================================================
// STEP 2B: ITEMS, STOCK, BATCHES, SERIALS, GODOWNS & PRICE LIST TABLES
// (item, item_category, item_unit, unit_conversion, item_batch, item_serial,
//  item_variant, warehouse, stock_ledger, stock_adjustment, stock_transfer,
//  item_bundle, price_list, price_list_item)
// ============================================================================

@Entity(
    tableName = "item",
    indices = [
        Index("company_id"),
        Index("category_id"),
        Index("item_code"),
        Index("barcode"),
        Index("hsn_sac_code"),
        Index("is_deleted")
    ]
)
data class ItemEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "item_type") val itemType: String = "PRODUCT", // PRODUCT, SERVICE
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "item_code") val itemCode: String = "",
    @ColumnInfo(name = "hsn_sac_code") val hsnSacCode: String = "",
    @ColumnInfo(name = "category_id") val categoryId: String = "",
    @ColumnInfo(name = "category_name") val categoryName: String = "General",
    @ColumnInfo(name = "brand") val brand: String = "",
    @ColumnInfo(name = "primary_unit") val primaryUnit: String = "PCS",
    @ColumnInfo(name = "secondary_unit") val secondaryUnit: String = "", // e.g., BOX
    @ColumnInfo(name = "conversion_rate") val conversionRate: Int = 1, // e.g., 1 BOX = 12 PCS
    @ColumnInfo(name = "sale_price_paise") val salePricePaise: Long = 0L,
    @ColumnInfo(name = "purchase_price_paise") val purchasePricePaise: Long = 0L,
    @ColumnInfo(name = "wholesale_price_paise") val wholesalePricePaise: Long = 0L,
    @ColumnInfo(name = "wholesale_min_qty") val wholesaleMinQty: Long = 10L,
    @ColumnInfo(name = "mrp_paise") val mrpPaise: Long = 0L,
    @ColumnInfo(name = "tax_rate_bps") val taxRateBps: Int = 1800, // 18% = 1800 bps
    @ColumnInfo(name = "tax_inclusive") val taxInclusive: Boolean = false,
    @ColumnInfo(name = "cess_bps") val cessBps: Int = 0,
    @ColumnInfo(name = "opening_stock_qty") val openingStockQty: Long = 0L,
    @ColumnInfo(name = "opening_stock_date") val openingStockDate: Long = 0L,
    @ColumnInfo(name = "current_stock_qty") val currentStockQty: Long = 0L,
    @ColumnInfo(name = "min_stock_alert_qty") val minStockAlertQty: Long = 5L,
    @ColumnInfo(name = "rack_location") val rackLocation: String = "",
    @ColumnInfo(name = "image_uri") val imageUri: String = "",
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "barcode") val barcode: String = "",
    @ColumnInfo(name = "track_batch") val trackBatch: Boolean = false,
    @ColumnInfo(name = "track_serial") val trackSerial: Boolean = false,
    @ColumnInfo(name = "warehouse_id") val warehouseId: String = "",
    @ColumnInfo(name = "warehouse_name") val warehouseName: String = "Main Godown",
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "item_category",
    indices = [Index("company_id"), Index("is_deleted")]
)
data class ItemCategoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "item_unit",
    indices = [Index("company_id"), Index("short_name"), Index("is_deleted")]
)
data class ItemUnitEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "short_name") val shortName: String, // PCS, BOX, KG, LTR, BAG, MTR, DOZ
    @ColumnInfo(name = "full_name") val fullName: String,
    @ColumnInfo(name = "uqc_code") val uqcCode: String = "", // GST UQC e.g. PCS-PIECES
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "unit_conversion",
    indices = [Index("company_id"), Index("is_deleted")]
)
data class UnitConversionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "base_unit") val baseUnit: String, // e.g., BOX
    @ColumnInfo(name = "secondary_unit") val secondaryUnit: String, // e.g., PCS
    @ColumnInfo(name = "multiplier") val multiplier: Int, // e.g., 12 (1 BOX = 12 PCS)
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "item_batch",
    indices = [
        Index("company_id"),
        Index("item_id"),
        Index("warehouse_id"),
        Index("expiry_date"),
        Index("is_deleted")
    ]
)
data class ItemBatchEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "item_id") val itemId: String,
    @ColumnInfo(name = "item_name") val itemName: String = "",
    @ColumnInfo(name = "warehouse_id") val warehouseId: String,
    @ColumnInfo(name = "warehouse_name") val warehouseName: String = "Main Godown",
    @ColumnInfo(name = "batch_number") val batchNumber: String,
    @ColumnInfo(name = "mfg_date") val mfgDate: Long = 0L,
    @ColumnInfo(name = "expiry_date") val expiryDate: Long = 0L,
    @ColumnInfo(name = "opening_qty") val openingQty: Long = 0L,
    @ColumnInfo(name = "available_qty") val availableQty: Long = 0L,
    @ColumnInfo(name = "purchase_price_paise") val purchasePricePaise: Long = 0L,
    @ColumnInfo(name = "sale_price_paise") val salePricePaise: Long = 0L,
    @ColumnInfo(name = "mrp_paise") val mrpPaise: Long = 0L,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "item_serial",
    indices = [
        Index("company_id"),
        Index("item_id"),
        Index("serial_or_imei"),
        Index("is_deleted")
    ]
)
data class ItemSerialEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "item_id") val itemId: String,
    @ColumnInfo(name = "batch_id") val batchId: String = "",
    @ColumnInfo(name = "warehouse_id") val warehouseId: String = "",
    @ColumnInfo(name = "serial_or_imei") val serialOrImei: String,
    @ColumnInfo(name = "status") val status: String = "IN_STOCK", // IN_STOCK, SOLD, RETURNED, DAMAGED
    @ColumnInfo(name = "warranty_months") val warrantyMonths: Int = 12,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "item_variant",
    indices = [Index("company_id"), Index("item_id"), Index("is_deleted")]
)
data class ItemVariantEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "item_id") val itemId: String,
    @ColumnInfo(name = "variant_name") val variantName: String, // e.g., "Size: L • Color: Indigo"
    @ColumnInfo(name = "sku_code") val skuCode: String = "",
    @ColumnInfo(name = "barcode") val barcode: String = "",
    @ColumnInfo(name = "sale_price_paise") val salePricePaise: Long = 0L,
    @ColumnInfo(name = "purchase_price_paise") val purchasePricePaise: Long = 0L,
    @ColumnInfo(name = "stock_qty") val stockQty: Long = 0L,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "warehouse",
    indices = [Index("company_id"), Index("is_deleted")]
)
data class WarehouseEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "name") val name: String, // e.g., "Main Godown", "Retail Shop Floor"
    @ColumnInfo(name = "code") val code: String = "GDN-01",
    @ColumnInfo(name = "address") val address: String = "",
    @ColumnInfo(name = "incharge_name") val inchargeName: String = "",
    @ColumnInfo(name = "phone") val phone: String = "",
    @ColumnInfo(name = "is_primary") val isPrimary: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "stock_ledger",
    indices = [
        Index("company_id"),
        Index("item_id"),
        Index("warehouse_id"),
        Index("movement_date"),
        Index("is_deleted")
    ]
)
data class StockLedgerEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "item_id") val itemId: String,
    @ColumnInfo(name = "item_name") val itemName: String = "",
    @ColumnInfo(name = "warehouse_id") val warehouseId: String,
    @ColumnInfo(name = "warehouse_name") val warehouseName: String = "Main Godown",
    @ColumnInfo(name = "batch_id") val batchId: String = "",
    @ColumnInfo(name = "movement_type") val movementType: String, // OPENING, PURCHASE_IN, SALE_OUT, ADJUSTMENT_ADD, ADJUSTMENT_REDUCE, TRANSFER_IN, TRANSFER_OUT, PRICE_REVISION
    @ColumnInfo(name = "voucher_number") val voucherNumber: String,
    @ColumnInfo(name = "qty_delta") val qtyDelta: Long, // +ve for inward, -ve for outward, 0 for price revision history
    @ColumnInfo(name = "closing_qty") val closingQty: Long,
    @ColumnInfo(name = "unit_rate_paise") val unitRatePaise: Long = 0L,
    @ColumnInfo(name = "movement_date") val movementDate: Long,
    @ColumnInfo(name = "remarks") val remarks: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "stock_adjustment",
    indices = [Index("company_id"), Index("item_id"), Index("warehouse_id"), Index("is_deleted")]
)
data class StockAdjustmentEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "item_id") val itemId: String,
    @ColumnInfo(name = "item_name") val itemName: String = "",
    @ColumnInfo(name = "warehouse_id") val warehouseId: String,
    @ColumnInfo(name = "warehouse_name") val warehouseName: String = "Main Godown",
    @ColumnInfo(name = "adjustment_type") val adjustmentType: String, // ADD, REDUCE
    @ColumnInfo(name = "qty") val qty: Long,
    @ColumnInfo(name = "reason") val reason: String, // DAMAGED, EXPIRED, AUDIT_CORRECTION, SHRINKAGE, PROMOTIONAL_SAMPLE
    @ColumnInfo(name = "unit_cost_paise") val unitCostPaise: Long = 0L,
    @ColumnInfo(name = "adjustment_date") val adjustmentDate: Long,
    @ColumnInfo(name = "notes") val notes: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "stock_transfer",
    indices = [Index("company_id"), Index("item_id"), Index("is_deleted")]
)
data class StockTransferEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "transfer_number") val transferNumber: String,
    @ColumnInfo(name = "item_id") val itemId: String,
    @ColumnInfo(name = "item_name") val itemName: String = "",
    @ColumnInfo(name = "from_warehouse_id") val fromWarehouseId: String,
    @ColumnInfo(name = "from_warehouse_name") val fromWarehouseName: String = "",
    @ColumnInfo(name = "to_warehouse_id") val toWarehouseId: String,
    @ColumnInfo(name = "to_warehouse_name") val toWarehouseName: String = "",
    @ColumnInfo(name = "qty") val qty: Long,
    @ColumnInfo(name = "transfer_date") val transferDate: Long,
    @ColumnInfo(name = "notes") val notes: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "item_bundle",
    indices = [Index("company_id"), Index("parent_item_id"), Index("is_deleted")]
)
data class ItemBundleEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "parent_item_id") val parentItemId: String,
    @ColumnInfo(name = "bundle_name") val bundleName: String,
    @ColumnInfo(name = "component_item_id") val componentItemId: String,
    @ColumnInfo(name = "component_item_name") val componentItemName: String,
    @ColumnInfo(name = "component_qty") val componentQty: Long = 1L,
    @ColumnInfo(name = "bundle_sale_price_paise") val bundleSalePricePaise: Long = 0L,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "price_list",
    indices = [Index("company_id"), Index("is_deleted")]
)
data class PriceListEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "name") val name: String, // e.g., "Standard Retail", "Distributor Tier-1", "VIP Wholesale"
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "default_discount_bps") val defaultDiscountBps: Int = 0,
    @ColumnInfo(name = "is_default") val isDefault: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "price_list_item",
    indices = [
        Index("company_id"),
        Index("price_list_id"),
        Index("item_id"),
        Index("is_deleted")
    ]
)
data class PriceListItemEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "price_list_id") val priceListId: String,
    @ColumnInfo(name = "item_id") val itemId: String,
    @ColumnInfo(name = "custom_sale_price_paise") val customSalePricePaise: Long,
    @ColumnInfo(name = "min_order_qty") val minOrderQty: Long = 1L,
    @ColumnInfo(name = "discount_bps") val discountBps: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)
