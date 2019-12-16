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

import java.util.List;

public class Information
{
    public static final String LINE = "────────";

    public static final String DEFAULT_LINE_COLOR = "§c";

    public static final String DEFAULT_NAME_COLOR = "§6";

    public static String pageHeader(String line, String color, String name, int page, int total, int size)
    {
        return pageHeader(new StringBuilder(), line, color, name, page, total, size).toString();
    }

    public static StringBuilder pageHeader(StringBuilder builder, String name, int page, int total, int size)
    {
        return pageHeader(builder, DEFAULT_LINE_COLOR, DEFAULT_NAME_COLOR, name, page, total, size);
    }

    public static StringBuilder pageHeader(StringBuilder builder, String line, String color, String name, int page, int total, int size)
    {
        return builder.append(line).append(LINE).append(" ").append(color).append(name).append(" §r[ §b").append(page).append(" §r/ §7").append(total)
                .append(" §r] §d").append(size).append(" ").append(line).append(LINE);
    }

    public static String[] pageInformation(String name, List<? extends Informable> list, int page, int length)
    {
        return pageInformation(DEFAULT_LINE_COLOR, DEFAULT_NAME_COLOR, name, list, page, length);
    }

    public static String[] pageInformation(String line, String color, String name, List<? extends Informable> list, int page, int length)
    {
        int size = list.size();
        int total = (size - 1) / length;

        if (page > total)
            page = total;
        else if (page < 0)
            page = 0;

        int index = page * 9;
        int end = Math.min((page + 1) * length, size);
        length = end - index;

        String[] informations = new String[length + 1];
        StringBuilder builder = new StringBuilder();
        informations[0] = pageHeader(builder, line, color, name, page + 1, total + 1, size).toString();

        while (index < end)
        {
            builder.setLength(0);
            list.get(index).information(index, builder);
            informations[length - (end - index) + 1] = builder.toString();
            index++;
        }

        return informations;
    }

    public static <T> String[] pageInformation(String name, List<T> list, Informer<T> informer, int page, int length)
    {
        return pageInformation(DEFAULT_LINE_COLOR, DEFAULT_NAME_COLOR, name, list, informer, page, length);
    }

    public static <T> String[] pageInformation(String line, String color, String name, List<T> list, Informer<T> informer, int page, int length)
    {
        int size = list.size();
        int total = (size - 1) / length;

        if (page > total)
            page = total;
        else if (page < 0)
            page = 0;

        int index = page * 9;
        int end = Math.min((page + 1) * length, size);
        length = end - index;

        String[] informations = new String[end - index + 1];
        StringBuilder builder = new StringBuilder();
        informations[0] = pageHeader(builder, line, color, name, page + 1, total + 1, size).toString();

        while (index < end)
        {
            builder.setLength(0);
            informer.information(index, builder, list.get(index));
            informations[length - (end - index) + 1] = builder.toString();
            index++;
        }

        return informations;
    }
}
