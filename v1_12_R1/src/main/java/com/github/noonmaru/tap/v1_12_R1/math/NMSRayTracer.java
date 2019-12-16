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
import com.github.noonmaru.tap.math.RayTraceResult;
import com.github.noonmaru.tap.math.RayTracer;
import com.google.common.collect.Lists;
import net.minecraft.server.v1_12_R1.AxisAlignedBB;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.MovingObjectPosition;
import net.minecraft.server.v1_12_R1.Vec3D;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.noonmaru.tap.v1_12_R1.math.NMSMathSupport.*;


@SuppressWarnings({"unchecked", "StaticPseudoFunctionalStyleMethod", "ConstantConditions", "Convert2MethodRef", "Guava"})
public final class NMSRayTracer implements RayTracer
{

    private Vec3D from, to;

    NMSRayTracer(Vec3D from, Vec3D to)
    {
        this.from = from;
        this.to = to;
    }

    @Override
    public Vector getFrom(Vector v)
    {
        return copyToVector(from, v);
    }

    @Override
    public Vector getFrom()
    {
        return toVector(from);
    }

    @Override
    public NMSRayTracer setFrom(Vector v)
    {
        from = toVec3D(v);

        return this;
    }

    @Override
    public NMSRayTracer setFrom(double x, double y, double z)
    {
        from = new Vec3D(x, y, z);

        return this;
    }

    @Override
    public Vector getTo(Vector v)
    {
        return copyToVector(to, v);
    }

    @Override
    public Vector getTo()
    {
        return toVector(to);
    }

    @Override
    public NMSRayTracer setTo(Vector v)
    {
        to = toVec3D(v);

        return this;
    }

    @Override
    public NMSRayTracer setTo(double x, double y, double z)
    {
        to = new Vec3D(x, y, z);

        return this;
    }

    @Override
    public NMSRayTraceResult setToRayTraceBlock(org.bukkit.World world, int option)
    {
        NMSRayTraceResult result = rayTraceBlock(world, option);

        if (result != null)
        {
            Vec3D pos = result.getHandle().pos;
            setTo(pos.x, pos.y, pos.z);
        }

        return result;
    }

    @Override
    public double getLength()
    {
        Vec3D from = this.from, to = this.to;

        double x = from.x - to.x;
        double y = from.y - to.y;
        double z = from.z - to.z;

        return Math.sqrt(x * x + y * y + z * z);
    }

    @Override
    public NMSBoundingBox toBox()
    {
        return new NMSBoundingBox(new AxisAlignedBB(from.x, from.y, from.z, to.x, to.y, to.z));
    }

    @Override
    public <T extends org.bukkit.entity.Entity> List<T> getEntitiesInBox(org.bukkit.World world, org.bukkit.entity.Entity exclusion, Predicate<org.bukkit.entity.Entity> selector)
    {
        Vec3D from = this.from, to = this.to;
        List<Entity> entities = NMSMathSupport.getEntitiesInBox(((CraftWorld) world).getHandle(), unwrapEntity(exclusion), new AxisAlignedBB(from.x, from.y, from.z, to.x, to.y, to.z), toNMSSelector(selector));

        return (List<T>) Lists.transform(entities, entity -> entity.getBukkitEntity());
    }

    @Override
    public NMSRayTraceResult rayTraceBlock(org.bukkit.World world, int option)
    {
        return wrapResult(NMSMathSupport.rayTraceBlock(((CraftWorld) world).getHandle(), from, to, option));
    }

    @Override
    public NMSRayTraceResult calculate(BoundingBox box)
    {
        return wrapResult(((NMSBoundingBox) box).getHandle().b(from, to));
    }

    @Override
    public NMSRayTraceResult calculate(org.bukkit.entity.Entity entity, double expand)
    {
        return wrapResult(((CraftEntity) entity).getHandle().getBoundingBox().grow(expand, expand, expand).b(from, to));
    }

    private List<Entity> getNMSEntities(org.bukkit.World world, org.bukkit.entity.Entity exclusion, double expand, com.google.common.base.Predicate<Entity> selector)
    {
        return NMSMathSupport.getEntitiesInBox(((CraftWorld) world).getHandle(), unwrapEntity(exclusion), new AxisAlignedBB(from.x, from.y, from.z, to.x, to.y, to.z).grow(expand, expand, expand), selector);
    }

    @Override
    public NMSRayTraceResult rayTraceEntity(org.bukkit.World world, org.bukkit.entity.Entity exclusion, double expand, Predicate<org.bukkit.entity.Entity> selector)
    {
        List<Entity> entities = getNMSEntities(world, exclusion, expand, toNMSSelector(selector));

        return wrapResult(calculateRayTrace(entities, from, to, expand));
    }

    @Override
    public List<NMSRayTraceResult> rayTraceEntities(org.bukkit.World world, org.bukkit.entity.Entity exclusion, double expand, Predicate<org.bukkit.entity.Entity> selector)
    {
        List<Entity> entities = getNMSEntities(world, exclusion, expand, toNMSSelector(selector));

        ArrayList<NMSRayTraceResult> results = new ArrayList<>(Math.min(10, entities.size()));
        Vec3D from = this.from, to = this.to;

        for (Entity entity : entities)
        {
            AxisAlignedBB box = entity.getBoundingBox().grow(expand, expand, expand);
            MovingObjectPosition result = box.b(from, to);

            if (result != null)
            {
                result.entity = entity;
                results.add(new NMSRayTraceResult(result));
            }
            else if (box.b(from))
            {
                result = new MovingObjectPosition(from, null);
                result.entity = entity;
                results.add(new NMSRayTraceResult(result));
            }
        }

        return results;
    }

    @Override
    public <T> RayTraceResult rayTraceCustom(Iterable<? extends T> iterable, T exclusion, Function<T, BoundingBox> func)
    {
        T found = null;
        MovingObjectPosition foundResult = null;
        double ds = 0.0D;

        for (T object : iterable)
        {
            if (object == exclusion)
                continue;

            MovingObjectPosition result = ((NMSBoundingBox) func.apply(object)).calculateRayTrace(from, to);

            if (result != null)
            {
                double cds = from.distanceSquared(result.pos);

                if (ds == 0.0D || cds < ds)
                {
                    found = object;
                    foundResult = result;
                    ds = cds;
                }
            }
        }

        return found == null ? null : new NMSRayTraceResultCustom(foundResult, found);
    }

}
