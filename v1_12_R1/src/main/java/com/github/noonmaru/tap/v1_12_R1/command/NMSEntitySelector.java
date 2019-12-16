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

package com.github.noonmaru.tap.v1_12_R1.command;

import com.github.noonmaru.tap.command.EntitySelector;
import com.github.noonmaru.tap.v1_12_R1.entity.NMSEntityTypes;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.command.CraftBlockCommandSender;
import org.bukkit.craftbukkit.v1_12_R1.command.CraftConsoleCommandSender;
import org.bukkit.craftbukkit.v1_12_R1.command.CraftRemoteConsoleCommandSender;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class NMSEntitySelector implements EntitySelector
{

    private static final MinecraftServer SERVER = ((CraftServer) Bukkit.getServer()).getServer();

    private static ICommandListener getICommandSender(CommandSender sender)
    {
        if (sender instanceof CraftEntity)
            return ((CraftEntity) sender).getHandle();
        if (sender instanceof CraftBlockCommandSender)
            return ((CraftBlockCommandSender) sender).getTileEntity();
        if (sender instanceof CraftConsoleCommandSender)
            return ((CraftServer) Bukkit.getServer()).getServer();
        if (sender instanceof CraftRemoteConsoleCommandSender)
            return ((DedicatedServer) ((CraftServer) Bukkit.getServer()).getServer()).remoteControlCommandListener;

        throw new IllegalArgumentException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends org.bukkit.entity.Entity> List<T> matchEntities(CommandSender sender, String token, Class<? extends org.bukkit.entity.Entity> target)
    {
        ICommandListener commandSender = getICommandSender(sender);
        MinecraftServer server = SERVER;
        Class<? extends Entity> targetEntity = NMSEntityTypes.getEntityClass(target);

        if (targetEntity == null)
            targetEntity = Entity.class;

        WorldServer[] defaultWorlds = server.worldServer;
        List<WorldServer> worldList = server.worlds;
        server.worldServer = worldList.toArray(new WorldServer[0]);

        try
        {
            List<? extends Entity> entities = PlayerSelector.getPlayers(commandSender, token, targetEntity);
            return entities.isEmpty() ? Collections.EMPTY_LIST : entities.stream().map((entity) -> (T) entity.getBukkitEntity()).collect(Collectors.toList());
        }
        catch (CommandException ignored)
        {
        }
        finally
        {
            server.worldServer = defaultWorlds;
        }

        return Collections.emptyList();
    }

}
