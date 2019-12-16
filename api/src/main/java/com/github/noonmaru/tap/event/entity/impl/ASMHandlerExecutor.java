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

package com.github.noonmaru.tap.event.entity.impl;

import com.github.noonmaru.tap.event.ASMEventExecutor;
import com.github.noonmaru.tap.event.entity.EntityListener;
import com.github.noonmaru.tools.asm.ClassDefiner;
import org.bukkit.event.Event;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.HashMap;

import static org.objectweb.asm.Opcodes.*;

/**
 * {@link EntityListener} 의 리스너 메소드를 호출하기 위한 {@link HandlerExecutor}를 ASM으로 구현하기 위한 클래스입니다.
 *
 * @author Nemo
 */
final class ASMHandlerExecutor
{
    private static final Method METHOD_EXECUTOR;

    private static final HashMap<Method, HandlerExecutor> CACHE = new HashMap<>();

    private static int handlerNumber;

    static
    {
        try
        {
            METHOD_EXECUTOR = HandlerExecutor.class.getMethod("execute", EntityListener.class, Event.class);
        }
        catch (NoSuchMethodException e)
        {
            throw new AssertionError(e);
        }
    }

    /**
     * 동적 바이트코드로 구현된 {@link HandlerExecutor}를 생성합니다.
     *
     * @param method {@link HandlerExecutor}에서 호출할 메소드
     * @return ASM으로 구현된 {@link HandlerExecutor}
     */
    static HandlerExecutor create(Method method)
    {
        return CACHE.computeIfAbsent(method, key -> {
            Class<?> listenerClass = key.getDeclaringClass();
            String className = ASMEventExecutor.class.getName() + "_" + listenerClass.getSimpleName() + "_" + key.getName() + "_" + handlerNumber++;
            byte[] classData = generateClassData(key, className.replace('.', '/'));
            Class<?> executorClass = ClassDefiner.defineClass(className, classData, listenerClass.getClassLoader());

            try
            {
                return (HandlerExecutor) executorClass.newInstance();
            }
            catch (Exception e)
            {
                throw new AssertionError(e);
            }
        });
    }

    /**
     * Method를 호출하기 위한 {@link HandlerExecutor} 클래스를 구현한 바이트코드를 생성합니다.
     *
     * @param method    {@link HandlerExecutor}에서 호출할 메소드
     * @param className ASM으로 구현될 {@link HandlerExecutor}의 클래스 이름
     * @return 바이트코드
     */
    private static byte[] generateClassData(Method method, String className)
    {
        String objectName = Type.getInternalName(Object.class);
        String listenerName = Type.getInternalName(method.getDeclaringClass());

        ClassWriter classWriter = new ClassWriter(0);
        classWriter.visit(V1_8, ACC_PUBLIC | ACC_FINAL | ACC_SUPER, className, null, objectName, new String[]{Type.getInternalName(HandlerExecutor.class)});
        classWriter.visitSource(".dynamic", null);

        //Constructor
        {
            MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, objectName, "<init>", "()V", false);
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {
            MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC, METHOD_EXECUTOR.getName(), Type.getMethodDescriptor(METHOD_EXECUTOR), null, null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitTypeInsn(CHECKCAST, listenerName);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitTypeInsn(CHECKCAST, Type.getInternalName(method.getParameterTypes()[0]));
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, listenerName, method.getName(), Type.getMethodDescriptor(method), false);
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(2, 3);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }
}
