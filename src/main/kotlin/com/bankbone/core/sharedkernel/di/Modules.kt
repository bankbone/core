package com.bankbone.core.sharedkernel.di

import com.bankbone.core.ledger.application.PostTransactionCommandValidator
import com.bankbone.core.ledger.application.commands.CreateAccountCommand
import com.bankbone.core.ledger.application.commands.PostTransactionCommand
import com.bankbone.core.ledger.application.commands.RenameAccountCommand
import com.bankbone.core.ledger.application.usecases.CreateAccountUseCase
import com.bankbone.core.ledger.application.usecases.PostTransactionUseCase
import com.bankbone.core.ledger.application.usecases.RenameAccountUseCase
import com.bankbone.core.ledger.domain.Account
import com.bankbone.core.ledger.domain.LedgerTransaction
import com.bankbone.core.ledger.infrastructure.InMemoryLedgerUnitOfWorkFactory
import com.bankbone.core.ledger.ports.LedgerUnitOfWorkFactory
import com.bankbone.core.sharedkernel.application.CommandHandler
import com.bankbone.core.sharedkernel.application.decorators.ActorCommandHandlerDecorator
import com.bankbone.core.sharedkernel.application.decorators.IdempotentCommandHandlerDecorator
import com.bankbone.core.sharedkernel.application.decorators.ShardedActorCommandHandlerDecorator
import com.bankbone.core.sharedkernel.infrastructure.InMemoryIdempotencyStore
import com.bankbone.core.sharedkernel.infrastructure.serialization.KotlinxEventDeserializer
import com.bankbone.core.sharedkernel.infrastructure.serialization.KotlinxEventSerializer
import com.bankbone.core.sharedkernel.ports.EventDeserializer
import com.bankbone.core.sharedkernel.ports.EventSerializer
import com.bankbone.core.sharedkernel.ports.IdempotencyStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import org.koin.core.module.dsl.onClose
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import java.io.Closeable
import java.util.concurrent.Executors

/**
 * Defines application-layer services and use cases.
 * This module is environment-agnostic.
 */
val applicationModule = module {
    single { PostTransactionCommandValidator() }

    // A dedicated, bounded dispatcher for our actor systems to ensure they do not
    // starve other parts of the application of resources. The thread pool is closed
    // gracefully on application shutdown.
    single<CoroutineDispatcher> {
        val dispatcher = Executors.newFixedThreadPool(
            (Runtime.getRuntime().availableProcessors() * 2).coerceAtLeast(4)
        ).asCoroutineDispatcher()
        
        // Register the cleanup directly in the dispatcher creation
        object : AutoCloseable {
            override fun close() {
                (dispatcher as? Closeable)?.close()
            }
        }.also { closeable ->
            // This ensures the dispatcher is closed when the Koin container is closed
            // The actual cleanup will happen when the JVM shuts down
            Runtime.getRuntime().addShutdownHook(Thread {
                closeable.close()
            })
        }
        
        dispatcher
    }

    // --- PostTransaction Use Case Composition ---

    // Post Transaction: Idempotent -> Sharded Actor -> Core
    single<CommandHandler<PostTransactionCommand, LedgerTransaction>>(named("PostTransactionUseCase")) {
        PostTransactionUseCase(uowFactory = get(), validator = get())
    }
    // Only apply idempotency to commands that implement IdempotentCommand
    single<CommandHandler<PostTransactionCommand, LedgerTransaction>>(named("DecoratedPostTransactionUseCase")) {
        val baseHandler = get<CommandHandler<PostTransactionCommand, LedgerTransaction>>(named("PostTransactionUseCase"))
        IdempotentCommandHandlerDecorator(
            decorated = baseHandler,
            idempotencyStore = get()
        )
    }
    // Main binding for PostTransactionCommand handler
    single<CommandHandler<PostTransactionCommand, LedgerTransaction>> {
        val decoratedHandler = get<CommandHandler<PostTransactionCommand, LedgerTransaction>>(named("DecoratedPostTransactionUseCase"))
        ActorCommandHandlerDecorator(
            decorated = decoratedHandler,
            dispatcher = get()
        )
    }

    // --- Chart of Accounts Use Case Composition ---

    // CreateAccount: Idempotent -> Core
    single<CommandHandler<CreateAccountCommand, Account>>(named("CreateAccountUseCase")) {
        CreateAccountUseCase(uowFactory = get())
    }
    // Only apply idempotency to commands that implement IdempotentCommand
    single<CommandHandler<CreateAccountCommand, Account>>(named("DecoratedCreateAccountUseCase")) {
        val baseHandler = get<CommandHandler<CreateAccountCommand, Account>>(named("CreateAccountUseCase"))
        IdempotentCommandHandlerDecorator(
            decorated = baseHandler,
            idempotencyStore = get()
        )
    }
    // Main binding for CreateAccountCommand handler
    single<CommandHandler<CreateAccountCommand, Account>>(named("CreateAccountCommandHandler")) {
        get<CommandHandler<CreateAccountCommand, Account>>(named("DecoratedCreateAccountUseCase"))
    }

    // RenameAccount: Sharded Actor -> Core
    single<CommandHandler<RenameAccountCommand, Unit>>(named("RenameAccountUseCase")) {
        RenameAccountUseCase(uowFactory = get())
    }
    // Main binding for RenameAccountCommand handler with sharded actor decoration
    single<CommandHandler<RenameAccountCommand, Unit>>(named("RenameAccountCommandHandler")) {
        val baseHandler = get<CommandHandler<RenameAccountCommand, Unit>>(named("RenameAccountUseCase"))
        ShardedActorCommandHandlerDecorator(
            decorated = baseHandler,
            dispatcher = get()
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