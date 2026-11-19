package com.example

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.AppPreferencesState
import com.example.core.database.AppUserEntity
import com.example.core.database.AuditLogEntity
import com.example.core.database.AutoLockTimeout
import com.example.core.database.CompanyEntity
import com.example.core.database.CoreRepository
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
import com.example.core.database.PriceListEntity
import com.example.core.database.PriceListItemEntity
import com.example.core.database.RolePermissionEntity
import com.example.core.database.RoundingModeOption
import com.example.core.database.StockAdjustmentEntity
import com.example.core.database.StockLedgerEntity
import com.example.core.database.StockTransferEntity
import com.example.core.database.ChequeEntity
import com.example.core.database.NumberSeriesEntity
import com.example.core.database.PaymentAllocationEntity
import com.example.core.database.PaymentEntity
import com.example.core.database.RecurringTemplateEntity
import com.example.core.database.TaxRateEntity
import com.example.core.database.ThemeMode
import com.example.core.database.TxnChargeEntity
import com.example.core.database.TxnEntity
import com.example.core.database.TxnLineEntity
import com.example.core.database.TxnLinkEntity
import com.example.core.database.TxnTaxLineEntity
import com.example.core.database.UnitConversionEntity
import com.example.core.database.WarehouseEntity
import com.example.core.model.AppRole
import com.example.core.model.ItemImportPreviewRow
import com.example.core.model.PartyImportPreviewRow
import com.example.feature.onboarding.OnboardingFormData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class VanijyaViewModel @Inject constructor(
    private val repository: CoreRepository
) : ViewModel() {

    val appPrefs: StateFlow<AppPreferencesState> = repository.appPrefs

    val companies: StateFlow<List<CompanyEntity>> = repository.observeCompanies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val users: StateFlow<List<AppUserEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeUsers(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val taxRates: StateFlow<List<TaxRateEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeTaxRates(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val rolePermissions: StateFlow<List<RolePermissionEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeRolePermissions(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val hsnMasterList: StateFlow<List<HsnMasterEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeHsnMaster(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentAuditLogs: StateFlow<List<AuditLogEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeRecentAuditLogs(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val numberSeries: StateFlow<List<NumberSeriesEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeNumberSeries(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ========================================================================
    // STEP 2 FLOWS: PARTIES & KHATA LEDGERS
    // ========================================================================

    val parties: StateFlow<List<PartyEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeParties(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val partyAddresses: StateFlow<List<PartyAddressEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observePartyAddresses(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val partyGroups: StateFlow<List<PartyGroupEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observePartyGroups(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val partyCustomFields: StateFlow<List<PartyCustomFieldEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observePartyCustomFields(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val partyLedgerEntries: StateFlow<List<PartyLedgerEntryEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeAllPartyLedgerEntries(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ========================================================================
    // STEP 2 FLOWS: ITEMS, STOCK, BATCHES, GODOWNS & PRICE LISTS
    // ========================================================================

    val items: StateFlow<List<ItemEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeItems(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val itemCategories: StateFlow<List<ItemCategoryEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeItemCategories(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val itemUnits: StateFlow<List<ItemUnitEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeItemUnits(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val unitConversions: StateFlow<List<UnitConversionEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeUnitConversions(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val itemBatches: StateFlow<List<ItemBatchEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeItemBatches(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val itemSerials: StateFlow<List<ItemSerialEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeItemSerials(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val itemVariants: StateFlow<List<ItemVariantEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeItemVariants(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val warehouses: StateFlow<List<WarehouseEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeWarehouses(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val stockLedger: StateFlow<List<StockLedgerEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeStockLedger(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val stockAdjustments: StateFlow<List<StockAdjustmentEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeStockAdjustments(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val stockTransfers: StateFlow<List<StockTransferEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeStockTransfers(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val itemBundles: StateFlow<List<ItemBundleEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeItemBundles(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val priceLists: StateFlow<List<PriceListEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observePriceLists(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val priceListItems: StateFlow<List<PriceListItemEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observePriceListItems(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ========================================================================
    // STEP 3A FLOWS: GST SALES DOCUMENTS, CONVERSIONS, PAYMENTS & CHEQUES
    // ========================================================================

    val transactions: StateFlow<List<TxnEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeTransactions(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val txnLines: StateFlow<List<TxnLineEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeTxnLines(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val txnCharges: StateFlow<List<TxnChargeEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeTxnCharges(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val txnTaxLines: StateFlow<List<TxnTaxLineEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeTxnTaxLines(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val txnLinks: StateFlow<List<TxnLinkEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeTxnLinks(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val payments: StateFlow<List<PaymentEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observePayments(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val paymentAllocations: StateFlow<List<PaymentAllocationEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observePaymentAllocations(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val cheques: StateFlow<List<ChequeEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeCheques(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recurringTemplates: StateFlow<List<RecurringTemplateEntity>> = appPrefs
        .flatMapLatest { prefs ->
            if (prefs.activeCompanyId.isBlank()) flowOf(emptyList())
            else repository.observeRecurringTemplates(prefs.activeCompanyId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isVaultLocked = MutableStateFlow(repository.appPrefs.value.onboardingCompleted)
    val isVaultLocked: StateFlow<Boolean> = _isVaultLocked.asStateFlow()

    private val _pinErrorMessage = MutableStateFlow<String?>(null)
    val pinErrorMessage: StateFlow<String?> = _pinErrorMessage.asStateFlow()

    private var lastBackgroundTimestampMs: Long = 0L

    fun onAppBackgrounded() {
        lastBackgroundTimestampMs = System.currentTimeMillis()
        if (appPrefs.value.onboardingCompleted &&
            appPrefs.value.autoLockTimeout == AutoLockTimeout.IMMEDIATELY
        ) {
            _isVaultLocked.value = true
        }
    }

    fun onAppResumed() {
        val prefs = appPrefs.value
        if (!prefs.onboardingCompleted || lastBackgroundTimestampMs == 0L) return
        val elapsed = System.currentTimeMillis() - lastBackgroundTimestampMs
        if (elapsed >= prefs.autoLockTimeout.timeoutMs) {
            _isVaultLocked.value = true
        }
    }

    fun lockVaultNow() {
        _isVaultLocked.value = true
    }

    fun unlockWithBiometric() {
        _pinErrorMessage.value = null
        _isVaultLocked.value = false
    }

    fun unlockWithPin(pin: String) {
        viewModelScope.launch {
            val matchedUser = repository.switchUserByPin(pin)
            if (matchedUser != null) {
                _pinErrorMessage.value = null
                _isVaultLocked.value = false
            } else {
                _pinErrorMessage.value = "Incorrect 6-digit PIN. Try your Owner PIN or Role PIN."
            }
        }
    }

    fun clearPinError() {
        _pinErrorMessage.value = null
    }

    fun completeOnboarding(form: OnboardingFormData) {
        viewModelScope.launch {
            repository.createBusinessWithDefaults(
                businessName = form.businessName,
                legalName = form.legalName,
                address = form.address,
                phone = form.phone,
                email = form.email,
                gstin = form.gstin,
                pan = form.pan,
                stateCode = form.stateCode,
                stateName = form.stateName,
                businessType = form.businessType,
                gstMode = form.gstMode,
                fyStartMonth = form.fyStartMonth,
                upiId = form.upiId,
                bankName = form.bankName,
                bankAccountNo = form.bankAccountNo,
                bankIfsc = form.bankIfsc,
                logoUri = form.logoUri,
                signatureUri = form.signatureUri,
                ownerPin = form.ownerPin,
                biometricEnabled = form.biometricEnabled,
                languageCode = form.languageCode
            )
            _isVaultLocked.value = true
        }
    }

    fun applyLanguage(languageCode: String) {
        repository.updatePreferences { it.copy(languageCode = languageCode) }
        val locales = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(locales)
    }

    fun toggleHideAmounts() {
        repository.updatePreferences { it.copy(hideAmounts = !it.hideAmounts) }
    }

    fun switchCompany(company: CompanyEntity) {
        viewModelScope.launch {
            repository.switchCompany(company)
        }
    }

    fun createAdditionalCompany(name: String, gstin: String) {
        viewModelScope.launch {
            val current = appPrefs.value
            repository.createBusinessWithDefaults(
                businessName = name,
                legalName = name,
                address = "",
                phone = "",
                email = "",
                gstin = gstin,
                pan = "",
                stateCode = "27",
                stateName = "Maharashtra",
                businessType = "Retail & Wholesale",
                gstMode = "REGULAR_GST",
                fyStartMonth = 4,
                upiId = "",
                bankName = "",
                bankAccountNo = "",
                bankIfsc = "",
                logoUri = "",
                signatureUri = "",
                ownerPin = "123456",
                biometricEnabled = current.biometricEnabled,
                languageCode = current.languageCode
            )
        }
    }

    fun switchUserByPin(pin: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.switchUserByPin(pin)
            if (user != null) {
                onResult(true, "Switched to ${user.fullName}")
            } else {
                onResult(false, "No user found matching that 6-digit PIN")
            }
        }
    }

    fun saveBusinessProfile(updatedCompany: CompanyEntity) {
        viewModelScope.launch {
            repository.updateCompanyProfile(updatedCompany)
        }
    }

    fun saveInvoiceSettings(prefix: String, startingNumber: Long, fyStartMonth: Int) {
        viewModelScope.launch {
            repository.updateInvoiceSettings(prefix, startingNumber, fyStartMonth)
        }
    }

    fun updateRoundingMode(mode: RoundingModeOption) {
        repository.updatePreferences {
            it.copy(
                roundingMode = mode,
                roundingNearestRupee = mode == RoundingModeOption.NEAREST
            )
        }
    }

    fun updateDecimals(decimals: Int) {
        repository.updatePreferences { it.copy(decimalPlaces = decimals) }
    }

    fun updateDateFormat(pattern: String) {
        repository.updatePreferences { it.copy(dateFormatPattern = pattern) }
    }

    fun updateTheme(mode: ThemeMode) {
        repository.updatePreferences { it.copy(themeMode = mode) }
    }

    fun updateAutoLock(timeout: AutoLockTimeout) {
        repository.updatePreferences { it.copy(autoLockTimeout = timeout) }
    }

    fun updateHideAmounts(hide: Boolean) {
        repository.updatePreferences { it.copy(hideAmounts = hide) }
    }

    fun updateBlockScreenshots(block: Boolean) {
        repository.updatePreferences { it.copy(blockScreenshots = block) }
    }

    fun updateBiometricEnabled(enabled: Boolean) {
        repository.updatePreferences { it.copy(biometricEnabled = enabled) }
    }

    fun changePin(currentPin: String, newPin: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val ok = repository.changeActiveUserPin(currentPin, newPin)
            if (ok) {
                onResult(true, "PIN updated")
            } else {
                onResult(false, "Current 6-digit PIN is incorrect")
            }
        }
    }

    fun addLocalUser(name: String, role: AppRole, pin: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val created = repository.addLocalUser(name, role, pin)
            if (created != null) {
                onResult(true, "Added ${created.fullName}")
            } else {
                onResult(false, "Could not add user. Check name and 6-digit PIN.")
            }
        }
    }

    fun updateTaxRate(rate: TaxRateEntity) {
        viewModelScope.launch {
            repository.updateTaxRate(rate)
        }
    }

    fun updateRolePermission(permission: RolePermissionEntity) {
        viewModelScope.launch {
            repository.updateRolePermission(permission)
        }
    }

    fun verifyPinOnly(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val matched = repository.verifyPinForActiveCompany(pin)
            onResult(matched != null)
        }
    }

    fun verifyPinAndResetApp(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val matched = repository.verifyPinForActiveCompany(pin)
            if (matched != null) {
                repository.resetEntireApp()
                _isVaultLocked.value = false
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun resetAppAfterBiometric() {
        viewModelScope.launch {
            repository.resetEntireApp()
            _isVaultLocked.value = false
        }
    }

    // ========================================================================
    // STEP 2 VIEWMODEL ACTIONS
    // ========================================================================

    fun saveParty(
        party: PartyEntity,
        addresses: List<PartyAddressEntity>,
        customFields: List<PartyCustomFieldEntity>
    ) {
        viewModelScope.launch {
            repository.savePartyWithDetails(party, addresses, customFields)
        }
    }

    fun recordPartyLedgerMovement(entry: PartyLedgerEntryEntity) {
        viewModelScope.launch {
            repository.recordPartyLedgerMovement(entry)
        }
    }

    fun deleteParties(partyIds: List<String>) {
        val companyId = appPrefs.value.activeCompanyId
        if (companyId.isBlank() || partyIds.isEmpty()) return
        viewModelScope.launch {
            repository.deleteParties(companyId, partyIds)
        }
    }

    fun bulkImportParties(rows: List<PartyImportPreviewRow>, onResult: (Int) -> Unit = {}) {
        val companyId = appPrefs.value.activeCompanyId
        if (companyId.isBlank()) return
        viewModelScope.launch {
            val count = repository.bulkImportParties(companyId, rows)
            onResult(count)
        }
    }

    fun savePartyGroup(name: String, description: String, discountBps: Int) {
        val companyId = appPrefs.value.activeCompanyId
        if (companyId.isBlank()) return
        viewModelScope.launch {
            repository.savePartyGroup(companyId, name, description, discountBps)
        }
    }

    fun saveItem(
        item: ItemEntity,
        initialBatch: ItemBatchEntity? = null,
        variants: List<ItemVariantEntity> = emptyList(),
        serials: List<ItemSerialEntity> = emptyList(),
        bundles: List<ItemBundleEntity> = emptyList()
    ) {
        viewModelScope.launch {
            repository.saveItemWithDetails(item, initialBatch, variants, serials, bundles)
        }
    }

    fun recordStockAdjustment(adjustment: StockAdjustmentEntity) {
        viewModelScope.launch {
            repository.recordStockAdjustment(adjustment)
        }
    }

    fun recordStockTransfer(transfer: StockTransferEntity) {
        viewModelScope.launch {
            repository.recordStockTransfer(transfer)
        }
    }

    fun bulkUpdateItemPricesAndTax(
        itemIds: List<String>,
        priceChangeBps: Int,
        newTaxRateBps: Int?,
        onResult: (Int) -> Unit = {}
    ) {
        val companyId = appPrefs.value.activeCompanyId
        if (companyId.isBlank()) return
        viewModelScope.launch {
            val updated = repository.bulkUpdateItemPricesAndTax(companyId, itemIds, priceChangeBps, newTaxRateBps)
            onResult(updated)
        }
    }

    fun bulkImportItems(rows: List<ItemImportPreviewRow>, onResult: (Int) -> Unit = {}) {
        val companyId = appPrefs.value.activeCompanyId
        if (companyId.isBlank()) return
        viewModelScope.launch {
            val count = repository.bulkImportItems(companyId, rows)
            onResult(count)
        }
    }

    fun saveItemCategory(name: String, description: String) {
        val companyId = appPrefs.value.activeCompanyId
        if (companyId.isBlank()) return
        viewModelScope.launch {
            repository.saveItemCategory(companyId, name, description)
        }
    }

    fun saveWarehouse(
        name: String,
        code: String,
        address: String,
        inchargeName: String,
        phone: String
    ) {
        val companyId = appPrefs.value.activeCompanyId
        if (companyId.isBlank()) return
        viewModelScope.launch {
            repository.saveWarehouse(companyId, name, code, address, inchargeName, phone)
        }
    }

    // ========================================================================
    // STEP 3A VIEWMODEL ACTIONS: SALES DOCUMENTS, CONVERSIONS, PAYMENTS & CHEQUES
    // ========================================================================

    fun saveOrUpdateSalesDocument(
        header: TxnEntity,
        lines: List<TxnLineEntity>,
        charges: List<TxnChargeEntity>,
        taxLines: List<TxnTaxLineEntity>,
        sourceTxnIdForConversion: String? = null,
        conversionRelationship: String? = null,
        immediatePaymentReceivedPaise: Long = 0L,
        immediatePaymentModesJson: String = "CASH",
        onSaved: (TxnEntity) -> Unit = {}
    ) {
        viewModelScope.launch {
            val saved = repository.saveOrUpdateSalesDocument(
                header = header,
                lines = lines,
                charges = charges,
                taxLines = taxLines,
                sourceTxnIdForConversion = sourceTxnIdForConversion,
                conversionRelationship = conversionRelationship,
                immediatePaymentReceivedPaise = immediatePaymentReceivedPaise,
                immediatePaymentModesJson = immediatePaymentModesJson
            )
            onSaved(saved)
        }
    }

    fun cancelSalesDocument(
        txnId: String,
        cancelReason: String,
        onCancelled: (TxnEntity?) -> Unit = {}
    ) {
        val companyId = appPrefs.value.activeCompanyId
        if (companyId.isBlank()) return
        viewModelScope.launch {
            val res = repository.cancelSalesDocument(companyId, txnId, cancelReason)
            onCancelled(res)
        }
    }

    fun convertSalesDocument(
        sourceTxnId: String,
        targetDocType: String,
        onConverted: (TxnEntity?) -> Unit = {}
    ) {
        val companyId = appPrefs.value.activeCompanyId
        if (companyId.isBlank()) return
        viewModelScope.launch {
            val converted = repository.convertSalesDocument(companyId, sourceTxnId, targetDocType)
            onConverted(converted)
        }
    }

    fun recordPaymentWithAllocations(
        payment: PaymentEntity,
        allocations: List<PaymentAllocationEntity>,
        cheque: ChequeEntity? = null,
        onSaved: (PaymentEntity) -> Unit = {}
    ) {
        viewModelScope.launch {
            val saved = repository.recordPaymentWithAllocations(payment, allocations, cheque)
            onSaved(saved)
        }
    }

    fun updateChequeStatus(
        chequeId: String,
        newStatus: String,
        bounceChargesPaise: Long = 0L,
        bounceReason: String = "",
        onUpdated: (ChequeEntity?) -> Unit = {}
    ) {
        val companyId = appPrefs.value.activeCompanyId
        if (companyId.isBlank()) return
        viewModelScope.launch {
            val updated = repository.updateChequeStatus(
                companyId = companyId,
                chequeId = chequeId,
                newStatus = newStatus,
                bounceChargesPaise = bounceChargesPaise,
                bounceReason = bounceReason
            )
            onUpdated(updated)
        }
    }

    fun saveRecurringTemplate(template: RecurringTemplateEntity) {
        viewModelScope.launch {
            repository.saveRecurringTemplate(template)
        }
    }

    fun runRecurringTemplateNow(
        templateId: String,
        onGenerated: (TxnEntity?) -> Unit = {}
    ) {
        val companyId = appPrefs.value.activeCompanyId
        if (companyId.isBlank()) return
        viewModelScope.launch {
            val generated = repository.runRecurringTemplateNow(companyId, templateId)
            onGenerated(generated)
        }
    }

    companion object {
        fun provideFactory(repository: CoreRepository): androidx.lifecycle.ViewModelProvider.Factory =
            object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return VanijyaViewModel(repository) as T
                }
            }
    }
}
