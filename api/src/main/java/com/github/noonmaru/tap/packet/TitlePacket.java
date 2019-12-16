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

package com.github.noonmaru.tap.packet;

import com.github.noonmaru.tap.Tap;
import com.github.noonmaru.tap.text.TextComponent;

public interface TitlePacket
{

    Packet reset();

    default Packet title(String text)
    {
        return title(Tap.TEXT.fromText(text));
    }

    Packet title(TextComponent text);

    default Packet subtitle(String text)
    {
        return subtitle(Tap.TEXT.fromText(text));
    }

    Packet subtitle(TextComponent text);

    Packet show(int fadeIn, int stay, int fadeOut);

    default Packet compound(String title, String subtitle, int fadeIn, int stay, int fadeOut)
    {
        return compound(Tap.TEXT.fromText(title == null ? "" : title), subtitle == null ? null : Tap.TEXT.fromText(subtitle), fadeIn, stay, fadeOut);
    }

    Packet compound(TextComponent title, TextComponent subtitle, int fadeIn, int stay, int fadeOut);

}
