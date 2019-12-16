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

public class HelpUtils
{
    public static String createHelp(String label, String componentLabel)
    {
        return createHelp(new StringBuilder(), label, componentLabel).toString();
    }

    public static String createHelp(String label, String componentLabel, String usage)
    {
        return createHelp(new StringBuilder(), label, componentLabel, usage).toString();
    }

    public static String createHelp(String label, String componentLabel, String usage, String description)
    {
        return createHelp(new StringBuilder(), label, componentLabel, usage, description).toString();
    }

    public static StringBuilder createHelp(StringBuilder builder, String label, String componentLabel)
    {
        return builder.append("§r/").append(label).append(" §6").append(componentLabel);
    }

    public static StringBuilder createHelp(StringBuilder builder, String label, String componentLabel, String usage)
    {
        createHelp(builder, label, componentLabel);

        if (usage != null)
            builder.append(" §r").append(usage);

        return builder;
    }

    public static StringBuilder createHelp(StringBuilder builder, String label, String componentLabel, String usage, String description)
    {
        createHelp(builder, label, componentLabel, usage);

        if (description != null)
            builder.append(" §r").append(description);

        return builder;
    }
}
