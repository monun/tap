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

package com.github.noonmaru.tap.nbt;

public interface NBTList
{

    int[] getIntArray(int i);

    void addIntArray(int[] value);

    float getFloat(int i);

    void addFloat(float value);

    double getDouble(int i);

    void addDouble(double value);

    String getString(int i);

    void addString(String value);

    NBTCompound getCompound(int i);

    void addCompound(NBTCompound compound);

    int size();

}
