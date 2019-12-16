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

import net.minecraft.server.v1_12_R1.EnumItemSlot;
import org.bukkit.inventory.EquipmentSlot;

public final class NMSEquipmentSlot
{

    private static final EnumItemSlot[] EQUIPMENT_SLOTS;

    static
    {
        EnumItemSlot[] slots = new EnumItemSlot[EnumItemSlot.values().length];
        slots[EquipmentSlot.CHEST.ordinal()] = EnumItemSlot.CHEST;
        slots[EquipmentSlot.FEET.ordinal()] = EnumItemSlot.FEET;
        slots[EquipmentSlot.HEAD.ordinal()] = EnumItemSlot.HEAD;
        slots[EquipmentSlot.LEGS.ordinal()] = EnumItemSlot.LEGS;
        slots[EquipmentSlot.HAND.ordinal()] = EnumItemSlot.MAINHAND;
        slots[EquipmentSlot.OFF_HAND.ordinal()] = EnumItemSlot.OFFHAND;
        EQUIPMENT_SLOTS = slots;
    }

    private NMSEquipmentSlot() {}

    public static EnumItemSlot toNMS(EquipmentSlot slot)
    {
        return EQUIPMENT_SLOTS[slot.ordinal()];
    }
}
