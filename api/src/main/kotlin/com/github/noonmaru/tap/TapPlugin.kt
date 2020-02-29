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
import com.github.noonmaru.tap.command.command
import com.github.noonmaru.tap.debug.CommandDebug
import com.github.noonmaru.tap.debug.CommandDebugBookMeta
import com.github.noonmaru.tap.fake.FakeArmorStand
import com.github.noonmaru.tap.fake.FakeEntityManager
import org.bukkit.Material
import org.bukkit.entity.Shulker
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Nemo
 */
class TapPlugin : JavaPlugin() {
    override fun onEnable() {
        Tools.loadAttachLibrary(dataFolder)

        //DEBUG (Command DSL)
        command("tap") {
            help("help")
            component("debug") {
                usage = "[Messages]"
                description = "디버그 명령입니다."
                CommandDebug()
            }
            component("book") {
                description = "BookMeta Support를 테스트합니다."
                CommandDebugBookMeta()
            }
        }


        val fakeEntityManager = FakeEntityManager()

        server.pluginManager.registerEvents(object : Listener {
            @EventHandler
            fun onJoin(event: PlayerJoinEvent) {
                fakeEntityManager.addPlayer(event.player)
            }

            @EventHandler
            fun onInteract(event: PlayerInteractEvent) {
                val loc = event.player.location.apply {
                    add(direction.multiply(5))
                    yaw = 0.0F
                    pitch = 0.0F
                }
                val stand = fakeEntityManager.createFakeEntity<FakeArmorStand>(loc).apply {
                    invisible = true
                    mark = true
                }
                val block = fakeEntityManager.createFallingBlock(loc, Material.RED_CONCRETE.createBlockData()).apply {
                    glowing = true
                }
                val shulker = fakeEntityManager.createFakeEntity(loc, Shulker::class.java).apply {
                    invisible = true
                }

                stand.addPassenger(shulker)
                stand.addPassenger(block)

                fakeEntityManager.destroyAll()
            }
        }, this)

        server.scheduler.runTaskTimer(this, Runnable {
            fakeEntityManager.run()
        }, 0, 1)

    }
}