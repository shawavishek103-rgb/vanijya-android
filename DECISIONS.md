# Architectural Decisions & Real-Device Testing Notes (`DECISIONS.md`)

## 1. Key Architectural Decisions

1. **Zero Network Permissions Enforcement**:
   - All network-capable template dependencies (Firebase, Retrofit, OkHttp, Play Services) were removed from active compilation in `app/build.gradle.kts`.
   - `AndroidManifest.xml` explicitly includes `tools:node="remove"` on `android.permission.INTERNET` and `android.permission.ACCESS_NETWORK_STATE`.
   - A custom Gradle task `verifyNoInternetPermission` is wired into `process*MainManifest` and `check` to fail the build immediately if `android.permission.INTERNET` ever enters the merged manifest.

2. **Money as Integer Paise (`@JvmInline value class Money(val paise: Long)`)**:
   - Floating-point (`Double`/`Float`) is strictly banned for currency.
   - All monetary columns in Database v1 and v2 (`opening_balance_paise`, `current_balance_paise`, `credit_limit_paise`, `debit_paise`, `credit_paise`, `running_balance_paise`, `sale_price_paise`, `purchase_price_paise`, `wholesale_price_paise`, `mrp_paise`, `rate_paise`) store integer paise (`Long`).

3. **SQLCipher + Android Keystore Envelope Encryption**:
   - A 256-bit random passphrase is generated via `SecureRandom` and encrypted with an `AES/GCM/NoPadding` 256-bit hardware-backed key in `AndroidKeyStore` (`vanijya_sqlcipher_master_key_v1`).
   - Room is initialized with `net.zetetic.database.sqlcipher.SupportOpenHelperFactory(passphraseBytes)`.

4. **Database v2 Non-Destructive Migration (`MIGRATION_1_2`) & Atomic `@Transaction` Writes**:
   - `VanijyaDatabase` v2 adds 19 master and ledger tables (`party`, `party_address`, `party_group`, `party_ledger_entry`, `party_custom_field`, `item`, `item_category`, `item_unit`, `unit_conversion`, `item_batch`, `item_serial`, `item_variant`, `warehouse`, `stock_ledger`, `stock_adjustment`, `stock_transfer`, `item_bundle`, `price_list`, `price_list_item`) via `MIGRATION_1_2` without dropping or renaming any existing v1 table.
   - Multi-table operations (saving a party with addresses/custom fields/opening ledger, recording a party voucher and recalculating running balances, saving an item with batches/serials/variants/bundles/opening stock ledger, recording stock adjustments, and transferring stock across godowns) execute atomically inside single `@Transaction` DAO methods.

5. **Offline Barcode Generation & Bundled HSN/SAC Asset Directory**:
   - `assets/hsn_sac_directory.csv` bundles 64 common Indian HSN and SAC codes across all GST slabs (`0%` to `40%`) for instant offline search and auto-fill.
   - `BarcodeAndImageHelper` generates crisp Code-128 1D barcode bitmaps purely in Kotlin (`android.graphics.Bitmap`) with zero external network or cloud dependencies.

6. **Database v3 Non-Destructive Migration (`MIGRATION_2_3`), Pure-Kotlin GST Engine & Atomic Sales/Payment Lifecycle**:
   - `VanijyaDatabase` v3 adds 9 sales, tax, conversion, payment, cheque, and recurring tables (`txn`, `txn_line`, `txn_charge`, `txn_tax_line`, `txn_link`, `payment`, `payment_allocation`, `cheque`, `recurring_template`) via `MIGRATION_2_3`.
   - `GstCalculationEngine` computes all GST math in integer paise (`Long`) and basis points (`1800` = `18.00%`), supporting intra-state (`CGST + SGST`), inter-state/SEZ/export (`IGST`), cess, composition (`Bill of Supply`), reverse charge (`RCM`), tax-inclusive back-calculation, proportional invoice discount distribution, round-off modes, TCS/TDS, and HSN summary aggregation.
   - Creating, editing, or cancelling a sales document (`saveOrUpdateSalesDocumentInTransaction`, `cancelDocumentInTransaction`), settling invoices (`recordPaymentWithAllocationsInTransaction`), and bouncing a cheque (`updateChequeStatusInTransaction`) each run inside a single Room `@Transaction`, reversing prior `stock_ledger` and `party_ledger_entry` effects before applying new movements and recording an immutable `audit_log` row.

7. **Modular Billing UI Architecture (`BillingComponents.kt`) & Shared `BillSummaryCard`**:
   - Extracted 9 dedicated, theme-aware billing composables (`SectionCard`, `DocTypeChips`, `DocNumberChip`, `PartyCard`, `ItemLineCard`, `ItemEditSheet`, `BillSummaryCard`, `PaymentCard`, `StickyTotalBar`) into `com.example.feature.sales.BillingComponents.kt`.
   - `SectionCard` uses an ~88% opaque `surface` / `surfaceContainerHigh` fill with a `24.dp` corner radius and `1.dp` `outlineVariant` border so dense billing fields remain high-contrast and legible across Light, Dark, and AMOLED themes at up to `1.3x` font scale.
   - `BillSummaryCard` is shared across the full-screen Billing Builder, the Invoice Detail screen, and the PDF/Share Preview sheet so subtotal, discount, taxable value, slab-grouped CGST/SGST/IGST breakup, round-off, grand total, and Indian amount-in-words render identically everywhere.

## 2. Items Requiring Real-Device Verification

Because the browser streaming emulator may not have physical biometric sensors, rear camera optics, populated phone contacts, or WhatsApp installed:

- **CameraX Live Barcode / SKU Scanner (`ItemsFoundationScreen`)**:
  - *Implementation*: Uses `androidx.camera.view.PreviewView` + `ProcessCameraProvider` + `ImageAnalysis` with runtime `Manifest.permission.CAMERA` request and an instant manual barcode entry / generated SKU fallback on emulators without a camera feed.
  - *Real-Device Test*: Grant Camera permission on a physical Android phone, open `Items and Stock > Scan`, point the rear camera at a product barcode, and verify live camera preview and barcode capture.
- **Phone Contacts Import (`PartiesFoundationScreen`)**:
  - *Implementation*: Requests optional `Manifest.permission.READ_CONTACTS` at runtime and launches `ActivityResultContracts.PickContact()`, querying `ContactsContract.CommonDataKinds.Phone` to populate party name and 10-digit mobile number.
  - *Real-Device Test*: Tap `Contact` on the `Parties` screen on a phone with saved contacts, pick a contact, and confirm the party form auto-fills name and phone number.
- **Android Share Sheet for PDF Statements, CSV/Excel Templates & WhatsApp/SMS Payment Reminders**:
  - *Implementation*: Uses `android.graphics.pdf.PdfDocument` and `androidx.core.content.FileProvider` (`${applicationId}.fileprovider`) with `Intent.ACTION_SEND` + `FLAG_GRANT_READ_URI_PERMISSION`.
  - *Real-Device Test*: Generate a Party Statement PDF, Stock Summary PDF, or Payment Reminder on a physical phone and share it directly to WhatsApp, Gmail, or local file manager.
- **Fingerprint / Biometric Hardware Sensor (`BiometricPrompt`)**:
  - *Implementation*: Uses real `androidx.biometric.BiometricPrompt` with `BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL` and automatic 6-digit PIN fallback.
  - *Real-Device Test*: Enroll a fingerprint/face on a physical Android phone and verify hardware biometric unlock on cold start and after auto-lock timeout.
- **System Photo Picker (`PickVisualMedia`) & Local JPEG Compression**:
  - *Implementation*: Uses zero-permission `ActivityResultContracts.PickVisualMedia()` and compresses item photos to `<= 720px` 80% JPEG inside `context.filesDir/item_images/`.
  - *Real-Device Test*: Attach a product photo from the device gallery in the Add/Edit Item sheet and verify compressed offline persistence.
