package com.bankbone.core.ledger.domain.events

import com.bankbone.core.ledger.domain.LedgerTransaction
import com.bankbone.core.sharedkernel.domain.Asset
import com.bankbone.core.sharedkernel.domain.DomainEvent
import java.math.BigDecimal
import java.time.Instant

data class LedgerTransactionPosted(
    val transactionId: LedgerTransaction.Id,
    val totalAmount: BigDecimal,
    val asset: Asset,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent