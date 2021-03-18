/*
 * Copyright (c) 2020 Noonmaru
 *
 *  Licensed under the General Public License, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-3.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 */

package com.github.monun.tap.task

import com.github.monun.tap.ref.UpstreamReference

class TickerTask internal constructor(
    scheduler: Ticker,
    val runnable: Runnable
) : Comparable<TickerTask> {
    companion object {
        internal const val ERROR = 0L
        internal const val NO_REPEATING = -1L
        internal const val CANCEL = -2L
        internal const val DONE = -3L
    }

    private val schedulerRef = UpstreamReference(scheduler)

    val scheduler: Ticker
        get() = schedulerRef.get()

    internal var nextRun: Long = -1L

    var period: Long = 0L
        internal set

    val isScheduled: Boolean
        get() = period.let { it != ERROR && it > CANCEL }

    val isCancelled
        get() = period == CANCEL

    val isDone
        get() = period == DONE

    internal fun execute() {
        runnable.runCatching { run() }
    }

    fun cancel() {
        if (!isScheduled) return

        period = CANCEL

        // 실행까지 남은 틱이 255 초과면 대기열에서 즉시 제거
        // 아닐경우 자연스럽게 제거
        if (nextRun - scheduler.currentTicks > 0xFF)
            scheduler.remove(this)
    }

    override fun compareTo(other: TickerTask): Int {
        return this.nextRun.compareTo(other.nextRun)
    }
}

