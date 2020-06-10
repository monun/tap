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

import com.github.noonmaru.tap.hash.pair
import com.github.noonmaru.tap.math.toSection
import com.github.noonmaru.tap.nfake.FakeChunk
import com.github.noonmaru.tap.nfake.FakeEntity
import com.github.noonmaru.tap.nfake.FakeWorld
import com.github.noonmaru.tap.ref.UpstreamReference
import com.google.common.collect.ImmutableList
import it.unimi.dsi.fastutil.longs.Long2LongLinkedOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import org.bukkit.Location
import org.bukkit.World

class FakeWorldImpl(
    server: FakeServerImpl,
    bukkitWorld: World
) : FakeWorld {
    private val serverRef = UpstreamReference(server)
    private val bukkitWorldRef = UpstreamReference(bukkitWorld)
    private val _entities = ArrayList<FakeEntity>()
    private val chunksByHash = Long2ObjectOpenHashMap<FakeChunk>()
    private val unloadQueue = Long2LongLinkedOpenHashMap()

    override val server: FakeServerImpl
        get() = serverRef.get()

    override val bukkitWorld: World
        get() = bukkitWorldRef.get()

    override val entities: List<FakeEntity>
        get() = ImmutableList.copyOf(_entities)

    override val chunks: List<FakeChunk>
        get() = ImmutableList.copyOf(chunksByHash.values)

    override fun getChunkAt(x: Int, z: Int): FakeChunkImpl? {
        return chunksByHash[x pair z]
    }

    internal fun addEntity(entity: FakeEntity) {
        TODO()
    }

    internal fun removeEntity(entity: FakeEntity) {
        TODO()
    }

    internal fun findNearbyEntities(center: Location, distance: Double, action: (FakeEntityImpl) -> Unit) {
        val distanceSquared = distance * distance
        val centerX = center.x
        val centerY = center.y
        val centerZ = center.z
        val minX = (centerX - distance).toSection()
        val minY = (centerY - distance).toSection().coerceIn(0, 15)
        val minZ = (centerZ - distance).toSection()
        val maxX = (centerX + distance).toSection()
        val maxY = (centerY + distance).toSection().coerceIn(minY, 15)
        val maxZ = (centerZ + distance).toSection()

        for (chunkX in minX..maxX) {
            for (chunkZ in minZ..maxZ) {
                getChunkAt(chunkX, chunkZ)?.let { chunk ->
                    for (sectionY in minY..maxY) {
                        for (entity in chunk.getSectionAt(sectionY).entities) {
                            if (center.distanceSquared(entity.currentLocation) < distanceSquared) {
                                action(entity)
                            }
                        }
                    }
                }
            }
        }
    }

    internal fun findNearbyEntities(center: Location, distance: Double, action: (FakeEntityImpl) -> Unit) {
        val distanceSquared = distance * distance
        val centerX = center.x
        val centerY = center.y
        val centerZ = center.z
        val minX = (centerX - distance).toSection()
        val minY = (centerY - distance).toSection().coerceIn(0, 15)
        val minZ = (centerZ - distance).toSection()
        val maxX = (centerX + distance).toSection()
        val maxY = (centerY + distance).toSection().coerceIn(minY, 15)
        val maxZ = (centerZ + distance).toSection()

        for (chunkX in minX..maxX) {
            for (chunkZ in minZ..maxZ) {
                getChunkAt(chunkX, chunkZ)?.let { chunk ->
                    for (sectionY in minY..maxY) {
                        for (entity in chunk.getSectionAt(sectionY).entities) {
                            if (center.distanceSquared(entity.currentLocation) < distanceSquared) {
                                action(entity)
                            }
                        }
                    }
                }
            }
        }
    }
}