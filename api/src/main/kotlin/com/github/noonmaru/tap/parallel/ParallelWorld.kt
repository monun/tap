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

package com.github.noonmaru.tap.parallel

import com.github.noonmaru.tap.hash.pair
import com.github.noonmaru.tap.math.toSection
import it.unimi.dsi.fastutil.longs.Long2LongLinkedOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import org.bukkit.util.NumberConversions.floor

class ParallelWorld {

    private companion object {
        private val initTime = System.nanoTime()

        internal fun currentTime(): Long {
            return (System.nanoTime() - initTime) / 1000000L
        }
    }

    private val chunksByHash = Long2ObjectOpenHashMap<ParallelChunk>()

    private val unloadQueue = Long2LongLinkedOpenHashMap()

    private val entities = ArrayList<ParallelEntity>()

    private fun getChunkAt(x: Int, z: Int): ParallelChunk? {
        return chunksByHash[x pair z]
    }

    //청크 리프레시
    private fun getOrCreateChunkAt(x: Int, z: Int): ParallelChunk {
        val hash = x pair z

        unloadQueue.remove(hash)

        return chunksByHash.computeIfAbsent(hash) {
            ParallelChunk(this@ParallelWorld, x, z)
        }
    }

    private fun unload(chunk: ParallelChunk) {
        unloadQueue.putAndMoveToLast(chunk.x pair chunk.z, currentTime() + 1000L * 60L * 5L)
    }

    private fun removeEntityAt(x: Int, z: Int, entity: ParallelEntity) {
        getChunkAt(x, z)?.let { chunk ->
            val sectionY = floor(entity.y) shr 4
            chunk.removeEntity(entity, sectionY)

            if (chunk.isEmpty) {
                unload(chunk)
            }
        }
    }

    internal fun moveEntity(entity: ParallelEntity, x: Double, y: Double, z: Double) {
        val oldChunkX = entity.x.toSection()
        val oldChunkZ = entity.z.toSection()

        val newChunkX = x.toSection()
        val newChunkZ = z.toSection()

        val oldSectionY = entity.y.toSection()
        val newSectionY = y.toSection()

        if (oldChunkX == newChunkX && oldChunkZ == newChunkZ) { // 청크내 이동
            if (oldSectionY != newSectionY) { // 섹션내 이동
                getChunkAt(newChunkX, newChunkZ)?.moveEntity(entity, oldSectionY, newSectionY)
            }
        } else { // 다른 청크로 이동
            removeEntityAt(oldChunkX, oldChunkZ, entity)
            getOrCreateChunkAt(newChunkX, newChunkZ).addEntity(entity, newSectionY)
        }
    }

    internal fun removeEntity(entity: ParallelEntity) {
        entities.remove(entity)

        val chunkX = floor(entity.x) shr 4
        val chunkZ = floor(entity.z) shr 4

        removeEntityAt(chunkX, chunkZ, entity)
    }

    internal fun addEntity(entity: ParallelEntity, x: Double, y: Double, z: Double) {
        entities.add(entity)

        val chunkX = floor(x) shr 4
        val chunkZ = floor(z) shr 4
        val chunk = getOrCreateChunkAt(chunkX, chunkZ)

        val sectionY = floor(y) shr 4

        chunk.addEntity(entity, sectionY)
    }

    internal fun unloadChunks() {
        val queue = unloadQueue
        val time = currentTime()

        while (queue.isNotEmpty()) {
            val chunkHash = queue.firstLongKey()
            val freeTime = queue[chunkHash]

            if (freeTime <= time) {
                queue.removeFirstLong()
                chunksByHash.remove(chunkHash)
            }
        }
    }
}

