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

import org.bukkit.ChatColor;

import java.util.List;
import java.util.ListIterator;

public final class ChatColorSupport
{

    private ChatColorSupport() {}

    public static List<String> frontColor(ChatColor color, List<String> list)
    {
        ListIterator<String> i = list.listIterator();

        while (i.hasNext())
        {
            String s = i.next();

            i.set(frontColor(color, s));
        }

        return list;
    }

    public static String[] frontColor(ChatColor color, String... s)
    {
        for (int i = 0, length = s.length; i < length; i++)
            s[i] = frontColor(color, s[i]);

        return s;
    }

    public static String frontColor(ChatColor color, String s)
    {
        return s.length() > 0 && s.charAt(0) == '§' ? s : color + s;
    }

    public static String color(String s)
    {
        return replaceColorCode(s, '&', '§');
    }

    public static String strip(String s)
    {
        return replaceColorCode(s, '§', '&');
    }

    private static String replaceColorCode(String s, char from, char to)
    {
        int i = s.indexOf(from);

        if (i == -1)
            return s;

        char[] chars = s.toCharArray();

        for (int length = s.length() - 1; i < length; i++)
        {
            if (chars[i] == from && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(chars[i + 1]) > -1)
            {
                chars[i] = to;
                chars[i + 1] = Character.toLowerCase(chars[i + 1]);
            }
        }

        return new String(chars);
    }

}
