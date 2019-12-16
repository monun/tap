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

package com.github.noonmaru.tap.lang;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 * @author Nemo
 */
public final class Language
{
    private static final Map<String, Language> cache = new MapMaker().weakValues().makeMap();

    private final Map<String, String> map = Maps.newHashMap();

    public static Language load(File langFile, String cacheName) throws IOException
    {
        Language language = cache.get(cacheName);

        if (language != null)
            return language;

        language = new Language();

        try (FileReader r = new FileReader(langFile))
        {
            language.parse(new BufferedReader(r));
        }

        cache.put(cacheName, language);

        return language;
    }

    void parse(BufferedReader reader) throws IOException
    {
        if (map.size() > 0)
            throw new IllegalStateException("Already parsed!");

        String s;

        while ((s = reader.readLine()) != null)
        {
            int i = s.indexOf('=');
            String key = s.substring(0, i - 1);
            String value = s.substring(i);

            map.put(key, value);
        }
    }

    public String translate(String key)
    {
        return map.get(key);
    }

    public String translateFormat(String key, Object... format)
    {
        String value = map.get(key);

        return value == null ? null : String.format(value, format);
    }
}
