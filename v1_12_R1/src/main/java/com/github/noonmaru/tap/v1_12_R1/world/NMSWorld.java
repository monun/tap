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
import com.github.noonmaru.tap.nbt.NBTCompound;
import com.github.noonmaru.tap.v1_12_R1.block.NMSBlockData;
import com.github.noonmaru.tap.v1_12_R1.block.NMSBlockSupport;
import com.github.noonmaru.tap.v1_12_R1.nbt.NMSNBTCompound;
import com.github.noonmaru.tap.world.TapWorld;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.ref.WeakReference;

@SuppressWarnings("deprecation")
public final class NMSWorld implements TapWorld
{

    private static final String WIDTH = "Width";

    private static final String HEIGHT = "Height";

    private static final String LENGTH = "Length";

    private static final String BLOCKS = "Blocks";

    private static final String DATA = "Data";

    private static final String ENTITIES = "Entities";

    private static final String TILE_ENTITIES = "TileEntities";

    public final WeakReference<WorldServer> world;

    NMSWorld(WorldServer world)
    {
        this.world = new WeakReference<>(world);
    }

    protected static int size(int width, int height, int length)
    {
        long size = (long) width * height * length;

        if (size > Integer.MAX_VALUE)
            throw new IllegalArgumentException("schematic size is too large!");

        return (int) size;
    }

    public WorldServer getHandle()
    {
        return world.get();
    }

    @Override
    public CraftWorld getWorld()
    {
        return getHandle().getWorld();
    }

    @Override
    public NMSChunk getChunk(int x, int z)
    {
        return NMSWorldSupport.getInstance().wrapChunk(getHandle().getChunkAt(x, z));
    }

    @Override
    public NMSBlockData getBlock(int x, int y, int z)
    {
        return NMSBlockSupport.getInstance().wrapBlockData(getHandle().getType(new BlockPosition(x, y, z)));
    }

    @Override
    public boolean setBlock(int x, int y, int z, TapBlockData block, boolean applyPhysics)
    {
        BlockPosition position = new BlockPosition(x, y, z);
        IBlockData blockData = ((NMSBlockData) block).getBlockData();
        WorldServer world = getHandle();

        if (applyPhysics)
        {
            world.setTypeAndData(position, blockData, 3);
        }
        else
        {
            IBlockData old = world.getType(position);
            boolean success = world.setTypeAndData(position, blockData, 18);

            if (success)
                world.notify(position, old, blockData, 3);

            return success;
        }

        return true;
    }

    @Override
    public NBTCompound saveToSchematic(int x, int y, int z, int width, int height, int length)
    {
        if (width < 1)
            throw new IllegalArgumentException("width must be greater than 0");
        if (width > 32767)
            throw new IllegalArgumentException("width cannot be greater than 32767");
        if (height < 1)
            throw new IllegalArgumentException("height must be greater than 0");
        if (height > 32767)
            throw new IllegalArgumentException("height cannot be greater than 32767");
        if (y + height > 255)
            throw new IllegalArgumentException("maximum Y cannot be greater than 255");
        if (length < 1)
            throw new IllegalArgumentException("length must be greater than 0");
        if (length > 32767)
            throw new IllegalArgumentException("length cannot be greater than 32767");

        int size = size(width, height, length);
        byte[] blocks = new byte[size];
        byte[] data = new byte[size];

        WorldServer w = getHandle();

        for (int blockX = 0; blockX < width; blockX++)
        {
            for (int blockY = 0; blockY < height; blockY++)
            {
                for (int blockZ = 0; blockZ < length; blockZ++)
                {
                    IBlockData blockData = w.getType(new BlockPosition(x + blockX, y + blockY, z + blockZ));
                    Block block = blockData.getBlock();

                    int index = (blockY * length + blockZ) * width + blockX;

                    blocks[index] = (byte) Block.getId(block);
                    data[index] = (byte) block.toLegacyData(blockData);
                }
            }
        }

        NBTTagList entities = new NBTTagList();

        for (Entity entity : w.getEntities(null, new AxisAlignedBB(x, y, z, x + width, y + height, z + length)))
        {
            NBTTagCompound entityTag = new NBTTagCompound();

            if (entity.d(entityTag))
            {
                NBTTagList pos = new NBTTagList();
                pos.add(new NBTTagDouble(entity.locX - x));
                pos.add(new NBTTagDouble(entity.locY - y));
                pos.add(new NBTTagDouble(entity.locZ - z));
                entityTag.set("Pos", pos);

                entityTag.remove("UUID");
                entityTag.remove("UUIDMost");
                entityTag.remove("UUIDLeast");

                entities.add(entityTag);
            }
        }

        NBTTagList tileEntities = new NBTTagList();

        for (TileEntity tileEntity : w.getTileEntities(x, y, z, x + width, y + height, z + length))
        {
            NBTTagCompound tileEntityTag = new NBTTagCompound();
            tileEntity.save(tileEntityTag);

            BlockPosition pos = tileEntity.getPosition();
            tileEntityTag.setInt("x", pos.getX() - x);
            tileEntityTag.setInt("y", pos.getY() - y);
            tileEntityTag.setInt("z", pos.getZ() - z);

            tileEntities.add(tileEntityTag);
        }

        NBTTagCompound schematic = new NBTTagCompound();

        schematic.setShort(WIDTH, (short) width);
        schematic.setShort(HEIGHT, (short) height);
        schematic.setShort(LENGTH, (short) length);
        schematic.setString("Materials", "Alpha");
        schematic.setByteArray(BLOCKS, blocks);
        schematic.setByteArray(DATA, data);
        schematic.set(ENTITIES, entities);
        schematic.set(TILE_ENTITIES, tileEntities);

        return new NMSNBTCompound(schematic);
    }

    @Override
    public void loadFromSchematic(int x, int y, int z, NBTCompound schematic)
    {
        int width = schematic.getShort(WIDTH);
        int height = schematic.getShort(HEIGHT);
        int length = schematic.getShort(LENGTH);
        byte[] blocks = schematic.getByteArray(BLOCKS);
        byte[] data = schematic.getByteArray(DATA);
        int size = blocks.length;

        if (width * height * length != size || size != data.length)
            throw new IllegalArgumentException("mismatch schematic data size");

        WorldServer w = getHandle();

        for (int blockY = 0; blockY < height; blockY++)
        {
            for (int blockZ = 0; blockZ < length; blockZ++)
            {
                for (int blockX = 0; blockX < width; blockX++)
                {
                    int index = (blockY * length + blockZ) * width + blockX;

                    w.setTypeAndData(new BlockPosition(x + blockX, y + blockY, z + blockZ), Block.getById(blocks[index] & 0xFF).fromLegacyData(data[index] & 0xFF), 3);
                }
            }
        }

        NBTTagCompound tag = ((NMSNBTCompound) schematic).getHandle();
        NBTTagList entities = (NBTTagList) tag.get(ENTITIES);

        for (int i = 0, j = entities.size(); i < j; i++)
        {
            NBTTagCompound entityTag = entities.get(i);

            Entity entity = EntityTypes.a(entityTag, w);

            if (entity != null)
            {
                entity.lastX = entity.N = entity.locX += x;
                entity.lastY = entity.M = entity.locY += y;
                entity.lastZ = entity.O = entity.locZ += z;
                entity.setPosition(entity.locX, entity.locY, entity.locZ);

                w.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
            }
        }

        NBTTagList tileEntities = (NBTTagList) tag.get(TILE_ENTITIES);

        for (int i = 0, j = tileEntities.size(); i < j; i++)
        {
            NBTTagCompound tileEntityTag = tileEntities.get(i);
            TileEntity tileEntity = TileEntity.create(w, tileEntityTag);

            if (tileEntity != null)
            {
                BlockPosition pos = tileEntity.getPosition();

                tileEntity.setPosition(new BlockPosition(pos.getX() + x, pos.getY() + y, pos.getZ() + z));
                w.setTileEntity(tileEntity.getPosition(), tileEntity);
            }
        }
    }
}
