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

package io.github.monun.tap.plugin

import io.github.monun.tap.fake.FakeEntity
import io.github.monun.tap.fake.FakeEntityServer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class TapPlugin : JavaPlugin() {
    private lateinit var fakeTest: FakeTest

    override fun onEnable() {
        fakeTest = FakeTest()
        fakeTest.register(this)
    }

    override fun onDisable() {
        fakeTest.unregister()
    }
}

class FakeTest : Listener, Runnable {

    private lateinit var fakeEntityServer: FakeEntityServer

    fun register(plugin: TapPlugin) {
        fakeEntityServer = FakeEntityServer.create(plugin)
        plugin.server.apply {
            pluginManager.registerEvents(this@FakeTest, plugin)
            scheduler.runTaskTimer(plugin, this@FakeTest, 0L, 1L)
        }

        Bukkit.getOnlinePlayers().forEach {
            fakeEntityServer.addPlayer(it)
        }
    }

    fun unregister() {
        fakeEntityServer.clear()
    }

    private val fakePlayers = arrayListOf<FakeEntity<*>>()

    override fun run() {
        Bukkit.getOnlinePlayers().firstOrNull()?.let { player ->
            fakePlayers.forEach {
                val loc = player.location
                it.rotate(loc.yaw, loc.pitch)
            }
        }

        fakeEntityServer.update()
    }

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
        val player = event.player
        val loc = player.location.apply {
            x = blockX.toDouble()
            y = blockY.toDouble()
            z = blockZ + 0.5
        }

        if (event.item?.type == Material.COMPOSTER) {
            val armorStand = fakeEntityServer.spawnEntity(loc, ArmorStand::class.java).apply {
                updateMetadata {
                    isMarker = true
                }
            }

            val passenger = fakeEntityServer.spawnFallingBlock(loc, Material.DIRT.createBlockData())

            armorStand.addPassenger(passenger)
        }
    }
}
