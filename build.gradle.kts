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

import org.gradle.internal.jvm.Jvm

plugins {
    id("com.github.johnrengelman.shadow") version "5.2.0"
    kotlin("jvm") version "1.4.0"
    `maven-publish`
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        maven("https://repo.maven.apache.org/maven2/")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven(url = "https://repo.dmulloy2.net/nexus/repository/public/")
        if (project.name != "api") { // craftbukkit
            mavenLocal()
        }
    }

    dependencies {
        compileOnly(kotlin("stdlib-jdk8"))
        compileOnly("com.comphenix.protocol:ProtocolLib:4.6.0-SNAPSHOT")
    }

    group = requireNotNull(properties["pluginGroup"]) { "Group is undefined in properties" }
    version = requireNotNull(properties["pluginVersion"]) { "Version is undefined in properties" }

    tasks {
        compileJava {
            options.encoding = "UTF-8"
        }
        javadoc {
            options.encoding = "UTF-8"
        }
    }
}

subprojects {
    apply(plugin = "maven-publish")

    dependencies {
        testImplementation(group = "junit", name = "junit", version = "4.13")

        if (project.name != "api") {
            implementation(project(":api"))
        }
    }

    tasks {
        create<Jar>("sourcesJar") {
            archiveClassifier.set("sources")
            from(sourceSets["main"].allSource)
        }

        if (project.name == "api") {
            processResources {
                filesMatching("**/*.yml") {
                    expand(project.properties)
                }
            }
        }
    }

    publishing {
        publications {
            create<MavenPublication>("Tap") {
                val parent = parent!!
                artifactId = project.name.let { if (it == "api") parent.name else "${parent.name}-${project.name}" }
                from(components["java"])
                artifact(tasks["sourcesJar"])
            }
        }
    }

    if (project.name != "api") {
        tasks.forEach { task ->
            if (task.name != "clean") {
                task.onlyIf {
                    gradle.taskGraph.hasTask(":shadowJar") || parent!!.hasProperty("withNMS")
                }
            }
        }
    }
}

project(":api") {
    dependencies {
        compileOnly(files(Jvm.current().toolsJar))
        compileOnly("com.destroystokyo.paper:paper-api:1.13.2-R0.1-SNAPSHOT")
        implementation("it.unimi.dsi:fastutil:8.3.1")
    }

    tasks {
        processResources {
            filesMatching("**.*.yml") {
                expand(project.properties)
            }
        }
    }
}

dependencies {
    subprojects {
        implementation(this)
    }
}

tasks {
    shadowJar {
        archiveClassifier.set("dist")
    }
    create<Copy>("distJar") {
        from(shadowJar)
        into("W:\\Servers\\tap-1.16.2\\plugins")
    }
}

if (!hasProperty("debug")) {
    tasks {
        shadowJar {
            relocate("it.unimi.dsi", "com.github.noonmaru.tap.shaded.it.unimi.dsi")
        }
    }
}
