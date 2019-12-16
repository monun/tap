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

package com.github.noonmaru.tap.v1_12_R1.nbt;

import com.github.noonmaru.tap.nbt.NBTCompound;
import com.github.noonmaru.tap.nbt.NBTList;
import net.minecraft.server.v1_12_R1.*;

import java.io.IOException;
import java.io.OutputStream;

public final class NMSNBTCompound implements NBTCompound
{

    private final NBTTagCompound compound;

    public NMSNBTCompound(NBTTagCompound compound)
    {
        this.compound = compound;
    }

    public NBTTagCompound getHandle()
    {
        return compound;
    }

    @SuppressWarnings("unchecked")
    private <T extends NBTBase> T get(String name)
    {
        return (T) this.compound.get(name);
    }

    private void set(String name, NBTBase value)
    {
        this.compound.set(name, value);
    }

    @Override
    public byte getByte(String name)
    {
        NBTTagByte value = get(name);

        return value == null ? (byte) 0 : value.g();
    }

    @Override
    public void setByte(String name, byte value)
    {
        set(name, new NBTTagByte(value));
    }

    @Override
    public byte[] getByteArray(String name)
    {
        NBTTagByteArray value = get(name);

        return value == null ? null : value.c();
    }

    @Override
    public void setByteArray(String name, byte[] value)
    {
        set(name, new NBTTagByteArray(value));
    }

    @Override
    public short getShort(String name)
    {
        NBTTagShort value = get(name);

        return value == null ? (short) 0 : value.f();
    }

    @Override
    public void setShort(String name, short value)
    {
        set(name, new NBTTagShort(value));
    }

    @Override
    public int getInt(String name)
    {
        NBTTagInt value = get(name);

        return value == null ? 0 : value.e();
    }

    @Override
    public void setInt(String name, int value)
    {
        set(name, new NBTTagInt(value));
    }

    @Override
    public int[] getIntArray(String name)
    {
        NBTTagIntArray value = get(name);

        return value == null ? null : value.d();
    }

    @Override
    public void setIntArray(String name, int[] value)
    {
        set(name, new NBTTagIntArray(value));
    }

    @Override
    public long getLong(String name)
    {
        NBTTagLong value = get(name);

        return value == null ? 0L : value.d();
    }

    @Override
    public void setLong(String name, long value)
    {
        set(name, new NBTTagLong(value));
    }

    @Override
    public float getFloat(String name)
    {
        NBTTagFloat value = get(name);

        return value == null ? 0F : value.i();
    }

    @Override
    public void setFloat(String name, float value)
    {
        set(name, new NBTTagFloat(value));
    }

    @Override
    public double getDouble(String name)
    {
        NBTTagDouble value = get(name);

        return value == null ? 0F : value.asDouble();
    }

    @Override
    public void setDouble(String name, double value)
    {
        set(name, new NBTTagDouble(value));
    }

    @Override
    public String getString(String name)
    {
        NBTTagString value = get(name);

        return value == null ? null : value.c_();
    }

    @Override
    public void setString(String name, String value)
    {
        set(name, new NBTTagString(value));
    }

    @Override
    public NBTList getList(String name)
    {
        NBTTagList value = get(name);

        return value == null ? null : new NMSNBTList(value);
    }

    @Override
    public void setList(String name, NBTList list)
    {
        set(name, ((NMSNBTList) list).getHandle());
    }

    @Override
    public NBTCompound getCompound(String name)
    {
        NBTTagCompound value = get(name);

        return value == null ? null : new NMSNBTCompound(value);
    }

    @Override
    public void setCompound(String name, NBTCompound compound)
    {
        set(name, ((NMSNBTCompound) compound).compound);
    }

    @Override
    public boolean contains(String name)
    {
        return compound.hasKey(name);
    }

    @Override
    public boolean isEmpty()
    {
        return compound.isEmpty();
    }

    @Override
    public NBTCompound copy()
    {
        return new NMSNBTCompound((NBTTagCompound) this.compound.clone());
    }

    @Override
    public void remove(String name)
    {
        this.compound.remove(name);
    }


    @Override
    public void save(OutputStream out)
    {
        try
        {
            NBTCompressedStreamTools.a(this.compound, out);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public StringBuilder toJsonString(StringBuilder builder)
    {
        return NBTWriters.write(this.compound, builder);
    }

    @Override
    public int hashCode()
    {
        return this.compound.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof NMSNBTCompound && this.compound.equals(((NMSNBTCompound) obj).compound);
    }

}
