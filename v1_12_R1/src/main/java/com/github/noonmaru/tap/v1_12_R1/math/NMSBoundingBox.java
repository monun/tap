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

import com.github.noonmaru.math.Vector;
import com.github.noonmaru.tap.math.BoundingBox;
import com.google.common.collect.Lists;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;

import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public final class NMSBoundingBox implements BoundingBox
{

    private final AxisAlignedBB box;

    public NMSBoundingBox(AxisAlignedBB box)
    {
        this.box = box;
    }

    @Override
    public double getMaxX()
    {
        return box.d;
    }

    @Override
    public double getMaxY()
    {
        return box.e;
    }

    @Override
    public double getMaxZ()
    {
        return box.f;
    }

    @Override
    public double getMinX()
    {
        return box.a;
    }

    @Override
    public double getMinY()
    {
        return box.b;
    }

    @Override
    public double getMinZ()
    {
        return box.c;
    }


    @Override
    public NMSBoundingBox move(double x, double y, double z)
    {
        AxisAlignedBB box = this.box;

        return new NMSBoundingBox(new AxisAlignedBB(box.a + x, box.b + y, box.c + z, box.d + x, box.e + y, box.f + z));
    }

    @Override
    public NMSBoundingBox addCoord(double x, double y, double z)
    {
        return new NMSBoundingBox(this.box.b(x, y, z));
    }


    @Override
    public NMSBoundingBox expand(double x, double y, double z)
    {
        AxisAlignedBB box = this.box;

        return new NMSBoundingBox(new AxisAlignedBB(box.a - x, box.b - y, box.c - z, box.d + x, box.e + y, box.f + z));
    }

    @Override
    public NMSBoundingBox expand(double v)
    {
        return expand(v, v, v);
    }

    @Override
    public NMSBoundingBox contract(double x, double y, double z)
    {
        AxisAlignedBB box = this.box;

        return new NMSBoundingBox(new AxisAlignedBB(box.a + x, box.b + y, box.c + z, box.d - x, box.e - y, box.f - z));
    }

    @Override
    public NMSBoundingBox contract(double v)
    {
        return contract(v, v, v);
    }

    @Override
    public boolean intersectWith(BoundingBox box)
    {
        return this.box.c(((NMSBoundingBox) box).box);
    }

    @Override
    public boolean intersectWith(org.bukkit.entity.Entity entity)
    {
        return box.c(((CraftEntity) entity).getHandle().getBoundingBox());
    }

    @Override
    public boolean isInside(double x, double y, double z)
    {
        AxisAlignedBB box = this.box;

        if (x <= box.a || x >= box.d)
            return false;

        if (y <= box.b || y >= box.e)
            return false;

        return !(z <= box.c) && !(z >= box.f);
    }

    @Override
    public <T extends org.bukkit.entity.Entity> List<T> getEntities(org.bukkit.World world, org.bukkit.entity.Entity exclude, Predicate<org.bukkit.entity.Entity> selector)
    {
        World nmsWorld = ((CraftWorld) world).getHandle();

        return (List<T>) Lists.transform(NMSMathSupport.getEntitiesInBox(nmsWorld, NMSMathSupport.unwrapEntity(exclude), box, NMSMathSupport.toNMSSelector(selector)), Entity::getBukkitEntity);
    }

    public MovingObjectPosition calculateRayTrace(Vec3D from, Vec3D to)
    {
        MovingObjectPosition hit = box.b(from, to);

        if (hit == null && box.b(from))
            hit = new MovingObjectPosition(from, null);

        return hit;
    }

    @Override
    public NMSRayTraceResult calculateRayTrace(Vector from, Vector to)
    {
        return NMSMathSupport.wrapResult(calculateRayTrace(NMSMathSupport.toVec3D(from), NMSMathSupport.toVec3D(to)));
    }

    @Override
    public NMSBoundingBox copy()
    {
        AxisAlignedBB box = this.box;

        return new NMSBoundingBox(new AxisAlignedBB(box.a, box.b, box.c, box.d, box.e, box.f));
    }

    public AxisAlignedBB getHandle()
    {
        return box;
    }

    @Override
    public String toString()
    {
        return box.toString();
    }

}
