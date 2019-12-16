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

package com.github.noonmaru.tap.event;

import com.github.noonmaru.tools.asm.ClassDefiner;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Warning;
import org.bukkit.event.*;
import org.bukkit.plugin.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

import static org.objectweb.asm.Opcodes.*;

public class ASMEventExecutor
{
    private static final HashMap<Method, EventExecutor> CACHE = new HashMap<>();

    private static final String[] THROWS = new String[]{Type.getInternalName(EventException.class)};

    private static final String EXE_DESC = Type.getMethodDescriptor(EventExecutor.class.getMethods()[0]);

    private static final String OBJECT_NAME = Type.getInternalName(Object.class);

    private static final String GET_CLASS_DESC;

    private static final String CLASS_NAME = Type.getInternalName(Class.class);

    private static final String IS_ASSIGNABLE_FROM_DESC;

    private static final String THROWABLE_NAME = Type.getInternalName(Throwable.class);

    private static final String EVENT_EXCE_NAME = Type.getInternalName(EventException.class);

    private static final String EVENT_EXCE_CONS_DESC;

    private static int IDs;

    static
    {
        String getClassDesc = null;
        String isAssignableFrom = null;
        String eventExceptionConsDesc = null;

        try
        {
            getClassDesc = Type.getMethodDescriptor(Object.class.getMethod("getClass"));
            isAssignableFrom = Type.getMethodDescriptor(Class.class.getMethod("isAssignableFrom", Class.class));
            eventExceptionConsDesc = Type.getConstructorDescriptor(EventException.class.getConstructor(Throwable.class));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        GET_CLASS_DESC = getClassDesc;
        IS_ASSIGNABLE_FROM_DESC = isAssignableFrom;
        EVENT_EXCE_CONS_DESC = eventExceptionConsDesc;
    }

    public static void registerEvents(Listener listener, Plugin plugin)
    {
        Validate.notNull(plugin, "Plugin can not be null");
        Validate.notNull(listener, "Listener can not be null");

        if (!plugin.isEnabled())
            throw new IllegalPluginAccessException("Plugin attempted to register " + listener + " while not enabled");

        for (Entry<Class<? extends Event>, Set<RegisteredListener>> entry : createRegisteredListeners(listener, plugin).entrySet())
        {
            getHandlerList(entry.getKey()).registerAll(entry.getValue());
        }
    }

    private static HandlerList getHandlerList(Class<? extends Event> clazz)
    {
        try
        {
            Method method = clazz.getDeclaredMethod("getHandlerList");
            method.setAccessible(true);

            return (HandlerList) method.invoke(null);
        }
        catch (NoSuchMethodException e)
        {
            Class<?> superClass = clazz.getSuperclass();

            if (superClass != null && !clazz.getSuperclass().equals(Event.class) && Event.class.isAssignableFrom(superClass))
            {
                return getHandlerList(superClass.asSubclass(Event.class));
            }

            throw new IllegalPluginAccessException("Unable to find handler list for event " + clazz.getName());
        }
        catch (Exception e)
        {
            throw new IllegalPluginAccessException(e.toString());
        }
    }

    private static Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(Listener listener, Plugin plugin)
    {
        boolean useTimings = Bukkit.getPluginManager().useTimings();

        Map<Class<? extends Event>, Set<RegisteredListener>> ret = new HashMap<>();
        Set<Method> methods;

        try
        {
            Method[] publicMethods = listener.getClass().getMethods();
            methods = new HashSet<>(publicMethods.length, 3.4028235E+38F);

            Collections.addAll(methods, publicMethods);
            Collections.addAll(methods, listener.getClass().getDeclaredMethods());
        }
        catch (NoClassDefFoundError e)
        {
            plugin.getLogger().severe("Plugin " + plugin.getDescription().getFullName() + " has failed to register events for " + listener.getClass() + " because "
                    + e.getMessage() + " does not exist.");
            return ret;
        }

        ClassLoader loader = listener.getClass().getClassLoader();

        for (Method method : methods)
        {
            EventHandler eh = method.getAnnotation(EventHandler.class);

            if (eh == null)
                continue;

            Class<?> checkClass;

            if (method.getParameterTypes().length != 1 || !Event.class.isAssignableFrom(checkClass = method.getParameterTypes()[0]))
            {
                plugin.getLogger().severe(plugin.getDescription().getFullName() + " attempted to register an invalid EventHandler method signature \""
                        + method.toGenericString() + "\" in " + listener.getClass());

                continue;
            }

            Class<? extends Event> eventClass = checkClass.asSubclass(Event.class);
            method.setAccessible(true);
            Set<RegisteredListener> eventSet = ret.computeIfAbsent(eventClass, k -> new HashSet<>());

            for (Class<?> clazz = eventClass; Event.class.isAssignableFrom(clazz); clazz = clazz.getSuperclass())
            {
                if (clazz.getAnnotation(Deprecated.class) != null)
                {
                    Warning warning = clazz.getAnnotation(Warning.class);
                    Warning.WarningState warningState = Bukkit.getWarningState();

                    if (!(warningState.printFor(warning)))
                    {
                        break;
                    }

                    plugin.getLogger().log(Level.WARNING, String.format(
                            "\"%s\" has registered a listener for %s on method \"%s\", but the event is Deprecated. \"%s\"; please notify the authors %s.",
                            plugin.getDescription().getFullName(), clazz.getName(), method.toGenericString(),
                            (warning != null && warning.reason().length() != 0) ? warning.reason() : "Server performance will be affected",
                            Arrays.toString(plugin.getDescription().getAuthors().toArray())
                            ),
                            (warningState == Warning.WarningState.ON) ? new AuthorNagException(null) : null
                    );

                    break;
                }
            }

            try
            {
                EventExecutor executor = createEventExecutor(loader, method, eventClass, plugin);

                if (useTimings)
                {
                    eventSet.add(new TimedRegisteredListener(listener, executor, eh.priority(), plugin, eh.ignoreCancelled()));
                }
                else
                {
                    eventSet.add(new RegisteredListener(listener, executor, eh.priority(), plugin, eh.ignoreCancelled()));
                }
            }
            catch (Exception e)
            {
                throw new AssertionError(e);
            }
        }
        return ret;
    }

    private static EventExecutor createEventExecutor(ClassLoader loader, final Method method, final Class<? extends Event> eventClass, Plugin plugin)
            throws InstantiationException, IllegalAccessException
    {
        if (CACHE.containsKey(method))
            return CACHE.get(method);

        if (!Modifier.isPublic(method.getModifiers()))
        {
            plugin.getLogger().log(Level.WARNING,
                    String.format("'%s' listener method '%s' is not public, it will create reflection EventExecutor (it's slower than asm EventExecutor)",
                            plugin.getDescription().getName(), method.toString()
                    )
            );

            method.setAccessible(true);

            EventExecutor executor = (listener, event) -> {
                try
                {
                    if (!eventClass.isAssignableFrom(event.getClass()))
                        return;

                    method.invoke(listener, event);
                }
                catch (InvocationTargetException ex)
                {
                    throw new EventException(ex.getCause());
                }
                catch (Throwable t)
                {
                    throw new EventException(t);
                }
            };

            CACHE.put(method, executor);

            return executor;
        }

        String name = generateClassName();
        String desc = name.replace('.', '/');

        ClassWriter cw = new ClassWriter(0);

        cw.visit(V1_6, ACC_PUBLIC | ACC_SUPER, desc, null, OBJECT_NAME, new String[]{Type.getInternalName(EventExecutor.class)});
        cw.visitSource(".dynamic", null);

        {
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, OBJECT_NAME, "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }

        {
            String instType = Type.getInternalName(method.getDeclaringClass());
            String eventType = Type.getInternalName(eventClass);

            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "execute", EXE_DESC, null, THROWS);
            mv.visitCode();

            mv.visitLdcInsn(Type.getType(eventClass));
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_NAME, "getClass", GET_CLASS_DESC, false);
            mv.visitMethodInsn(INVOKEVIRTUAL, CLASS_NAME, "isAssignableFrom", IS_ASSIGNABLE_FROM_DESC, false);

            Label tryStart = new Label();
            Label tryEnd = new Label();
            Label catchStart = new Label();
            Label catchEnd = new Label();

            mv.visitJumpInsn(IFNE, tryStart);
            mv.visitInsn(RETURN);
            mv.visitTryCatchBlock(tryStart, tryEnd, catchStart, THROWABLE_NAME);
            mv.visitLabel(tryStart);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, instType);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitTypeInsn(CHECKCAST, eventType);
            mv.visitMethodInsn(INVOKEVIRTUAL, instType, method.getName(), Type.getMethodDescriptor(method), false);
            mv.visitJumpInsn(GOTO, catchEnd);
            mv.visitLabel(tryEnd);
            mv.visitLabel(catchStart);
            mv.visitVarInsn(ASTORE, 3);
            mv.visitTypeInsn(NEW, EVENT_EXCE_NAME);
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKESPECIAL, EVENT_EXCE_NAME, "<init>", EVENT_EXCE_CONS_DESC, false);
            mv.visitInsn(ATHROW);
            mv.visitLabel(catchEnd);
            mv.visitInsn(RETURN);
            mv.visitMaxs(3, 4);
            mv.visitEnd();
        }

        EventExecutor executor = (EventExecutor) ClassDefiner.defineClass(name, cw.toByteArray(), loader).newInstance();
        CACHE.put(method, executor);

        return executor;
    }

    private static String generateClassName()
    {
        return ASMEventExecutor.class.getName() + "_" + IDs++;
    }

}