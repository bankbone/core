package com.bankbone.core.sharedkernel.application.decorators

import com.bankbone.core.sharedkernel.application.CommandHandler
import com.bankbone.core.sharedkernel.application.ShardedCommand
import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShardedActorCommandHandlerDecoratorTest {

    // A command that carries a shard key
    data class FakeShardedCommand(override val shardKey: String) : ShardedCommand

    // A fake handler that introduces a delay and records the execution start and end times.
    class FakeDelayedCommandHandler : CommandHandler<FakeShardedCommand, Long> {
        val executionTimestamps = CopyOnWriteArrayList<Pair<Long, Long>>()

        override suspend fun handle(command: FakeShardedCommand): Long {
            val startTime = System.nanoTime()
            delay(50) // Simulate work
            val endTime = System.nanoTime()
            executionTimestamps.add(Pair(startTime, endTime))
            return endTime
        }
    }

    @Test
    fun `should process commands for the same shard key sequentially`() {
        runBlocking {
            val dispatcher = Executors.newFixedThreadPool(4).asCoroutineDispatcher()
            val decoratedHandler = FakeDelayedCommandHandler()
            val decorator = ShardedActorCommandHandlerDecorator(decoratedHandler, dispatcher)

            val shardKey = "account-123"
            val commands = List(4) { FakeShardedCommand(shardKey) }

            // Launch all commands concurrently
            val jobs = commands.map { command ->
                async(Dispatchers.Default) {
                    decorator.handle(command)
                }
            }
            jobs.awaitAll()

            // --- Verification ---
            assertEquals(4, decoratedHandler.executionTimestamps.size, "All commands should have been processed.")

            // Sort the timestamps by their start time
            val sortedTimestamps = decoratedHandler.executionTimestamps.sortedBy { it.first }

            // Check that each command's start time is after the previous one's end time.
            // This proves they did not overlap and were executed sequentially.
            for (i in 0 until sortedTimestamps.size - 1) {
                val current = sortedTimestamps[i]
                val next = sortedTimestamps[i + 1]
                assertTrue(next.first >= current.second, "Execution of command ${i+1} should start after command $i finishes.")
            }
            dispatcher.close()
        }
    }
}