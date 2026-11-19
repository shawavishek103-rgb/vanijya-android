package com.example.feature.purchases

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.SectionHeader
import com.example.core.designsystem.VanijyaEmptyState

@Composable
fun PurchasesFoundationScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SectionHeader(title = "Purchases, Supplier Bills & Expenses")
        Spacer(modifier = Modifier.height(12.dp))
        VanijyaEmptyState(
            icon = Icons.Default.ShoppingCart,
            title = "Purchases & Expense Vouchers Foundation",
            description = "Step 1 Foundation Ready: Supplier inward bills, input tax credit (ITC), purchase orders, and direct/indirect expense vouchers attach here."
        )
    }
}
