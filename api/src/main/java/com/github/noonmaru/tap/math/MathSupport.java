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

import java.util.function.Predicate;

public interface MathSupport
{

    BoundingBox getBoundingBox(Entity entity);

    BoundingBox newBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);

    RayTraceResult rayTraceBlock(World world, Vector from, Vector to, int options);

    RayTraceResult rayTraceEntity(World world, Entity entity, Vector from, Vector to, double expand, Predicate<Entity> selector);

    RayTraceResult rayTrace(World world, Entity entity, Vector from, Vector to, int options, double expand, Predicate<Entity> selector);

    RayTracer newRayTraceCalculator(Vector from, Vector to);

}
