/*
 *
 *  * Copyright (c) 2020 Noonmaru
 *  *
 *  * Licensed under the General Public License, Version 3.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * https://opensource.org/licenses/gpl-3.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *
 */

package com.github.noonmaru.tap.event;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface EntityProvider<T extends Event> {
    Entity getFrom(@NotNull T event);

    final class PlayerPickupItem {
        public static final class PickupItem implements EntityProvider<EntityPickupItemEvent> {
            @Override
            public Entity getFrom(@NotNull EntityPickupItemEvent event) {
                return event.getItem();
            }
        }
    }

    final class PlayerInteractEntity {
        public static final class Clicked implements EntityProvider<PlayerInteractEntityEvent> {
            @Override
            public Entity getFrom(@NotNull PlayerInteractEntityEvent event) {
                return event.getRightClicked();
            }
        }
    }

    final class EntityDeath {
        public static final class Killer implements EntityProvider<EntityDeathEvent> {
            @Override
            public Entity getFrom(@NotNull EntityDeathEvent event) {
                return event.getEntity().getKiller();
            }
        }
    }

    final class EntityDamageByEntity {
        public static final class Damager implements EntityProvider<EntityDamageByEntityEvent> {
            @Override
            public Entity getFrom(@NotNull EntityDamageByEntityEvent event) {
                return event.getDamager();
            }
        }

        public static final class Shooter implements EntityProvider<EntityDamageByEntityEvent> {
            @Override
            public Entity getFrom(@NotNull EntityDamageByEntityEvent event) {
                Entity damager = event.getDamager();

                if (damager instanceof Projectile) {
                    ProjectileSource source = ((Projectile) damager).getShooter();

                    if (source instanceof Entity)
                        return (Entity) source;
                }

                return null;
            }
        }
    }

    final class ProjectileHit {
        public static final class Shooter implements EntityProvider<ProjectileHitEvent> {
            @Override
            public Entity getFrom(@NotNull ProjectileHitEvent event) {
                ProjectileSource shooter = event.getEntity().getShooter();

                return shooter instanceof Entity ? (Entity) shooter : null;
            }
        }
    }

}
