package com.bankbone.core.sharedkernel.infrastructure.serialization

import com.bankbone.core.ledger.domain.events.LedgerTransactionPosted
import com.bankbone.core.sharedkernel.domain.DomainEvent
import com.bankbone.core.sharedkernel.domain.ids.TypedId
import com.bankbone.core.sharedkernel.domain.ids.TypedIdSerializer
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Configuration for JSON serialization/deserialization
 */
object JsonConfig {
    val eventJson = Json {
        // Configure JSON serialization with existing serializers
        serializersModule = SerializersModule {
            // Register custom serializers
            contextual(BigDecimal::class, BigDecimalSerializer)
            contextual(Instant::class, InstantSerializer)
            contextual(UUID::class, UUIDSerializer)
            
            // Register TypedId serializer for all ID types
            contextual(TypedId::class, TypedIdSerializer)
            
            // Register all domain event subtypes
            polymorphic(DomainEvent::class) {
                subclass(LedgerTransactionPosted::class)
            }
        }
        
        // Additional JSON configuration
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }
}