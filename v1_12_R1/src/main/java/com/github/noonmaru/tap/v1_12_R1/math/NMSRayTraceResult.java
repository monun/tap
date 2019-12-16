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

package com.github.noonmaru.tap.v1_12_R1.math;

import com.github.noonmaru.tap.math.BlockPoint;
import com.github.noonmaru.tap.math.RayTraceResult;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EnumDirection;
import net.minecraft.server.v1_12_R1.MovingObjectPosition;
import org.bukkit.block.BlockFace;

public class NMSRayTraceResult implements RayTraceResult
{

    private final MovingObjectPosition result;

    public NMSRayTraceResult(MovingObjectPosition result)
    {
        this.result = result;
    }

    public MovingObjectPosition getHandle()
    {
        return result;
    }

    @Override
    public double getX()
    {
        return result.pos.x;
    }

    @Override
    public double getY()
    {
        return result.pos.y;
    }

    @Override
    public double getZ()
    {
        return result.pos.z;
    }

    @Override
    public BlockPoint getBlockPoint()
    {
        BlockPosition pos = result.a();

        return pos == null ? null : new BlockPoint(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public BlockFace getFace()
    {
        EnumDirection direction = this.result.direction;

        if (direction == null)
            return null;

        switch (direction)
        {
            case DOWN:
                return BlockFace.DOWN;
            case EAST:
                return BlockFace.EAST;
            case NORTH:
                return BlockFace.NORTH;
            case SOUTH:
                return BlockFace.SOUTH;
            case UP:
                return BlockFace.UP;
            case WEST:
                return BlockFace.WEST;
            default:
                return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends org.bukkit.entity.Entity> T getEntity()
    {
        Entity entity = result.entity;

        return (T) (entity == null ? null : entity.getBukkitEntity());
    }

    @Override
    public <T> T getCustom()
    {
        return null;
    }

}
