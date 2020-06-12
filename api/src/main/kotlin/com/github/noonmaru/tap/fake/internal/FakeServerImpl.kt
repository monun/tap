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

package com.github.noonmaru.tap.fake.internal

import com.github.noonmaru.tap.fake.FakeEntity
import com.github.noonmaru.tap.fake.FakeServer
import com.github.noonmaru.tap.fake.createFakeEntity
import com.github.noonmaru.tap.fake.setLocation
import com.google.common.collect.ImmutableList
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.ArrayList

class FakeServerImpl
    : FakeServer {

    private val trackersByPlayer = WeakHashMap<Player, FakeTracker>()
    internal val _entities = ArrayList<FakeEntityImpl>()
    private val updateQueue = ArrayDeque<FakeEntityImpl>()

    override val entities: List<FakeEntity>
        get() = ImmutableList.copyOf(_entities)

    internal val trackers: Collection<FakeTracker>
        get() = trackersByPlayer.values

    override fun spawnEntity(location: Location, clazz: Class<out Entity>): FakeEntity {
        val bukkitWorld = location.world
        val bukkitEntity = requireNotNull(clazz.createFakeEntity(bukkitWorld)) {
            "Cannot create entity $clazz"
        }.apply {
            setLocation(location)
        }

        val fakeEntity = FakeEntityImpl(this, bukkitEntity, location)
        _entities += fakeEntity
        enqueue(fakeEntity)

        return fakeEntity
    }

    override fun addPlayer(player: Player) {
        trackersByPlayer.computeIfAbsent(player) {
            FakeTracker(this, it).apply { broadcastSelf() }
        }
    }

    override fun removePlayer(player: Player) {
        trackersByPlayer.remove(player)?.destroy()
    }

    override fun update() {
        trackersByPlayer.entries.iterator().let { iterator ->
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val tracker = entry.value

                tracker.update()

                if (!tracker.valid) {
                    iterator.remove()
                }
            }
        }

        val updateQueue = updateQueue
        var nextTickEntity: FakeEntityImpl? = null
        while (updateQueue.isNotEmpty()) {
            val entity = updateQueue.peek()

            if (entity === nextTickEntity) {
                break
            }

            updateQueue.remove()

            entity.update()

            if (!entity.valid) {
                _entities.remove(entity)
            } else {
                if (!entity.isDone) {
                    if (nextTickEntity == null)
                        nextTickEntity = entity

                    entity.enqueue()
                }
            }
        }
    }

    override fun clear() {
        updateQueue.clear()
        trackersByPlayer.run {
            for (tracker in values) {
                tracker.clearEntities()
            }
            clear()
        }
        _entities.apply {
            for (entity in this) {
                entity.despawn()
                entity.clearTrackers()
            }
            clear()
        }
    }

    internal fun enqueue(entity: FakeEntityImpl) {
        updateQueue.offer(entity)
    }
}