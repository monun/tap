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

package io.github.monun.tap.event

import org.bukkit.entity.Entity
import org.bukkit.entity.Projectile
import org.bukkit.event.Event
import org.bukkit.event.entity.*
import org.bukkit.event.player.PlayerInteractEntityEvent

/**
 * 이벤트를 수신할 대상을 지정합니다.
 * [EntityDamageByEntityEvent]와 같이 [Entity]가 여러개 포함된 이벤트일 경우
 * [EntityProvider.EntityDamageByEntity.Damager]를 사용하여 공격자를 대상으로 지정할 수 있습니다.
 */
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

    class EntityTarget {
        class Target : EntityProvider<EntityTargetEvent> {
            override fun getFrom(event: EntityTargetEvent): Entity? {
                return event.target
            }
        }
    }

    class EntityTame {
        class Owner : EntityProvider<EntityTameEvent> {
            override fun getFrom(event: EntityTameEvent): Entity? {
                return event.owner.takeIf { it is Entity } as Entity?
            }
        }
    }

    class ProjectileLaunch {
        class Shooter : EntityProvider<ProjectileLaunchEvent> {
            override fun getFrom(event: ProjectileLaunchEvent): Entity? {
                return event.entity.shooter?.takeIf { it is Entity } as Entity?
            }
        }
    }

    class ProjectileHit {
        class Shooter : EntityProvider<ProjectileHitEvent> {
            override fun getFrom(event: ProjectileHitEvent): Entity? {
                return event.entity.shooter?.takeIf { it is Entity } as Entity?
            }
        }
    }
}
