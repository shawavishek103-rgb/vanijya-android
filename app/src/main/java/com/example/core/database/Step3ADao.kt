package com.example.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.core.model.Money
import com.example.core.model.SalesDocTypes
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface Step3ADao {

    // ========================================================================
    // 1. OBSERVERS & QUERIES
    // ========================================================================

    @Query("SELECT * FROM txn WHERE company_id = :companyId AND is_deleted = 0 ORDER BY txn_date DESC, created_at DESC")
    fun observeTransactions(companyId: String): Flow<List<TxnEntity>>

    @Query("SELECT * FROM txn WHERE company_id = :companyId AND is_deleted = 0 ORDER BY txn_date DESC, created_at DESC")
    suspend fun getTransactionsNow(companyId: String): List<TxnEntity>

    @Query("SELECT * FROM txn WHERE id = :txnId AND is_deleted = 0 LIMIT 1")
    suspend fun getTxnByIdNow(txnId: String): TxnEntity?

    @Query("SELECT * FROM txn_line WHERE company_id = :companyId AND is_deleted = 0 ORDER BY txn_id ASC, line_index ASC")
    fun observeAllTxnLines(companyId: String): Flow<List<TxnLineEntity>>

    @Query("SELECT * FROM txn_line WHERE txn_id = :txnId AND is_deleted = 0 ORDER BY line_index ASC")
    suspend fun getTxnLinesForTxnNow(txnId: String): List<TxnLineEntity>

    @Query("SELECT * FROM txn_charge WHERE company_id = :companyId AND is_deleted = 0 ORDER BY created_at ASC")
    fun observeAllTxnCharges(companyId: String): Flow<List<TxnChargeEntity>>

    @Query("SELECT * FROM txn_charge WHERE txn_id = :txnId AND is_deleted = 0 ORDER BY created_at ASC")
    suspend fun getTxnChargesForTxnNow(txnId: String): List<TxnChargeEntity>

    @Query("SELECT * FROM txn_tax_line WHERE company_id = :companyId AND is_deleted = 0 ORDER BY hsn_sac_code ASC")
    fun observeAllTxnTaxLines(companyId: String): Flow<List<TxnTaxLineEntity>>

    @Query("SELECT * FROM txn_tax_line WHERE txn_id = :txnId AND is_deleted = 0 ORDER BY hsn_sac_code ASC")
    suspend fun getTxnTaxLinesForTxnNow(txnId: String): List<TxnTaxLineEntity>

    @Query("SELECT * FROM txn_link WHERE company_id = :companyId AND is_deleted = 0 ORDER BY created_at DESC")
    fun observeTxnLinks(companyId: String): Flow<List<TxnLinkEntity>>

    @Query("SELECT * FROM payment WHERE company_id = :companyId AND is_deleted = 0 ORDER BY payment_date DESC, created_at DESC")
    fun observePayments(companyId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payment WHERE id = :paymentId AND is_deleted = 0 LIMIT 1")
    suspend fun getPaymentByIdNow(paymentId: String): PaymentEntity?

    @Query("SELECT * FROM payment_allocation WHERE company_id = :companyId AND is_deleted = 0 ORDER BY created_at DESC")
    fun observeAllPaymentAllocations(companyId: String): Flow<List<PaymentAllocationEntity>>

    @Query("SELECT * FROM payment_allocation WHERE payment_id = :paymentId AND is_deleted = 0 ORDER BY created_at ASC")
    suspend fun getAllocationsForPaymentNow(paymentId: String): List<PaymentAllocationEntity>

    @Query("SELECT * FROM cheque WHERE company_id = :companyId AND is_deleted = 0 ORDER BY due_date ASC, created_at DESC")
    fun observeCheques(companyId: String): Flow<List<ChequeEntity>>

    @Query("SELECT * FROM cheque WHERE id = :chequeId AND is_deleted = 0 LIMIT 1")
    suspend fun getChequeByIdNow(chequeId: String): ChequeEntity?

    @Query("SELECT * FROM recurring_template WHERE company_id = :companyId AND is_deleted = 0 ORDER BY next_run_date ASC")
    fun observeRecurringTemplates(companyId: String): Flow<List<RecurringTemplateEntity>>

    @Query("SELECT * FROM recurring_template WHERE company_id = :companyId AND is_deleted = 0 ORDER BY next_run_date ASC")
    suspend fun getRecurringTemplatesNow(companyId: String): List<RecurringTemplateEntity>

    // ========================================================================
    // 2. UPSERTS, DELETES & CROSS-TABLE HELPERS
    // ========================================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTxn(txn: TxnEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTxnLines(lines: List<TxnLineEntity>)

    @Query("DELETE FROM txn_line WHERE txn_id = :txnId")
    suspend fun deleteTxnLinesForTxn(txnId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTxnCharges(charges: List<TxnChargeEntity>)

    @Query("DELETE FROM txn_charge WHERE txn_id = :txnId")
    suspend fun deleteTxnChargesForTxn(txnId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTxnTaxLines(taxLines: List<TxnTaxLineEntity>)

    @Query("DELETE FROM txn_tax_line WHERE txn_id = :txnId")
    suspend fun deleteTxnTaxLinesForTxn(txnId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTxnLink(link: TxnLinkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPayment(payment: PaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentAllocations(allocations: List<PaymentAllocationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCheque(cheque: ChequeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecurringTemplate(template: RecurringTemplateEntity)

    @Query("DELETE FROM recurring_template")
    suspend fun clearRecurringTemplates()

    @Query("DELETE FROM cheque")
    suspend fun clearCheques()

    @Query("DELETE FROM payment_allocation")
    suspend fun clearPaymentAllocations()

    @Query("DELETE FROM payment")
    suspend fun clearPayments()

    @Query("DELETE FROM txn_link")
    suspend fun clearTxnLinks()

    @Query("DELETE FROM txn_tax_line")
    suspend fun clearTxnTaxLines()

    @Query("DELETE FROM txn_charge")
    suspend fun clearTxnCharges()

    @Query("DELETE FROM txn_line")
    suspend fun clearTxnLines()

    @Query("DELETE FROM txn")
    suspend fun clearTransactions()

    // Cross-table helpers for atomic transactions
    @Query("SELECT * FROM number_series WHERE company_id = :companyId AND doc_type = :seriesDocType AND is_deleted = 0 ORDER BY updated_at DESC LIMIT 1")
    suspend fun getNumberSeriesForType(companyId: String, seriesDocType: String): NumberSeriesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNumberSeries(series: NumberSeriesEntity)

    @Query("SELECT * FROM item WHERE id = :itemId AND is_deleted = 0 LIMIT 1")
    suspend fun getItemById(itemId: String): ItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: ItemEntity)

    @Query("SELECT * FROM item_batch WHERE id = :batchId AND is_deleted = 0 LIMIT 1")
    suspend fun getBatchById(batchId: String): ItemBatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBatch(batch: ItemBatchEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStockLedger(entry: StockLedgerEntity)

    @Query("UPDATE stock_ledger SET is_deleted = 1, updated_at = :updatedAt WHERE company_id = :companyId AND voucher_number = :voucherNumber")
    suspend fun markStockLedgerDeletedByVoucher(companyId: String, voucherNumber: String, updatedAt: Long)

    @Query("SELECT * FROM party WHERE id = :partyId AND is_deleted = 0 LIMIT 1")
    suspend fun getPartyById(partyId: String): PartyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertParty(party: PartyEntity)

    @Query("SELECT * FROM party_ledger_entry WHERE party_id = :partyId AND is_deleted = 0 ORDER BY entry_date ASC, created_at ASC")
    suspend fun getPartyLedgerEntriesNow(partyId: String): List<PartyLedgerEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPartyLedgerEntry(entry: PartyLedgerEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPartyLedgerEntries(entries: List<PartyLedgerEntryEntity>)

    @Query("UPDATE party_ledger_entry SET is_deleted = 1, updated_at = :updatedAt WHERE id = :entryId")
    suspend fun markPartyLedgerEntryDeleted(entryId: String, updatedAt: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // ========================================================================
    // 3. GAP-FREE NUMBER ALLOCATION WITH FINANCIAL-YEAR RESET
    // ========================================================================

    @Transaction
    suspend fun allocateGapFreeDocNumberInTransaction(
        companyId: String,
        docType: String,
        fyCode: String = "FY 2026-27",
        fallbackPrefix: String = "INV/26-27/",
        now: Long = System.currentTimeMillis()
    ): String {
        val seriesKey = SalesDocTypes.seriesDocTypeKey(docType)
        val existing = getNumberSeriesForType(companyId, seriesKey)

        if (existing == null) {
            val defaultPrefix = fallbackPrefix.ifBlank { SalesDocTypes.defaultPrefix(docType) }
            val newSeries = NumberSeriesEntity(
                id = "${companyId}_$seriesKey",
                companyId = companyId,
                docType = seriesKey,
                prefix = defaultPrefix,
                fyCode = fyCode,
                nextNumber = 2L,
                paddingDigits = 4,
                createdAt = now,
                updatedAt = now
            )
            upsertNumberSeries(newSeries)
            return "$defaultPrefix${1L.toString().padStart(4, '0')}"
        }

        // Reset sequence to 1 if financial year has changed
        if (existing.fyCode != fyCode && fyCode.isNotBlank()) {
            val resetSeries = existing.copy(
                fyCode = fyCode,
                nextNumber = 2L,
                updatedAt = now
            )
            upsertNumberSeries(resetSeries)
            return "${resetSeries.prefix}${1L.toString().padStart(resetSeries.paddingDigits.coerceAtLeast(3), '0')}"
        }

        val currentNo = existing.nextNumber.coerceAtLeast(1L)
        val formatted = "${existing.prefix}${currentNo.toString().padStart(existing.paddingDigits.coerceAtLeast(3), '0')}"
        upsertNumberSeries(existing.copy(nextNumber = currentNo + 1L, updatedAt = now))
        return formatted
    }

    // ========================================================================
    // 4. ATOMIC MULTI-TABLE TRANSACTION: SAVE OR EDIT SALES / BILLING DOCUMENT
    // ========================================================================

    @Transaction
    suspend fun saveOrUpdateSalesDocumentInTransaction(
        header: TxnEntity,
        lines: List<TxnLineEntity>,
        charges: List<TxnChargeEntity>,
        taxLines: List<TxnTaxLineEntity>,
        userId: String = "owner",
        userName: String = "Owner",
        sourceTxnIdForConversion: String? = null,
        conversionRelationship: String? = null,
        immediatePaymentReceivedPaise: Long = 0L,
        immediatePaymentModesJson: String = "CASH",
        immediatePaymentNumber: String? = null
    ): TxnEntity {
        val now = System.currentTimeMillis()
        val existingTxn = getTxnByIdNow(header.id)
        val isEditing = existingTxn != null

        // 1. If editing an existing active document, reverse its previous stock and party ledger effects first
        if (existingTxn != null && existingTxn.status != "Cancelled") {
            reverseDocumentEffectsInternal(existingTxn, now)
        }

        // 2. Allocate gap-free document number if blank
        val resolvedDocNumber = when {
            isEditing && existingTxn != null -> existingTxn.docNumber
            header.docNumber.isNotBlank() -> header.docNumber
            else -> allocateGapFreeDocNumberInTransaction(
                companyId = header.companyId,
                docType = header.docType,
                fallbackPrefix = SalesDocTypes.defaultPrefix(header.docType),
                now = now
            )
        }

        val paidPaise = when {
            header.docType == SalesDocTypes.CASH_SALE -> header.grandTotalPaise
            immediatePaymentReceivedPaise > 0L -> immediatePaymentReceivedPaise.coerceAtMost(header.grandTotalPaise)
            existingTxn != null -> existingTxn.paidAmountPaise.coerceAtMost(header.grandTotalPaise)
            else -> header.paidAmountPaise.coerceIn(0L, header.grandTotalPaise)
        }
        val balancePaise = (header.grandTotalPaise - paidPaise).coerceAtLeast(0L)
        val resolvedStatus = when {
            header.status == "Cancelled" -> "Cancelled"
            header.status == "Converted" -> "Converted"
            header.docType == SalesDocTypes.CREDIT_NOTE -> "Paid"
            paidPaise >= header.grandTotalPaise && header.grandTotalPaise > 0L -> "Paid"
            paidPaise > 0L -> "Partial"
            header.dueDate in 1 until now && SalesDocTypes.affectsPartyLedger(header.docType) -> "Overdue"
            else -> header.status.ifBlank { "Unpaid" }
        }

        val finalHeader = header.copy(
            docNumber = resolvedDocNumber,
            paidAmountPaise = paidPaise,
            balanceAmountPaise = balancePaise,
            status = resolvedStatus,
            updatedAt = now
        )

        // 3. Upsert header, lines, charges, tax lines
        upsertTxn(finalHeader)
        deleteTxnLinesForTxn(finalHeader.id)
        if (lines.isNotEmpty()) {
            upsertTxnLines(lines.map { it.copy(txnId = finalHeader.id, updatedAt = now) })
        }
        deleteTxnChargesForTxn(finalHeader.id)
        if (charges.isNotEmpty()) {
            upsertTxnCharges(charges.map { it.copy(txnId = finalHeader.id, updatedAt = now) })
        }
        deleteTxnTaxLinesForTxn(finalHeader.id)
        if (taxLines.isNotEmpty()) {
            upsertTxnTaxLines(taxLines.map { it.copy(txnId = finalHeader.id, updatedAt = now) })
        }

        // 4. Optional conversion link (`txn_link`)
        if (!sourceTxnIdForConversion.isNullOrBlank()) {
            val sourceTxn = getTxnByIdNow(sourceTxnIdForConversion)
            if (sourceTxn != null) {
                insertTxnLink(
                    TxnLinkEntity(
                        id = UUID.randomUUID().toString(),
                        companyId = finalHeader.companyId,
                        sourceTxnId = sourceTxn.id,
                        sourceDocType = sourceTxn.docType,
                        sourceDocNumber = sourceTxn.docNumber,
                        targetTxnId = finalHeader.id,
                        targetDocType = finalHeader.docType,
                        targetDocNumber = finalHeader.docNumber,
                        relationship = conversionRelationship ?: "${sourceTxn.docType}_TO_${finalHeader.docType}",
                        createdAt = now
                    )
                )
                if (sourceTxn.docType in setOf(SalesDocTypes.ESTIMATE, SalesDocTypes.PROFORMA, SalesDocTypes.SALE_ORDER, SalesDocTypes.PURCHASE_ORDER, SalesDocTypes.DELIVERY_CHALLAN)) {
                    upsertTxn(sourceTxn.copy(status = "Converted", updatedAt = now))
                }
            }
        }

        // 5. Apply Stock Movements if document affects inventory
        if (finalHeader.status != "Cancelled" && SalesDocTypes.affectsStock(finalHeader.docType)) {
            val isStockInward = SalesDocTypes.isInwardStock(finalHeader.docType)
            lines.forEach { line ->
                val item = getItemById(line.itemId)
                if (item != null && item.itemType == "PRODUCT") {
                    val unitMovementQty = line.qty.coerceAtLeast(0L) + line.freeQty.coerceAtLeast(0L)
                    if (unitMovementQty > 0L) {
                        val qtyDelta = if (isStockInward) unitMovementQty else -unitMovementQty
                        val newStockQty = item.currentStockQty + qtyDelta
                        upsertItem(item.copy(currentStockQty = newStockQty, updatedAt = now))

                        if (line.batchId.isNotBlank()) {
                            val batch = getBatchById(line.batchId)
                            if (batch != null) {
                                upsertBatch(
                                    batch.copy(
                                        availableQty = (batch.availableQty + qtyDelta).coerceAtLeast(0L),
                                        updatedAt = now
                                    )
                                )
                            }
                        }

                        upsertStockLedger(
                            StockLedgerEntity(
                                id = "${finalHeader.id}_stk_${line.lineIndex}",
                                companyId = finalHeader.companyId,
                                itemId = item.id,
                                itemName = item.name,
                                warehouseId = finalHeader.warehouseId.ifBlank { item.warehouseId },
                                warehouseName = finalHeader.warehouseName.ifBlank { item.warehouseName },
                                batchId = line.batchId,
                                movementType = when {
                                    finalHeader.docType == SalesDocTypes.PURCHASE_BILL -> "PURCHASE_IN"
                                    isStockInward -> "SALE_RETURN_IN"
                                    else -> "SALE_OUT"
                                },
                                voucherNumber = finalHeader.docNumber,
                                qtyDelta = qtyDelta,
                                closingQty = newStockQty,
                                unitRatePaise = line.unitPricePaise,
                                movementDate = finalHeader.txnDate,
                                remarks = "${finalHeader.docType} (${finalHeader.partyName})",
                                createdAt = now,
                                updatedAt = now
                            )
                        )
                    }
                }
            }
        }

        // 6. Apply Party Ledger Movement if document affects party khata
        if (finalHeader.status != "Cancelled" && finalHeader.partyId.isNotBlank() && SalesDocTypes.affectsPartyLedger(finalHeader.docType)) {
            val isCreditToParty = SalesDocTypes.isCreditToPartyLedger(finalHeader.docType)
            val debitPaise = if (isCreditToParty) 0L else finalHeader.grandTotalPaise
            val creditPaise = if (isCreditToParty) finalHeader.grandTotalPaise else 0L

            upsertPartyLedgerEntry(
                PartyLedgerEntryEntity(
                    id = "${finalHeader.id}_ple",
                    companyId = finalHeader.companyId,
                    partyId = finalHeader.partyId,
                    entryDate = finalHeader.txnDate,
                    voucherType = when (finalHeader.docType) {
                        SalesDocTypes.CREDIT_NOTE -> "CREDIT_NOTE"
                        SalesDocTypes.DEBIT_NOTE -> "DEBIT_NOTE"
                        SalesDocTypes.PURCHASE_BILL -> "PURCHASE_BILL"
                        else -> "SALE_INVOICE"
                    },
                    voucherNumber = finalHeader.docNumber,
                    debitPaise = debitPaise,
                    creditPaise = creditPaise,
                    runningBalancePaise = 0L,
                    dueDate = finalHeader.dueDate,
                    isSettled = balancePaise == 0L,
                    notes = "${SalesDocTypes.displayLabel(finalHeader.docType)} • ${finalHeader.classification}",
                    createdAt = now,
                    updatedAt = now
                )
            )

            // Immediate payment received on invoice creation
            if (immediatePaymentReceivedPaise > 0L && !isEditing) {
                val payId = "${finalHeader.id}_imm_pay"
                val payNo = immediatePaymentNumber ?: allocateGapFreeDocNumberInTransaction(
                    companyId = finalHeader.companyId,
                    docType = SalesDocTypes.PAYMENT_IN,
                    fallbackPrefix = "RCPT/26-27/",
                    now = now
                )
                val allocatedAmt = immediatePaymentReceivedPaise.coerceAtMost(finalHeader.grandTotalPaise)
                val immPay = PaymentEntity(
                    id = payId,
                    companyId = finalHeader.companyId,
                    paymentNumber = payNo,
                    paymentType = "PAYMENT_IN",
                    paymentDate = finalHeader.txnDate,
                    partyId = finalHeader.partyId,
                    partyName = finalHeader.partyName,
                    totalAmountPaise = allocatedAmt,
                    discountPaise = 0L,
                    unallocatedAdvancePaise = 0L,
                    primaryMode = immediatePaymentModesJson.substringBefore(":"),
                    modesSplitJson = immediatePaymentModesJson,
                    referenceNo = "",
                    status = "Paid",
                    notes = "Received against ${finalHeader.docNumber}",
                    createdAt = now,
                    updatedAt = now
                )
                upsertPayment(immPay)
                insertPaymentAllocations(
                    listOf(
                        PaymentAllocationEntity(
                            id = "${payId}_alloc_1",
                            companyId = finalHeader.companyId,
                            paymentId = payId,
                            txnId = finalHeader.id,
                            txnDocNumber = finalHeader.docNumber,
                            allocatedAmountPaise = allocatedAmt,
                            discountAllowedPaise = 0L,
                            createdAt = now,
                            updatedAt = now
                        )
                    )
                )
                upsertPartyLedgerEntry(
                    PartyLedgerEntryEntity(
                        id = "${payId}_ple",
                        companyId = finalHeader.companyId,
                        partyId = finalHeader.partyId,
                        entryDate = finalHeader.txnDate,
                        voucherType = "PAYMENT_IN",
                        voucherNumber = payNo,
                        debitPaise = 0L,
                        creditPaise = allocatedAmt,
                        runningBalancePaise = 0L,
                        dueDate = 0L,
                        isSettled = true,
                        notes = "Immediate receipt against ${finalHeader.docNumber}",
                        createdAt = now + 1L,
                        updatedAt = now + 1L
                    )
                )
            } else if (finalHeader.docType == SalesDocTypes.CASH_SALE && !isEditing) {
                upsertPartyLedgerEntry(
                    PartyLedgerEntryEntity(
                        id = "${finalHeader.id}_cash_ple",
                        companyId = finalHeader.companyId,
                        partyId = finalHeader.partyId,
                        entryDate = finalHeader.txnDate,
                        voucherType = "PAYMENT_IN",
                        voucherNumber = "${finalHeader.docNumber}-CASH",
                        debitPaise = 0L,
                        creditPaise = finalHeader.grandTotalPaise,
                        runningBalancePaise = 0L,
                        dueDate = 0L,
                        isSettled = true,
                        notes = "Retail / Cash Sale immediate settlement",
                        createdAt = now + 1L,
                        updatedAt = now + 1L
                    )
                )
            }

            recalculatePartyRunningBalanceInternal(finalHeader.partyId, now)
        }

        // 7. Audit Log
        insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                companyId = finalHeader.companyId,
                userId = userId,
                userName = userName,
                action = if (isEditing) "EDIT_${finalHeader.docType}" else "CREATE_${finalHeader.docType}",
                entityType = "txn",
                entityId = finalHeader.id,
                summary = "${if (isEditing) "Edited" else "Created"} ${finalHeader.docNumber} (${finalHeader.partyName}) for ${Money(finalHeader.grandTotalPaise).formatIndian()}",
                createdAt = now,
                updatedAt = now
            )
        )

        return finalHeader
    }

    // ========================================================================
    // 5. ATOMIC MULTI-TABLE TRANSACTION: CANCEL DOCUMENT (KEEP NUMBER + REASON)
    // ========================================================================

    @Transaction
    suspend fun cancelDocumentInTransaction(
        companyId: String,
        txnId: String,
        cancelReason: String,
        userId: String = "owner",
        userName: String = "Owner"
    ): TxnEntity? {
        val existing = getTxnByIdNow(txnId) ?: return null
        if (existing.status == "Cancelled") return existing
        val now = System.currentTimeMillis()

        reverseDocumentEffectsInternal(existing, now)

        val cancelled = existing.copy(
            status = "Cancelled",
            cancelReason = cancelReason.ifBlank { "Cancelled by user" },
            balanceAmountPaise = 0L,
            updatedAt = now
        )
        upsertTxn(cancelled)

        insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                companyId = companyId,
                userId = userId,
                userName = userName,
                action = "CANCEL_${existing.docType}",
                entityType = "txn",
                entityId = existing.id,
                summary = "Cancelled ${existing.docNumber} (${existing.partyName}). Reason: ${cancelled.cancelReason}",
                createdAt = now,
                updatedAt = now
            )
        )

        return cancelled
    }

    // ========================================================================
    // 6. ATOMIC MULTI-TABLE TRANSACTION: RECORD PAYMENT-IN WITH BILL-TO-BILL
    //    SETTLEMENT, MULTI-MODE SPLIT, DISCOUNT, ADVANCE & CHEQUE REGISTER
    // ========================================================================

    @Transaction
    suspend fun recordPaymentWithAllocationsInTransaction(
        payment: PaymentEntity,
        allocations: List<PaymentAllocationEntity>,
        cheque: ChequeEntity? = null,
        userId: String = "owner",
        userName: String = "Owner"
    ): PaymentEntity {
        val now = System.currentTimeMillis()
        val allocatedPayNo = if (payment.paymentNumber.isBlank()) {
            allocateGapFreeDocNumberInTransaction(
                companyId = payment.companyId,
                docType = payment.paymentType,
                fallbackPrefix = "RCPT/26-27/",
                now = now
            )
        } else {
            payment.paymentNumber
        }

        val totalAllocated = allocations.sumOf { it.allocatedAmountPaise }
        val unallocatedAdvance = (payment.totalAmountPaise - totalAllocated).coerceAtLeast(0L)

        val finalPayment = payment.copy(
            paymentNumber = allocatedPayNo,
            unallocatedAdvancePaise = unallocatedAdvance,
            chequeId = cheque?.id ?: payment.chequeId,
            updatedAt = now
        )
        upsertPayment(finalPayment)

        if (allocations.isNotEmpty()) {
            val updatedAllocations = allocations.map {
                it.copy(
                    paymentId = finalPayment.id,
                    createdAt = now,
                    updatedAt = now
                )
            }
            insertPaymentAllocations(updatedAllocations)

            updatedAllocations.forEach { alloc ->
                val targetTxn = getTxnByIdNow(alloc.txnId)
                if (targetTxn != null && targetTxn.status != "Cancelled") {
                    val newPaid = (targetTxn.paidAmountPaise + alloc.allocatedAmountPaise + alloc.discountAllowedPaise)
                        .coerceAtMost(targetTxn.grandTotalPaise)
                    val newBalance = (targetTxn.grandTotalPaise - newPaid).coerceAtLeast(0L)
                    val newStatus = if (newBalance == 0L) "Paid" else "Partial"
                    upsertTxn(
                        targetTxn.copy(
                            paidAmountPaise = newPaid,
                            balanceAmountPaise = newBalance,
                            status = newStatus,
                            updatedAt = now
                        )
                    )
                }
            }
        }

        if (cheque != null) {
            upsertCheque(
                cheque.copy(
                    paymentId = finalPayment.id,
                    partyId = finalPayment.partyId,
                    partyName = finalPayment.partyName,
                    updatedAt = now
                )
            )
        }

        if (finalPayment.partyId.isNotBlank()) {
            val isPaymentIn = finalPayment.paymentType == "PAYMENT_IN"
            val effectiveCredit = if (isPaymentIn) (finalPayment.totalAmountPaise + finalPayment.discountPaise) else 0L
            val effectiveDebit = if (!isPaymentIn) (finalPayment.totalAmountPaise + finalPayment.discountPaise) else 0L

            upsertPartyLedgerEntry(
                PartyLedgerEntryEntity(
                    id = "${finalPayment.id}_ple",
                    companyId = finalPayment.companyId,
                    partyId = finalPayment.partyId,
                    entryDate = finalPayment.paymentDate,
                    voucherType = finalPayment.paymentType,
                    voucherNumber = finalPayment.paymentNumber,
                    debitPaise = effectiveDebit,
                    creditPaise = effectiveCredit,
                    runningBalancePaise = 0L,
                    dueDate = 0L,
                    isSettled = true,
                    notes = "${finalPayment.primaryMode} receipt • ${finalPayment.notes}",
                    createdAt = now,
                    updatedAt = now
                )
            )
            recalculatePartyRunningBalanceInternal(finalPayment.partyId, now)
        }

        insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                companyId = finalPayment.companyId,
                userId = userId,
                userName = userName,
                action = "RECORD_${finalPayment.paymentType}",
                entityType = "payment",
                entityId = finalPayment.id,
                summary = "Recorded ${finalPayment.paymentNumber} (${finalPayment.partyName}) for ${Money(finalPayment.totalAmountPaise).formatIndian()} via ${finalPayment.primaryMode}",
                createdAt = now,
                updatedAt = now
            )
        )

        return finalPayment
    }

    // ========================================================================
    // 7. ATOMIC MULTI-TABLE TRANSACTION: CHEQUE CLEARANCE & BOUNCED-CHEQUE
    //    REVERSAL (RESTORES INVOICE BALANCE + DEBITS PARTY LEDGER + BOUNCE FEE)
    // ========================================================================

    @Transaction
    suspend fun updateChequeStatusInTransaction(
        companyId: String,
        chequeId: String,
        newStatus: String,
        bounceChargesPaise: Long = 0L,
        bounceReason: String = "",
        userId: String = "owner",
        userName: String = "Owner"
    ): ChequeEntity? {
        val cheque = getChequeByIdNow(chequeId) ?: return null
        val now = System.currentTimeMillis()

        val updatedCheque = cheque.copy(
            status = newStatus,
            depositedDate = if (newStatus == "DEPOSITED") now else cheque.depositedDate,
            clearedOrBouncedDate = if (newStatus == "CLEARED" || newStatus == "BOUNCED") now else cheque.clearedOrBouncedDate,
            bounceReason = if (newStatus == "BOUNCED") bounceReason.ifBlank { "Dishonoured / Insufficient Funds" } else cheque.bounceReason,
            bounceChargesPaise = if (newStatus == "BOUNCED") bounceChargesPaise.coerceAtLeast(0L) else cheque.bounceChargesPaise,
            updatedAt = now
        )
        upsertCheque(updatedCheque)

        if (newStatus == "BOUNCED" && cheque.status != "BOUNCED") {
            // 1. Mark linked payment as BOUNCED and reverse its bill-to-bill allocations
            if (cheque.paymentId.isNotBlank()) {
                val payment = getPaymentByIdNow(cheque.paymentId)
                if (payment != null) {
                    upsertPayment(payment.copy(status = "BOUNCED", updatedAt = now))
                    val allocations = getAllocationsForPaymentNow(payment.id)
                    allocations.forEach { alloc ->
                        val inv = getTxnByIdNow(alloc.txnId)
                        if (inv != null && inv.status != "Cancelled") {
                            val restoredPaid = (inv.paidAmountPaise - alloc.allocatedAmountPaise - alloc.discountAllowedPaise).coerceAtLeast(0L)
                            val restoredBalance = (inv.grandTotalPaise - restoredPaid).coerceAtLeast(0L)
                            val restoredStatus = if (restoredPaid == 0L) "Unpaid" else "Partial"
                            upsertTxn(
                                inv.copy(
                                    paidAmountPaise = restoredPaid,
                                    balanceAmountPaise = restoredBalance,
                                    status = restoredStatus,
                                    updatedAt = now
                                )
                            )
                        }
                    }
                }
            }

            // 2. Post Debit Reversal + Bounce Penalty in Party Ledger
            if (cheque.partyId.isNotBlank()) {
                val totalReversalPaise = cheque.amountPaise + bounceChargesPaise.coerceAtLeast(0L)
                val isReceivableCheque = cheque.chequeType == "RECEIVABLE" || cheque.chequeType == "RECEIVED"
                upsertPartyLedgerEntry(
                    PartyLedgerEntryEntity(
                        id = "${cheque.id}_bounce_ple",
                        companyId = companyId,
                        partyId = cheque.partyId,
                        entryDate = now,
                        voucherType = "DEBIT_NOTE",
                        voucherNumber = "CHQ-BNC-${cheque.chequeNumber}",
                        debitPaise = if (isReceivableCheque) totalReversalPaise else 0L,
                        creditPaise = if (!isReceivableCheque) totalReversalPaise else 0L,
                        runningBalancePaise = 0L,
                        dueDate = now,
                        isSettled = false,
                        notes = "Cheque #${cheque.chequeNumber} (${cheque.bankName}) bounced: ${updatedCheque.bounceReason}" +
                            if (bounceChargesPaise > 0L) " + Bounce charge ${Money(bounceChargesPaise).formatIndian()}" else "",
                        createdAt = now,
                        updatedAt = now
                    )
                )
                recalculatePartyRunningBalanceInternal(cheque.partyId, now)
            }
        }

        insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                companyId = companyId,
                userId = userId,
                userName = userName,
                action = "CHEQUE_$newStatus",
                entityType = "cheque",
                entityId = cheque.id,
                summary = "Cheque #${cheque.chequeNumber} (${cheque.partyName}) marked $newStatus",
                createdAt = now,
                updatedAt = now
            )
        )

        return updatedCheque
    }

    // ========================================================================
    // 8. INTERNAL REVERSAL & RUNNING BALANCE HELPERS
    // ========================================================================

    suspend fun reverseDocumentEffectsInternal(existingTxn: TxnEntity, now: Long) {
        if (SalesDocTypes.affectsStock(existingTxn.docType)) {
            val wasStockInward = SalesDocTypes.isInwardStock(existingTxn.docType)
            val oldLines = getTxnLinesForTxnNow(existingTxn.id)
            oldLines.forEach { oldLine ->
                val item = getItemById(oldLine.itemId)
                if (item != null && item.itemType == "PRODUCT") {
                    val oldUnitQty = oldLine.qty.coerceAtLeast(0L) + oldLine.freeQty.coerceAtLeast(0L)
                    val reverseDelta = if (wasStockInward) -oldUnitQty else oldUnitQty
                    val restoredQty = item.currentStockQty + reverseDelta
                    upsertItem(item.copy(currentStockQty = restoredQty, updatedAt = now))

                    if (oldLine.batchId.isNotBlank()) {
                        val batch = getBatchById(oldLine.batchId)
                        if (batch != null) {
                            upsertBatch(
                                batch.copy(
                                    availableQty = (batch.availableQty + reverseDelta).coerceAtLeast(0L),
                                    updatedAt = now
                                )
                            )
                        }
                    }
                }
            }
            markStockLedgerDeletedByVoucher(existingTxn.companyId, existingTxn.docNumber, now)
        }

        if (existingTxn.partyId.isNotBlank() && SalesDocTypes.affectsPartyLedger(existingTxn.docType)) {
            markPartyLedgerEntryDeleted("${existingTxn.id}_ple", now)
            markPartyLedgerEntryDeleted("${existingTxn.id}_cash_ple", now)
            recalculatePartyRunningBalanceInternal(existingTxn.partyId, now)
        }
    }

    suspend fun recalculatePartyRunningBalanceInternal(partyId: String, updatedAt: Long) {
        val party = getPartyById(partyId) ?: return
        val entries = getPartyLedgerEntriesNow(partyId)
        var runningPaise = 0L
        val updatedEntries = entries.map { entry ->
            runningPaise += (entry.debitPaise - entry.creditPaise)
            entry.copy(runningBalancePaise = runningPaise, updatedAt = updatedAt)
        }
        if (updatedEntries.isNotEmpty()) {
            upsertPartyLedgerEntries(updatedEntries)
        }
        upsertParty(
            party.copy(
                currentBalancePaise = runningPaise,
                updatedAt = updatedAt
            )
        )
    }
}
