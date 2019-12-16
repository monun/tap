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

package com.github.noonmaru.tap.plugin;

import com.github.noonmaru.tap.command.CommandManager;
import com.github.noonmaru.tap.debug.CommandDebug;
import com.github.noonmaru.tap.debug.DebugManager;
import com.github.noonmaru.tap.init.TapLoader;
import org.bukkit.plugin.java.JavaPlugin;

public final class TapPlugin extends JavaPlugin
{

    private static TapPlugin instance;

    private CommandManager commandManager;

    private DebugManager debugManager;

    public static TapPlugin getInstance()
    {
        return instance;
    }

    @Override
    public void onLoad()
    {
        TapLoader.init(this);
    }

    @Override
    public void onEnable()
    {
        TapLoader.load(this);

        setupCommands();
        setupDebugger();

        instance = this;
    }

    private void setupCommands()
    {
        commandManager = new CommandManager().addHelp("help").register(getCommand("tap"));
    }

    private void setupDebugger()
    {
        debugManager = new DebugManager();
        commandManager.addComponent("debug", new CommandDebug(debugManager));
    }

    public CommandManager getCommandManager()
    {
        return commandManager;
    }

    public DebugManager getDebugManager()
    {
        return debugManager;
    }

}
