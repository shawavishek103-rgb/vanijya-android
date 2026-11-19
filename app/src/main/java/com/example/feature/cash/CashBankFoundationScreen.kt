package com.example.feature.cash

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.SectionHeader
import com.example.core.designsystem.VanijyaEmptyState

@Composable
fun CashBankFoundationScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SectionHeader(title = "Cash in Hand, Bank Accounts & Cheques")
        Spacer(modifier = Modifier.height(12.dp))
        VanijyaEmptyState(
            icon = Icons.Default.AccountBalanceWallet,
            title = "Cash & Bank Books Ready",
            description = "Step 1 Foundation Ready: Payment-In, Payment-Out, Contra entries, UPI QR receipts, and cheque tracking build on this module."
        )
    }
}
