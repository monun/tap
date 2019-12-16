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

import com.github.noonmaru.tap.nbt.NBT;
import net.minecraft.server.v1_12_R1.*;

import java.util.Iterator;

abstract class NBTWriters
{

    private static final NBTWriterCompound WRITER_COMPOUND = new NBTWriterCompound();

    private static final NBTWriterList WRITER_LIST = new NBTWriterList();

    private static final NBTWriter[] WRITERS;

    static
    {
        NBTWriter[] writers = new NBTWriter[12];

        writers[NBT.BYTE] = new NBTWriterByte();
        writers[NBT.SHORT] = new NBTWriterShort();
        writers[NBT.INT] = new NBTWriterInt();
        writers[NBT.LONG] = new NBTWriterLong();
        writers[NBT.FLOAT] = new NBTWriterFloat();
        writers[NBT.DOUBLE] = new NBTWriterDouble();
        writers[NBT.BYTE_ARRAY] = new NBTWriterByteArray();
        writers[NBT.INT_ARRAY] = new NBTWriterIntArray();
        writers[NBT.STRING] = new NBTWriterString();
        writers[NBT.LIST] = WRITER_LIST;
        writers[NBT.COMPOUND] = WRITER_COMPOUND;

        WRITERS = writers;
    }

    static StringBuilder write(NBTTagCompound compound, StringBuilder builder)
    {
        WRITER_COMPOUND.toString(compound, builder);

        return builder;
    }

    static StringBuilder write(NBTTagList list, StringBuilder builder)
    {
        WRITER_LIST.toString(list, builder);

        return builder;
    }

    private static abstract class NBTWriter
    {
        abstract void write(NBTBase nbt, StringBuilder builder);
    }

    private static final class NBTWriterByte extends NBTWriter
    {
        @Override
        void write(NBTBase nbt, StringBuilder builder)
        {
            builder.append(((NBTTagByte) nbt).g());
        }
    }

    private static final class NBTWriterShort extends NBTWriter
    {
        @Override
        void write(NBTBase nbt, StringBuilder builder)
        {
            builder.append(((NBTTagShort) nbt).f());
        }
    }

    private static final class NBTWriterInt extends NBTWriter
    {
        @Override
        void write(NBTBase nbt, StringBuilder builder)
        {
            builder.append(((NBTTagInt) nbt).e());
        }
    }

    private static final class NBTWriterFloat extends NBTWriter
    {
        @Override
        void write(NBTBase nbt, StringBuilder builder)
        {
            builder.append(((NBTTagFloat) nbt).i());
        }
    }

    private static final class NBTWriterLong extends NBTWriter
    {
        @Override
        void write(NBTBase nbt, StringBuilder builder)
        {
            builder.append(((NBTTagLong) nbt).d());
        }
    }

    private static final class NBTWriterDouble extends NBTWriter
    {
        @Override
        void write(NBTBase nbt, StringBuilder builder)
        {
            builder.append(((NBTTagDouble) nbt).asDouble());
        }
    }

    private static final class NBTWriterString extends NBTWriter
    {
        @Override
        void write(NBTBase nbt, StringBuilder builder)
        {
            String s = ((NBTTagString) nbt).c_();

            builder.append('"').append(s.replace("\"", "\\\"")).append('"');
        }
    }

    private static final class NBTWriterByteArray extends NBTWriter
    {
        @Override
        void write(NBTBase nbt, StringBuilder builder)
        {
            byte[] bytes = ((NBTTagByteArray) nbt).c();
            builder.append('[');

            int length = bytes.length;

            if (length > 0)
            {
                int i = 0;

                do
                {
                    builder.append(bytes[i++]);
                }
                while (i < length);
            }

            builder.append(']');
        }
    }

    private static final class NBTWriterIntArray extends NBTWriter
    {
        @Override
        void write(NBTBase nbt, StringBuilder builder)
        {
            int[] ints = ((NBTTagIntArray) nbt).d();
            builder.append('[');

            int length = ints.length;

            if (length > 0)
            {
                int i = 0;

                do
                {
                    builder.append(ints[i++]);
                }
                while (i < length);
            }

            builder.append(']');
        }
    }

    private static final class NBTWriterList extends NBTWriter
    {
        @Override
        void write(NBTBase nbt, StringBuilder builder)
        {
            toString((NBTTagList) nbt, builder);
        }

        void toString(NBTTagList list, StringBuilder builder)
        {
            builder.append('[');

            int size = list.size();

            if (size > 0)
            {
                int i = 0;

                switch (list.g())
                {
                    case NBT.DOUBLE:
                        while (true)
                        {
                            builder.append(list.f(i++));

                            if (i >= size)
                                break;

                            builder.append(',');
                        }
                        break;

                    case NBT.INT_ARRAY:
                        while (true)
                        {
                            int[] ints = list.d(i++);

                            builder.append('[');

                            int length = ints.length;

                            if (length > 0)
                            {
                                int j = 0;

                                while (true)
                                {
                                    builder.append(ints[j++]);

                                    if (j >= length)
                                        break;

                                    builder.append(',');
                                }
                            }

                            builder.append(']');

                            if (i >= size)
                                break;

                            builder.append(',');
                        }
                        break;

                    case NBT.STRING:
                        while (true)
                        {
                            String s = list.getString(i++);

                            builder.append('"').append(s.replace("\"", "\\\"")).append('"');

                            if (i >= size)
                                break;

                            builder.append(',');
                        }
                        break;
                    case NBT.COMPOUND:
                        while (true)
                        {
                            WRITER_COMPOUND.toString(list.get(i++), builder);

                            if (i >= size)
                                break;

                            builder.append(',');
                        }
                }
            }

            builder.append(']');
        }
    }

    private static final class NBTWriterCompound extends NBTWriter
    {
        void toString(NBTTagCompound compound, StringBuilder builder)
        {
            if (compound.isEmpty())
                return;

            builder.append('{');

            Iterator<String> iterator = compound.c().iterator();

            while (true)
            {
                String name = iterator.next();
                builder.append('\"').append(name.replace("\"", "\\\"")).append('\"').append(':');
                NBTBase value = compound.get(name);
                WRITERS[value.getTypeId()].write(value, builder);

                if (iterator.hasNext())
                    builder.append(',');
                else
                    break;
            }

            builder.append('}');
        }

        @Override
        void write(NBTBase nbt, StringBuilder builder)
        {
            toString((NBTTagCompound) nbt, builder);
        }
    }

}
