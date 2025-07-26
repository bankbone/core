package com.bankbone.core.sharedkernel.di

import com.bankbone.core.ledger.application.ChartOfAccountsService
import com.bankbone.core.ledger.application.LedgerPostingService
import com.bankbone.core.ledger.application.PostTransactionCommandValidator
import com.bankbone.core.ledger.infrastructure.InMemoryLedgerUnitOfWorkFactory
import com.bankbone.core.ledger.ports.LedgerUnitOfWorkFactory
import com.bankbone.core.sharedkernel.infrastructure.InMemoryIdempotencyStore
import com.bankbone.core.sharedkernel.infrastructure.serialization.KotlinxEventDeserializer
import com.bankbone.core.sharedkernel.infrastructure.serialization.KotlinxEventSerializer
import com.bankbone.core.sharedkernel.ports.EventDeserializer
import com.bankbone.core.sharedkernel.ports.EventSerializer
import com.bankbone.core.sharedkernel.ports.IdempotencyStore
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Defines application-layer services and use cases.
 * This module is environment-agnostic.
 */
val applicationModule = module {
    single { PostTransactionCommandValidator() }
    single {
        LedgerPostingService(
            uowFactory = get(),
            validator = get(),
            idempotencyStore = get()
        )
    }
    single {
        ChartOfAccountsService(
            uowFactory = get(),
            idempotencyStore = get()
        )
    }
}

/**
 * Defines shared kernel services, like serialization.
 * This module is environment-agnostic.
 */
val sharedKernelModule = module {
    single<EventSerializer> { KotlinxEventSerializer() }
    single<EventDeserializer> { KotlinxEventDeserializer() }
}

/**
 * Defines infrastructure bindings for a PRODUCTION environment.
 * This is where you would bind to a real database, message queue, etc.
 */
val productionPersistenceModule = module {
    // For a real app, this would be an Exposed, JDBC, or other persistent implementation.
    // single<LedgerUnitOfWorkFactory> { ExposedLedgerUnitOfWorkFactory(get()) }
    // For now, we'll keep using the in-memory version as a placeholder.
    single<LedgerUnitOfWorkFactory> { InMemoryLedgerUnitOfWorkFactory() }
    single<IdempotencyStore> { InMemoryIdempotencyStore() } // Replace with Redis/DB implementation
}

/**
 * Defines infrastructure bindings for a TESTING environment.
 * This module provides in-memory or fake implementations for fast and isolated tests.
 */
val testingPersistenceModule = module {
    // Define the concrete InMemoryLedgerUnitOfWorkFactory as a singleton and
    // also bind it to the LedgerUnitOfWorkFactory interface. This allows tests
    // to inject the concrete class while the application uses the interface.
    single { InMemoryLedgerUnitOfWorkFactory() } bind LedgerUnitOfWorkFactory::class
    single<IdempotencyStore> { InMemoryIdempotencyStore() }
}