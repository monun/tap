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

import java.io.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;


public final class MessageUtils
{

    public static Map<String, String> load(File file) throws IOException
    {
        BufferedReader reader = null;

        try
        {
            return load(reader = new BufferedReader(new FileReader(file)));
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Map<String, String> load(BufferedReader reader) throws IOException
    {
        BufferedReader br = new BufferedReader(reader);
        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        String s;

        while ((s = br.readLine()) != null)
        {
            int index = s.indexOf('=');

            if (index == -1 || index == 0)
                continue;

            map.put(s.substring(0, index), ChatColorSupport.color(s.substring(index + 1).replace("\\n", "\n")));
        }

        br.close();

        return map;
    }

    public static void save(Map<String, String> map, File file) throws IOException
    {
        save(map, new FileWriter(file));
    }

    public static void save(Map<String, String> map, Writer writer) throws IOException
    {
        BufferedWriter bw = new BufferedWriter(writer);

        if (map.size() > 0)
        {
            Iterator<Entry<String, String>> iter = map.entrySet().iterator();

            while (true)
            {
                Entry<String, String> entry = iter.next();

                bw.append(entry.getKey()).append('=').append(ChatColorSupport.strip(entry.getValue().replace("\n", "\\n")));

                if (iter.hasNext())
                {
                    bw.append('\n');
                }
                else
                    break;
            }
        }

        bw.close();
    }
}
