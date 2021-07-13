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
import org.bukkit.entity.Creeper
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin

class TapPlugin : JavaPlugin() {
    override fun onEnable() {
        val fakeEntityServer = FakeEntityServer.create(this)
        server.apply {
            scheduler.runTaskTimer(this@TapPlugin, fakeEntityServer::update, 0L, 1L)
            pluginManager.registerEvents(EventListener(this@TapPlugin, fakeEntityServer), this@TapPlugin)
        }
    }
}

class EventListener(
    private val plugin: JavaPlugin,
    private val fakeEntityServer: FakeEntityServer
) : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        fakeEntityServer.addPlayer(event.player)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        fakeEntityServer.removePlayer(event.player)
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val action = event.action

        if (action == Action.LEFT_CLICK_AIR) {

            val player = event.player
            val location = player.location.apply {
                add(direction.multiply(10))
            }
            val fakeEntity = fakeEntityServer.spawnEntity(location, Creeper::class.java)

            plugin.server.scheduler.runTaskTimer(plugin, Runnable {
                fakeEntity.moveTo(player.location.apply { add(direction.multiply(10)) })
            }, 0L, 1L)
        } else if (action == Action.RIGHT_CLICK_AIR) {
            fakeEntityServer.entities.forEach { it.remove() }
        }
    }
}
