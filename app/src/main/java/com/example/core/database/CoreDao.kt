package com.example.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CoreDao {

    // Companies
    @Query("SELECT * FROM company WHERE is_deleted = 0 ORDER BY created_at ASC")
    fun observeCompanies(): Flow<List<CompanyEntity>>

    @Query("SELECT * FROM company WHERE is_deleted = 0 ORDER BY created_at ASC")
    suspend fun getCompaniesNow(): List<CompanyEntity>

    @Query("SELECT * FROM company WHERE id = :companyId AND is_deleted = 0 LIMIT 1")
    fun observeCompanyById(companyId: String): Flow<CompanyEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCompany(company: CompanyEntity)

    // Users
    @Query("SELECT * FROM app_user WHERE company_id = :companyId AND is_deleted = 0 ORDER BY created_at ASC")
    fun observeUsers(companyId: String): Flow<List<AppUserEntity>>

    @Query("SELECT * FROM app_user WHERE company_id = :companyId AND is_deleted = 0 ORDER BY created_at ASC")
    suspend fun getUsersNow(companyId: String): List<AppUserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: AppUserEntity)

    // Role Permissions
    @Query("SELECT * FROM role_permission WHERE company_id = :companyId AND is_deleted = 0")
    fun observeRolePermissions(companyId: String): Flow<List<RolePermissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRolePermissions(permissions: List<RolePermissionEntity>)

    @Update
    suspend fun updateRolePermission(permission: RolePermissionEntity)

    // Settings
    @Query("SELECT * FROM setting WHERE company_id = :companyId AND is_deleted = 0")
    fun observeSettings(companyId: String): Flow<List<SettingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSetting(setting: SettingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: List<SettingEntity>)

    // Number Series
    @Query("SELECT * FROM number_series WHERE company_id = :companyId AND is_deleted = 0 ORDER BY doc_type ASC")
    fun observeNumberSeries(companyId: String): Flow<List<NumberSeriesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNumberSeries(series: List<NumberSeriesEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSingleNumberSeries(series: NumberSeriesEntity)

    // Financial Years
    @Query("SELECT * FROM financial_year WHERE company_id = :companyId AND is_deleted = 0 ORDER BY start_epoch_ms DESC")
    fun observeFinancialYears(companyId: String): Flow<List<FinancialYearEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFinancialYear(fy: FinancialYearEntity)

    // Tax Rates
    @Query("SELECT * FROM tax_rate WHERE company_id = :companyId AND is_deleted = 0 ORDER BY rate_bps ASC")
    fun observeTaxRates(companyId: String): Flow<List<TaxRateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTaxRates(rates: List<TaxRateEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSingleTaxRate(rate: TaxRateEntity)

    // HSN Master
    @Query("SELECT * FROM hsn_master WHERE company_id = :companyId AND is_deleted = 0 ORDER BY hsn_code ASC")
    fun observeHsnMaster(companyId: String): Flow<List<HsnMasterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHsnList(items: List<HsnMasterEntity>)

    // Audit Logs
    @Query("SELECT * FROM audit_log WHERE company_id = :companyId AND is_deleted = 0 ORDER BY created_at DESC LIMIT 50")
    fun observeRecentAuditLogs(companyId: String): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // Attachments
    @Query("SELECT * FROM attachment WHERE company_id = :companyId AND is_deleted = 0 ORDER BY created_at DESC")
    fun observeAttachments(companyId: String): Flow<List<AttachmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: AttachmentEntity)

    // Reset all tables
    @Query("DELETE FROM company")
    suspend fun clearCompanies()

    @Query("DELETE FROM app_user")
    suspend fun clearUsers()

    @Query("DELETE FROM role_permission")
    suspend fun clearRolePermissions()

    @Query("DELETE FROM setting")
    suspend fun clearSettings()

    @Query("DELETE FROM number_series")
    suspend fun clearNumberSeries()

    @Query("DELETE FROM audit_log")
    suspend fun clearAuditLogs()

    @Query("DELETE FROM attachment")
    suspend fun clearAttachments()

    @Query("DELETE FROM financial_year")
    suspend fun clearFinancialYears()

    @Query("DELETE FROM tax_rate")
    suspend fun clearTaxRates()

    @Query("DELETE FROM hsn_master")
    suspend fun clearHsnMaster()
}
