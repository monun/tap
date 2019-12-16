/*
 * Copyright (c) 2019 Noonmaru
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

package com.github.noonmaru.tap.event.entity.impl;

import com.github.noonmaru.tap.event.entity.EntityHandler;
import com.github.noonmaru.tap.event.entity.EntityProvider;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.*;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.vehicle.VehicleEvent;

/**
 * 기본 이벤트 엔티티 제공자입니다.
 * {@link EntityHandler}에서 {@link EntityHandler#provider()}를 이 클래스로 설정하면 미리 선언된 제공자를 사용합니다.
 *
 * @author Nemo
 * @see EntityProvider
 */
public abstract class DefaultProvider<T> implements EntityProvider
{

    private DefaultProvider() {}

    static final class BlockBreakEntityProvider implements EntityProvider<BlockBreakEvent>
    {
        public Entity getFrom(BlockBreakEvent event)
        {
            return event.getPlayer();
        }
    }

    static final class BlockDamageEntityProvider implements EntityProvider<BlockDamageEvent>
    {
        @Override
        public Entity getFrom(BlockDamageEvent event)
        {
            return event.getPlayer();
        }
    }

    static final class BlockIgniteEntityProvider implements EntityProvider<BlockIgniteEvent>
    {
        public Entity getFrom(BlockIgniteEvent event)
        {
            return event.getIgnitingEntity();
        }
    }

    static final class BlockPlaceEntityProvider implements EntityProvider<BlockPlaceEvent>
    {
        @Override
        public Entity getFrom(BlockPlaceEvent event)
        {
            return event.getPlayer();
        }
    }

    static final class SignChangeEntityProvider implements EntityProvider<SignChangeEvent>
    {
        @Override
        public Entity getFrom(SignChangeEvent event)
        {
            return event.getPlayer();
        }
    }

    static final class EnchantItemEntityProvider implements EntityProvider<EnchantItemEvent>
    {
        @Override
        public Entity getFrom(EnchantItemEvent event)
        {
            return event.getEnchanter();
        }
    }

    static final class PrepareItemEnchantEntityProvider implements EntityProvider<PrepareItemEnchantEvent>
    {
        @Override
        public Entity getFrom(PrepareItemEnchantEvent event)
        {
            return event.getEnchanter();
        }
    }

    static final class EntityEntityProvider implements EntityProvider<EntityEvent>
    {
        public Entity getFrom(EntityEvent event)
        {
            return event.getEntity();
        }
    }

    static final class HangingBreakByEntityEntityProvider implements EntityProvider<HangingBreakByEntityEvent>
    {
        public Entity getFrom(HangingBreakByEntityEvent event)
        {
            return event.getRemover();
        }
    }

    static final class HangingPlaceEntityProvider implements EntityProvider<HangingPlaceEvent>
    {
        @Override
        public Entity getFrom(HangingPlaceEvent event)
        {
            return event.getPlayer();
        }
    }

    static final class FurnaceExtractEntityProvider implements EntityProvider<FurnaceExtractEvent>
    {
        @Override
        public Entity getFrom(FurnaceExtractEvent event)
        {
            return event.getPlayer();
        }
    }

    static final class InventoryCloseEntityProvider implements EntityProvider<InventoryCloseEvent>
    {
        @Override
        public Entity getFrom(InventoryCloseEvent event)
        {
            return event.getPlayer();
        }
    }

    static final class InventoryInteractEntityProvider implements EntityProvider<InventoryInteractEvent>
    {
        @Override
        public Entity getFrom(InventoryInteractEvent event)
        {
            return event.getWhoClicked();
        }
    }

    static final class InventoryOpenEntityProvider implements EntityProvider<InventoryOpenEvent>
    {
        @Override
        public Entity getFrom(InventoryOpenEvent event)
        {
            return event.getPlayer();
        }
    }

    static final class PlayerProvider implements EntityProvider<PlayerEvent>
    {
        @Override
        public Entity getFrom(PlayerEvent event)
        {
            return event.getPlayer();
        }
    }

    static final class VehicleProvider implements EntityProvider<VehicleEvent>
    {
        @Override
        public Entity getFrom(VehicleEvent event)
        {
            return event.getVehicle();
        }
    }

}
