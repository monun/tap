/*
 * Copyright (c) $date.year Noonmaru
 *
 *  Licensed under the General Public License, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.noonmaru.tap.nfake.internal

import com.github.noonmaru.tap.nfake.FakeChunk
import com.github.noonmaru.tap.nfake.FakeEntity
import com.github.noonmaru.tap.nfake.FakeWorld
import com.github.noonmaru.tap.ref.UpstreamReference

class FakeChunkImpl(
    world: FakeWorld,
    override val x: Int,
    override val z: Int
) : FakeChunk {
    private val worldRef = UpstreamReference(world)

    override val world: FakeWorld
        get() = worldRef.get()
    override val entities: List<FakeEntity>
        get() = TODO("Not yet implemented")
    override val valid: Boolean = true

    private val _entities = ArrayList<FakeEntityImpl>()
    private val sections: Array<out FakeSection>

    init {
        sections = Array(16) { FakeSection() }
    }

    internal fun getSectionAt(y: Int): FakeSection {
        return sections[y]
    }
}

internal class FakeSection {
    val trackers = ArrayList<FakeTracker>()
    val entities = ArrayList<FakeEntityImpl>()
}