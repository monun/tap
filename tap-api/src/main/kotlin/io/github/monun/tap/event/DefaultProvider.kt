/*
 * Tap
 * Copyright (C) 2021 Monun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.monun.tap.event

import org.bukkit.entity.Entity
import org.bukkit.event.Event
import org.bukkit.event.block.*
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.enchantment.PrepareItemEnchantEvent
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.inventory.FurnaceExtractEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.vehicle.VehicleEvent

@Suppress("unused")
abstract class DefaultProvider<T : Event> private constructor() : EntityProvider<T> {
    internal class BlockBreakEntityProvider : EntityProvider<BlockBreakEvent> {
        override fun getFrom(event: BlockBreakEvent): Entity {
            return event.player
        }
    }

    internal class BlockDamageEntityProvider : EntityProvider<BlockDamageEvent> {
        override fun getFrom(event: BlockDamageEvent): Entity {
            return event.player
        }
    }

    internal class BlockIgniteEntityProvider : EntityProvider<BlockIgniteEvent> {
        override fun getFrom(event: BlockIgniteEvent): Entity? {
            return event.ignitingEntity
        }
    }

    internal class BlockPlaceEntityProvider : EntityProvider<BlockPlaceEvent> {
        override fun getFrom(event: BlockPlaceEvent): Entity {
            return event.player
        }
    }

    internal class SignChangeEntityProvider : EntityProvider<SignChangeEvent> {
        override fun getFrom(event: SignChangeEvent): Entity {
            return event.player
        }
    }

    internal class EnchantItemEntityProvider : EntityProvider<EnchantItemEvent> {
        override fun getFrom(event: EnchantItemEvent): Entity {
            return event.enchanter
        }
    }

    internal class PrepareItemEnchantEntityProvider : EntityProvider<PrepareItemEnchantEvent> {
        override fun getFrom(event: PrepareItemEnchantEvent): Entity {
            return event.enchanter
        }
    }

    internal class EntityEntityProvider : EntityProvider<EntityEvent> {
        override fun getFrom(event: EntityEvent): Entity {
            return event.entity
        }
    }

    internal class HangingBreakByEntityEntityProvider : EntityProvider<HangingBreakByEntityEvent> {
        override fun getFrom(event: HangingBreakByEntityEvent): Entity? {
            return event.remover
        }
    }

    internal class HangingPlaceEntityProvider : EntityProvider<HangingPlaceEvent> {
        override fun getFrom(event: HangingPlaceEvent): Entity? {
            return event.player
        }
    }

    internal class FurnaceExtractEntityProvider : EntityProvider<FurnaceExtractEvent> {
        override fun getFrom(event: FurnaceExtractEvent): Entity {
            return event.player
        }
    }

    internal class InventoryCloseEntityProvider : EntityProvider<InventoryCloseEvent> {
        override fun getFrom(event: InventoryCloseEvent): Entity {
            return event.player
        }
    }

    internal class InventoryInteractEntityProvider : EntityProvider<InventoryInteractEvent> {
        override fun getFrom(event: InventoryInteractEvent): Entity {
            return event.whoClicked
        }
    }

    internal class InventoryOpenEntityProvider : EntityProvider<InventoryOpenEvent> {
        override fun getFrom(event: InventoryOpenEvent): Entity {
            return event.player
        }
    }

    internal class PlayerProvider : EntityProvider<PlayerEvent> {
        override fun getFrom(event: PlayerEvent): Entity {
            return event.player
        }
    }

    internal class VehicleProvider : EntityProvider<VehicleEvent> {
        override fun getFrom(event: VehicleEvent): Entity {
            return event.vehicle
        }
    }
}