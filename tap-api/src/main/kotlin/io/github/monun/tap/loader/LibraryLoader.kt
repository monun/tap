/*
 * Copyright (C) 2022 Monun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.monun.tap.loader

import org.bukkit.Bukkit
import java.lang.reflect.InvocationTargetException

object LibraryLoader {

    /**
     * 구현 라이브러리 인스턴스를 로드합니다
     *
     * 패키지는 `<[type]의 패키지>.internal.<[type]의 이름>+Impl` 입니다.
     *
     * ex) `io.github.sample.Sample -> io.github.sample.internal.SampleImpl`
     */
    fun <T> loadImplement(type: Class<T>, vararg initArgs: Any? = emptyArray()): T {
        val packageName = type.`package`.name
        val className = "${type.simpleName}Impl"
        val parameterTypes = initArgs.map { it?.javaClass }.toTypedArray()

        return try {
            val internalClass =
                Class.forName("$packageName.internal.$className", true, type.classLoader).asSubclass(type)

            val constructor = kotlin.runCatching {
                internalClass.getConstructor(*parameterTypes)
            }.getOrNull()
                ?: throw UnsupportedOperationException("${type.name} does not have Constructor for [${parameterTypes.joinToString()}]")
            constructor.newInstance(*initArgs) as T
        } catch (exception: ClassNotFoundException) {
            throw UnsupportedOperationException("${type.name} a does not have implement", exception)
        } catch (exception: IllegalAccessException) {
            throw UnsupportedOperationException("${type.name} constructor is not visible")
        } catch (exception: InstantiationException) {
            throw UnsupportedOperationException("${type.name} is abstract class")
        } catch (exception: InvocationTargetException) {
            throw UnsupportedOperationException(
                "${type.name} has an error occurred while creating the instance",
                exception
            )
        }
    }

    /**
     * net.minecraft.server 를 지원하는 라이브러리 인스턴스를 로드합니다.
     *
     * 패키지는 <[type]의 패키지>.[minecraftVersion].NMS + <[type]의 이름> 입니다.
     *
     *
     * ex) `io.github.sample.Sample -> io.github.sample.v1_18_R1.NMSSample`
     */
    fun <T> loadNMS(type: Class<T>, vararg initArgs: Any? = emptyArray()): T {
        val packageName = type.`package`.name
        val className = "NMS${type.simpleName}"
        val parameterTypes = initArgs.map {
            it?.javaClass
        }.toTypedArray()

        val candidates = ArrayList<String>(2)
        candidates.add("$packageName.$libraryVersion.$className")

        val lastDot = packageName.lastIndexOf('.')
        if (lastDot > 0) {
            val superPackageName = packageName.substring(0, lastDot)
            val subPackageName = packageName.substring(lastDot + 1)
            candidates.add("$superPackageName.$libraryVersion.$subPackageName.$className")
        }

        return try {
            val nmsClass = candidates.firstNotNullOfOrNull { candidate ->
                try {
                    Class.forName(candidate, true, type.classLoader).asSubclass(type)
                } catch (exception: ClassNotFoundException) {
                    null
                }
            } ?: throw ClassNotFoundException("Not found nms library class: $candidates")
            val constructor = kotlin.runCatching {
                nmsClass.getConstructor(*parameterTypes)
            }.getOrNull()
                ?: throw UnsupportedOperationException("${type.name} does not have Constructor for [${parameterTypes.joinToString()}]")
            constructor.newInstance(*initArgs) as T
        } catch (exception: ClassNotFoundException) {
            throw UnsupportedOperationException(
                "${type.name} does not support this version: $libraryVersion",
                exception
            )
        } catch (exception: IllegalAccessException) {
            throw UnsupportedOperationException("${type.name} constructor is not visible")
        } catch (exception: InstantiationException) {
            throw UnsupportedOperationException("${type.name} is abstract class")
        } catch (exception: InvocationTargetException) {
            throw UnsupportedOperationException(
                "${type.name} has an error occurred while creating the instance",
                exception
            )
        }
    }

    val bukkitVersion by lazy {
        Bukkit.getServer().bukkitVersion
    }

    val minecraftVersion by lazy {
        Bukkit.getServer().minecraftVersion
    }

    val libraryVersion by lazy { "v${minecraftVersion.replace('.', '_')}" }
}