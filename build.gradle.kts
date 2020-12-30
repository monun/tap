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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.OutputStream


plugins {
    kotlin("jvm") version "1.4.21"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    `maven-publish`
}

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

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
        maven("https://papermc.io/repo/repository/maven-public/")
        mavenLocal()
    }

    dependencies {
        compileOnly(kotlin("stdlib"))
        compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")
        compileOnly(rootProject.fileTree("dir" to "libs", "include" to "*.jar"))

        implementation("org.mozilla:rhino:1.7.13")

        testImplementation("junit:junit:4.13")
        testImplementation("org.mockito:mockito-core:3.3.3")
        testImplementation("org.powermock:powermock-module-junit4:2.0.7")
        testImplementation("org.powermock:powermock-api-mockito2:2.0.7")
        testImplementation("org.slf4j:slf4j-api:1.7.25")
        testImplementation("org.apache.logging.log4j:log4j-core:2.8.2")
        testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:2.8.2")
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "11"
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
            compileOnly("com.destroystokyo.paper:paper-api:1.13.2-R0.1-SNAPSHOT")

            testImplementation("com.destroystokyo.paper:paper-api:1.13.2-R0.1-SNAPSHOT")
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
    create<Copy>("paper") {
        from(shadowJar)
        var dest = file(".paper/plugins")
        if (File(dest, shadowJar.get().archiveFileName.get()).exists()) dest = File(dest, "update")
        into(dest)
    }
    shadowJar {
        archiveClassifier.set("")
        exclude("LICENSE.txt") // mpl
        gradle.taskGraph.whenReady {
            if (hasTask(":publishTapPublicationToMavenLocal")) { // maven publish
                dependencies {
                    exclude(project(":paper"))
                }
            } else {
                archiveVersion.set("")
                archiveBaseName.set(project(":paper").property("pluginName").toString())
            }
        }
    }
    create<DefaultTask>("setupWorkspace") {
        doLast {
            val versions = arrayOf(
                "1.16.4",
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

            registering(de.undercouch.gradle.tasks.download.Download::class) {
                src("https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar")
                dest(buildtools)
                download()
            }
            runCatching {
                for (v in missingVersions) {
                    println("Downloading spigot-$v...")

                    javaexec {
                        workingDir(buildtoolsDir)
                        main = "-jar"
                        args = listOf("./${buildtools.name}", "--rev", v)
                        // Silent
                        standardOutput = OutputStream.nullOutputStream()
                        errorOutput = OutputStream.nullOutputStream()
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