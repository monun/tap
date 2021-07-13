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

package io.github.monun.tap.fake

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