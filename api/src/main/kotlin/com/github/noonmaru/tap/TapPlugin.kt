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

package com.github.noonmaru.tap

import com.github.noonmaru.tap.attach.Tools
import com.github.noonmaru.tap.fake.FakeEntity
import com.github.noonmaru.tap.fake.FakeServer
import com.github.noonmaru.tap.fake.invisible
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Nemo
 */
class TapPlugin : JavaPlugin() {
    override fun onEnable() {
        Tools.loadAttachLibrary(dataFolder)

        debug()
    }

    private val fakeServer = FakeServer.create()
    private var fakeEntity: FakeEntity? = null

    private fun debug() {
        Bukkit.getOnlinePlayers().forEach { fakeServer.addPlayer(it) }
        server.apply {
            pluginManager.registerEvents(EventListener(), this@TapPlugin)
            scheduler.runTaskTimer(this@TapPlugin, Runnable {
                fakeEntity?.let { entity ->
                    Bukkit.getPlayerExact("Heptagram")?.let { player ->
                        entity.moveTo(player.location.apply {
                            add(direction.multiply(4.5))
                        })
                    }
                }

                fakeServer.update()
            }, 0L, 1L)
        }
    }

    inner class EventListener : Listener {
        @EventHandler
        fun onPlayerInteract(event: PlayerInteractEvent) {
            fakeEntity?.remove()
            fakeEntity = fakeServer.spawnEntity(event.player.location, ArmorStand::class.java).apply {
                metadata<ArmorStand> {
                    invisible = true
                    isMarker = true
//                    isGlowing = true
                    true
                }
                armorStandItem {
                    setItem(EquipmentSlot.HEAD, ItemStack(Material.STONE))
                    true
                }
            }
        }

        @EventHandler
        fun onPlayerJoin(event: PlayerJoinEvent) {
            fakeServer.addPlayer(event.player)
        }
    }

    override fun onDisable() {
        fakeServer.clear()
    }
}