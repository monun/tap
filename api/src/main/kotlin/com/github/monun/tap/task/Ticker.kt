/*
 * Copyright 2021 Monun
 *
 * Licensed under the Apache License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.monun.tap.task

import java.util.*
import kotlin.math.max

/**
 * Tick 기반의 태스크 스케쥴러 (Tick + Timer = Ticker)
 *
 * @see TickerTask
 */
abstract class Ticker : Runnable {
    companion object {
        /**
         * 호출 횟수에 따라 tick이 증가하는 Ticker를 생성합니다.
         */
        fun plank(): Ticker {
            return Plank()
        }

        /**
         * 시간에 따라 tick이 변화하는 Ticker를 생성합니다.
         *
         * 태스크는 시간 지연 관계없이 tick과 동기화되어 실행됩니다.
         */
        fun precision(tick: Pair<() -> Long, Long> = System::nanoTime to 50L * 1000L * 1000L): Ticker {
            return Precision(tick.first, tick.second)
        }

        /**
         * 시간에 따라 tick이 변화하는 Ticker를 생성합니다.
         *
         * 태스크는 시간 지연에 따라 한번만 호출됩니다.
         */
        fun flex(tick: Pair<() -> Long, Long> = System::nanoTime to 50L * 1000L * 1000L): Ticker {
            return Flex(tick.first, tick.second)
        }
    }

    /**
     * Ticker 내부에서 사용하는 tick입니다.
     */
    abstract val currentTicks: Long

    protected val queue = PriorityQueue<TickerTask>()

    var removeDelay = 0xFF

    protected open fun TickerTask.calculateNextRun() = nextRun + period

    /**
     * 지연 실행할 태스크를 등록합니다.
     *
     * 태스크는 한번만 호출된 이후 [TickerTask.isDone] 상태가 됩니다.
     */
    fun runTask(runnable: Runnable, delay: Long = 0L) = registerTask(runnable, delay, TickerTask.NO_REPEATING)

    /**
     * 반복 실행할 태스크를 등록합니다.
     *
     * 태스크는 지연 이후에 period tick 마다 호출됩니다.
     */
    fun runTaskTimer(runnable: Runnable, delay: Long, period: Long) = registerTask(runnable, delay, max(1L, period))

    private fun registerTask(runnable: Runnable, delay: Long, period: Long) = TickerTask(this, runnable).apply {
        this.nextRun = currentTicks + max(0L, delay)
        this.period = period
    }.also(queue::offer)

    /**
     * 모든 태스크를 실행합니다.
     */
    override fun run() {
        val queue = queue
        while (queue.peek().let { it != null && it.nextRun <= currentTicks }) {
            val task = queue.remove()

            if (task.isScheduled) task.run {
                execute()

                if (period > 0L) {
                    nextRun = task.calculateNextRun()
                    queue.offer(task)
                } else {
                    period = TickerTask.DONE
                }
            }
        }
    }

    internal fun remove(task: TickerTask) {
        queue.remove(task)
    }

    private class Plank : Ticker() {
        override var currentTicks = 0L

        override fun run() {
            super.run()
            currentTicks++
        }
    }

    private class Precision(
        private val time: () -> Long,
        private val tickTime: Long
    ) : Ticker() {
        private val currentTicksByTime = time()
            get() = (time() - field) / tickTime

        override var currentTicks = 0L

        override fun run() {
            super.run()

            while (currentTicks < currentTicksByTime) {
                if (queue.isEmpty()) {
                    currentTicks = currentTicksByTime
                    return
                }
                currentTicks++
                super.run()
            }
        }
    }

    private class Flex(
        private val time: () -> Long,
        private val tickTime: Long
    ) : Ticker() {
        override val currentTicks = time()
            get() = (time() - field) / tickTime

        override fun TickerTask.calculateNextRun() = currentTicks + period
    }
}