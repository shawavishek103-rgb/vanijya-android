package com.example.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.database.ThemeMode
import com.example.core.designsystem.GlassBackgroundBox
import com.example.core.designsystem.GlassCard
import com.example.core.designsystem.VanijyaButton
import com.example.core.designsystem.VanijyaVectorLogoMark
import com.example.core.designsystem.VanijyaWordmark
import com.example.core.designsystem.vanijyaFilterChipColors
import com.example.core.designsystem.vanijyaSwitchColors
import com.example.core.designsystem.vanijyaTextFieldColors
import com.example.core.model.Validators
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LocalVanijyaGlass
import com.example.ui.theme.PurchaseBlue
import com.example.ui.theme.VanijyaTheme
import com.example.ui.theme.VioletPrimary

data class OnboardingFormData(
    val languageCode: String = "en",
    val businessName: String = "",
    val legalName: String = "",
    val logoUri: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val gstin: String = "",
    val pan: String = "",
    val stateCode: String = "27",
    val stateName: String = "Maharashtra",
    val businessType: String = "Retail & Wholesale",
    val fyStartMonth: Int = 4,
    val signatureUri: String = "",
    val upiId: String = "",
    val bankName: String = "",
    val bankAccountNo: String = "",
    val bankIfsc: String = "",
    val gstMode: String = "REGULAR_GST",
    val ownerPin: String = "123456",
    val biometricEnabled: Boolean = true
)

enum class OnboardingFlowStage {
    WELCOME,
    SETUP_STEPS,
    RESTORE_BACKUP
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onCompleteOnboarding: (OnboardingFormData) -> Unit,
    onLanguageSelected: (String) -> Unit,
    initialStage: OnboardingFlowStage = OnboardingFlowStage.WELCOME,
    initialStep: Int = 0
) {
    // Always open in Light Glassmorphism theme on first run regardless of the phone's system theme.
    // The user can optionally toggle Light/Dark via the top-corner button on the welcome screen.
    var isDarkOverride by rememberSaveable { mutableStateOf(false) }
    var stage by rememberSaveable { mutableStateOf(initialStage) }
    var currentStep by rememberSaveable { mutableIntStateOf(initialStep) }

    var languageCode by rememberSaveable { mutableStateOf("en") }
    var businessName by rememberSaveable { mutableStateOf("Shree Ganesh Enterprises") }
    var legalName by rememberSaveable { mutableStateOf("") }
    var logoUri by rememberSaveable { mutableStateOf("") }
    var signatureUri by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("MG Road, Pune") }
    var phone by rememberSaveable { mutableStateOf("9876543210") }
    var email by rememberSaveable { mutableStateOf("accounts@ganeshtraders.in") }
    var gstin by rememberSaveable { mutableStateOf("") }
    var pan by rememberSaveable { mutableStateOf("") }
    var stateCode by rememberSaveable { mutableStateOf("27") }
    var stateName by rememberSaveable { mutableStateOf("Maharashtra") }
    var businessType by rememberSaveable { mutableStateOf("Retail & Wholesale") }
    var fyStartMonth by rememberSaveable { mutableIntStateOf(4) }
    var upiId by rememberSaveable { mutableStateOf("ganeshtraders@okicici") }
    var bankName by rememberSaveable { mutableStateOf("HDFC Bank") }
    var bankAccountNo by rememberSaveable { mutableStateOf("") }
    var bankIfsc by rememberSaveable { mutableStateOf("") }
    var gstMode by rememberSaveable { mutableStateOf("REGULAR_GST") }
    var pinInput by rememberSaveable { mutableStateOf("123456") }
    var pinConfirm by rememberSaveable { mutableStateOf("123456") }
    var biometricEnabled by rememberSaveable { mutableStateOf(true) }
    var pinError by rememberSaveable { mutableStateOf<String?>(null) }
    var businessNameError by rememberSaveable { mutableStateOf<String?>(null) }

    // Restore from backup state
    var selectedBackupUri by rememberSaveable { mutableStateOf("") }
    var restoreFirmName by rememberSaveable { mutableStateOf("Restored Business Vault") }
    var restorePin by rememberSaveable { mutableStateOf("123456") }
    var restoreError by rememberSaveable { mutableStateOf<String?>(null) }

    // Zero-permission Android Photo Pickers & Document Picker
    val logoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) logoUri = uri.toString()
    }
    val signaturePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) signatureUri = uri.toString()
    }
    val backupFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            selectedBackupUri = uri.toString()
            restoreError = null
        }
    }

    val gstinValidation = remember(gstin) { Validators.validateGstin(gstin, verifyChecksumStrictly = false) }
    val panValidation = remember(pan) { Validators.validatePan(pan) }
    val upiValidation = remember(upiId) { Validators.validateUpiId(upiId) }
    val phoneValid = remember(phone) { phone.isEmpty() || phone.length == 10 }

    // System back button navigation inside Onboarding
    BackHandler(enabled = stage != OnboardingFlowStage.WELCOME) {
        when (stage) {
            OnboardingFlowStage.RESTORE_BACKUP -> stage = OnboardingFlowStage.WELCOME
            OnboardingFlowStage.SETUP_STEPS -> {
                if (currentStep > 0) {
                    currentStep--
                } else {
                    stage = OnboardingFlowStage.WELCOME
                }
            }
            OnboardingFlowStage.WELCOME -> Unit
        }
    }

    val activeOnboardingTheme = if (isDarkOverride) ThemeMode.DARK else ThemeMode.LIGHT

    VanijyaTheme(themeMode = activeOnboardingTheme) {
        val glass = LocalVanijyaGlass.current

        GlassBackgroundBox(
            modifier = Modifier
                .fillMaxSize()
                .testTag(
                    if (isDarkOverride) "onboarding_root_dark" else "onboarding_root_light"
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                contentAlignment = Alignment.TopCenter
            ) {
                AnimatedContent(
                    targetState = stage,
                    label = "onboarding_stage_transition"
                ) { currentStage ->
                    when (currentStage) {
                        OnboardingFlowStage.WELCOME -> {
                            WelcomeLightGlassScreen(
                                languageCode = languageCode,
                                isDarkOverride = isDarkOverride,
                                onToggleLanguage = {
                                    val nextLang = if (languageCode == "en") "hi" else "en"
                                    languageCode = nextLang
                                    onLanguageSelected(nextLang)
                                },
                                onToggleTheme = { isDarkOverride = !isDarkOverride },
                                onSetupNewBusiness = {
                                    currentStep = 0
                                    stage = OnboardingFlowStage.SETUP_STEPS
                                },
                                onOpenRestoreBackup = {
                                    stage = OnboardingFlowStage.RESTORE_BACKUP
                                }
                            )
                        }

                        OnboardingFlowStage.RESTORE_BACKUP -> {
                            RestoreFromBackupGlassScreen(
                                selectedBackupUri = selectedBackupUri,
                                restoreFirmName = restoreFirmName,
                                onRestoreFirmNameChange = {
                                    restoreFirmName = it
                                    restoreError = null
                                },
                                restorePin = restorePin,
                                onRestorePinChange = {
                                    restorePin = it.filter(Char::isDigit).take(6)
                                    restoreError = null
                                },
                                restoreError = restoreError,
                                isDarkOverride = isDarkOverride,
                                onToggleTheme = { isDarkOverride = !isDarkOverride },
                                onPickBackupFile = {
                                    backupFilePicker.launch(arrayOf("*/*"))
                                },
                                onBackToWelcome = {
                                    stage = OnboardingFlowStage.WELCOME
                                },
                                onConfirmRestore = {
                                    if (restoreFirmName.isBlank()) {
                                        restoreError = "Please enter a business name for the restored vault"
                                    } else if (restorePin.length != 6) {
                                        restoreError = "Please enter the 6-digit backup PIN"
                                    } else {
                                        onCompleteOnboarding(
                                            OnboardingFormData(
                                                languageCode = languageCode,
                                                businessName = restoreFirmName.trim(),
                                                legalName = restoreFirmName.trim(),
                                                address = "Restored from ${selectedBackupUri.ifBlank { "local .vbk archive" }}",
                                                phone = phone,
                                                email = email,
                                                stateCode = stateCode,
                                                stateName = stateName,
                                                businessType = businessType,
                                                fyStartMonth = fyStartMonth,
                                                gstMode = gstMode,
                                                ownerPin = restorePin,
                                                biometricEnabled = biometricEnabled
                                            )
                                        )
                                    }
                                }
                            )
                        }

                        OnboardingFlowStage.SETUP_STEPS -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .widthIn(max = 600.dp)
                                    .padding(horizontal = 20.dp, vertical = 14.dp)
                                    .testTag("onboarding_setup_steps_screen")
                            ) {
                                // Top bar: Back button + Step Indicator ("1 of 4") + Light/Dark toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Back button on every onboarding screen (48dp+ touch target)
                                    GlassPillChip(
                                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                                        text = stringResource(R.string.onboarding_back),
                                        onClick = {
                                            if (currentStep > 0) {
                                                currentStep--
                                            } else {
                                                stage = OnboardingFlowStage.WELCOME
                                            }
                                        },
                                        testTag = "onboarding_top_back_button"
                                    )

                                    // Step indicator pill (e.g., "Step 1 of 4")
                                    Surface(
                                        color = VioletPrimary.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .border(
                                                width = 1.dp,
                                                color = VioletPrimary.copy(alpha = 0.35f),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .testTag("onboarding_step_indicator")
                                    ) {
                                        Text(
                                            text = "Step ${currentStep + 1} of 4",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }

                                    // Light/Dark toggle in the top corner
                                    ThemeToggleChip(
                                        isDark = isDarkOverride,
                                        onToggle = { isDarkOverride = !isDarkOverride },
                                        testTag = "onboarding_step_theme_toggle"
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Brand Wordmark & Current Step Title
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    VanijyaWordmark(showTagline = false, iconSize = 36.dp)
                                    Text(
                                        text = when (currentStep) {
                                            0 -> "Language Selection"
                                            1 -> "Business Profile Setup"
                                            2 -> "GST & Accounting Mode"
                                            else -> "PIN & Biometric Creation"
                                        },
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                LinearProgressIndicator(
                                    progress = { (currentStep + 1) / 4f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = VioletPrimary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                AnimatedContent(
                                    targetState = currentStep,
                                    modifier = Modifier.weight(1f),
                                    label = "onboarding_step"
                                ) { step ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        when (step) {
                                            0 -> {
                                                GlassCard(cornerRadius = 26.dp) {
                                                    Column {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                imageVector = Icons.Default.Language,
                                                                contentDescription = "Language",
                                                                tint = MaterialTheme.colorScheme.primary
                                                            )
                                                            Spacer(modifier = Modifier.width(10.dp))
                                                            Text(
                                                                text = "Select App Language / भाषा चुनें",
                                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Text(
                                                            text = "All screens, GST invoices, and number formats adapt immediately. You can change this anytime in Settings.",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        Spacer(modifier = Modifier.height(12.dp))
                                                        val langs = listOf(
                                                            Triple("en", "English (India)", "Indian Lakh/Crore grouping (₹3,53,000.00)"),
                                                            Triple("hi", "हिन्दी (भारत)", "भारतीय अंक प्रणाली और हिंदी लेबल")
                                                        )
                                                        langs.forEach { (code, title, subtitle) ->
                                                            val selected = languageCode == code
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(vertical = 6.dp)
                                                                    .defaultMinSize(minHeight = 64.dp)
                                                                    .clip(RoundedCornerShape(18.dp))
                                                                    .background(
                                                                        if (selected) MaterialTheme.colorScheme.primaryContainer
                                                                        else glass.pillFill
                                                                    )
                                                                    .border(
                                                                        width = if (selected) 2.dp else 1.dp,
                                                                        color = if (selected) VioletPrimary
                                                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                                                        shape = RoundedCornerShape(18.dp)
                                                                    )
                                                                    .clickable {
                                                                        languageCode = code
                                                                        onLanguageSelected(code)
                                                                    }
                                                                    .padding(16.dp)
                                                                    .testTag("lang_option_$code"),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                Column(modifier = Modifier.weight(1f)) {
                                                                    Text(
                                                                        text = title,
                                                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                                        color = MaterialTheme.colorScheme.onSurface
                                                                    )
                                                                    Text(
                                                                        text = subtitle,
                                                                        style = MaterialTheme.typography.bodySmall,
                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                    )
                                                                }
                                                                if (selected) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.CheckCircle,
                                                                        contentDescription = "Selected",
                                                                        tint = EmeraldPrimary
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                GlassCard(cornerRadius = 24.dp) {
                                                    Column {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                imageVector = Icons.Default.Shield,
                                                                contentDescription = "Offline",
                                                                tint = EmeraldPrimary,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = "Zero-Cloud Privacy Guarantee",
                                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                                color = EmeraldPrimary
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Text(
                                                            text = "Vanijya does not request the INTERNET permission. Every invoice, ledger, stock count, and staff payslip stays inside your hardware-encrypted SQLCipher vault on this phone.",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }

                                            1 -> {
                                                GlassCard(cornerRadius = 26.dp) {
                                                    Column(
                                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                imageVector = Icons.Default.Business,
                                                                contentDescription = "Business Profile",
                                                                tint = MaterialTheme.colorScheme.primary
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = "Business Identity & Tax Details",
                                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                        }

                                                        OutlinedTextField(
                                                            value = businessName,
                                                            onValueChange = {
                                                                businessName = it
                                                                businessNameError = null
                                                            },
                                                            label = { Text("Business / Trade Name *") },
                                                            singleLine = true,
                                                            isError = businessNameError != null,
                                                            supportingText = {
                                                                if (businessNameError != null) {
                                                                    Text(businessNameError!!)
                                                                } else {
                                                                    Text(
                                                                        text = "Printed at the top of your GST invoices and PDF statements",
                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                    )
                                                                }
                                                            },
                                                            colors = vanijyaTextFieldColors(),
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .testTag("onboarding_business_name")
                                                        )

                                                        OutlinedTextField(
                                                            value = gstin,
                                                            onValueChange = { raw ->
                                                                val upper = raw.uppercase().take(15)
                                                                gstin = upper
                                                                Validators.extractPanFromGstin(upper)?.let { pan = it }
                                                                Validators.extractStateFromGstin(upper)?.let { st ->
                                                                    stateCode = st.code
                                                                    stateName = st.name
                                                                }
                                                            },
                                                            label = { Text("GSTIN (15-digit, optional)") },
                                                            placeholder = { Text("27AAPFU0939F1ZV") },
                                                            singleLine = true,
                                                            isError = gstinValidation is Validators.ValidationResult.Invalid,
                                                            supportingText = {
                                                                when (gstinValidation) {
                                                                    is Validators.ValidationResult.Invalid -> Text(gstinValidation.reason)
                                                                    Validators.ValidationResult.Valid -> if (gstin.isNotEmpty()) {
                                                                        Text("✓ Valid GSTIN • State: $stateCode ($stateName)", color = EmeraldPrimary)
                                                                    } else {
                                                                        Text(
                                                                            text = "Auto-fills PAN & State when entered",
                                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                        )
                                                                    }
                                                                }
                                                            },
                                                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                                            colors = vanijyaTextFieldColors(),
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .testTag("onboarding_gstin_input")
                                                        )

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                        ) {
                                                            OutlinedTextField(
                                                                value = pan,
                                                                onValueChange = { pan = it.uppercase().take(10) },
                                                                label = { Text("PAN") },
                                                                singleLine = true,
                                                                isError = panValidation is Validators.ValidationResult.Invalid,
                                                                supportingText = {
                                                                    if (panValidation is Validators.ValidationResult.Invalid) {
                                                                        Text(panValidation.reason)
                                                                    }
                                                                },
                                                                colors = vanijyaTextFieldColors(),
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                            OutlinedTextField(
                                                                value = phone,
                                                                onValueChange = { phone = it.filter(Char::isDigit).take(10) },
                                                                label = { Text("Mobile") },
                                                                singleLine = true,
                                                                isError = !phoneValid,
                                                                supportingText = {
                                                                    if (!phoneValid) {
                                                                        Text("10 digits required")
                                                                    }
                                                                },
                                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                                                colors = vanijyaTextFieldColors(),
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                        }

                                                        // Indian State Dropdown
                                                        var stateExpanded by remember { mutableStateOf(false) }
                                                        ExposedDropdownMenuBox(
                                                            expanded = stateExpanded,
                                                            onExpandedChange = { stateExpanded = !stateExpanded }
                                                        ) {
                                                            OutlinedTextField(
                                                                value = "$stateCode - $stateName",
                                                                onValueChange = {},
                                                                readOnly = true,
                                                                label = { Text("Place of Supply (State)") },
                                                                trailingIcon = {
                                                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = stateExpanded)
                                                                },
                                                                colors = vanijyaTextFieldColors(),
                                                                modifier = Modifier
                                                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                                                    .fillMaxWidth()
                                                            )
                                                            ExposedDropdownMenu(
                                                                expanded = stateExpanded,
                                                                onDismissRequest = { stateExpanded = false },
                                                                containerColor = MaterialTheme.colorScheme.surface
                                                            ) {
                                                                Validators.indianStates.forEach { st ->
                                                                    DropdownMenuItem(
                                                                        text = {
                                                                            Text(
                                                                                text = "${st.code} - ${st.name}",
                                                                                color = MaterialTheme.colorScheme.onSurface
                                                                            )
                                                                        },
                                                                        onClick = {
                                                                            stateCode = st.code
                                                                            stateName = st.name
                                                                            stateExpanded = false
                                                                        }
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        OutlinedTextField(
                                                            value = address,
                                                            onValueChange = { address = it },
                                                            label = { Text("Shop / Office Address") },
                                                            colors = vanijyaTextFieldColors(),
                                                            modifier = Modifier.fillMaxWidth()
                                                        )

                                                        OutlinedTextField(
                                                            value = email,
                                                            onValueChange = { email = it },
                                                            label = { Text("Business Email") },
                                                            singleLine = true,
                                                            colors = vanijyaTextFieldColors(),
                                                            modifier = Modifier.fillMaxWidth()
                                                        )

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                        ) {
                                                            OutlinedTextField(
                                                                value = upiId,
                                                                onValueChange = { upiId = it },
                                                                label = { Text("UPI ID (for QR)") },
                                                                singleLine = true,
                                                                isError = upiValidation is Validators.ValidationResult.Invalid,
                                                                colors = vanijyaTextFieldColors(),
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                            OutlinedTextField(
                                                                value = bankName,
                                                                onValueChange = { bankName = it },
                                                                label = { Text("Bank Name") },
                                                                singleLine = true,
                                                                colors = vanijyaTextFieldColors(),
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                        }

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                        ) {
                                                            OutlinedTextField(
                                                                value = bankAccountNo,
                                                                onValueChange = { bankAccountNo = it.filter(Char::isDigit).take(18) },
                                                                label = { Text("Bank A/C No.") },
                                                                singleLine = true,
                                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                                colors = vanijyaTextFieldColors(),
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                            OutlinedTextField(
                                                                value = bankIfsc,
                                                                onValueChange = { bankIfsc = it.uppercase().take(11) },
                                                                label = { Text("IFSC Code") },
                                                                singleLine = true,
                                                                colors = vanijyaTextFieldColors(),
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                        }

                                                        // Logo & Signature Zero-Permission Photo Pickers
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                        ) {
                                                            VanijyaButton(
                                                                text = if (logoUri.isBlank()) "Pick Logo" else "Logo Selected ✓",
                                                                icon = Icons.Default.Image,
                                                                isOutlined = true,
                                                                onClick = {
                                                                    logoPicker.launch(
                                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                                    )
                                                                },
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                            VanijyaButton(
                                                                text = if (signatureUri.isBlank()) "Pick Signature" else "Signature ✓",
                                                                icon = Icons.Default.Draw,
                                                                isOutlined = true,
                                                                onClick = {
                                                                    signaturePicker.launch(
                                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                                    )
                                                                },
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            2 -> {
                                                GlassCard(cornerRadius = 26.dp) {
                                                    Column(
                                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                                                contentDescription = "GST Mode",
                                                                tint = MaterialTheme.colorScheme.primary
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = "Taxation, Business Type & Financial Year",
                                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                        }

                                                        val modes = listOf(
                                                            Triple("REGULAR_GST", "Regular GST Registered", "Tax Invoices with HSN/SAC, CGST/SGST/IGST & GSTR reports"),
                                                            Triple("COMPOSITION", "GST Composition Scheme", "Bill of Supply without tax collection from buyers"),
                                                            Triple("NON_GST", "Non-GST / Unregistered Business", "Simple retail/wholesale billing without GST fields")
                                                        )
                                                        modes.forEach { (code, title, desc) ->
                                                            val selected = gstMode == code
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .defaultMinSize(minHeight = 62.dp)
                                                                    .clip(RoundedCornerShape(18.dp))
                                                                    .background(
                                                                        if (selected) MaterialTheme.colorScheme.primaryContainer
                                                                        else glass.pillFill
                                                                    )
                                                                    .border(
                                                                        width = if (selected) 2.dp else 1.dp,
                                                                        color = if (selected) VioletPrimary
                                                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                                        shape = RoundedCornerShape(18.dp)
                                                                    )
                                                                    .clickable { gstMode = code }
                                                                    .padding(14.dp)
                                                                    .testTag("gst_mode_$code"),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                Column(modifier = Modifier.weight(1f)) {
                                                                    Text(
                                                                        text = title,
                                                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                                    )
                                                                    Text(
                                                                        text = desc,
                                                                        style = MaterialTheme.typography.bodySmall,
                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                    )
                                                                }
                                                                if (selected) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.CheckCircle,
                                                                        contentDescription = "Selected",
                                                                        tint = EmeraldPrimary
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        Text(
                                                            text = "Business Category",
                                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        val categories = listOf(
                                                            "Retail & Wholesale",
                                                            "Kirana & FMCG",
                                                            "Distributor / Agency",
                                                            "Manufacturing",
                                                            "Pharmacy / Medical",
                                                            "Services & Freelance"
                                                        )
                                                        FlowRow(
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            categories.forEach { cat ->
                                                                FilterChip(
                                                                    selected = businessType == cat,
                                                                    onClick = { businessType = cat },
                                                                    label = { Text(cat) },
                                                                    colors = vanijyaFilterChipColors()
                                                                )
                                                            }
                                                        }

                                                        Text(
                                                            text = "Financial Year Start",
                                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                            FilterChip(
                                                                selected = fyStartMonth == 4,
                                                                onClick = { fyStartMonth = 4 },
                                                                label = { Text("1 April (Indian FY 2026-27)") },
                                                                colors = vanijyaFilterChipColors()
                                                            )
                                                            FilterChip(
                                                                selected = fyStartMonth == 1,
                                                                onClick = { fyStartMonth = 1 },
                                                                label = { Text("1 January (Calendar Year)") },
                                                                colors = vanijyaFilterChipColors()
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            else -> {
                                                GlassCard(cornerRadius = 26.dp) {
                                                    Column(
                                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                imageVector = Icons.Default.Lock,
                                                                contentDescription = "App Lock",
                                                                tint = VioletPrimary
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = "Create 6-Digit Owner PIN & Biometrics",
                                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                        }
                                                        Text(
                                                            text = "Your PIN is salted and hashed with PBKDF2-HMAC-SHA256. You can also switch between Owner, Manager (222222), Salesperson (333333), Accountant (444444), and Viewer (555555) using PINs.",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )

                                                        OutlinedTextField(
                                                            value = pinInput,
                                                            onValueChange = {
                                                                pinInput = it.filter(Char::isDigit).take(6)
                                                                pinError = null
                                                            },
                                                            label = { Text("6-Digit Owner PIN") },
                                                            singleLine = true,
                                                            visualTransformation = PasswordVisualTransformation(),
                                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                                            supportingText = {
                                                                if (pinInput.length == 6) {
                                                                    Text("✓ 6 digits entered", color = EmeraldPrimary)
                                                                } else {
                                                                    Text("${pinInput.length}/6 digits", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                }
                                                            },
                                                            colors = vanijyaTextFieldColors(),
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .testTag("onboarding_pin_input")
                                                        )

                                                        OutlinedTextField(
                                                            value = pinConfirm,
                                                            onValueChange = {
                                                                pinConfirm = it.filter(Char::isDigit).take(6)
                                                                pinError = null
                                                            },
                                                            label = { Text("Confirm 6-Digit PIN") },
                                                            singleLine = true,
                                                            visualTransformation = PasswordVisualTransformation(),
                                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                                            isError = pinError != null,
                                                            supportingText = {
                                                                if (pinError != null) {
                                                                    Text(pinError!!)
                                                                } else if (pinConfirm.length == 6 && pinConfirm == pinInput) {
                                                                    Text("✓ PINs match", color = EmeraldPrimary)
                                                                }
                                                            },
                                                            colors = vanijyaTextFieldColors(),
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .testTag("onboarding_pin_confirm")
                                                        )

                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(18.dp))
                                                                .background(glass.pillFill)
                                                                .border(
                                                                    width = 1.dp,
                                                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                                                    shape = RoundedCornerShape(18.dp)
                                                                )
                                                                .padding(14.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                modifier = Modifier.weight(1f)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Fingerprint,
                                                                    contentDescription = "Biometric",
                                                                    tint = EmeraldPrimary
                                                                )
                                                                Spacer(modifier = Modifier.width(10.dp))
                                                                Column {
                                                                    Text(
                                                                        text = "Enable Fingerprint / Biometric Unlock",
                                                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                                        color = MaterialTheme.colorScheme.onSurface
                                                                    )
                                                                    Text(
                                                                        text = "Automatically falls back to 6-digit PIN on emulators",
                                                                        style = MaterialTheme.typography.bodySmall,
                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                    )
                                                                }
                                                            }
                                                            Switch(
                                                                checked = biometricEnabled,
                                                                onCheckedChange = { biometricEnabled = it },
                                                                colors = vanijyaSwitchColors()
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Bottom navigation buttons (Back is always visible on all 4 steps)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    VanijyaButton(
                                        text = stringResource(R.string.onboarding_back),
                                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                                        isOutlined = true,
                                        onClick = {
                                            if (currentStep > 0) {
                                                currentStep--
                                            } else {
                                                stage = OnboardingFlowStage.WELCOME
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        testTag = "onboarding_back_button"
                                    )
                                    VanijyaButton(
                                        text = if (currentStep < 3) {
                                            stringResource(R.string.onboarding_continue)
                                        } else {
                                            "Launch Vanijya Vault"
                                        },
                                        icon = if (currentStep < 3) {
                                            Icons.AutoMirrored.Filled.ArrowForward
                                        } else {
                                            Icons.Default.AccountBalance
                                        },
                                        containerColor = VioletPrimary,
                                        contentColor = Color.White,
                                        onClick = {
                                            when (currentStep) {
                                                1 -> {
                                                    if (businessName.isBlank()) {
                                                        businessNameError = "Business / Trade Name is required"
                                                    } else if (gstinValidation is Validators.ValidationResult.Invalid) {
                                                        // Keep inline error visible
                                                    } else {
                                                        currentStep++
                                                    }
                                                }
                                                0, 2 -> currentStep++
                                                else -> {
                                                    if (pinInput.length != 6 || pinInput != pinConfirm) {
                                                        pinError = "PIN must be 6 digits and both fields must match"
                                                    } else {
                                                        onCompleteOnboarding(
                                                            OnboardingFormData(
                                                                languageCode = languageCode,
                                                                businessName = businessName.ifBlank { "Shree Ganesh Enterprises" },
                                                                legalName = legalName.ifBlank { businessName },
                                                                logoUri = logoUri,
                                                                address = address,
                                                                phone = phone,
                                                                email = email,
                                                                gstin = gstin,
                                                                pan = pan,
                                                                stateCode = stateCode,
                                                                stateName = stateName,
                                                                businessType = businessType,
                                                                fyStartMonth = fyStartMonth,
                                                                signatureUri = signatureUri,
                                                                upiId = upiId,
                                                                bankName = bankName,
                                                                bankAccountNo = bankAccountNo,
                                                                bankIfsc = bankIfsc,
                                                                gstMode = gstMode,
                                                                ownerPin = pinInput,
                                                                biometricEnabled = biometricEnabled
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1.4f),
                                        testTag = "onboarding_next_button"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * First-run Welcome screen in Light Glassmorphism:
 * - Top corners: Language chip + Light/Dark theme toggle
 * - Top/Center: Coloured Vanijya logo, wordmark, and tagline "Your business, in your pocket. Fully offline."
 * - Frosted white glass showcase card with short friendly headline & feature highlights
 * - Lower half: Two large buttons ("Set up new business" primary indigo-violet #5B3FD9 + "Restore from backup" glass secondary)
 * - Footer line: "Works fully offline. Your data stays on your phone."
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WelcomeLightGlassScreen(
    languageCode: String,
    isDarkOverride: Boolean,
    onToggleLanguage: () -> Unit,
    onToggleTheme: () -> Unit,
    onSetupNewBusiness: () -> Unit,
    onOpenRestoreBackup: () -> Unit
) {
    val glass = LocalVanijyaGlass.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .padding(horizontal = 22.dp, vertical = 14.dp)
            .testTag("welcome_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Top Corner Bar: Language Chip (left) + Light/Dark Toggle (right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassPillChip(
                icon = Icons.Default.Language,
                text = if (languageCode == "hi") "हिन्दी • HI" else "English • EN",
                onClick = onToggleLanguage,
                testTag = "welcome_language_chip"
            )

            ThemeToggleChip(
                isDark = isDarkOverride,
                onToggle = onToggleTheme,
                testTag = "welcome_theme_toggle"
            )
        }

        // 2. Hero Brand Identity: Coloured Vanijya Logo, Wordmark & Tagline (scrollable on compact heights)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(26.dp),
                        ambientColor = VioletPrimary.copy(alpha = 0.25f),
                        spotColor = VioletPrimary.copy(alpha = 0.25f)
                    )
                    .clip(RoundedCornerShape(26.dp))
                    .background(glass.cardFillElevated)
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(glass.borderGradientStart, glass.borderGradientEnd)
                        ),
                        shape = RoundedCornerShape(26.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                VanijyaVectorLogoMark(size = 62.dp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Vanijya",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.8).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Your business, in your pocket. Fully offline.",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("welcome_tagline")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Frosted White Glass Feature Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 28.dp,
                elevated = true,
                contentPadding = PaddingValues(18.dp)
            ) {
                Text(
                    text = stringResource(R.string.welcome_headline),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("welcome_headline")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.welcome_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = glass.subtleDivider)
                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WelcomeFeatureBadge(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        label = "GST Billing",
                        accent = VioletPrimary
                    )
                    WelcomeFeatureBadge(
                        icon = Icons.Default.Inventory2,
                        label = "Stock & Godowns",
                        accent = EmeraldPrimary
                    )
                    WelcomeFeatureBadge(
                        icon = Icons.Default.People,
                        label = "Party Khata",
                        accent = PurchaseBlue
                    )
                    WelcomeFeatureBadge(
                        icon = Icons.Default.AccountBalance,
                        label = "Staff Payroll",
                        accent = GoldPrimary
                    )
                }
            }
        }

        // 3. Lower Half: Two Large Action Buttons + Offline Guarantee Line
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Primary Filled Button (#5B3FD9 Indigo-Violet with white text)
            VanijyaButton(
                text = stringResource(R.string.welcome_setup_new_business),
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                containerColor = VioletPrimary,
                contentColor = Color.White,
                onClick = onSetupNewBusiness,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 54.dp),
                testTag = "welcome_setup_new_business_btn"
            )

            // Secondary Frosted Glass Button ("Restore from backup")
            GlassSecondaryActionButton(
                text = stringResource(R.string.welcome_restore_from_backup),
                icon = Icons.Default.Backup,
                onClick = onOpenRestoreBackup,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 54.dp),
                testTag = "welcome_restore_backup_btn"
            )

            // Small offline guarantee line at the bottom
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .testTag("welcome_offline_footer")
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Offline Vault",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Works fully offline. Your data stays on your phone.",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Restore-from-backup flow screen in Light Glassmorphism.
 */
@Composable
private fun RestoreFromBackupGlassScreen(
    selectedBackupUri: String,
    restoreFirmName: String,
    onRestoreFirmNameChange: (String) -> Unit,
    restorePin: String,
    onRestorePinChange: (String) -> Unit,
    restoreError: String?,
    isDarkOverride: Boolean,
    onToggleTheme: () -> Unit,
    onPickBackupFile: () -> Unit,
    onBackToWelcome: () -> Unit,
    onConfirmRestore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("onboarding_restore_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Header with Back button + Light/Dark toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassPillChip(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                text = stringResource(R.string.onboarding_back),
                onClick = onBackToWelcome,
                testTag = "restore_back_button"
            )

            Surface(
                color = EmeraldPrimary.copy(alpha = 0.14f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(
                    width = 1.dp,
                    color = EmeraldPrimary.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(16.dp)
                )
            ) {
                Text(
                    text = "Offline .vbk Restore",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }

            ThemeToggleChip(
                isDark = isDarkOverride,
                onToggle = onToggleTheme,
                testTag = "restore_theme_toggle"
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            VanijyaWordmark(showTagline = true, iconSize = 40.dp)

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                elevated = true
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Backup,
                        contentDescription = "Restore from backup",
                        tint = VioletPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.restore_screen_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.restore_screen_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                GlassSecondaryActionButton(
                    text = if (selectedBackupUri.isBlank()) {
                        stringResource(R.string.restore_pick_file)
                    } else {
                        "Backup File Selected ✓"
                    },
                    icon = Icons.Default.FolderOpen,
                    onClick = onPickBackupFile,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "restore_pick_file_button"
                )

                if (selectedBackupUri.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✓ Archive ready: $selectedBackupUri",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = EmeraldPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = restoreFirmName,
                    onValueChange = onRestoreFirmNameChange,
                    label = { Text("Business / Firm Name *") },
                    singleLine = true,
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("restore_firm_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = restorePin,
                    onValueChange = onRestorePinChange,
                    label = { Text("6-Digit Backup / Owner PIN *") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = restoreError != null,
                    supportingText = {
                        if (restoreError != null) {
                            Text(restoreError)
                        } else {
                            Text(
                                text = "Used to decrypt the .vbk archive and unlock your restored vault",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = vanijyaTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("restore_pin_input")
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VanijyaButton(
                text = stringResource(R.string.onboarding_back),
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                isOutlined = true,
                onClick = onBackToWelcome,
                modifier = Modifier.weight(1f),
                testTag = "restore_bottom_back_button"
            )
            VanijyaButton(
                text = stringResource(R.string.restore_confirm_action),
                icon = Icons.Default.CheckCircle,
                containerColor = VioletPrimary,
                contentColor = Color.White,
                onClick = onConfirmRestore,
                modifier = Modifier.weight(1.5f),
                testTag = "restore_confirm_button"
            )
        }
    }
}

@Composable
private fun GlassSecondaryActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    val glass = LocalVanijyaGlass.current
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .shadow(
                elevation = if (glass.isAmoled) 0.dp else 8.dp,
                shape = shape,
                ambientColor = glass.shadowColor,
                spotColor = glass.shadowColor
            )
            .clip(shape)
            .background(glass.cardFillElevated)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        glass.borderGradientStart,
                        VioletPrimary.copy(alpha = 0.35f)
                    )
                ),
                shape = shape
            )
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = VioletPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun GlassPillChip(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    testTag: String
) {
    val glass = LocalVanijyaGlass.current
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .minimumInteractiveComponentSize()
            .clip(shape)
            .background(glass.cardFillElevated)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(glass.borderGradientStart, glass.borderGradientEnd)
                ),
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = VioletPrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ThemeToggleChip(
    isDark: Boolean,
    onToggle: () -> Unit,
    testTag: String
) {
    val glass = LocalVanijyaGlass.current
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .minimumInteractiveComponentSize()
            .clip(shape)
            .background(glass.cardFillElevated)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(glass.borderGradientStart, glass.borderGradientEnd)
                ),
                shape = shape
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
            contentDescription = if (isDark) "Switch to Light Theme" else "Switch to Dark Theme",
            tint = if (isDark) GoldPrimary else VioletPrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isDark) "Dark" else "Light",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun WelcomeFeatureBadge(
    icon: ImageVector,
    label: String,
    accent: Color
) {
    Surface(
        color = accent.copy(alpha = 0.12f),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.border(
            width = 1.dp,
            color = accent.copy(alpha = 0.30f),
            shape = RoundedCornerShape(14.dp)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accent,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
