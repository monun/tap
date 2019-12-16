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

package com.github.noonmaru.tap.inventory;

public final class Slot
{

    private static final int[] RAW_SLOTS = new int[40];

    static
    {
        for (int i = 0; i < 40; i++)
        {
            int slot;

            if (i < 9)
                slot = i + 36;
            else if (i >= 36)
                slot = i - 36;
            else
                slot = i;

            RAW_SLOTS[i] = slot;
        }
    }

    private Slot()
    {}

    public static int getRawSlot(int slot)
    {
        return RAW_SLOTS[slot];
    }

}
