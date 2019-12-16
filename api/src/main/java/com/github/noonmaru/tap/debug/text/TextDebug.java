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

package com.github.noonmaru.tap.debug.text;

import com.github.noonmaru.tap.ChatType;
import com.github.noonmaru.tap.Tap;
import com.github.noonmaru.tap.debug.DebugProcess;
import com.github.noonmaru.tap.entity.TapPlayer;
import com.github.noonmaru.tap.item.TapItemStack;
import com.github.noonmaru.tap.packet.Packet;
import com.github.noonmaru.tap.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class TextDebug extends DebugProcess
{


    @Override
    public void onStart()
    {
        registerListener(new Listener()
        {
            @EventHandler
            public void onPlayerInteract(PlayerInteractEvent event)
            {
                Action action = event.getAction();

                if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR)
                {
                    TapPlayer tapPlayer = Tap.ENTITY.wrapEntity(event.getPlayer());
                    TapItemStack itemStack = tapPlayer.getHeldItemMainHand();

                    if (!itemStack.isEmpty())
                    {
                        for (TextComponent.Color color : TextComponent.Color.values())
                        {
                            TextComponent.Builder builder = TextComponent.builder();
                            TextComponent component = builder.input().text("COLOR: " + color.name()).color(color).hover().showItem(itemStack).build();
                            Packet.INFO.chat(component, ChatType.SYSTEM).sendAll();
                        }

                        for (TextComponent.Style style : TextComponent.Style.values())
                        {
                            TextComponent.Builder builder = TextComponent.builder();
                            TextComponent component = builder.input().text("STYLE: " + style.name()).style(style).hover().showItem(itemStack).build();
                            Packet.INFO.chat(component, ChatType.SYSTEM).sendAll();
                        }
                    }
                }
            }

            @EventHandler
            public void onAsyncPlayerChat(AsyncPlayerChatEvent event)
            {
                String message = event.getMessage();

                if (message.startsWith("text"))
                {
                    TextComponent.Builder builder = TextComponent.builder();
                    TextComponent component = builder.input().text(message).build();
                    Packet.INFO.chat(component, ChatType.SYSTEM).sendAll();
                }
                else if (message.startsWith("selector"))
                {
                    TextComponent.Builder builder = TextComponent.builder();
                    TextComponent component = builder.input().selector("@p").build();
                    Packet.INFO.chat(component, ChatType.SYSTEM).sendAll();
                }
                else if (message.startsWith("score"))
                {
                    TextComponent.Builder builder = TextComponent.builder();
                    TextComponent component = builder.input().score(event.getPlayer().getName(), Bukkit.getScoreboardManager().getMainScoreboard().getObjectives().iterator().next().getName()).build();
                    Packet.INFO.chat(component, ChatType.SYSTEM).sendAll();
                }
                else if (message.startsWith("translate"))
                {
                    {
                        TextComponent.Builder builder = TextComponent.builder();
                        TextComponent component = builder.input().translate("gui.toTitle").build();
                        Packet.INFO.chat(component, ChatType.SYSTEM).sendAll();
                    }
                    {
                        TextComponent.Builder builder = TextComponent.builder();
                        TextComponent component = builder.input().translate("commands.generic.entity.invalidType", "@p").build();
                        Packet.INFO.chat(component, ChatType.SYSTEM).sendAll();
                    }
                    {
                        TextComponent.Builder builder = TextComponent.builder();
                        TextComponent component = builder.input().translate("Insert a %s here.", "STRING").build();
                        Packet.INFO.chat(component, ChatType.SYSTEM).sendAll();
                    }
                }
                else if (message.startsWith("keybind"))
                {
                    TextComponent.Builder builder = TextComponent.builder();
                    TextComponent component = builder.input().keybind("key.inventory").build();
                    Packet.INFO.chat(component, ChatType.SYSTEM).sendAll();
                }
                else if (message.startsWith("hover"))
                {
                    TextComponent.Builder builder = TextComponent.builder();
                    TextComponent component = builder.input().text("HOVER ").hover().showText(message)
                            .next().input().text("ENTITY").hover().showEntity("크리퍼", "Creeper", "ID SECTION").build();
                    Packet.INFO.chat(component, ChatType.SYSTEM).sendAll();
                }
                else if (message.startsWith("click"))
                {
                    TextComponent.Builder builder = TextComponent.builder();
                    TextComponent component = builder.input().text("CLICK ")
                            .next().input().text(" openURL").click().openURL("https://www.youtube.com/?noredirect=1")
                            .next().input().text(" runCommand").click().runCommand("/say Run Command")
                            .next().input().text(" suggestCommand").click().suggestCommand("Suggest").build();
                    Packet.INFO.chat(component, ChatType.SYSTEM).sendAll();
                }
                else if (message.startsWith("extra"))
                {
                    TextComponent.Builder builder = TextComponent.builder();
                    builder.input().text("FIRST").style(TextComponent.Style.BOLD).color(TextComponent.Color.AQUA).extra().input().text("SECOND").style(TextComponent.Style.STRIKE_THROUGH).extra().input().text("THIRD").style(TextComponent.Style.UNDERLINED).color(TextComponent.Color.RED);
                    Packet.INFO.chat(builder.build(), ChatType.SYSTEM).sendAll();
                }
                else if (message.startsWith("insertion"))
                {
                    TextComponent.Builder builder = TextComponent.builder();
                    builder.input().text("insertion").insertion("INSERTION TEXT").build();

                    Packet.INFO.chat(builder.build(), ChatType.SYSTEM).sendAll();
                }
            }
        });
    }
}
