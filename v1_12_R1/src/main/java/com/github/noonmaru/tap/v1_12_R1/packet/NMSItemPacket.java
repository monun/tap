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

import com.github.noonmaru.tap.item.TapItem;
import com.github.noonmaru.tap.packet.ItemPacket;
import com.github.noonmaru.tap.v1_12_R1.item.NMSItem;
import net.minecraft.server.v1_12_R1.PacketPlayOutSetCooldown;

public class NMSItemPacket implements ItemPacket
{

    @Override
    public NMSPacket cooldown(TapItem item, int cooldownTicks)
    {
        return new NMSPacketFixed(new PacketPlayOutSetCooldown(((NMSItem) item).getHandle(), cooldownTicks));
    }

}
