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

package com.github.noonmaru.tap.modhelper.customnpc;

import com.github.noonmaru.tap.LibraryLoader;
import com.nemosw.tools.asm.ClassDefiner;
import org.bukkit.entity.LivingEntity;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;

public abstract class CustomNPC
{

    private static final CustomNPC INSTANCE = LibraryLoader.load(CustomNPC.class);

    private static final NameExtractor EXTRACTOR;

    static
    {
        NameExtractor extractor = null;

        try
        {
            Class<?> customNPC = Class.forName("noppes.npcs.entity.EntityNPCInterface");
            Class<?> display = Class.forName("noppes.npcs.DataDisplay");

            System.out.println("found CustomNPC mod");

            String name = NameExtractor.class.getName().concat(".CustomNPCNameExtractor");
            String desc = name.replace('.', '/');
            String objectDesc = Type.getInternalName(Object.class);

            ClassWriter cw = new ClassWriter(0);
            cw.visit(V1_6, ACC_PUBLIC | ACC_SUPER, desc, null, objectDesc, new String[]{Type.getInternalName(NameExtractor.class)});
            cw.visitSource(".dynamic", null);
            {
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, objectDesc, "<init>", "()V", false);
                mv.visitInsn(RETURN);
                mv.visitMaxs(1, 1);
                mv.visitEnd();
            }
            {
                String customNPCDesc = Type.getInternalName(customNPC);
                String displayDesc = Type.getInternalName(display);

                Method method = NameExtractor.class.getDeclaredMethods()[0];
                MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, method.getName(), Type.getMethodDescriptor(method), null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 1); // load Entity
                mv.visitTypeInsn(INSTANCEOF, customNPCDesc);
                Label l1 = new Label();
                mv.visitJumpInsn(IFEQ, l1);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitTypeInsn(CHECKCAST, Type.getInternalName(customNPC));
                mv.visitFieldInsn(GETFIELD, customNPCDesc, "display", "L" + displayDesc + ";");
                mv.visitFieldInsn(GETFIELD, displayDesc, "name", "L" + Type.getInternalName(String.class) + ";");
                Label l2 = new Label();
                mv.visitJumpInsn(GOTO, l2);
                mv.visitLabel(l1);
                mv.visitInsn(ACONST_NULL);
                mv.visitLabel(l2);
                mv.visitInsn(ARETURN);
                mv.visitMaxs(1, 2);
                mv.visitEnd();
            }

            extractor = ClassDefiner.defineClass(name, cw.toByteArray(), CustomNPC.class.getClassLoader()).asSubclass(NameExtractor.class).newInstance();
        }
        catch (ClassNotFoundException e)
        {
            System.out.println("not found CustomNPC mod");
        }
        catch (InstantiationException | IllegalAccessException impossible)
        {
            throw new AssertionError(impossible);
        }

        EXTRACTOR = extractor;
    }

    public static String getName(LivingEntity entity)
    {
        return EXTRACTOR == null ? null : EXTRACTOR.getName(INSTANCE.getNMSEntity(entity));
    }

    protected abstract Object getNMSEntity(LivingEntity entity);

}
