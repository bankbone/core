package com.bankbone.core.sharedkernel.infrastructure.serialization

import com.bankbone.core.sharedkernel.domain.AggregateId
import kotlinx.serialization.KSerializer as KotlinxKSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.UUID

abstract class UuidBasedIdSerializer<T : AggregateId>(
    serialName: String,
    private val fromUuid: (UUID) -> T,
) : KotlinxKSerializer<T> {

    override val descriptor = PrimitiveSerialDescriptor(serialName, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: T) = encoder.encodeString(value.value.toString())

    override fun deserialize(decoder: Decoder): T = fromUuid(UUID.fromString(decoder.decodeString()))
}