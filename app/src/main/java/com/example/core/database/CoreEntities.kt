package com.example.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "company",
    indices = [Index("company_id"), Index("is_deleted")]
)
data class CompanyEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "business_name") val businessName: String,
    @ColumnInfo(name = "legal_name") val legalName: String = "",
    @ColumnInfo(name = "logo_uri") val logoUri: String = "",
    @ColumnInfo(name = "address") val address: String = "",
    @ColumnInfo(name = "phone") val phone: String = "",
    @ColumnInfo(name = "email") val email: String = "",
    @ColumnInfo(name = "gstin") val gstin: String = "",
    @ColumnInfo(name = "pan") val pan: String = "",
    @ColumnInfo(name = "state_code") val stateCode: String = "27",
    @ColumnInfo(name = "state_name") val stateName: String = "Maharashtra",
    @ColumnInfo(name = "business_type") val businessType: String = "Retail & Wholesale",
    @ColumnInfo(name = "gst_mode") val gstMode: String = "REGULAR_GST", // REGULAR_GST, COMPOSITION, NON_GST
    @ColumnInfo(name = "fy_start_month") val fyStartMonth: Int = 4, // April
    @ColumnInfo(name = "signature_uri") val signatureUri: String = "",
    @ColumnInfo(name = "upi_id") val upiId: String = "",
    @ColumnInfo(name = "bank_name") val bankName: String = "",
    @ColumnInfo(name = "bank_account_no") val bankAccountNo: String = "",
    @ColumnInfo(name = "bank_ifsc") val bankIfsc: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "app_user",
    indices = [Index("company_id"), Index("role")]
)
data class AppUserEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "full_name") val fullName: String,
    @ColumnInfo(name = "role") val role: String, // OWNER, MANAGER, SALESPERSON, ACCOUNTANT, VIEWER
    @ColumnInfo(name = "pin_hash") val pinHash: String,
    @ColumnInfo(name = "pin_salt") val pinSalt: String,
    @ColumnInfo(name = "is_active") val isActive: Boolean = true,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "role_permission",
    indices = [Index("company_id"), Index(value = ["company_id", "role", "module"], unique = true)]
)
data class RolePermissionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "role") val role: String,
    @ColumnInfo(name = "module") val module: String,
    @ColumnInfo(name = "can_view") val canView: Boolean,
    @ColumnInfo(name = "can_create") val canCreate: Boolean,
    @ColumnInfo(name = "can_edit") val canEdit: Boolean,
    @ColumnInfo(name = "can_delete") val canDelete: Boolean,
    @ColumnInfo(name = "can_export") val canExport: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "setting",
    indices = [Index("company_id"), Index(value = ["company_id", "setting_key"], unique = true)]
)
data class SettingEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "setting_key") val settingKey: String,
    @ColumnInfo(name = "setting_value") val settingValue: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "number_series",
    indices = [Index("company_id"), Index(value = ["company_id", "doc_type", "fy_code"], unique = true)]
)
data class NumberSeriesEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "doc_type") val docType: String, // SALE_INVOICE, PURCHASE_BILL, ESTIMATE, PAYMENT_IN, PAYMENT_OUT, PAYSLIP
    @ColumnInfo(name = "prefix") val prefix: String,
    @ColumnInfo(name = "fy_code") val fyCode: String, // e.g., "2026-27"
    @ColumnInfo(name = "next_number") val nextNumber: Long = 1L,
    @ColumnInfo(name = "padding_digits") val paddingDigits: Int = 4,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "audit_log",
    indices = [Index("company_id"), Index("created_at")]
)
data class AuditLogEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "user_name") val userName: String,
    @ColumnInfo(name = "action") val action: String,
    @ColumnInfo(name = "entity_type") val entityType: String,
    @ColumnInfo(name = "entity_id") val entityId: String,
    @ColumnInfo(name = "summary") val summary: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "attachment",
    indices = [Index("company_id"), Index("owner_record_id")]
)
data class AttachmentEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "owner_record_type") val ownerRecordType: String,
    @ColumnInfo(name = "owner_record_id") val ownerRecordId: String,
    @ColumnInfo(name = "file_name") val fileName: String,
    @ColumnInfo(name = "mime_type") val mimeType: String,
    @ColumnInfo(name = "local_path") val localPath: String,
    @ColumnInfo(name = "size_bytes") val sizeBytes: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "financial_year",
    indices = [Index("company_id"), Index("fy_code")]
)
data class FinancialYearEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "fy_code") val fyCode: String, // e.g., "FY 2026-27"
    @ColumnInfo(name = "start_epoch_ms") val startEpochMs: Long,
    @ColumnInfo(name = "end_epoch_ms") val endEpochMs: Long,
    @ColumnInfo(name = "is_current") val isCurrent: Boolean = true,
    @ColumnInfo(name = "is_locked") val isLocked: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

/**
 * Editable GST tax slab table.
 * Rates are stored in basis points (1% = 100 bps) so 0.25% = 25 bps, 18% = 1800 bps, 40% = 4000 bps.
 */
@Entity(
    tableName = "tax_rate",
    indices = [Index("company_id")]
)
data class TaxRateEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "label") val label: String, // e.g., "GST 18%"
    @ColumnInfo(name = "rate_bps") val rateBps: Int, // 0, 25, 300, 500, 1200, 1800, 2800, 4000
    @ColumnInfo(name = "cgst_bps") val cgstBps: Int,
    @ColumnInfo(name = "sgst_bps") val sgstBps: Int,
    @ColumnInfo(name = "igst_bps") val igstBps: Int,
    @ColumnInfo(name = "cess_bps") val cessBps: Int = 0,
    @ColumnInfo(name = "is_active") val isActive: Boolean = true,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)

@Entity(
    tableName = "hsn_master",
    indices = [Index("company_id"), Index("hsn_code")]
)
data class HsnMasterEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "company_id") val companyId: String,
    @ColumnInfo(name = "hsn_code") val hsnCode: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "default_tax_rate_bps") val defaultTaxRateBps: Int,
    @ColumnInfo(name = "is_service") val isService: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false
)
