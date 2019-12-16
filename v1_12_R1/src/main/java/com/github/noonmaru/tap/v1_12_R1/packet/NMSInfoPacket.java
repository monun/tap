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

package com.github.noonmaru.tap.v1_12_R1.packet;

import com.github.noonmaru.tap.ChatType;
import com.github.noonmaru.tap.packet.InfoPacket;
import com.github.noonmaru.tap.text.TextComponent;
import com.github.noonmaru.tap.v1_12_R1.text.NMSTextComponent;
import com.github.noonmaru.tools.reflection.ReflectionUtils;
import net.minecraft.server.v1_12_R1.ChatMessageType;
import net.minecraft.server.v1_12_R1.PacketPlayOutChat;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerListHeaderFooter;

import java.lang.reflect.Field;

public class NMSInfoPacket implements InfoPacket
{

    private static final ChatMessageType[] CHAT_MESSAGE_TYPES;

    private static final Field FIELD_HEADER = ReflectionUtils.findPrivateField(PacketPlayOutPlayerListHeaderFooter.class, "a");

    private static final Field FIELD_FOOTER = ReflectionUtils.findPrivateField(PacketPlayOutPlayerListHeaderFooter.class, "b");

    static
    {
        ChatMessageType[] chatMessageTypes = new ChatMessageType[Math.max(ChatType.values().length, ChatMessageType.values().length)];
        chatMessageTypes[ChatType.CHAT.ordinal()] = ChatMessageType.CHAT;
        chatMessageTypes[ChatType.SYSTEM.ordinal()] = ChatMessageType.SYSTEM;
        chatMessageTypes[ChatType.GAME_INFO.ordinal()] = ChatMessageType.GAME_INFO;
        CHAT_MESSAGE_TYPES = chatMessageTypes;
    }

    @Override
    public NMSPacket chat(TextComponent text, ChatType type)
    {
        return new NMSPacketFixed(new PacketPlayOutChat(((NMSTextComponent) text).component, CHAT_MESSAGE_TYPES[type.ordinal()]));
    }

    @Override
    public NMSPacket playerListHeaderFooter(TextComponent header, TextComponent footer)
    {
        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();

        try
        {
            FIELD_HEADER.set(packet, ((NMSTextComponent) header).component);
            FIELD_FOOTER.set(packet, ((NMSTextComponent) footer).component);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return new NMSPacketFixed(packet);
    }

}
