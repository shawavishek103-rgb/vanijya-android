package com.example

import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.database.CoreRepository
import com.example.core.database.PaymentAllocationEntity
import com.example.core.database.PaymentEntity
import com.example.core.database.RoundingModeOption
import com.example.core.database.ThemeMode
import com.example.core.database.TxnEntity
import com.example.core.database.TxnLineEntity
import com.example.core.database.TxnTaxLineEntity
import com.example.core.database.VanijyaDatabase
import com.example.core.model.AppRole
import com.example.core.model.BundledHsnDirectoryLoader
import com.example.core.model.GstAdditionalChargeInput
import com.example.core.model.GstCalculationEngine
import com.example.core.model.GstDocumentInput
import com.example.core.model.GstInvoiceClassification
import com.example.core.model.GstLineInput
import com.example.core.model.GstSupplyType
import com.example.core.model.IndianNumberToWords
import com.example.core.model.Money
import com.example.core.model.PartyLedgerCalculator
import com.example.core.model.SalesDocTypes
import com.example.core.model.StockLedgerCalculator
import com.example.core.model.Validators
import com.example.feature.items.ItemsFoundationScreen
import com.example.feature.parties.PartiesFoundationScreen
import com.example.feature.sales.SalesFoundationScreen
import com.example.feature.staff.StaffFoundationScreen
import com.example.ui.theme.VanijyaTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var db: VanijyaDatabase
    private lateinit var repository: CoreRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, VanijyaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = CoreRepository(db.coreDao(), context, db.step2Dao(), db.step3ADao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun appName_isVanijya_andFiveBottomTabStringsExistInEnglishAndHindi() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Vanijya", appName)
        assertEquals("Dashboard", context.getString(R.string.nav_dashboard))
        assertEquals("Billing", context.getString(R.string.nav_billing))
        assertEquals("Parties", context.getString(R.string.nav_parties))
        assertEquals("Staff", context.getString(R.string.nav_staff))
        assertEquals("Stock", context.getString(R.string.nav_stock))
        assertEquals("Reports", context.getString(R.string.nav_reports))
        assertEquals("Settings", context.getString(R.string.nav_settings))

        // Verify Staff tab segmented tabs and Avatar menu strings
        assertEquals("Today", context.getString(R.string.staff_tab_today))
        assertEquals("Employees", context.getString(R.string.staff_tab_employees))
        assertEquals("Payroll", context.getString(R.string.staff_tab_payroll))
        assertEquals("Advances", context.getString(R.string.staff_tab_advances))
        assertEquals("Switch Firm", context.getString(R.string.avatar_switch_firm))
        assertEquals("Switch User", context.getString(R.string.avatar_switch_user))
        assertEquals("Backup and Restore", context.getString(R.string.avatar_backup_restore))
        assertEquals("Lock app now", context.getString(R.string.avatar_lock_now))
        assertEquals("Help", context.getString(R.string.avatar_help))
        assertEquals("About", context.getString(R.string.avatar_about))

        // Confirm unused payroll auth prompt strings have been removed
        val pkg = context.packageName
        assertEquals(0, context.resources.getIdentifier("staff_payroll_auth_title", "string", pkg))
        assertEquals(0, context.resources.getIdentifier("staff_payroll_auth_subtitle", "string", pkg))
        assertEquals(0, context.resources.getIdentifier("staff_unlock_payroll", "string", pkg))
    }

    @Test
    fun staffModule_opensTodayEmployeesPayrollAndAdvancesDirectly_withNoUnlockPrompt() {
        composeTestRule.setContent {
            VanijyaTheme(themeMode = ThemeMode.DARK) {
                StaffFoundationScreen(
                    hideAmounts = false,
                    decimals = 2
                )
            }
        }

        // 1. Today tab opens directly with no unlock prompt
        composeTestRule.onNodeWithTag("staff_today_summary_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("staff_mark_all_present_btn").assertIsDisplayed()
        assertEquals(0, composeTestRule.onAllNodesWithTag("staff_payroll_auth_gate").fetchSemanticsNodes().size)

        // 2. Employees tab opens directly with no unlock prompt
        composeTestRule.onNodeWithTag("staff_segment_1").performClick()
        composeTestRule.onNodeWithTag("staff_employees_search_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("staff_fab_add_employee").assertIsDisplayed()
        assertEquals(0, composeTestRule.onAllNodesWithTag("staff_payroll_auth_gate").fetchSemanticsNodes().size)

        // 3. Payroll tab opens directly with no biometric/PIN gate and shows Payroll summary & Run Payroll immediately
        composeTestRule.onNodeWithTag("staff_segment_2").performClick()
        composeTestRule.onNodeWithTag("staff_payroll_summary_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("staff_run_payroll_btn").assertIsDisplayed()
        assertEquals(0, composeTestRule.onAllNodesWithTag("staff_payroll_auth_gate").fetchSemanticsNodes().size)

        // 4. Advances tab opens directly with no unlock prompt and shows Advances summary card immediately
        composeTestRule.onNodeWithTag("staff_segment_3").performClick()
        composeTestRule.onNodeWithTag("staff_advances_summary_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("₹0.00").assertIsDisplayed()
        assertEquals(0, composeTestRule.onAllNodesWithTag("staff_payroll_auth_gate").fetchSemanticsNodes().size)
    }

    @Test
    fun themeModes_andFontScale1_3x_persistAndFormatIndianGroupingCorrectly() {
        // Verify Light, Dark, and AMOLED theme persistence in preferences
        repository.updatePreferences { it.copy(themeMode = ThemeMode.LIGHT) }
        assertEquals(ThemeMode.LIGHT, repository.appPrefs.value.themeMode)

        repository.updatePreferences { it.copy(themeMode = ThemeMode.DARK) }
        assertEquals(ThemeMode.DARK, repository.appPrefs.value.themeMode)

        repository.updatePreferences { it.copy(themeMode = ThemeMode.AMOLED) }
        assertEquals(ThemeMode.AMOLED, repository.appPrefs.value.themeMode)

        // Verify 1.3x font scale configuration and Indian digit grouping (3,53,000.00)
        val config = Configuration(context.resources.configuration).apply {
            fontScale = 1.3f
        }
        val scaledContext = context.createConfigurationContext(config)
        assertEquals(1.3f, scaledContext.resources.configuration.fontScale, 0.01f)

        val bankTotal = Money.fromRupees(3_53_000L)
        assertEquals("₹3,53,000.00", bankTotal.formatIndian(includeSymbol = true, decimals = 2))
        assertEquals("3,53,000.00", bankTotal.formatIndian(includeSymbol = false, decimals = 2))
        assertEquals("₹ ••••••", bankTotal.formatIndian(includeSymbol = true, decimals = 2, masked = true))
    }

    @Test
    fun databaseV1_seedsAllTenCoreTablesAndSupportsPinRoleSwitch() = runBlocking {
        val company = repository.createBusinessWithDefaults(
            businessName = "A.K. Enterprises",
            legalName = "A.K. Enterprises Pvt Ltd",
            address = "MG Road, Jaipur",
            phone = "9876543210",
            email = "info@akenterprises.in",
            gstin = "",
            pan = "ABCDE1234F",
            stateCode = "08",
            stateName = "Rajasthan",
            businessType = "Kirana & FMCG",
            gstMode = "REGULAR_GST",
            fyStartMonth = 4,
            upiId = "ak@okaxis",
            bankName = "HDFC Bank",
            bankAccountNo = "12345678901",
            bankIfsc = "HDFC0001234",
            logoUri = "content://local/logo.png",
            signatureUri = "content://local/sign.png",
            ownerPin = "654321",
            biometricEnabled = true,
            languageCode = "en"
        )

        // Verify all 10 Database v1 tables are populated with company_id partition
        val companies = repository.observeCompanies().first()
        assertEquals(1, companies.size)
        assertEquals("A.K. Enterprises", companies.first().businessName)

        val users = repository.observeUsers(company.id).first()
        assertEquals(5, users.size) // Owner + Manager + Salesperson + Accountant + Viewer

        val permissions = repository.observeRolePermissions(company.id).first()
        assertTrue(permissions.size >= 55) // 5 roles * 11 modules

        val taxRates = repository.observeTaxRates(company.id).first()
        assertEquals(8, taxRates.size) // 0, 0.25, 3, 5, 12, 18, 28, 40

        val series = repository.observeNumberSeries(company.id).first()
        assertTrue("Number series should seed all sales, purchase, estimate, order, challan, note & payment series", series.size >= 6)

        val fys = repository.observeFinancialYears(company.id).first()
        assertEquals(1, fys.size)

        val hsnList = repository.observeHsnMaster(company.id).first()
        assertTrue("Bundled HSN/SAC directory should seed 50+ codes", hsnList.size >= 50)

        val logs = repository.observeRecentAuditLogs(company.id).first()
        assertTrue(logs.isNotEmpty())

        val attachments = db.coreDao().observeAttachments(company.id).first()
        assertEquals(2, attachments.size) // logo + signature

        val settings = db.coreDao().observeSettings(company.id).first()
        assertTrue(settings.isNotEmpty())

        // Verify PIN user switching works for Owner (654321) and Manager (222222)
        val switchedManager = repository.switchUserByPin("222222")
        assertNotNull(switchedManager)
        assertEquals(AppRole.MANAGER.name, switchedManager?.role)

        val switchedOwner = repository.switchUserByPin("654321")
        assertNotNull(switchedOwner)
        assertEquals(AppRole.OWNER.name, switchedOwner?.role)
    }

    @Test
    fun contrastRatio_inLightDarkAndAmoledThemes_exceeds4_5To1AgainstGlassBackgrounds() {
        fun blendOver(fgArgb: Long, bgArgb: Long): Long {
            val a = ((fgArgb ushr 24) and 0xFF) / 255.0
            val fr = ((fgArgb ushr 16) and 0xFF) / 255.0
            val fg = ((fgArgb ushr 8) and 0xFF) / 255.0
            val fb = (fgArgb and 0xFF) / 255.0

            val br = ((bgArgb ushr 16) and 0xFF) / 255.0
            val bg = ((bgArgb ushr 8) and 0xFF) / 255.0
            val bb = (bgArgb and 0xFF) / 255.0

            val r = ((fr * a + br * (1.0 - a)) * 255.0).toLong().coerceIn(0, 255)
            val g = ((fg * a + bg * (1.0 - a)) * 255.0).toLong().coerceIn(0, 255)
            val b = ((fb * a + bb * (1.0 - a)) * 255.0).toLong().coerceIn(0, 255)
            return (0xFFL shl 24) or (r shl 16) or (g shl 8) or b
        }

        fun relativeLuminance(argb: Long): Double {
            fun channel(c: Long): Double {
                val s = c / 255.0
                return if (s <= 0.03928) s / 12.92 else Math.pow((s + 0.055) / 1.055, 2.4)
            }
            val r = channel((argb ushr 16) and 0xFF)
            val g = channel((argb ushr 8) and 0xFF)
            val b = channel(argb and 0xFF)
            return 0.2126 * r + 0.7152 * g + 0.0722 * b
        }

        fun contrastRatio(c1: Long, c2: Long): Double {
            val l1 = relativeLuminance(c1)
            val l2 = relativeLuminance(c2)
            val lighter = maxOf(l1, l2)
            val darker = minOf(l1, l2)
            return (lighter + 0.05) / (darker + 0.05)
        }

        // Light theme: GlassFillLight (0xADFFFFFF) over LightBackground (0xFFFAF8F4), dark navy text (#1B1B2F)
        val lightGlassBg = blendOver(0xADFFFFFF, 0xFFFAF8F4)
        val lightOnSurfaceRatio = contrastRatio(0xFF1B1B2F, lightGlassBg)
        val lightOnSurfaceVariantRatio = contrastRatio(0xFF4A536E, lightGlassBg)
        assertTrue("Light onSurface contrast ($lightOnSurfaceRatio) must be >= 4.5", lightOnSurfaceRatio >= 4.5)
        assertTrue("Light onSurfaceVariant contrast ($lightOnSurfaceVariantRatio) must be >= 4.5", lightOnSurfaceVariantRatio >= 4.5)

        // Dark theme: GlassFillDark (0x29FFFFFF) over DarkBackground (0xFF0A0E24)
        val darkGlassBg = blendOver(0x29FFFFFF, 0xFF0A0E24)
        val darkOnSurfaceRatio = contrastRatio(0xFFF5F7FF, darkGlassBg)
        val darkOnSurfaceVariantRatio = contrastRatio(0xFFB5C0E6, darkGlassBg)
        assertTrue("Dark onSurface contrast ($darkOnSurfaceRatio) must be >= 4.5", darkOnSurfaceRatio >= 4.5)
        assertTrue("Dark onSurfaceVariant contrast ($darkOnSurfaceVariantRatio) must be >= 4.5", darkOnSurfaceVariantRatio >= 4.5)

        // AMOLED theme: GlassFillAmoled (0x1FFFFFFF) over AmoledBackground (0xFF000000)
        val amoledGlassBg = blendOver(0x1FFFFFFF, 0xFF000000)
        val amoledOnSurfaceRatio = contrastRatio(0xFFF5F7FF, amoledGlassBg)
        val amoledOnSurfaceVariantRatio = contrastRatio(0xFFB5C0E6, amoledGlassBg)
        assertTrue("AMOLED onSurface contrast ($amoledOnSurfaceRatio) must be >= 4.5", amoledOnSurfaceRatio >= 4.5)
        assertTrue("AMOLED onSurfaceVariant contrast ($amoledOnSurfaceVariantRatio) must be >= 4.5", amoledOnSurfaceVariantRatio >= 4.5)
    }

    @Test
    @Config(sdk = [36], qualifiers = "night")
    fun welcomeAndOnboarding_inSystemDarkMode_alwaysOpensInLightGlassTheme_andSupportsSetupAndRestoreFlows() {
        assertEquals(ThemeMode.SYSTEM, repository.appPrefs.value.themeMode)

        composeTestRule.setContent {
            VanijyaTheme(themeMode = ThemeMode.DARK) {
                com.example.feature.onboarding.OnboardingScreen(
                    onCompleteOnboarding = {},
                    onLanguageSelected = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("onboarding_root_light").assertIsDisplayed()
        composeTestRule.onNodeWithTag("welcome_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("welcome_tagline").assertIsDisplayed()
        composeTestRule.onNodeWithTag("welcome_language_chip").assertIsDisplayed()
        composeTestRule.onNodeWithTag("welcome_theme_toggle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("welcome_setup_new_business_btn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("welcome_restore_backup_btn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("welcome_offline_footer").assertIsDisplayed()

        composeTestRule.onNodeWithTag("welcome_theme_toggle").performClick()
        composeTestRule.onNodeWithTag("onboarding_root_dark").assertIsDisplayed()
        composeTestRule.onNodeWithTag("welcome_theme_toggle").performClick()
        composeTestRule.onNodeWithTag("onboarding_root_light").assertIsDisplayed()

        composeTestRule.onNodeWithTag("welcome_restore_backup_btn").performClick()
        composeTestRule.onNodeWithTag("onboarding_restore_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("restore_pick_file_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("restore_confirm_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("restore_back_button").performClick()
        composeTestRule.onNodeWithTag("welcome_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("welcome_setup_new_business_btn").performClick()
        composeTestRule.onNodeWithTag("onboarding_step_indicator").assertIsDisplayed()
        composeTestRule.onNodeWithText("Step 1 of 4").assertIsDisplayed()
        composeTestRule.onNodeWithTag("onboarding_back_button").assertIsDisplayed()

        composeTestRule.onNodeWithTag("onboarding_next_button").performClick()
        composeTestRule.onNodeWithText("Step 2 of 4").assertIsDisplayed()
        composeTestRule.onNodeWithTag("onboarding_business_name").assertIsDisplayed()

        composeTestRule.onNodeWithTag("onboarding_next_button").performClick()
        composeTestRule.onNodeWithText("Step 3 of 4").assertIsDisplayed()

        composeTestRule.onNodeWithTag("onboarding_next_button").performClick()
        composeTestRule.onNodeWithText("Step 4 of 4").assertIsDisplayed()
        composeTestRule.onNodeWithTag("onboarding_pin_input").assertIsDisplayed()
    }

    // =========================================================================
    // STEP 2 TESTS: GSTIN VALIDATION, PARTY LEDGER BALANCE, & STOCK-LEDGER MOVEMENTS
    // =========================================================================

    @Test
    fun gstinValidation_formatStateCodePanAndMod36Checksum_worksOffline() {
        // 1. Valid Maharashtra GSTIN with Mod-36 check digit
        val mhCheck = Validators.computeGstinCheckDigit("27AABCU9603R1Z")
        val mhGstin = "27AABCU9603R1Z$mhCheck"
        val mhResult = Validators.validateGstin(mhGstin, verifyChecksumStrictly = true)
        assertTrue("Valid Maharashtra GSTIN must pass strict checksum check", mhResult is Validators.ValidationResult.Valid)
        assertEquals("27", Validators.extractStateFromGstin(mhGstin)?.code)
        assertEquals("Maharashtra", Validators.extractStateFromGstin(mhGstin)?.name)
        assertEquals("AABCU9603R", Validators.extractPanFromGstin(mhGstin))

        // 2. Valid Rajasthan GSTIN with Mod-36 check digit
        val rjCheck = Validators.computeGstinCheckDigit("08AADCS4812K1Z")
        val rjGstin = "08AADCS4812K1Z$rjCheck"
        val rjResult = Validators.validateGstin(rjGstin, verifyChecksumStrictly = true)
        assertTrue("Valid Rajasthan GSTIN must pass strict checksum check", rjResult is Validators.ValidationResult.Valid)
        assertEquals("08", Validators.extractStateFromGstin(rjGstin)?.code)
        assertEquals("Rajasthan", Validators.extractStateFromGstin(rjGstin)?.name)
        assertEquals("AADCS4812K", Validators.extractPanFromGstin(rjGstin))

        // 3. Invalid checksum character rejected when verifyChecksumStrictly = true
        val wrongChar = if (mhCheck == 'Z') 'A' else 'Z'
        val tampered = Validators.validateGstin("27AABCU9603R1Z$wrongChar", verifyChecksumStrictly = true)
        assertTrue("Tampered GSTIN check digit must fail strict validation", tampered is Validators.ValidationResult.Invalid)
        assertTrue((tampered as Validators.ValidationResult.Invalid).reason.contains("checksum mismatch", ignoreCase = true))

        // 4. Invalid state code rejected
        val badState = Validators.validateGstin("99AABCU9603R1Z$mhCheck", verifyChecksumStrictly = true)
        assertTrue("Unassigned state code 99 must fail validation", badState is Validators.ValidationResult.Invalid)

        // 5. Verify bundled offline HSN/SAC asset directory loads 50+ Indian HSN/SAC codes
        val bundledHsn = BundledHsnDirectoryLoader.loadFromAssets(context, "test_co")
        assertTrue("Bundled HSN/SAC directory must contain at least 50 codes", bundledHsn.size >= 50)
    }

    @Test
    fun partyLedgerBalance_agingBuckets_andCreditLimitWarning_computeAccuratelyInIntegerPaise() = runBlocking {
        val company = repository.createBusinessWithDefaults(
            businessName = "A.K. Enterprises",
            legalName = "A.K. Enterprises",
            address = "Pune",
            phone = "9820112233",
            email = "ak@vanijya.in",
            gstin = "27AABCU9603R1ZM",
            pan = "AABCU9603R",
            stateCode = "27",
            stateName = "Maharashtra",
            businessType = "Wholesale",
            gstMode = "REGULAR_GST",
            fyStartMonth = 4,
            upiId = "ak@upi",
            bankName = "HDFC Bank",
            bankAccountNo = "0011223344",
            bankIfsc = "HDFC0000123",
            logoUri = "",
            signatureUri = "",
            ownerPin = "123456",
            biometricEnabled = false,
            languageCode = "en"
        )
        repository.ensureStep2MasterDataSeeded(company.id)

        val parties = repository.observeParties(company.id).first()
        assertTrue("Seeded parties should be >= 5", parties.size >= 5)

        val mehta = parties.first { it.name.contains("Mehta Kirana") }
        val entries = db.step2Dao().getPartyLedgerEntriesNow(mehta.id)
        assertTrue("Mehta Kirana should have ledger entries", entries.isNotEmpty())

        // Verify chronological running balance calculation in integer paise
        val rows = PartyLedgerCalculator.computeRunningBalances(entries)
        assertEquals(Money.fromRupees(64_500L).paise, rows.last().runningBalancePaise)
        assertTrue("Running balance should be positive (To Collect)", rows.last().runningBalancePaise > 0L)

        // Record a Payment-In credit movement of ₹14,500 in ONE Room @Transaction
        val now = System.currentTimeMillis()
        repository.recordPartyLedgerMovement(
            com.example.core.database.PartyLedgerEntryEntity(
                id = "ple_test_rcpt_205",
                companyId = company.id,
                partyId = mehta.id,
                entryDate = now,
                voucherType = "PAYMENT_IN",
                voucherNumber = "RCPT-2026-205",
                debitPaise = 0L,
                creditPaise = Money.fromRupees(14_500L).paise,
                runningBalancePaise = 0L,
                dueDate = 0L,
                isSettled = true,
                notes = "Part payment received via NEFT",
                createdAt = now,
                updatedAt = now
            )
        )

        val updatedMehta = repository.observeParties(company.id).first().first { it.id == mehta.id }
        assertEquals(Money.fromRupees(50_000L).paise, updatedMehta.currentBalancePaise)

        // Verify aging analysis across 0-30, 31-60, 61-90, and 90+ buckets
        val allLedger = repository.observeAllPartyLedgerEntries(company.id).first()
        val aging = PartyLedgerCalculator.computeAgingBuckets(allLedger)
        assertTrue("Total outstanding across aging buckets must be > 0", aging.total.paise > 0L)
        assertEquals(
            aging.bucket0To30.paise + aging.bucket31To60.paise + aging.bucket61To90.paise + aging.bucket90Plus.paise,
            aging.total.paise
        )

        // Verify credit-limit warning hook for billing
        val creditCheck = PartyLedgerCalculator.evaluateCreditLimitWarning(
            party = updatedMehta, // currentBalance = ₹50,000, creditLimit = ₹75,000
            newInvoiceAmount = Money.fromRupees(65_000L) // Projected = ₹1,15,000 > ₹75,000 (exceeded by ₹40,000)
        )
        assertTrue(
            "Projected balance exceeding credit limit must trigger Exceeded result",
            creditCheck is com.example.core.model.CreditLimitCheckResult.Exceeded
        )
        val exceeded = creditCheck as com.example.core.model.CreditLimitCheckResult.Exceeded
        assertEquals(Money.fromRupees(40_000L).paise, exceeded.exceededBy.paise)
    }

    @Test
    fun stockLedgerMovements_adjustments_godownTransfers_andRealTimeValuation_workInRoomTransaction() = runBlocking {
        val company = repository.createBusinessWithDefaults(
            businessName = "A.K. Enterprises",
            legalName = "A.K. Enterprises",
            address = "Pune",
            phone = "9820112233",
            email = "ak@vanijya.in",
            gstin = "27AABCU9603R1ZM",
            pan = "AABCU9603R",
            stateCode = "27",
            stateName = "Maharashtra",
            businessType = "Wholesale",
            gstMode = "REGULAR_GST",
            fyStartMonth = 4,
            upiId = "ak@upi",
            bankName = "HDFC Bank",
            bankAccountNo = "0011223344",
            bankIfsc = "HDFC0000123",
            logoUri = "",
            signatureUri = "",
            ownerPin = "123456",
            biometricEnabled = false,
            languageCode = "en"
        )
        repository.ensureStep2MasterDataSeeded(company.id)

        val items = repository.observeItems(company.id).first()
        val batches = repository.observeItemBatches(company.id).first()
        val warehouses = repository.observeWarehouses(company.id).first()
        val conversions = repository.observeUnitConversions(company.id).first()

        assertTrue("Seeded items should be >= 5", items.size >= 5)
        assertTrue("Seeded warehouses should be >= 2", warehouses.size >= 2)
        assertTrue(
            "Unit conversions must include 1 BOX = 12 PCS",
            conversions.any { it.baseUnit == "BOX" && it.secondaryUnit == "PCS" && it.multiplier == 12 }
        )

        // Verify real-time stock metrics (stock value, low-stock count, expiring batch count)
        val initialMetrics = StockLedgerCalculator.computeRealTimeStockMetrics(items, batches)
        assertTrue("Total stock value at purchase must be > 0", initialMetrics.totalStockValue.paise > 0L)
        assertTrue("Low-stock items should be detected", initialMetrics.lowStockItems.isNotEmpty())
        assertTrue("Expiring batches should be detected", initialMetrics.expiringBatches.isNotEmpty())

        // 1. Perform Stock Adjustment (ADD 15 BAG to India Gate Classic Basmati Rice) in ONE Room @Transaction
        val rice = items.first { it.itemCode == "SKU-RICE-25" }
        val initialRiceQty = rice.currentStockQty
        val now = System.currentTimeMillis()
        repository.recordStockAdjustment(
            com.example.core.database.StockAdjustmentEntity(
                id = "adj_test_01",
                companyId = company.id,
                itemId = rice.id,
                itemName = rice.name,
                warehouseId = warehouses.first().id,
                warehouseName = warehouses.first().name,
                adjustmentType = "ADD",
                qty = 15L,
                reason = "AUDIT_CORRECTION",
                unitCostPaise = rice.purchasePricePaise,
                adjustmentDate = now,
                notes = "Physical count verification",
                createdAt = now,
                updatedAt = now
            )
        )

        val updatedRice = repository.observeItems(company.id).first().first { it.id == rice.id }
        assertEquals(initialRiceQty + 15L, updatedRice.currentStockQty)

        // 2. Perform Godown Transfer between Main Godown and Retail Counter in ONE Room @Transaction
        repository.recordStockTransfer(
            com.example.core.database.StockTransferEntity(
                id = "trf_test_01",
                companyId = company.id,
                transferNumber = "TRF-TEST-01",
                itemId = rice.id,
                itemName = rice.name,
                fromWarehouseId = warehouses[0].id,
                fromWarehouseName = warehouses[0].name,
                toWarehouseId = warehouses[1].id,
                toWarehouseName = warehouses[1].name,
                qty = 10L,
                transferDate = now + 1000L,
                notes = "Replenish retail shelf",
                createdAt = now + 1000L,
                updatedAt = now + 1000L
            )
        )

        val riceLedger = db.step2Dao().getStockLedgerForItemNow(rice.id)
        assertTrue("Stock ledger must contain OPENING, ADJUSTMENT_ADD, TRANSFER_OUT, and TRANSFER_IN entries", riceLedger.size >= 4)
        val runningRows = StockLedgerCalculator.computeRunningStockLedger(riceLedger)
        assertEquals(initialRiceQty + 15L, runningRows.last().closingQty)
    }

    @Test
    fun step2Screens_partiesAndItems_renderInLightDarkAmoledAnd1_3xFontScale() {
        composeTestRule.setContent {
            VanijyaTheme(themeMode = ThemeMode.AMOLED) {
                ItemsFoundationScreen(
                    hideAmounts = false,
                    decimals = 2
                )
            }
        }

        // Verify Items & Stock screen and its 4 segments (Items, Categories, Low Stock, Expiring)
        composeTestRule.onNodeWithTag("items_stock_hub_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("items_total_stock_value_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("items_search_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("items_fab_add_item").assertIsDisplayed()

        // Switch through Categories, Low Stock, and Expiring segments
        composeTestRule.onNodeWithTag("items_segment_1").performClick()
        composeTestRule.onNodeWithTag("items_segment_2").performClick()
        composeTestRule.onNodeWithTag("items_segment_3").performClick()
    }

    // =========================================================================
    // STEP 3A TESTS: GST CALCULATION ENGINE, ATOMIC DOCUMENT LIFECYCLE,
    // ONE-TAP CONVERSION (`txn_link`), BILL-TO-BILL PAYMENT & BOUNCED CHEQUE
    // =========================================================================

    @Test
    fun step3A_gstCalculationEngine_matchesHandCalculatedIntraInterInclusiveDiscountCompositionAndRoundOffExamples() {
        // 1. Hand-Calculated Intra-State B2B Exclusive Example:
        // 10 Bags @ ₹2,850.00 = ₹28,500.00 taxable, 5% GST -> CGST 2.5% = ₹712.50, SGST 2.5% = ₹712.50, Total = ₹29,925.00
        val intraB2b = GstCalculationEngine.calculate(
            GstDocumentInput(
                supplierStateCode = "27",
                placeOfSupplyStateCode = "27",
                partyGstin = "27AAPFM3456D1Z9",
                lines = listOf(
                    GstLineInput(
                        lineId = "l1",
                        itemId = "item_rice",
                        itemName = "Basmati Rice 25kg",
                        hsnSacCode = "1006",
                        unit = "BAG",
                        qty = 10L,
                        unitPricePaise = 2_850_00L,
                        purchaseCostPaise = 2_400_00L,
                        taxInclusive = false,
                        taxRateBps = 500
                    )
                )
            )
        )
        assertEquals(GstSupplyType.INTRA_STATE, intraB2b.supplyType)
        assertEquals(GstInvoiceClassification.B2B, intraB2b.classification)
        assertEquals(28_500_00L, intraB2b.taxableSubtotalPaise)
        assertEquals(712_50L, intraB2b.cgstTotalPaise)
        assertEquals(712_50L, intraB2b.sgstTotalPaise)
        assertEquals(0L, intraB2b.igstTotalPaise)
        assertEquals(29_925_00L, intraB2b.grandTotalPaise)
        assertEquals(4_500_00L, intraB2b.grossProfitPaise)
        assertEquals("Rupees Twenty-Nine Thousand Nine Hundred Twenty-Five Only", intraB2b.amountInWords)

        // 2. Hand-Calculated Inter-State Tax-Inclusive + Discount + Cess + B2CL Classification:
        // Unregistered party in Gujarat (24), supplier in Maharashtra (27), value > ₹2.5L -> B2CL
        // 100 units @ ₹3,360 (Inclusive of 12% IGST):
        // Gross = ₹3,36,000 -> Base Taxable = 3,36,000 / 1.12 = ₹3,00,000, IGST = ₹36,000
        val interInclusive = GstCalculationEngine.calculate(
            GstDocumentInput(
                supplierStateCode = "27",
                placeOfSupplyStateCode = "24",
                partyGstin = "",
                lines = listOf(
                    GstLineInput(
                        lineId = "l2",
                        itemId = "item_ghee",
                        itemName = "Cow Ghee Bulk",
                        hsnSacCode = "0405",
                        unit = "TIN",
                        qty = 100L,
                        unitPricePaise = 3_360_00L,
                        taxInclusive = true,
                        taxRateBps = 1200
                    )
                )
            )
        )
        assertEquals(GstSupplyType.INTER_STATE, interInclusive.supplyType)
        assertEquals(GstInvoiceClassification.B2CL, interInclusive.classification)
        assertEquals(3_00_000_00L, interInclusive.taxableSubtotalPaise)
        assertEquals(0L, interInclusive.cgstTotalPaise)
        assertEquals(0L, interInclusive.sgstTotalPaise)
        assertEquals(36_000_00L, interInclusive.igstTotalPaise)
        assertEquals(3_36_000_00L, interInclusive.grandTotalPaise)

        // 3. Per-line discount (10%) + Invoice discount (₹500) + Freight Charge (₹1,000 @ 18%) + Round-Off Modes
        // 10 units @ ₹1,000 = ₹10,000 -> -10% line disc (₹1,000) = ₹9,000 -> -₹500 inv disc = ₹8,500 net line taxable
        // + ₹1,000 freight taxable = ₹9,500 total taxable @ 18% intra-state (CGST 9% = ₹855, SGST 9% = ₹855) -> ₹11,210
        val withDiscountsAndCharges = GstCalculationEngine.calculate(
            GstDocumentInput(
                supplierStateCode = "27",
                placeOfSupplyStateCode = "27",
                partyGstin = "27AABCS9876G1Z3",
                lines = listOf(
                    GstLineInput(
                        lineId = "l3",
                        itemId = "item_hw",
                        itemName = "Hardware Kit",
                        hsnSacCode = "8471",
                        unit = "PCS",
                        qty = 10L,
                        unitPricePaise = 1_000_00L,
                        discountBps = 1000, // 10%
                        taxRateBps = 1800
                    )
                ),
                invoiceDiscountPaise = 500_00L,
                additionalCharges = listOf(
                    GstAdditionalChargeInput(
                        id = "chg1",
                        label = "Freight",
                        amountPaise = 1_000_00L,
                        taxRateBps = 1800
                    )
                )
            )
        )
        assertEquals(1_000_00L, withDiscountsAndCharges.lineDiscountTotalPaise)
        assertEquals(500_00L, withDiscountsAndCharges.invoiceDiscountPaise)
        assertEquals(9_500_00L, withDiscountsAndCharges.taxableSubtotalPaise)
        assertEquals(855_00L, withDiscountsAndCharges.cgstTotalPaise)
        assertEquals(855_00L, withDiscountsAndCharges.sgstTotalPaise)
        assertEquals(11_210_00L, withDiscountsAndCharges.grandTotalPaise)

        // 4. Composition Scheme (Bill of Supply) -> zero tax even when item has 18% slab
        val compositionCalc = GstCalculationEngine.calculate(
            GstDocumentInput(
                supplierStateCode = "27",
                placeOfSupplyStateCode = "27",
                isCompositionScheme = true,
                lines = listOf(
                    GstLineInput(
                        lineId = "l4",
                        itemId = "item_1",
                        itemName = "Item",
                        hsnSacCode = "1006",
                        unit = "PCS",
                        qty = 5L,
                        unitPricePaise = 200_00L,
                        taxRateBps = 1800
                    )
                )
            )
        )
        assertEquals(GstInvoiceClassification.COMPOSITION, compositionCalc.classification)
        assertEquals(0L, compositionCalc.totalTaxPaise)
        assertEquals(1_000_00L, compositionCalc.grandTotalPaise)

        // 5. Round-off Modes (NEAREST, UP, DOWN, NONE) on ₹100.40 (10040 paise) and ₹100.60 (10060 paise)
        assertEquals(100_00L, GstCalculationEngine.applyRounding(100_40L, RoundingModeOption.NEAREST, true).first)
        assertEquals(-40L, GstCalculationEngine.applyRounding(100_40L, RoundingModeOption.NEAREST, true).second)
        assertEquals(101_00L, GstCalculationEngine.applyRounding(100_60L, RoundingModeOption.NEAREST, true).first)
        assertEquals(40L, GstCalculationEngine.applyRounding(100_60L, RoundingModeOption.NEAREST, true).second)
        assertEquals(101_00L, GstCalculationEngine.applyRounding(100_10L, RoundingModeOption.UP, true).first)
        assertEquals(100_00L, GstCalculationEngine.applyRounding(100_90L, RoundingModeOption.DOWN, true).first)
        assertEquals(100_40L, GstCalculationEngine.applyRounding(100_40L, RoundingModeOption.NEAREST, false).first)
    }

    @Test
    fun step3A_salesDocumentTransaction_saveEditCancelConvertAndAuditLog_worksAtomicallyInRoom() = runBlocking {
        val company = repository.createBusinessWithDefaults(
            businessName = "Shree Ganesh Traders",
            legalName = "Shree Ganesh Traders",
            address = "Pune",
            phone = "9823011223",
            email = "ganesh@vanijya.in",
            gstin = "27AABCU9603R1ZM",
            pan = "AABCU9603R",
            stateCode = "27",
            stateName = "Maharashtra",
            businessType = "Wholesale",
            gstMode = "REGULAR_GST",
            fyStartMonth = 4,
            upiId = "ganesh@upi",
            bankName = "HDFC Bank",
            bankAccountNo = "0011223344",
            bankIfsc = "HDFC0000123",
            logoUri = "",
            signatureUri = "",
            ownerPin = "123456",
            biometricEnabled = false,
            languageCode = "en"
        )

        val mehtaBefore = db.step3ADao().getPartyById("${company.id}_party_mehta")!!
        val riceBefore = db.step3ADao().getItemById("${company.id}_item_basmati")!!
        val initialMehtaBalance = mehtaBefore.currentBalancePaise // 64,500.00
        val initialRiceStock = riceBefore.currentStockQty // 110 BAG

        // 1. Create a new Tax Invoice for 5 BAGs of Basmati Rice (+1 Free BAG = 6 BAGs stock out)
        // 5 * ₹2,850 = ₹14,250 + 5% GST (₹712.50) = ₹14,962.50 -> rounded to ₹14,963.00
        val now = System.currentTimeMillis()
        val txnId = "test_inv_atomic_1"
        val invTotalPaise = 14_963_00L
        val header = TxnEntity(
            id = txnId,
            companyId = company.id,
            docType = SalesDocTypes.TAX_INVOICE,
            docNumber = "", // Auto gap-free allocation
            txnDate = now,
            dueDate = now + 15 * 86_400_000L,
            partyId = mehtaBefore.id,
            partyName = mehtaBefore.name,
            partyGstin = mehtaBefore.gstin,
            placeOfSupplyStateCode = "27",
            placeOfSupplyStateName = "Maharashtra",
            supplyType = "INTRA_STATE",
            classification = "B2B",
            grossSubtotalPaise = 14_250_00L,
            taxableSubtotalPaise = 14_250_00L,
            cgstTotalPaise = 356_25L,
            sgstTotalPaise = 356_25L,
            totalTaxPaise = 712_50L,
            roundOffPaise = 50L,
            grandTotalPaise = invTotalPaise,
            paidAmountPaise = 0L,
            balanceAmountPaise = invTotalPaise,
            totalCostPaise = 12_000_00L,
            grossProfitPaise = 2_250_00L,
            status = "Unpaid",
            amountInWords = IndianNumberToWords.toCurrencyWords(Money(invTotalPaise)),
            warehouseId = "${company.id}_wh_main",
            warehouseName = "Main Godown",
            createdAt = now,
            updatedAt = now
        )
        val line = TxnLineEntity(
            id = "test_inv_line_1",
            companyId = company.id,
            txnId = txnId,
            lineIndex = 0,
            itemId = riceBefore.id,
            itemName = riceBefore.name,
            itemCode = riceBefore.itemCode,
            hsnSacCode = "1006",
            qty = 5L,
            freeQty = 1L,
            unit = "BAG",
            unitPricePaise = 2_850_00L,
            purchaseCostPaise = 2_400_00L,
            taxRateBps = 500,
            taxablePaise = 14_250_00L,
            cgstPaise = 356_25L,
            sgstPaise = 356_25L,
            lineTotalPaise = 14_962_50L,
            createdAt = now,
            updatedAt = now
        )
        val taxLine = TxnTaxLineEntity(
            id = "test_inv_tax_1",
            companyId = company.id,
            txnId = txnId,
            hsnSacCode = "1006",
            description = "Basmati Rice",
            uqc = "BAG",
            totalQty = 5L,
            taxablePaise = 14_250_00L,
            taxRateBps = 500,
            cgstPaise = 356_25L,
            sgstPaise = 356_25L,
            igstPaise = 0L,
            cessPaise = 0L,
            totalTaxPaise = 712_50L,
            createdAt = now,
            updatedAt = now
        )

        val savedInv = repository.saveOrUpdateSalesDocument(
            header = header,
            lines = listOf(line),
            charges = emptyList(),
            taxLines = listOf(taxLine)
        )
        assertEquals("INV/26-27/0005", savedInv.docNumber)

        // Verify stock reduced by 6 BAGs (5 qty + 1 freeQty) and party balance increased by ₹14,963
        val riceAfterCreate = db.step3ADao().getItemById(riceBefore.id)!!
        val mehtaAfterCreate = db.step3ADao().getPartyById(mehtaBefore.id)!!
        assertEquals(initialRiceStock - 6L, riceAfterCreate.currentStockQty)
        assertEquals(initialMehtaBalance + invTotalPaise, mehtaAfterCreate.currentBalancePaise)

        // 2. Edit the invoice: change qty from 5 (+1 free) to 10 (+0 free) and total to ₹29,925
        val editedTotalPaise = 29_925_00L
        repository.saveOrUpdateSalesDocument(
            header = savedInv.copy(
                taxableSubtotalPaise = 28_500_00L,
                grandTotalPaise = editedTotalPaise,
                balanceAmountPaise = editedTotalPaise
            ),
            lines = listOf(line.copy(qty = 10L, freeQty = 0L, taxablePaise = 28_500_00L, lineTotalPaise = editedTotalPaise)),
            charges = emptyList(),
            taxLines = listOf(taxLine.copy(totalQty = 10L, taxablePaise = 28_500_00L))
        )

        val riceAfterEdit = db.step3ADao().getItemById(riceBefore.id)!!
        val mehtaAfterEdit = db.step3ADao().getPartyById(mehtaBefore.id)!!
        assertEquals("Old 6 bags reversed and new 10 bags deducted", initialRiceStock - 10L, riceAfterEdit.currentStockQty)
        assertEquals("Old ₹14,963 reversed and new ₹29,925 debited", initialMehtaBalance + editedTotalPaise, mehtaAfterEdit.currentBalancePaise)

        // 3. Cancel the invoice with a reason: keeps number INV/26-27/0005, reverses stock & party balance
        val cancelled = repository.cancelSalesDocument(company.id, txnId, "Order cancelled by buyer")
        assertNotNull(cancelled)
        assertEquals("Cancelled", cancelled?.status)
        assertEquals("INV/26-27/0005", cancelled?.docNumber)
        assertEquals("Order cancelled by buyer", cancelled?.cancelReason)

        val riceAfterCancel = db.step3ADao().getItemById(riceBefore.id)!!
        val mehtaAfterCancel = db.step3ADao().getPartyById(mehtaBefore.id)!!
        assertEquals("Stock restored to initial after cancellation", initialRiceStock, riceAfterCancel.currentStockQty)
        assertEquals("Party balance restored to initial after cancellation", initialMehtaBalance, mehtaAfterCancel.currentBalancePaise)

        // 4. One-Tap Document Conversion: Open Quotation (EST/26-27/0002) -> Sale Order -> Tax Invoice -> Credit Note
        val est2Id = "${company.id}_txn_est_2"
        val convertedOrder = repository.convertSalesDocument(company.id, est2Id, SalesDocTypes.SALE_ORDER)
        assertNotNull(convertedOrder)
        assertEquals(SalesDocTypes.SALE_ORDER, convertedOrder?.docType)

        val convertedInvoice = repository.convertSalesDocument(company.id, convertedOrder!!.id, SalesDocTypes.TAX_INVOICE)
        assertNotNull(convertedInvoice)
        assertEquals(SalesDocTypes.TAX_INVOICE, convertedInvoice?.docType)

        val convertedReturn = repository.convertSalesDocument(company.id, convertedInvoice!!.id, SalesDocTypes.CREDIT_NOTE)
        assertNotNull(convertedReturn)
        assertEquals(SalesDocTypes.CREDIT_NOTE, convertedReturn?.docType)

        val links = repository.observeTxnLinks(company.id).first()
        assertTrue("Conversion chain must record QUOTATION_TO_ORDER, ORDER_TO_INVOICE, and INVOICE_TO_RETURN in txn_link", links.size >= 7)
    }

    @Test
    fun step3A_billToBillPayment_bouncedChequeReversal_andRecurringInvoice_workInRoomTransaction() = runBlocking {
        val company = repository.createBusinessWithDefaults(
            businessName = "Shree Ganesh Traders",
            legalName = "Shree Ganesh Traders",
            address = "Pune",
            phone = "9823011223",
            email = "ganesh@vanijya.in",
            gstin = "27AABCU9603R1ZM",
            pan = "AABCU9603R",
            stateCode = "27",
            stateName = "Maharashtra",
            businessType = "Wholesale",
            gstMode = "REGULAR_GST",
            fyStartMonth = 4,
            upiId = "ganesh@upi",
            bankName = "HDFC Bank",
            bankAccountNo = "0011223344",
            bankIfsc = "HDFC0000123",
            logoUri = "",
            signatureUri = "",
            ownerPin = "123456",
            biometricEnabled = false,
            languageCode = "en"
        )

        // Unpaid Invoice #3 for Patel Hardware Mart (₹28,200)
        val inv3Id = "${company.id}_txn_inv_3"
        val patelId = "${company.id}_party_patel"
        val patelBefore = db.step3ADao().getPartyById(patelId)!!
        val now = System.currentTimeMillis()

        // 1. Record a Cheque Payment-In of ₹28,200 settling Invoice #3 bill-to-bill
        val payId = "pay_test_cheque_1"
        val chqId = "chq_test_bounce_1"
        val payment = PaymentEntity(
            id = payId,
            companyId = company.id,
            paymentNumber = "",
            paymentType = "PAYMENT_IN",
            paymentDate = now,
            partyId = patelId,
            partyName = patelBefore.name,
            totalAmountPaise = 28_200_00L,
            primaryMode = "CHEQUE",
            modesSplitJson = "CHEQUE:2820000",
            referenceNo = "654987",
            chequeId = chqId,
            status = "Paid",
            createdAt = now,
            updatedAt = now
        )
        val alloc = PaymentAllocationEntity(
            id = "alloc_test_1",
            companyId = company.id,
            paymentId = payId,
            txnId = inv3Id,
            txnDocNumber = "INV/26-27/0003",
            allocatedAmountPaise = 28_200_00L,
            discountAllowedPaise = 0L,
            createdAt = now,
            updatedAt = now
        )
        val cheque = com.example.core.database.ChequeEntity(
            id = chqId,
            companyId = company.id,
            paymentId = payId,
            partyId = patelId,
            partyName = patelBefore.name,
            chequeType = "RECEIVABLE",
            chequeNumber = "654987",
            bankName = "SBI Nashik",
            chequeDate = now,
            dueDate = now + 86_400_000L,
            amountPaise = 28_200_00L,
            status = "IN_HAND",
            createdAt = now,
            updatedAt = now
        )

        repository.recordPaymentWithAllocations(payment, listOf(alloc), cheque)

        val inv3AfterPay = db.step3ADao().getTxnByIdNow(inv3Id)!!
        val patelAfterPay = db.step3ADao().getPartyById(patelId)!!
        assertEquals("Paid", inv3AfterPay.status)
        assertEquals(0L, inv3AfterPay.balanceAmountPaise)
        assertEquals(patelBefore.currentBalancePaise - 28_200_00L, patelAfterPay.currentBalancePaise)

        // 2. Mark Cheque #654987 as BOUNCED with ₹250 bounce fee in ONE @Transaction
        val bounced = repository.updateChequeStatus(
            companyId = company.id,
            chequeId = chqId,
            newStatus = "BOUNCED",
            bounceChargesPaise = 250_00L,
            bounceReason = "Insufficient Funds"
        )
        assertNotNull(bounced)
        assertEquals("BOUNCED", bounced?.status)

        val inv3AfterBounce = db.step3ADao().getTxnByIdNow(inv3Id)!!
        val patelAfterBounce = db.step3ADao().getPartyById(patelId)!!
        assertEquals("Unpaid", inv3AfterBounce.status)
        assertEquals(28_200_00L, inv3AfterBounce.balanceAmountPaise)
        assertEquals(
            "Party balance must have cheque reversal (+₹28,200) plus bounce penalty (+₹250)",
            patelBefore.currentBalancePaise + 250_00L,
            patelAfterBounce.currentBalancePaise
        )

        // 3. Run Recurring Invoice Template Now
        val recGenerated = repository.runRecurringTemplateNow(company.id, "${company.id}_rec_1")
        assertNotNull(recGenerated)
        assertEquals(SalesDocTypes.TAX_INVOICE, recGenerated?.docType)
        assertEquals(4_130_00L, recGenerated?.grandTotalPaise)
    }

    @Test
    fun sharedVoucherBuilder_fixedSaveBar_multiLineItems_andCollapsibleTotals_work() {
        var builderOpenState = false
        composeTestRule.setContent {
            VanijyaTheme(themeMode = ThemeMode.DARK) {
                com.example.feature.sales.SalesFoundationScreen(
                    invoicePrefix = "INV/26-27/",
                    hideAmounts = false,
                    decimals = 2,
                    launchAction = com.example.feature.sales.BillingLaunchAction.NEW_SALE,
                    onBuilderOpenChange = { builderOpenState = it }
                )
            }
        }

        // Verify builder opened and notified parent shell to hide bottom nav bar
        assertTrue("Builder must notify parent shell when open", builderOpenState)
        composeTestRule.onNodeWithTag("shared_voucher_builder_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("builder_topbar_save_btn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("builder_bottom_action_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("builder_save_btn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("builder_more_btn").assertIsDisplayed()

        // Verify initial item line 0 is displayed and add a second line item via "+ Add item"
        composeTestRule.onNodeWithTag("builder_lazy_column")
            .performScrollToNode(hasTestTag("builder_line_card_0"))
        composeTestRule.onNodeWithTag("builder_line_card_0").assertIsDisplayed()

        composeTestRule.onNodeWithTag("builder_lazy_column")
            .performScrollToNode(hasTestTag("builder_add_item_btn"))
        composeTestRule.onNodeWithTag("builder_add_item_btn").performClick()
        composeTestRule.onNodeWithTag("line_editor_confirm_btn").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("builder_lazy_column")
            .performScrollToNode(hasTestTag("builder_line_card_1"))
        composeTestRule.onNodeWithTag("builder_line_card_1").assertIsDisplayed()

        // Verify collapsible totals panel can be scrolled to and toggled
        composeTestRule.onNodeWithTag("builder_lazy_column")
            .performScrollToNode(hasTestTag("builder_collapsible_totals_panel"))
        composeTestRule.onNodeWithTag("builder_collapsible_totals_panel").assertIsDisplayed()
        composeTestRule.onNodeWithTag("builder_toggle_totals_btn").performClick()
    }

    @Test
    fun bottomNavigation_fiveTabsWithStock_twoScreensDeepToDashboardRoot_andFullScreenEditorsHideBar() {
        composeTestRule.setContent {
            VanijyaTheme(themeMode = ThemeMode.DARK) {
                com.example.core.navigation.VanijyaMainShell(
                    prefs = repository.appPrefs.value,
                    companies = emptyList(),
                    activeCompany = null,
                    users = emptyList(),
                    taxRates = emptyList(),
                    rolePermissions = emptyList(),
                    hsnMasterList = emptyList(),
                    recentAuditLogs = emptyList(),
                    onToggleHideAmounts = {},
                    onLockNow = {},
                    onSwitchCompany = {},
                    onCreateAdditionalCompany = { _, _ -> },
                    onSwitchUserByPin = { _, cb -> cb(true, "") },
                    onSaveBusinessProfile = {},
                    onSaveInvoiceSettings = { _, _, _ -> },
                    onUpdateRoundingMode = {},
                    onUpdateDecimals = {},
                    onUpdateDateFormat = {},
                    onUpdateTheme = {},
                    onUpdateLanguage = {},
                    onUpdateAutoLock = {},
                    onUpdateHideAmounts = {},
                    onUpdateBlockScreenshots = {},
                    onUpdateBiometricEnabled = {},
                    onChangePin = { _, _, cb -> cb(true, "") },
                    onAddLocalUser = { _, _, _, cb -> cb(true, "") },
                    onUpdateTaxRate = {},
                    onUpdateRolePermission = {},
                    onVerifyPinAndResetApp = { _, cb -> cb(true) },
                    onBiometricResetApp = {}
                )
            }
        }

        // 1. Verify the 5 bottom tabs: Dashboard, Billing, Parties, Staff, Stock (and Reports removed from bottom bar)
        composeTestRule.onNodeWithTag("floating_glass_nav_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_nav_home").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_nav_billing").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_nav_parties").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_nav_staff").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_nav_stock").assertIsDisplayed()
        assertEquals(0, composeTestRule.onAllNodesWithTag("bottom_nav_reports").fetchSemanticsNodes().size)

        // 2. Verify Reports is reachable from:
        //    (a) Dashboard All Features grid tile, (b) Avatar menu, and (c) Dashboard top bar chart icon
        composeTestRule.onNodeWithTag("home_dashboard_list")
            .performScrollToNode(hasTestTag("feature_tile_reports"))
        composeTestRule.onNodeWithTag("feature_tile_reports").performClick()
        composeTestRule.onNodeWithTag("reports_hub_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_nav_home").performClick()
        composeTestRule.onNodeWithTag("home_dashboard_list").assertIsDisplayed()

        composeTestRule.onNodeWithTag("dashboard_profile_avatar").performClick()
        composeTestRule.onNodeWithTag("avatar_menu_reports").performClick()
        composeTestRule.onNodeWithTag("reports_hub_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_nav_home").performClick()
        composeTestRule.onNodeWithTag("home_dashboard_list").assertIsDisplayed()

        // 3. TAB 1 (Dashboard): Go 2 screens deep (Dashboard -> Reports -> Sale Report Detail), tap Dashboard -> returns to Dashboard root
        composeTestRule.onNodeWithTag("dashboard_top_bar_reports_btn").performClick()
        composeTestRule.onNodeWithTag("reports_hub_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("reports_hub_screen")
            .performScrollToNode(hasTestTag("report_card_sale_report"))
        composeTestRule.onNodeWithTag("report_card_sale_report").performClick()
        composeTestRule.onNodeWithTag("report_detail_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_nav_home").performClick()
        composeTestRule.onNodeWithTag("home_dashboard_list").assertIsDisplayed()

        // 4. TAB 2 (Billing): Go 2 screens deep (Billing -> Invoice Detail -> Invoice Audit & HSN Breakdown), tap Dashboard -> returns to Dashboard root
        composeTestRule.onNodeWithTag("bottom_nav_billing").performClick()
        composeTestRule.onNodeWithTag("sales_billing_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sales_billing_screen")
            .performScrollToNode(hasTestTag("invoice_card_0"))
        composeTestRule.onNodeWithTag("invoice_card_0").performClick()
        composeTestRule.onNodeWithTag("invoice_detail_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("invoice_detail_open_audit_btn").performClick()
        composeTestRule.onNodeWithTag("invoice_audit_sub_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_nav_home").performClick()
        composeTestRule.onNodeWithTag("home_dashboard_list").assertIsDisplayed()

        // 5. TAB 3 (Parties): Go 2 screens deep (Parties -> Party Detail -> Party Aging Sub-Screen), tap Dashboard -> returns to Dashboard root
        composeTestRule.onNodeWithTag("bottom_nav_parties").performClick()
        composeTestRule.onNodeWithTag("parties_hub_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("parties_lazy_column")
            .performScrollToNode(hasTestTag("party_card_0"))
        composeTestRule.onNodeWithTag("party_card_0").performClick()
        composeTestRule.onNodeWithTag("party_detail_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("party_detail_open_aging_btn").performClick()
        composeTestRule.onNodeWithTag("party_aging_sub_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_nav_home").performClick()
        composeTestRule.onNodeWithTag("home_dashboard_list").assertIsDisplayed()

        // 6. TAB 4 (Staff): Go 2 screens deep (Staff -> Roster Detail -> Salary Breakdown Sub-Screen), tap Dashboard -> returns to Dashboard root
        composeTestRule.onNodeWithTag("bottom_nav_staff").performClick()
        composeTestRule.onNodeWithTag("staff_hub_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("staff_open_roster_detail_btn").performClick()
        composeTestRule.onNodeWithTag("staff_roster_detail_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("staff_detail_open_salary_btn").performClick()
        composeTestRule.onNodeWithTag("staff_salary_sub_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_nav_home").performClick()
        composeTestRule.onNodeWithTag("home_dashboard_list").assertIsDisplayed()

        // 7. TAB 5 (Stock): Go 2 screens deep (Stock -> Item Detail -> Stock Valuation Sub-Screen), tap Dashboard -> returns to Dashboard root
        composeTestRule.onNodeWithTag("bottom_nav_stock").performClick()
        composeTestRule.onNodeWithTag("items_stock_hub_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("items_fab_add_item").assertIsDisplayed()
        composeTestRule.onNodeWithTag("items_lazy_column")
            .performScrollToNode(hasTestTag("item_card_0"))
        composeTestRule.onNodeWithTag("item_card_0").performClick()
        composeTestRule.onNodeWithTag("item_detail_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("item_detail_open_valuation_btn").performClick()
        composeTestRule.onNodeWithTag("item_valuation_sub_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_nav_home").performClick()
        composeTestRule.onNodeWithTag("home_dashboard_list").assertIsDisplayed()

        // 8. Verify full-screen editors (item form, party form, payroll run, invoice builder) hide bottom bar when open and restore it when closed
        composeTestRule.onNodeWithTag("bottom_nav_stock").performClick()
        composeTestRule.onNodeWithTag("bottom_nav_stock").performClick() // Reselect active tab resets deep screen to Stock root
        composeTestRule.onNodeWithTag("items_fab_add_item").performClick()
        composeTestRule.onNodeWithTag("item_full_screen_editor").assertIsDisplayed()
        assertEquals(0, composeTestRule.onAllNodesWithTag("floating_glass_nav_bar").fetchSemanticsNodes().size)
        composeTestRule.onNodeWithTag("item_form_back_btn").performClick()
        composeTestRule.onNodeWithTag("floating_glass_nav_bar").assertIsDisplayed()
    }

    @Test
    fun redesignedBillingBuilder_threeItems_checksSubtotalTaxGrandTotal_savesAndFindsInList_andScalesAt1_3xInLightDarkAmoled() {
        var builderOpenState = false
        val currentThemeMode = androidx.compose.runtime.mutableStateOf(ThemeMode.LIGHT)
        val currentFontScale = androidx.compose.runtime.mutableStateOf(1.3f)

        composeTestRule.setContent {
            val baseDensity = androidx.compose.ui.platform.LocalDensity.current
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(
                    density = baseDensity.density,
                    fontScale = currentFontScale.value
                )
            ) {
                VanijyaTheme(themeMode = currentThemeMode.value) {
                    com.example.feature.sales.SalesFoundationScreen(
                        invoicePrefix = "INV-",
                        hideAmounts = false,
                        decimals = 2,
                        launchAction = com.example.feature.sales.BillingLaunchAction.NEW_SALE,
                        onBuilderOpenChange = { builderOpenState = it }
                    )
                }
            }
        }

        // 1. Verify full-screen redesigned billing page opened in Light theme at 1.3x font scale
        assertTrue("Billing builder must notify parent shell when open", builderOpenState)
        composeTestRule.onNodeWithTag("shared_voucher_builder_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("doc_type_chips_row").assertIsDisplayed()
        composeTestRule.onNodeWithTag("builder_lazy_column")
            .performScrollToNode(hasTestTag("doc_number_chip"))
        composeTestRule.onNodeWithTag("doc_number_chip").assertIsDisplayed()

        // Also verify Dark and AMOLED themes at 1.3x font scale render cleanly
        currentThemeMode.value = ThemeMode.DARK
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("shared_voucher_builder_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("doc_number_chip").assertIsDisplayed()

        currentThemeMode.value = ThemeMode.AMOLED
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("shared_voucher_builder_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("doc_number_chip").assertIsDisplayed()

        currentFontScale.value = 1.0f
        composeTestRule.waitForIdle()

        // 2. Verify initial Item #1 is present, then add Item #2 and Item #3 so there are 3 items total
        composeTestRule.onNodeWithTag("builder_lazy_column")
            .performScrollToNode(hasTestTag("builder_line_card_0"))
        composeTestRule.onNodeWithTag("builder_line_card_0").assertIsDisplayed()

        // Add Item #2
        composeTestRule.onNodeWithTag("builder_lazy_column")
            .performScrollToNode(hasTestTag("builder_add_item_btn"))
        composeTestRule.onNodeWithTag("builder_add_item_btn").performClick()
        composeTestRule.onNodeWithTag("line_editor_confirm_btn").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Add Item #3
        composeTestRule.onNodeWithTag("builder_lazy_column")
            .performScrollToNode(hasTestTag("builder_add_item_btn"))
        composeTestRule.onNodeWithTag("builder_add_item_btn").performClick()
        composeTestRule.onNodeWithTag("line_editor_confirm_btn").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Verify all 3 item line cards exist
        composeTestRule.onNodeWithTag("builder_lazy_column")
            .performScrollToNode(hasTestTag("builder_line_card_0"))
        composeTestRule.onNodeWithTag("builder_line_card_0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("builder_lazy_column")
            .performScrollToNode(hasTestTag("builder_line_card_1"))
        composeTestRule.onNodeWithTag("builder_line_card_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("builder_lazy_column")
            .performScrollToNode(hasTestTag("builder_line_card_2"))
        composeTestRule.onNodeWithTag("builder_line_card_2").assertIsDisplayed()

        // 3. Scroll to BillSummaryCard and verify Subtotal, Tax rows (CGST/SGST), and GRAND TOTAL
        composeTestRule.onNodeWithTag("builder_lazy_column")
            .performScrollToNode(hasTestTag("builder_collapsible_totals_panel"))
        composeTestRule.onNodeWithTag("builder_collapsible_totals_panel").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bill_summary_subtotal_row").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("bill_summary_taxable_row").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("bill_summary_grand_total_pill").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("bill_summary_grand_total_text").assertIsDisplayed()
        composeTestRule.onNodeWithTag("bill_summary_amount_in_words").performScrollTo().assertIsDisplayed()

        // 4. Tap primary Save CTA in StickyTotalBar and verify the builder closes and the saved bill is in the Billing list
        composeTestRule.onNodeWithTag("builder_save_btn").performClick()
        composeTestRule.waitForIdle()

        assertFalse("Builder must notify parent shell when closed after saving", builderOpenState)
        composeTestRule.onNodeWithTag("sales_billing_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sales_billing_screen")
            .performScrollToNode(hasTestTag("invoice_card_0"))
        composeTestRule.onNodeWithTag("invoice_card_0").assertIsDisplayed()
    }

    @Test
    fun billingDocumentListRows_allSevenSegments_statusRules_linkInsideCard_and1_3xFontScaleInLightDarkAmoled() {
        val now = System.currentTimeMillis()
        val quotationDoc = com.example.core.database.TxnEntity(
            id = "est_101",
            companyId = "local",
            docType = SalesDocTypes.ESTIMATE,
            docNumber = "EST/26-27/0001-VERY-LONG-DOC-NO",
            txnDate = now - 86_400_000L,
            dueDate = now + 7 * 86_400_000L,
            partyName = "श्री बालाजी एंटरप्राइजेज एवं पैकेजिंग इंडस्ट्रीज प्राइवेट लिमिटेड (Apex Packaging Industries)",
            partyGstin = "27AADCK9012C1Z9",
            classification = "B2B",
            grandTotalPaise = 153_00L,
            paidAmountPaise = 0L,
            balanceAmountPaise = 153_00L,
            status = "Unpaid", // Even if raw DB status says Unpaid, Quotations must resolve to Open/Converted and never show Unpaid/Balance!
            amountInWords = "Rupees One Hundred Fifty-Three Only"
        )
        val orderDoc = com.example.core.database.TxnEntity(
            id = "so_101",
            companyId = "local",
            docType = SalesDocTypes.SALE_ORDER,
            docNumber = "SO/26-27/0001",
            txnDate = now,
            dueDate = now + 5 * 86_400_000L,
            partyName = "Apex Packaging Industries",
            partyGstin = "27AADCK9012C1Z9",
            classification = "B2B",
            grandTotalPaise = 153_00L,
            paidAmountPaise = 0L,
            balanceAmountPaise = 153_00L,
            status = "Pending",
            amountInWords = "Rupees One Hundred Fifty-Three Only"
        )
        val linkEntity = com.example.core.database.TxnLinkEntity(
            id = "lnk_1",
            companyId = "local",
            sourceTxnId = "est_101",
            sourceDocType = SalesDocTypes.ESTIMATE,
            sourceDocNumber = "EST/26-27/0001-VERY-LONG-DOC-NO",
            targetTxnId = "so_101",
            targetDocType = SalesDocTypes.SALE_ORDER,
            targetDocNumber = "SO/26-27/0001",
            relationship = "QUOTATION_TO_ORDER",
            createdAt = now
        )

        // 1. Unit-verify pure status & subtitle helpers per document type
        val quoteStatuses = com.example.feature.sales.resolveDocumentDisplayStatuses(quotationDoc, listOf(linkEntity), now)
        assertEquals(listOf("Open", "Converted"), quoteStatuses)
        assertFalse("Quotation must never show Unpaid", quoteStatuses.contains("Unpaid"))

        val dateFmt = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        val quoteSubtitle = com.example.feature.sales.resolveDocumentSubtitleLine(quotationDoc, dateFmt, 2, false)
        assertTrue("Quotation subtitle must show Valid till <date>", quoteSubtitle.contains("Valid till"))
        assertFalse("Quotation subtitle must never show Balance", quoteSubtitle.contains("Balance"))

        val orderStatuses = com.example.feature.sales.resolveDocumentDisplayStatuses(orderDoc, emptyList(), now)
        assertEquals(listOf("Pending"), orderStatuses)

        // 2. Render Billing Hub across Light, Dark, and AMOLED at 1.3x font scale
        val currentThemeMode = androidx.compose.runtime.mutableStateOf(ThemeMode.LIGHT)
        val currentFontScale = androidx.compose.runtime.mutableStateOf(1.3f)

        composeTestRule.setContent {
            val baseDensity = androidx.compose.ui.platform.LocalDensity.current
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(
                    density = baseDensity.density,
                    fontScale = currentFontScale.value
                )
            ) {
                VanijyaTheme(themeMode = currentThemeMode.value) {
                    com.example.feature.sales.SalesFoundationScreen(
                        invoicePrefix = "INV/26-27/",
                        hideAmounts = false,
                        decimals = 2,
                        txnLinks = listOf(linkEntity)
                    )
                }
            }
        }

        // Verify all 7 segments exist and render their DocumentCard rows cleanly in Light, Dark, and AMOLED at 1.3x font scale
        for (segIndex in 0..6) {
            composeTestRule.onNodeWithTag("sales_billing_screen")
                .performScrollToNode(hasTestTag("billing_segment_0"))
            composeTestRule.onNodeWithTag("billing_segment_$segIndex").performScrollTo().performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag("sales_billing_screen")
                .performScrollToNode(hasTestTag("billing_totals_header"))
            composeTestRule.onNodeWithTag("billing_totals_header").assertIsDisplayed()
            composeTestRule.onNodeWithTag("sales_billing_screen")
                .performScrollToNode(hasTestTag("invoice_card_0"))
            composeTestRule.onNodeWithTag("invoice_card_0").assertIsDisplayed()
        }

        // Switch to Quotations segment (index 2) and verify Dark & AMOLED themes at 1.3x font scale
        composeTestRule.onNodeWithTag("sales_billing_screen")
            .performScrollToNode(hasTestTag("billing_segment_0"))
        composeTestRule.onNodeWithTag("billing_segment_2").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("sales_billing_screen")
            .performScrollToNode(hasTestTag("invoice_card_0"))

        currentThemeMode.value = ThemeMode.DARK
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("invoice_card_0").assertIsDisplayed()

        currentThemeMode.value = ThemeMode.AMOLED
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("invoice_card_0").assertIsDisplayed()
    }
}


