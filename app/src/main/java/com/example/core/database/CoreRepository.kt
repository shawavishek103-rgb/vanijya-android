package com.example.core.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.model.AppRole
import com.example.core.model.BundledHsnDirectoryLoader
import com.example.core.model.GstAdditionalChargeInput
import com.example.core.model.GstCalculationEngine
import com.example.core.model.GstDocumentInput
import com.example.core.model.GstLineInput
import com.example.core.model.GstTaxCategory
import com.example.core.model.IndianNumberToWords
import com.example.core.model.ItemImportPreviewRow
import com.example.core.model.Money
import com.example.core.model.PartyImportPreviewRow
import com.example.core.model.RecurringInvoiceNotificationHelper
import com.example.core.model.RolePermissionDefaults
import com.example.core.model.SalesDocTypes
import com.example.core.security.PinHasher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.vanijyaDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "vanijya_settings_datastore"
)

enum class ThemeMode(val label: String) {
    SYSTEM("System default"),
    LIGHT("Light"),
    DARK("Dark"),
    AMOLED("AMOLED Black")
}

enum class AutoLockTimeout(val label: String, val timeoutMs: Long) {
    IMMEDIATELY("Immediately", 0L),
    ONE_MINUTE("1 min", 60_000L),
    FIVE_MINUTES("5 min", 300_000L)
}

enum class RoundingModeOption(val label: String) {
    NEAREST("Nearest"),
    UP("Up"),
    DOWN("Down")
}

data class AppPreferencesState(
    val activeCompanyId: String = "",
    val activeUserId: String = "",
    val activeUserName: String = "Owner",
    val activeUserRole: AppRole = AppRole.OWNER,
    val languageCode: String = "en",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val autoLockTimeout: AutoLockTimeout = AutoLockTimeout.ONE_MINUTE,
    val hideAmounts: Boolean = false,
    val blockScreenshots: Boolean = false,
    val biometricEnabled: Boolean = true,
    val invoicePrefix: String = "INV/26-27/",
    val invoiceStartingNumber: Long = 1L,
    val fyStartMonth: Int = 4,
    val roundingMode: RoundingModeOption = RoundingModeOption.NEAREST,
    val roundingNearestRupee: Boolean = true,
    val decimalPlaces: Int = 2,
    val dateFormatPattern: String = "dd MMM yyyy",
    val onboardingCompleted: Boolean = false
)

@Singleton
class CoreRepository @Inject constructor(
    private val coreDao: CoreDao,
    @param:ApplicationContext private val context: Context,
    private val step2Dao: Step2Dao? = null,
    private val step3ADao: Step3ADao? = null
) {
    private val prefs = context.getSharedPreferences("vanijya_app_prefs", Context.MODE_PRIVATE)
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private object Keys {
        val ACTIVE_COMPANY_ID = stringPreferencesKey("active_company_id")
        val ACTIVE_USER_ID = stringPreferencesKey("active_user_id")
        val ACTIVE_USER_NAME = stringPreferencesKey("active_user_name")
        val ACTIVE_USER_ROLE = stringPreferencesKey("active_user_role")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val AUTO_LOCK_TIMEOUT = stringPreferencesKey("auto_lock_timeout")
        val HIDE_AMOUNTS = booleanPreferencesKey("hide_amounts")
        val BLOCK_SCREENSHOTS = booleanPreferencesKey("block_screenshots")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val INVOICE_PREFIX = stringPreferencesKey("invoice_prefix")
        val INVOICE_STARTING_NO = longPreferencesKey("invoice_starting_no")
        val FY_START_MONTH = intPreferencesKey("fy_start_month")
        val ROUNDING_MODE = stringPreferencesKey("rounding_mode")
        val DECIMAL_PLACES = intPreferencesKey("decimal_places")
        val DATE_FORMAT = stringPreferencesKey("date_format_pattern")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    private val _appPrefs = MutableStateFlow(loadPrefsFromDisk())
    val appPrefs: StateFlow<AppPreferencesState> = _appPrefs.asStateFlow()

    init {
        repoScope.launch {
            val activeId = _appPrefs.value.activeCompanyId
            if (activeId.isNotBlank()) {
                ensureStep2MasterDataSeeded(activeId)
                ensureStep3ADataSeeded(activeId)
            }
            runCatching {
                context.vanijyaDataStore.data.collectLatest { ds ->
                    if (ds.asMap().isNotEmpty()) {
                        val current = _appPrefs.value
                        val roleStr = ds[Keys.ACTIVE_USER_ROLE] ?: current.activeUserRole.name
                        val themeStr = ds[Keys.THEME_MODE] ?: current.themeMode.name
                        val lockStr = ds[Keys.AUTO_LOCK_TIMEOUT] ?: current.autoLockTimeout.name
                        val roundStr = ds[Keys.ROUNDING_MODE] ?: current.roundingMode.name
                        val roundingMode = runCatching { RoundingModeOption.valueOf(roundStr) }
                            .getOrDefault(current.roundingMode)
                        val nextCompanyId = ds[Keys.ACTIVE_COMPANY_ID] ?: current.activeCompanyId
                        _appPrefs.value = current.copy(
                            activeCompanyId = nextCompanyId,
                            activeUserId = ds[Keys.ACTIVE_USER_ID] ?: current.activeUserId,
                            activeUserName = ds[Keys.ACTIVE_USER_NAME] ?: current.activeUserName,
                            activeUserRole = runCatching { AppRole.valueOf(roleStr) }.getOrDefault(current.activeUserRole),
                            languageCode = ds[Keys.LANGUAGE_CODE] ?: current.languageCode,
                            themeMode = runCatching { ThemeMode.valueOf(themeStr) }.getOrDefault(current.themeMode),
                            autoLockTimeout = runCatching { AutoLockTimeout.valueOf(lockStr) }.getOrDefault(current.autoLockTimeout),
                            hideAmounts = ds[Keys.HIDE_AMOUNTS] ?: current.hideAmounts,
                            blockScreenshots = ds[Keys.BLOCK_SCREENSHOTS] ?: current.blockScreenshots,
                            biometricEnabled = ds[Keys.BIOMETRIC_ENABLED] ?: current.biometricEnabled,
                            invoicePrefix = ds[Keys.INVOICE_PREFIX] ?: current.invoicePrefix,
                            invoiceStartingNumber = ds[Keys.INVOICE_STARTING_NO] ?: current.invoiceStartingNumber,
                            fyStartMonth = ds[Keys.FY_START_MONTH] ?: current.fyStartMonth,
                            roundingMode = roundingMode,
                            roundingNearestRupee = roundingMode == RoundingModeOption.NEAREST,
                            decimalPlaces = ds[Keys.DECIMAL_PLACES] ?: current.decimalPlaces,
                            dateFormatPattern = ds[Keys.DATE_FORMAT] ?: current.dateFormatPattern,
                            onboardingCompleted = ds[Keys.ONBOARDING_COMPLETED] ?: current.onboardingCompleted
                        )
                        if (nextCompanyId.isNotBlank()) {
                            ensureStep2MasterDataSeeded(nextCompanyId)
                            ensureStep3ADataSeeded(nextCompanyId)
                        }
                    }
                }
            }
        }
    }

    private fun loadPrefsFromDisk(): AppPreferencesState {
        val roleStr = prefs.getString("active_user_role", AppRole.OWNER.name) ?: AppRole.OWNER.name
        val themeStr = prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val lockStr = prefs.getString("auto_lock_timeout", AutoLockTimeout.ONE_MINUTE.name) ?: AutoLockTimeout.ONE_MINUTE.name
        val roundStr = prefs.getString("rounding_mode", RoundingModeOption.NEAREST.name) ?: RoundingModeOption.NEAREST.name
        val roundingMode = runCatching { RoundingModeOption.valueOf(roundStr) }.getOrDefault(RoundingModeOption.NEAREST)
        return AppPreferencesState(
            activeCompanyId = prefs.getString("active_company_id", "") ?: "",
            activeUserId = prefs.getString("active_user_id", "") ?: "",
            activeUserName = prefs.getString("active_user_name", "Owner") ?: "Owner",
            activeUserRole = runCatching { AppRole.valueOf(roleStr) }.getOrDefault(AppRole.OWNER),
            languageCode = prefs.getString("language_code", "en") ?: "en",
            themeMode = runCatching { ThemeMode.valueOf(themeStr) }.getOrDefault(ThemeMode.SYSTEM),
            autoLockTimeout = runCatching { AutoLockTimeout.valueOf(lockStr) }.getOrDefault(AutoLockTimeout.ONE_MINUTE),
            hideAmounts = prefs.getBoolean("hide_amounts", false),
            blockScreenshots = prefs.getBoolean("block_screenshots", false),
            biometricEnabled = prefs.getBoolean("biometric_enabled", true),
            invoicePrefix = prefs.getString("invoice_prefix", "INV/26-27/") ?: "INV/26-27/",
            invoiceStartingNumber = prefs.getLong("invoice_starting_no", 1L),
            fyStartMonth = prefs.getInt("fy_start_month", 4),
            roundingMode = roundingMode,
            roundingNearestRupee = roundingMode == RoundingModeOption.NEAREST,
            decimalPlaces = prefs.getInt("decimal_places", 2),
            dateFormatPattern = prefs.getString("date_format_pattern", "dd MMM yyyy") ?: "dd MMM yyyy",
            onboardingCompleted = prefs.getBoolean("onboarding_completed", false)
        )
    }

    // Step 1 Flows
    fun observeCompanies(): Flow<List<CompanyEntity>> = coreDao.observeCompanies()
    fun observeUsers(companyId: String): Flow<List<AppUserEntity>> = coreDao.observeUsers(companyId)
    fun observeRolePermissions(companyId: String): Flow<List<RolePermissionEntity>> = coreDao.observeRolePermissions(companyId)
    fun observeTaxRates(companyId: String): Flow<List<TaxRateEntity>> = coreDao.observeTaxRates(companyId)
    fun observeNumberSeries(companyId: String): Flow<List<NumberSeriesEntity>> = coreDao.observeNumberSeries(companyId)
    fun observeHsnMaster(companyId: String): Flow<List<HsnMasterEntity>> = coreDao.observeHsnMaster(companyId)
    fun observeRecentAuditLogs(companyId: String): Flow<List<AuditLogEntity>> = coreDao.observeRecentAuditLogs(companyId)
    fun observeFinancialYears(companyId: String): Flow<List<FinancialYearEntity>> = coreDao.observeFinancialYears(companyId)

    // Step 2 Flows — Parties
    fun observeParties(companyId: String): Flow<List<PartyEntity>> =
        step2Dao?.observeParties(companyId) ?: emptyFlow()

    fun observePartyAddresses(companyId: String): Flow<List<PartyAddressEntity>> =
        step2Dao?.observePartyAddresses(companyId) ?: emptyFlow()

    fun observePartyGroups(companyId: String): Flow<List<PartyGroupEntity>> =
        step2Dao?.observePartyGroups(companyId) ?: emptyFlow()

    fun observePartyCustomFields(companyId: String): Flow<List<PartyCustomFieldEntity>> =
        step2Dao?.observePartyCustomFields(companyId) ?: emptyFlow()

    fun observeAllPartyLedgerEntries(companyId: String): Flow<List<PartyLedgerEntryEntity>> =
        step2Dao?.observeAllPartyLedgerEntries(companyId) ?: emptyFlow()

    // Step 2 Flows — Items & Stock
    fun observeItems(companyId: String): Flow<List<ItemEntity>> =
        step2Dao?.observeItems(companyId) ?: emptyFlow()

    fun observeItemCategories(companyId: String): Flow<List<ItemCategoryEntity>> =
        step2Dao?.observeItemCategories(companyId) ?: emptyFlow()

    fun observeItemUnits(companyId: String): Flow<List<ItemUnitEntity>> =
        step2Dao?.observeItemUnits(companyId) ?: emptyFlow()

    fun observeUnitConversions(companyId: String): Flow<List<UnitConversionEntity>> =
        step2Dao?.observeUnitConversions(companyId) ?: emptyFlow()

    fun observeItemBatches(companyId: String): Flow<List<ItemBatchEntity>> =
        step2Dao?.observeItemBatches(companyId) ?: emptyFlow()

    fun observeItemSerials(companyId: String): Flow<List<ItemSerialEntity>> =
        step2Dao?.observeItemSerials(companyId) ?: emptyFlow()

    fun observeItemVariants(companyId: String): Flow<List<ItemVariantEntity>> =
        step2Dao?.observeItemVariants(companyId) ?: emptyFlow()

    fun observeWarehouses(companyId: String): Flow<List<WarehouseEntity>> =
        step2Dao?.observeWarehouses(companyId) ?: emptyFlow()

    fun observeStockLedger(companyId: String): Flow<List<StockLedgerEntity>> =
        step2Dao?.observeStockLedger(companyId) ?: emptyFlow()

    fun observeStockAdjustments(companyId: String): Flow<List<StockAdjustmentEntity>> =
        step2Dao?.observeStockAdjustments(companyId) ?: emptyFlow()

    fun observeStockTransfers(companyId: String): Flow<List<StockTransferEntity>> =
        step2Dao?.observeStockTransfers(companyId) ?: emptyFlow()

    fun observeItemBundles(companyId: String): Flow<List<ItemBundleEntity>> =
        step2Dao?.observeItemBundles(companyId) ?: emptyFlow()

    fun observePriceLists(companyId: String): Flow<List<PriceListEntity>> =
        step2Dao?.observePriceLists(companyId) ?: emptyFlow()

    fun observePriceListItems(companyId: String): Flow<List<PriceListItemEntity>> =
        step2Dao?.observePriceListItems(companyId) ?: emptyFlow()

    // Step 3A Flows — GST Sales Documents, Links, Payments, Allocations, Cheques & Recurring Templates
    fun observeTransactions(companyId: String): Flow<List<TxnEntity>> =
        step3ADao?.observeTransactions(companyId) ?: emptyFlow()

    fun observeTxnLines(companyId: String): Flow<List<TxnLineEntity>> =
        step3ADao?.observeAllTxnLines(companyId) ?: emptyFlow()

    fun observeTxnCharges(companyId: String): Flow<List<TxnChargeEntity>> =
        step3ADao?.observeAllTxnCharges(companyId) ?: emptyFlow()

    fun observeTxnTaxLines(companyId: String): Flow<List<TxnTaxLineEntity>> =
        step3ADao?.observeAllTxnTaxLines(companyId) ?: emptyFlow()

    fun observeTxnLinks(companyId: String): Flow<List<TxnLinkEntity>> =
        step3ADao?.observeTxnLinks(companyId) ?: emptyFlow()

    fun observePayments(companyId: String): Flow<List<PaymentEntity>> =
        step3ADao?.observePayments(companyId) ?: emptyFlow()

    fun observePaymentAllocations(companyId: String): Flow<List<PaymentAllocationEntity>> =
        step3ADao?.observeAllPaymentAllocations(companyId) ?: emptyFlow()

    fun observeCheques(companyId: String): Flow<List<ChequeEntity>> =
        step3ADao?.observeCheques(companyId) ?: emptyFlow()

    fun observeRecurringTemplates(companyId: String): Flow<List<RecurringTemplateEntity>> =
        step3ADao?.observeRecurringTemplates(companyId) ?: emptyFlow()

    fun updatePreferences(transform: (AppPreferencesState) -> AppPreferencesState) {
        val updated = transform(_appPrefs.value)
        prefs.edit()
            .putString("active_company_id", updated.activeCompanyId)
            .putString("active_user_id", updated.activeUserId)
            .putString("active_user_name", updated.activeUserName)
            .putString("active_user_role", updated.activeUserRole.name)
            .putString("language_code", updated.languageCode)
            .putString("theme_mode", updated.themeMode.name)
            .putString("auto_lock_timeout", updated.autoLockTimeout.name)
            .putBoolean("hide_amounts", updated.hideAmounts)
            .putBoolean("block_screenshots", updated.blockScreenshots)
            .putBoolean("biometric_enabled", updated.biometricEnabled)
            .putString("invoice_prefix", updated.invoicePrefix)
            .putLong("invoice_starting_no", updated.invoiceStartingNumber)
            .putInt("fy_start_month", updated.fyStartMonth)
            .putString("rounding_mode", updated.roundingMode.name)
            .putBoolean("rounding_nearest_rupee", updated.roundingNearestRupee)
            .putInt("decimal_places", updated.decimalPlaces)
            .putString("date_format_pattern", updated.dateFormatPattern)
            .putBoolean("onboarding_completed", updated.onboardingCompleted)
            .apply()
        _appPrefs.value = updated

        repoScope.launch {
            runCatching {
                context.vanijyaDataStore.edit { ds ->
                    ds[Keys.ACTIVE_COMPANY_ID] = updated.activeCompanyId
                    ds[Keys.ACTIVE_USER_ID] = updated.activeUserId
                    ds[Keys.ACTIVE_USER_NAME] = updated.activeUserName
                    ds[Keys.ACTIVE_USER_ROLE] = updated.activeUserRole.name
                    ds[Keys.LANGUAGE_CODE] = updated.languageCode
                    ds[Keys.THEME_MODE] = updated.themeMode.name
                    ds[Keys.AUTO_LOCK_TIMEOUT] = updated.autoLockTimeout.name
                    ds[Keys.HIDE_AMOUNTS] = updated.hideAmounts
                    ds[Keys.BLOCK_SCREENSHOTS] = updated.blockScreenshots
                    ds[Keys.BIOMETRIC_ENABLED] = updated.biometricEnabled
                    ds[Keys.INVOICE_PREFIX] = updated.invoicePrefix
                    ds[Keys.INVOICE_STARTING_NO] = updated.invoiceStartingNumber
                    ds[Keys.FY_START_MONTH] = updated.fyStartMonth
                    ds[Keys.ROUNDING_MODE] = updated.roundingMode.name
                    ds[Keys.DECIMAL_PLACES] = updated.decimalPlaces
                    ds[Keys.DATE_FORMAT] = updated.dateFormatPattern
                    ds[Keys.ONBOARDING_COMPLETED] = updated.onboardingCompleted
                }
            }
        }
    }

    suspend fun createBusinessWithDefaults(
        businessName: String,
        legalName: String,
        address: String,
        phone: String,
        email: String,
        gstin: String,
        pan: String,
        stateCode: String,
        stateName: String,
        businessType: String,
        gstMode: String,
        fyStartMonth: Int,
        upiId: String,
        bankName: String,
        bankAccountNo: String,
        bankIfsc: String,
        logoUri: String,
        signatureUri: String,
        ownerPin: String,
        biometricEnabled: Boolean,
        languageCode: String
    ): CompanyEntity {
        val now = System.currentTimeMillis()
        val companyId = UUID.randomUUID().toString()

        val company = CompanyEntity(
            id = companyId,
            companyId = companyId,
            businessName = businessName.ifBlank { "Shree Ganesh Traders" },
            legalName = legalName.ifBlank { businessName.ifBlank { "Shree Ganesh Traders" } },
            logoUri = logoUri,
            address = address,
            phone = phone,
            email = email,
            gstin = gstin.trim().uppercase(),
            pan = pan.trim().uppercase(),
            stateCode = stateCode,
            stateName = stateName,
            businessType = businessType,
            gstMode = gstMode,
            fyStartMonth = fyStartMonth,
            signatureUri = signatureUri,
            upiId = upiId.trim(),
            bankName = bankName.trim(),
            bankAccountNo = bankAccountNo.trim(),
            bankIfsc = bankIfsc.trim().uppercase(),
            createdAt = now,
            updatedAt = now,
            isDeleted = false
        )
        coreDao.upsertCompany(company)

        val ownerSalted = PinHasher.hashPin(ownerPin.ifBlank { "123456" })
        val ownerUser = AppUserEntity(
            id = UUID.randomUUID().toString(),
            companyId = companyId,
            fullName = "Business Owner",
            role = AppRole.OWNER.name,
            pinHash = ownerSalted.hashHex,
            pinSalt = ownerSalted.saltHex,
            isActive = true,
            createdAt = now,
            updatedAt = now
        )
        coreDao.upsertUser(ownerUser)

        val defaultRolePins = listOf(
            Triple("Store Manager", AppRole.MANAGER, "222222"),
            Triple("Sales Counter", AppRole.SALESPERSON, "333333"),
            Triple("Senior Accountant", AppRole.ACCOUNTANT, "444444"),
            Triple("Audit Viewer", AppRole.VIEWER, "555555")
        )
        defaultRolePins.forEach { (name, role, pin) ->
            val salted = PinHasher.hashPin(pin)
            coreDao.upsertUser(
                AppUserEntity(
                    id = UUID.randomUUID().toString(),
                    companyId = companyId,
                    fullName = name,
                    role = role.name,
                    pinHash = salted.hashHex,
                    pinSalt = salted.saltHex,
                    isActive = true,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }

        val permissionEntities = AppRole.entries.flatMap { role ->
            RolePermissionDefaults.defaultPermissionsForRole(role).map { perm ->
                RolePermissionEntity(
                    id = UUID.randomUUID().toString(),
                    companyId = companyId,
                    role = perm.role.name,
                    module = perm.module.name,
                    canView = perm.canView,
                    canCreate = perm.canCreate,
                    canEdit = perm.canEdit,
                    canDelete = perm.canDelete,
                    canExport = perm.canExport,
                    createdAt = now,
                    updatedAt = now
                )
            }
        }
        coreDao.upsertRolePermissions(permissionEntities)

        val slabs = listOf(
            Triple("GST 0% (Exempt / Nil)", 0, 0),
            Triple("GST 0.25% (Rough Diamonds)", 25, 0),
            Triple("GST 3% (Gold / Bullion)", 300, 0),
            Triple("GST 5% (Essential Goods)", 500, 0),
            Triple("GST 12% (Standard I)", 1200, 0),
            Triple("GST 18% (Standard II)", 1800, 0),
            Triple("GST 28% (Luxury Goods)", 2800, 1200),
            Triple("GST 40% (Peak Slab)", 4000, 0)
        ).map { (label, rateBps, cessBps) ->
            TaxRateEntity(
                id = UUID.randomUUID().toString(),
                companyId = companyId,
                label = label,
                rateBps = rateBps,
                cgstBps = rateBps / 2,
                sgstBps = rateBps / 2,
                igstBps = rateBps,
                cessBps = cessBps,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
        }
        coreDao.upsertTaxRates(slabs)

        val fyCode = if (fyStartMonth == 4) "FY 2026-27" else "CY 2026"
        coreDao.upsertFinancialYear(
            FinancialYearEntity(
                id = UUID.randomUUID().toString(),
                companyId = companyId,
                fyCode = fyCode,
                startEpochMs = now,
                endEpochMs = now + 365L * 24L * 3600L * 1000L,
                isCurrent = true,
                isLocked = false,
                createdAt = now,
                updatedAt = now
            )
        )

        val seriesList = listOf(
            "SALE_INVOICE" to "INV/26-27/",
            "BILL_OF_SUPPLY" to "BOS/26-27/",
            "CASH_SALE" to "CSH/26-27/",
            "PURCHASE_BILL" to "PUR/26-27/",
            "ESTIMATE" to "EST/26-27/",
            "PROFORMA" to "PRF/26-27/",
            "SALE_ORDER" to "SO/26-27/",
            "DELIVERY_CHALLAN" to "DC/26-27/",
            "CREDIT_NOTE" to "CN/26-27/",
            "DEBIT_NOTE" to "DN/26-27/",
            "PAYMENT_IN" to "RCPT/26-27/",
            "PAYMENT_OUT" to "PYMT/26-27/",
            "PAYSLIP" to "PAY/26-27/"
        ).map { (docType, prefix) ->
            NumberSeriesEntity(
                id = "${companyId}_$docType",
                companyId = companyId,
                docType = docType,
                prefix = prefix,
                fyCode = fyCode,
                nextNumber = 1L,
                paddingDigits = 4,
                createdAt = now,
                updatedAt = now
            )
        }
        coreDao.upsertNumberSeries(seriesList)

        // Load bundled offline HSN/SAC directory asset (fallback to core list if in unit test without asset stream)
        val bundledHsn = BundledHsnDirectoryLoader.loadFromAssets(context, companyId)
        val hsnItems = if (bundledHsn.isNotEmpty()) {
            bundledHsn
        } else {
            listOf(
                Triple("1006", "Basmati & Non-Basmati Rice", 500),
                Triple("1701", "Refined Sugar & Jaggery", 500),
                Triple("3004", "Medicaments & Pharmacy Supplies", 1200),
                Triple("6203", "Readymade Garments & Apparel", 1200),
                Triple("7113", "Gold & Silver Jewellery", 300),
                Triple("8471", "Computers, Laptops & Peripherals", 1800),
                Triple("8517", "Smartphones & Telecom Equipment", 1800),
                Triple("9983", "IT, Accounting & Professional Services (SAC)", 1800)
            ).map { (code, desc, bps) ->
                HsnMasterEntity(
                    id = "${companyId}_hsn_$code",
                    companyId = companyId,
                    hsnCode = code,
                    description = desc,
                    defaultTaxRateBps = bps,
                    isService = code.startsWith("99"),
                    createdAt = now,
                    updatedAt = now
                )
            }
        }
        coreDao.upsertHsnList(hsnItems)

        val defaultSettings = listOf(
            "invoice_prefix" to "INV/26-27/",
            "invoice_starting_no" to "1",
            "fy_start_month" to fyStartMonth.toString(),
            "rounding_mode" to RoundingModeOption.NEAREST.name,
            "decimal_places" to "2",
            "date_format_pattern" to "dd MMM yyyy",
            "gst_mode" to gstMode
        ).map { (k, v) ->
            SettingEntity(
                id = UUID.randomUUID().toString(),
                companyId = companyId,
                settingKey = k,
                settingValue = v,
                createdAt = now,
                updatedAt = now
            )
        }
        coreDao.upsertSettings(defaultSettings)

        if (logoUri.isNotBlank()) {
            coreDao.insertAttachment(
                AttachmentEntity(
                    id = "${companyId}_logo",
                    companyId = companyId,
                    ownerRecordType = "company_logo",
                    ownerRecordId = companyId,
                    fileName = "company_logo.png",
                    mimeType = "image/*",
                    localPath = logoUri,
                    sizeBytes = 0L,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
        if (signatureUri.isNotBlank()) {
            coreDao.insertAttachment(
                AttachmentEntity(
                    id = "${companyId}_signature",
                    companyId = companyId,
                    ownerRecordType = "company_signature",
                    ownerRecordId = companyId,
                    fileName = "company_signature.png",
                    mimeType = "image/*",
                    localPath = signatureUri,
                    sizeBytes = 0L,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }

        // Seed Step 2 Parties, Groups, Price Lists, Godowns, Units, Categories, Items, Batches & Ledgers
        ensureStep2MasterDataSeeded(companyId)
        // Seed Step 3A Sales Invoices, Quotations, Orders, Challans, Credit Notes, Payments, Cheques & Recurring Templates
        ensureStep3ADataSeeded(companyId)

        coreDao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                companyId = companyId,
                userId = ownerUser.id,
                userName = ownerUser.fullName,
                action = "COMPANY_CREATED",
                entityType = "company",
                entityId = companyId,
                summary = "Initialized encrypted company vault for ${company.businessName} ($gstMode)",
                createdAt = now,
                updatedAt = now
            )
        )

        updatePreferences {
            it.copy(
                activeCompanyId = companyId,
                activeUserId = ownerUser.id,
                activeUserName = ownerUser.fullName,
                activeUserRole = AppRole.OWNER,
                languageCode = languageCode,
                fyStartMonth = fyStartMonth,
                biometricEnabled = biometricEnabled,
                onboardingCompleted = true
            )
        }

        return company
    }

    /**
     * Seeds default Step 2 master data (Party Groups, Price Lists, Warehouses/Godowns,
     * Units, Unit Conversions, Item Categories, Parties + Ledgers, Items + Batches + Stock Ledgers)
     * if the company does not yet have any parties or items.
     */
    suspend fun ensureStep2MasterDataSeeded(companyId: String) {
        val dao = step2Dao ?: return
        if (companyId.isBlank()) return

        // Ensure bundled HSN directory is loaded
        val bundledHsn = BundledHsnDirectoryLoader.loadFromAssets(context, companyId)
        if (bundledHsn.isNotEmpty()) {
            coreDao.upsertHsnList(bundledHsn)
        }

        val existingParties = dao.getPartiesNow(companyId)
        val existingItems = dao.getItemsNow(companyId)
        if (existingParties.isNotEmpty() || existingItems.isNotEmpty()) return

        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L

        // 1. Party Groups
        val groups = listOf(
            PartyGroupEntity("${companyId}_grp_retail", companyId, "Retail Customers", "Regular store & walk-in retail buyers", 0, now, now),
            PartyGroupEntity("${companyId}_grp_wholesale", companyId, "Wholesale Buyers", "Bulk B2B dealers & sub-distributors", 300, now, now),
            PartyGroupEntity("${companyId}_grp_supplier", companyId, "FMCG & Raw Suppliers", "Primary factory & super-stockist suppliers", 0, now, now),
            PartyGroupEntity("${companyId}_grp_logistics", companyId, "Transport & Logistics", "GTA freight & local courier partners", 0, now, now)
        )
        dao.upsertPartyGroups(groups)

        // 2. Price Lists
        val priceLists = listOf(
            PriceListEntity("${companyId}_pl_retail", companyId, "Standard Retail", "Default MRP / Retail selling price", 0, true, now, now),
            PriceListEntity("${companyId}_pl_wholesale", companyId, "Wholesale Tier-1", "Bulk wholesale rate for registered B2B buyers", 500, false, now, now),
            PriceListEntity("${companyId}_pl_distributor", companyId, "Distributor VIP", "Special net rate for district stockists", 800, false, now, now)
        )
        dao.upsertPriceLists(priceLists)

        // 3. Warehouses (Godowns)
        val mainGodownId = "${companyId}_wh_main"
        val shopFloorId = "${companyId}_wh_shop"
        val coldStorageId = "${companyId}_wh_cold"
        val warehouses = listOf(
            WarehouseEntity(mainGodownId, companyId, "Main Godown", "GDN-01", "Plot 14, MIDC Industrial Area, Pune", "Ramesh Kulkarni", "9823011223", true, now, now),
            WarehouseEntity(shopFloorId, companyId, "Retail Counter", "SHP-01", "MG Road Ground Floor Showroom, Pune", "Suresh Patil", "9823044556", false, now, now),
            WarehouseEntity(coldStorageId, companyId, "Cold Storage Annex", "CLD-02", "Market Yard Phase 2, Pune", "Mahesh Deshmukh", "9823077889", false, now, now)
        )
        dao.upsertWarehouses(warehouses)

        // 4. Units & Conversions (1 BOX = 12 PCS, 1 BAG = 25 KG, 1 CASE = 24 LTR)
        val units = listOf(
            ItemUnitEntity("${companyId}_u_pcs", companyId, "PCS", "Pieces", "PCS-PIECES", now, now),
            ItemUnitEntity("${companyId}_u_box", companyId, "BOX", "Corrugated Box", "BOX-BOX", now, now),
            ItemUnitEntity("${companyId}_u_kg", companyId, "KG", "Kilograms", "KGS-KILOGRAMS", now, now),
            ItemUnitEntity("${companyId}_u_gm", companyId, "GM", "Grams", "GMS-GRAMMES", now, now),
            ItemUnitEntity("${companyId}_u_ltr", companyId, "LTR", "Litres", "LTR-LITRES", now, now),
            ItemUnitEntity("${companyId}_u_bag", companyId, "BAG", "Jute / PP Bag", "BAG-BAGS", now, now),
            ItemUnitEntity("${companyId}_u_doz", companyId, "DOZ", "Dozen (12 Pcs)", "DOZ-DOZENS", now, now)
        )
        dao.upsertItemUnits(units)

        val conversions = listOf(
            UnitConversionEntity("${companyId}_conv_box_pcs", companyId, "BOX", "PCS", 12, now, now),
            UnitConversionEntity("${companyId}_conv_bag_kg", companyId, "BAG", "KG", 25, now, now),
            UnitConversionEntity("${companyId}_conv_doz_pcs", companyId, "DOZ", "PCS", 12, now, now)
        )
        dao.upsertUnitConversions(conversions)

        // 5. Item Categories
        val catKiranaId = "${companyId}_cat_kirana"
        val catElecId = "${companyId}_cat_elec"
        val catFmcgId = "${companyId}_cat_fmcg"
        val catHardwareId = "${companyId}_cat_hw"
        val catServicesId = "${companyId}_cat_srv"
        val categories = listOf(
            ItemCategoryEntity(catKiranaId, companyId, "Kirana & Staples", "Rice, Atta, Edible Oil, Sugar & Spices", now, now),
            ItemCategoryEntity(catElecId, companyId, "Electricals & Appliances", "LED Bulbs, Fans, Wires, Inverters & POS Hardware", now, now),
            ItemCategoryEntity(catFmcgId, companyId, "FMCG & Dairy", "Packaged Snacks, Ghee, Beverages & Personal Care", now, now),
            ItemCategoryEntity(catHardwareId, companyId, "Hardware & Packaging", "Adhesives, Fasteners, Cartons & Tools", now, now),
            ItemCategoryEntity(catServicesId, companyId, "Services & AMC", "Installation, Delivery & Maintenance Services", now, now)
        )
        dao.upsertItemCategories(categories)

        // 6. Seed Parties with valid 15-char GSTINs, Addresses, Custom Fields, and Multi-Bucket Aging Ledger Entries
        data class SeedPartySpec(
            val idSuffix: String,
            val name: String,
            val type: String,
            val phone: String,
            val email: String,
            val gstin: String,
            val pan: String,
            val stateCode: String,
            val stateName: String,
            val groupId: String,
            val groupName: String,
            val openingPaise: Long,
            val isToCollect: Boolean,
            val creditLimitPaise: Long,
            val creditDays: Int,
            val city: String,
            val addressLine: String,
            val ageDaysAgo: Int
        )

        val partySpecs = listOf(
            SeedPartySpec(
                "mehta", "Mehta Kirana & Traders", "CUSTOMER", "9876501234", "mehtakirana@gmail.com",
                "27AAPFM3456D1Z9", "AAPFM3456D", "27", "Maharashtra",
                "${companyId}_grp_wholesale", "Wholesale Buyers",
                64_500_00L, true, 75_000_00L, 15, "Pune", "Shop 12, Market Yard Wholesale Mandi", 18
            ),
            SeedPartySpec(
                "balaji", "Shree Balaji Distributors", "CUSTOMER", "9823098765", "accounts@balajidist.in",
                "27AABCS9876G1Z3", "AABCS9876G", "27", "Maharashtra",
                "${companyId}_grp_wholesale", "Wholesale Buyers",
                42_800_00L, true, 40_000_00L, 30, "Mumbai", "Gala 4, Bhiwandi Logistics Park", 45
            ),
            SeedPartySpec(
                "patel", "Patel Hardware Mart", "BOTH", "9765432109", "patelhardware@yahoo.in",
                "27AAJFP4321F1Z8", "AAJFP4321F", "27", "Maharashtra",
                "${companyId}_grp_retail", "Retail Customers",
                28_200_00L, true, 50_000_00L, 30, "Nashik", "44 Panchavati Main Road", 72
            ),
            SeedPartySpec(
                "kaveri", "Kaveri Supermart", "CUSTOMER", "9811223344", "Purchase@kaverimart.in",
                "27AADCK9012C1Z9", "AADCK9012C", "27", "Maharashtra",
                "${companyId}_grp_retail", "Retail Customers",
                18_900_00L, true, 50_000_00L, 15, "Nagpur", "Plot 9, Sitabuldi Commercial Complex", 98
            ),
            SeedPartySpec(
                "hindustan", "Hindustan Agro Supply Co.", "SUPPLIER", "9820445566", "billing@hindustanagro.co.in",
                "27AABCH5678B1Z2", "AABCH5678B", "27", "Maharashtra",
                "${companyId}_grp_supplier", "FMCG & Raw Suppliers",
                51_000_00L, false, 2_00_000_00L, 45, "Kolhapur", "APMC Grain Hub Sector 3", 22
            ),
            SeedPartySpec(
                "apex", "Apex Packaging Industries", "SUPPLIER", "9820112233", "sales@apexpack.in",
                "27AAACA1234A1Z5", "AAACA1234A", "27", "Maharashtra",
                "${companyId}_grp_supplier", "FMCG & Raw Suppliers",
                24_600_00L, false, 1_50_000_00L, 30, "Chakan", "Phase II MIDC Chakan", 52
            ),
            SeedPartySpec(
                "narmada", "Narmada Logistics", "SUPPLIER", "9898776655", "ops@narmadalogistics.in",
                "24AABCN7890E1Z4", "AABCN7890E", "24", "Gujarat",
                "${companyId}_grp_logistics", "Transport & Logistics",
                11_900_00L, false, 1_00_000_00L, 15, "Surat", "Ring Road Transport Nagar", 12
            )
        )

        partySpecs.forEach { spec ->
            val partyId = "${companyId}_party_${spec.idSuffix}"
            val entryTimestamp = now - spec.ageDaysAgo * dayMs
            val signedBalance = if (spec.isToCollect) spec.openingPaise else -spec.openingPaise
            val party = PartyEntity(
                id = partyId,
                companyId = companyId,
                name = spec.name,
                partyType = spec.type,
                phone = spec.phone,
                altPhone = "",
                email = spec.email,
                gstin = spec.gstin,
                pan = spec.pan,
                stateCode = spec.stateCode,
                stateName = spec.stateName,
                groupId = spec.groupId,
                groupName = spec.groupName,
                openingBalancePaise = spec.openingPaise,
                isToCollect = spec.isToCollect,
                currentBalancePaise = signedBalance,
                creditLimitPaise = spec.creditLimitPaise,
                creditDays = spec.creditDays,
                priceListId = "${companyId}_pl_wholesale",
                priceListName = "Wholesale Tier-1",
                notes = "Verified B2B partner in ${spec.city}",
                birthday = "15 Aug",
                anniversary = "26 Jan",
                createdAt = entryTimestamp,
                updatedAt = now
            )
            val addresses = listOf(
                PartyAddressEntity(
                    id = "${partyId}_addr_bill",
                    companyId = companyId,
                    partyId = partyId,
                    addressType = "BILLING",
                    label = "Registered Billing Office",
                    line1 = spec.addressLine,
                    city = spec.city,
                    stateCode = spec.stateCode,
                    stateName = spec.stateName,
                    pincode = "411001",
                    isDefault = true,
                    createdAt = entryTimestamp,
                    updatedAt = now
                ),
                PartyAddressEntity(
                    id = "${partyId}_addr_ship",
                    companyId = companyId,
                    partyId = partyId,
                    addressType = "SHIPPING",
                    label = "Warehouse Delivery Gate",
                    line1 = "${spec.addressLine} (Godown Gate 2)",
                    city = spec.city,
                    stateCode = spec.stateCode,
                    stateName = spec.stateName,
                    pincode = "411001",
                    isDefault = false,
                    createdAt = entryTimestamp,
                    updatedAt = now
                )
            )
            val customFields = listOf(
                PartyCustomFieldEntity(
                    id = "${partyId}_cf_fssai",
                    companyId = companyId,
                    partyId = partyId,
                    fieldKey = "FSSAI / Trade License",
                    fieldValue = "115240${spec.phone.takeLast(6)}",
                    createdAt = entryTimestamp,
                    updatedAt = now
                ),
                PartyCustomFieldEntity(
                    id = "${partyId}_cf_transport",
                    companyId = companyId,
                    partyId = partyId,
                    fieldKey = "Preferred Transport",
                    fieldValue = "Narmada Roadlines",
                    createdAt = entryTimestamp,
                    updatedAt = now
                )
            )
            dao.savePartyWithDetailsInTransaction(party, addresses, customFields)
        }

        // 7. Seed Items, Batches (including expiring within 30 days), Variants, Serials, Bundles & Stock Ledger
        val item1Id = "${companyId}_item_basmati"
        val item2Id = "${companyId}_item_ghee"
        val item3Id = "${companyId}_item_oil"
        val item4Id = "${companyId}_item_bulb"
        val item5Id = "${companyId}_item_scanner"
        val item6Id = "${companyId}_item_carton"
        val item7Id = "${companyId}_item_amc"

        val items = listOf(
            // Normal high-value stock
            ItemEntity(
                id = item1Id,
                companyId = companyId,
                itemType = "PRODUCT",
                name = "India Gate Classic Basmati Rice (25kg Bag)",
                itemCode = "SKU-RICE-25",
                hsnSacCode = "1006",
                categoryId = catKiranaId,
                categoryName = "Kirana & Staples",
                brand = "India Gate",
                primaryUnit = "BAG",
                secondaryUnit = "KG",
                conversionRate = 25,
                salePricePaise = 2_850_00L,
                purchasePricePaise = 2_400_00L,
                wholesalePricePaise = 2_680_00L,
                wholesaleMinQty = 5L,
                mrpPaise = 3_100_00L,
                taxRateBps = 500,
                taxInclusive = false,
                cessBps = 0,
                openingStockQty = 110L,
                openingStockDate = now - 30 * dayMs,
                currentStockQty = 110L, // ₹2,64,000 cost
                minStockAlertQty = 20L,
                rackLocation = "Aisle A • Rack 01",
                barcode = "8901491101844",
                trackBatch = true,
                trackSerial = false,
                warehouseId = mainGodownId,
                warehouseName = "Main Godown",
                description = "Aged extra-long grain basmati rice in moisture-proof 25kg PP bag",
                createdAt = now - 30 * dayMs,
                updatedAt = now
            ),
            // Expiring batch item + normal stock
            ItemEntity(
                id = item2Id,
                companyId = companyId,
                itemType = "PRODUCT",
                name = "Amul Pure Cow Ghee 1L Tin",
                itemCode = "SKU-GHEE-1L",
                hsnSacCode = "0405",
                categoryId = catFmcgId,
                categoryName = "FMCG & Dairy",
                brand = "Amul",
                primaryUnit = "PCS",
                secondaryUnit = "BOX",
                conversionRate = 12,
                salePricePaise = 640_00L,
                purchasePricePaise = 560_00L,
                wholesalePricePaise = 610_00L,
                wholesaleMinQty = 12L,
                mrpPaise = 665_00L,
                taxRateBps = 1200,
                taxInclusive = true,
                cessBps = 0,
                openingStockQty = 180L,
                openingStockDate = now - 25 * dayMs,
                currentStockQty = 180L, // ₹1,00,800 cost
                minStockAlertQty = 24L,
                rackLocation = "Cold Annex • Shelf B2",
                barcode = "8901262010023",
                trackBatch = true,
                trackSerial = false,
                warehouseId = coldStorageId,
                warehouseName = "Cold Storage Annex",
                description = "Agmark Special Grade pure cow ghee 1 Litre tin pack",
                createdAt = now - 25 * dayMs,
                updatedAt = now
            ),
            // LOW STOCK #1 + EXPIRING BATCH #2
            ItemEntity(
                id = item3Id,
                companyId = companyId,
                itemType = "PRODUCT",
                name = "Fortune Sunlite Refined Sunflower Oil 15L Jar",
                itemCode = "SKU-OIL-15L",
                hsnSacCode = "1512",
                categoryId = catKiranaId,
                categoryName = "Kirana & Staples",
                brand = "Fortune",
                primaryUnit = "PCS",
                secondaryUnit = "BOX",
                conversionRate = 4,
                salePricePaise = 2_180_00L,
                purchasePricePaise = 1_950_00L,
                wholesalePricePaise = 2_090_00L,
                wholesaleMinQty = 4L,
                mrpPaise = 2_350_00L,
                taxRateBps = 500,
                taxInclusive = false,
                cessBps = 0,
                openingStockQty = 8L,
                openingStockDate = now - 20 * dayMs,
                currentStockQty = 8L, // <= minStockAlertQty (15) -> LOW STOCK! (₹15,600 cost)
                minStockAlertQty = 15L,
                rackLocation = "Aisle B • Rack 04",
                barcode = "8906007280194",
                trackBatch = true,
                trackSerial = false,
                warehouseId = mainGodownId,
                warehouseName = "Main Godown",
                description = "15 Litre commercial tin jar with Vitamin A & D",
                createdAt = now - 20 * dayMs,
                updatedAt = now
            ),
            // LOW STOCK #2 with Unit Conversion (1 BOX = 12 PCS)
            ItemEntity(
                id = item4Id,
                companyId = companyId,
                itemType = "PRODUCT",
                name = "Philips 9W AceBright LED Bulb (Cool Day Light)",
                itemCode = "SKU-LED-9W",
                hsnSacCode = "8539",
                categoryId = catElecId,
                categoryName = "Electricals & Appliances",
                brand = "Philips",
                primaryUnit = "PCS",
                secondaryUnit = "BOX",
                conversionRate = 12,
                salePricePaise = 95_00L,
                purchasePricePaise = 65_00L,
                wholesalePricePaise = 82_00L,
                wholesaleMinQty = 12L,
                mrpPaise = 130_00L,
                taxRateBps = 1200,
                taxInclusive = false,
                cessBps = 0,
                openingStockQty = 16L,
                openingStockDate = now - 15 * dayMs,
                currentStockQty = 16L, // <= minStockAlertQty (24) -> LOW STOCK! (₹1,040 cost)
                minStockAlertQty = 24L,
                rackLocation = "Counter • Bay 02",
                barcode = "8718696574119",
                trackBatch = false,
                trackSerial = false,
                warehouseId = shopFloorId,
                warehouseName = "Retail Counter",
                description = "B22 base 9W 6500K energy saver LED bulb (1 Box = 12 Pcs)",
                createdAt = now - 15 * dayMs,
                updatedAt = now
            ),
            // Serial / IMEI Tracked Item
            ItemEntity(
                id = item5Id,
                companyId = companyId,
                itemType = "PRODUCT",
                name = "TVS-E BS-i302G Wireless 2D Barcode Scanner",
                itemCode = "SKU-POS-2D",
                hsnSacCode = "8471",
                categoryId = catElecId,
                categoryName = "Electricals & Appliances",
                brand = "TVS Electronics",
                primaryUnit = "PCS",
                secondaryUnit = "BOX",
                conversionRate = 10,
                salePricePaise = 4_250_00L,
                purchasePricePaise = 3_400_00L,
                wholesalePricePaise = 3_950_00L,
                wholesaleMinQty = 3L,
                mrpPaise = 4_999_00L,
                taxRateBps = 1800,
                taxInclusive = false,
                cessBps = 0,
                openingStockQty = 30L,
                openingStockDate = now - 12 * dayMs,
                currentStockQty = 30L, // ₹1,02,000 cost
                minStockAlertQty = 5L,
                rackLocation = "Locked Cabinet • C1",
                barcode = "8901122334455",
                trackBatch = false,
                trackSerial = true,
                warehouseId = shopFloorId,
                warehouseName = "Retail Counter",
                description = "High-speed 1D/2D QR & GST e-Invoice barcode scanner with USB dongle",
                createdAt = now - 12 * dayMs,
                updatedAt = now
            ),
            // LOW STOCK #3
            ItemEntity(
                id = item6Id,
                companyId = companyId,
                itemType = "PRODUCT",
                name = "5-Ply Heavy Duty Corrugated Master Carton",
                itemCode = "SKU-BOX-5PLY",
                hsnSacCode = "4819",
                categoryId = catHardwareId,
                categoryName = "Hardware & Packaging",
                brand = "Apex Pack",
                primaryUnit = "PCS",
                secondaryUnit = "DOZ",
                conversionRate = 12,
                salePricePaise = 65_00L,
                purchasePricePaise = 46_00L,
                wholesalePricePaise = 56_00L,
                wholesaleMinQty = 50L,
                mrpPaise = 80_00L,
                taxRateBps = 1800,
                taxInclusive = false,
                cessBps = 0,
                openingStockQty = 55L,
                openingStockDate = now - 10 * dayMs,
                currentStockQty = 55L, // <= minStockAlertQty (100) -> LOW STOCK! (₹2,530 cost)
                minStockAlertQty = 100L,
                rackLocation = "Mezzanine Loft • M3",
                barcode = "8909988776655",
                trackBatch = false,
                trackSerial = false,
                warehouseId = mainGodownId,
                warehouseName = "Main Godown",
                description = "24x18x18 inch 5-ply kraft shipping box",
                createdAt = now - 10 * dayMs,
                updatedAt = now
            ),
            // Service Item (SAC 9987)
            ItemEntity(
                id = item7Id,
                companyId = companyId,
                itemType = "SERVICE",
                name = "Annual POS & Weighing Scale Calibration AMC",
                itemCode = "SRV-AMC-POS",
                hsnSacCode = "9987",
                categoryId = catServicesId,
                categoryName = "Services & AMC",
                brand = "Vanijya Care",
                primaryUnit = "PCS",
                secondaryUnit = "",
                conversionRate = 1,
                salePricePaise = 3_500_00L,
                purchasePricePaise = 0L,
                wholesalePricePaise = 3_000_00L,
                wholesaleMinQty = 1L,
                mrpPaise = 3_500_00L,
                taxRateBps = 1800,
                taxInclusive = false,
                cessBps = 0,
                openingStockQty = 0L,
                openingStockDate = now,
                currentStockQty = 0L,
                minStockAlertQty = 0L,
                rackLocation = "On-Site Service",
                barcode = "9987001122",
                trackBatch = false,
                trackSerial = false,
                warehouseId = shopFloorId,
                warehouseName = "Retail Counter",
                description = "On-site preventive maintenance & stamping service (SAC 9987)",
                createdAt = now - 5 * dayMs,
                updatedAt = now
            )
        )
        // Total seeded Stock Value = 2,64,000 + 1,00,800 + 15,600 + 1,040 + 1,02,000 + 2,530 = ₹4,85,970.00
        // Low Stock SKUs = 3 (item3, item4, item6)
        // Expiring Batches = 2 (below)

        val batches = listOf(
            // Expiring in 11 days!
            ItemBatchEntity(
                id = "${item2Id}_batch_exp1",
                companyId = companyId,
                itemId = item2Id,
                itemName = "Amul Pure Cow Ghee 1L Tin",
                warehouseId = coldStorageId,
                warehouseName = "Cold Storage Annex",
                batchNumber = "B-AMUL-0926A",
                mfgDate = now - 170 * dayMs,
                expiryDate = now + 11 * dayMs, // Expiring within 30 days
                openingQty = 40L,
                availableQty = 40L,
                purchasePricePaise = 560_00L,
                salePricePaise = 640_00L,
                mrpPaise = 665_00L,
                createdAt = now - 25 * dayMs,
                updatedAt = now
            ),
            // Fresh batch (180 days left)
            ItemBatchEntity(
                id = "${item2Id}_batch_fresh",
                companyId = companyId,
                itemId = item2Id,
                itemName = "Amul Pure Cow Ghee 1L Tin",
                warehouseId = coldStorageId,
                warehouseName = "Cold Storage Annex",
                batchNumber = "B-AMUL-1126B",
                mfgDate = now - 15 * dayMs,
                expiryDate = now + 180 * dayMs,
                openingQty = 140L,
                availableQty = 140L,
                purchasePricePaise = 560_00L,
                salePricePaise = 640_00L,
                mrpPaise = 665_00L,
                createdAt = now - 15 * dayMs,
                updatedAt = now
            ),
            // Expiring in 19 days!
            ItemBatchEntity(
                id = "${item3Id}_batch_exp2",
                companyId = companyId,
                itemId = item3Id,
                itemName = "Fortune Sunlite Refined Sunflower Oil 15L Jar",
                warehouseId = mainGodownId,
                warehouseName = "Main Godown",
                batchNumber = "B-FORT-0826X",
                mfgDate = now - 160 * dayMs,
                expiryDate = now + 19 * dayMs, // Expiring within 30 days
                openingQty = 8L,
                availableQty = 8L,
                purchasePricePaise = 1_950_00L,
                salePricePaise = 2_180_00L,
                mrpPaise = 2_350_00L,
                createdAt = now - 20 * dayMs,
                updatedAt = now
            ),
            // Long shelf-life Basmati batch
            ItemBatchEntity(
                id = "${item1Id}_batch_main",
                companyId = companyId,
                itemId = item1Id,
                itemName = "India Gate Classic Basmati Rice (25kg Bag)",
                warehouseId = mainGodownId,
                warehouseName = "Main Godown",
                batchNumber = "B-IGR-2026-Q4",
                mfgDate = now - 30 * dayMs,
                expiryDate = now + 330 * dayMs,
                openingQty = 110L,
                availableQty = 110L,
                purchasePricePaise = 2_400_00L,
                salePricePaise = 2_850_00L,
                mrpPaise = 3_100_00L,
                createdAt = now - 30 * dayMs,
                updatedAt = now
            )
        )

        val variants = listOf(
            ItemVariantEntity(
                id = "${item1Id}_var_25kg",
                companyId = companyId,
                itemId = item1Id,
                variantName = "Pack Size: 25kg Commercial Bag",
                skuCode = "SKU-RICE-25",
                barcode = "8901491101844",
                salePricePaise = 2_850_00L,
                purchasePricePaise = 2_400_00L,
                stockQty = 80L,
                createdAt = now,
                updatedAt = now
            ),
            ItemVariantEntity(
                id = "${item1Id}_var_5kg",
                companyId = companyId,
                itemId = item1Id,
                variantName = "Pack Size: 5kg Family Pouch (x5)",
                skuCode = "SKU-RICE-5X5",
                barcode = "8901491101851",
                salePricePaise = 2_950_00L,
                purchasePricePaise = 2_450_00L,
                stockQty = 30L,
                createdAt = now,
                updatedAt = now
            )
        )

        val serials = listOf(
            ItemSerialEntity("${item5Id}_sn1", companyId, item5Id, "", shopFloorId, "TVSE-2D-2026-00981", "IN_STOCK", 24, now, now),
            ItemSerialEntity("${item5Id}_sn2", companyId, item5Id, "", shopFloorId, "TVSE-2D-2026-00982", "IN_STOCK", 24, now, now),
            ItemSerialEntity("${item5Id}_sn3", companyId, item5Id, "", shopFloorId, "TVSE-2D-2026-00983", "SOLD", 24, now, now)
        )

        val bundles = listOf(
            ItemBundleEntity(
                id = "${companyId}_bundle_festive_kirana",
                companyId = companyId,
                parentItemId = item1Id,
                bundleName = "Festive Ration Combo (25kg Basmati + 2L Cow Ghee)",
                componentItemId = item2Id,
                componentItemName = "Amul Pure Cow Ghee 1L Tin",
                componentQty = 2L,
                bundleSalePricePaise = 3_990_00L,
                createdAt = now,
                updatedAt = now
            )
        )

        items.forEach { item ->
            dao.saveItemWithDetailsInTransaction(item = item)
        }
        dao.upsertItemBatches(batches)
        dao.upsertItemVariants(variants)
        dao.upsertItemSerials(serials)
        dao.upsertItemBundles(bundles)
    }

    // ========================================================================
    // STEP 2 REPOSITORY MUTATION METHODS (PARTIES & ITEMS/STOCK)
    // ========================================================================

    suspend fun savePartyWithDetails(
        party: PartyEntity,
        addresses: List<PartyAddressEntity>,
        customFields: List<PartyCustomFieldEntity>
    ) {
        val dao = step2Dao ?: return
        val now = System.currentTimeMillis()
        dao.savePartyWithDetailsInTransaction(
            party = party.copy(updatedAt = now),
            addresses = addresses,
            customFields = customFields
        )
        coreDao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                companyId = party.companyId,
                userId = _appPrefs.value.activeUserId,
                userName = _appPrefs.value.activeUserName,
                action = "PARTY_SAVED",
                entityType = "party",
                entityId = party.id,
                summary = "Saved ${party.partyType} master '${party.name}' (GSTIN: ${party.gstin.ifBlank { "URD" }})",
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun recordPartyLedgerMovement(entry: PartyLedgerEntryEntity): Long {
        val dao = step2Dao ?: return 0L
        val newBalance = dao.recordPartyLedgerMovementInTransaction(entry)
        val now = System.currentTimeMillis()
        coreDao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                companyId = entry.companyId,
                userId = _appPrefs.value.activeUserId,
                userName = _appPrefs.value.activeUserName,
                action = "PARTY_LEDGER_ENTRY",
                entityType = "party_ledger_entry",
                entityId = entry.id,
                summary = "Recorded ${entry.voucherType} (${entry.voucherNumber}) • Running Balance: ${Money(newBalance).formatIndian()}",
                createdAt = now,
                updatedAt = now
            )
        )
        return newBalance
    }

    suspend fun deleteParties(companyId: String, partyIds: List<String>) {
        val dao = step2Dao ?: return
        val now = System.currentTimeMillis()
        dao.softDeletePartiesInTransaction(partyIds, now)
        coreDao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                companyId = companyId,
                userId = _appPrefs.value.activeUserId,
                userName = _appPrefs.value.activeUserName,
                action = "PARTIES_ARCHIVED",
                entityType = "party",
                entityId = partyIds.firstOrNull() ?: "",
                summary = "Archived ${partyIds.size} party record(s)",
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun bulkImportParties(
        companyId: String,
        rows: List<PartyImportPreviewRow>,
        skipDuplicates: Boolean = true
    ): Int {
        val dao = step2Dao ?: return 0
        val now = System.currentTimeMillis()
        var imported = 0
        rows.forEach { row ->
            if (skipDuplicates && row.isDuplicate) return@forEach
            val partyId = UUID.randomUUID().toString()
            val signedPaise = if (row.isToCollect) row.openingBalance.paise else -row.openingBalance.paise
            val party = PartyEntity(
                id = partyId,
                companyId = companyId,
                name = row.name.trim(),
                partyType = row.partyType,
                phone = row.phone,
                gstin = row.gstin,
                pan = com.example.core.model.Validators.extractPanFromGstin(row.gstin) ?: "",
                stateCode = row.stateCode,
                stateName = row.stateName,
                groupId = "${companyId}_grp_wholesale",
                groupName = "Wholesale Buyers",
                openingBalancePaise = row.openingBalance.paise,
                isToCollect = row.isToCollect,
                currentBalancePaise = signedPaise,
                creditLimitPaise = row.creditLimit.paise,
                creditDays = row.creditDays,
                priceListId = "${companyId}_pl_retail",
                priceListName = "Standard Retail",
                createdAt = now,
                updatedAt = now
            )
            dao.savePartyWithDetailsInTransaction(party, emptyList(), emptyList())
            imported++
        }
        return imported
    }

    suspend fun saveItemWithDetails(
        item: ItemEntity,
        initialBatch: ItemBatchEntity? = null,
        variants: List<ItemVariantEntity> = emptyList(),
        serials: List<ItemSerialEntity> = emptyList(),
        bundles: List<ItemBundleEntity> = emptyList()
    ) {
        val dao = step2Dao ?: return
        val now = System.currentTimeMillis()
        dao.saveItemWithDetailsInTransaction(
            item = item.copy(updatedAt = now),
            initialBatch = initialBatch,
            variants = variants,
            serials = serials,
            bundles = bundles
        )
        coreDao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                companyId = item.companyId,
                userId = _appPrefs.value.activeUserId,
                userName = _appPrefs.value.activeUserName,
                action = "ITEM_SAVED",
                entityType = "item",
                entityId = item.id,
                summary = "Saved ${item.itemType} '${item.name}' (HSN: ${item.hsnSacCode.ifBlank { "-" }}, Stock: ${item.currentStockQty} ${item.primaryUnit})",
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun recordStockAdjustment(adjustment: StockAdjustmentEntity): Long {
        val dao = step2Dao ?: return 0L
        val newClosing = dao.recordStockAdjustmentInTransaction(adjustment)
        val now = System.currentTimeMillis()
        coreDao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                companyId = adjustment.companyId,
                userId = _appPrefs.value.activeUserId,
                userName = _appPrefs.value.activeUserName,
                action = "STOCK_ADJUSTED",
                entityType = "stock_adjustment",
                entityId = adjustment.id,
                summary = "${adjustment.adjustmentType} ${adjustment.qty} units on '${adjustment.itemName}' (${adjustment.reason}) → Closing: $newClosing",
                createdAt = now,
                updatedAt = now
            )
        )
        return newClosing
    }

    suspend fun recordStockTransfer(transfer: StockTransferEntity) {
        val dao = step2Dao ?: return
        dao.recordStockTransferInTransaction(transfer)
        val now = System.currentTimeMillis()
        coreDao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                companyId = transfer.companyId,
                userId = _appPrefs.value.activeUserId,
                userName = _appPrefs.value.activeUserName,
                action = "GODOWN_TRANSFER",
                entityType = "stock_transfer",
                entityId = transfer.id,
                summary = "Transferred ${transfer.qty} units of '${transfer.itemName}' from ${transfer.fromWarehouseName} to ${transfer.toWarehouseName}",
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun bulkUpdateItemPricesAndTax(
        companyId: String,
        itemIds: List<String>,
        priceChangeBps: Int,
        newTaxRateBps: Int?
    ): Int {
        val dao = step2Dao ?: return 0
        val now = System.currentTimeMillis()
        val updatedCount = dao.bulkUpdateItemPricesAndTaxInTransaction(
            companyId = companyId,
            itemIds = itemIds,
            priceChangeBps = priceChangeBps,
            newTaxRateBps = newTaxRateBps,
            now = now
        )
        if (updatedCount > 0) {
            coreDao.insertAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    companyId = companyId,
                    userId = _appPrefs.value.activeUserId,
                    userName = _appPrefs.value.activeUserName,
                    action = "BULK_ITEM_UPDATE",
                    entityType = "item",
                    entityId = itemIds.firstOrNull() ?: "",
                    summary = "Bulk updated $updatedCount item(s) (Price Δ ${priceChangeBps / 100}%)",
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
        return updatedCount
    }

    suspend fun bulkImportItems(
        companyId: String,
        rows: List<ItemImportPreviewRow>,
        skipDuplicates: Boolean = true
    ): Int {
        val dao = step2Dao ?: return 0
        val now = System.currentTimeMillis()
        val defaultWh = dao.getWarehousesNow(companyId).firstOrNull()
        var imported = 0
        rows.forEach { row ->
            if (skipDuplicates && row.duplicateReason != null) return@forEach
            val itemId = UUID.randomUUID().toString()
            val item = ItemEntity(
                id = itemId,
                companyId = companyId,
                itemType = "PRODUCT",
                name = row.name.trim(),
                itemCode = row.itemCode.ifBlank { "SKU-${(1000..9999).random()}" },
                hsnSacCode = row.hsnSacCode,
                categoryId = "${companyId}_cat_kirana",
                categoryName = row.categoryName,
                primaryUnit = row.primaryUnit,
                secondaryUnit = row.secondaryUnit,
                conversionRate = row.conversionRate,
                salePricePaise = row.salePrice.paise,
                purchasePricePaise = row.purchasePrice.paise,
                wholesalePricePaise = row.wholesalePrice.paise,
                mrpPaise = row.mrp.paise,
                taxRateBps = row.taxRateBps,
                openingStockQty = row.openingStockQty,
                openingStockDate = now,
                currentStockQty = row.openingStockQty,
                minStockAlertQty = row.minStockAlertQty,
                barcode = row.barcode,
                warehouseId = defaultWh?.id ?: "${companyId}_wh_main",
                warehouseName = defaultWh?.name ?: "Main Godown",
                createdAt = now,
                updatedAt = now
            )
            dao.saveItemWithDetailsInTransaction(item = item)
            imported++
        }
        return imported
    }

    suspend fun saveItemCategory(companyId: String, name: String, description: String) {
        val dao = step2Dao ?: return
        val now = System.currentTimeMillis()
        dao.upsertItemCategory(
            ItemCategoryEntity(
                id = UUID.randomUUID().toString(),
                companyId = companyId,
                name = name.trim(),
                description = description.trim(),
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun saveWarehouse(
        companyId: String,
        name: String,
        code: String,
        address: String,
        inchargeName: String,
        phone: String
    ) {
        val dao = step2Dao ?: return
        val now = System.currentTimeMillis()
        dao.upsertWarehouse(
            WarehouseEntity(
                id = UUID.randomUUID().toString(),
                companyId = companyId,
                name = name.trim(),
                code = code.trim().uppercase().ifBlank { "GDN-NEW" },
                address = address.trim(),
                inchargeName = inchargeName.trim(),
                phone = phone.trim(),
                isPrimary = false,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun savePartyGroup(companyId: String, name: String, description: String, discountBps: Int) {
        val dao = step2Dao ?: return
        val now = System.currentTimeMillis()
        dao.upsertPartyGroup(
            PartyGroupEntity(
                id = UUID.randomUUID().toString(),
                companyId = companyId,
                name = name.trim(),
                description = description.trim(),
                discountBps = discountBps,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun updateCompanyProfile(updatedCompany: CompanyEntity) {
        val now = System.currentTimeMillis()
        val entity = updatedCompany.copy(updatedAt = now)
        coreDao.upsertCompany(entity)
        if (entity.logoUri.isNotBlank()) {
            coreDao.insertAttachment(
                AttachmentEntity(
                    id = "${entity.id}_logo",
                    companyId = entity.id,
                    ownerRecordType = "company_logo",
                    ownerRecordId = entity.id,
                    fileName = "company_logo.png",
                    mimeType = "image/*",
                    localPath = entity.logoUri,
                    sizeBytes = 0L,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
        if (entity.signatureUri.isNotBlank()) {
            coreDao.insertAttachment(
                AttachmentEntity(
                    id = "${entity.id}_signature",
                    companyId = entity.id,
                    ownerRecordType = "company_signature",
                    ownerRecordId = entity.id,
                    fileName = "company_signature.png",
                    mimeType = "image/*",
                    localPath = entity.signatureUri,
                    sizeBytes = 0L,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
        updatePreferences { it.copy(fyStartMonth = entity.fyStartMonth) }
        coreDao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                companyId = entity.id,
                userId = _appPrefs.value.activeUserId,
                userName = _appPrefs.value.activeUserName,
                action = "COMPANY_PROFILE_UPDATED",
                entityType = "company",
                entityId = entity.id,
                summary = "Updated business profile for ${entity.businessName}",
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun updateInvoiceSettings(prefix: String, startingNumber: Long, fyStartMonth: Int) {
        val now = System.currentTimeMillis()
        val companyId = _appPrefs.value.activeCompanyId
        updatePreferences {
            it.copy(
                invoicePrefix = prefix,
                invoiceStartingNumber = startingNumber.coerceAtLeast(1L),
                fyStartMonth = fyStartMonth
            )
        }
        if (companyId.isNotBlank()) {
            val fyCode = if (fyStartMonth == 4) "FY 2026-27" else "CY 2026"
            coreDao.upsertSingleNumberSeries(
                NumberSeriesEntity(
                    id = "${companyId}_SALE_INVOICE",
                    companyId = companyId,
                    docType = "SALE_INVOICE",
                    prefix = prefix,
                    fyCode = fyCode,
                    nextNumber = startingNumber.coerceAtLeast(1L),
                    paddingDigits = 4,
                    createdAt = now,
                    updatedAt = now
                )
            )
            coreDao.upsertSettings(
                listOf(
                    SettingEntity("${companyId}_invoice_prefix", companyId, "invoice_prefix", prefix, now, now),
                    SettingEntity("${companyId}_invoice_start", companyId, "invoice_starting_no", startingNumber.toString(), now, now),
                    SettingEntity("${companyId}_fy_start", companyId, "fy_start_month", fyStartMonth.toString(), now, now)
                )
            )
            val companies = coreDao.getCompaniesNow()
            val active = companies.firstOrNull { it.id == companyId }
            if (active != null && active.fyStartMonth != fyStartMonth) {
                coreDao.upsertCompany(active.copy(fyStartMonth = fyStartMonth, updatedAt = now))
            }
        }
    }

    suspend fun changeActiveUserPin(currentPin: String, newPin: String): Boolean {
        if (newPin.length != 6) return false
        val matched = verifyPinForActiveCompany(currentPin) ?: return false
        val salted = PinHasher.hashPin(newPin)
        val now = System.currentTimeMillis()
        coreDao.upsertUser(
            matched.copy(
                pinHash = salted.hashHex,
                pinSalt = salted.saltHex,
                updatedAt = now
            )
        )
        coreDao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                companyId = matched.companyId,
                userId = matched.id,
                userName = matched.fullName,
                action = "PIN_CHANGED",
                entityType = "app_user",
                entityId = matched.id,
                summary = "Updated 6-digit PIN for ${matched.fullName}",
                createdAt = now,
                updatedAt = now
            )
        )
        return true
    }

    suspend fun addLocalUser(fullName: String, role: AppRole, pin: String): AppUserEntity? {
        if (fullName.isBlank() || pin.length != 6) return null
        val companyId = _appPrefs.value.activeCompanyId.ifBlank {
            coreDao.getCompaniesNow().firstOrNull()?.id ?: return null
        }
        val now = System.currentTimeMillis()
        val salted = PinHasher.hashPin(pin)
        val newUser = AppUserEntity(
            id = UUID.randomUUID().toString(),
            companyId = companyId,
            fullName = fullName.trim(),
            role = role.name,
            pinHash = salted.hashHex,
            pinSalt = salted.saltHex,
            isActive = true,
            createdAt = now,
            updatedAt = now
        )
        coreDao.upsertUser(newUser)
        coreDao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                companyId = companyId,
                userId = _appPrefs.value.activeUserId,
                userName = _appPrefs.value.activeUserName,
                action = "USER_ADDED",
                entityType = "app_user",
                entityId = newUser.id,
                summary = "Added local user ${newUser.fullName} with role ${role.displayName}",
                createdAt = now,
                updatedAt = now
            )
        )
        return newUser
    }

    suspend fun verifyPinForActiveCompany(pin: String): AppUserEntity? {
        val companyId = _appPrefs.value.activeCompanyId
        val users = if (companyId.isNotBlank()) {
            coreDao.getUsersNow(companyId)
        } else {
            val firstCompany = coreDao.getCompaniesNow().firstOrNull() ?: return null
            coreDao.getUsersNow(firstCompany.id)
        }
        return users.firstOrNull { user ->
            PinHasher.verifyPin(pin, user.pinHash, user.pinSalt)
        }
    }

    suspend fun switchUserByPin(pin: String): AppUserEntity? {
        val matched = verifyPinForActiveCompany(pin) ?: return null
        val role = runCatching { AppRole.valueOf(matched.role) }.getOrDefault(AppRole.VIEWER)
        updatePreferences {
            it.copy(
                activeUserId = matched.id,
                activeUserName = matched.fullName,
                activeUserRole = role
            )
        }
        val now = System.currentTimeMillis()
        coreDao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                companyId = matched.companyId,
                userId = matched.id,
                userName = matched.fullName,
                action = "USER_SWITCH",
                entityType = "app_user",
                entityId = matched.id,
                summary = "Switched active session to ${matched.fullName} (${role.displayName})",
                createdAt = now,
                updatedAt = now
            )
        )
        return matched
    }

    suspend fun switchCompany(company: CompanyEntity) {
        ensureStep2MasterDataSeeded(company.id)
        ensureStep3ADataSeeded(company.id)
        val users = coreDao.getUsersNow(company.id)
        val owner = users.firstOrNull { it.role == AppRole.OWNER.name } ?: users.firstOrNull()
        updatePreferences {
            it.copy(
                activeCompanyId = company.id,
                activeUserId = owner?.id ?: "",
                activeUserName = owner?.fullName ?: "Owner",
                activeUserRole = AppRole.OWNER,
                fyStartMonth = company.fyStartMonth
            )
        }
    }

    // ========================================================================
    // STEP 3A SEEDING & MUTATION METHODS (GST ENGINE, SALES DOCS & PAYMENTS)
    // ========================================================================

    suspend fun ensureStep3ADataSeeded(companyId: String) {
        val dao = step3ADao ?: return
        if (companyId.isBlank()) return
        val existingTxns = dao.getTransactionsNow(companyId)
        if (existingTxns.isNotEmpty()) return

        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L
        val invPrefix = _appPrefs.value.invoicePrefix.ifBlank { "INV/26-27/" }

        val mehtaId = "${companyId}_party_mehta"
        val balajiId = "${companyId}_party_balaji"
        val patelId = "${companyId}_party_patel"
        val kaveriId = "${companyId}_party_kaveri"

        val itemRiceId = "${companyId}_item_basmati"
        val itemGheeId = "${companyId}_item_ghee"
        val itemOilId = "${companyId}_item_oil"
        val itemBulbId = "${companyId}_item_bulb"
        val itemScannerId = "${companyId}_item_scanner"
        val itemAmcId = "${companyId}_item_amc"
        val mainWhId = "${companyId}_wh_main"

        // 1. Quotation EST/26-27/0001 -> Converted to SO/26-27/0001 -> DC/26-27/0001 -> INV/26-27/0001
        val est1Id = "${companyId}_txn_est_1"
        val so1Id = "${companyId}_txn_so_1"
        val dc1Id = "${companyId}_txn_dc_1"
        val inv1Id = "${companyId}_txn_inv_1"
        val inv2Id = "${companyId}_txn_inv_2"
        val inv3Id = "${companyId}_txn_inv_3"
        val inv4Id = "${companyId}_txn_inv_4"
        val est2Id = "${companyId}_txn_est_2"
        val cn1Id = "${companyId}_txn_cn_1"

        val inv1Total = 29_925_00L // 10 bags Basmati @ ₹2,850 = ₹28,500 + 5% GST (₹1,425) = ₹29,925
        val inv1Cost = 24_000_00L  // 10 bags @ ₹2,400 = ₹24,000 -> Gross Profit = ₹4,500

        val inv2Total = 42_800_00L // Partial paid (₹20,000 paid, ₹22,800 balance)
        val inv2Cost = 34_200_00L

        val inv3Total = 28_200_00L // Unpaid
        val inv3Cost = 22_100_00L

        val inv4Total = 18_900_00L // Overdue
        val inv4Cost = 14_800_00L

        val seededTxns = listOf(
            // Paid Tax Invoice #1 (Mehta Kirana - Today)
            TxnEntity(
                id = inv1Id,
                companyId = companyId,
                docType = SalesDocTypes.TAX_INVOICE,
                docNumber = "${invPrefix}0001",
                txnDate = now - 2 * 3600_000L,
                dueDate = now + 15 * dayMs,
                partyId = mehtaId,
                partyName = "Mehta Kirana & Traders",
                partyGstin = "27AAPFM3456D1Z9",
                partyPhone = "9876501234",
                billingAddress = "Shop 12, Market Yard Wholesale Mandi, Pune",
                shippingAddress = "Shop 12, Market Yard Wholesale Mandi (Godown Gate 2), Pune",
                placeOfSupplyStateCode = "27",
                placeOfSupplyStateName = "Maharashtra",
                supplyType = "INTRA_STATE",
                classification = "B2B",
                grossSubtotalPaise = 28_500_00L,
                lineDiscountTotalPaise = 0L,
                invoiceDiscountPaise = 0L,
                taxableSubtotalPaise = 28_500_00L,
                cgstTotalPaise = 712_50L,
                sgstTotalPaise = 712_50L,
                igstTotalPaise = 0L,
                cessTotalPaise = 0L,
                totalTaxPaise = 1_425_00L,
                grandTotalPaise = inv1Total,
                paidAmountPaise = inv1Total,
                balanceAmountPaise = 0L,
                totalCostPaise = inv1Cost,
                grossProfitPaise = 4_500_00L,
                status = "Paid",
                paymentTerms = "15 Days Credit",
                notes = "Delivered via Narmada Roadlines",
                termsText = "Goods once sold will not be taken back. Subject to Pune jurisdiction.",
                amountInWords = IndianNumberToWords.toCurrencyWords(Money(inv1Total)),
                warehouseId = mainWhId,
                warehouseName = "Main Godown",
                createdAt = now - 2 * 3600_000L,
                updatedAt = now
            ),
            // Partial Tax Invoice #2 (Shree Balaji Distributors - 2 days ago)
            TxnEntity(
                id = inv2Id,
                companyId = companyId,
                docType = SalesDocTypes.TAX_INVOICE,
                docNumber = "${invPrefix}0002",
                txnDate = now - 2 * dayMs,
                dueDate = now + 12 * dayMs,
                partyId = balajiId,
                partyName = "Shree Balaji Distributors",
                partyGstin = "27AABCS9876G1Z3",
                partyPhone = "9823098765",
                billingAddress = "Gala 4, Bhiwandi Logistics Park, Mumbai",
                shippingAddress = "Gala 4, Bhiwandi Logistics Park, Mumbai",
                placeOfSupplyStateCode = "27",
                placeOfSupplyStateName = "Maharashtra",
                supplyType = "INTRA_STATE",
                classification = "B2B",
                grossSubtotalPaise = 38_214_28L,
                taxableSubtotalPaise = 38_214_28L,
                cgstTotalPaise = 2_292_86L,
                sgstTotalPaise = 2_292_86L,
                totalTaxPaise = 4_585_72L,
                grandTotalPaise = inv2Total,
                paidAmountPaise = 20_000_00L,
                balanceAmountPaise = 22_800_00L,
                totalCostPaise = inv2Cost,
                grossProfitPaise = 4_014_28L,
                status = "Partial",
                paymentTerms = "30 Days Credit",
                notes = "Part payment received via UPI",
                termsText = "Interest @18% p.a. applicable after due date.",
                amountInWords = IndianNumberToWords.toCurrencyWords(Money(inv2Total)),
                warehouseId = mainWhId,
                warehouseName = "Main Godown",
                createdAt = now - 2 * dayMs,
                updatedAt = now
            ),
            // Unpaid Tax Invoice #3 (Patel Hardware Mart - 5 days ago)
            TxnEntity(
                id = inv3Id,
                companyId = companyId,
                docType = SalesDocTypes.TAX_INVOICE,
                docNumber = "${invPrefix}0003",
                txnDate = now - 5 * dayMs,
                dueDate = now + 10 * dayMs,
                partyId = patelId,
                partyName = "Patel Hardware Mart",
                partyGstin = "27AAJFP4321F1Z8",
                partyPhone = "9765432109",
                billingAddress = "44 Panchavati Main Road, Nashik",
                shippingAddress = "44 Panchavati Main Road, Nashik",
                placeOfSupplyStateCode = "27",
                placeOfSupplyStateName = "Maharashtra",
                supplyType = "INTRA_STATE",
                classification = "B2B",
                grossSubtotalPaise = 23_898_30L,
                taxableSubtotalPaise = 23_898_30L,
                cgstTotalPaise = 2_150_85L,
                sgstTotalPaise = 2_150_85L,
                totalTaxPaise = 4_301_70L,
                grandTotalPaise = inv3Total,
                paidAmountPaise = 0L,
                balanceAmountPaise = inv3Total,
                totalCostPaise = inv3Cost,
                grossProfitPaise = 1_798_30L,
                status = "Unpaid",
                paymentTerms = "15 Days Credit",
                notes = "LED & Scanner wholesale lot",
                termsText = "Standard B2B warranty applies.",
                amountInWords = IndianNumberToWords.toCurrencyWords(Money(inv3Total)),
                warehouseId = mainWhId,
                warehouseName = "Main Godown",
                createdAt = now - 5 * dayMs,
                updatedAt = now
            ),
            // Overdue Tax Invoice #4 (Kaveri Supermart - 22 days ago, due 7 days ago)
            TxnEntity(
                id = inv4Id,
                companyId = companyId,
                docType = SalesDocTypes.TAX_INVOICE,
                docNumber = "${invPrefix}0004",
                txnDate = now - 22 * dayMs,
                dueDate = now - 7 * dayMs,
                partyId = kaveriId,
                partyName = "Kaveri Supermart",
                partyGstin = "27AADCK9012C1Z9",
                partyPhone = "9811223344",
                billingAddress = "Plot 9, Sitabuldi Commercial Complex, Nagpur",
                shippingAddress = "Plot 9, Sitabuldi Commercial Complex, Nagpur",
                placeOfSupplyStateCode = "27",
                placeOfSupplyStateName = "Maharashtra",
                supplyType = "INTRA_STATE",
                classification = "B2B",
                grossSubtotalPaise = 18_000_00L,
                taxableSubtotalPaise = 18_000_00L,
                cgstTotalPaise = 450_00L,
                sgstTotalPaise = 450_00L,
                totalTaxPaise = 900_00L,
                grandTotalPaise = inv4Total,
                paidAmountPaise = 0L,
                balanceAmountPaise = inv4Total,
                totalCostPaise = inv4Cost,
                grossProfitPaise = 3_200_00L,
                status = "Overdue",
                paymentTerms = "15 Days Credit",
                notes = "Follow up for NEFT settlement",
                termsText = "Subject to Pune jurisdiction.",
                amountInWords = IndianNumberToWords.toCurrencyWords(Money(inv4Total)),
                warehouseId = mainWhId,
                warehouseName = "Main Godown",
                createdAt = now - 22 * dayMs,
                updatedAt = now
            ),
            // Quotation #1 (Converted to Order)
            TxnEntity(
                id = est1Id,
                companyId = companyId,
                docType = SalesDocTypes.ESTIMATE,
                docNumber = "EST/26-27/0001",
                txnDate = now - 6 * dayMs,
                dueDate = now + 7 * dayMs,
                partyId = mehtaId,
                partyName = "Mehta Kirana & Traders",
                partyGstin = "27AAPFM3456D1Z9",
                partyPhone = "9876501234",
                placeOfSupplyStateCode = "27",
                placeOfSupplyStateName = "Maharashtra",
                supplyType = "INTRA_STATE",
                classification = "B2B",
                grossSubtotalPaise = 28_500_00L,
                taxableSubtotalPaise = 28_500_00L,
                cgstTotalPaise = 712_50L,
                sgstTotalPaise = 712_50L,
                totalTaxPaise = 1_425_00L,
                grandTotalPaise = inv1Total,
                paidAmountPaise = 0L,
                balanceAmountPaise = inv1Total,
                totalCostPaise = inv1Cost,
                grossProfitPaise = 4_500_00L,
                status = "Converted",
                notes = "Converted to SO/26-27/0001",
                amountInWords = IndianNumberToWords.toCurrencyWords(Money(inv1Total)),
                createdAt = now - 6 * dayMs,
                updatedAt = now
            ),
            // Quotation #2 (Open Estimate for Kaveri Supermart)
            TxnEntity(
                id = est2Id,
                companyId = companyId,
                docType = SalesDocTypes.ESTIMATE,
                docNumber = "EST/26-27/0002",
                txnDate = now - 1 * dayMs,
                dueDate = now + 14 * dayMs,
                partyId = kaveriId,
                partyName = "Kaveri Supermart",
                partyGstin = "27AADCK9012C1Z9",
                partyPhone = "9811223344",
                placeOfSupplyStateCode = "27",
                placeOfSupplyStateName = "Maharashtra",
                supplyType = "INTRA_STATE",
                classification = "B2B",
                grossSubtotalPaise = 32_000_00L,
                taxableSubtotalPaise = 32_000_00L,
                cgstTotalPaise = 1_920_00L,
                sgstTotalPaise = 1_920_00L,
                totalTaxPaise = 3_840_00L,
                grandTotalPaise = 35_840_00L,
                paidAmountPaise = 0L,
                balanceAmountPaise = 35_840_00L,
                totalCostPaise = 28_000_00L,
                grossProfitPaise = 4_000_00L,
                status = "Unpaid",
                notes = "Diwali festive ghee bulk quotation",
                amountInWords = IndianNumberToWords.toCurrencyWords(Money(35_840_00L)),
                createdAt = now - 1 * dayMs,
                updatedAt = now
            ),
            // Sale Order #1
            TxnEntity(
                id = so1Id,
                companyId = companyId,
                docType = SalesDocTypes.SALE_ORDER,
                docNumber = "SO/26-27/0001",
                txnDate = now - 4 * dayMs,
                dueDate = now + 7 * dayMs,
                partyId = mehtaId,
                partyName = "Mehta Kirana & Traders",
                partyGstin = "27AAPFM3456D1Z9",
                partyPhone = "9876501234",
                placeOfSupplyStateCode = "27",
                placeOfSupplyStateName = "Maharashtra",
                supplyType = "INTRA_STATE",
                classification = "B2B",
                grossSubtotalPaise = 28_500_00L,
                taxableSubtotalPaise = 28_500_00L,
                cgstTotalPaise = 712_50L,
                sgstTotalPaise = 712_50L,
                totalTaxPaise = 1_425_00L,
                grandTotalPaise = inv1Total,
                paidAmountPaise = 0L,
                balanceAmountPaise = inv1Total,
                totalCostPaise = inv1Cost,
                grossProfitPaise = 4_500_00L,
                status = "Converted",
                notes = "Converted from EST/26-27/0001",
                amountInWords = IndianNumberToWords.toCurrencyWords(Money(inv1Total)),
                createdAt = now - 4 * dayMs,
                updatedAt = now
            ),
            // Delivery Challan #1
            TxnEntity(
                id = dc1Id,
                companyId = companyId,
                docType = SalesDocTypes.DELIVERY_CHALLAN,
                docNumber = "DC/26-27/0001",
                txnDate = now - 3 * dayMs,
                dueDate = now - 3 * dayMs,
                partyId = mehtaId,
                partyName = "Mehta Kirana & Traders",
                partyGstin = "27AAPFM3456D1Z9",
                partyPhone = "9876501234",
                placeOfSupplyStateCode = "27",
                placeOfSupplyStateName = "Maharashtra",
                supplyType = "INTRA_STATE",
                classification = "B2B",
                grossSubtotalPaise = 28_500_00L,
                taxableSubtotalPaise = 28_500_00L,
                cgstTotalPaise = 712_50L,
                sgstTotalPaise = 712_50L,
                totalTaxPaise = 1_425_00L,
                grandTotalPaise = inv1Total,
                paidAmountPaise = 0L,
                balanceAmountPaise = inv1Total,
                totalCostPaise = inv1Cost,
                grossProfitPaise = 4_500_00L,
                status = "Paid",
                notes = "Dispatched from Main Godown",
                amountInWords = IndianNumberToWords.toCurrencyWords(Money(inv1Total)),
                createdAt = now - 3 * dayMs,
                updatedAt = now
            ),
            // Sale Return / Credit Note #1
            TxnEntity(
                id = cn1Id,
                companyId = companyId,
                docType = SalesDocTypes.CREDIT_NOTE,
                docNumber = "CN/26-27/0001",
                txnDate = now - 1 * dayMs,
                dueDate = now - 1 * dayMs,
                partyId = patelId,
                partyName = "Patel Hardware Mart",
                partyGstin = "27AAJFP4321F1Z8",
                partyPhone = "9765432109",
                placeOfSupplyStateCode = "27",
                placeOfSupplyStateName = "Maharashtra",
                supplyType = "INTRA_STATE",
                classification = "B2B",
                grossSubtotalPaise = 2_033_90L,
                taxableSubtotalPaise = 2_033_90L,
                cgstTotalPaise = 183_05L,
                sgstTotalPaise = 183_05L,
                totalTaxPaise = 366_10L,
                grandTotalPaise = 2_400_00L,
                paidAmountPaise = 2_400_00L,
                balanceAmountPaise = 0L,
                totalCostPaise = 1_500_00L,
                grossProfitPaise = -533_90L,
                status = "Paid",
                notes = "Return of 2 transit-damaged LED units",
                amountInWords = IndianNumberToWords.toCurrencyWords(Money(2_400_00L)),
                createdAt = now - 1 * dayMs,
                updatedAt = now
            )
        )
        seededTxns.forEach { dao.upsertTxn(it) }

        // Seed TxnLines
        val seededLines = listOf(
            TxnLineEntity(
                id = "${inv1Id}_line_1",
                companyId = companyId,
                txnId = inv1Id,
                lineIndex = 0,
                itemId = itemRiceId,
                itemName = "India Gate Classic Basmati Rice (25kg Bag)",
                itemCode = "SKU-RICE-25",
                hsnSacCode = "1006",
                batchId = "${companyId}_batch_rice_1",
                batchNumber = "IG-26A",
                qty = 10L,
                freeQty = 1L,
                unit = "BAG",
                unitPricePaise = 2_850_00L,
                purchaseCostPaise = 2_400_00L,
                taxRateBps = 500,
                taxablePaise = 28_500_00L,
                cgstPaise = 712_50L,
                sgstPaise = 712_50L,
                igstPaise = 0L,
                cessPaise = 0L,
                lineTotalPaise = 29_925_00L,
                createdAt = now,
                updatedAt = now
            ),
            TxnLineEntity(
                id = "${inv2Id}_line_1",
                companyId = companyId,
                txnId = inv2Id,
                lineIndex = 0,
                itemId = itemGheeId,
                itemName = "Amul Pure Cow Ghee 1L Tin",
                itemCode = "SKU-GHEE-1L",
                hsnSacCode = "0405",
                batchId = "${companyId}_batch_ghee_exp",
                batchNumber = "AML-EXP01",
                qty = 66L,
                freeQty = 2L,
                unit = "PCS",
                unitPricePaise = 648_48L,
                purchaseCostPaise = 560_00L,
                taxInclusive = true,
                taxRateBps = 1200,
                taxablePaise = 38_214_28L,
                cgstPaise = 2_292_86L,
                sgstPaise = 2_292_86L,
                lineTotalPaise = 42_800_00L,
                createdAt = now,
                updatedAt = now
            ),
            TxnLineEntity(
                id = "${inv3Id}_line_1",
                companyId = companyId,
                txnId = inv3Id,
                lineIndex = 0,
                itemId = itemScannerId,
                itemName = "TVS Posiflex 2D Wireless Barcode Scanner",
                itemCode = "SKU-SCAN-2D",
                hsnSacCode = "8471",
                qty = 6L,
                freeQty = 0L,
                unit = "PCS",
                unitPricePaise = 3_983_05L,
                purchaseCostPaise = 3_150_00L,
                taxRateBps = 1800,
                taxablePaise = 23_898_30L,
                cgstPaise = 2_150_85L,
                sgstPaise = 2_150_85L,
                lineTotalPaise = 28_200_00L,
                createdAt = now,
                updatedAt = now
            ),
            TxnLineEntity(
                id = "${inv4Id}_line_1",
                companyId = companyId,
                txnId = inv4Id,
                lineIndex = 0,
                itemId = itemOilId,
                itemName = "Fortune Sunlite Refined Sunflower Oil 15L Jar",
                itemCode = "SKU-OIL-15L",
                hsnSacCode = "1512",
                qty = 8L,
                freeQty = 0L,
                unit = "PCS",
                unitPricePaise = 2_250_00L,
                purchaseCostPaise = 1_850_00L,
                taxRateBps = 500,
                taxablePaise = 18_000_00L,
                cgstPaise = 450_00L,
                sgstPaise = 450_00L,
                lineTotalPaise = 18_900_00L,
                createdAt = now,
                updatedAt = now
            ),
            TxnLineEntity(
                id = "${est1Id}_line_1",
                companyId = companyId,
                txnId = est1Id,
                lineIndex = 0,
                itemId = itemRiceId,
                itemName = "India Gate Classic Basmati Rice (25kg Bag)",
                itemCode = "SKU-RICE-25",
                hsnSacCode = "1006",
                qty = 10L,
                unit = "BAG",
                unitPricePaise = 2_850_00L,
                purchaseCostPaise = 2_400_00L,
                taxRateBps = 500,
                taxablePaise = 28_500_00L,
                cgstPaise = 712_50L,
                sgstPaise = 712_50L,
                lineTotalPaise = 29_925_00L,
                createdAt = now,
                updatedAt = now
            ),
            TxnLineEntity(
                id = "${est2Id}_line_1",
                companyId = companyId,
                txnId = est2Id,
                lineIndex = 0,
                itemId = itemGheeId,
                itemName = "Amul Pure Cow Ghee 1L Tin",
                itemCode = "SKU-GHEE-1L",
                hsnSacCode = "0405",
                qty = 50L,
                unit = "PCS",
                unitPricePaise = 640_00L,
                purchaseCostPaise = 560_00L,
                taxRateBps = 1200,
                taxablePaise = 32_000_00L,
                cgstPaise = 1_920_00L,
                sgstPaise = 1_920_00L,
                lineTotalPaise = 35_840_00L,
                createdAt = now,
                updatedAt = now
            ),
            TxnLineEntity(
                id = "${so1Id}_line_1",
                companyId = companyId,
                txnId = so1Id,
                lineIndex = 0,
                itemId = itemRiceId,
                itemName = "India Gate Classic Basmati Rice (25kg Bag)",
                itemCode = "SKU-RICE-25",
                hsnSacCode = "1006",
                qty = 10L,
                unit = "BAG",
                unitPricePaise = 2_850_00L,
                purchaseCostPaise = 2_400_00L,
                taxRateBps = 500,
                taxablePaise = 28_500_00L,
                cgstPaise = 712_50L,
                sgstPaise = 712_50L,
                lineTotalPaise = 29_925_00L,
                createdAt = now,
                updatedAt = now
            ),
            TxnLineEntity(
                id = "${dc1Id}_line_1",
                companyId = companyId,
                txnId = dc1Id,
                lineIndex = 0,
                itemId = itemRiceId,
                itemName = "India Gate Classic Basmati Rice (25kg Bag)",
                itemCode = "SKU-RICE-25",
                hsnSacCode = "1006",
                qty = 10L,
                unit = "BAG",
                unitPricePaise = 2_850_00L,
                purchaseCostPaise = 2_400_00L,
                taxRateBps = 500,
                taxablePaise = 28_500_00L,
                cgstPaise = 712_50L,
                sgstPaise = 712_50L,
                lineTotalPaise = 29_925_00L,
                createdAt = now,
                updatedAt = now
            ),
            TxnLineEntity(
                id = "${cn1Id}_line_1",
                companyId = companyId,
                txnId = cn1Id,
                lineIndex = 0,
                itemId = itemBulbId,
                itemName = "Philips 9W B22 Cool Day Light LED Bulb",
                itemCode = "SKU-LED-9W",
                hsnSacCode = "8539",
                qty = 24L,
                unit = "PCS",
                unitPricePaise = 84_75L,
                purchaseCostPaise = 62_50L,
                taxRateBps = 1800,
                taxablePaise = 2_033_90L,
                cgstPaise = 183_05L,
                sgstPaise = 183_05L,
                lineTotalPaise = 2_400_00L,
                createdAt = now,
                updatedAt = now
            )
        )
        dao.upsertTxnLines(seededLines)

        // Seed HSN Tax Summary Lines
        val seededTaxLines = listOf(
            TxnTaxLineEntity("${inv1Id}_tax_1", companyId, inv1Id, "1006", "Basmati Rice", "BAG", 10L, 28_500_00L, 500, 712_50L, 712_50L, 0L, 0L, 1_425_00L, now, now),
            TxnTaxLineEntity("${inv2Id}_tax_1", companyId, inv2Id, "0405", "Cow Ghee", "PCS", 66L, 38_214_28L, 1200, 2_292_86L, 2_292_86L, 0L, 0L, 4_585_72L, now, now),
            TxnTaxLineEntity("${inv3Id}_tax_1", companyId, inv3Id, "8471", "Barcode Scanners", "PCS", 6L, 23_898_30L, 1800, 2_150_85L, 2_150_85L, 0L, 0L, 4_301_70L, now, now),
            TxnTaxLineEntity("${inv4Id}_tax_1", companyId, inv4Id, "1512", "Refined Sunflower Oil", "PCS", 8L, 18_000_00L, 500, 450_00L, 450_00L, 0L, 0L, 900_00L, now, now),
            TxnTaxLineEntity("${cn1Id}_tax_1", companyId, cn1Id, "8539", "LED Bulbs (Credit Note)", "PCS", 24L, 2_033_90L, 1800, 183_05L, 183_05L, 0L, 0L, 366_10L, now, now)
        )
        dao.upsertTxnTaxLines(seededTaxLines)

        // Seed Conversion Links (Quotation -> Order -> Challan -> Invoice, and Invoice -> Credit Note)
        val seededLinks = listOf(
            TxnLinkEntity("${companyId}_link_1", companyId, est1Id, "ESTIMATE", "EST/26-27/0001", so1Id, "SALE_ORDER", "SO/26-27/0001", "QUOTATION_TO_ORDER", now - 4 * dayMs),
            TxnLinkEntity("${companyId}_link_2", companyId, so1Id, "SALE_ORDER", "SO/26-27/0001", dc1Id, "DELIVERY_CHALLAN", "DC/26-27/0001", "ORDER_TO_CHALLAN", now - 3 * dayMs),
            TxnLinkEntity("${companyId}_link_3", companyId, dc1Id, "DELIVERY_CHALLAN", "DC/26-27/0001", inv1Id, "TAX_INVOICE", "${invPrefix}0001", "CHALLAN_TO_INVOICE", now - 2 * 3600_000L),
            TxnLinkEntity("${companyId}_link_4", companyId, inv3Id, "TAX_INVOICE", "${invPrefix}0003", cn1Id, "CREDIT_NOTE", "CN/26-27/0001", "INVOICE_TO_RETURN", now - 1 * dayMs)
        )
        seededLinks.forEach { dao.insertTxnLink(it) }

        // Seed Payments & Bill-to-Bill Allocations
        val pay1Id = "${companyId}_pay_1"
        val pay2Id = "${companyId}_pay_2"
        val seededPayments = listOf(
            PaymentEntity(
                id = pay1Id,
                companyId = companyId,
                paymentNumber = "RCPT/26-27/0001",
                paymentType = "PAYMENT_IN",
                paymentDate = now - 2 * 3600_000L,
                partyId = mehtaId,
                partyName = "Mehta Kirana & Traders",
                totalAmountPaise = inv1Total,
                discountPaise = 0L,
                unallocatedAdvancePaise = 0L,
                primaryMode = "UPI",
                modesSplitJson = "UPI:${inv1Total / 2}|CASH:${inv1Total - inv1Total / 2}",
                referenceNo = "UPI-6328910421",
                status = "Paid",
                notes = "Split UPI + Cash settlement against ${invPrefix}0001",
                createdAt = now - 2 * 3600_000L,
                updatedAt = now
            ),
            PaymentEntity(
                id = pay2Id,
                companyId = companyId,
                paymentNumber = "RCPT/26-27/0002",
                paymentType = "PAYMENT_IN",
                paymentDate = now - 1 * dayMs,
                partyId = balajiId,
                partyName = "Shree Balaji Distributors",
                totalAmountPaise = 20_000_00L,
                discountPaise = 0L,
                unallocatedAdvancePaise = 0L,
                primaryMode = "CHEQUE",
                modesSplitJson = "CHEQUE:2000000",
                referenceNo = "CHQ-448912",
                chequeId = "${companyId}_chq_1",
                status = "Paid",
                notes = "Part payment cheque against ${invPrefix}0002",
                createdAt = now - 1 * dayMs,
                updatedAt = now
            )
        )
        seededPayments.forEach { dao.upsertPayment(it) }

        val seededAllocations = listOf(
            PaymentAllocationEntity("${pay1Id}_alloc_1", companyId, pay1Id, inv1Id, "${invPrefix}0001", inv1Total, 0L, now, now),
            PaymentAllocationEntity("${pay2Id}_alloc_1", companyId, pay2Id, inv2Id, "${invPrefix}0002", 20_000_00L, 0L, now, now)
        )
        dao.insertPaymentAllocations(seededAllocations)

        // Seed Cheques (including 1 due tomorrow for Dashboard "Cheques Due" alert)
        val seededCheques = listOf(
            ChequeEntity(
                id = "${companyId}_chq_1",
                companyId = companyId,
                paymentId = pay2Id,
                partyId = balajiId,
                partyName = "Shree Balaji Distributors",
                chequeType = "RECEIVABLE",
                chequeNumber = "448912",
                bankName = "HDFC Bank, Bhiwandi Branch",
                ifscCode = "HDFC0001204",
                chequeDate = now - 1 * dayMs,
                dueDate = now + 1 * dayMs, // Due Tomorrow
                amountPaise = 20_000_00L,
                status = "IN_HAND",
                createdAt = now - 1 * dayMs,
                updatedAt = now
            ),
            ChequeEntity(
                id = "${companyId}_chq_2",
                companyId = companyId,
                paymentId = "",
                partyId = patelId,
                partyName = "Patel Hardware Mart",
                chequeType = "RECEIVABLE",
                chequeNumber = "781029",
                bankName = "State Bank of India, Nashik",
                ifscCode = "SBIN0000438",
                chequeDate = now - 4 * dayMs,
                dueDate = now,
                amountPaise = 15_000_00L,
                status = "DEPOSITED",
                depositedDate = now - 1 * dayMs,
                createdAt = now - 4 * dayMs,
                updatedAt = now
            )
        )
        seededCheques.forEach { dao.upsertCheque(it) }

        // Seed Recurring Invoice Templates
        val seededRecurring = listOf(
            RecurringTemplateEntity(
                id = "${companyId}_rec_1",
                companyId = companyId,
                templateTitle = "Monthly POS & Weighing Scale AMC",
                docType = SalesDocTypes.TAX_INVOICE,
                partyId = kaveriId,
                partyName = "Kaveri Supermart",
                partyGstin = "27AADCK9012C1Z9",
                placeOfSupplyStateCode = "27",
                placeOfSupplyStateName = "Maharashtra",
                frequency = "MONTHLY",
                intervalDays = 30,
                nextRunDate = now + 3 * dayMs,
                lastGeneratedTxnId = inv4Id,
                defaultItemId = itemAmcId,
                defaultItemName = "Annual POS & Weighing Scale Calibration AMC",
                defaultHsnCode = "9987",
                defaultQty = 1L,
                defaultUnit = "PCS",
                defaultRatePaise = 3_500_00L,
                defaultTaxRateBps = 1800,
                estimatedGrandTotalPaise = 4_130_00L,
                autoNotify = true,
                isActive = true,
                notes = "Auto-generates on 1st of every month",
                createdAt = now - 15 * dayMs,
                updatedAt = now
            ),
            RecurringTemplateEntity(
                id = "${companyId}_rec_2",
                companyId = companyId,
                templateTitle = "Fortnightly Basmati Wholesale Lot",
                docType = SalesDocTypes.TAX_INVOICE,
                partyId = mehtaId,
                partyName = "Mehta Kirana & Traders",
                partyGstin = "27AAPFM3456D1Z9",
                placeOfSupplyStateCode = "27",
                placeOfSupplyStateName = "Maharashtra",
                frequency = "WEEKLY",
                intervalDays = 14,
                nextRunDate = now + 5 * dayMs,
                lastGeneratedTxnId = inv1Id,
                defaultItemId = itemRiceId,
                defaultItemName = "India Gate Classic Basmati Rice (25kg Bag)",
                defaultHsnCode = "1006",
                defaultQty = 10L,
                defaultUnit = "BAG",
                defaultRatePaise = 2_850_00L,
                defaultTaxRateBps = 500,
                estimatedGrandTotalPaise = 29_925_00L,
                autoNotify = true,
                isActive = true,
                notes = "Standing wholesale standing order",
                createdAt = now - 10 * dayMs,
                updatedAt = now
            )
        )
        seededRecurring.forEach { dao.upsertRecurringTemplate(it) }

        // Ensure SALE_INVOICE number series is advanced past the 4 seeded invoices
        val fyCode = if (_appPrefs.value.fyStartMonth == 4) "FY 2026-27" else "CY 2026"
        coreDao.upsertSingleNumberSeries(
            NumberSeriesEntity(
                id = "${companyId}_SALE_INVOICE",
                companyId = companyId,
                docType = "SALE_INVOICE",
                prefix = invPrefix,
                fyCode = fyCode,
                nextNumber = 5L,
                paddingDigits = 4,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun allocateGapFreeDocNumber(
        companyId: String,
        docType: String,
        fallbackPrefix: String? = null
    ): String {
        val dao = step3ADao ?: return "${fallbackPrefix ?: "INV/26-27/"}0001"
        val seriesKey = SalesDocTypes.seriesDocTypeKey(docType)
        val fyCode = if (_appPrefs.value.fyStartMonth == 4) "FY 2026-27" else "CY 2026"
        val prefix = fallbackPrefix ?: if (seriesKey == "SALE_INVOICE") {
            _appPrefs.value.invoicePrefix
        } else {
            SalesDocTypes.defaultPrefix(docType)
        }
        return dao.allocateGapFreeDocNumberInTransaction(
            companyId = companyId,
            docType = seriesKey,
            fyCode = fyCode,
            fallbackPrefix = prefix,
            now = System.currentTimeMillis()
        )
    }

    suspend fun saveOrUpdateSalesDocument(
        header: TxnEntity,
        lines: List<TxnLineEntity>,
        charges: List<TxnChargeEntity>,
        taxLines: List<TxnTaxLineEntity>,
        sourceTxnIdForConversion: String? = null,
        conversionRelationship: String? = null,
        immediatePaymentReceivedPaise: Long = 0L,
        immediatePaymentModesJson: String = "CASH"
    ): TxnEntity {
        val dao = step3ADao ?: return header
        val fyCode = if (_appPrefs.value.fyStartMonth == 4) "FY 2026-27" else "CY 2026"
        val finalDocNumber = if (header.docNumber.isBlank()) {
            allocateGapFreeDocNumber(header.companyId, header.docType)
        } else {
            header.docNumber
        }
        val paymentNumber = if (immediatePaymentReceivedPaise > 0L) {
            dao.allocateGapFreeDocNumberInTransaction(
                companyId = header.companyId,
                docType = "PAYMENT_IN",
                fyCode = fyCode,
                fallbackPrefix = "RCPT/26-27/"
            )
        } else {
            null
        }
        return dao.saveOrUpdateSalesDocumentInTransaction(
            header = header.copy(docNumber = finalDocNumber),
            lines = lines,
            charges = charges,
            taxLines = taxLines,
            userId = _appPrefs.value.activeUserId,
            userName = _appPrefs.value.activeUserName,
            sourceTxnIdForConversion = sourceTxnIdForConversion,
            conversionRelationship = conversionRelationship,
            immediatePaymentReceivedPaise = immediatePaymentReceivedPaise,
            immediatePaymentModesJson = immediatePaymentModesJson,
            immediatePaymentNumber = paymentNumber
        )
    }

    suspend fun cancelSalesDocument(
        companyId: String,
        txnId: String,
        cancelReason: String
    ): TxnEntity? {
        val dao = step3ADao ?: return null
        return dao.cancelDocumentInTransaction(
            companyId = companyId,
            txnId = txnId,
            cancelReason = cancelReason,
            userId = _appPrefs.value.activeUserId,
            userName = _appPrefs.value.activeUserName
        )
    }

    /**
     * One-tap document conversion (Quotation -> Sale Order -> Delivery Challan -> Tax Invoice -> Sale Return)
     * with automatic gap-free document number allocation and txn_link audit trace.
     */
    suspend fun convertSalesDocument(
        companyId: String,
        sourceTxnId: String,
        targetDocType: String
    ): TxnEntity? {
        val dao = step3ADao ?: return null
        val source = dao.getTxnByIdNow(sourceTxnId) ?: return null
        val sourceLines = dao.getTxnLinesForTxnNow(sourceTxnId)
        val sourceCharges = dao.getTxnChargesForTxnNow(sourceTxnId)
        val sourceTaxLines = dao.getTxnTaxLinesForTxnNow(sourceTxnId)

        val now = System.currentTimeMillis()
        val newTxnId = UUID.randomUUID().toString()
        val newDocNumber = allocateGapFreeDocNumber(companyId, targetDocType)
        val relationship = when {
            source.docType == SalesDocTypes.ESTIMATE && targetDocType == SalesDocTypes.SALE_ORDER -> "QUOTATION_TO_ORDER"
            source.docType == SalesDocTypes.ESTIMATE && targetDocType == SalesDocTypes.TAX_INVOICE -> "QUOTATION_TO_INVOICE"
            source.docType == SalesDocTypes.SALE_ORDER && targetDocType == SalesDocTypes.DELIVERY_CHALLAN -> "ORDER_TO_CHALLAN"
            source.docType == SalesDocTypes.SALE_ORDER && targetDocType == SalesDocTypes.TAX_INVOICE -> "ORDER_TO_INVOICE"
            source.docType == SalesDocTypes.DELIVERY_CHALLAN && targetDocType == SalesDocTypes.TAX_INVOICE -> "CHALLAN_TO_INVOICE"
            targetDocType == SalesDocTypes.CREDIT_NOTE -> "INVOICE_TO_RETURN"
            else -> "${source.docType}_TO_$targetDocType"
        }

        val isReturn = targetDocType == SalesDocTypes.CREDIT_NOTE
        val newHeader = source.copy(
            id = newTxnId,
            docType = targetDocType,
            docNumber = newDocNumber,
            txnDate = now,
            dueDate = now + 15L * 86_400_000L,
            paidAmountPaise = if (isReturn) source.grandTotalPaise else 0L,
            balanceAmountPaise = if (isReturn) 0L else source.grandTotalPaise,
            grossProfitPaise = if (isReturn) -source.grossProfitPaise else source.grossProfitPaise,
            status = if (isReturn) "Paid" else "Unpaid",
            notes = "Converted from ${source.docNumber}",
            createdAt = now,
            updatedAt = now
        )
        val newLines = sourceLines.map {
            it.copy(
                id = UUID.randomUUID().toString(),
                txnId = newTxnId,
                createdAt = now,
                updatedAt = now
            )
        }
        val newCharges = sourceCharges.map {
            it.copy(
                id = UUID.randomUUID().toString(),
                txnId = newTxnId,
                createdAt = now,
                updatedAt = now
            )
        }
        val newTaxLines = sourceTaxLines.map {
            it.copy(
                id = UUID.randomUUID().toString(),
                txnId = newTxnId,
                createdAt = now,
                updatedAt = now
            )
        }

        return dao.saveOrUpdateSalesDocumentInTransaction(
            header = newHeader,
            lines = newLines,
            charges = newCharges,
            taxLines = newTaxLines,
            userId = _appPrefs.value.activeUserId,
            userName = _appPrefs.value.activeUserName,
            sourceTxnIdForConversion = source.id,
            conversionRelationship = relationship
        )
    }

    suspend fun recordPaymentWithAllocations(
        payment: PaymentEntity,
        allocations: List<PaymentAllocationEntity>,
        cheque: ChequeEntity? = null
    ): PaymentEntity {
        val dao = step3ADao ?: return payment
        val finalPaymentNo = if (payment.paymentNumber.isBlank()) {
            allocateGapFreeDocNumber(payment.companyId, SalesDocTypes.PAYMENT_IN, "RCPT/26-27/")
        } else {
            payment.paymentNumber
        }
        return dao.recordPaymentWithAllocationsInTransaction(
            payment = payment.copy(paymentNumber = finalPaymentNo),
            allocations = allocations,
            cheque = cheque,
            userId = _appPrefs.value.activeUserId,
            userName = _appPrefs.value.activeUserName
        )
    }

    suspend fun updateChequeStatus(
        companyId: String,
        chequeId: String,
        newStatus: String,
        bounceChargesPaise: Long = 0L,
        bounceReason: String = ""
    ): ChequeEntity? {
        val dao = step3ADao ?: return null
        return dao.updateChequeStatusInTransaction(
            companyId = companyId,
            chequeId = chequeId,
            newStatus = newStatus,
            bounceChargesPaise = bounceChargesPaise,
            bounceReason = bounceReason,
            userId = _appPrefs.value.activeUserId,
            userName = _appPrefs.value.activeUserName
        )
    }

    suspend fun saveRecurringTemplate(template: RecurringTemplateEntity) {
        val dao = step3ADao ?: return
        val now = System.currentTimeMillis()
        dao.upsertRecurringTemplate(template.copy(updatedAt = now))
        coreDao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                companyId = template.companyId,
                userId = _appPrefs.value.activeUserId,
                userName = _appPrefs.value.activeUserName,
                action = "RECURRING_TEMPLATE_SAVED",
                entityType = "recurring_template",
                entityId = template.id,
                summary = "Saved recurring template '${template.templateTitle}' (${template.frequency}) for ${template.partyName}",
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun runRecurringTemplateNow(companyId: String, templateId: String): TxnEntity? {
        val dao = step3ADao ?: return null
        val template = dao.getRecurringTemplatesNow(companyId).firstOrNull { it.id == templateId } ?: return null
        val activeCompany = coreDao.getCompaniesNow().firstOrNull { it.id == companyId }
        val supplierState = activeCompany?.stateCode?.ifBlank { "27" } ?: "27"
        val isComposition = activeCompany?.gstMode == "COMPOSITION"

        val gstResult = GstCalculationEngine.calculate(
            GstDocumentInput(
                supplierStateCode = supplierState,
                placeOfSupplyStateCode = template.placeOfSupplyStateCode.ifBlank { supplierState },
                partyGstin = template.partyGstin,
                isCompositionScheme = isComposition,
                roundingMode = _appPrefs.value.roundingMode,
                lines = listOf(
                    GstLineInput(
                        lineId = "rec_line_1",
                        itemId = template.defaultItemId,
                        itemName = template.defaultItemName.ifBlank { template.templateTitle },
                        hsnSacCode = template.defaultHsnCode.ifBlank { "9983" },
                        unit = template.defaultUnit.ifBlank { "PCS" },
                        qty = template.defaultQty.coerceAtLeast(1L),
                        unitPricePaise = template.defaultRatePaise,
                        taxRateBps = template.defaultTaxRateBps
                    )
                )
            )
        )

        val now = System.currentTimeMillis()
        val txnId = UUID.randomUUID().toString()
        val docNo = allocateGapFreeDocNumber(companyId, template.docType)
        val calcLine = gstResult.lines.first()

        val header = TxnEntity(
            id = txnId,
            companyId = companyId,
            docType = template.docType,
            docNumber = docNo,
            txnDate = now,
            dueDate = now + 15L * 86_400_000L,
            partyId = template.partyId,
            partyName = template.partyName,
            partyGstin = template.partyGstin,
            placeOfSupplyStateCode = template.placeOfSupplyStateCode,
            placeOfSupplyStateName = template.placeOfSupplyStateName,
            supplyType = gstResult.supplyType.name,
            classification = gstResult.classification.name,
            isComposition = isComposition,
            grossSubtotalPaise = gstResult.grossSubtotalPaise,
            taxableSubtotalPaise = gstResult.taxableSubtotalPaise,
            cgstTotalPaise = gstResult.cgstTotalPaise,
            sgstTotalPaise = gstResult.sgstTotalPaise,
            igstTotalPaise = gstResult.igstTotalPaise,
            cessTotalPaise = gstResult.cessTotalPaise,
            totalTaxPaise = gstResult.totalTaxPaise,
            roundOffPaise = gstResult.roundOffPaise,
            roundingMode = _appPrefs.value.roundingMode.name,
            grandTotalPaise = gstResult.grandTotalPaise,
            paidAmountPaise = 0L,
            balanceAmountPaise = gstResult.grandTotalPaise,
            totalCostPaise = gstResult.totalCostPaise,
            grossProfitPaise = gstResult.grossProfitPaise,
            status = "Unpaid",
            notes = "Auto-generated from recurring template: ${template.templateTitle}",
            amountInWords = gstResult.amountInWords,
            recurringTemplateId = template.id,
            createdAt = now,
            updatedAt = now
        )

        val lineEntity = TxnLineEntity(
            id = UUID.randomUUID().toString(),
            companyId = companyId,
            txnId = txnId,
            lineIndex = 0,
            itemId = calcLine.itemId,
            itemName = calcLine.itemName,
            hsnSacCode = calcLine.hsnSacCode,
            qty = calcLine.qty,
            unit = calcLine.unit,
            unitPricePaise = calcLine.unitPricePaise,
            taxRateBps = calcLine.taxRateBps,
            taxablePaise = calcLine.netTaxablePaise,
            cgstPaise = calcLine.cgstPaise,
            sgstPaise = calcLine.sgstPaise,
            igstPaise = calcLine.igstPaise,
            cessPaise = calcLine.cessPaise,
            lineTotalPaise = calcLine.lineTotalPaise,
            createdAt = now,
            updatedAt = now
        )

        val taxEntities = gstResult.hsnSummary.map { hsn ->
            TxnTaxLineEntity(
                id = UUID.randomUUID().toString(),
                companyId = companyId,
                txnId = txnId,
                hsnSacCode = hsn.hsnSacCode,
                description = hsn.description,
                uqc = hsn.uqc,
                totalQty = hsn.totalQty,
                taxablePaise = hsn.taxablePaise,
                taxRateBps = hsn.taxRateBps,
                cgstPaise = hsn.cgstPaise,
                sgstPaise = hsn.sgstPaise,
                igstPaise = hsn.igstPaise,
                cessPaise = hsn.cessPaise,
                totalTaxPaise = hsn.totalTaxPaise,
                createdAt = now,
                updatedAt = now
            )
        }

        val saved = dao.saveOrUpdateSalesDocumentInTransaction(
            header = header,
            lines = listOf(lineEntity),
            charges = emptyList(),
            taxLines = taxEntities,
            userId = _appPrefs.value.activeUserId,
            userName = _appPrefs.value.activeUserName
        )

        val nextDate = now + template.intervalDays.coerceAtLeast(1) * 86_400_000L
        dao.upsertRecurringTemplate(
            template.copy(
                lastGeneratedTxnId = saved.id,
                nextRunDate = nextDate,
                updatedAt = now
            )
        )

        if (template.autoNotify) {
            RecurringInvoiceNotificationHelper.postRecurringGeneratedNotification(
                context = context,
                templateTitle = template.templateTitle,
                docNumber = saved.docNumber,
                partyName = saved.partyName,
                amount = Money(saved.grandTotalPaise)
            )
        }
        return saved
    }

    suspend fun updateTaxRate(taxRate: TaxRateEntity) {
        coreDao.upsertSingleTaxRate(taxRate.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun updateRolePermission(permission: RolePermissionEntity) {
        coreDao.updateRolePermission(permission.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun updateInvoicePrefix(prefix: String) {
        updateInvoiceSettings(
            prefix = prefix,
            startingNumber = _appPrefs.value.invoiceStartingNumber,
            fyStartMonth = _appPrefs.value.fyStartMonth
        )
    }

    suspend fun resetEntireApp() {
        step3ADao?.let { dao ->
            dao.clearRecurringTemplates()
            dao.clearCheques()
            dao.clearPaymentAllocations()
            dao.clearPayments()
            dao.clearTxnLinks()
            dao.clearTxnTaxLines()
            dao.clearTxnCharges()
            dao.clearTxnLines()
            dao.clearTransactions()
        }
        step2Dao?.let { dao ->
            dao.clearPriceListItems()
            dao.clearPriceLists()
            dao.clearItemBundles()
            dao.clearStockTransfers()
            dao.clearStockAdjustments()
            dao.clearStockLedger()
            dao.clearWarehouses()
            dao.clearItemVariants()
            dao.clearItemSerials()
            dao.clearItemBatches()
            dao.clearUnitConversions()
            dao.clearItemUnits()
            dao.clearItemCategories()
            dao.clearItems()
            dao.clearPartyCustomFields()
            dao.clearPartyLedgerEntries()
            dao.clearPartyGroups()
            dao.clearPartyAddresses()
            dao.clearParties()
        }
        coreDao.clearAuditLogs()
        coreDao.clearAttachments()
        coreDao.clearHsnMaster()
        coreDao.clearTaxRates()
        coreDao.clearNumberSeries()
        coreDao.clearFinancialYears()
        coreDao.clearSettings()
        coreDao.clearRolePermissions()
        coreDao.clearUsers()
        coreDao.clearCompanies()
        prefs.edit().clear().commit()
        runCatching { context.vanijyaDataStore.edit { it.clear() } }
        _appPrefs.value = AppPreferencesState()
    }
}
