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

package com.github.noonmaru.tap.attach;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public final class Tools
{

    static final String TOOLS_DIR = "tools/";

    static final String ATTACH_DIR = "attach/";

    private static final String NATIVE_DIR = "natives/";

    private Tools()
    {}

    public static void loadAttachLibrary(File dir) throws Exception
    {
        String path = installBinary(dir);
        String property = "java.library.path";
        String libraryPath = System.getProperty("java.library.path");
        System.setProperty(property, libraryPath != null ? path + System.getProperty("path.separator") + libraryPath : path);

        Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
        fieldSysPath.setAccessible(true);
        fieldSysPath.set(null, null); //clear cache
        //System.loadLibrary("attach");
    }

    private static String installBinary(File dir) throws IOException
    {
        Platform platform = Platform.getPlatform();
        String path = TOOLS_DIR + ATTACH_DIR + NATIVE_DIR + (Platform.is64Bit() ? "64/" : "32/") + platform.getDir() + platform.getBinary();
        File file = new File(dir, path);

        if (!file.exists())
        {
            if (!file.getParentFile().mkdirs())
                throw new RuntimeException("Failed to create directory");

            InputStream in = Tools.class.getClassLoader().getResourceAsStream(path);

            try
            {
                Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            finally
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return file.getParentFile().getCanonicalPath();
    }

    public static byte[] getBytesFromClass(Class<?> clazz) throws IOException
    {
        return getBytesFromStream(clazz.getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/') + ".class"));
    }

    public static byte[] getBytesFromResource(ClassLoader classLoader, String resource) throws IOException
    {
        return getBytesFromStream(classLoader.getResourceAsStream(resource));
    }

    public static byte[] getBytesFromStream(InputStream in) throws IOException
    {
        byte[] buffer = new byte[Math.max(1024, in.available())];
        int offset = 0;

        for (int bytesRead; -1 != (bytesRead = in.read(buffer, offset, buffer.length - offset)); )
        {
            offset += bytesRead;

            if (offset == buffer.length)
                buffer = Arrays.copyOf(buffer, buffer.length + Math.max(in.available(), buffer.length >> 1));
        }

        return (offset == buffer.length) ? buffer : Arrays.copyOf(buffer, offset);
    }

    public static Class<?> defineClass(ClassLoader loader, InputStream inputStream)
    {
        try
        {
            byte[] bytes = getBytesFromStream(inputStream);
            Method defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            defineClassMethod.setAccessible(true);
            return (Class<?>) defineClassMethod.invoke(loader, null, bytes, 0, bytes.length);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

}
