/*
 * Copyright (c) 2020 Noonmaru
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

package com.github.noonmaru.tap.fake

import com.google.common.collect.ImmutableList
import org.bukkit.Location

class FakeProjectileManager {
    val projectiles: List<FakeProjectile>
        get() = ImmutableList.copyOf(_projectiles)

    private val _projectiles = ArrayList<FakeProjectile>()

    fun launch(location: Location, projectile: FakeProjectile) {
        projectile.checkState()
        require(!projectile.launched) { "Already launched projectile" }

        projectile.init(location)
        _projectiles += projectile
    }

    fun update() {
        updateProjectiles()
    }

    private fun updateProjectiles() {
        val projectiles = _projectiles
        val iterator = projectiles.iterator()

        while (iterator.hasNext()) {
            val projectile = iterator.next()

            projectile.update()

            if (!projectile.isValid)
                iterator.remove()
        }
    }

    fun clear() {
        for (projectile in _projectiles) {
            projectile.remove()
        }
    }
}