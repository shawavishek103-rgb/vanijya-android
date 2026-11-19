# Vanijya Feature Checklist (`FEATURES.md`)

## Step 1 — Foundation, Security, Design System, Core Database v1 & Complete Settings
- [x] **A) Project Setup**:
  - [x] Gradle Kotlin DSL + Version Catalog (`libs.versions.toml`)
  - [x] Dagger Hilt dependency injection + Room + SQLCipher 256-bit encryption (`net.zetetic:sqlcipher-android`)
  - [x] `minSdk = 26`, edge-to-edge (`enableEdgeToEdge()`), single activity (`MainActivity`), R8 enabled for release builds
  - [x] Automated Gradle CI check (`verifyNoInternetPermission`) failing build if `android.permission.INTERNET` appears in merged manifest
- [x] **B) Brand & Vector Logo**:
  - [x] Rounded-square adaptive app icon with indigo-to-violet gradient (`#2A1B6B` to `#5B3FD9`)
  - [x] Ascending bold "V" foreground (left bar emerald `#0FB57F`, right bar white `#FFFFFF`) + gold dot (`#F2B134`) above right bar
  - [x] Monochrome icon layer (`ic_launcher_monochrome.xml`)
  - [x] Android 12+ SplashScreen with animated vector icon (`avd_splash_logo.xml`)
  - [x] In-app vector wordmark `"Vanijya"` and tagline `"Your business, in your pocket. Fully offline."`
- [x] **C) Design System (Premium Glassmorphism & Material 3 Expressive)**:
  - [x] Dark theme: deep navy-to-indigo gradient background with soft blurred colour glows (`GlassBackgroundBox`)
  - [x] Light theme: warm ivory/cream gradient with soft mint and sky-blue glows (`GlassBackgroundBox`)
  - [x] AMOLED Black theme (`#000000` background, glass cards with thin specular borders)
  - [x] Bundled `Plus Jakarta Sans` font (`res/font/plus_jakarta_sans.ttf`) with tabular numerals (`tnum`)
  - [x] `Money` value class (paise `Long`), Indian digit grouping (`3,53,000.00`), count-up animation respecting reduce-motion, and Indian Lakh/Crore number-to-words converter
  - [x] Reusable glass components in `VanijyaComponents.kt`: `GlassBackgroundBox`, `GlassCard`, `GlassSheet` (all propagating `LocalContentColor provides MaterialTheme.colorScheme.onSurface`), `GlassTile`, `AmountText`, `StatTile` (with animated emerald up-trend line for money-in and coral for money-out), `RingChart` (Cash vs Bank share), `StatusChip` (`Paid`, `Partial`, `Unpaid`, `Overdue`), `SegmentedTabs`, `FloatingGlassNavBar`, `PartyCard`, `InvoiceRow`, `VanijyaEmptyState`, `SectionHeader`, `SkeletonBox`, `SalesSparklineChart`, plus theme-aware `vanijyaTextFieldColors()`, `vanijyaFilterChipColors()`, `vanijyaSwitchColors()`, and `vanijyaCheckboxColors()` ensuring `>= 4.5:1` contrast in Light, Dark, and AMOLED themes
- [x] **D) 5-Tab Floating Glass Navigation, Tablet Rail & Shared Top App Bar**:
  - [x] Floating pill-shaped glass bottom navigation bar with 5 tabs (`1. Dashboard`, `2. Billing`, `3. Parties`, `4. Staff`, `5. Stock`), filled/outlined icon states (box/inventory `Inventory2` icon for `Stock`), always-visible labels, animated selection indicator, and per-tab state/scroll preservation (`saveState` / `restoreState` with `launchSingleTop` and `popUpTo` start destination)
  - [x] `"Stock"` tab opens the `Items and Stock` screen (`Items`, `Categories`, `Low Stock`, `Expiring` segments with floating `"+ Add Item"` button)
  - [x] `"Reports"` stays reachable from 3 places: the `"Reports"` tile in the Dashboard All Features grid, the Profile Avatar menu, and the chart icon (`Assessment`) in the Dashboard top bar
  - [x] Root-cause navigation fix: tapping `Dashboard` from any screen (including 2+ screens deep in `Billing`, `Parties`, `Staff`, `Stock`, `Reports`, or `Settings`) always returns to the `Dashboard` root; tapping the active tab again resets sub-screens and scrolls that tab to the top; pressing `Back` on `Dashboard` root exits the app while `Back` elsewhere goes up one screen
  - [x] Full-screen editors (`invoice builder`, `item form`, `party form`, `payroll run`) hide the floating bottom bar and tablet rail while open and show them again when closed; all other screens include generous bottom padding so no content hides behind the floating bar
  - [x] Adaptive glass `NavigationRail` on tablets / expanded screens (`>= 600.dp`) with the same 5 items (`Dashboard`, `Billing`, `Parties`, `Staff`, `Stock`)
  - [x] Shared Top App Bar across every main tab with: screen title (plus brand mark and Reports chart icon on Dashboard), gear icon opening full-screen `Settings`, notification bell, and profile avatar
  - [x] Profile Avatar glass sheet with: profile and business name, `Switch Firm`, `Switch User`, `Reports`, `Settings`, `Backup and Restore`, `Lock app now`, `Help`, and `About`
  - [x] Clean glassmorphic `ComingSoonModuleScreen` with module name and Back button for unbuilt modules
- [x] **E) Reorganised Glassmorphic Dashboard**:
  - [x] 1. Top bar: `"Vanijya"` wordmark + screen title, Reports chart icon, notification bell (due payments, low stock, expiry, backup reminder), settings gear, profile avatar
  - [x] 2. `"Switch Firm"` dropdown chip (`A.K. Enterprises` / active company name) + Role chip + quick Lock icon
  - [x] 3. Hero glass card: Total Cash (large), Total Bank Balance (large, with expandable bank accounts), and animated circular `RingChart` showing Cash vs Bank share; tap to open `Cash and Bank`
  - [x] 4. Date range selector chip (`Today`, `Yesterday`, `This Week`, `This Month`, `This Quarter`, `This FY`, `Custom`) filtering every dashboard widget
  - [x] 5. Quick actions row: `Sale` (amber/orange), `Purchase` (blue), `Quotation` (grey/silver), plus `More` tile opening `"All actions"` glass bottom sheet (`Payment In`, `Payment Out`, `Expense`, `Sale Order`, `Delivery Challan`, `Sale Return`, `Purchase Order`, `Add Party`, `Add Item`, `Stock Adjustment`, `Scan Barcode`, `Mark Attendance`)
  - [x] 6. Summary glass tiles (swipeable horizontal row): `Sales`, `Purchases`, `Expenses`, `Profit` for selected range with change vs previous period
  - [x] 6B. Glass `"Stock"` summary tile (`Stock Value`, `Low Stock` count, `Expiring` count computed live from Room database) opening `Stock` (`Items and Stock`)
  - [x] 7. `"To Collect"` (emerald + up-trend line) and `"To Pay"` (red) glass cards with top 3 parties from Room database and `"Remind"` button
  - [x] 8. Alerts strip: `Low stock`, `Expiring items`, `Overdue invoices`, `Cheques due`, `Backup ready` (with real-time database counts)
  - [x] 9. Recent activity list: invoice glass cards with number, party, amount, and status chip (`Paid`, `Partial`, `Unpaid`, `Overdue`); tap to open, button to share PDF
  - [x] 10. `"All Features"` grid (glass icon tiles): `Reports`, `Items and Stock`, `Cash and Bank`, `Expenses`, `Staff and Payroll`, `GST`, `Day Book`, `Backup and Restore`, `Import/Export`, `Settings`
- [x] **F) Onboarding, Welcome & Security**:
  - [x] Light Glassmorphism First-Run Welcome & Onboarding flow (warm ivory-to-cream gradient `#FAF8F4` with soft mint `#6EE7B7` and sky-blue `#93C5FD` glows, frosted white glass cards `0xADFFFFFF`, dark navy text `#1B1B2F`, muted grey `#4A536E`, and indigo-violet `#5B3FD9` primary buttons) — always opens in Light mode on first run regardless of system dark mode, with a top-corner Light/Dark toggle on the Welcome screen and default `System` theme after setup finishes
  - [x] 4-Step Setup Wizard (`Step 1 of 4` Language, `Step 2 of 4` Business Profile & GSTIN, `Step 3 of 4` GST Mode, `Step 4 of 4` PIN & Biometrics) with step indicator, back button on every step, and inline validation, plus full Restore-from-Backup (`.vbk`) screen
  - [x] Glassmorphic Lock Screen (`BiometricPrompt` + 6-digit PIN keypad with role PIN switching, Hide Amounts, and `FLAG_SECURE`)
- [x] **G) Database v1 Core Tables**:
  - [x] `company`, `app_user`, `role_permission`, `setting`, `number_series`, `audit_log`, `attachment`, `financial_year`, `tax_rate` (0, 0.25, 3, 5, 12, 18, 28, 40 + cess), `hsn_master`

## Step 2 — Parties Master, Items/Stock Master & Database v2 (`MIGRATION_1_2`)
- [x] **A) Database v2 (`MIGRATION_1_2` + 19 New Room Tables)**:
  - [x] Non-destructive `MIGRATION_1_2` upgrading `VanijyaDatabase` from v1 to v2 without dropping or renaming any existing v1 tables
  - [x] All 19 Step 2 tables created with indices and integer paise (`Long`) money columns: `party`, `party_address`, `party_group`, `party_ledger_entry`, `party_custom_field`, `item`, `item_category`, `item_unit`, `unit_conversion`, `item_batch`, `item_serial`, `item_variant`, `warehouse`, `stock_ledger`, `stock_adjustment`, `stock_transfer`, `item_bundle`, `price_list`, `price_list_item`
  - [x] Multi-table writes executed atomically inside single Room `@Transaction` methods (`savePartyWithDetailsInTransaction`, `recordPartyLedgerMovementInTransaction`, `softDeletePartiesInTransaction`, `saveItemWithDetailsInTransaction`, `recordStockAdjustmentInTransaction`, `recordStockTransferInTransaction`, `bulkUpdateItemPricesAndTaxInTransaction`)
  - [x] Exported Room v2 schema JSON to `app/schemas/`
- [x] **B) Parties Module (`PartiesFoundationScreen.kt`)**:
  - [x] Segmented tabs (`Customers`, `Suppliers`, `All`), search bar, filter chips (`All Balances`, `To Collect`, `To Pay`, `Over Credit Limit`), multi-select mode with batch Archive
  - [x] Full Party Form: name, type (`CUSTOMER`, `SUPPLIER`, `BOTH`), primary & secondary phones, email, multiple billing & shipping addresses (`party_address`), GSTIN with offline format + Mod-36 checksum validation and automatic state-code & PAN detection, party group/category (`party_group`), opening balance (`To Collect` / `To Pay`), credit limit, credit days, price list (`price_list`), custom fields (`party_custom_field`), birthday/anniversary, and notes
  - [x] Import from Phone Contacts (optional runtime `READ_CONTACTS` permission + `ContactsContract` picker) and CSV/Excel import with live preview & duplicate detection, plus CSV/Excel export & downloadable template via Share Sheet
  - [x] Party Detail & Ledger Statement Sheet: chronological voucher ledger with running balance (`PartyLedgerCalculator`), date filters (`All Time`, `This Month`, `Last 30 Days`), Receivable/Payable Aging analysis (`0-30`, `31-60`, `61-90`, `90+` days), interactive Credit-Limit Warning hook for billing, new Ledger Movement recording (`SALE`, `PURCHASE`, `PAYMENT_IN`, `PAYMENT_OUT`), Printable Party Statement PDF via Android `PdfDocument` (`VanijyaPdfExporter`) + Share Sheet, and offline Payment-Reminder templates (`Polite`, `Overdue`, `Hindi`) opened via Share Sheet (WhatsApp/SMS/email)
  - [x] Registered `"Party Statement"` and `"Receivable/Payable Aging"` in the `Reports` tab catalogue (`ReportsFoundationScreen.kt`)
- [x] **C) Items & Stock Module (`ItemsFoundationScreen.kt`)**:
  - [x] Wired into Dashboard Stock summary tile, All Features grid (`Items and Stock` & `Import/Export`), and Dashboard `"More"` quick-action sheet (`Add Item`, `Scan Barcode`, `Stock Adjustment`)
  - [x] 4 Segmented tabs: `Items`, `Categories`, `Low Stock`, `Expiring`
  - [x] Real-time Stock Valuation header card (`Stock Value at Cost`, `Stock Value at Sale`, `Low Stock SKUs`, `Expiring Batches`) and live alerts wired into Dashboard Stock tile, Operational Alerts strip, and Top-Bar Notification Bell
  - [x] Full Item Form: `PRODUCT` or `SERVICE`, name, SKU code (with duplicate detection), searchable bundled offline HSN/SAC directory (`assets/hsn_sac_directory.csv` with 64 Indian codes), category, brand, base & secondary units with conversion factor (`1 BOX = 12 PCS`), sale/purchase/wholesale/MRP prices in integer paise, GST slab (`0%` to `40%`) with inclusive/exclusive flag and cess, opening stock with as-of date, minimum-stock alert threshold, godown & rack/shelf location, compressed local photo attachment (`PickVisualMedia` -> 80% JPEG in local storage), barcode/QR code (offline Code-128 bitmap generator, CameraX live scanner preview, and manual entry fallback), batch tracking with manufacturing & expiry dates (`item_batch`), serial/IMEI tracking (`item_serial`), size/colour variants (`item_variant`), and item bundles/kits (`item_bundle`)
  - [x] Stock Ledger & Operations: chronological stock ledger (`stock_ledger`) with running closing quantity, Stock Adjustment sheet (`ADD`/`REDUCE` with reason: `Audit Surplus`, `Damage`, `Shrinkage`, `Expired`, `Internal Use`), Godown Transfer sheet (`stock_transfer` between multiple warehouses), Bulk Price & GST Slab Update sheet, Price History in Item Detail sheet, and CSV/Excel template/import/export + Stock Valuation PDF via Android `PdfDocument`

## Step 3A — GST Calculation Engine, Sales Documents, Payments, Cheques, Recurring Invoices & Database v3 (`MIGRATION_2_3`)
- [x] **A) Pure-Kotlin GST Calculation Engine (`GstCalculationEngine.kt`)**:
  - [x] Intra-state (`CGST + SGST` split equally in basis points) vs Inter-state / Export / SEZ (`IGST` full rate) automatic place-of-supply detection
  - [x] Compensation cess (`cessBps`), Composition scheme (`Bill of Supply`, zero tax output), and Reverse-Charge Mechanism (`RCM`, tax computed for disclosure without adding to customer payable)
  - [x] Tax-exclusive and tax-inclusive line item pricing (`Taxable = Gross * 10000 / (10000 + RateBps + CessBps)`), per-line % or ₹ discount, proportional invoice-level discount allocation across lines, and taxable/non-taxable additional charges (freight, packing, shipping)
  - [x] Round-off modes (`NONE`, `NEAREST`, `UP`, `DOWN`) to nearest Rupee, TCS (`tcsBps`) and TDS (`tdsBps`) computation, HSN-wise tax summary rows (`HsnTaxSummaryRow`), automatic GST classification (`B2B`, `B2CL`, `B2CS`, `EXPORT`, `SEZ`, `COMPOSITION`, `NIL_EXEMPT`), and Indian Lakh/Crore amount-in-words (`IndianNumberToWords`)
  - [x] Driven by editable slabs in the `tax_rate` table
- [x] **B) Database v3 (`MIGRATION_2_3` + 9 New Room Tables)**:
  - [x] Non-destructive `MIGRATION_2_3` upgrading `VanijyaDatabase` from v2 to v3 without dropping or renaming any v1 or v2 table
  - [x] All 9 Step 3A tables with indices and integer paise (`Long`) money columns: `txn`, `txn_line`, `txn_charge`, `txn_tax_line`, `txn_link`, `payment`, `payment_allocation`, `cheque`, `recurring_template`
  - [x] Atomic `@Transaction` lifecycle in `Step3ADao`:
    - `saveOrUpdateSalesDocumentInTransaction`: gap-free document numbering (`number_series` with prefix and FY reset), edit mode reversing old `stock_ledger` & `party_ledger_entry` effects before applying updated lines, batch/serial updates, immediate partial/full payment & `payment_allocation`, conversion `txn_link`, and `audit_log` entry
    - `cancelDocumentInTransaction`: preserves gap-free voucher number, records cancellation reason, reverses stock & party ledger movements, and logs to `audit_log`
    - `recordPaymentWithAllocationsInTransaction`: multi-mode payment (`UPI`, `CASH`, `BANK_NEFT`, `CHEQUE`, `SPLIT`), bill-to-bill invoice settlement (`payment_allocation`), advance/unallocated credit tracking, payment discount, cheque registration, party ledger credit/debit, and `audit_log`
    - `updateChequeStatusInTransaction`: marks cheque `CLEARED` or `BOUNCED`; on `BOUNCED`, atomically reverses settled invoices back to `UNPAID`/`PARTIAL`, debits party ledger for cheque amount + bounce charges, and logs to `audit_log`
- [x] **C) Billing Hub, Document Builders & Conversion Chain (`SalesFoundationScreen.kt`)**:
  - [x] 6 Billing segments (`Sales`, `Quotations`, `Orders`, `Challans`, `Returns`, `Payments`) with contextual floating `+ New` button creating the document type for the active segment
  - [x] 10 Document types supported: `Tax Invoice`, `Bill of Supply`, `Retail/Cash Sale`, `Sale Order`, `Estimate/Quotation`, `Proforma Invoice`, `Delivery Challan`, `Sale Return / Credit Note`, `Debit Note`, and `Payment-In`
  - [x] Search bar, Status filter chips (`All`, `Paid`, `Partial`, `Unpaid`, `Overdue`, `Cancelled`), Date range filter (`All Dates`, `Today`, `This Week`, `This Month`, `This FY`), Party filter chip, and live segment Totals Header (`Total`, `Tax`, `Collected`, `Balance`)
  - [x] Full GST Invoice & Document Builder Sheet: party selector with inline Quick-Add Party & live Credit-Limit Warning banner (`PartyLedgerCalculator`), gap-free voucher number preview, invoice & due dates, payment terms, place of supply (`Validators.indianStateMap`), line items with unit/rate/discount/GST slab/inclusive toggle/free qty/batch & serial pickers, additional charges, round-off mode, RCM toggle, TCS/TDS bps, notes/terms/signature, immediate payment received (with cheque details when mode is `CHEQUE`), and live HSN + CGST/SGST/IGST breakdown card
  - [x] Document Detail & One-Tap Conversion Sheet: full line & HSN tax breakdown, linked conversion history trace (`txn_link`), one-tap conversion (`Estimate/Quotation -> Sale Order -> Delivery Challan -> Tax Invoice -> Sale Return / Credit Note`), Edit document, Cancel with reason dialog, and Share Voucher PDF (`Step3AExportersAndRecurringHelper`)
- [x] **D) Payments, Cheque Register, Recurring Invoices, Dashboard & Reports Wiring**:
  - [x] Bill-to-Bill Payment-In Sheet with automatic FIFO or manual per-invoice allocation (`payment_allocation`), discount on settlement, and cheque drawer/bank/number capture
  - [x] Cheque Register Sheet with filter chips (`All`, `Pending`, `Deposited`, `Cleared`, `Bounced`), one-tap Clear, and Bounce dialog with penalty charges & atomic invoice/ledger reversal
  - [x] Recurring Invoices Sheet (`WEEKLY`, `MONTHLY`, `QUARTERLY`, `YEARLY`) with `"Generate Now"`, automatic due-template scheduler on app startup, and local notification via `RecurringInvoiceNotificationHelper`
  - [x] Dashboard `Sales`, `Profit`, `To Collect`, `Overdue invoices`, `Cheques due`, and `Recent Activity` wired to live Step 3A Room tables respecting the selected Dashboard date range, plus Quick Actions (`Sale`, `Quotation`) and `More` sheet actions (`Payment In`, `Sale Order`, `Delivery Challan`, `Sale Return`) opening the real builders
  - [x] Registered `"Sale Report"` and `"Bill-wise Profit"` in the `Reports` tab catalogue (`ReportsFoundationScreen.kt`) with date-filtered live tables and PDF export (`Step3AExportersAndRecurringHelper`)
  - [x] **Shared Voucher Builder & Complete Billing Redesign (`BillingComponents.kt` + `SalesFoundationScreen.kt`)**:
    - Replaced the cramped voucher form with a clean, full-screen scrolling card layout (`12.dp` gaps, `16.dp` screen margins, `24.dp` card radius, ~88% opaque `SectionCard` fill) covering `Sale`, `Purchase`, `Quotation`, `Sale Order`, `Purchase Order`, `Delivery Challan`, `Sale Return`, and `Purchase Return` (plus `Cash Sale` and `Bill of Supply`), hiding the bottom navigation bar while open.
    - Built 9 reusable components in `BillingComponents.kt`:
      1. `SectionCard`: High-readability (~88% opaque) glass section container with `24.dp` radius and `1.dp` border.
      2. `DocTypeChips`: Horizontal document type selector (`Sale`, `Purchase`, `Quotation`, `Sale Order`, `Purchase Order`, `Challan`, `Sale Return`, `Purchase Return`, `Cash Sale`, `Bill of Supply`) with unsaved-item confirmation prompt before switching.
      3. `DocNumberChip`: Compact `INV-0001 [pencil]` chip opening a bottom sheet to edit Prefix and Next Number with live preview, paired on a single row with `Date` and `Due date` / `Valid till` / `Delivery` date pills.
      4. `PartyCard`: Search field, recent-party chips, `+ New Party` button, `"Cash sale"` toggle, selected party summary (phone, GSTIN, state, receivable/payable balance), Place of supply badge (`Intra-state (CGST + SGST)` vs `Inter-state (IGST)`) with `[Change]`, plus document-specific variants (Purchase supplier bill no/date & `ITC Eligible` toggle; Returns original invoice link & `Adjust in Ledger / Immediate Refund` choice; Challan hide-prices toggle).
      5. `ItemLineCard`: Scannable item card with bold `16.sp` item name & tabular line total, `HSN | qty UNIT × ₹rate | Disc | GST` subtitle, quick `[-] qty [+]` stepper, reorder (`↑`/`↓`), `[edit]`, `[bin]`, and swipe-to-delete with Undo banner.
      6. `ItemEditSheet`: Full item line bottom sheet with searchable catalog picker, quantity & unit chips, rate, discount (`%` or `₹` toggle), GST rate chips (`0%`, `5%`, `12%`, `18%`, `28%`, `40%`), tax inclusive/exclusive switch, batch/serial, live line-total preview, and Save/Delete buttons.
      7. `BillSummaryCard`: Reused across the billing builder, invoice detail screen, and share/PDF preview sheet; displays `Subtotal`, `Discount` (emerald with `-` sign & `[+ add discount]`), `Taxable amount`, `TAX` mini-heading with `CGST`/`SGST`/`IGST` grouped by slab + expandable `Tax breakup`, `Other charges (freight etc.)` (`[+ add]`), `Round off`, **GRAND TOTAL** (`28.sp` bold white on gradient pill), and **Amount in words** (zero rows stay visible and dimmed).
      8. `PaymentCard`: Shown for Sale, Purchase, and Returns (hidden for Quotation, Order, and Challan); defaults to `Cash` (never `Bank`), with `[ Paid ] [ Partial ] [ Unpaid ]` status chips, amount received/paid input, `[Cash] [UPI] [Card] [Bank] [Cheque]` mode chips, and coral/emerald `Balance due`.
      9. `StickyTotalBar`: Fixed bottom bar (`imePadding()` + `navigationBarsPadding()`) showing `Total` and `Balance` on the left, primary CTA with live total (`"Save & Generate Invoice (₹236.00)"`, `"Save Purchase Bill (₹...)"`, `"Save Quotation (₹...)"`) on the right, and `[v]` menu (`Save & Share`, `Save & New`, `Save as Draft`, `Cancel`).
    - Added inline validation banner with auto-scroll to the first invalid section and an unsaved-changes `"Save draft or discard?"` dialog (`Save Draft`, `Discard`, `Keep Editing`) when pressing Close (`X`) or system Back.
  - [x] **Billing Document List Row Redesign (`DocumentCard`, `StatusChip`, `DocumentActionsRow`)**:
    - Redesigned all 7 Billing segments (`Sales`, `Purchases`, `Quotations`, `Orders`, `Challans`, `Returns`, `Payments`) so every document is rendered as a single self-contained glass card (`16.dp` padding, `24.dp` radius, `12.dp` gap between cards) with nothing drawn between cards.
    - `StatusChip`: `maxLines = 1`, `softWrap = false`, wrap-content width, `10.dp` horizontal & `4.dp` vertical padding, `12.sp` medium text, leading status dot or `Link` icon (for `Converted`), `CircleShape`, and theme-aware high-contrast colours across Light, Dark, and AMOLED (`Paid`/`Accepted`/`Completed`/`Invoiced`/`Adjusted` in emerald; `Partial`/`Pending`/`Partially fulfilled` in amber; `Unpaid`/`Rejected`/`Refunded` in coral; `Overdue`/`Bounced` in red; `Cancelled`/`Expired` in grey; `Draft` in grey outline; `Converted` in violet with link icon; `Open` in blue).
    - Segment-accurate status & subtitle rules: `Quotations` show `Open`/`Accepted`/`Converted`/`Expired`/`Rejected` and `"Valid till <date>"` (never `Unpaid`, `Paid`, or `Balance`); `Orders` show `Pending`/`Partially fulfilled`/`Completed`; `Challans` show `Pending`/`Invoiced`; `Returns` and `Payments` show `Adjusted`/`Refunded`/`Pending`; `Sales` and `Purchases` show `Paid`/`Partial`/`Unpaid`/`Overdue`/`Cancelled` with the balance line.
    - Link badge (`"Converted to SO/..."` or `"Linked: EST/..."`) is rendered inside the card and tappable to open the linked document; amount-in-words is shown only on the document detail screen.
    - Sticky search bar with horizontally scrollable filter chips (`All`, `Today`, `This week`, `This month`, plus segment-specific status chips), totals header (`"N documents  |  ₹..."`), muted date section headers (`Today`, `Yesterday`, `This Week`, `Earlier`), and a collapsible floating `"New ..."` button (`VanijyaExtendedFab(expanded)`) with `196.dp` bottom list padding.

## Upcoming Steps (Planned)
- [ ] **Step 3B**: Multi-Template Invoice PDFs (A4 Classic/Modern/Compact, Thermal 58mm/80mm) & Fast POS Billing Counter
- [ ] **Step 4**: Purchases, Purchase Orders, Debit Notes & Stock Inward
- [ ] **Step 5**: Cash Book, Bank Accounts, UPI QR Generator & Expense Vouchers
- [ ] **Step 6**: Staff Directory, Daily Attendance, Advances, Monthly Payroll & Printable Payslip PDF
- [ ] **Step 7A & 7B**: GST Reports (GSTR-1, GSTR-3B, HSN Summary), Business Reports & ITR Pack
- [ ] **Step 8 & 9**: Single-File `.vbk` Encrypted Backup/Restore & Final Polish

