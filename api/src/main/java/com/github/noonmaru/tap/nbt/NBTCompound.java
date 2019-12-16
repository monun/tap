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


import com.google.gson.JsonObject;
import com.nemosw.tools.gson.JsonIO;

import java.io.*;

public interface NBTCompound
{
    /**
     * 키에 지정된 boolean을 가져옵니다.
     *
     * @param name 키 이름
     * @return 키에 지정된 boolean값, 지정된 값이 없을경우 null
     * @throws ClassCastException 키에 지정된 값이 boolean형태가 아닐 경우 발생합니다.
     */
    default boolean getBoolean(String name)
    {
        return getByte(name) != 0;
    }

    default void setBoolean(String name, boolean value)
    {
        setByte(name, value ? (byte) 1 : (byte) 0);
    }

    /**
     * 키에 지정된 byte를 가져옵니다.
     *
     * @param name 키 이름
     * @return 키에 지정된 byte값 지정된 값이 없을경우 null
     * @throws ClassCastException 키에 지정된 값이 byte형태가 아닐 경우 발생합니다.
     */
    byte getByte(String name);

    void setByte(String name, byte value);

    /**
     * byte[]을 가져옵니다.
     *
     * @param name 키 이름
     * @return 값
     */
    byte[] getByteArray(String name);

    void setByteArray(String name, byte[] value);

    /**
     * short를 가져옵니다.
     *
     * @param name 키 이름
     * @return 값
     */
    short getShort(String name);

    void setShort(String name, short value);

    /**
     * int를 가져옵니다.
     *
     * @param name 키 이름
     * @return 값
     */
    int getInt(String name);

    void setInt(String name, int value);

    /**
     * int[]를 가져옵니다.
     *
     * @param name 키 이름
     * @return 값
     */
    int[] getIntArray(String name);

    void setIntArray(String name, int[] value);

    /**
     * long을 가져옵니다.
     *
     * @param name 키 이름
     * @return 값
     */
    long getLong(String name);

    void setLong(String name, long value);

    /**
     * float을 가져옵니다.
     *
     * @param name 키 이름
     * @return 값
     */
    float getFloat(String name);

    void setFloat(String name, float value);

    /**
     * double을 가져옵니다.
     *
     * @param name 키 이름
     * @return 값
     */
    double getDouble(String name);

    void setDouble(String name, double value);

    /**
     * string을 가져옵니다.
     *
     * @param name 키 이름
     * @return 값
     */
    String getString(String name);

    void setString(String name, String value);

    /**
     * NBTList를 가져옵니다.
     *
     * @param name 키 이름
     * @return 값
     */
    NBTList getList(String name);

    void setList(String name, NBTList list);

    /**
     * NBTCompound를 가져옵니다.
     *
     * @param name 키 이름
     * @return 값
     */
    NBTCompound getCompound(String name);

    void setCompound(String name, NBTCompound compound);

    /**
     * 키가 있는지 확인합니다.
     *
     * @param name 키 이름
     * @return 등록 여부
     */
    boolean contains(String name);

    void remove(String name);

    /**
     * NBTCompound가 비어있는지 확인합니다.
     *
     * @return 결과
     */
    boolean isEmpty();

    NBTCompound copy();

    void save(OutputStream out) throws IOException;

    default byte[] save()
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try
        {
            save(out);
        }
        catch (IOException impossible)
        {
            throw new AssertionError(impossible);
        }

        return out.toByteArray();
    }

    default void save(File file) throws IOException
    {
        FileOutputStream out = null;

        try
        {
            File temp = new File(file.getPath() + ".tmp");

            out = new FileOutputStream(temp);

            save(out);
            out.close();

            file.delete();
            temp.renameTo(file);
        }
        finally
        {
            if (out != null)
            {
                try
                {
                    out.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    default JsonObject toJson()
    {
        return (JsonObject) JsonIO.getParser().parse(toString());
    }

    StringBuilder toJsonString(StringBuilder builder);

    default String toJsonString()
    {
        return toJsonString(new StringBuilder()).toString();
    }

}
