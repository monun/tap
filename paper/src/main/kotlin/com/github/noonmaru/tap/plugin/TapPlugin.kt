/*
 * Copyright (c) 2020 Noonmaru
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

package com.github.noonmaru.tap.plugin

import com.comphenix.protocol.utility.MinecraftVersion
import org.bukkit.plugin.java.JavaPlugin

class TapPlugin : JavaPlugin() {
    override fun onEnable() {
        try {
            classLoader.loadClass("com.comphenix.protocol.wrappers.Pair")
        } catch (exception: ClassNotFoundException) {
            if (MinecraftVersion.getCurrentVersion().minor > 15) {
                logger.warning(
                    "If you are using 1.16 or later, please use the latest" +
                            " ProtocolLib snapshot build from: https://ci.dmulloy2.net/job/ProtocolLib/lastSuccessfulBuild/"
                )
            }
        }

//        val server = FakeEntityServer.create(this)
//        val test = Test(server)
//        this.server.pluginManager.registerEvents(test, this)
//        this.server.scheduler.runTaskTimer(this, test, 0L, 1L)
    }
}

//class Test(
//    private val server: FakeEntityServer
//): Runnable, Listener {
//
//    private val map = HashMap<Player, FakeEntity>()
//
//    init {
//        Bukkit.getOnlinePlayers().forEach { register(it)}
//    }
//
//    private fun register(player: Player) {
//        val loc = player.location
//        val armorStand = server.spawnEntity(loc, ArmorStand::class.java).apply {
//            updateMetadata<ArmorStand> {
//                isMarker = true
//            }
//        }
//        val item = server.spawnEntity(loc, Item::class.java).apply {
//            updateMetadata<Item> {
//                setItemStack(ItemStack(Material.STONE))
//            }
//        }
//        armorStand.addPassenger(item)
//
//        map[player]  = armorStand
//        server.addPlayer(player)
//    }
//
//    @EventHandler
//    fun onJoin(event: PlayerJoinEvent) {
//        register(event.player)
//    }
//
//    @EventHandler
//    fun onQuit(event: PlayerQuitEvent) {
//        map.remove(event.player)?.let { it ->
//            it.passengers.forEach { it.remove() }
//            it.remove()
//        }
//    }
//
//    override fun run() {
//        for ((player, entity) in map) {
//            entity.moveTo(player.eyeLocation.add(0.0, 1.0, 0.0))
//        }
//
//        server.update()
//    }
//}