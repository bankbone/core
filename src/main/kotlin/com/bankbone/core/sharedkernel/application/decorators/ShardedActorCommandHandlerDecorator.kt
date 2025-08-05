package com.bankbone.core.sharedkernel.application.decorators

import com.bankbone.core.sharedkernel.actors.ShardedActorSystem
import com.bankbone.core.sharedkernel.application.CommandHandler
import com.bankbone.core.sharedkernel.application.ShardedCommand
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * A decorator that uses a sharded actor system to ensure that commands for the same shard key
 * are processed sequentially. This is ideal for per-entity concurrency control.
 *
 * @param C The type of command this handler can process (must implement ShardedCommand)
 * @param R The type of result this handler returns
 * @property decorated The underlying command handler to delegate to
 * @property dispatcher The coroutine dispatcher to use for the actors (default: Dispatchers.Default)
 * @property capacity The maximum number of pending commands per shard (default: 1000)
 */
class ShardedActorCommandHandlerDecorator<C : ShardedCommand, R>(
    private val decorated: CommandHandler<C, R>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val capacity: Int = 1000
) : CommandHandler<C, R> {
    private val actorSystem = ShardedActorSystem<String, C, R>(
        scope = CoroutineScope(dispatcher),
        capacity = capacity
    ) { command ->
        decorated.handle(command)
    }

    override suspend fun handle(command: C): R = actorSystem.ask(command.shardKey, command)

    /**
     * Returns the number of active shards.
     */
    suspend fun shardCount(): Int = actorSystem.shardCount()

    /**
     * Closes the underlying actor system, stopping all actors.
     * 
     * @param cause Optional exception that caused the shutdown
     */
    suspend fun close(cause: Throwable? = null) {
        actorSystem.close(cause)
    }
}