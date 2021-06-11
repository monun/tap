/*
 * Copyright 2021 Monun
 *
 * Licensed under the Apache License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://opensource.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.io.IOException
import java.io.OutputStream

plugins {
    kotlin("jvm") version "1.5.10"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    `maven-publish`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

/*
// ProtocolLib 파일 다운로드 링크 (저장소 응답 없을시 사용)
downloadLibrary(
    "https://ci.dmulloy2.net/job/ProtocolLib/lastSuccessfulBuild/artifact/target/ProtocolLib.jar",
    "ProtocolLib.jar"
)

fun downloadLibrary(url: String, fileName: String) {
    val parent = File(projectDir, "libs").also {
        it.mkdirs()
    }
    val jar = File(parent, fileName)

    uri(url).toURL().openConnection().run {
        val lastModified = lastModified
        if (lastModified != jar.lastModified()) {
            inputStream.use { stream ->
                jar.writeBytes(stream.readBytes())
                jar.setLastModified(lastModified)
            }
        }
    }
}
*/

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenLocal()
        mavenCentral()
        maven(url = "https://papermc.io/repo/repository/maven-public/")
        maven(url = "https://repo.dmulloy2.net/repository/public/")
    }

    dependencies {
        compileOnly(kotlin("stdlib"))
        compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
        compileOnly("com.comphenix.protocol:ProtocolLib:4.6.0")
//        compileOnly(rootProject.fileTree("dir" to "libs", "include" to "*.jar"))

        implementation("org.mariuszgromada.math:MathParser.org-mXparser:4.4.2")

        testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")
        testImplementation("org.mockito:mockito-core:3.6.28")
        testImplementation("org.spigotmc:spigot:1.16.5-R0.1-SNAPSHOT")
    }

    tasks {
        test {
            useJUnitPlatform()
        }
    }
}

project(":paper") {
    dependencies {
        implementation(project(":api"))

        subprojects.filter { it.name != path }.forEach { subproject ->
            implementation(subproject)
        }
    }
}

subprojects {
    if (path in setOf(":api", ":paper")) {
        // setup api & test plugin
        dependencies {
            compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")

            testImplementation("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
        }
    } else {
        //setup nms
        dependencies {
            implementation(project(":api"))
        }
    }
}

dependencies {
    implementation(project(":api"))
    implementation(project(":paper"))
    implementation(fileTree("dir" to ".jitpack", "include" to "*.jar"))
}

tasks {
    jar {
        subprojects.filter { it.name != ":paper" }.forEach { subproject ->
            from(subproject.sourceSets["main"].output)
        }
    }
    create<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        for (subproject in subprojects) {
            from(subproject.sourceSets["main"].allSource)
        }
    }
    shadowJar {
        archiveClassifier.set("") // for publish
        exclude("LICENSE.txt") // mpl
        dependencies { exclude(project(":paper")) }
        relocate("org.mariuszgromada.math", "${rootProject.group}.${rootProject.name}.org.mariuszgromada.math")
    }
    create<ShadowJar>("debugJar") {
        archiveBaseName.set("Tap")
        archiveVersion.set("") // For bukkit plugin update
        archiveClassifier.set("DEBUG")
        from(sourceSets["main"].output)
        configurations = listOf(project.configurations.implementation.get().apply { isCanBeResolved = true })

        var dest = File(rootDir, ".server/plugins")
        val pluginName = archiveFileName.get()
        val pluginFile = File(dest, pluginName)
        if (pluginFile.exists()) dest = File(dest, "update")

        doLast {
//            dest.mkdirs()
            copy {
                from(archiveFile)
                into(dest)
            }
        }
    }
    create<DefaultTask>("setupWorkspace") {
        doLast {
            val versions = arrayOf(
                "1.17",
                "1.16.5",
                "1.16.3",
                "1.16.1",
                "1.15.2",
                "1.14.4",
                "1.13.2"
            )
            val buildtoolsDir = File(".buildtools")
            val buildtools = File(buildtoolsDir, "BuildTools.jar")

            val maven = File(System.getProperty("user.home"), ".m2/repository/org/spigotmc/spigot/")
            val repos = maven.listFiles { file: File -> file.isDirectory } ?: emptyArray()
            val missingVersions = versions.filter { version ->
                repos.find { it.name.startsWith(version) }?.also { println("Skip downloading spigot-$version") } == null
            }.also { if (it.isEmpty()) return@doLast }

            val download by registering(de.undercouch.gradle.tasks.download.Download::class) {
                src("https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar")
                dest(buildtools)
            }
            download.get().download()

            runCatching {
                for (v in missingVersions) {
                    println("Downloading spigot-$v...")

                    javaexec {
                        workingDir(buildtoolsDir)
                        main = "-jar"
                        args = listOf("./${buildtools.name}", "--rev", v)//, "-Xmx16384M", "-Xms16384M", "-Xmn16384m")
                        // Silent
                        standardOutput = nullOutputStream()
                        errorOutput = nullOutputStream()
                    }
                }
            }.onFailure {
                it.printStackTrace()
            }
            buildtoolsDir.deleteRecursively()
        }
    }
    create<Jar>("jitpack") {
        dependsOn(subprojects.map { it.tasks.getByName("classes") })
        from(subprojects.filter { it.name.startsWith("v") }.map { it.sourceSets["main"].output })
        destinationDirectory.set(file(".jitpack"))
        archiveVersion.set("")
        archiveBaseName.set("jitpack")
    }
    build {
        dependsOn(named("jitpack"))
        finalizedBy(shadowJar)
    }
}

publishing {
    publications {
        create<MavenPublication>("Tap") {
            project.shadow.component(this)
            artifact(tasks["sourcesJar"])
        }
    }
}

fun nullOutputStream(): OutputStream {
    return object : OutputStream() {
        @Volatile
        private var closed = false

        private fun ensureOpen() {
            if (closed) {
                throw IOException("Stream closed")
            }
        }

        override fun write(b: Int) {
            ensureOpen()
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            ensureOpen()
        }

        override fun close() {
            closed = true
        }
    }
}