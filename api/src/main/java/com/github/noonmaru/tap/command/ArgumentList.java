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

package com.github.noonmaru.tap.command;

import java.util.NoSuchElementException;

public final class ArgumentList
{

    private final String[] arguments;

    private int cursor;

    public ArgumentList(String[] arguments, int index)
    {
        this.arguments = arguments;
        this.cursor = index;
    }

    public int getCursor()
    {
        return this.cursor;
    }

    public ArgumentList setCursor(int cursor)
    {
        if (cursor < 0 || cursor > this.arguments.length)
            throw new ArrayIndexOutOfBoundsException(cursor);

        this.cursor = cursor;

        return this;
    }

    public boolean isLast()
    {
        return remain() == 1;
    }

    public int remain()
    {
        return this.arguments.length - this.cursor;
    }

    public boolean isEmpty()
    {
        return this.arguments.length == 0;
    }

    public boolean hasNext()
    {
        return cursor < arguments.length;
    }

    public String next()
    {
        if (this.cursor >= this.arguments.length)
            throw new NoSuchElementException();

        return this.arguments[cursor++];
    }

    public boolean nextBoolean()
    {
        return Boolean.parseBoolean(next());
    }


    public byte nextByte()
    {
        return Byte.parseByte(next());
    }

    public byte nextByte(byte defaultValue)
    {
        try
        {
            return Byte.parseByte(next());
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }

    public short nextShort()
    {
        return Short.parseShort(next());
    }

    public short nextShort(short defaultValue)
    {
        try
        {
            return Short.parseShort(next());
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }

    public int nextInt()
    {
        return Integer.parseInt(next());
    }

    public int nextInt(int defaultValue)
    {
        try
        {
            return Integer.parseInt(next());
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }

    public long nextLong()
    {
        return Long.parseLong(next());
    }

    public long nextLong(long defaultValue)
    {
        try
        {
            return Long.parseLong(next());
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }

    public float nextFloat()
    {
        return Float.parseFloat(next());
    }

    public float nextFloat(float defaultValue)
    {
        try
        {
            return Float.parseFloat(next());
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }

    public double nextDouble()
    {
        return Double.parseDouble(next());
    }

    public double nextDouble(double defaultValue)
    {
        try
        {
            return Double.parseDouble(next());
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }

    private void checkJoinLength(int length)
    {
        if (length < 0 || length > remain())
            throw new ArrayIndexOutOfBoundsException();
    }

    public String join()
    {
        return join(remain());
    }

    public String join(int length)
    {
        checkJoinLength(length);

        String[] args = arguments;
        int index = this.cursor, capacity = 0;

        for (int i = 0; i < length; i++)
            capacity += args[index + i].length();

        StringBuilder builder = new StringBuilder(capacity);

        for (int i = 0; i < length; i++)
            builder.append(args[index + i]);

        this.cursor += length;

        return builder.toString();
    }

    public String join(char separator)
    {
        return join(separator, remain());
    }

    public String join(char separator, int length)
    {
        checkJoinLength(length);

        String[] args = arguments;
        int index = this.cursor, capacity = Math.max(length - 1, 1);

        for (int i = 0; i < length; i++)
            capacity += args[index + i].length();

        StringBuilder builder = new StringBuilder(capacity);

        for (int i = 0; i < length; i++)
        {
            builder.append(args[index + i]);

            if (i + 1 < length)
                builder.append(separator);
        }

        this.cursor += length;

        return builder.toString();
    }

    public String join(String separator)
    {
        return join(separator, remain());
    }

    public String join(String separator, int length)
    {
        checkJoinLength(length);

        String[] args = arguments;
        int index = this.cursor, capacity = Math.max(length - 1, 1) * separator.length();

        for (int i = 0; i < length; i++)
            capacity += args[index + i].length();

        StringBuilder builder = new StringBuilder(capacity);

        for (int i = 0; i < length; i++)
        {
            builder.append(args[index + i]);

            if (i + 1 < length)
                builder.append(separator);
        }

        this.cursor += length;

        return builder.toString();
    }

}
