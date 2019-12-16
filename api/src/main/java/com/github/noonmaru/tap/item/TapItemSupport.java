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

package com.github.noonmaru.tap.item;

import com.github.noonmaru.tap.nbt.NBTCompound;
import org.bukkit.inventory.ItemStack;

public interface TapItemSupport
{

    TapItem getItem(int id);

    TapItem getItem(String name);

    Iterable<? extends TapItem> getItems();

    TapItemStack newItemStack(TapItem item, int amount, int data);

    default TapItemStack newItemStack(int id, int amount, int data)
    {
        return newItemStack(getItem(id), amount, data);
    }

    default TapItemStack newItemStack(String name, int amount, int data)
    {
        return newItemStack(getItem(name), amount, data);
    }

    TapItemStack fromItemStack(ItemStack itemStack);

    TapItemStack loadItemStack(NBTCompound compound);

}
