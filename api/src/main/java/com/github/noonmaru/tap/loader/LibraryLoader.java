/*
 * Copyright (c) 2020 Noonmaru
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

package com.github.noonmaru.tap.loader;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.ConstructorUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NMS Library의 로드를 돕는 클래스입니다.
 * <p>
 * x_x_Rx 로 패키지 이름을 정하면 해당 클래스가 로딩됩니다.
 *
 * @author Noonmaru
 */
public final class LibraryLoader {
    private LibraryLoader() {
    }

    private static <T> Class<? extends T> findClass(List<String> candidates, Class<T> type) throws ClassNotFoundException {
        for (String className : candidates) {
            try {
                return Class.forName(className, true, type.getClassLoader()).asSubclass(type);
            } catch (ClassNotFoundException ignored) {
            }
        }

        throw new ClassNotFoundException("Not found nms library class: " + candidates.toString());
    }

    @SuppressWarnings("unchecked")
    public static <T> T load(@NotNull final String packageName, @NotNull final String className, @NotNull final Class<T> type, @NotNull final Object... initargs) {
        Class[] parameterTypes = ClassUtils.toClass(initargs);

        List<String> candiates = new ArrayList<>(2);

        String bukkitVersion = getBukkitVersion();

        candiates.add(packageName + '.' + bukkitVersion + '.' + className);

        int lastDot = packageName.lastIndexOf('.');

        if (lastDot > 0) {
            String superPackageName = packageName.substring(0, lastDot);
            String subPackageName = packageName.substring(lastDot + 1);
            candiates.add(superPackageName + '.' + bukkitVersion + '.' + subPackageName + '.' + className);
        }

        try {
            Class<? extends T> nmsClass = findClass(candiates, type);
            Constructor<?> constructor = ConstructorUtils.getMatchingAccessibleConstructor(nmsClass, parameterTypes);

            if (constructor == null)
                throw new UnsupportedOperationException(type.getName() + " does not have Constructor for [" + StringUtils.join(parameterTypes, ", ") + "]");

            return (T) constructor.newInstance(initargs);
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException(type.getName() + " does not support this version (" + getMinecraftVersion() + ")", e);
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException(type.getName() + " constructor is not visible");
        } catch (InstantiationException e) {
            throw new UnsupportedOperationException(type.getName() + " is abstract class");
        } catch (InvocationTargetException e) {
            throw new UnsupportedOperationException(type.getName() + " has an error occurred while creating the instance", e);
        }
    }

    @NotNull
    public static <T> T load(@NotNull final Class<T> type, @NotNull final Object... initargs) {
        String name = StringUtils.removeStart(type.getSimpleName(), "Tap");

        return load(type.getPackage().getName(), "NMS" + name, type, initargs);
    }

    /**
     * 버킷 버전을 반환합니다.
     *
     * @return 버킷 버전
     */
    public static String getBukkitVersion() {
        Matcher matcher = Pattern.compile("v\\d+_\\d+_R\\d+").matcher(Bukkit.getServer().getClass().getPackage().getName());

        return matcher.find() ? matcher.group() : null;
    }

    /**
     * 마인크래프트 버전을 반환합니다.
     *
     * @return 마인크래프트 버전
     */
    public static String getMinecraftVersion() {
        Matcher matcher = Pattern.compile("(\\(MC: )([\\d.]+)(\\))").matcher(Bukkit.getVersion());

        return matcher.find() ? matcher.group(2) : null;
    }
}
