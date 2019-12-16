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

package com.github.noonmaru.tap.v1_12_R1.packet;

import com.github.noonmaru.tap.AnimationType;
import com.github.noonmaru.tap.item.TapItemStack;
import com.github.noonmaru.tap.packet.EntityPacket;
import com.github.noonmaru.tap.packet.Packet;
import com.github.noonmaru.tap.v1_12_R1.entity.NMSEquipmentSlot;
import com.github.noonmaru.tap.v1_12_R1.item.NMSItemStack;
import net.minecraft.server.v1_12_R1.*;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntity.PacketPlayOutRelEntityMove;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;

public class NMSEntityPacket implements EntityPacket
{

    @Override
    public NMSPacket animation(org.bukkit.entity.Entity entity, AnimationType animation)
    {
        return new NMSPacketFixed(new PacketPlayOutAnimation(((CraftEntity) entity).getHandle(), animation.getId()));
    }

    @Override
    public NMSPacket destroy(int... entityIds)
    {
        return new NMSPacketFixed(new PacketPlayOutEntityDestroy(entityIds));
    }

    @Override
    public NMSPacket equipment(int entityId, EquipmentSlot slot, TapItemStack item)
    {
        return new NMSPacketFixed(new PacketPlayOutEntityEquipment(entityId, NMSEquipmentSlot.toNMS(slot), item == null ? ItemStack.a : ((NMSItemStack) item).getHandle()));
    }

    @Override
    public NMSPacket headRotation(org.bukkit.entity.Entity entity, float yaw)
    {
        return new NMSPacketFixed(new PacketPlayOutEntityHeadRotation(((CraftEntity) entity).getHandle(), (byte) (yaw * 255.0D / 360D)));
    }

    @Override
    public NMSPacket metadata(org.bukkit.entity.Entity entity)
    {
        Entity nmsEntity = ((CraftEntity) entity).getHandle();

        return new NMSPacketFixed(new PacketPlayOutEntityMetadata(nmsEntity.getId(), nmsEntity.getDataWatcher(), true));
    }

    @Override
    public NMSPacket relativeMove(int entityId, double moveX, double moveY, double moveZ, boolean onGround)
    {
        return new NMSPacketFixed(new PacketPlayOutRelEntityMove(entityId, (long) (moveX * 4096), (long) (moveY * 4096), (long) (moveZ * 4096), onGround));
    }

    @Override
    public Packet relativeMoveLook(int entityId, double moveX, double moveY, double moveZ, float yaw, float pitch, boolean onGround)
    {
        return new NMSPacketFixed(new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(entityId, (long) (moveX * 4096), (long) (moveY * 4096), (long) (moveZ * 4096), (byte) (yaw * 255.0D / 360.0D), (byte) (pitch * 255.0D / 360.0D), onGround));
    }

    @Override
    public NMSPacket spawnMob(LivingEntity entity)
    {
        return new NMSPacketFixed(new PacketPlayOutSpawnEntityLiving(((CraftLivingEntity) entity).getHandle()));
    }

    @Override
    public NMSPacket teleport(org.bukkit.entity.Entity entity, double x, double y, double z, float yaw, float pitch, boolean onGround)
    {
        Entity nmsEntity = ((CraftEntity) entity).getHandle();
        nmsEntity.locX = x;
        nmsEntity.locY = y;
        nmsEntity.locZ = z;
        nmsEntity.yaw = yaw;
        nmsEntity.pitch = pitch;
        nmsEntity.onGround = onGround;

        return new NMSPacketFixed(new PacketPlayOutEntityTeleport(nmsEntity));
    }

}
