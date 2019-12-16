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

package com.github.noonmaru.tap.v1_12_R1.block;

import com.github.noonmaru.tap.block.TapBlockSupport;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

import java.util.IdentityHashMap;
import java.util.Map;

public final class NMSBlockSupport implements TapBlockSupport
{

    private static NMSBlockSupport instance;

    private final Map<Block, NMSBlock> blocks = new IdentityHashMap<>(512);

    private final Map<IBlockData, NMSBlockData> blockDatas = new IdentityHashMap<>(4096);

    public NMSBlockSupport()
    {
        if (instance != null)
            throw new IllegalStateException();

        instance = this;
    }

    public static NMSBlockSupport getInstance()
    {
        return instance;
    }

    @Override
    public NMSBlock getBlock(int id)
    {
        return wrapBlock(Block.getById(id));
    }

    @Override
    public NMSBlock getBlock(String name)
    {
        return wrapBlock(Block.getByName(name));
    }

    @Override
    public NMSBlockData getBlockData(org.bukkit.World world, int x, int y, int z)
    {
        World nmsWorld = ((CraftWorld) world).getHandle();
        IBlockData blockData = nmsWorld.getType(new BlockPosition(x, y, z));

        return blockData == null ? null : wrapBlockData(blockData);
    }

    public NMSBlock wrapBlock(Block block)
    {
        if (block == null)
            throw new NullPointerException("Block data cannot be null");

        NMSBlock nmsBlock = blocks.get(block);

        if (nmsBlock == null)
            blocks.put(block, nmsBlock = new NMSBlock(block));

        return nmsBlock;
    }

    public NMSBlockData wrapBlockData(IBlockData blockData)
    {
        if (blockData == null)
            throw new NullPointerException("Block data cannot be null");

        NMSBlockData nmsBlockData = blockDatas.get(blockData);

        if (nmsBlockData == null)
            blockDatas.put(blockData, nmsBlockData = new NMSBlockData(blockData));

        return nmsBlockData;
    }

}
