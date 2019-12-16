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

import com.github.noonmaru.tap.packet.TitlePacket;
import com.github.noonmaru.tap.text.TextComponent;
import com.github.noonmaru.tap.v1_12_R1.text.NMSTextComponent;
import net.minecraft.server.v1_12_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_12_R1.PacketPlayOutTitle.EnumTitleAction;

public class NMSTitlePacket implements TitlePacket
{

    @Override
    public NMSPacket reset()
    {
        return new NMSPacketFixed(new PacketPlayOutTitle(EnumTitleAction.RESET, null));
    }

    @Override
    public NMSPacket title(TextComponent text)
    {
        return new NMSPacketFixed(new PacketPlayOutTitle(EnumTitleAction.TITLE, ((NMSTextComponent) text).component));
    }

    @Override
    public NMSPacket subtitle(TextComponent text)
    {
        return new NMSPacketFixed(new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, ((NMSTextComponent) text).component));
    }

    @Override
    public NMSPacket show(int fadeIn, int stay, int fadeOut)
    {
        return new NMSPacketFixed(new PacketPlayOutTitle(fadeIn, stay, fadeOut));
    }

    @Override
    public NMSPacket compound(TextComponent title, TextComponent subtitle, int fadeIn, int stay, int fadeOut)
    {
        PacketPlayOutTitle resetPacket = new PacketPlayOutTitle(EnumTitleAction.RESET, null);
        PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(EnumTitleAction.TITLE, ((NMSTextComponent) title).component);
        PacketPlayOutTitle showPacket = new PacketPlayOutTitle(fadeIn, stay, fadeOut);

        if (subtitle == null)
            return new NMSPacketMulti(resetPacket, titlePacket, showPacket);

        PacketPlayOutTitle subtitlePacket = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, ((NMSTextComponent) subtitle).component);

        return new NMSPacketMulti(resetPacket, titlePacket, subtitlePacket, showPacket);
    }

}
