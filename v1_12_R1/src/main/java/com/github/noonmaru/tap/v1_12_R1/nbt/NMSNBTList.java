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

public final class NMSNBTList implements NBTList
{

    private final NBTTagList list;

    public NMSNBTList(NBTTagList list)
    {
        this.list = list;
    }

    public NBTTagList getHandle()
    {
        return list;
    }

    @Override
    public int[] getIntArray(int i)
    {
        return this.list.d(i);
    }

    @Override
    public void addIntArray(int[] value)
    {
        this.list.add(new NBTTagIntArray(value));
    }

    @Override
    public void addCompound(NBTCompound compound)
    {
        this.list.add(((NMSNBTCompound) compound).getHandle());
    }

    @Override
    public float getFloat(int i)
    {
        return this.list.g(i);
    }

    @Override
    public void addFloat(float value)
    {
        this.list.add(new NBTTagFloat(value));
    }

    @Override
    public double getDouble(int i)
    {
        return this.list.f(i);
    }

    @Override
    public void addDouble(double value)
    {
        this.list.add(new NBTTagDouble(value));
    }

    @Override
    public String getString(int i)
    {
        return this.list.getString(i);
    }

    @Override
    public void addString(String value)
    {
        this.list.add(new NBTTagString(value));
    }

    @Override
    public NMSNBTCompound getCompound(int i)
    {
        return new NMSNBTCompound(this.list.get(i));
    }

    @Override
    public int size()
    {
        return this.list.size();
    }

    @Override
    public int hashCode()
    {
        return this.list.hashCode();
    }

    @Override
    public String toString()
    {
        return this.list.toString();
    }

}
