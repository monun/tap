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

package com.github.noonmaru.tap.v1_12_R1.inventory;

import com.github.noonmaru.tap.inventory.InventorySupport;
import com.github.noonmaru.tap.inventory.TapInventory;
import com.github.noonmaru.tap.v1_12_R1.entity.NMSEntitySupport;
import com.github.noonmaru.tap.v1_12_R1.entity.NMSPlayer;
import com.google.common.collect.MapMaker;
import net.minecraft.server.v1_12_R1.IInventory;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftInventory;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;

public final class NMSInventorySupport implements InventorySupport
{

    private final Map<IInventory, TapInventory> cache;

    public NMSInventorySupport()
    {
        cache = new MapMaker().weakValues().makeMap();
    }

    @Override
    public TapInventory fromInventory(org.bukkit.inventory.Inventory inventory)
    {
        Map<IInventory, TapInventory> cache = this.cache;
        CraftInventory craftInventory = (CraftInventory) inventory;
        IInventory iinventory = craftInventory.getInventory();
        TapInventory tapInventory = cache.get(iinventory);

        if (tapInventory == null)
        {
            if (inventory instanceof org.bukkit.inventory.PlayerInventory)
            {
                NMSPlayer nmsPlayer = NMSEntitySupport.getInstance().wrapEntity(((PlayerInventory) inventory).getHolder());

                return nmsPlayer.getInventory();
            }

            tapInventory = new NMSInventory(iinventory, craftInventory);
            cache.put(iinventory, tapInventory);
        }

        return tapInventory;
    }

}
