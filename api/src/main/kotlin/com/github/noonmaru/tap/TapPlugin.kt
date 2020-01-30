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

//class Debug(val plugin: TapPlugin) {
//
//    val fem: FakeEntityManager = FakeEntityManager()
//
//    init {
//        plugin.server.scheduler.runTaskTimer(plugin, Runnable {
//            fem.run()
//        }, 0, 1)
//        plugin.server.pluginManager.registerEvents(object:Listener {
//            @EventHandler
//            fun onInteract(event: PlayerInteractEvent) {
//
//                val loc = event.player.eyeLocation
//                loc.add(loc.direction.multiply(5))
//
//                fem.createFakeEntity<FakeArmorStand>(loc, ArmorStand::class.java).apply {
//                    glowing = true
//                    mark = true
//                    invisible = true
//                    setItem(EquipmentSlot.HEAD, ItemStack(Material.ARROW))
//                    setItem(EquipmentSlot.HAND, ItemStack(Material.DIAMOND_SWORD))
//                }
//
//                val effect = FireworkEffect.builder().with(FireworkEffect.Type.STAR).withColor(Color.AQUA).build()
//                loc.world.playFirework(loc, effect)
//            }
//        }, plugin)
//    }
//
//}