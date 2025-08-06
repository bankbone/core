package com.bankbone.core.sharedkernel.application.decorators

import com.bankbone.core.sharedkernel.application.CommandHandler
import com.bankbone.core.sharedkernel.application.IdempotentCommand
import com.bankbone.core.sharedkernel.domain.IdempotencyKey
import com.bankbone.core.sharedkernel.infrastructure.InMemoryIdempotencyStore
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals

class IdempotentCommandHandlerDecoratorTest {

    private lateinit var decoratedHandler: FakeCommandHandler
    private lateinit var idempotencyStore: InMemoryIdempotencyStore
    private lateinit var decorator: IdempotentCommandHandlerDecorator<FakeIdempotentCommand, String>

    // A simple command for testing purposes
    data class FakeIdempotentCommand(override val idempotencyKey: IdempotencyKey) : IdempotentCommand

    // A fake handler that just counts how many times it has been called
    class FakeCommandHandler : CommandHandler<FakeIdempotentCommand, String> {
        val callCount = AtomicInteger(0)
        override suspend fun handle(command: FakeIdempotentCommand): String {
            callCount.incrementAndGet()
            return "OK"
        }
    }

    @BeforeEach
    fun setUp() {
        decoratedHandler = FakeCommandHandler()
        idempotencyStore = InMemoryIdempotencyStore()
        decorator = IdempotentCommandHandlerDecorator(decoratedHandler, idempotencyStore)
    }

    @Test
    fun `should execute the decorated handler on the first call`() = runBlocking {
        val command = FakeIdempotentCommand(IdempotencyKey())
        decorator.handle(command)
        assertEquals(1, decoratedHandler.callCount.get())
    }

    @Test
    fun `should not execute the decorated handler on subsequent calls with the same key`() = runBlocking {
        val key = IdempotencyKey()
        val command1 = FakeIdempotentCommand(key)
        val command2 = FakeIdempotentCommand(key)

        decorator.handle(command1)
        decorator.handle(command2)

        assertEquals(1, decoratedHandler.callCount.get(), "The decorated handler should only be called once.")
    }
}