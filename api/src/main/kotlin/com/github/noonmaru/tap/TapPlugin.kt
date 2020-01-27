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
import com.github.noonmaru.tap.effect.playFirework
import com.github.noonmaru.tap.event.EntityEventManager
import com.github.noonmaru.tap.event.RegisteredEntityListener
import com.github.noonmaru.tap.mojang.getProfile
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Nemo
 */
class TapPlugin : JavaPlugin() {

    override fun onEnable() {
        Tools.loadAttachLibrary(dataFolder)

        entityEventManager = EntityEventManager(this)
    }

    lateinit var entityEventManager: EntityEventManager

    lateinit var registeredListener: RegisteredEntityListener

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        sender as Player

        val effect = FireworkEffect.builder().withColor(Color.AQUA).with(FireworkEffect.Type.STAR).build()

        sender.playFirework(sender.location.add(0.0, 10.0, 0.0), effect)
        sender.playSound(sender.location, Sound.ENTITY_BLAZE_SHOOT, 1.0F, 1.0F)

        registeredListener = entityEventManager.registerEvents(sender, object : Listener {
            @EventHandler
            fun onPlayerToggleSneak(event: PlayerToggleSneakEvent) {
                val player = event.player

                player.world.createExplosion(player.location.add(10.0, 0.0, 0.0), 4.0F)
                registeredListener.unregister()
            }
        })

        if (args.isNotEmpty()) {
            val profile = getProfile(args[0])

            profile?.let {
                sender.sendMessage("Found ${profile.name}, ${profile.uniqueId}")
            }
        }


        return true
    }
}