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

package com.github.noonmaru.tap.v1_12_R1.item;

import com.github.noonmaru.tap.item.TapItem;
import net.minecraft.server.v1_12_R1.Item;
import net.minecraft.server.v1_12_R1.ItemBlock;

public final class NMSItem implements TapItem
{

    private final Item item;

    NMSItem(Item item)
    {
        this.item = item;
    }

    @Override
    public int getId()
    {
        return Item.getId(item);
    }

    @Override
    public String getName()
    {
        return item.getName();
    }

    @Override
    public String getUnlocalizedName()
    {
        return getName() + ".name";
    }

    @Override
    public String getTextureId()
    {
        return Item.REGISTRY.b(item).toString();
    }

    @Override
    public int getMaxStackSize()
    {
        return item.getMaxStackSize();
    }

    @Override
    public int getMaxDurability()
    {
        return item.getMaxDurability();
    }

    @Override
    public boolean isBlock()
    {
        return item instanceof ItemBlock;
    }

    public Item getHandle()
    {
        return item;
    }

}
