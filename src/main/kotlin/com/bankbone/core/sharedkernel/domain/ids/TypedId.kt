package com.bankbone.core.sharedkernel.domain.ids

import com.bankbone.core.sharedkernel.domain.AggregateId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

/**
 * A type-safe wrapper around UUID for domain entity IDs.
 * 
 * @param T The type of entity this ID identifies (used for type safety)
 * @property value The underlying UUID value
 */
@JvmInline
@kotlinx.serialization.Serializable(with = TypedIdSerializer::class)
value class TypedId<out T>(
    override val value: UUID
) : AggregateId, java.io.Serializable {
    
    /** Returns the string representation of the underlying UUID */
    override fun toString(): String = value.toString()
    
    // Value classes automatically provide equals and hashCode based on the underlying value
    
    companion object {
        /** Creates a new random ID for the specified type */
        fun <T> random(): TypedId<T> = TypedId(UUID.randomUUID())
        
        /** Creates an ID from a string representation of a UUID */
        fun <T> fromString(uuid: String): TypedId<T> = TypedId(UUID.fromString(uuid))
        
        /** Creates an ID from an existing UUID */
        fun <T> fromUuid(uuid: UUID): TypedId<T> = TypedId(uuid)
    }
}

/**
 * A serializer for [TypedId] that handles conversion to/from string representation.
 * This single serializer works for all [TypedId] instances regardless of their type parameter.
 */
object TypedIdSerializer : KSerializer<TypedId<*>> {
    override val descriptor: SerialDescriptor = 
        PrimitiveSerialDescriptor("TypedId", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: TypedId<*>) {
        encoder.encodeString(value.value.toString())
    }
    
    override fun deserialize(decoder: Decoder): TypedId<*> {
        return TypedId<Any>(UUID.fromString(decoder.decodeString()))
    }
}

/**
 * Creates a type alias for a specific entity's ID type.
 * Usage: `typealias AccountId = TypedId<Account>`
 */
typealias EntityId<T> = TypedId<T>
