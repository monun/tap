/*
 * Copyright 2021 Monun
 *
 * Licensed under the General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://opensource.org/licenses/gpl-3.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.monun.tap.fake.internal

import io.github.monun.tap.fake.*
import com.google.common.collect.ImmutableList
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import kotlin.collections.ArrayList

class FakeEntityServerImpl(plugin: JavaPlugin) : FakeEntityServer {

    internal val _entities = ArrayList<FakeEntityImpl>()
    private val updateQueue = ArrayDeque<FakeEntityImpl>()

    override val entities: List<FakeEntity>
        get() = ImmutableList.copyOf(_entities)

    private val trackersByPlayer = WeakHashMap<Player, FakeTracker>()

    internal val trackers: Collection<FakeTracker>
        get() = trackersByPlayer.values

    private val listener = FakeListener()

    private var isRunning = true

    init {
        Bukkit.getPluginManager().registerEvents(listener, plugin)
    }

    override fun spawnEntity(location: Location, clazz: Class<out Entity>): FakeEntity {
        checkState()

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

    override fun spawnFallingBlock(location: Location, blockData: BlockData): FakeEntity {
        val bukkitFallingBlock = createFallingBlock(blockData).apply {
            setLocation(location)
        }
        val fakeEntity = FakeEntityImpl(this, bukkitFallingBlock, location)
        _entities += fakeEntity
        enqueue(fakeEntity)

        return fakeEntity
    }

    override fun dropItem(location: Location, item: ItemStack): FakeEntity {
        val bukkitItemEntity = createItemEntity(item).apply {
            setLocation(location)
        }
        val fakeEntity = FakeEntityImpl(this, bukkitItemEntity, location)
        _entities += fakeEntity
        enqueue(fakeEntity)

        return fakeEntity
    }

    override fun addPlayer(player: Player) {
        checkState()

        trackersByPlayer.computeIfAbsent(player) {
            FakeTracker(this, it).apply { broadcastSelf() }
        }
    }

    override fun removePlayer(player: Player) {
        trackersByPlayer.remove(player)?.destroy()
    }

    override fun update() {
        checkState()

        updateTrackers()
        updateEntities()
    }

    private fun updateTrackers() {
        trackersByPlayer.entries.iterator().let { iterator ->
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val tracker = entry.value

                tracker.update()
            }
        }
    }

    private fun updateEntities() {
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

    fun checkState() {
        require(isRunning) { "Invalid ${this.javaClass.simpleName}@${System.identityHashCode(this).toString(0x10)}" }
    }

    override fun shutdown() {
        if (!isRunning) return

        isRunning = false
        clear()
        HandlerList.unregisterAll(this.listener)
    }

    internal fun enqueue(entity: FakeEntityImpl) {
        updateQueue.offer(entity)
    }

    inner class FakeListener : Listener {
        @EventHandler
        fun onPlayerDeath(event: PlayerDeathEvent) {
            trackersByPlayer[event.entity]?.clear()
        }

        @EventHandler
        fun onPlayerQuit(event: PlayerQuitEvent) {
            trackersByPlayer.remove(event.player)?.clear()
        }
    }
}

