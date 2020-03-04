/*
 *
 *  * Copyright (c) 2020 Noonmaru
 *  *
 *  * Licensed under the General Public License, Version 3.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * https://opensource.org/licenses/gpl-3.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *
 */

package com.github.noonmaru.tap.attach;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.List;

public enum Platform
{
    LINUX("linux/", "libattach.so", Arrays.asList("LinuxVirtualMachine", "LinuxAttachProvider", "LinuxVirtualMachine$SocketInputStream")),
    MAC("mac/", "libattach.dylib", Arrays.asList("BsdVirtualMachine", "BsdAttachProvider", "BsdVirtualMachine$SocketInputStream")),
    WINDOWS("windows/", "attach.dll", Arrays.asList("WindowsVirtualMachine", "WindowsAttachProvider", "WindowsVirtualMachine$PipedInputStream"));

    private final String dir;

    private final String binary;

    private final List<String> classes;

    Platform(String dir, String binary, List<String> classes)
    {
        this.dir = dir;
        this.binary = binary;
        this.classes = ImmutableList.copyOf(classes);
    }

    public static Platform getPlatform()
    {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win"))
            return WINDOWS;

        if ((os.contains("nix")) || (os.contains("nux")) || (os.contains("aix")))
            return LINUX;

        if (os.contains("mac"))
            return MAC;

        throw new UnsupportedOperationException();
    }

    public static boolean is64Bit()
    {
        String osArch = System.getProperty("os.arch");
        return "amd64".equals(osArch) || "x86_64".equals(osArch);
    }

    public String getDir()
    {
        return dir;
    }

    public String getBinary()
    {
        return binary;
    }

    public List<String> getClasses()
    {
        return classes;
    }
}
