package com.bankbone.core.sharedkernel.actors

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

/**
 * A system that manages a set of actors, each responsible for handling messages for a specific shard key.
 * This ensures that messages with the same shard key are processed sequentially.
 *
 * @param K The type of the shard key
 * @param T The type of messages this system can process
 * @param R The type of results this system can return
 * @param scope The coroutine scope in which the actors will run
 * @param context Additional coroutine context for the actors
 * @param capacity The capacity of each actor's mailbox (default: 1000)
 * @param handler The handler function that processes messages
 */
class ShardedActorSystem<K : Any, T : Any, R>(
    private val scope: CoroutineScope,
    private val context: CoroutineContext = Dispatchers.Default,
    private val capacity: Int = 1000,
    private val handler: suspend (T) -> R
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val mutex = Mutex()
    private val actors = mutableMapOf<K, Actor<T, R>>()

    /**
     * Gets or creates an actor for the given shard key.
     */
    suspend fun get(shardKey: K): Actor<T, R> = mutex.withLock {
        actors.getOrPut(shardKey) {
            val newActor = Actor<T, R>(
                scope = scope,
                context = context,
                capacity = capacity,
                handler = handler
            )
            
            // Set up a completion handler to clean up the actor when it fails
            scope.launch {
                try {
                    // The actor doesn't have a direct join method, so we'll rely on the channel being closed
                    // as an indication that the actor has completed
                    (newActor as? AutoCloseable)?.close()
                } catch (e: Exception) {
                    logger.error("Actor for shard $shardKey failed: ${e.message}", e)
                } finally {
                    mutex.withLock {
                        actors.remove(shardKey)
                    }
                }
            }
            
            newActor
        }
    }

    /**
     * Sends a message to the appropriate actor based on the shard key.
     * 
     * @param shardKey The key that determines which actor processes the message
     * @param message The message to send
     * @return A Deferred result that will be completed when the message is processed
     */
    suspend fun ask(shardKey: K, message: T): R = coroutineScope {
        // Use the same actor for the same shard key to ensure sequential processing
        val actor = get(shardKey)
        // Use mutex to ensure we don't process multiple messages for the same shard concurrently
        mutex.withLock {
            actor.ask(message)
        }
    }

    /**
     * Sends a message to the appropriate actor without waiting for a result.
     * 
     * @param shardKey The key that determines which actor processes the message
     * @param message The message to send
     */
    fun tell(shardKey: K, message: T) {
        scope.launch {
            try {
                get(shardKey).tell(message)
            } catch (e: Exception) {
                logger.error("Failed to send message to shard $shardKey: ${e.message}", e)
            }
        }
    }

    /**
     * Closes all actors in the system.
     * 
     * @param cause Optional exception that caused the shutdown
     */
    suspend fun close(cause: Throwable? = null) {
        mutex.withLock {
            actors.values.forEach { it.close(cause) }
            actors.clear()
        }
    }

    /**
     * Returns the number of active shards.
     */
    suspend fun shardCount(): Int = mutex.withLock { actors.size }
}
