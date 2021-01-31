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

package com.github.monun.tap.event

import org.bukkit.entity.Entity
import org.bukkit.entity.Projectile
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEntityEvent

@Suppress("unused")
fun interface EntityProvider<T : Event> {
    fun getFrom(event: T): Entity?

    class PlayerPickupItem {
        class PickupItem : EntityProvider<EntityPickupItemEvent> {
            override fun getFrom(event: EntityPickupItemEvent): Entity {
                return event.item
            }
        }
    }

    class PlayerInteractEntity {
        class Clicked : EntityProvider<PlayerInteractEntityEvent> {
            override fun getFrom(event: PlayerInteractEntityEvent): Entity {
                return event.rightClicked
            }
        }
    }

    class EntityDeath {
        class Killer : EntityProvider<EntityDeathEvent> {
            override fun getFrom(event: EntityDeathEvent): Entity? {
                return event.entity.killer
            }
        }
    }

    class EntityDamageByEntity {
        class Damager : EntityProvider<EntityDamageByEntityEvent> {
            override fun getFrom(event: EntityDamageByEntityEvent): Entity {
                return event.damager
            }
        }

        class Shooter : EntityProvider<EntityDamageByEntityEvent> {
            override fun getFrom(event: EntityDamageByEntityEvent): Entity? {
                val damager = event.damager

                if (damager is Projectile) {
                    val source = damager.shooter
                    if (source is Entity) return source
                }

                return null
            }
        }
    }

    class ProjectileHit {
        class Shooter : EntityProvider<ProjectileHitEvent> {
            override fun getFrom(event: ProjectileHitEvent): Entity? {
                val shooter = event.entity.shooter

                return if (shooter is Entity) shooter else null
            }
        }
    }
}
