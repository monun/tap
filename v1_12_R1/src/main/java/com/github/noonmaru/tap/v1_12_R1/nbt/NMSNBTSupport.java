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
import com.github.noonmaru.tap.nbt.NBTSupport;
import net.minecraft.server.v1_12_R1.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class NMSNBTSupport implements NBTSupport
{
    @Override
    public NBTCompound loadCompound(InputStream in)
    {
        try
        {
            return new NMSNBTCompound(NBTCompressedStreamTools.a(in));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public NBTCompound loadCompound(byte[] bytes)
    {
        return loadCompound(new ByteArrayInputStream(bytes));
    }

    @Override
    public NMSNBTCompound newCompound()
    {
        return new NMSNBTCompound(new NBTTagCompound());
    }

    @Override
    public NBTList newList()
    {
        return new NMSNBTList(new NBTTagList());
    }

    @Override
    public NMSNBTCompound fromJsonString(String json)
    {
        try
        {
            return new NMSNBTCompound(MojangsonParser.parse(json));
        }
        catch (MojangsonParseException e)
        {
            throw new IllegalArgumentException(e);
        }
    }
}
