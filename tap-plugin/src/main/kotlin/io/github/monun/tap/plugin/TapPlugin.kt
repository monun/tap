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
 */

package io.github.monun.tap.plugin

import io.github.monun.tap.fake.FakeEntityServer
import io.github.monun.tap.protocol.PacketSupport
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class TapPlugin : JavaPlugin() {
    override fun onEnable() {
        FakeTest().apply {
            register()
        }
        SetSlotTest().apply {
            register()
        }
    }
}

class SetSlotTest {
    fun JavaPlugin.register() {
        fun Player.updateSlot(isSneaking: Boolean = this.isSneaking) {
            if (isSneaking) {
                repeat(9) {
                    PacketSupport.containerSetSlot(-2, 0, 0 + it, ItemStack(Material.RED_CONCRETE)).sendTo(this)
                }
            } else {
                updateInventory()
            }
        }

        server.pluginManager.registerEvents(object : Listener {
            @EventHandler
            fun onPlayerToggleSneak(event: PlayerToggleSneakEvent) {
                event.player.updateSlot(event.isSneaking)
            }
        }, this)

        server.scheduler.runTaskTimer(this, Runnable {
            Bukkit.getOnlinePlayers().forEach {
                if (it.isSneaking) it.updateSlot()
            }
        }, 0L, 1L)
    }
}

class FakeTest {
    private lateinit var fakeEntityServer: FakeEntityServer

    fun JavaPlugin.register() {
        fakeEntityServer = FakeEntityServer.create(this)

        server.scheduler.runTaskTimer(this, this@FakeTest::update, 0L, 1L)
        server.pluginManager.registerEvents(object : Listener {
            @EventHandler
            fun onJoin(event: PlayerJoinEvent) {
                fakeEntityServer.addPlayer(event.player)
            }

            @EventHandler
            fun onQuit(event: PlayerQuitEvent) {
                fakeEntityServer.removePlayer(event.player)
            }

            @EventHandler
            fun onPlayerInteract(event: PlayerInteractEvent) {
                val player = event.player
                val action = event.action

                if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                    val target = player.getTargetBlock(32)!!.location.add(0.0, 1.0, 0.0)
                    val item = fakeEntityServer.spawnItem(target, ItemStack(Material.EMERALD)).apply {
                        broadcast {
                            PacketSupport.entityVelocity(bukkitEntity.entityId, bukkitEntity.velocity)
                        }
                    }
                    server.scheduler.runTaskLater(this@register, Runnable {
                        item.remove()
                    }, 100)

                } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                    player.getTargetEntity(32)?.let { target ->
                        for (entity in fakeEntityServer.entities) {
                            val bukkitEntity = entity.bukkitEntity

                            if (bukkitEntity is Item) {
                                entity.broadcastImmediately(
                                    PacketSupport.takeItem(
                                        bukkitEntity.entityId,
                                        target.entityId,
                                        1
                                    )
                                )
                                entity.remove()
                            }
                        }
                    }
                }
            }
        }, this)

        Bukkit.getOnlinePlayers().forEach { fakeEntityServer.addPlayer(it) }
    }

    private fun update() {
        fakeEntityServer.update()
    }
}