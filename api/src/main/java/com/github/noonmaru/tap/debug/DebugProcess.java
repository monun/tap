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

import com.github.noonmaru.tap.plugin.TapPlugin;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedList;
import java.util.List;

public class DebugProcess
{

    private DebugModule module;

    private List<Listener> listeners;

    private List<BukkitTask> tasks;

    private boolean running;

    public final boolean isRunning()
    {
        return this.running;
    }

    private void checkRunning()
    {
        if (!running)
            throw new IllegalStateException();
    }

    final void start(DebugModule module)
    {
        if (running)
            throw new IllegalStateException("Process is already running!");

        this.module = module;
        running = true;

        try
        {
            onStart();
        }
        catch (Exception e)
        {
            running = false;

            throw e;
        }
    }

    /**
     * 프로세스가 시작할 때 호출됩니다.
     * Override하여 사용하세요.
     */
    public void onStart() {}

    final void stop()
    {
        running = false;
        clear();

        try
        {
            onStop();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void onStop() {}

    public final boolean shutdown()
    {
        if (!running)
            return false;

        return this.module.stop();
    }

    public final void registerListener(Listener listener)
    {
        checkRunning();
        TapPlugin plugin = TapPlugin.getInstance();
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);

        if (listeners == null)
            listeners = new LinkedList<>();

        listeners.add(listener);
    }

    public final void registerTask(Runnable runnable, long delay, long period)
    {
        checkRunning();
        TapPlugin plugin = TapPlugin.getInstance();
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, period);

        if (tasks == null)
            tasks = new LinkedList<>();

        tasks.add(task);
    }

    final void clear()
    {
        if (this.listeners != null)
        {
            listeners.forEach(HandlerList::unregisterAll);
            listeners.clear();
        }

        if (this.tasks != null)
        {
            tasks.forEach(BukkitTask::cancel);
            tasks.clear();
        }
    }
}
