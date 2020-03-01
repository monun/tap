/*
 * Copyright (c) 2020 Noonmaru
 *
 * Licensed under the General Public License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-2.0.php
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.noonmaru.tap

import com.github.noonmaru.tap.attach.Tools
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Nemo
 */
class TapPlugin : JavaPlugin() {
    override fun onEnable() {
        Tools.loadAttachLibrary(dataFolder)
//        Debug(this)
    }
}

//class Debug(plugin: TapPlugin) : Listener {
//
//    private val fakeManager = FakeManager()
//
//    private val fakeStand: FakeArmorStand
//
//    private val fakeBlock: FakeFallingBlock
//
//    init {
//        val loc = Bukkit.getWorlds().first().spawnLocation
//
//        fakeStand = fakeManager.createFakeEntity(loc)
//        fakeBlock = fakeManager.createFallingBlock(loc, Material.STONE.createBlockData())
//
//        fakeStand.addPassenger(fakeBlock)
//
//        plugin.server.scheduler.runTaskTimer(plugin, fakeManager, 0, 1)
//        plugin.server.pluginManager.registerEvents(this, plugin)
//    }
//
//    @EventHandler
//    fun onJoin(event: PlayerJoinEvent) {
//        fakeManager.addPlayer(event.player)
//        fakeStand.setPosition(event.player.location)
//    }
//
//    @EventHandler
//    fun onInteract(event: PlayerInteractEvent) {
//        fakeBlock.show = !fakeBlock.show
//
//        fakeStand.apply {
//            invisible =false
//            mark = true
//        }
//
//        fakeStand.setPosition(event.player.location.apply {
//            add(direction.multiply(5))
//        })
//
//        fakeManager.createFakeEntity(event.player.location.apply {
//            add(direction.multiply(10))
//        }, Parrot::class.java).applyMetadata<Parrot> {
//            it.variant = Parrot.Variant.GRAY
//            it.customName = "앵무새"
//            it.isCustomNameVisible = true
//        }
//    }
//}