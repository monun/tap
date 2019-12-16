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

package com.github.noonmaru.tap.v1_12_R1.world;

import com.github.noonmaru.tap.Tap;
import com.github.noonmaru.tap.world.WorldSupport;
import com.google.common.base.Suppliers;
import com.google.common.collect.MapMaker;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.World;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

import java.util.Map;
import java.util.function.Supplier;

public final class NMSWorldSupport implements WorldSupport
{

    private static final Supplier<NMSWorldSupport> instance = Suppliers.memoize(() -> (NMSWorldSupport) Tap.WORLD);

    private final Map<World, NMSWorld> worlds = new MapMaker().weakKeys().weakValues().makeMap();

    private final Map<Chunk, NMSChunk> chunks = new MapMaker().weakKeys().weakValues().makeMap();

    public static NMSWorldSupport getInstance()
    {
        return instance.get();
    }

    @Override
    public NMSWorld fromWorld(org.bukkit.World world)
    {
        return wrapWorld(((CraftWorld) world).getHandle());
    }

    public NMSWorld wrapWorld(WorldServer world)
    {
        NMSWorld nmsWorld = worlds.get(world);

        if (nmsWorld == null)
            worlds.put(world, nmsWorld = new NMSWorld(world));

        return nmsWorld;
    }

    @Override
    public NMSChunk fromChunk(org.bukkit.Chunk chunk)
    {
        return wrapChunk(((CraftChunk) chunk).getHandle());
    }

    public NMSChunk wrapChunk(Chunk chunk)
    {
        NMSChunk nmsChunk = chunks.get(chunk);

        if (nmsChunk == null)
        {
            nmsChunk = new NMSChunk(chunk);
            chunks.put(chunk, nmsChunk);
        }

        return nmsChunk;
    }

}
