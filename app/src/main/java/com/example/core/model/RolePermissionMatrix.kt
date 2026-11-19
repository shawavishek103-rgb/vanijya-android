package com.example.core.model

enum class AppRole(val displayName: String, val badgeHindi: String, val description: String) {
    OWNER("Owner", "मालिक", "Full access to all business modules, settings, staff, and encrypted backups"),
    MANAGER("Manager", "प्रबंधक", "Can create/edit invoices, parties, stock, and staff attendance; cannot reset app"),
    SALESPERSON("Salesperson", "सेल्सपर्सन", "Can create sales, quotations, and payment-in; read-only items & parties"),
    ACCOUNTANT("Accountant", "मुनीम / लेखाकार", "Full access to GST, reports, cash/bank, purchases, sales, and PDF export"),
    VIEWER("Viewer", "दर्शक", "Read-only access to dashboards, ledgers, and reports")
}

enum class AppModule(val displayName: String) {
    SALES("Sales & Invoicing"),
    PURCHASES("Purchases & Bills"),
    PARTIES("Parties & Ledgers"),
    ITEMS("Items & Inventory"),
    CASH_BANK("Cash & Bank"),
    EXPENSES("Expenses"),
    STAFF_PAYROLL("Staff & Payroll"),
    GST("GST Returns & Tax"),
    REPORTS("Business Reports"),
    BACKUP("Vault Backup (.vbk)"),
    SETTINGS("Company Settings")
}

enum class PermissionAction {
    VIEW,
    CREATE,
    EDIT,
    DELETE,
    EXPORT
}

data class ModulePermission(
    val role: AppRole,
    val module: AppModule,
    val canView: Boolean,
    val canCreate: Boolean,
    val canEdit: Boolean,
    val canDelete: Boolean,
    val canExport: Boolean
)

object RolePermissionDefaults {

    fun defaultPermissionsForRole(role: AppRole): List<ModulePermission> {
        return AppModule.entries.map { module ->
            when (role) {
                AppRole.OWNER -> ModulePermission(
                    role = role,
                    module = module,
                    canView = true,
                    canCreate = true,
                    canEdit = true,
                    canDelete = true,
                    canExport = true
                )
                AppRole.MANAGER -> ModulePermission(
                    role = role,
                    module = module,
                    canView = true,
                    canCreate = module != AppModule.SETTINGS,
                    canEdit = module != AppModule.SETTINGS,
                    canDelete = module in setOf(AppModule.SALES, AppModule.PURCHASES, AppModule.EXPENSES),
                    canExport = true
                )
                AppRole.SALESPERSON -> ModulePermission(
                    role = role,
                    module = module,
                    canView = module in setOf(AppModule.SALES, AppModule.PARTIES, AppModule.ITEMS, AppModule.CASH_BANK),
                    canCreate = module in setOf(AppModule.SALES, AppModule.PARTIES),
                    canEdit = module == AppModule.SALES,
                    canDelete = false,
                    canExport = module == AppModule.SALES
                )
                AppRole.ACCOUNTANT -> ModulePermission(
                    role = role,
                    module = module,
                    canView = module != AppModule.SETTINGS,
                    canCreate = module in setOf(AppModule.SALES, AppModule.PURCHASES, AppModule.CASH_BANK, AppModule.EXPENSES, AppModule.GST),
                    canEdit = module in setOf(AppModule.SALES, AppModule.PURCHASES, AppModule.CASH_BANK, AppModule.EXPENSES, AppModule.GST, AppModule.STAFF_PAYROLL),
                    canDelete = false,
                    canExport = true
                )
                AppRole.VIEWER -> ModulePermission(
                    role = role,
                    module = module,
                    canView = module != AppModule.SETTINGS && module != AppModule.BACKUP,
                    canCreate = false,
                    canEdit = false,
                    canDelete = false,
                    canExport = false
                )
            }
        }
    }
}
