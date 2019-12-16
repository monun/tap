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

package com.github.noonmaru.tap.debug;

import com.github.noonmaru.tap.command.ArgumentList;
import com.github.noonmaru.tap.command.CommandComponent;
import com.github.noonmaru.tap.command.TabSupport;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;

public final class CommandDebug extends CommandComponent
{
    private final DebugManager manager;

    public CommandDebug(DebugManager manager)
    {
        super("<Module> <start | stop>", "Starts or stops debugging.", "tap.debug", 2);

        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String componentLabel, ArgumentList args)
    {
        String moduleName = args.next();
        DebugModule module = manager.getModule(moduleName);

        if (module == null)
        {
            sender.sendMessage("Not found debug module for " + moduleName);
            return true;
        }

        String state = args.next();

        if (state.equalsIgnoreCase("start"))
        {
            if (!module.start())
                sender.sendMessage("Debug " + module.getName() + " is already running.");
        }
        else if (state.equalsIgnoreCase("stop"))
        {
            if (!module.stop())
                sender.sendMessage("Debug " + module.getName() + " is not running");
        }
        else
        {
            return false;
        }

        return true;
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, Command command, String label, String componentLabel, ArgumentList args)
    {
        String arg = args.next();

        if (!args.hasNext())
            return TabSupport.complete(manager.getModuleNames(), arg);

        arg = args.next();

        if (!args.hasNext())
            return TabSupport.complete(Arrays.asList("start", "stop"), arg);

        return Collections.emptyList();
    }
}
