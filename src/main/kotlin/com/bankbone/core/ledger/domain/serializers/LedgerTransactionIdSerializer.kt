package com.bankbone.core.ledger.domain.serializers

import com.bankbone.core.ledger.domain.LedgerTransaction
import com.bankbone.core.sharedkernel.infrastructure.serialization.UuidBasedIdSerializer

object LedgerTransactionIdSerializer : UuidBasedIdSerializer<LedgerTransaction.Id>(
    serialName = "com.bankbone.core.ledger.domain.LedgerTransaction.Id",
    fromUuid = { uuid -> LedgerTransaction.Id(uuid) }
)
