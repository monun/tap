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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class TabSupport
{

    private TabSupport()
    {}

    private static Predicate<String> createFilter(String filter)
    {
        return s -> s.regionMatches(true, 0, filter, 0, filter.length());
    }

    public static <T> List<String> complete(Iterable<T> iterable, Function<? super T, String> function, String filter)
    {
        return StreamSupport.stream(iterable.spliterator(), false).map(function).filter(createFilter(filter)).collect(Collectors.toList());
    }

    public static List<String> complete(Iterable<String> iterable, String filter)
    {
        return StreamSupport.stream(iterable.spliterator(), false).filter(createFilter(filter)).collect(Collectors.toList());
    }

}
