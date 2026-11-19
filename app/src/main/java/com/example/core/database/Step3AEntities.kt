package com.example.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// ============================================================================
// STEP 3A: SALES, BILLING, GST TAX LINES, DOCUMENT LINKS, PAYMENTS, CHEQUES
//          AND RECURRING INVOICE TABLES
// Tables (9):
//   1. txn
//   2. txn_line
//   3. txn_charge
//   4. txn_tax_line
//   5. txn_link
//   6. payment
//   7. payment_allocation
//   8. cheque
//   9. recurring_template
// All monetary values are integer paise (Long) — never floating point.
// ============================================================================

@Entity(
    tableName = "txn",
    indices = [
        Index("company_id"),
        Index("doc_type"),
        Index("doc_number"),
        Index("party_id"),
        Index("txn_date"),
        Index("status"),
        Index("is_deleted")
    ]
)
data class TxnEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "doc_type") val docType: String,
    @ColumnInfo(name = "doc_number") val docNumber: String,
    @ColumnInfo(name = "txn_date") val txnDate: Long,
    @ColumnInfo(name = "due_date") val dueDate: Long = 0L,
    @ColumnInfo(name = "party_id") val partyId: String = "",
    @ColumnInfo(name = "party_name") val partyName: String = "",
    @ColumnInfo(name = "party_gstin") val partyGstin: String = "",
    @ColumnInfo(name = "party_phone") val partyPhone: String = "",
    @ColumnInfo(name = "billing_address") val billingAddress: String = "",
    @ColumnInfo(name = "shipping_address") val shippingAddress: String = "",
    @ColumnInfo(name = "place_of_supply_state_code") val placeOfSupplyStateCode: String = "27",
    @ColumnInfo(name = "place_of_supply_state_name") val placeOfSupplyStateName: String = "Maharashtra",
    @ColumnInfo(name = "supply_type") val supplyType: String = "INTRA_STATE",
    @ColumnInfo(name = "classification") val classification: String = "B2B",
    @ColumnInfo(name = "is_reverse_charge") val isReverseCharge: Boolean = false,
    @ColumnInfo(name = "is_composition") val isComposition: Boolean = false,
    @ColumnInfo(name = "gross_subtotal_paise") val grossSubtotalPaise: Long = 0L,
    @ColumnInfo(name = "line_discount_total_paise") val lineDiscountTotalPaise: Long = 0L,
    @ColumnInfo(name = "invoice_discount_bps") val invoiceDiscountBps: Int = 0,
    @ColumnInfo(name = "invoice_discount_paise") val invoiceDiscountPaise: Long = 0L,
    @ColumnInfo(name = "taxable_subtotal_paise") val taxableSubtotalPaise: Long = 0L,
    @ColumnInfo(name = "cgst_total_paise") val cgstTotalPaise: Long = 0L,
    @ColumnInfo(name = "sgst_total_paise") val sgstTotalPaise: Long = 0L,
    @ColumnInfo(name = "igst_total_paise") val igstTotalPaise: Long = 0L,
    @ColumnInfo(name = "cess_total_paise") val cessTotalPaise: Long = 0L,
    @ColumnInfo(name = "total_tax_paise") val totalTaxPaise: Long = 0L,
    @ColumnInfo(name = "additional_charges_total_paise") val additionalChargesTotalPaise: Long = 0L,
    @ColumnInfo(name = "tcs_bps") val tcsBps: Int = 0,
    @ColumnInfo(name = "tcs_paise") val tcsPaise: Long = 0L,
    @ColumnInfo(name = "tds_bps") val tdsBps: Int = 0,
    @ColumnInfo(name = "tds_paise") val tdsPaise: Long = 0L,
    @ColumnInfo(name = "round_off_paise") val roundOffPaise: Long = 0L,
    @ColumnInfo(name = "rounding_mode") val roundingMode: String = "NEAREST",
    @ColumnInfo(name = "grand_total_paise") val grandTotalPaise: Long = 0L,
    @ColumnInfo(name = "paid_amount_paise") val paidAmountPaise: Long = 0L,
    @ColumnInfo(name = "balance_amount_paise") val balanceAmountPaise: Long = 0L,
    @ColumnInfo(name = "total_cost_paise") val totalCostPaise: Long = 0L,
    @ColumnInfo(name = "gross_profit_paise") val grossProfitPaise: Long = 0L,
    @ColumnInfo(name = "status") val status: String = "Unpaid",
    @ColumnInfo(name = "cancel_reason") val cancelReason: String = "",
    @ColumnInfo(name = "payment_terms") val paymentTerms: String = "15 Days Credit",
    @ColumnInfo(name = "notes") val notes: String = "",
    @ColumnInfo(name = "terms_text") val termsText: String = "",
    @ColumnInfo(name = "signature_uri") val signatureUri: String = "",
    @ColumnInfo(name = "amount_in_words") val amountInWords: String = "",
    @ColumnInfo(name = "warehouse_id") val warehouseId: String = "",
    @ColumnInfo(name = "warehouse_name") val warehouseName: String = "Main Godown",
    @ColumnInfo(name = "recurring_template_id") val recurringTemplateId: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "txn_line",
    indices = [
        Index("company_id"),
        Index("txn_id"),
        Index("item_id"),
        Index("hsn_sac_code"),
        Index("is_deleted")
    ]
)
data class TxnLineEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "txn_id") val txnId: String,
    @ColumnInfo(name = "line_index") val lineIndex: Int = 0,
    @ColumnInfo(name = "item_id") val itemId: String = "",
    @ColumnInfo(name = "item_name") val itemName: String,
    @ColumnInfo(name = "item_code") val itemCode: String = "",
    @ColumnInfo(name = "hsn_sac_code") val hsnSacCode: String = "",
    @ColumnInfo(name = "batch_id") val batchId: String = "",
    @ColumnInfo(name = "batch_number") val batchNumber: String = "",
    @ColumnInfo(name = "serial_numbers_csv") val serialNumbersCsv: String = "",
    @ColumnInfo(name = "qty") val qty: Long,
    @ColumnInfo(name = "free_qty") val freeQty: Long = 0L,
    @ColumnInfo(name = "unit") val unit: String = "PCS",
    @ColumnInfo(name = "unit_price_paise") val unitPricePaise: Long,
    @ColumnInfo(name = "purchase_cost_paise") val purchaseCostPaise: Long = 0L,
    @ColumnInfo(name = "tax_inclusive") val taxInclusive: Boolean = false,
    @ColumnInfo(name = "discount_bps") val discountBps: Int = 0,
    @ColumnInfo(name = "discount_paise") val discountPaise: Long = 0L,
    @ColumnInfo(name = "tax_category") val taxCategory: String = "TAXABLE",
    @ColumnInfo(name = "tax_rate_bps") val taxRateBps: Int = 1800,
    @ColumnInfo(name = "cess_bps") val cessBps: Int = 0,
    @ColumnInfo(name = "taxable_paise") val taxablePaise: Long = 0L,
    @ColumnInfo(name = "cgst_paise") val cgstPaise: Long = 0L,
    @ColumnInfo(name = "sgst_paise") val sgstPaise: Long = 0L,
    @ColumnInfo(name = "igst_paise") val igstPaise: Long = 0L,
    @ColumnInfo(name = "cess_paise") val cessPaise: Long = 0L,
    @ColumnInfo(name = "line_total_paise") val lineTotalPaise: Long = 0L,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "txn_charge",
    indices = [
        Index("company_id"),
        Index("txn_id"),
        Index("is_deleted")
    ]
)
data class TxnChargeEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "txn_id") val txnId: String,
    @ColumnInfo(name = "label") val label: String,
    @ColumnInfo(name = "amount_paise") val amountPaise: Long,
    @ColumnInfo(name = "tax_rate_bps") val taxRateBps: Int = 0,
    @ColumnInfo(name = "cgst_paise") val cgstPaise: Long = 0L,
    @ColumnInfo(name = "sgst_paise") val sgstPaise: Long = 0L,
    @ColumnInfo(name = "igst_paise") val igstPaise: Long = 0L,
    @ColumnInfo(name = "total_charge_paise") val totalChargePaise: Long = 0L,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "txn_tax_line",
    indices = [
        Index("company_id"),
        Index("txn_id"),
        Index("hsn_sac_code"),
        Index("is_deleted")
    ]
)
data class TxnTaxLineEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "txn_id") val txnId: String,
    @ColumnInfo(name = "hsn_sac_code") val hsnSacCode: String,
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "uqc") val uqc: String = "PCS",
    @ColumnInfo(name = "total_qty") val totalQty: Long = 0L,
    @ColumnInfo(name = "taxable_paise") val taxablePaise: Long = 0L,
    @ColumnInfo(name = "tax_rate_bps") val taxRateBps: Int = 1800,
    @ColumnInfo(name = "cgst_paise") val cgstPaise: Long = 0L,
    @ColumnInfo(name = "sgst_paise") val sgstPaise: Long = 0L,
    @ColumnInfo(name = "igst_paise") val igstPaise: Long = 0L,
    @ColumnInfo(name = "cess_paise") val cessPaise: Long = 0L,
    @ColumnInfo(name = "total_tax_paise") val totalTaxPaise: Long = 0L,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "txn_link",
    indices = [
        Index("company_id"),
        Index("source_txn_id"),
        Index("target_txn_id"),
        Index("is_deleted")
    ]
)
data class TxnLinkEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "source_txn_id") val sourceTxnId: String,
    @ColumnInfo(name = "source_doc_type") val sourceDocType: String,
    @ColumnInfo(name = "source_doc_number") val sourceDocNumber: String,
    @ColumnInfo(name = "target_txn_id") val targetTxnId: String,
    @ColumnInfo(name = "target_doc_type") val targetDocType: String,
    @ColumnInfo(name = "target_doc_number") val targetDocNumber: String,
    @ColumnInfo(name = "relationship") val relationship: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "payment",
    indices = [
        Index("company_id"),
        Index("party_id"),
        Index("payment_date"),
        Index("status"),
        Index("is_deleted")
    ]
)
data class PaymentEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "payment_number") val paymentNumber: String,
    @ColumnInfo(name = "payment_type") val paymentType: String = "PAYMENT_IN",
    @ColumnInfo(name = "payment_date") val paymentDate: Long,
    @ColumnInfo(name = "party_id") val partyId: String,
    @ColumnInfo(name = "party_name") val partyName: String,
    @ColumnInfo(name = "total_amount_paise") val totalAmountPaise: Long,
    @ColumnInfo(name = "discount_paise") val discountPaise: Long = 0L,
    @ColumnInfo(name = "unallocated_advance_paise") val unallocatedAdvancePaise: Long = 0L,
    @ColumnInfo(name = "primary_mode") val primaryMode: String = "UPI",
    @ColumnInfo(name = "modes_split_json") val modesSplitJson: String = "",
    @ColumnInfo(name = "reference_no") val referenceNo: String = "",
    @ColumnInfo(name = "cheque_id") val chequeId: String = "",
    @ColumnInfo(name = "status") val status: String = "Paid",
    @ColumnInfo(name = "notes") val notes: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "payment_allocation",
    indices = [
        Index("company_id"),
        Index("payment_id"),
        Index("txn_id"),
        Index("is_deleted")
    ]
)
data class PaymentAllocationEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "payment_id") val paymentId: String,
    @ColumnInfo(name = "txn_id") val txnId: String,
    @ColumnInfo(name = "txn_doc_number") val txnDocNumber: String,
    @ColumnInfo(name = "allocated_amount_paise") val allocatedAmountPaise: Long,
    @ColumnInfo(name = "discount_allowed_paise") val discountAllowedPaise: Long = 0L,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "cheque",
    indices = [
        Index("company_id"),
        Index("payment_id"),
        Index("party_id"),
        Index("status"),
        Index("due_date"),
        Index("is_deleted")
    ]
)
data class ChequeEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "payment_id") val paymentId: String = "",
    @ColumnInfo(name = "party_id") val partyId: String,
    @ColumnInfo(name = "party_name") val partyName: String,
    @ColumnInfo(name = "cheque_type") val chequeType: String = "RECEIVABLE",
    @ColumnInfo(name = "cheque_number") val chequeNumber: String,
    @ColumnInfo(name = "bank_name") val bankName: String,
    @ColumnInfo(name = "ifsc_code") val ifscCode: String = "",
    @ColumnInfo(name = "cheque_date") val chequeDate: Long,
    @ColumnInfo(name = "due_date") val dueDate: Long,
    @ColumnInfo(name = "amount_paise") val amountPaise: Long,
    @ColumnInfo(name = "status") val status: String = "IN_HAND",
    @ColumnInfo(name = "deposited_date") val depositedDate: Long = 0L,
    @ColumnInfo(name = "cleared_or_bounced_date") val clearedOrBouncedDate: Long = 0L,
    @ColumnInfo(name = "bounce_charges_paise") val bounceChargesPaise: Long = 0L,
    @ColumnInfo(name = "bounce_reason") val bounceReason: String = "",
    @ColumnInfo(name = "notes") val notes: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "recurring_template",
    indices = [
        Index("company_id"),
        Index("party_id"),
        Index("next_run_date"),
        Index("is_active"),
        Index("is_deleted")
    ]
)
data class RecurringTemplateEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "template_title") val templateTitle: String,
    @ColumnInfo(name = "doc_type") val docType: String = "TAX_INVOICE",
    @ColumnInfo(name = "party_id") val partyId: String = "",
    @ColumnInfo(name = "party_name") val partyName: String = "",
    @ColumnInfo(name = "party_gstin") val partyGstin: String = "",
    @ColumnInfo(name = "place_of_supply_state_code") val placeOfSupplyStateCode: String = "27",
    @ColumnInfo(name = "place_of_supply_state_name") val placeOfSupplyStateName: String = "Maharashtra",
    @ColumnInfo(name = "frequency") val frequency: String = "MONTHLY",
    @ColumnInfo(name = "interval_days") val intervalDays: Int = 30,
    @ColumnInfo(name = "next_run_date") val nextRunDate: Long,
    @ColumnInfo(name = "last_generated_txn_id") val lastGeneratedTxnId: String = "",
    @ColumnInfo(name = "default_item_id") val defaultItemId: String = "",
    @ColumnInfo(name = "default_item_name") val defaultItemName: String = "",
    @ColumnInfo(name = "default_hsn_code") val defaultHsnCode: String = "",
    @ColumnInfo(name = "default_qty") val defaultQty: Long = 1L,
    @ColumnInfo(name = "default_unit") val defaultUnit: String = "PCS",
    @ColumnInfo(name = "default_rate_paise") val defaultRatePaise: Long = 0L,
    @ColumnInfo(name = "default_tax_rate_bps") val defaultTaxRateBps: Int = 1800,
    @ColumnInfo(name = "estimated_grand_total_paise") val estimatedGrandTotalPaise: Long = 0L,
    @ColumnInfo(name = "auto_notify") val autoNotify: Boolean = true,
    @ColumnInfo(name = "is_active") val isActive: Boolean = true,
    @ColumnInfo(name = "notes") val notes: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)
