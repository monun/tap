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

package com.github.noonmaru.tap

import com.github.noonmaru.tap.attach.Tools
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Nemo
 */
class TapPlugin : JavaPlugin() {
    override fun onEnable() {
        Tools.loadAttachLibrary(dataFolder)
        //DEBUG (Command DSL)
//        command("tap") {
//            help("help")
//            component("debug") {
//                usage = "[Messages]"
//                description = "디버그 명령입니다."
//                CommandDebug()
//            }
//            component("book") {
//                description = "BookMeta Support를 테스트합니다."
//                CommandDebugBookMeta()
//            }
//        }
    }
}