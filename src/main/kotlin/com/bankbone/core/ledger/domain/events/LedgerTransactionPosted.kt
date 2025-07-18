package com.bankbone.core.ledger.domain.events

import com.bankbone.core.ledger.domain.LedgerTransaction
import com.bankbone.core.sharedkernel.domain.Asset
import com.bankbone.core.sharedkernel.domain.DomainEvent
import com.bankbone.core.ledger.domain.serializers.LedgerTransactionIdSerializer
import com.bankbone.core.sharedkernel.infrastructure.serialization.BigDecimalSerializer
import com.bankbone.core.sharedkernel.infrastructure.serialization.InstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Serializable
@SerialName("LedgerTransactionPosted")
data class LedgerTransactionPosted(
    @Serializable(with = LedgerTransactionIdSerializer::class)
    val transactionId: LedgerTransaction.Id,
    @Serializable(with = BigDecimalSerializer::class)
    val totalAmount: BigDecimal,
    val asset: Asset,
    @Serializable(with = InstantSerializer::class)
    override val occurredAt: Instant
) : DomainEvent {
    @Transient
    override val aggregateId: UUID = transactionId.value
    @Transient
    override val eventType: String = "LedgerTransactionPosted"
}
