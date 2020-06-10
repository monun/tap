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
import com.github.noonmaru.tap.nfake.FakeServer
import com.github.noonmaru.tap.nfake.FakeWorld
import com.github.noonmaru.tap.ref.UpstreamReference
import com.google.common.collect.ImmutableList
import org.bukkit.World
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector

class FakeWorldImpl(
    server: FakeServer,
    bukkitWorld: World
) : FakeWorld {
    private val serverRef = UpstreamReference(server)
    private val bukkitWorldRef = UpstreamReference(bukkitWorld)
    private val _entities = ArrayList<FakeEntity>()
    private val chunksByHash = Long2ObjectOpenHashMap<>()

    override val server: FakeServer
        get() = serverRef.get()

    override val bukkitWorld: World
        get() = bukkitWorldRef.get()

    override val entities: List<FakeEntity>
        get() = ImmutableList.copyOf(_entities)

    override fun getChunkAt(x: Int, z: Int): FakeChunk? {
        TODO("Not yet implemented")
    }

    override fun rayTrace(start: Vector, direction: Vector, maxDistance: Double) {
        TODO("Not yet implemented")
    }

    override fun getNearbyEntities(box: BoundingBox, filter: ((FakeEntity) -> Boolean)?) {
        TODO("Not yet implemented")
    }
}