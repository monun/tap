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
import com.github.noonmaru.tap.math.MathSupport;
import com.github.noonmaru.tap.math.RayTraceOption;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;

import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings({"Guava", "ConstantConditions"})
public final class NMSMathSupport implements MathSupport
{

    private static NMSMathSupport instance;

    public NMSMathSupport()
    {
        if (instance != null)
            throw new IllegalStateException();

        instance = this;
    }

    public static NMSMathSupport getInstance()
    {
        return instance;
    }

    public static Vec3D toVec3D(Vector v)
    {
        if (v == null)
            throw new NullPointerException("Vector cannot be null");

        return new Vec3D(v.x, v.y, v.z);
    }

    public static Vector toVector(Vec3D v)
    {
        return new Vector(v.x, v.y, v.z);
    }

    public static Vector copyToVector(Vec3D from, Vector to)
    {
        to.x = from.x;
        to.y = from.y;
        to.z = from.z;

        return to;
    }

    public static NMSBoundingBox wrapBox(AxisAlignedBB box)
    {
        return box == null ? null : new NMSBoundingBox(box);
    }

    public static NMSRayTraceResult wrapResult(MovingObjectPosition result)
    {
        return result == null ? null : new NMSRayTraceResult(result);
    }

    public static MovingObjectPosition rayTraceBlock(World world, Vec3D from, Vec3D to, int options)
    {
        return world.rayTrace(from, to, RayTraceOption.is(options, RayTraceOption.STOP_ON_LIQUID), RayTraceOption.is(options, RayTraceOption.IGNORE_BLOCK_WITHOUT_BOUNDING_BOX), RayTraceOption.is(options, RayTraceOption.RETURN_LAST_UNCOLLIDABLE_BLOCK));
    }

    public static List<Entity> getEntitiesInBox(World world, Entity entity, AxisAlignedBB box, com.google.common.base.Predicate<Entity> selector)
    {
        return world.getEntities(entity, box, selector);
    }

    public static MovingObjectPosition calculateRayTrace(List<Entity> entities, Vec3D from, Vec3D to, double expand)
    {
        double ds = 0D;
        MovingObjectPosition result = null;

        for (Entity entity : entities)
        {
            AxisAlignedBB box = entity.getBoundingBox().grow(expand, expand, expand);
            MovingObjectPosition currentResult = box.b(from, to);

            if (currentResult != null)
            {
                double cds = from.distanceSquared(currentResult.pos);

                if (cds < ds || ds == 0D)
                {
                    ds = cds;
                    currentResult.entity = entity;
                    result = currentResult;
                }
            }
            else if (box.b(from))
            {
                result = new MovingObjectPosition(from, null);
                result.entity = entity;
                break;
            }
        }

        return result;
    }

    public static MovingObjectPosition calculateRayTrace(World world, Entity exclusion, Vec3D from, Vec3D to, int options, double expand, com.google.common.base.Predicate<Entity> selector)
    {
        MovingObjectPosition blockHit = rayTraceBlock(world, from, to, options);

        if (blockHit != null)
            to = blockHit.pos;

        List<Entity> entities = getEntitiesInBox(world, exclusion, new AxisAlignedBB(from.x, from.y, from.z, to.x, to.y, to.z).grow(expand, expand, expand), selector);

        MovingObjectPosition entityHit = calculateRayTrace(entities, from, to, expand);

        return entityHit != null ? entityHit : blockHit;
    }

    static Entity unwrapEntity(org.bukkit.entity.Entity entity)
    {
        return entity == null ? null : ((CraftEntity) entity).getHandle();
    }

    public static com.google.common.base.Predicate<Entity> toNMSSelector(Predicate<org.bukkit.entity.Entity> selector)
    {
        return (entity -> selector.test(entity.getBukkitEntity()));
    }

    @Override
    public NMSBoundingBox getBoundingBox(org.bukkit.entity.Entity entity)
    {
        return new NMSBoundingBox(((CraftEntity) entity).getHandle().getBoundingBox());
    }

    @Override
    public NMSBoundingBox newBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
    {
        return new NMSBoundingBox(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
    }

    @Override
    public NMSRayTraceResult rayTraceBlock(org.bukkit.World world, Vector from, Vector to, int options)
    {
        return wrapResult(rayTraceBlock(((CraftWorld) world).getHandle(), toVec3D(from), toVec3D(to), options));
    }

    @Override
    public NMSRayTraceResult rayTraceEntity(org.bukkit.World world, org.bukkit.entity.Entity exclusion, Vector from, Vector to, double expand, Predicate<org.bukkit.entity.Entity> selector)
    {
        World nmsWorld = ((CraftWorld) world).getHandle();
        Entity nmsExclusion = unwrapEntity(exclusion);
        AxisAlignedBB nmsBox = new AxisAlignedBB(from.x, from.y, from.z, to.x, to.y, to.z).grow(expand, expand, expand);

        return wrapResult(calculateRayTrace(getEntitiesInBox(nmsWorld, nmsExclusion, nmsBox, toNMSSelector(selector)), toVec3D(from), toVec3D(to), expand));
    }

    @Override
    public NMSRayTraceResult rayTrace(org.bukkit.World world, org.bukkit.entity.Entity exclusion, Vector from, Vector to, int options, double expand, Predicate<org.bukkit.entity.Entity> selector)
    {
        World nmsWorld = ((CraftWorld) world).getHandle();
        Entity nmsExclusion = unwrapEntity(exclusion);

        return wrapResult(calculateRayTrace(nmsWorld, nmsExclusion, toVec3D(from), toVec3D(to), options, expand, toNMSSelector(selector)));
    }

    @Override
    public NMSRayTracer newRayTraceCalculator(Vector from, Vector to)
    {
        return new NMSRayTracer(toVec3D(from), toVec3D(to));
    }

}
