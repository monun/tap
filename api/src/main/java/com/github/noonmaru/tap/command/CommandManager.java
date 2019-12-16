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

import org.apache.commons.lang.Validate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CommandManager
{
    private static SoftReference<ArrayList<Entry<String, CommandComponent>>> tempReference;

    final LinkedHashMap<String, CommandComponent> components = new LinkedHashMap<>();

    private CommandExecutor executor;

    private static ArrayList<Entry<String, CommandComponent>> temp()
    {
        ArrayList<Entry<String, CommandComponent>> temp;

        if (tempReference == null)
        {
            tempReference = new SoftReference<>(temp = new ArrayList<>());
        }
        else
        {
            temp = tempReference.get();

            if (temp == null)
                tempReference = new SoftReference<>(temp = new ArrayList<>());
            else
                temp.clear();
        }

        return temp;
    }

    private static int getLevenshteinDistance(String a, String b)
    {
        int len0 = a.length() + 1;
        int len1 = b.length() + 1;

        int[] cost = new int[len0];
        int[] newCost = new int[len0];

        for (int i = 0; i < len0; i++)
            cost[i] = i;

        for (int j = 1; j < len1; j++)
        {
            newCost[0] = j;

            for (int i = 1; i < len0; i++)
            {
                int match = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;

                int cost_replace = cost[i - 1] + match;
                int cost_insert = cost[i] + 1;
                int cost_delete = newCost[i - 1] + 1;

                newCost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            int[] swap = cost;
            cost = newCost;
            newCost = swap;
        }

        return cost[len0 - 1];
    }

    public final CommandManager addHelp(String label)
    {
        addComponent(label, new CommandHelp());

        return this;
    }

    public final CommandManager addComponent(String label, CommandComponent component)
    {
        if (label == null)
            throw new NullPointerException("Label cannot be null");

        if (component == null)
            throw new NullPointerException("Component can not be null");

        label = label.toLowerCase();

        if (this.components.containsKey(label))
            throw new IllegalArgumentException('\'' + label + "' is already registered command");

        this.components.put(label, component);
        return this;
    }

    public final ArrayList<Entry<String, CommandComponent>> getPerformableList(CommandSender sender)
    {
        ArrayList<Entry<String, CommandComponent>> temp = temp();

        for (Entry<String, CommandComponent> component : this.components.entrySet())
        {
            if (component.getValue().testPermission(sender))
                temp.add(component);
        }

        return temp;
    }

    public final CommandComponent getComponent(String label)
    {
        return components.get(label.toLowerCase());
    }

    public final String getNearestLabel(CommandSender sender, String label)
    {
        String nearest = null;
        int distance = Integer.MAX_VALUE;

        for (Entry<String, CommandComponent> entry : getPerformableList(sender))
        {
            String componentLabel = entry.getKey();

            int curDistance = getLevenshteinDistance(label, componentLabel);

            if (curDistance < distance)
            {
                distance = curDistance;
                nearest = componentLabel;

                if (distance == 0)
                    break;
            }
        }

        return nearest;
    }

    public boolean onCommand(CommandSender sender, Command command, String label)
    {
        return false;
    }

    public final CommandManager register(PluginCommand command)
    {
        Validate.notNull(command, "Command cannot be null");

        if (this.executor == null)
            this.executor = new CommandExecutor();

        command.setExecutor(this.executor);
        command.setTabCompleter(this.executor);

        if (command.getPermission() != null)
            command.setPermissionMessage(Message.NO_PERMISSION);

        return this;
    }

    private class CommandHelp extends CommandComponent implements Informer<Entry<String, CommandComponent>>
    {
        private String label;

        public CommandHelp()
        {
            super("<page>", Message.HELP_DESCRIPTION);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String componentLabel, ArgumentList args)
        {
            List<Entry<String, CommandComponent>> componentList = CommandManager.this.getPerformableList(sender);

            if (componentList.isEmpty())
            {
                sender.sendMessage(Message.NOT_EXISTS_PERFORMABLE_COMMAND);
                return true;
            }

            this.label = label;

            for (String message : Information.pageInformation(label, componentList, this, args.hasNext() ? Math.max(args.nextInt(1) - 1, 0) : 0, 9))
                sender.sendMessage(message);

            this.label = null;

            return true;
        }

        @Override
        public void information(int index, StringBuilder builder, Entry<String, CommandComponent> o)
        {
            CommandComponent component = o.getValue();
            HelpUtils.createHelp(builder, label, o.getKey(), component.usage, component.description);
        }
    }

    private class CommandExecutor implements TabExecutor
    {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
        {
            List<Entry<String, CommandComponent>> performables = getPerformableList(sender);

            if (performables.isEmpty())
            {
                sender.sendMessage('/' + label + ' ' + Message.NOT_EXISTS_PERFORMABLE_COMMAND);
                return true;
            }

            if (args.length == 0)
            {
                if (CommandManager.this.onCommand(sender, command, label))
                    return true;

                StringBuilder usage = new StringBuilder().append('/').append(label).append("§6");

                for (Entry<String, CommandComponent> entry : performables)
                    usage.append(' ').append(entry.getKey());

                sender.sendMessage(usage.toString());
                return true;
            }

            String componentLabel = args[0];

            CommandComponent component = getComponent(componentLabel);

            if (component == null)
            {
                sender.sendMessage('/' + label + " §6" + componentLabel + ' ' + Message.NO_COMPONENT);
                sender.sendMessage('/' + label + " §6" + getNearestLabel(sender, componentLabel));
                return true;
            }

            String permissionMessage = component.getPermissionMessage(sender);

            if (permissionMessage != null)
            {
                sender.sendMessage('/' + label + " §6" + componentLabel + " §r" + permissionMessage);
                return true;
            }

            try
            {
                if (args.length <= component.argumentsLength || !component.onCommand(sender, command, label, componentLabel, new ArgumentList(args, 1)))
                    sender.sendMessage(HelpUtils.createHelp(label, componentLabel, component.usage, component.description));
            }
            catch (Throwable t)
            {
                t.printStackTrace();

                if (sender instanceof Player)
                {
                    sender.sendMessage(Message.createErrorMessage(label, componentLabel, t, CommandExecutor.class.getName()));
                }
            }

            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
        {
            String componentLabel = args[0];

            if (args.length == 1)
                return TabSupport.complete(components.keySet(), componentLabel);

            CommandComponent component = getComponent(componentLabel);

            if (component != null && component.testPermission(sender))
            {
                Iterable<String> iterable = component.onTabComplete(sender, command, label, componentLabel, new ArgumentList(args, 1));

                if (iterable == null)
                    return null;

                if (iterable instanceof List)
                    return (List<String>) iterable;

                return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
            }

            return Collections.emptyList();
        }
    }
}
