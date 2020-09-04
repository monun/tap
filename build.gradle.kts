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

plugins {
    kotlin("jvm") version "1.4.0"
    `maven-publish`
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://papermc.io/repo/repository/maven-public/")
        maven(url = "https://repo.dmulloy2.net/nexus/repository/public/")
    }

    dependencies {
        compileOnly(kotlin("stdlib-jdk8"))
        compileOnly("com.comphenix.protocol:ProtocolLib:4.6.0-SNAPSHOT")

        testImplementation("junit:junit:4.13")
        testImplementation("org.mockito:mockito-core:3.3.3")
        testImplementation("org.powermock:powermock-module-junit4:2.0.7")
        testImplementation("org.powermock:powermock-api-mockito2:2.0.7")
        testImplementation("org.slf4j:slf4j-api:1.7.25")
        testImplementation("org.apache.logging.log4j:log4j-core:2.8.2")
        testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:2.8.2")
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
    dependencies {
        if (project.name != "api") {
            implementation(project(":api"))
        }
    }
}

project(":api") {
    dependencies {
        compileOnly("com.destroystokyo.paper:paper-api:1.13.2-R0.1-SNAPSHOT")

        testImplementation("org.spigotmc:spigot:1.13.2-R0.1-SNAPSHOT")
    }

    tasks {
        processResources {
            filesMatching("**.*.yml") {
                expand(project.properties)
            }
        }
    }
}

tasks {
    jar {
        for (subproject in subprojects) {
            from(subproject.sourceSets["main"].output)
        }
    }
    create<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        for (subproject in subprojects) {
            from(subproject.sourceSets["main"].allSource)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("Tap") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
        }
    }
}