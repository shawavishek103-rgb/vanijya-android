package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.navigation.VanijyaMainShell
import com.example.feature.onboarding.OnboardingScreen
import com.example.feature.security.LockScreen
import com.example.ui.theme.VanijyaTheme

class MainActivity : AppCompatActivity() {

    private val viewModel: VanijyaViewModel by viewModels {
        val app = application as VanijyaApp
        VanijyaViewModel.provideFactory(app.coreRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val prefs by viewModel.appPrefs.collectAsStateWithLifecycle()
            val companies by viewModel.companies.collectAsStateWithLifecycle()
            val users by viewModel.users.collectAsStateWithLifecycle()
            val taxRates by viewModel.taxRates.collectAsStateWithLifecycle()
            val rolePermissions by viewModel.rolePermissions.collectAsStateWithLifecycle()
            val hsnMasterList by viewModel.hsnMasterList.collectAsStateWithLifecycle()
            val recentAuditLogs by viewModel.recentAuditLogs.collectAsStateWithLifecycle()
            val isLocked by viewModel.isVaultLocked.collectAsStateWithLifecycle()
            val pinError by viewModel.pinErrorMessage.collectAsStateWithLifecycle()

            // Step 2: Parties & Items / Stock Flows
            val parties by viewModel.parties.collectAsStateWithLifecycle()
            val partyAddresses by viewModel.partyAddresses.collectAsStateWithLifecycle()
            val partyGroups by viewModel.partyGroups.collectAsStateWithLifecycle()
            val partyCustomFields by viewModel.partyCustomFields.collectAsStateWithLifecycle()
            val partyLedgerEntries by viewModel.partyLedgerEntries.collectAsStateWithLifecycle()
            val items by viewModel.items.collectAsStateWithLifecycle()
            val itemCategories by viewModel.itemCategories.collectAsStateWithLifecycle()
            val itemUnits by viewModel.itemUnits.collectAsStateWithLifecycle()
            val unitConversions by viewModel.unitConversions.collectAsStateWithLifecycle()
            val itemBatches by viewModel.itemBatches.collectAsStateWithLifecycle()
            val itemSerials by viewModel.itemSerials.collectAsStateWithLifecycle()
            val itemVariants by viewModel.itemVariants.collectAsStateWithLifecycle()
            val warehouses by viewModel.warehouses.collectAsStateWithLifecycle()
            val stockLedger by viewModel.stockLedger.collectAsStateWithLifecycle()
            val itemBundles by viewModel.itemBundles.collectAsStateWithLifecycle()
            val priceLists by viewModel.priceLists.collectAsStateWithLifecycle()

            // Step 3A: GST Sales Documents, Conversions, Payments, Cheques & Recurring Templates
            val numberSeries by viewModel.numberSeries.collectAsStateWithLifecycle()
            val transactions by viewModel.transactions.collectAsStateWithLifecycle()
            val txnLines by viewModel.txnLines.collectAsStateWithLifecycle()
            val txnCharges by viewModel.txnCharges.collectAsStateWithLifecycle()
            val txnTaxLines by viewModel.txnTaxLines.collectAsStateWithLifecycle()
            val txnLinks by viewModel.txnLinks.collectAsStateWithLifecycle()
            val payments by viewModel.payments.collectAsStateWithLifecycle()
            val paymentAllocations by viewModel.paymentAllocations.collectAsStateWithLifecycle()
            val cheques by viewModel.cheques.collectAsStateWithLifecycle()
            val recurringTemplates by viewModel.recurringTemplates.collectAsStateWithLifecycle()

            LaunchedEffect(prefs.blockScreenshots) {
                if (prefs.blockScreenshots) {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_STOP -> viewModel.onAppBackgrounded()
                        Lifecycle.Event.ON_START -> viewModel.onAppResumed()
                        else -> Unit
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            val activeCompany = companies.firstOrNull { it.id == prefs.activeCompanyId }
                ?: companies.firstOrNull()

            val rootThemeMode = if (!prefs.onboardingCompleted) {
                com.example.core.database.ThemeMode.LIGHT
            } else {
                prefs.themeMode
            }

            VanijyaTheme(themeMode = rootThemeMode) {
                val screenStage = when {
                    !prefs.onboardingCompleted -> "ONBOARDING"
                    isLocked -> "LOCK_SCREEN"
                    else -> "DASHBOARD"
                }

                Crossfade(targetState = screenStage, label = "root_flow") { stage ->
                    when (stage) {
                        "ONBOARDING" -> OnboardingScreen(
                            onCompleteOnboarding = { formData ->
                                viewModel.completeOnboarding(formData)
                            },
                            onLanguageSelected = { lang ->
                                viewModel.applyLanguage(lang)
                            }
                        )

                        "LOCK_SCREEN" -> LockScreen(
                            companyName = activeCompany?.businessName ?: "Vanijya Vault",
                            activeUserName = prefs.activeUserName,
                            biometricEnabled = prefs.biometricEnabled,
                            pinErrorMessage = pinError,
                            onPinSubmitted = { pin -> viewModel.unlockWithPin(pin) },
                            onBiometricUnlocked = { viewModel.unlockWithBiometric() },
                            onClearError = { viewModel.clearPinError() }
                        )

                        else -> VanijyaMainShell(
                            prefs = prefs,
                            companies = companies,
                            activeCompany = activeCompany,
                            users = users,
                            taxRates = taxRates,
                            rolePermissions = rolePermissions,
                            hsnMasterList = hsnMasterList,
                            recentAuditLogs = recentAuditLogs,
                            onToggleHideAmounts = { viewModel.toggleHideAmounts() },
                            onLockNow = { viewModel.lockVaultNow() },
                            onSwitchCompany = { viewModel.switchCompany(it) },
                            onCreateAdditionalCompany = { name, gstin ->
                                viewModel.createAdditionalCompany(name, gstin)
                            },
                            onSwitchUserByPin = { pin, cb ->
                                viewModel.switchUserByPin(pin, cb)
                            },
                            onSaveBusinessProfile = { viewModel.saveBusinessProfile(it) },
                            onSaveInvoiceSettings = { prefix, startNo, fyMonth ->
                                viewModel.saveInvoiceSettings(prefix, startNo, fyMonth)
                            },
                            onUpdateRoundingMode = { viewModel.updateRoundingMode(it) },
                            onUpdateDecimals = { viewModel.updateDecimals(it) },
                            onUpdateDateFormat = { viewModel.updateDateFormat(it) },
                            onUpdateTheme = { viewModel.updateTheme(it) },
                            onUpdateLanguage = { viewModel.applyLanguage(it) },
                            onUpdateAutoLock = { viewModel.updateAutoLock(it) },
                            onUpdateHideAmounts = { viewModel.updateHideAmounts(it) },
                            onUpdateBlockScreenshots = { viewModel.updateBlockScreenshots(it) },
                            onUpdateBiometricEnabled = { viewModel.updateBiometricEnabled(it) },
                            onChangePin = { cur, next, cb -> viewModel.changePin(cur, next, cb) },
                            onAddLocalUser = { name, role, pin, cb ->
                                viewModel.addLocalUser(name, role, pin, cb)
                            },
                            onUpdateTaxRate = { viewModel.updateTaxRate(it) },
                            onUpdateRolePermission = { viewModel.updateRolePermission(it) },
                            onVerifyPin = { pin, cb -> viewModel.verifyPinOnly(pin, cb) },
                            onVerifyPinAndResetApp = { pin, cb ->
                                viewModel.verifyPinAndResetApp(pin, cb)
                            },
                            onBiometricResetApp = { viewModel.resetAppAfterBiometric() },
                            parties = parties,
                            partyAddresses = partyAddresses,
                            partyGroups = partyGroups,
                            partyCustomFields = partyCustomFields,
                            partyLedgerEntries = partyLedgerEntries,
                            items = items,
                            itemCategories = itemCategories,
                            itemUnits = itemUnits,
                            unitConversions = unitConversions,
                            itemBatches = itemBatches,
                            itemSerials = itemSerials,
                            itemVariants = itemVariants,
                            warehouses = warehouses,
                            stockLedger = stockLedger,
                            itemBundles = itemBundles,
                            priceLists = priceLists,
                            onSaveParty = { party, addresses, customFields ->
                                viewModel.saveParty(party, addresses, customFields)
                            },
                            onRecordPartyLedgerMovement = { entry ->
                                viewModel.recordPartyLedgerMovement(entry)
                            },
                            onDeleteParties = { ids ->
                                viewModel.deleteParties(ids)
                            },
                            onBulkImportParties = { rows, cb ->
                                viewModel.bulkImportParties(rows, cb)
                            },
                            onSaveItem = { item, initialBatch, variants, serials, bundles ->
                                viewModel.saveItem(item, initialBatch, variants, serials, bundles)
                            },
                            onRecordStockAdjustment = { adjustment ->
                                viewModel.recordStockAdjustment(adjustment)
                            },
                            onRecordStockTransfer = { transfer ->
                                viewModel.recordStockTransfer(transfer)
                            },
                            onBulkUpdateItemPricesAndTax = { itemIds, priceChangeBps, newTaxBps, cb ->
                                viewModel.bulkUpdateItemPricesAndTax(itemIds, priceChangeBps, newTaxBps, cb)
                            },
                            onBulkImportItems = { rows, cb ->
                                viewModel.bulkImportItems(rows, cb)
                            },
                            onSaveItemCategory = { name, desc ->
                                viewModel.saveItemCategory(name, desc)
                            },
                            onSaveWarehouse = { name, code, addr, incharge, phone ->
                                viewModel.saveWarehouse(name, code, addr, incharge, phone)
                            },
                            numberSeries = numberSeries,
                            transactions = transactions,
                            txnLines = txnLines,
                            txnCharges = txnCharges,
                            txnTaxLines = txnTaxLines,
                            txnLinks = txnLinks,
                            payments = payments,
                            paymentAllocations = paymentAllocations,
                            cheques = cheques,
                            recurringTemplates = recurringTemplates,
                            onSaveOrUpdateDocument = { header, lines, charges, taxLines, srcId, rel, paidNow, modesJson ->
                                viewModel.saveOrUpdateSalesDocument(
                                    header = header,
                                    lines = lines,
                                    charges = charges,
                                    taxLines = taxLines,
                                    sourceTxnIdForConversion = srcId,
                                    conversionRelationship = rel,
                                    immediatePaymentReceivedPaise = paidNow,
                                    immediatePaymentModesJson = modesJson
                                )
                            },
                            onCancelDocument = { txnId, reason ->
                                viewModel.cancelSalesDocument(txnId, reason)
                            },
                            onConvertDocument = { srcId, targetType ->
                                viewModel.convertSalesDocument(srcId, targetType)
                            },
                            onRecordPayment = { payment, allocations, cheque ->
                                viewModel.recordPaymentWithAllocations(payment, allocations, cheque)
                            },
                            onUpdateChequeStatus = { chequeId, status, penaltyPaise, reason ->
                                viewModel.updateChequeStatus(chequeId, status, penaltyPaise, reason)
                            },
                            onSaveRecurringTemplate = { tpl ->
                                viewModel.saveRecurringTemplate(tpl)
                            },
                            onRunRecurringNow = { tplId ->
                                viewModel.runRecurringTemplateNow(tplId)
                            }
                        )
                    }
                }
            }
        }
    }
}
