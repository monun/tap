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

package com.github.noonmaru.tap.attach.agent;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.spi.AttachProvider;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.util.Locale;

public final class AgentLoaderHotSpot
{

    public static VirtualMachine getVirtualMachine()
    {
        if (VirtualMachine.list().size() > 0)
        {
            // tools jar present
            String pid = getPid();
            try
            {
                return VirtualMachine.attach(pid);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        String jvm = System.getProperty("java.vm.name").toLowerCase(Locale.ENGLISH);
        if (jvm.contains("hotspot") || jvm.contains("openjdk") || jvm.contains("dynamic code evolution"))
        {
            // tools jar not present, but it's a sun vm
            Class<VirtualMachine> virtualMachineClass = pickVmImplementation();
            try
            {
                final AttachProviderPlaceHolder attachProvider = new AttachProviderPlaceHolder();
                Constructor<VirtualMachine> vmConstructor = virtualMachineClass.getDeclaredConstructor(AttachProvider.class, String.class);
                vmConstructor.setAccessible(true);

                return vmConstructor.newInstance(attachProvider, getPid());
            }
            catch (UnsatisfiedLinkError e)
            {
                throw new RuntimeException("This jre doesn't support the native library for attaching to the jvm", e);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        // not a hotspot based virtual machine
        return null;
    }

    /**
     * Gets the current jvm pid.
     *
     * @return the pid as String
     */
    public static String getPid()
    {
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        return nameOfRunningVM.substring(0, p);
    }

    /**
     * Picks one of the Oracle's implementations of VirtualMachine
     */
    @SuppressWarnings("unchecked")
    private static Class<VirtualMachine> pickVmImplementation()
    {
        String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

        try
        {
            if (os.contains("win"))
            {
                return (Class<VirtualMachine>) AgentLoaderHotSpot.class.getClassLoader().loadClass("sun.tools.attach.WindowsVirtualMachine");
            }
            if (os.contains("nix") || os.contains("nux") || os.indexOf("aix") > 0)
            {
                return (Class<VirtualMachine>) AgentLoaderHotSpot.class.getClassLoader().loadClass("sun.tools.attach.LinuxVirtualMachine");
            }
            if (os.contains("mac"))
            {
                return (Class<VirtualMachine>) AgentLoaderHotSpot.class.getClassLoader().loadClass("sun.tools.attach.BsdVirtualMachine");
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }

        throw new RuntimeException("Can't find a vm implementation for the operational system: " + System.getProperty("os.name"));
    }

    public void attachAgentToJVM(File agentFile) throws IOException, AgentLoadException, AgentInitializationException
    {
        VirtualMachine vm = getVirtualMachine();

        if (vm == null)
        {
            throw new RuntimeException("Not found VirtualMachine");
        }

        try
        {
            vm.loadAgent(agentFile.getCanonicalPath());
        }
        finally
        {
            vm.detach();
        }
    }
}
