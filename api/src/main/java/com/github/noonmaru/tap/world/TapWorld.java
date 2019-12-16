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

package com.github.noonmaru.tap.world;

import com.github.noonmaru.tap.block.TapBlockData;
import com.github.noonmaru.tap.nbt.NBTCompound;

public interface TapWorld
{

    org.bukkit.World getWorld();

    TapChunk getChunk(int x, int z);

    TapBlockData getBlock(int x, int y, int z);

    default boolean setBlock(int x, int y, int z, TapBlockData blockData)
    {
        return setBlock(x, y, z, blockData, true);
    }

    boolean setBlock(int x, int y, int z, TapBlockData block, boolean applyPhysics);

    NBTCompound saveToSchematic(int x, int y, int z, int width, int height, int length);

    void loadFromSchematic(int x, int y, int z, NBTCompound schematic);

}
