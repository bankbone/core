package com.bankbone.core.ledger.domain

import com.bankbone.core.sharedkernel.domain.Asset
import com.bankbone.core.sharedkernel.domain.Amount
import java.math.BigDecimal
import java.time.Instant

enum class LedgerEntryType { DEBIT, CREDIT }

data class LedgerEntry(
    val accountId: Account.Id,
    val amount: Amount,
    val type: LedgerEntryType,
    val description: String? = null,
    val postedAt: Instant = Instant.now()
) {
    val asset: Asset
        get() = amount.asset

    init {
        require(amount.value > BigDecimal.ZERO) { "Amount must be positive" }
    }
}
