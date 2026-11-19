# Vanijya (वाणिज्य)

> *"Your business, in your pocket. Fully offline."*

**Vanijya** is a 100% offline-first native Android business management, GST invoicing, inventory, accounting, and staff payroll application built for Indian small and medium businesses (Kirana, Retail, Wholesale, Distributors, Manufacturing, and Service providers).

## Core Guarantees

1. **100% Air-Gapped Offline**: Vanijya does **not** declare `android.permission.INTERNET` or `ACCESS_NETWORK_STATE`. A custom Gradle verification task (`verifyNoInternetPermission`) inspects merged manifests at build time and fails the build if `INTERNET` ever appears.
2. **Hardware-Backed SQLCipher Encryption**: All business records reside in an encrypted SQLite database (`vanijya_encrypted_v1.db`) powered by **SQLCipher (256-bit AES)** with the database key wrapped by an AES-256-GCM master key inside the **Android Keystore**.
3. **Exact Integer Paise Arithmetic (`Money`)**: Monetary values are stored and calculated exclusively as `Long` integer paise (`1 Rupee = 100 Paise`) via the `@JvmInline value class Money(val paise: Long)` — never floating-point.
4. **Multi-Business & Role-Based Access Control (RBAC)**: Supports multiple isolated business partitions (`company_id`) and 5 local roles (`Owner`, `Manager`, `Salesperson`, `Accountant`, `Viewer`) with per-module permissions (`View`, `Create`, `Edit`, `Delete`, `Export PDF`) and instant user-switching by 6-digit PIN.

## Architecture & Tech Stack

- **Language**: Kotlin 2.2
- **UI**: Jetpack Compose + Material 3 (Light, Dark Indigo-Slate, and AMOLED Pitch-Black themes)
- **Typography**: Bundled `Plus Jakarta Sans` (`res/font/plus_jakarta_sans.ttf`) with tabular numerals (`tnum`) and Indian Lakh/Crore digit grouping (`12,34,567.00`)
- **DI**: Dagger Hilt
- **Database**: Room v1 (`exportSchema = true` -> `app/schemas/`) + SQLCipher (`net.zetetic:sqlcipher-android`)
- **Security**: AndroidX `BiometricPrompt` + PBKDF2-HMAC-SHA256 salted 6-digit PIN fallback + configurable Auto-Lock + `FLAG_SECURE` screenshot blocker

## Package Structure (Package-by-Feature)

- `com.example.core.model` — `Money` value class, `IndianNumberToWords` (Lakh/Crore), `Validators` (offline 15-char GSTIN & Mod-36 checksum, PAN, State codes), `RolePermissionMatrix`
- `com.example.core.security` — `KeystoreSqlCipherManager`, `PinHasher`, `BiometricAuthHelper`
- `com.example.core.database` — `VanijyaDatabase` (10 core v1 tables), `CoreEntities`, `CoreDao`, `CoreRepository`, `DatabaseModule`
- `com.example.core.designsystem` — `VanijyaWordmark`, `VanijyaButton`, `AmountText`, `StatTile`, `PartyCard`, `InvoiceRow`, `SectionHeader`, `VanijyaEmptyState`, `SkeletonBox`, `SalesSparklineChart`
- `com.example.core.navigation` — `VanijyaMainShell` (Floating Bottom Bar, centered `+` Quick-Action Sheet, `More` Hub, Tablet `NavigationRail`)
- `com.example.feature.onboarding` — 4-step Onboarding wizard
- `com.example.feature.security` — Biometric + 6-digit PIN Lock Screen
- `com.example.feature.home` — Bento-grid Home Dashboard
- `com.example.feature.settings` — Invoice prefixes, rounding, decimals, date formats, theme, language, auto-lock, GST slab editor, RBAC matrix editor, and Biometric/PIN Reset
- `com.example.feature.{parties,items,sales,purchases,cash,staff,payroll,gst,reports}` — Modular feature packages
