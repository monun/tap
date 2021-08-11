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
import org.bukkit.Bukkit
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin

class TapPlugin : JavaPlugin() {
    override fun onEnable() {
        FakeTest().apply {
            register()
        }
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
                event.item?.let { item ->
                    val type = item.type

                    if (type.isBlock) {
                        val fakeFallingBlock = fakeEntityServer.spawnFallingBlock(
                            event.player.eyeLocation.apply { add(direction.multiply(5.0)) },
                            type.createBlockData()
                        )
                        val fakeStand = fakeEntityServer.spawnEntity(event.player.eyeLocation.apply { add(direction.multiply(5.0)) }, ArmorStand::class.java).apply {
                            updateMetadata<ArmorStand> {
                                isInvisible = true
                                isMarker = true
                            }
                        }

                        fakeStand.addPassenger(fakeFallingBlock)
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