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

public final class MessageColor
{
    private MessageColor()
    {}

    public static String color(String s)
    {
        return replaceColorCode('&', '§', s);
    }

    public static String strip(String s)
    {
        return replaceColorCode('§', '&', s);
    }

    private static String replaceColorCode(char from, char to, String s)
    {
        int i = s.indexOf(from);

        if (i == -1)
            return s;

        char[] b = s.toCharArray();
        int length = b.length - 1;

        while (i < length)
        {
            if ((b[i] == from) && ("0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[(i + 1)]) > -1))
            {
                b[i] = to;
                b[(i + 1)] = Character.toLowerCase(b[(i + 1)]);
            }

            i++;
        }

        return new String(b);
    }
}
