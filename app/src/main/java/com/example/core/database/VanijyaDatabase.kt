package com.example.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        // Step 1 Core Foundation Tables (10)
        CompanyEntity::class,
        AppUserEntity::class,
        RolePermissionEntity::class,
        SettingEntity::class,
        NumberSeriesEntity::class,
        AuditLogEntity::class,
        AttachmentEntity::class,
        FinancialYearEntity::class,
        TaxRateEntity::class,
        HsnMasterEntity::class,
        // Step 2 Parties & Items/Stock Master Tables (19)
        PartyEntity::class,
        PartyAddressEntity::class,
        PartyGroupEntity::class,
        PartyLedgerEntryEntity::class,
        PartyCustomFieldEntity::class,
        ItemEntity::class,
        ItemCategoryEntity::class,
        ItemUnitEntity::class,
        UnitConversionEntity::class,
        ItemBatchEntity::class,
        ItemSerialEntity::class,
        ItemVariantEntity::class,
        WarehouseEntity::class,
        StockLedgerEntity::class,
        StockAdjustmentEntity::class,
        StockTransferEntity::class,
        ItemBundleEntity::class,
        PriceListEntity::class,
        PriceListItemEntity::class,
        // Step 3A Sales, GST Tax Lines, Links, Payments, Cheques & Recurring Tables (9)
        TxnEntity::class,
        TxnLineEntity::class,
        TxnChargeEntity::class,
        TxnTaxLineEntity::class,
        TxnLinkEntity::class,
        PaymentEntity::class,
        PaymentAllocationEntity::class,
        ChequeEntity::class,
        RecurringTemplateEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class VanijyaDatabase : RoomDatabase() {
    abstract fun coreDao(): CoreDao
    abstract fun step2Dao(): Step2Dao
    abstract fun step3ADao(): Step3ADao

    companion object {
        const val DATABASE_NAME = "vanijya_encrypted_v1.db"

        /**
         * Explicit non-destructive Room migration from v2 to v3.
         * Preserves all 29 v1+v2 tables and creates the 9 Step 3A tables.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. txn
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `txn` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `doc_type` TEXT NOT NULL,
                        `doc_number` TEXT NOT NULL,
                        `txn_date` INTEGER NOT NULL,
                        `due_date` INTEGER NOT NULL,
                        `party_id` TEXT NOT NULL,
                        `party_name` TEXT NOT NULL,
                        `party_gstin` TEXT NOT NULL,
                        `party_phone` TEXT NOT NULL,
                        `billing_address` TEXT NOT NULL,
                        `shipping_address` TEXT NOT NULL,
                        `place_of_supply_state_code` TEXT NOT NULL,
                        `place_of_supply_state_name` TEXT NOT NULL,
                        `supply_type` TEXT NOT NULL,
                        `classification` TEXT NOT NULL,
                        `is_reverse_charge` INTEGER NOT NULL,
                        `is_composition` INTEGER NOT NULL,
                        `gross_subtotal_paise` INTEGER NOT NULL,
                        `line_discount_total_paise` INTEGER NOT NULL,
                        `invoice_discount_bps` INTEGER NOT NULL,
                        `invoice_discount_paise` INTEGER NOT NULL,
                        `taxable_subtotal_paise` INTEGER NOT NULL,
                        `cgst_total_paise` INTEGER NOT NULL,
                        `sgst_total_paise` INTEGER NOT NULL,
                        `igst_total_paise` INTEGER NOT NULL,
                        `cess_total_paise` INTEGER NOT NULL,
                        `total_tax_paise` INTEGER NOT NULL,
                        `additional_charges_total_paise` INTEGER NOT NULL,
                        `tcs_bps` INTEGER NOT NULL,
                        `tcs_paise` INTEGER NOT NULL,
                        `tds_bps` INTEGER NOT NULL,
                        `tds_paise` INTEGER NOT NULL,
                        `round_off_paise` INTEGER NOT NULL,
                        `rounding_mode` TEXT NOT NULL,
                        `grand_total_paise` INTEGER NOT NULL,
                        `paid_amount_paise` INTEGER NOT NULL,
                        `balance_amount_paise` INTEGER NOT NULL,
                        `total_cost_paise` INTEGER NOT NULL,
                        `gross_profit_paise` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `cancel_reason` TEXT NOT NULL,
                        `payment_terms` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        `terms_text` TEXT NOT NULL,
                        `signature_uri` TEXT NOT NULL,
                        `amount_in_words` TEXT NOT NULL,
                        `warehouse_id` TEXT NOT NULL,
                        `warehouse_name` TEXT NOT NULL,
                        `recurring_template_id` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_txn_company_id` ON `txn` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_txn_doc_type` ON `txn` (`doc_type`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_txn_doc_number` ON `txn` (`doc_number`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_txn_party_id` ON `txn` (`party_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_txn_txn_date` ON `txn` (`txn_date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_txn_status` ON `txn` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_txn_is_deleted` ON `txn` (`is_deleted`)")

                // 2. txn_line
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `txn_line` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `txn_id` TEXT NOT NULL,
                        `line_index` INTEGER NOT NULL,
                        `item_id` TEXT NOT NULL,
                        `item_name` TEXT NOT NULL,
                        `item_code` TEXT NOT NULL,
                        `hsn_sac_code` TEXT NOT NULL,
                        `batch_id` TEXT NOT NULL,
                        `batch_number` TEXT NOT NULL,
                        `serial_numbers_csv` TEXT NOT NULL,
                        `qty` INTEGER NOT NULL,
                        `free_qty` INTEGER NOT NULL,
                        `unit` TEXT NOT NULL,
                        `unit_price_paise` INTEGER NOT NULL,
                        `purchase_cost_paise` INTEGER NOT NULL,
                        `tax_inclusive` INTEGER NOT NULL,
                        `discount_bps` INTEGER NOT NULL,
                        `discount_paise` INTEGER NOT NULL,
                        `tax_category` TEXT NOT NULL,
                        `tax_rate_bps` INTEGER NOT NULL,
                        `cess_bps` INTEGER NOT NULL,
                        `taxable_paise` INTEGER NOT NULL,
                        `cgst_paise` INTEGER NOT NULL,
                        `sgst_paise` INTEGER NOT NULL,
                        `igst_paise` INTEGER NOT NULL,
                        `cess_paise` INTEGER NOT NULL,
                        `line_total_paise` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_txn_line_company_id` ON `txn_line` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_txn_line_txn_id` ON `txn_line` (`txn_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_txn_line_item_id` ON `txn_line` (`item_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_txn_line_hsn_sac_code` ON `txn_line` (`hsn_sac_code`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_txn_line_is_deleted` ON `txn_line` (`is_deleted`)")

                // 3. txn_charge
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `txn_charge` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `txn_id` TEXT NOT NULL,
                        `label` TEXT NOT NULL,
                        `amount_paise` INTEGER NOT NULL,
                        `tax_rate_bps` INTEGER NOT NULL,
                        `cgst_paise` INTEGER NOT NULL,
                        `sgst_paise` INTEGER NOT NULL,
                        `igst_paise` INTEGER NOT NULL,
                        `total_charge_paise` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_txn_charge_company_id` ON `txn_charge` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_txn_charge_txn_id` ON `txn_charge` (`txn_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_txn_charge_is_deleted` ON `txn_charge` (`is_deleted`)")

                // 4. txn_tax_line
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `txn_tax_line` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `txn_id` TEXT NOT NULL,
                        `hsn_sac_code` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `uqc` TEXT NOT NULL,
                        `total_qty` INTEGER NOT NULL,
                        `taxable_paise` INTEGER NOT NULL,
                        `tax_rate_bps` INTEGER NOT NULL,
                        `cgst_paise` INTEGER NOT NULL,
                        `sgst_paise` INTEGER NOT NULL,
                        `igst_paise` INTEGER NOT NULL,
                        `cess_paise` INTEGER NOT NULL,
                        `total_tax_paise` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_txn_tax_line_company_id` ON `txn_tax_line` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_txn_tax_line_txn_id` ON `txn_tax_line` (`txn_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_txn_tax_line_hsn_sac_code` ON `txn_tax_line` (`hsn_sac_code`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_txn_tax_line_is_deleted` ON `txn_tax_line` (`is_deleted`)")

                // 5. txn_link
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `txn_link` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `source_txn_id` TEXT NOT NULL,
                        `source_doc_type` TEXT NOT NULL,
                        `source_doc_number` TEXT NOT NULL,
                        `target_txn_id` TEXT NOT NULL,
                        `target_doc_type` TEXT NOT NULL,
                        `target_doc_number` TEXT NOT NULL,
                        `relationship` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_txn_link_company_id` ON `txn_link` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_txn_link_source_txn_id` ON `txn_link` (`source_txn_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_txn_link_target_txn_id` ON `txn_link` (`target_txn_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_txn_link_is_deleted` ON `txn_link` (`is_deleted`)")

                // 6. payment
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `payment` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `payment_number` TEXT NOT NULL,
                        `payment_type` TEXT NOT NULL,
                        `payment_date` INTEGER NOT NULL,
                        `party_id` TEXT NOT NULL,
                        `party_name` TEXT NOT NULL,
                        `total_amount_paise` INTEGER NOT NULL,
                        `discount_paise` INTEGER NOT NULL,
                        `unallocated_advance_paise` INTEGER NOT NULL,
                        `primary_mode` TEXT NOT NULL,
                        `modes_split_json` TEXT NOT NULL,
                        `reference_no` TEXT NOT NULL,
                        `cheque_id` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_company_id` ON `payment` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_party_id` ON `payment` (`party_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_payment_date` ON `payment` (`payment_date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_status` ON `payment` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_is_deleted` ON `payment` (`is_deleted`)")

                // 7. payment_allocation
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `payment_allocation` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `payment_id` TEXT NOT NULL,
                        `txn_id` TEXT NOT NULL,
                        `txn_doc_number` TEXT NOT NULL,
                        `allocated_amount_paise` INTEGER NOT NULL,
                        `discount_allowed_paise` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_allocation_company_id` ON `payment_allocation` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_allocation_payment_id` ON `payment_allocation` (`payment_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_allocation_txn_id` ON `payment_allocation` (`txn_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_allocation_is_deleted` ON `payment_allocation` (`is_deleted`)")

                // 8. cheque
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `cheque` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `payment_id` TEXT NOT NULL,
                        `party_id` TEXT NOT NULL,
                        `party_name` TEXT NOT NULL,
                        `cheque_type` TEXT NOT NULL,
                        `cheque_number` TEXT NOT NULL,
                        `bank_name` TEXT NOT NULL,
                        `ifsc_code` TEXT NOT NULL,
                        `cheque_date` INTEGER NOT NULL,
                        `due_date` INTEGER NOT NULL,
                        `amount_paise` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `deposited_date` INTEGER NOT NULL,
                        `cleared_or_bounced_date` INTEGER NOT NULL,
                        `bounce_charges_paise` INTEGER NOT NULL,
                        `bounce_reason` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_cheque_company_id` ON `cheque` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_cheque_payment_id` ON `cheque` (`payment_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_cheque_party_id` ON `cheque` (`party_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_cheque_status` ON `cheque` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_cheque_due_date` ON `cheque` (`due_date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_cheque_is_deleted` ON `cheque` (`is_deleted`)")

                // 9. recurring_template
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `recurring_template` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `template_title` TEXT NOT NULL,
                        `doc_type` TEXT NOT NULL,
                        `party_id` TEXT NOT NULL,
                        `party_name` TEXT NOT NULL,
                        `party_gstin` TEXT NOT NULL,
                        `place_of_supply_state_code` TEXT NOT NULL,
                        `place_of_supply_state_name` TEXT NOT NULL,
                        `frequency` TEXT NOT NULL,
                        `interval_days` INTEGER NOT NULL,
                        `next_run_date` INTEGER NOT NULL,
                        `last_generated_txn_id` TEXT NOT NULL,
                        `default_item_id` TEXT NOT NULL,
                        `default_item_name` TEXT NOT NULL,
                        `default_hsn_code` TEXT NOT NULL,
                        `default_qty` INTEGER NOT NULL,
                        `default_unit` TEXT NOT NULL,
                        `default_rate_paise` INTEGER NOT NULL,
                        `default_tax_rate_bps` INTEGER NOT NULL,
                        `estimated_grand_total_paise` INTEGER NOT NULL,
                        `auto_notify` INTEGER NOT NULL,
                        `is_active` INTEGER NOT NULL,
                        `notes` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_template_company_id` ON `recurring_template` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_template_party_id` ON `recurring_template` (`party_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_template_next_run_date` ON `recurring_template` (`next_run_date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_template_is_active` ON `recurring_template` (`is_active`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_template_is_deleted` ON `recurring_template` (`is_deleted`)")
            }
        }

        /**
         * Explicit non-destructive Room migration from v1 to v2.
         * Preserves all 10 existing v1 tables and creates the 19 Step 2 Parties and Items/Stock tables.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. party
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `party` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `party_type` TEXT NOT NULL,
                        `phone` TEXT NOT NULL,
                        `alt_phone` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `gstin` TEXT NOT NULL,
                        `pan` TEXT NOT NULL,
                        `state_code` TEXT NOT NULL,
                        `state_name` TEXT NOT NULL,
                        `group_id` TEXT NOT NULL,
                        `group_name` TEXT NOT NULL,
                        `opening_balance_paise` INTEGER NOT NULL,
                        `is_to_collect` INTEGER NOT NULL,
                        `current_balance_paise` INTEGER NOT NULL,
                        `credit_limit_paise` INTEGER NOT NULL,
                        `credit_days` INTEGER NOT NULL,
                        `price_list_id` TEXT NOT NULL,
                        `price_list_name` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        `birthday` TEXT NOT NULL,
                        `anniversary` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_party_company_id` ON `party` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_party_party_type` ON `party` (`party_type`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_party_group_id` ON `party` (`group_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_party_gstin` ON `party` (`gstin`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_party_is_deleted` ON `party` (`is_deleted`)")

                // 2. party_address
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `party_address` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `party_id` TEXT NOT NULL,
                        `address_type` TEXT NOT NULL,
                        `label` TEXT NOT NULL,
                        `line1` TEXT NOT NULL,
                        `city` TEXT NOT NULL,
                        `state_code` TEXT NOT NULL,
                        `state_name` TEXT NOT NULL,
                        `pincode` TEXT NOT NULL,
                        `is_default` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_party_address_company_id` ON `party_address` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_party_address_party_id` ON `party_address` (`party_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_party_address_is_deleted` ON `party_address` (`is_deleted`)")

                // 3. party_group
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `party_group` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `discount_bps` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_party_group_company_id` ON `party_group` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_party_group_is_deleted` ON `party_group` (`is_deleted`)")

                // 4. party_ledger_entry
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `party_ledger_entry` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `party_id` TEXT NOT NULL,
                        `entry_date` INTEGER NOT NULL,
                        `voucher_type` TEXT NOT NULL,
                        `voucher_number` TEXT NOT NULL,
                        `debit_paise` INTEGER NOT NULL,
                        `credit_paise` INTEGER NOT NULL,
                        `running_balance_paise` INTEGER NOT NULL,
                        `due_date` INTEGER NOT NULL,
                        `is_settled` INTEGER NOT NULL,
                        `notes` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_party_ledger_entry_company_id` ON `party_ledger_entry` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_party_ledger_entry_party_id` ON `party_ledger_entry` (`party_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_party_ledger_entry_entry_date` ON `party_ledger_entry` (`entry_date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_party_ledger_entry_is_deleted` ON `party_ledger_entry` (`is_deleted`)")

                // 5. party_custom_field
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `party_custom_field` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `party_id` TEXT NOT NULL,
                        `field_key` TEXT NOT NULL,
                        `field_value` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_party_custom_field_company_id` ON `party_custom_field` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_party_custom_field_party_id` ON `party_custom_field` (`party_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_party_custom_field_is_deleted` ON `party_custom_field` (`is_deleted`)")

                // 6. item
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `item` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `item_type` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `item_code` TEXT NOT NULL,
                        `hsn_sac_code` TEXT NOT NULL,
                        `category_id` TEXT NOT NULL,
                        `category_name` TEXT NOT NULL,
                        `brand` TEXT NOT NULL,
                        `primary_unit` TEXT NOT NULL,
                        `secondary_unit` TEXT NOT NULL,
                        `conversion_rate` INTEGER NOT NULL,
                        `sale_price_paise` INTEGER NOT NULL,
                        `purchase_price_paise` INTEGER NOT NULL,
                        `wholesale_price_paise` INTEGER NOT NULL,
                        `wholesale_min_qty` INTEGER NOT NULL,
                        `mrp_paise` INTEGER NOT NULL,
                        `tax_rate_bps` INTEGER NOT NULL,
                        `tax_inclusive` INTEGER NOT NULL,
                        `cess_bps` INTEGER NOT NULL,
                        `opening_stock_qty` INTEGER NOT NULL,
                        `opening_stock_date` INTEGER NOT NULL,
                        `current_stock_qty` INTEGER NOT NULL,
                        `min_stock_alert_qty` INTEGER NOT NULL,
                        `rack_location` TEXT NOT NULL,
                        `image_uri` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `barcode` TEXT NOT NULL,
                        `track_batch` INTEGER NOT NULL,
                        `track_serial` INTEGER NOT NULL,
                        `warehouse_id` TEXT NOT NULL,
                        `warehouse_name` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_company_id` ON `item` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_category_id` ON `item` (`category_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_item_code` ON `item` (`item_code`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_barcode` ON `item` (`barcode`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_hsn_sac_code` ON `item` (`hsn_sac_code`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_is_deleted` ON `item` (`is_deleted`)")

                // 7. item_category
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `item_category` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_category_company_id` ON `item_category` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_category_is_deleted` ON `item_category` (`is_deleted`)")

                // 8. item_unit
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `item_unit` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `short_name` TEXT NOT NULL,
                        `full_name` TEXT NOT NULL,
                        `uqc_code` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_unit_company_id` ON `item_unit` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_unit_short_name` ON `item_unit` (`short_name`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_unit_is_deleted` ON `item_unit` (`is_deleted`)")

                // 9. unit_conversion
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `unit_conversion` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `base_unit` TEXT NOT NULL,
                        `secondary_unit` TEXT NOT NULL,
                        `multiplier` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_unit_conversion_company_id` ON `unit_conversion` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_unit_conversion_is_deleted` ON `unit_conversion` (`is_deleted`)")

                // 10. item_batch
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `item_batch` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `item_id` TEXT NOT NULL,
                        `item_name` TEXT NOT NULL,
                        `warehouse_id` TEXT NOT NULL,
                        `warehouse_name` TEXT NOT NULL,
                        `batch_number` TEXT NOT NULL,
                        `mfg_date` INTEGER NOT NULL,
                        `expiry_date` INTEGER NOT NULL,
                        `opening_qty` INTEGER NOT NULL,
                        `available_qty` INTEGER NOT NULL,
                        `purchase_price_paise` INTEGER NOT NULL,
                        `sale_price_paise` INTEGER NOT NULL,
                        `mrp_paise` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_batch_company_id` ON `item_batch` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_batch_item_id` ON `item_batch` (`item_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_batch_warehouse_id` ON `item_batch` (`warehouse_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_batch_expiry_date` ON `item_batch` (`expiry_date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_batch_is_deleted` ON `item_batch` (`is_deleted`)")

                // 11. item_serial
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `item_serial` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `item_id` TEXT NOT NULL,
                        `batch_id` TEXT NOT NULL,
                        `warehouse_id` TEXT NOT NULL,
                        `serial_or_imei` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `warranty_months` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_serial_company_id` ON `item_serial` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_serial_item_id` ON `item_serial` (`item_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_serial_serial_or_imei` ON `item_serial` (`serial_or_imei`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_serial_is_deleted` ON `item_serial` (`is_deleted`)")

                // 12. item_variant
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `item_variant` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `item_id` TEXT NOT NULL,
                        `variant_name` TEXT NOT NULL,
                        `sku_code` TEXT NOT NULL,
                        `barcode` TEXT NOT NULL,
                        `sale_price_paise` INTEGER NOT NULL,
                        `purchase_price_paise` INTEGER NOT NULL,
                        `stock_qty` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_variant_company_id` ON `item_variant` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_variant_item_id` ON `item_variant` (`item_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_variant_is_deleted` ON `item_variant` (`is_deleted`)")

                // 13. warehouse
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `warehouse` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `code` TEXT NOT NULL,
                        `address` TEXT NOT NULL,
                        `incharge_name` TEXT NOT NULL,
                        `phone` TEXT NOT NULL,
                        `is_primary` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_warehouse_company_id` ON `warehouse` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_warehouse_is_deleted` ON `warehouse` (`is_deleted`)")

                // 14. stock_ledger
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `stock_ledger` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `item_id` TEXT NOT NULL,
                        `item_name` TEXT NOT NULL,
                        `warehouse_id` TEXT NOT NULL,
                        `warehouse_name` TEXT NOT NULL,
                        `batch_id` TEXT NOT NULL,
                        `movement_type` TEXT NOT NULL,
                        `voucher_number` TEXT NOT NULL,
                        `qty_delta` INTEGER NOT NULL,
                        `closing_qty` INTEGER NOT NULL,
                        `unit_rate_paise` INTEGER NOT NULL,
                        `movement_date` INTEGER NOT NULL,
                        `remarks` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_ledger_company_id` ON `stock_ledger` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_ledger_item_id` ON `stock_ledger` (`item_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_ledger_warehouse_id` ON `stock_ledger` (`warehouse_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_ledger_movement_date` ON `stock_ledger` (`movement_date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_ledger_is_deleted` ON `stock_ledger` (`is_deleted`)")

                // 15. stock_adjustment
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `stock_adjustment` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `item_id` TEXT NOT NULL,
                        `item_name` TEXT NOT NULL,
                        `warehouse_id` TEXT NOT NULL,
                        `warehouse_name` TEXT NOT NULL,
                        `adjustment_type` TEXT NOT NULL,
                        `qty` INTEGER NOT NULL,
                        `reason` TEXT NOT NULL,
                        `unit_cost_paise` INTEGER NOT NULL,
                        `adjustment_date` INTEGER NOT NULL,
                        `notes` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_adjustment_company_id` ON `stock_adjustment` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_adjustment_item_id` ON `stock_adjustment` (`item_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_adjustment_warehouse_id` ON `stock_adjustment` (`warehouse_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_adjustment_is_deleted` ON `stock_adjustment` (`is_deleted`)")

                // 16. stock_transfer
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `stock_transfer` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `transfer_number` TEXT NOT NULL,
                        `item_id` TEXT NOT NULL,
                        `item_name` TEXT NOT NULL,
                        `from_warehouse_id` TEXT NOT NULL,
                        `from_warehouse_name` TEXT NOT NULL,
                        `to_warehouse_id` TEXT NOT NULL,
                        `to_warehouse_name` TEXT NOT NULL,
                        `qty` INTEGER NOT NULL,
                        `transfer_date` INTEGER NOT NULL,
                        `notes` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_transfer_company_id` ON `stock_transfer` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_transfer_item_id` ON `stock_transfer` (`item_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_transfer_is_deleted` ON `stock_transfer` (`is_deleted`)")

                // 17. item_bundle
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `item_bundle` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `parent_item_id` TEXT NOT NULL,
                        `bundle_name` TEXT NOT NULL,
                        `component_item_id` TEXT NOT NULL,
                        `component_item_name` TEXT NOT NULL,
                        `component_qty` INTEGER NOT NULL,
                        `bundle_sale_price_paise` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_bundle_company_id` ON `item_bundle` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_bundle_parent_item_id` ON `item_bundle` (`parent_item_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_item_bundle_is_deleted` ON `item_bundle` (`is_deleted`)")

                // 18. price_list
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `price_list` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `default_discount_bps` INTEGER NOT NULL,
                        `is_default` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_price_list_company_id` ON `price_list` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_price_list_is_deleted` ON `price_list` (`is_deleted`)")

                // 19. price_list_item
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `price_list_item` (
                        `id` TEXT NOT NULL,
                        `company_id` TEXT NOT NULL,
                        `price_list_id` TEXT NOT NULL,
                        `item_id` TEXT NOT NULL,
                        `custom_sale_price_paise` INTEGER NOT NULL,
                        `min_order_qty` INTEGER NOT NULL,
                        `discount_bps` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        `is_deleted` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_price_list_item_company_id` ON `price_list_item` (`company_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_price_list_item_price_list_id` ON `price_list_item` (`price_list_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_price_list_item_item_id` ON `price_list_item` (`item_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_price_list_item_is_deleted` ON `price_list_item` (`is_deleted`)")
            }
        }
    }
}
