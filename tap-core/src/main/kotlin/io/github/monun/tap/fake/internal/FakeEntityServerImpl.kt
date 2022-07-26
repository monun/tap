/*
 * Tap
 * Copyright (C) 2021 Monun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Modified - octomarine
 */

package io.github.monun.tap.fake.internal

import com.google.common.collect.ImmutableList
import io.github.monun.tap.fake.*
import io.github.monun.tap.protocol.PacketSupport.Companion.entityMetadata
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Entity
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class FakeEntityServerImpl(plugin: JavaPlugin) : FakeEntityServer {

    internal val _entities = ArrayList<FakeEntityImpl<*>>()
    private val updateQueue = ArrayDeque<FakeEntityImpl<*>>()

    override val entities: List<FakeEntity<*>>
        get() = ImmutableList.copyOf(_entities)

    private val trackersByPlayer = WeakHashMap<Player, FakeTracker>()

    internal val trackers: Collection<FakeTracker>
        get() = trackersByPlayer.values

    private val listener = FakeListener()

    private var isRunning = true

    init {
        Bukkit.getPluginManager().registerEvents(listener, plugin)
    }

    override fun <T : Entity> spawnEntity(location: Location, clazz: Class<T>): FakeEntity<T> {
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

    override fun spawnFallingBlock(location: Location, blockData: BlockData): FakeEntity<FallingBlock> {
        val bukkitFallingBlock = createFallingBlock(blockData).apply {
            setLocation(location)
        }
        val fakeEntity = FakeEntityImpl(this, bukkitFallingBlock, location)
        _entities += fakeEntity
        enqueue(fakeEntity)

        return fakeEntity
    }

    override fun spawnItem(location: Location, item: ItemStack): FakeEntity<Item> {
        val bukkitItemEntity = createItemEntity(item).apply {
            setLocation(location)
        }
        val fakeEntity = FakeEntityImpl(this, bukkitItemEntity, location)
        _entities += fakeEntity
        enqueue(fakeEntity)

        return fakeEntity
    }

    /* Modified */
    override fun spawnPlayer(location: Location, data: PlayerData): FakeEntity<Player> {
        val bukkitPlayer = createPlayerEntity(data).apply {
            setLocation(location)
        }
        val fakeEntity = FakeEntityImpl(this, bukkitPlayer, location)
        entityMetadata(bukkitPlayer)

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
        var nextTickEntity: FakeEntityImpl<*>? = null
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

    internal fun enqueue(entity: FakeEntityImpl<*>) {
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

