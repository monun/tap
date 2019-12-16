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

import com.github.noonmaru.tap.block.TapBlock;
import com.github.noonmaru.tap.block.TapBlockData;
import com.google.common.collect.ImmutableList;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.IBlockData;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public final class NMSBlock implements TapBlock
{

    private final Block block;

    private List<TapBlockData> blockDataList;

    NMSBlock(Block block)
    {
        this.block = block;
    }

    public Block getHandle()
    {
        return block;
    }

    @Override
    public String getTextureId()
    {
        return Block.REGISTRY.b(block).toString();
    }

    @Override
    public int getId()
    {
        return Block.getId(block);
    }

    @Override
    public TapBlockData getBlockData()
    {
        return NMSBlockSupport.getInstance().wrapBlockData(block.getBlockData());
    }

    @Override
    public NMSBlockData getBlockData(int data)
    {
        return NMSBlockSupport.getInstance().wrapBlockData(block.fromLegacyData(data));
    }

    @Override
    public List<TapBlockData> getBlockDataList()
    {
        List<TapBlockData> list = blockDataList;

        if (list != null)
            return list;

        NMSBlockSupport blockSupport = NMSBlockSupport.getInstance();
        List<IBlockData> nmsList = block.s().a();
        List<NMSBlockData> tapList = new ArrayList<>(nmsList.size());

        for (IBlockData data : nmsList)
            tapList.add(blockSupport.wrapBlockData(data));

        return blockDataList = ImmutableList.copyOf(tapList);
    }
}
