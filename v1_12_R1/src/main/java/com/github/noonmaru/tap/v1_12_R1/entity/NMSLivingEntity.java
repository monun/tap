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

package com.github.noonmaru.tap.v1_12_R1.entity;

import com.github.noonmaru.tap.entity.TapLivingEntity;
import com.github.noonmaru.tap.item.TapItemStack;
import com.github.noonmaru.tap.v1_12_R1.item.NMSItemStack;
import com.github.noonmaru.tap.v1_12_R1.item.NMSItemSupport;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityLiving;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;

public class NMSLivingEntity extends NMSEntity implements TapLivingEntity
{

    protected final EntityLiving living;

    NMSLivingEntity(Entity entity)
    {
        super(entity);

        this.living = (EntityLiving) entity;
    }

    @Override
    public EntityLiving getHandle()
    {
        return living;
    }

    @Override
    public LivingEntity getBukkitEntity()
    {
        return (LivingEntity) super.getBukkitEntity();
    }

    @Override
    public float getEyeHeight()
    {
        return this.living.getHeadHeight();
    }

    @Override
    public double getHealth()
    {
        return this.living.getHealth();
    }

    @Override
    public NMSItemStack getEquipment(EquipmentSlot slot)
    {
        return NMSItemSupport.wrapItemStack(living.getEquipment(NMSEquipmentSlot.toNMS(slot)));
    }

    @Override
    public void setEquipment(EquipmentSlot slot, TapItemStack item)
    {
        living.setSlot(NMSEquipmentSlot.toNMS(slot), NMSItemSupport.unwrapItemStack(item));
    }

}
