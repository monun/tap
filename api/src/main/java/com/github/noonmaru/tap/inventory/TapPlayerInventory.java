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

package com.github.noonmaru.tap.inventory;

import com.github.noonmaru.tap.entity.TapPlayer;
import com.github.noonmaru.tap.item.TapItemStack;

import java.util.List;

public interface TapPlayerInventory extends TapInventory
{

    List<? extends TapItemStack> getInventoryContents();

    TapPlayer getHolder();

    TapItemStack getHelmet();

    void setHelmet(TapItemStack helmet);

    TapItemStack getChestplate();

    void setChestplate(TapItemStack chestplate);

    TapItemStack getLeggings();

    TapItemStack getBoots();

    void setBoots(TapItemStack boots);

    List<? extends TapItemStack> getArmorContents();

    void setArmorContents(List<? extends TapItemStack> armors);

    TapItemStack getHeldItemMainHand();

    void setHeldItemMainHand(TapItemStack item);

    TapItemStack getHeldItemOffHand();

    void setHeldItemOffHand(TapItemStack item);

    TapItemStack getCursor();

    void setCursor(TapItemStack cursor);

    int getHeldItemSlot();

    void setHeldItemSlot(int slot);

    default boolean pickup(TapItemStack item)
    {
        return addItem(item) == item.getAmount();
    }

    void setLeggins(TapItemStack leggins);

    void update();

    void update(int slot);

    void updateCursor();

}
