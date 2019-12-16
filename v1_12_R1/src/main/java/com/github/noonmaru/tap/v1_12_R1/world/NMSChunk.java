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

import com.github.noonmaru.tap.block.TapBlockData;
import com.github.noonmaru.tap.v1_12_R1.block.NMSBlockSupport;
import com.github.noonmaru.tap.world.TapChunk;
import com.github.noonmaru.tap.world.TapWorld;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.WorldServer;

import java.lang.ref.WeakReference;

public final class NMSChunk implements TapChunk
{

    private final WorldServer world;

    private final int x, z;

    private TapWorld tapWorld;

    private WeakReference<Chunk> weakChunk;

    NMSChunk(Chunk chunk)
    {
        this.weakChunk = new WeakReference<>(chunk);
        this.world = (WorldServer) chunk.getWorld();
        this.x = chunk.locX;
        this.z = chunk.locZ;
    }

    public Chunk getHandle()
    {
        Chunk chunk = this.weakChunk.get();

        if (chunk == null)
        {
            chunk = this.world.getChunkAt(this.x, this.z);
            this.weakChunk = new WeakReference<>(chunk);
        }

        return chunk;
    }

    @Override
    public TapWorld getWorld()
    {
        TapWorld world = this.tapWorld;

        return world == null ? this.tapWorld = NMSWorldSupport.getInstance().wrapWorld(this.world) : world;
    }

    @Override
    public int getX()
    {
        return this.x;
    }

    @Override
    public int getZ()
    {
        return this.z;
    }

    @Override
    public TapBlockData getBlockData(int x, int y, int z)
    {
        return NMSBlockSupport.getInstance().wrapBlockData(getHandle().getBlockData(new BlockPosition(x, y, z)));
    }

}
