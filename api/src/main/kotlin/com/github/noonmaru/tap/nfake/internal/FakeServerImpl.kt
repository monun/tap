/*
 * Copyright (c) $date.year Noonmaru
 *
 *  Licensed under the General Public License, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.noonmaru.tap.nfake.internal

import com.github.noonmaru.tap.fake.createFakeEntity
import com.github.noonmaru.tap.nfake.FakeEntity
import com.github.noonmaru.tap.nfake.FakeServer
import com.github.noonmaru.tap.nfake.FakeWorld
import com.google.common.collect.ImmutableList
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import java.util.*

class FakeServerImpl
    : FakeServer {
    private val worldsByWorld = WeakHashMap<World, FakeWorldImpl>()

    override val worlds: List<FakeWorld>
        get() = ImmutableList.copyOf(worldsByWorld.values)

    override fun adaptWorld(bukkitWorld: World): FakeWorldImpl {
        return worldsByWorld.computeIfAbsent(bukkitWorld) {
            FakeWorldImpl(this, it)
        }
    }

    override fun spawnEntity(location: Location, clazz: Class<out Entity>): FakeEntity {
        val bukkitWorld = location.world
        val bukkitEntity = requireNotNull(clazz.createFakeEntity(bukkitWorld)) {
            "Cannot create entity $clazz"
        }

        val fakeWorld = adaptWorld(bukkitWorld)
        val fakeEntity = FakeEntityImpl(fakeWorld, bukkitEntity, location)
        fakeWorld.addEntity(fakeEntity)

        return fakeEntity
    }

    override fun update() {
        for (world in worldsByWorld.values) {
            world.update()
        }
    }

    override fun destroyAll() {
        TODO("Not yet implemented")
    }

    fun enqueue(fakeEntityImpl: FakeEntityImpl) {

    }
}