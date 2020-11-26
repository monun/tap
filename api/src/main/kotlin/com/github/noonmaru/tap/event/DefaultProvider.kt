package com.github.noonmaru.tap.event

import org.bukkit.entity.Entity
import org.bukkit.event.Event
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.SignChangeEvent
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
        override fun getFrom(event: BlockIgniteEvent): Entity {
            return requireNotNull(event.ignitingEntity)
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
        override fun getFrom(event: HangingBreakByEntityEvent): Entity {
            return requireNotNull(event.remover)
        }
    }

    internal class HangingPlaceEntityProvider : EntityProvider<HangingPlaceEvent> {
        override fun getFrom(event: HangingPlaceEvent): Entity {
            return requireNotNull(event.player)
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