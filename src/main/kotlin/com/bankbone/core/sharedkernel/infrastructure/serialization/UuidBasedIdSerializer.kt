package com.bankbone.core.sharedkernel.infrastructure.serialization

import com.bankbone.core.sharedkernel.domain.AggregateId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.UUID

/**
 * Creates a serializer for an AggregateId type that uses UUID as its underlying value.
 *
 * @param T The specific AggregateId type
 * @param fromUuid A factory function to create an instance of T from a UUID
 * @param serialName Optional custom serial name (defaults to the class name)
 */
inline fun <reified T : AggregateId> aggregateIdSerializer(
    crossinline fromUuid: (UUID) -> T,
    serialName: String = T::class.qualifiedName ?: T::class.simpleName!!
): KSerializer<T> = object : KSerializer<T> {
    override val descriptor = PrimitiveSerialDescriptor(serialName, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeString(value.value.toString())
    }

    override fun deserialize(decoder: Decoder): T {
        return fromUuid(UUID.fromString(decoder.decodeString()))
    }
}

/**
 * Creates a serializer for an AggregateId type with a companion object that has a `fromUuid` factory function.
 *
 * @param T The specific AggregateId type
 * @param companion The companion object that contains the fromUuid function
 * @param serialName Optional custom serial name (defaults to the class name)
 */
inline fun <reified T : AggregateId> aggregateIdSerializer(
    companion: Any,
    serialName: String = T::class.qualifiedName ?: T::class.simpleName!!
): KSerializer<T> {
    val fromUuid = companion::class.java.getMethod("fromUuid", UUID::class.java)
    return aggregateIdSerializer(
        fromUuid = { uuid -> fromUuid.invoke(companion, uuid) as T },
        serialName = serialName
    )
}