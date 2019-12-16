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

package com.github.noonmaru.tap.math;

import com.github.noonmaru.math.Vector;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface RayTracer
{
    Vector getFrom(Vector v);

    Vector getFrom();

    RayTracer setFrom(Vector v);

    RayTracer setFrom(double x, double y, double z);

    Vector getTo(Vector v);

    Vector getTo();

    RayTracer setTo(Vector v);

    RayTracer setTo(double x, double y, double z);

    RayTraceResult setToRayTraceBlock(World world, int option);

    double getLength();

    BoundingBox toBox();

    <T extends Entity> List<T> getEntitiesInBox(World world, Entity exclusion, Predicate<Entity> selector);

    RayTraceResult rayTraceBlock(World world, int option);

    RayTraceResult calculate(BoundingBox box);

    RayTraceResult calculate(Entity entity, double expand);

    RayTraceResult rayTraceEntity(World world, Entity exclusion, double expand, Predicate<Entity> selector);

    List<? extends RayTraceResult> rayTraceEntities(World world, Entity exclusion, double expand, Predicate<Entity> selector);

    <T> RayTraceResult rayTraceCustom(Iterable<? extends T> iterable, T exclusion, Function<T, BoundingBox> func);

}
