/*
 * Copyright (C) 2022 Monun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.monun.tap.fake.internal

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent
import com.destroystokyo.paper.profile.ProfileProperty
import com.google.common.collect.ImmutableList
import io.github.monun.tap.fake.*
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class FakeEntityServerImpl(plugin: JavaPlugin) : FakeEntityServer {

    override var spawnDistance: Double = 160.0
        set(value) {
            require(value < despawnDistance) { "spawnDistance < despawnDistance" }
            field = value
            _entities.forEach { it.updateTrackers() }
        }

    override var despawnDistance: Double = 176.0
        set(value) {
            require(value > spawnDistance) { "despawnDistance > spawnDistance" }
            field = value
            _entities.forEach { it.updateTrackers() }
        }

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
            tap().location = location
        }

        val fakeEntity = FakeEntityImpl(this, bukkitEntity, location)
        _entities += fakeEntity
        enqueue(fakeEntity)

        return fakeEntity
    }

    override fun spawnFallingBlock(location: Location, blockData: BlockData): FakeEntity<FallingBlock> {
        val bukkitFallingBlock = blockData.createFallingBlock().apply {
            tap().location = location
        }
        val fakeEntity = FakeEntityImpl(this, bukkitFallingBlock, location)
        _entities += fakeEntity
        enqueue(fakeEntity)

        return fakeEntity
    }

    override fun spawnItem(location: Location, item: ItemStack): FakeEntity<Item> {
        val bukkitItemEntity = item.createItemEntity().apply {
            tap().location = location
        }
        val fakeEntity = FakeEntityImpl(this, bukkitItemEntity, location)
        _entities += fakeEntity
        enqueue(fakeEntity)

        return fakeEntity
    }

    override fun spawnPlayer(
        location: Location,
        name: String,
        profileProperties: Set<ProfileProperty>,
        skinParts: FakeSkinParts,
        uniqueId: UUID
    ): FakeEntity<Player> {
        val bukkitPlayer = createPlayerEntity(name, profileProperties, skinParts, uniqueId).apply {
            tap().location = location
        }
        val fakeEntity = FakeEntityImpl(this, bukkitPlayer, location)

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

        @EventHandler(priority = EventPriority.HIGH)
        fun onInteract(event: PlayerUseUnknownEntityEvent) {
            if (event.player !in trackersByPlayer) return
            val fakeEntity = entities.find { it.bukkitEntity.entityId == event.entityId } ?: return
            val entity = fakeEntity.bukkitEntity
            if (
                entity !is Item &&
                entity !is ExperienceOrb &&
                entity !is AbstractArrow
//                (entity !== event.player || event.player.gameMode == GameMode.SPECTATOR)
//                위에 기능은 카메라 시점기능인데 구현하기 뭐한감이 있어 삭제
            ) {
                if (event.isAttack) event.player.resetCooldown()
                PlayerInteractFakeEntityEvent(
                    event.player,
                    fakeEntity,
                    event.isAttack,
                    event.hand
                ).callEvent()
            } else {
                event.player.kick(
                    Component.translatable("multiplayer.disconnect.invalid_entity_attacked"),
                    PlayerKickEvent.Cause.INVALID_ENTITY_ATTACKED
                )
                Bukkit.getLogger().warning("Player ${event.player.name} tried to attack an invalid entity")
            }
        }
    }
}

