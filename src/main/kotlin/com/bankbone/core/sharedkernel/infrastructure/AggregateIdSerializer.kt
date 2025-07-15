package com.bankbone.core.sharedkernel.infrastructure

import com.bankbone.core.sharedkernel.domain.AggregateId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.UUID

open class AggregateIdSerializer<T : AggregateId>(
    private val constructor: (UUID) -> T
) : KSerializer<T> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("AggregateId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeString(value.value.toString())
    }

    override fun deserialize(decoder: Decoder): T {
        return constructor(UUID.fromString(decoder.decodeString()))
    }
}