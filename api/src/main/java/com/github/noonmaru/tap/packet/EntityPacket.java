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

package com.github.noonmaru.tap.packet;

import com.github.noonmaru.tap.AnimationType;
import com.github.noonmaru.tap.item.TapItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;

public interface EntityPacket
{

    Packet animation(Entity entity, AnimationType animation);

    Packet destroy(int... entityIds);

    Packet equipment(int entityId, EquipmentSlot slot, TapItemStack item);

    Packet headRotation(Entity entity, float yaw);

    Packet metadata(Entity entity);

    Packet relativeMove(int entityId, double moveX, double moveY, double moveZ, boolean onGround);

    Packet relativeMoveLook(int entityId, double moveX, double moveY, double moveZ, float yaw, float pitch, boolean onGround);

    Packet spawnMob(LivingEntity entity);

    Packet teleport(Entity entity, double x, double y, double z, float yaw, float pitch, boolean onGround);

}
