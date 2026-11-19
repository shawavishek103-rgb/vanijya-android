# Vanijya Privacy Policy (`PRIVACY.md`)

**Effective Date:** November 2026  
**Architecture:** 100% Air-Gapped Local Storage

## 1. Data Never Leaves Your Device

Vanijya is engineered from the ground up as a **100% offline-first** Android application:

- **Zero Network Access**: Vanijya does **not** request `android.permission.INTERNET` or `android.permission.ACCESS_NETWORK_STATE`. The application is technically incapable of opening network sockets or transmitting data over Wi-Fi or mobile data.
- **No Cloud Servers or Third-Party SDKs**: There are no Firebase services, no Google Analytics, no crash-reporting beacons, no advertising SDKs, and no external AI/LLM API calls.

## 2. On-Device Encryption at Rest

- All business profiles, customer/supplier ledgers, invoices, inventory records, GST returns, and staff payroll records are stored inside an on-device **SQLCipher 256-bit AES encrypted database**.
- The encryption passphrase is protected by a hardware-backed master key generated inside the **Android Keystore** (`AndroidKeyStore`) and never leaves the device's secure enclave.

## 3. Biometric & PIN Access Control

- Biometric verification uses Android's system `BiometricPrompt` APIs locally on your device hardware. Biometric templates are verified by the Android OS TEE/StrongBox and are never accessible to the app.
- App PINs are salted and hashed locally using `PBKDF2WithHmacSHA256`.

## 4. Backups & PDF Exports

- Backups (`.vbk` encrypted archives) and PDF exports (invoices, payslips, statements) are generated strictly on your device and saved only to folders or share targets that you explicitly choose via the Android Storage Access Framework (SAF).
