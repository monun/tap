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
//    private val fakeEntityServer = FakeEntityServer.create(plugin)
//    private val fakeProjectileManager = FakeProjectileManager()
//
//    init {
//        for (player in Bukkit.getOnlinePlayers()) {
//            fakeEntityServer.addPlayer(player)
//        }
//    }
//
//    override fun run() {
//        fakeProjectileManager.update()
//        fakeEntityServer.update()
//    }
//
//    @EventHandler
//    fun onPlayerJoin(event: PlayerJoinEvent) {
//        fakeEntityServer.addPlayer(event.player)
//    }
//
//    @EventHandler
//    fun onPlayerInteract(event: PlayerInteractEvent) {
//        val location = event.player.eyeLocation
//        val offset = Vector(-32.0, 0.0, 20.0).rotateAroundY(-Math.toRadians(location.yaw.toDouble()))
//        location.add(offset)
//        val v = Vector(2.0, 0.0, 0.0).rotateAroundY(-Math.toRadians(location.yaw.toDouble()))
//
//        val armorStand = fakeEntityServer.spawnEntity(location, ArmorStand::class.java).apply {
//            updateMetadata<ArmorStand> {
//                invisible = true
//                isMarker = true
//            }
//            updateEquipment {
//                helmet = ItemStack(Material.DIAMOND_SWORD)
//            }
//        }
//        val projectile = TestProjectile().apply {
//            setPassenger(armorStand) { entity, location ->
//                entity.moveTo(location.add(0.0, -2.0, 0.0).apply {
//                    yaw += 90
//                    entity.updateMetadata<ArmorStand> {
//                        headPose = EulerAngle(0.0, 0.0, Math.toRadians(pitch + 45.0))
//                    }
//                })
//            }
//            velocity = v
//        }
//
//        fakeProjectileManager.launch(location, projectile)
//    }
//}
//
//class TestProjectile : FakeProjectile(100, 100.0) {
//
//    override fun onMove(movement: Movement) {
//        val to = movement.to
//        to.world.spawnParticle(
//            Particle.FLAME,
//            to.clone().add(0.0, 0.2, 0.0),
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
//    override fun onTrail(trail: Trail) {
//        val from = trail.from
//        val to = trail.to
//
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
//            from.world.rayTrace(from, v, length, FluidCollisionMode.NEVER, true, 0.5) { entity ->
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