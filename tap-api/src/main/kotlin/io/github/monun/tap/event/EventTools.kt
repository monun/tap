/*
 * Tap
 * Copyright (C) 2021 Monun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.monun.tap.event

import org.bukkit.event.Event
import org.bukkit.plugin.IllegalPluginAccessException
import java.lang.reflect.ParameterizedType
import java.util.*

object EventTools {
    private val CUSTOM_PROVIDERS = WeakHashMap<Class<*>, EventEntityProvider>()
    private val DEFAULT_PROVIDERS: Array<EventEntityProvider>

    /**
     * [Event]를 상속한 클래스들 중 [org.bukkit.event.HandlerList]가 있는 클래스를 찾아서 반환합니다.
     */
    @JvmStatic
    fun getRegistrationClass(clazz: Class<*>): Class<*> {
        return try {
            clazz.getDeclaredMethod("getHandlerList")
            clazz
        } catch (e: NoSuchMethodException) {
            if (clazz.superclass != null
                    && clazz.superclass != Event::class.java
                    && Event::class.java.isAssignableFrom(clazz.superclass)) {
                getRegistrationClass(clazz.superclass.asSubclass(Event::class.java))
            } else {
                throw IllegalPluginAccessException("Unable to find handler list for event ${clazz.name}. Static getHandlerList method required!")
            }
        }
    }

    /**
     * [DefaultProvider]에서 호환 가능한 제공자를 반환합니다.
     *
     * @param eventClass 찾아낼 클래스
     * @return 호환되는 엔티티 제공자
     * @see DefaultProvider
     */
    @JvmStatic
    fun findDefaultProvider(eventClass: Class<*>): EventEntityProvider {
        for (provider in DEFAULT_PROVIDERS) {
            if (provider.eventClass.isAssignableFrom(eventClass))
                return provider
        }

        throw IllegalArgumentException("Not found DefaultProvider for $eventClass")
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun getOrCreateCustomProvide(providerClass: Class<*>): EventEntityProvider {
        return CUSTOM_PROVIDERS.computeIfAbsent(providerClass) { clazz: Class<*> ->
            try {
                return@computeIfAbsent EventEntityProvider(clazz.asSubclass(EntityProvider::class.java).getConstructor().newInstance() as EntityProvider<Event>)
            } catch (e: InstantiationException) {
                throw AssertionError(e)
            } catch (e: IllegalAccessException) {
                throw AssertionError(e)
            }
        }
    }

    @JvmStatic
    fun getGenericEventType(providerClass: Class<*>): Class<*> {
        var clazz = providerClass
        val prefix = EntityProvider::class.java.name + "<" //제너릭 타임 이름은 ClassName<Type>으로 반환됨
        val genericInterfaces = clazz.genericInterfaces

        do {
            for (genericInterface in genericInterfaces) {
                if (genericInterface.typeName.startsWith(prefix)) {
                    return (genericInterface as ParameterizedType).actualTypeArguments[0] as Class<*>
                }
            }
        } while (clazz.superclass.also { clazz = it } != Any::class.java)

        throw IllegalArgumentException("$clazz is not EntityProvider")
    }

    init {
        // 기본 개체 제공자 초기화
        val classes = DefaultProvider::class.java.declaredClasses
        val defaultProviders = ArrayList<EventEntityProvider>(classes.size)

        for (clazz in classes) {
            if (EntityProvider::class.java.isAssignableFrom(clazz)) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    defaultProviders.add(EventEntityProvider(clazz.getConstructor().newInstance() as EntityProvider<Event>))
                } catch (e: Exception) {
                    throw AssertionError(e)
                }
            }
        }

        DEFAULT_PROVIDERS = defaultProviders.toTypedArray()
    }
}