package com.bankbone.core.ledger.domain.serializers

import com.bankbone.core.ledger.domain.LedgerTransaction
import com.bankbone.core.sharedkernel.infrastructure.AggregateIdSerializer

object LedgerTransactionIdSerializer : AggregateIdSerializer<LedgerTransaction.Id>({ uuid -> LedgerTransaction.Id(uuid) })
