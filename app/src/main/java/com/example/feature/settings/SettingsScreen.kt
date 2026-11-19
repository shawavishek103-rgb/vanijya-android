package com.example.feature.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.BuildConfig
import com.example.R
import com.example.core.database.AppPreferencesState
import com.example.core.database.AppUserEntity
import com.example.core.database.AutoLockTimeout
import com.example.core.database.CompanyEntity
import com.example.core.database.RolePermissionEntity
import com.example.core.database.RoundingModeOption
import com.example.core.database.TaxRateEntity
import com.example.core.database.ThemeMode
import com.example.core.designsystem.GlassCard
import com.example.core.designsystem.VanijyaButton
import com.example.core.designsystem.vanijyaCheckboxColors
import com.example.core.designsystem.vanijyaFilterChipColors
import com.example.core.designsystem.vanijyaSwitchColors
import com.example.core.designsystem.vanijyaTextFieldColors
import com.example.core.model.AppRole
import com.example.core.model.Validators
import com.example.core.security.BiometricAuthHelper
import com.example.ui.theme.CoralError
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.PurchaseBlue

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    prefs: AppPreferencesState,
    companies: List<CompanyEntity>,
    activeCompany: CompanyEntity?,
    users: List<AppUserEntity>,
    taxRates: List<TaxRateEntity>,
    rolePermissions: List<RolePermissionEntity>,
    onBack: () -> Unit,
    onUpdateTheme: (ThemeMode) -> Unit,
    onUpdateLanguage: (String) -> Unit,
    onSaveBusinessProfile: (CompanyEntity) -> Unit,
    onSaveInvoiceSettings: (String, Long, Int) -> Unit,
    onUpdateRoundingMode: (RoundingModeOption) -> Unit,
    onUpdateDecimals: (Int) -> Unit,
    onUpdateDateFormat: (String) -> Unit,
    onChangePin: (String, String, (Boolean, String) -> Unit) -> Unit,
    onUpdateBiometricEnabled: (Boolean) -> Unit,
    onUpdateAutoLock: (AutoLockTimeout) -> Unit,
    onUpdateHideAmounts: (Boolean) -> Unit,
    onUpdateBlockScreenshots: (Boolean) -> Unit,
    onAddLocalUser: (String, AppRole, String, (Boolean, String) -> Unit) -> Unit,
    onUpdateRolePermission: (RolePermissionEntity) -> Unit,
    onUpdateTaxRate: (TaxRateEntity) -> Unit,
    onSwitchCompany: (CompanyEntity) -> Unit,
    onCreateAdditionalCompany: (String, String) -> Unit,
    onVerifyPin: (String, (Boolean) -> Unit) -> Unit = { pin, cb -> cb(pin.length == 6) },
    onVerifyPinAndResetApp: (String, (Boolean) -> Unit) -> Unit,
    onBiometricResetApp: () -> Unit
) {
    val context = LocalContext.current
    val textFieldColors = vanijyaTextFieldColors()
    val filterChipColors = vanijyaFilterChipColors()

    // Business Profile Form State (initialized from activeCompany)
    var bizName by remember(activeCompany?.id, activeCompany?.businessName) {
        mutableStateOf(activeCompany?.businessName ?: "Shree Ganesh Enterprises")
    }
    var bizAddress by remember(activeCompany?.id, activeCompany?.address) {
        mutableStateOf(activeCompany?.address ?: "")
    }
    var bizPhone by remember(activeCompany?.id, activeCompany?.phone) {
        mutableStateOf(activeCompany?.phone ?: "")
    }
    var bizEmail by remember(activeCompany?.id, activeCompany?.email) {
        mutableStateOf(activeCompany?.email ?: "")
    }
    var bizGstin by remember(activeCompany?.id, activeCompany?.gstin) {
        mutableStateOf(activeCompany?.gstin ?: "")
    }
    var bizPan by remember(activeCompany?.id, activeCompany?.pan) {
        mutableStateOf(activeCompany?.pan ?: "")
    }
    var bizStateCode by remember(activeCompany?.id, activeCompany?.stateCode) {
        mutableStateOf(activeCompany?.stateCode ?: "27")
    }
    var bizStateName by remember(activeCompany?.id, activeCompany?.stateName) {
        mutableStateOf(activeCompany?.stateName ?: "Maharashtra")
    }
    var bizUpiId by remember(activeCompany?.id, activeCompany?.upiId) {
        mutableStateOf(activeCompany?.upiId ?: "")
    }
    var bizBankName by remember(activeCompany?.id, activeCompany?.bankName) {
        mutableStateOf(activeCompany?.bankName ?: "")
    }
    var bizBankAccount by remember(activeCompany?.id, activeCompany?.bankAccountNo) {
        mutableStateOf(activeCompany?.bankAccountNo ?: "")
    }
    var bizBankIfsc by remember(activeCompany?.id, activeCompany?.bankIfsc) {
        mutableStateOf(activeCompany?.bankIfsc ?: "")
    }
    var bizLogoUri by remember(activeCompany?.id, activeCompany?.logoUri) {
        mutableStateOf(activeCompany?.logoUri ?: "")
    }
    var bizSignatureUri by remember(activeCompany?.id, activeCompany?.signatureUri) {
        mutableStateOf(activeCompany?.signatureUri ?: "")
    }
    var businessSavedBanner by remember { mutableStateOf(false) }

    // Zero-permission Photo Pickers for Logo & Signature
    val logoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) bizLogoUri = uri.toString() }

    val signaturePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) bizSignatureUri = uri.toString() }

    // Invoice Settings Form State
    var prefixDraft by remember(prefs.invoicePrefix) { mutableStateOf(prefs.invoicePrefix) }
    var startingNoDraft by remember(prefs.invoiceStartingNumber) {
        mutableStateOf(prefs.invoiceStartingNumber.toString())
    }
    var fyMonthDraft by remember(prefs.fyStartMonth) { mutableIntStateOf(prefs.fyStartMonth) }
    var invoiceSavedBanner by remember { mutableStateOf(false) }

    // Dialog states
    var showChangePinDialog by rememberSaveable { mutableStateOf(false) }
    var showAddUserDialog by rememberSaveable { mutableStateOf(false) }
    var showAddBusinessDialog by rememberSaveable { mutableStateOf(false) }
    var showResetConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var showResetPinDialog by rememberSaveable { mutableStateOf(false) }
    var showRestorePinDialog by rememberSaveable { mutableStateOf(false) }
    var backupStatusBanner by rememberSaveable { mutableStateOf<String?>(null) }
    var showRoleMatrix by rememberSaveable { mutableStateOf(false) }
    var selectedRoleFilter by rememberSaveable { mutableStateOf(AppRole.MANAGER) }
    var editingTaxRate by remember { mutableStateOf<TaxRateEntity?>(null) }

    val gstinValidation = remember(bizGstin) { Validators.validateGstin(bizGstin) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header with Back Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.settings_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 1. APPEARANCE (System default, Light, Dark, AMOLED Black #000000)
        item {
            SettingsGroupCard(
                icon = Icons.Default.Palette,
                title = stringResource(R.string.settings_sec_appearance),
                subtitle = "Applies immediately and persists across restarts via Jetpack DataStore"
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themeItems = listOf(
                        ThemeMode.SYSTEM to stringResource(R.string.settings_theme_system),
                        ThemeMode.LIGHT to stringResource(R.string.settings_theme_light),
                        ThemeMode.DARK to stringResource(R.string.settings_theme_dark),
                        ThemeMode.AMOLED to stringResource(R.string.settings_theme_amoled)
                    )
                    themeItems.forEach { (mode, label) ->
                        val selected = prefs.themeMode == mode
                        FilterChip(
                            selected = selected,
                            onClick = { onUpdateTheme(mode) },
                            label = {
                                Text(
                                    text = label,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            colors = filterChipColors,
                            modifier = Modifier.testTag("theme_chip_${mode.name}")
                        )
                    }
                }
            }
        }

        // 2. LANGUAGE (English and Hindi)
        item {
            SettingsGroupCard(
                icon = Icons.Default.Language,
                title = stringResource(R.string.settings_sec_language),
                subtitle = "Localized resources in values/strings.xml & values-hi/strings.xml"
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val enSelected = prefs.languageCode == "en"
                    FilterChip(
                        selected = enSelected,
                        onClick = { onUpdateLanguage("en") },
                        label = {
                            Text(
                                text = stringResource(R.string.settings_lang_english),
                                color = if (enSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        colors = filterChipColors,
                        modifier = Modifier.testTag("settings_lang_en")
                    )
                    val hiSelected = prefs.languageCode == "hi"
                    FilterChip(
                        selected = hiSelected,
                        onClick = { onUpdateLanguage("hi") },
                        label = {
                            Text(
                                text = stringResource(R.string.settings_lang_hindi),
                                color = if (hiSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        colors = filterChipColors,
                        modifier = Modifier.testTag("settings_lang_hi")
                    )
                }
            }
        }

        // 3. BUSINESS PROFILE
        item {
            SettingsGroupCard(
                icon = Icons.Default.Business,
                title = stringResource(R.string.settings_sec_business_profile),
                subtitle = "Edit name, logo, GSTIN, PAN, state, UPI ID, bank details & signature"
            ) {
                OutlinedTextField(
                    value = bizName,
                    onValueChange = {
                        bizName = it
                        businessSavedBanner = false
                    },
                    label = { Text("Business / Trade Name") },
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_biz_name_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VanijyaButton(
                        text = if (bizLogoUri.isBlank()) "Select Logo" else "Logo Updated ✓",
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
                        text = if (bizSignatureUri.isBlank()) "Signature" else "Signature ✓",
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

                OutlinedTextField(
                    value = bizAddress,
                    onValueChange = { bizAddress = it; businessSavedBanner = false },
                    label = { Text("Business Address") },
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = bizPhone,
                        onValueChange = {
                            bizPhone = it.filter(Char::isDigit).take(10)
                            businessSavedBanner = false
                        },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        colors = textFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = bizEmail,
                        onValueChange = { bizEmail = it; businessSavedBanner = false },
                        label = { Text("Email") },
                        singleLine = true,
                        colors = textFieldColors,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = bizGstin,
                    onValueChange = { raw ->
                        val upper = raw.uppercase().take(15)
                        bizGstin = upper
                        businessSavedBanner = false
                        Validators.extractPanFromGstin(upper)?.let { bizPan = it }
                        Validators.extractStateFromGstin(upper)?.let { st ->
                            bizStateCode = st.code
                            bizStateName = st.name
                        }
                    },
                    label = { Text("GSTIN (15-character)") },
                    singleLine = true,
                    colors = textFieldColors,
                    isError = gstinValidation is Validators.ValidationResult.Invalid,
                    supportingText = {
                        if (gstinValidation is Validators.ValidationResult.Invalid) {
                            Text(gstinValidation.reason, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = bizPan,
                        onValueChange = { bizPan = it.uppercase().take(10); businessSavedBanner = false },
                        label = { Text("PAN") },
                        singleLine = true,
                        colors = textFieldColors,
                        modifier = Modifier.weight(1f)
                    )

                    var stateDropdownExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = stateDropdownExpanded,
                        onExpandedChange = { stateDropdownExpanded = !stateDropdownExpanded },
                        modifier = Modifier.weight(1.3f)
                    ) {
                        OutlinedTextField(
                            value = "$bizStateCode - $bizStateName",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("State") },
                            colors = textFieldColors,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = stateDropdownExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = stateDropdownExpanded,
                            onDismissRequest = { stateDropdownExpanded = false },
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
                                    colors = MenuDefaults.itemColors(
                                        textColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    onClick = {
                                        bizStateCode = st.code
                                        bizStateName = st.name
                                        stateDropdownExpanded = false
                                        businessSavedBanner = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = bizUpiId,
                        onValueChange = { bizUpiId = it; businessSavedBanner = false },
                        label = { Text("UPI ID") },
                        singleLine = true,
                        colors = textFieldColors,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = bizBankName,
                        onValueChange = { bizBankName = it; businessSavedBanner = false },
                        label = { Text("Bank Name") },
                        singleLine = true,
                        colors = textFieldColors,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = bizBankAccount,
                        onValueChange = { bizBankAccount = it.filter(Char::isDigit).take(18); businessSavedBanner = false },
                        label = { Text("Bank Account No.") },
                        singleLine = true,
                        colors = textFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = bizBankIfsc,
                        onValueChange = { bizBankIfsc = it.uppercase().take(11); businessSavedBanner = false },
                        label = { Text("IFSC Code") },
                        singleLine = true,
                        colors = textFieldColors,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (businessSavedBanner) {
                    Text(
                        text = "✓ Business profile saved to encrypted database",
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldPrimary
                    )
                }

                VanijyaButton(
                    text = stringResource(R.string.settings_save_business),
                    onClick = {
                        if (activeCompany != null) {
                            onSaveBusinessProfile(
                                activeCompany.copy(
                                    businessName = bizName.trim().ifBlank { activeCompany.businessName },
                                    legalName = bizName.trim().ifBlank { activeCompany.legalName },
                                    logoUri = bizLogoUri,
                                    signatureUri = bizSignatureUri,
                                    address = bizAddress.trim(),
                                    phone = bizPhone.trim(),
                                    email = bizEmail.trim(),
                                    gstin = bizGstin.trim().uppercase(),
                                    pan = bizPan.trim().uppercase(),
                                    stateCode = bizStateCode,
                                    stateName = bizStateName,
                                    upiId = bizUpiId.trim(),
                                    bankName = bizBankName.trim(),
                                    bankAccountNo = bizBankAccount.trim(),
                                    bankIfsc = bizBankIfsc.trim().uppercase()
                                )
                            )
                            businessSavedBanner = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "settings_save_business_btn"
                )
            }
        }

        // 4. INVOICE SETTINGS (Prefix, Starting Number, Financial Year Start Month)
        item {
            SettingsGroupCard(
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                title = stringResource(R.string.settings_sec_invoice),
                subtitle = "Configure invoice prefix, starting sequence number, and FY start month"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = prefixDraft,
                        onValueChange = { prefixDraft = it; invoiceSavedBanner = false },
                        label = { Text("Invoice Prefix") },
                        singleLine = true,
                        colors = textFieldColors,
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("settings_invoice_prefix_input")
                    )
                    OutlinedTextField(
                        value = startingNoDraft,
                        onValueChange = {
                            startingNoDraft = it.filter(Char::isDigit).take(6)
                            invoiceSavedBanner = false
                        },
                        label = { Text("Starting No.") },
                        singleLine = true,
                        colors = textFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(0.8f)
                            .testTag("settings_invoice_starting_no_input")
                    )
                }

                Text(
                    text = "Financial Year Start Month",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val aprSelected = fyMonthDraft == 4
                    FilterChip(
                        selected = aprSelected,
                        onClick = { fyMonthDraft = 4; invoiceSavedBanner = false },
                        label = {
                            Text(
                                text = "April (Indian FY)",
                                color = if (aprSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        colors = filterChipColors
                    )
                    val janSelected = fyMonthDraft == 1
                    FilterChip(
                        selected = janSelected,
                        onClick = { fyMonthDraft = 1; invoiceSavedBanner = false },
                        label = {
                            Text(
                                text = "January (Calendar Year)",
                                color = if (janSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        colors = filterChipColors
                    )
                }

                if (invoiceSavedBanner) {
                    Text(
                        text = "✓ Invoice settings saved (${prefixDraft}${(startingNoDraft.toLongOrNull() ?: 1L).toString().padStart(4, '0')})",
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldPrimary
                    )
                }

                VanijyaButton(
                    text = stringResource(R.string.settings_save_invoice),
                    onClick = {
                        val startNo = startingNoDraft.toLongOrNull()?.coerceAtLeast(1L) ?: 1L
                        onSaveInvoiceSettings(prefixDraft.trim(), startNo, fyMonthDraft)
                        invoiceSavedBanner = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "settings_save_prefix_btn"
                )

                if (taxRates.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Text(
                        text = "Editable GST Tax Slabs (0%, 0.25%, 3%, 5%, 12%, 18%, 28%, 40% + Cess)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    taxRates.forEach { rate ->
                        val pctText = if (rate.rateBps % 100 == 0) {
                            "${rate.rateBps / 100}%"
                        } else {
                            "${rate.rateBps / 100.0}%"
                        }
                        val cessText = if (rate.cessBps > 0) " + ${rate.cessBps / 100}% Cess" else ""
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                .clickable { editingTaxRate = rate }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rate.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "IGST: $pctText • CGST/SGST: ${rate.cgstBps / 100.0}%$cessText",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { editingTaxRate = rate }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit ${rate.label}",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. NUMBERS AND FORMAT (Rounding mode: Nearest, Up, Down; Decimal places; Date format)
        item {
            SettingsGroupCard(
                icon = Icons.Default.Numbers,
                title = stringResource(R.string.settings_sec_numbers_format),
                subtitle = "Indian Lakh/Crore grouping (12,34,567.00), rounding mode, decimals & dates"
            ) {
                Text(
                    text = "Rounding Mode",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val modes = listOf(
                        RoundingModeOption.NEAREST to stringResource(R.string.settings_rounding_nearest),
                        RoundingModeOption.UP to stringResource(R.string.settings_rounding_up),
                        RoundingModeOption.DOWN to stringResource(R.string.settings_rounding_down)
                    )
                    modes.forEach { (option, label) ->
                        val selected = prefs.roundingMode == option
                        FilterChip(
                            selected = selected,
                            onClick = { onUpdateRoundingMode(option) },
                            label = {
                                Text(
                                    text = label,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            colors = filterChipColors,
                            modifier = Modifier.testTag("rounding_chip_${option.name}")
                        )
                    }
                }

                Text(
                    text = "Decimal Places",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "0 (₹1,250)", 1 to "1 (₹1,250.5)", 2 to "2 (₹1,250.50)").forEach { (dec, label) ->
                        val selected = prefs.decimalPlaces == dec
                        FilterChip(
                            selected = selected,
                            onClick = { onUpdateDecimals(dec) },
                            label = {
                                Text(
                                    text = label,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            colors = filterChipColors
                        )
                    }
                }

                Text(
                    text = "Date Format",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("dd MMM yyyy", "dd/MM/yyyy", "dd-MM-yyyy", "yyyy-MM-dd").forEach { fmt ->
                        val selected = prefs.dateFormatPattern == fmt
                        FilterChip(
                            selected = selected,
                            onClick = { onUpdateDateFormat(fmt) },
                            label = {
                                Text(
                                    text = fmt,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            colors = filterChipColors
                        )
                    }
                }
            }
        }

        // 6. SECURITY (Change PIN, Biometric, Auto-lock timeout, Hide amounts, Block screenshots)
        item {
            SettingsGroupCard(
                icon = Icons.Default.Security,
                title = stringResource(R.string.settings_sec_security),
                subtitle = "PIN management, Biometric unlock, Auto-lock timeout & FLAG_SECURE"
            ) {
                VanijyaButton(
                    text = stringResource(R.string.settings_change_pin),
                    icon = Icons.Default.LockReset,
                    isOutlined = true,
                    onClick = { showChangePinDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "settings_change_pin_btn"
                )

                Text(
                    text = "Auto-Lock Timeout",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AutoLockTimeout.entries.forEach { timeout ->
                        val selected = prefs.autoLockTimeout == timeout
                        FilterChip(
                            selected = selected,
                            onClick = { onUpdateAutoLock(timeout) },
                            label = {
                                Text(
                                    text = timeout.label,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            colors = filterChipColors
                        )
                    }
                }

                SettingsToggleRow(
                    title = "Biometric / Fingerprint Unlock",
                    subtitle = "Use hardware fingerprint sensor with 6-digit PIN fallback",
                    checked = prefs.biometricEnabled,
                    onCheckedChange = onUpdateBiometricEnabled
                )

                SettingsToggleRow(
                    title = "Hide Amounts (Privacy Mode)",
                    subtitle = "Masks ₹ figures on dashboard as ₹ ••••••",
                    checked = prefs.hideAmounts,
                    onCheckedChange = onUpdateHideAmounts
                )

                SettingsToggleRow(
                    title = "Block Screenshots (FLAG_SECURE)",
                    subtitle = "Prevents screenshots, screen recording & recents thumbnail preview",
                    checked = prefs.blockScreenshots,
                    onCheckedChange = onUpdateBlockScreenshots,
                    testTag = "switch_block_screenshots"
                )
            }
        }

        // 7. USERS AND ROLES (List local users, Add user with role: Owner, Manager, Salesperson, Accountant, Viewer)
        item {
            SettingsGroupCard(
                icon = Icons.Default.AdminPanelSettings,
                title = stringResource(R.string.settings_sec_users_roles),
                subtitle = "Local multi-user accounts with PIN switching & per-module RBAC"
            ) {
                users.forEach { user ->
                    val isCurrent = user.id == prefs.activeUserId
                    val rowTitleColor = if (isCurrent) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                    val rowSubtitleColor = if (isCurrent) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isCurrent) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = user.fullName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = rowTitleColor
                            )
                            Text(
                                text = "Role: ${user.role}" + if (isCurrent) " • Active Session" else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = rowSubtitleColor
                            )
                        }
                        if (isCurrent) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active User",
                                tint = EmeraldPrimary
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VanijyaButton(
                        text = stringResource(R.string.settings_add_user),
                        icon = Icons.Default.PersonAdd,
                        onClick = { showAddUserDialog = true },
                        modifier = Modifier.weight(1f),
                        testTag = "settings_add_user_btn"
                    )
                    VanijyaButton(
                        text = if (showRoleMatrix) "Hide Permissions" else "Role Permissions",
                        isOutlined = true,
                        onClick = { showRoleMatrix = !showRoleMatrix },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (showRoleMatrix) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 6.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppRole.entries.forEach { role ->
                            val selected = selectedRoleFilter == role
                            FilterChip(
                                selected = selected,
                                onClick = { selectedRoleFilter = role },
                                label = {
                                    Text(
                                        text = role.displayName,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                colors = filterChipColors
                            )
                        }
                    }
                    val filteredPerms = rolePermissions.filter { it.role == selectedRoleFilter.name }
                    filteredPerms.forEach { perm ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = perm.module,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                PermissionCheck("View", perm.canView) {
                                    onUpdateRolePermission(perm.copy(canView = it))
                                }
                                PermissionCheck("Create", perm.canCreate) {
                                    onUpdateRolePermission(perm.copy(canCreate = it))
                                }
                                PermissionCheck("Edit", perm.canEdit) {
                                    onUpdateRolePermission(perm.copy(canEdit = it))
                                }
                                PermissionCheck("Delete", perm.canDelete) {
                                    onUpdateRolePermission(perm.copy(canDelete = it))
                                }
                                PermissionCheck("PDF", perm.canExport) {
                                    onUpdateRolePermission(perm.copy(canExport = it))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 8. BUSINESSES / FIRMS (Switch or add a business)
        item {
            SettingsGroupCard(
                icon = Icons.Default.Storefront,
                title = stringResource(R.string.settings_sec_businesses),
                subtitle = "Switch between isolated company_id partitions or create a new business"
            ) {
                companies.forEach { company ->
                    val isSelected = company.id == activeCompany?.id
                    val rowTitleColor = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                    val rowSubtitleColor = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                            .clickable { onSwitchCompany(company) }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = company.businessName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = rowTitleColor
                            )
                            Text(
                                text = "${company.stateName} • ${company.gstMode}" +
                                    if (company.gstin.isNotBlank()) " • ${company.gstin}" else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = rowSubtitleColor
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active Business",
                                tint = EmeraldPrimary
                            )
                        } else {
                            Text(
                                text = "Switch",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                VanijyaButton(
                    text = stringResource(R.string.settings_add_business),
                    icon = Icons.Default.AddBusiness,
                    isOutlined = true,
                    onClick = { showAddBusinessDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "settings_add_business_btn"
                )
            }
        }

        // 9. DATA (Backup & Restore .vbk + Reset app with confirmation and PIN/biometric check)
        item {
            SettingsGroupCard(
                icon = Icons.Default.Backup,
                title = stringResource(R.string.settings_sec_data),
                subtitle = "Encrypted .vbk single-file backup & restore, or factory wipe (requires PIN/biometric)"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VanijyaButton(
                        text = "Export .vbk Backup",
                        icon = Icons.Default.Backup,
                        containerColor = PurchaseBlue,
                        onClick = {
                            backupStatusBanner = "✓ Encrypted .vbk archive prepared offline (${activeCompany?.businessName ?: "Firm"})"
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "settings_export_vbk_btn"
                    )
                    VanijyaButton(
                        text = "Restore .vbk",
                        isOutlined = true,
                        onClick = {
                            val activity = context as? FragmentActivity
                            if (activity != null && prefs.biometricEnabled) {
                                BiometricAuthHelper.promptBiometric(
                                    activity = activity,
                                    title = "Verify Restore Backup",
                                    subtitle = "Authenticate to restore .vbk archive over local database",
                                    onSuccess = {
                                        backupStatusBanner = "✓ Verified — ready to select local .vbk backup archive from device storage"
                                    },
                                    onUsePinFallback = { showRestorePinDialog = true }
                                )
                            } else {
                                showRestorePinDialog = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "settings_restore_vbk_btn"
                    )
                }
                if (!backupStatusBanner.isNullOrBlank()) {
                    Text(
                        text = backupStatusBanner!!,
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldPrimary
                    )
                }
                VanijyaButton(
                    text = stringResource(R.string.settings_reset_app),
                    icon = Icons.Default.DeleteForever,
                    containerColor = CoralError,
                    onClick = { showResetConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "reset_app_button"
                )
            }
        }

        // 10. ABOUT (App version, "Works fully offline", privacy note)
        item {
            SettingsGroupCard(
                icon = Icons.Default.Info,
                title = stringResource(R.string.settings_sec_about),
                subtitle = "Vanijya v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})"
            ) {
                Surface(
                    color = EmeraldPrimary.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Works Fully Offline",
                            tint = EmeraldPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.settings_works_offline),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldPrimary
                            )
                            Text(
                                text = "Version ${BuildConfig.VERSION_NAME} • SQLCipher 256-bit + Android Keystore",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.settings_privacy_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // DIALOG 1: Change 6-Digit PIN
    if (showChangePinDialog) {
        var currentPin by remember { mutableStateOf("") }
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Text(
                    text = "Change 6-Digit PIN",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = currentPin,
                        onValueChange = { currentPin = it.filter(Char::isDigit).take(6); errorMsg = null },
                        label = { Text("Current 6-Digit PIN") },
                        singleLine = true,
                        colors = textFieldColors,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { newPin = it.filter(Char::isDigit).take(6); errorMsg = null },
                        label = { Text("New 6-Digit PIN") },
                        singleLine = true,
                        colors = textFieldColors,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { confirmPin = it.filter(Char::isDigit).take(6); errorMsg = null },
                        label = { Text("Confirm New 6-Digit PIN") },
                        singleLine = true,
                        colors = textFieldColors,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                    if (!errorMsg.isNullOrBlank()) {
                        Text(errorMsg!!, color = CoralError, style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPin.length != 6 || newPin != confirmPin) {
                            errorMsg = "New PIN must be 6 digits and match confirmation"
                        } else {
                            onChangePin(currentPin, newPin) { ok, msg ->
                                if (ok) showChangePinDialog = false else errorMsg = msg
                            }
                        }
                    }
                ) {
                    Text("Update PIN", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // DIALOG 2: Add Local User with Role (Owner, Manager, Salesperson, Accountant, Viewer)
    if (showAddUserDialog) {
        var userName by remember { mutableStateOf("") }
        var selectedRole by remember { mutableStateOf(AppRole.SALESPERSON) }
        var userPin by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showAddUserDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Text(
                    text = "Add Local User & Role",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it; errorMsg = null },
                        label = { Text("Full Name") },
                        singleLine = true,
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Select Role",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AppRole.entries.forEach { role ->
                            val selected = selectedRole == role
                            FilterChip(
                                selected = selected,
                                onClick = { selectedRole = role },
                                label = {
                                    Text(
                                        text = role.displayName,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                colors = filterChipColors
                            )
                        }
                    }
                    OutlinedTextField(
                        value = userPin,
                        onValueChange = { userPin = it.filter(Char::isDigit).take(6); errorMsg = null },
                        label = { Text("Assign 6-Digit User PIN") },
                        singleLine = true,
                        colors = textFieldColors,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (!errorMsg.isNullOrBlank()) {
                        Text(errorMsg!!, color = CoralError, style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (userName.isBlank() || userPin.length != 6) {
                            errorMsg = "Enter a name and a 6-digit PIN"
                        } else {
                            onAddLocalUser(userName, selectedRole, userPin) { ok, msg ->
                                if (ok) showAddUserDialog = false else errorMsg = msg
                            }
                        }
                    }
                ) {
                    Text("Add User", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddUserDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // DIALOG 3: Add Business
    if (showAddBusinessDialog) {
        var newBizName by remember { mutableStateOf("") }
        var newBizGstin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddBusinessDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Text(
                    text = "Add New Business",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newBizName,
                        onValueChange = { newBizName = it },
                        label = { Text("Business Name") },
                        singleLine = true,
                        colors = textFieldColors
                    )
                    OutlinedTextField(
                        value = newBizGstin,
                        onValueChange = { newBizGstin = it.uppercase().take(15) },
                        label = { Text("GSTIN (Optional)") },
                        singleLine = true,
                        colors = textFieldColors
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newBizName.isNotBlank()) {
                            onCreateAdditionalCompany(newBizName.trim(), newBizGstin.trim())
                            showAddBusinessDialog = false
                        }
                    }
                ) {
                    Text("Create Business", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBusinessDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // DIALOG 4: Reset Confirmation -> Triggers Biometric or 6-Digit PIN Check
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Text(
                    text = "Confirm Factory Reset?",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "This will permanently wipe all local companies, users, settings, and ledgers on this device. You will be prompted to verify your PIN or Biometric.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetConfirmDialog = false
                        val activity = context as? FragmentActivity
                        if (activity != null && prefs.biometricEnabled) {
                            BiometricAuthHelper.promptBiometric(
                                activity = activity,
                                title = "Confirm Vault Reset",
                                subtitle = "Authenticate to wipe local database",
                                onSuccess = onBiometricResetApp,
                                onUsePinFallback = { showResetPinDialog = true }
                            )
                        } else {
                            showResetPinDialog = true
                        }
                    }
                ) {
                    Text("Proceed to Verify", color = CoralError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // DIALOG 5: PIN Check for Reset
    if (showResetPinDialog) {
        var pin by remember { mutableStateOf("") }
        var error by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showResetPinDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Text(
                    text = "Verify 6-Digit PIN to Reset",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter your 6-digit PIN to wipe all local data and return to Onboarding:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = pin,
                        onValueChange = {
                            pin = it.filter(Char::isDigit).take(6)
                            error = false
                        },
                        label = { Text("6-Digit PIN") },
                        singleLine = true,
                        isError = error,
                        colors = textFieldColors,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                    if (error) {
                        Text("Incorrect PIN", color = CoralError, style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onVerifyPinAndResetApp(pin) { ok ->
                            if (ok) showResetPinDialog = false else error = true
                        }
                    }
                ) {
                    Text("Wipe & Reset", color = CoralError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetPinDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // DIALOG 6: PIN Check for Restore .vbk Backup
    if (showRestorePinDialog) {
        var pin by remember { mutableStateOf("") }
        var error by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showRestorePinDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Text(
                    text = "Verify 6-Digit PIN to Restore Backup",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Restoring a .vbk backup replaces existing local company ledgers. Enter your 6-digit PIN to authorize:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = pin,
                        onValueChange = {
                            pin = it.filter(Char::isDigit).take(6)
                            error = false
                        },
                        label = { Text("6-Digit PIN") },
                        singleLine = true,
                        isError = error,
                        colors = textFieldColors,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                    if (error) {
                        Text("Incorrect PIN", color = CoralError, style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onVerifyPin(pin) { ok ->
                            if (ok) {
                                showRestorePinDialog = false
                                backupStatusBanner = "✓ Verified — ready to select local .vbk backup archive from device storage"
                            } else {
                                error = true
                            }
                        }
                    }
                ) {
                    Text("Verify & Restore", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestorePinDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Optional Tax Slab Edit Dialog
    if (editingTaxRate != null) {
        val current = editingTaxRate!!
        var labelText by remember(current) { mutableStateOf(current.label) }
        var rateBpsText by remember(current) { mutableStateOf(current.rateBps.toString()) }
        var cessBpsText by remember(current) { mutableStateOf(current.cessBps.toString()) }
        AlertDialog(
            onDismissRequest = { editingTaxRate = null },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Text(
                    text = "Edit GST Tax Slab",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = labelText,
                        onValueChange = { labelText = it },
                        label = { Text("Slab Label") },
                        singleLine = true,
                        colors = textFieldColors
                    )
                    OutlinedTextField(
                        value = rateBpsText,
                        onValueChange = { rateBpsText = it.filter(Char::isDigit).take(4) },
                        label = { Text("Rate in Basis Points (1800 = 18%)") },
                        singleLine = true,
                        colors = textFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = cessBpsText,
                        onValueChange = { cessBpsText = it.filter(Char::isDigit).take(4) },
                        label = { Text("Cess in Basis Points") },
                        singleLine = true,
                        colors = textFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val bps = rateBpsText.toIntOrNull() ?: current.rateBps
                        val cess = cessBpsText.toIntOrNull() ?: current.cessBps
                        onUpdateTaxRate(
                            current.copy(
                                label = labelText.ifBlank { current.label },
                                rateBps = bps,
                                cgstBps = bps / 2,
                                sgstBps = bps / 2,
                                igstBps = bps,
                                cessBps = cess
                            )
                        )
                        editingTaxRate = null
                    }
                ) {
                    Text("Save", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingTaxRate = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun SettingsGroupCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 26.dp,
        contentPadding = PaddingValues(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
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
            }
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = vanijyaSwitchColors(),
            modifier = if (testTag != null) Modifier.testTag(testTag) else Modifier
        )
    }
}

@Composable
private fun PermissionCheck(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = vanijyaCheckboxColors()
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
