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

package com.github.noonmaru.tap

import com.github.noonmaru.tap.attach.Tools
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Nemo
 */
class TapPlugin : JavaPlugin() {
    override fun onEnable() {
        Tools.loadAttachLibrary(dataFolder)

//        debug()
    }

//    private fun debug() {
//        Debug(this).also { debug ->
//            server.apply {
//                pluginManager.registerEvents(debug, this@TapPlugin)
//                scheduler.runTaskTimer(this@TapPlugin, debug, 0L, 1L)
//            }
//        }
//    }
}

//class Debug(plugin: JavaPlugin) : Listener, Runnable {
//    private val fakeServer = FakeServer.create(plugin)
//
//    override fun run() {
//        fakeServer.update()
//    }
//
//    @EventHandler
//    fun onPlayerJoin(event: PlayerJoinEvent) {
//        fakeServer.addPlayer(event.player)
//    }
//
//    @EventHandler
//    fun onPlayerInteract(event: PlayerInteractEvent) {
//        val location = event.player.eyeLocation
//        val offset = Vector(-32.0, 0.0, 20.0).rotateAroundY(-Math.toRadians(location.yaw.toDouble()))
//        location.add(offset)
//        val v = Vector(5.0, 0.0, 0.0).rotateAroundY(-Math.toRadians(location.yaw.toDouble()))
//
//        val projectile = TestProjectile().apply {
//            mount(fakeServer.spawnEntity(location, ArmorStand::class.java), Vector(0.0, -1.62, 0.0))
//            velocity = v
//        }
//
//        fakeServer.launch(location, projectile)
//    }
//}
//
//class TestProjectile : FakeProjectile(100, 100.0) {
//    override fun onMove(movement: Movement) {
//        val to = movement.to
//
//        to.world.spawnParticle(
//            Particle.FLAME,
//            to,
//            0,
//            0.0,
//            0.0,
//            0.0,
//            0.0,
//            null,
//            true
//        )
//    }
//
//    override fun onTrail(from: Location, to: Location) {
//        to.world.spawnParticle(
//            Particle.VILLAGER_HAPPY,
//            to.add(0.0, 0.25, 0.0),
//            0,
//            0.0,
//            0.0,
//            0.0,
//            0.0,
//            null,
//            true
//        )
//
//        if (from.world === to.world) {
//            val v = from.vector(to)
//            val length = v.normalizeAndLength()
//
//            from.world.rayTrace(from, v, length, FluidCollisionMode.NEVER, true,  0.5) { entity ->
//                entity is LivingEntity
//            }?.let { result ->
//                val hit = result.hitPosition
//                from.world.createExplosion(hit.x, hit.y, hit.z, 4.0F)
//                remove()
//            }
//        }
//    }
//
//    override fun onRemove() {
//        passenger?.fakeEntity?.remove()
//    }
//}