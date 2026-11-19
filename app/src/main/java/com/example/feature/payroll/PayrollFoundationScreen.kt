package com.example.feature.payroll

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.SectionHeader
import com.example.core.designsystem.VanijyaEmptyState

@Composable
fun PayrollFoundationScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SectionHeader(title = "Monthly Payroll, PF/ESI & PDF Payslips")
        Spacer(modifier = Modifier.height(12.dp))
        VanijyaEmptyState(
            icon = Icons.Default.Payments,
            title = "Staff Payroll Engine Foundation",
            description = "Step 1 Foundation Ready: Salary structures, advance deductions, attendance-linked payouts, and printable PDF payslips attach here."
        )
    }
}
