/*
 * Copyright (c) 2019 Noonmaru
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

package com.github.noonmaru.tap.debug;

import com.github.noonmaru.tap.debug.block.DebugBlock;
import com.github.noonmaru.tap.debug.event.entity.EntityEventDebug;
import com.github.noonmaru.tap.debug.text.TextDebug;
import com.google.common.base.Suppliers;

import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;

public final class DebugManager
{

    private final TreeMap<String, DebugModule> modulesByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private final Supplier<Set<String>> moduleNames = Suppliers.memoize(modulesByName::keySet);

    private final Supplier<Collection<DebugModule>> modules = Suppliers.memoize(modulesByName::values);

    public DebugManager()
    {
        registerModule("block", DebugBlock::new);
        registerModule("text", TextDebug::new);
        registerModule("entity.event", EntityEventDebug::new);
    }

    private void registerModule(String name, Supplier<? extends DebugProcess> processSupplier)
    {
        if (modulesByName.containsKey(name))
            throw new IllegalArgumentException("Name " + name + " is already in use");

        modulesByName.put(name, new DebugModule(name, processSupplier));
    }

    public DebugModule getModule(String moduleName)
    {
        return modulesByName.get(moduleName);
    }

    public final Set<String> getModuleNames()
    {
        return moduleNames.get();
    }

    public Collection<DebugModule> getModules()
    {
        return modules.get();
    }

}
