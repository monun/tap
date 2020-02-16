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

package com.github.noonmaru.tap.command

/**
 * @author Nemo
 */
class CommandContainer internal constructor(val label: String, init: CommandContainer.() -> CommandComponent) {

    val component: CommandComponent = init()

    var usage: String? = null

    var description: String? = null

    var permission: String? = null

    var permissionMessage: String = "권한이 없습니다."

}