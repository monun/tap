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

package com.github.noonmaru.tap.parallel

class ParallelChunk(
    val world: ParallelWorld,
    val x: Int,
    val z: Int
) {
    private val entities = ArrayList<ParallelEntity>()

    val isEmpty: Boolean
        get() = entities.isEmpty()

    private val sections = arrayOfNulls<ParallelSection>(16)

    private fun getSectionAt(floor: Int): ParallelSection? {
        return sections[floor]
    }

    private fun getOrCreateSectionAt(floor: Int): ParallelSection {
        var section = sections[floor]

        if (section == null) {
            section = ParallelSection()
            sections[floor] = section
        }

        return section
    }

    internal fun moveEntity(entity: ParallelEntity, oldFloor: Int, newFloor: Int) {
        getSectionAt(oldFloor)?.removeEntity(entity)
        getOrCreateSectionAt(newFloor).addEntity(entity)
    }

    internal fun removeEntity(entity: ParallelEntity, floor: Int) {
        entities.remove(entity)
        getSectionAt(floor)?.removeEntity(entity)
    }

    internal fun addEntity(entity: ParallelEntity, floor: Int) {
        entities += entity
        getOrCreateSectionAt(floor).addEntity(entity)
    }
}

class ParallelSection {
    private val entities = ArrayList<ParallelEntity>()

    fun addEntity(entity: ParallelEntity) {
        entities += entity
    }

    fun removeEntity(entity: ParallelEntity) {
        entities -= entity
    }
}