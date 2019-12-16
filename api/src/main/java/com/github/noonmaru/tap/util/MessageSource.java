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

package com.github.noonmaru.tap.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageSource
{
    private final String[] macro;

    private String prefix;

    private String message;

    private String toString;

    public MessageSource(String message)
    {
        this.message = message;

        Pattern pattern = Pattern.compile("<(.*?)>");
        Matcher matcher = pattern.matcher(message);

        ArrayList<String> macro = null;

        while (matcher.find())
        {
            if (macro == null)
                macro = new ArrayList<>(2);

            macro.add(matcher.group());
        }

        this.macro = macro == null ? null : macro.toArray(new String[0]);
        this.toString = message;
    }

    public static void load(Class<?> clazz, File file)
    {
        if (file.exists())
        {
            try
            {
                Map<String, String> map = MessageUtils.load(file);

                for (Field field : getMessageSourceFields(clazz))
                {
                    String message = map.get(field.getName().toLowerCase());

                    if (message != null)
                    {
                        try
                        {
                            ((MessageSource) field.get(null)).setMessage(message);
                        }
                        catch (IllegalArgumentException | IllegalAccessException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            List<Field> fields = getMessageSourceFields(clazz);
            LinkedHashMap<String, String> map = new LinkedHashMap<>(fields.size());

            for (Field field : fields)
            {
                try
                {
                    map.put(field.getName().toLowerCase(), ((MessageSource) field.get(null)).message);
                }
                catch (IllegalArgumentException | IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }

            try
            {
                MessageUtils.save(map, file);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void setPrefix(Class<?> clazz, String prefix)
    {
        if (prefix == null)
            throw new NullPointerException("prefix cannot be null");

        for (Field field : getMessageSourceFields(clazz))
        {
            try
            {
                ((MessageSource) field.get(clazz)).setPrefix(prefix);
            }
            catch (IllegalArgumentException | IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
    }

    private static List<Field> getMessageSourceFields(Class<?> clazz)
    {
        Field[] declaredFields = clazz.getDeclaredFields();
        ArrayList<Field> fields = new ArrayList<>(declaredFields.length);

        for (Field field : declaredFields)
        {
            if (field.getType() == MessageSource.class && Modifier.isStatic(field.getModifiers()))
            {
                field.setAccessible(true);
                fields.add(field);
            }
        }

        return fields;
    }

    private void setPrefix(String prefix)
    {
        this.prefix = prefix;
        this.toString = prefix + this.message;
    }

    public String getMessage()
    {
        return message;
    }

    private void setMessage(String message)
    {
        this.message = message;
        this.toString = this.prefix == null ? message : this.prefix.concat(message);
    }

    public String toString(String... args)
    {
        if (this.macro == null)
            return this.toString;

        StringBuilder builder = new StringBuilder(this.toString);

        for (int i = 0, length = Math.min(this.macro.length, args.length); i < length; i++)
        {
            String macro = this.macro[i];
            int index = builder.indexOf(macro);

            if (index >= 0)
                builder.replace(index, index + macro.length(), args[i]);
        }

        return builder.toString();
    }

    @Override
    public String toString()
    {
        return this.toString;
    }
}
