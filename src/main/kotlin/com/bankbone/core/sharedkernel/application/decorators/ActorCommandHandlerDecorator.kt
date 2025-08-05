package com.bankbone.core.sharedkernel.application.decorators

import com.bankbone.core.sharedkernel.actors.Actor
import com.bankbone.core.sharedkernel.application.CommandHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

/**
 * A decorator that uses a single actor to ensure that all commands are processed
 * sequentially. This is ideal for use cases that require a global lock.
 *
 * @param C The type of command this handler can process
 * @param R The type of result this handler returns
 * @property decorated The underlying command handler to delegate to
 * @property dispatcher The coroutine dispatcher to use for the actor (default: Dispatchers.Default)
 * @property capacity The maximum number of pending commands (default: 1000)
 */
class ActorCommandHandlerDecorator<C : Any, R>(
    private val decorated: CommandHandler<C, R>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val capacity: Int = 1000
) : CommandHandler<C, R> {
    private val actor = Actor<C, R>(
        scope = CoroutineScope(dispatcher),
        context = dispatcher,
        capacity = capacity,
        handler = { command ->
            decorated.handle(command)
        }
    )

    override suspend fun handle(command: C): R = actor.ask(command)

    /**
     * Closes the underlying actor, stopping it from processing new commands.
     * 
     * @param cause Optional exception that caused the actor to close
     */
    fun close(cause: Throwable? = null) {
        actor.close(cause)
    }
    
    /**
     * Ensures resources are cleaned up when this object is garbage collected.
     */
    @Suppress("OVERRIDE_DEPRECATION")
    protected fun finalize() {
        runBlocking {
            try {
                close()
            } catch (e: Exception) {
                // Ignore errors during finalization
            }
        }
    }
}