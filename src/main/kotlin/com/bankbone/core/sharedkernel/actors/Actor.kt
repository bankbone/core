package com.bankbone.core.sharedkernel.actors

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

@OptIn(kotlinx.coroutines.ObsoleteCoroutinesApi::class)

/**
 * A simple actor implementation that processes messages sequentially.
 *
 * @param T The type of messages this actor can process
 * @param R The type of results this actor can return
 * @param scope The coroutine scope in which this actor will run
 * @param context Additional coroutine context for the actor
 * @param capacity The capacity of the actor's mailbox (default: 1000)
 * @param handler The handler function that processes messages
 */
class Actor<T : Any, R>(
    private val scope: CoroutineScope,
    private val context: CoroutineContext = Dispatchers.Default,
    private val capacity: Int = 1000,
    private val handler: suspend (T) -> R
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val mutex = Mutex()
    
    private val channel = scope.actor<T>(context, capacity = capacity) {
        for (message in channel) {
            try {
                mutex.withLock {
                    handler(message)
                }
            } catch (e: Exception) {
                logger.error("Error processing message in actor: ${e.message}", e)
                throw e
            }
        }
    }

    /**
     * Sends a message to the actor and returns a Deferred result.
     */
    suspend fun ask(message: T): R = coroutineScope {
        mutex.withLock {
            handler(message)
        }
    }

    /**
     * Sends a message to the actor without waiting for a result.
     */
    fun tell(message: T) {
        scope.launch {
            try {
                channel.send(message)
            } catch (e: Exception) {
                logger.error("Failed to send message to actor: ${e.message}", e)
            }
        }
    }

    /**
     * Closes the actor's channel, stopping it from receiving new messages.
     */
    fun close(cause: Throwable? = null) {
        channel.close(cause)
    }
}

/**
 * Creates a new actor with the given handler function.
 */
fun <T : Any, R> CoroutineScope.actor(
    context: CoroutineContext = Dispatchers.Default,
    capacity: Int = 1000,
    handler: suspend (T) -> R
): Actor<T, R> = Actor(this, context, capacity, handler)
