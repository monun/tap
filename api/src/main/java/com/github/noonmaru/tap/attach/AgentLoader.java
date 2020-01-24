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
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AgentLoader
{
    private static AgentLoaderInterface agentLoader;

    public static void attachAgentToJVM(ClassLoader loader, File agentFile)
    {
        getAgentLoader().loadAgent(agentFile);
    }

    private synchronized static AgentLoaderInterface getAgentLoader()
    {
        if (agentLoader != null)
            return agentLoader;

        Class<?> agentLoaderClass;

        try
        {
            Class.forName("com.sun.tools.attach.VirtualMachine"); //default jdk or hotspot agent
            agentLoaderClass = Class.forName("com.github.noonmaru.tap.attach.agent.AgentLoaderHotSpot");
        }
        catch (Exception ex)
        {
            Platform platform = Platform.getPlatform();
            ClassLoader systemLoader = ClassLoader.getSystemClassLoader();

            String path = Tools.TOOLS_DIR + Tools.ATTACH_DIR + "classes/";
            String platformPath = path + platform.getDir();

            List<String> shaded = new ArrayList<>(14);
            shaded.addAll(Stream.of(
                    "AttachProvider",
                    "VirtualMachine",
                    "VirtualMachineDescriptor",
                    "HotSpotVirtualMachine",
                    "HotSpotAttachProvider",
                    "HotSpotAttachProvider$HotSpotVirtualMachineDescriptor",
                    "AgentInitializationException",
                    "AgentLoadException",
                    "AttachNotSupportedException",
                    "AttachOperationFailedException",
                    "AttachPermission"
            ).map(s -> path + s).collect(Collectors.toList()));
            shaded.addAll(platform.getClasses().stream().map(s -> platformPath + s).collect(Collectors.toList()));
            shaded.add("com/github/noonmaru/tap/attach/agent/AttachProviderPlaceHolder");

            for (String s : shaded)
            {
                try
                {
                    Tools.defineClass(systemLoader, AgentLoader.class.getResourceAsStream("/" + s + ".class"));
                }
                catch (Exception e)
                {
                    throw new RuntimeException("Error defining: " + s, e);
                }
            }

            try
            {
                agentLoaderClass = Tools.defineClass(systemLoader, AgentLoader.class.getResourceAsStream("/com/github/noonmaru/tap/attach/agent/AgentLoaderHotSpot.class"));
            }
            catch (Exception e)
            {
                throw new RuntimeException("Error loading AgentLoader implementation", e);
            }
        }

        try
        {
            Object agentLoaderObject = agentLoaderClass.newInstance();

            agentLoader = agentFile -> {
                try
                {
                    final Method loadAgentMethod = agentLoaderObject.getClass().getMethod("attachAgentToJVM", File.class);
                    loadAgentMethod.invoke(agentLoaderObject, agentFile);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            };
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error getting agent loader implementation", e);
        }

        return agentLoader;
    }

    public static File generateAgentJar(File file, ClassLoader classLoader, String agentClass, List<String> resources) throws IOException
    {
        file.delete();
        file.deleteOnExit();

        Manifest manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();

        mainAttributes.put(Name.MANIFEST_VERSION, "1.0");
        mainAttributes.put(new Name("Agent-Class"), agentClass);
        mainAttributes.put(new Name("Can-Retransform-Classes"), "true");
        mainAttributes.put(new Name("Can-Redefine-Classes"), "true");

        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(file), manifest))
        {
            String agentPath = unqualify(agentClass);
            jos.putNextEntry(new JarEntry(agentPath));
            jos.write(Tools.getBytesFromStream(Objects.requireNonNull(classLoader.getResourceAsStream(agentPath))));
            jos.closeEntry();

            for (String name : resources)
            {
                jos.putNextEntry(new JarEntry(name));
                jos.write(Tools.getBytesFromStream(Objects.requireNonNull(classLoader.getResourceAsStream(name))));
                jos.closeEntry();
            }

            jos.flush();
        }

        return file;
    }

    private static String unqualify(String className)
    {
        return className.replace('.', '/') + ".class";
    }

    interface AgentLoaderInterface
    {
        void loadAgent(File agentFile);
    }

}
