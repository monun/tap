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

package com.github.noonmaru.tap.fake

import com.github.noonmaru.tap.loader.LibraryLoader
import org.bukkit.Location
import org.bukkit.entity.Entity

/**
 * @author Nemo
 */
interface FakeSupport {

    fun getNetworkId(entity: Entity): Int

    fun <T : Entity> createEntity(entityClass: Class<out Entity>): T?

    fun setPosition(entity: Entity, loc: Location)

    fun setPositionAndRotation(entity: Entity, loc: Location)

    fun setInvisible(entity: Entity, invisible: Boolean)

    fun isInvisible(entity: Entity): Boolean
}

internal val FakeSupportNMS = LibraryLoader.load(FakeSupport::class.java)

val Entity.networkId
    get() = FakeSupportNMS.getNetworkId(this)

var Entity.invisible
    get() = FakeSupportNMS.isInvisible(this)
    set(value) {
        FakeSupportNMS.setInvisible(this, value)
    }

fun <T : Entity> Class<T>.createFakeEntity(): T? {
    return FakeSupportNMS.createEntity(this)
}

fun Entity.setPosition(loc: Location) {
    FakeSupportNMS.setPosition(this, loc)
}

fun Entity.setPositionAndRotation(loc: Location) {
    FakeSupportNMS.setPositionAndRotation(this, loc)
}