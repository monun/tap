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

package com.github.noonmaru.tap.entity;

import com.github.noonmaru.tap.Tap;
import com.github.noonmaru.tap.item.TapItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;

public interface TapLivingEntity extends TapEntity
{

    static LivingEntity wrapLiving(LivingEntity entity)
    {
        return Tap.ENTITY.wrapEntity(entity);
    }

    LivingEntity getBukkitEntity();

    float getEyeHeight();

    double getHealth();

    TapItemStack getEquipment(EquipmentSlot slot);

    void setEquipment(EquipmentSlot slot, TapItemStack item);

}
