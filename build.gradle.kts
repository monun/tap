import de.undercouch.gradle.tasks.download.Download
import net.md_5.specialsource.Jar
import net.md_5.specialsource.JarMapping
import net.md_5.specialsource.JarRemapper
import net.md_5.specialsource.provider.JarProvider
import net.md_5.specialsource.provider.JointProvider
import org.apache.tools.ant.taskdefs.condition.Os
import java.io.OutputStream.nullOutputStream
import org.gradle.jvm.tasks.Jar as GradleJar

plugins {
    kotlin("jvm") version "1.5.20"
    `maven-publish`
    signing
    id("org.jetbrains.dokka") version "1.4.32"
}

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("net.md-5:SpecialSource:1.10.0")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
        maven(url = "https://papermc.io/repo/repository/maven-public/")
        mavenLocal()
    }

    dependencies {
        implementation(kotlin("stdlib"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
        implementation("org.mariuszgromada.math:MathParser.org-mXparser:4.4.2")

        testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.2")
        testImplementation("org.mockito:mockito-core:3.6.28")
        testImplementation("org.spigotmc:spigot:1.17-R0.1-SNAPSHOT:remapped-mojang")
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
    }
}

project(":api") {
    apply(plugin = "org.jetbrains.dokka")

    tasks {
        create<GradleJar>("dokkaJar") {
            archiveClassifier.set("javadoc")
            dependsOn("dokkaHtml")

            from("$buildDir/dokka/html/") {
                include("**")
            }
        }
    }
}

subprojects {
    if (path in setOf(":api", ":paper")) {
        // setup api & test plugin
        dependencies {
            compileOnly("io.papermc.paper:paper-api:1.17-R0.1-SNAPSHOT")

            testImplementation("io.papermc.paper:paper-api:1.17-R0.1-SNAPSHOT")
        }
    } else {
        configurations {
            create("mojangMapping")
            create("spigotMapping")
        }

        //setup nms
        dependencies {
            implementation(project(":api"))
        }
    }
}

tasks {
    jar {
        from(project(":paper").sourceSets["main"].output)

        val targetProjects = subprojects.filter { it.path != ":paper" }.onEach { from(it.sourceSets["main"].output) }
        val nmsProjects = targetProjects.filter { it.path != ":api" }

        doLast {
            fun remap(jarFile: File, outputFile: File, mappingFile: File, reversed: Boolean = false) {
                val inputJar = Jar.init(jarFile)

                val mapping = JarMapping()
                mapping.loadMappings(mappingFile.canonicalPath, reversed, false, null, null)

                val provider = JointProvider()
                provider.add(JarProvider(inputJar))
                mapping.setFallbackInheritanceProvider(provider)

                val mapper = JarRemapper(mapping)
                mapper.remapJar(inputJar, outputFile)
                inputJar.close()
            }

            val archiveFile = archiveFile.get().asFile

            val obfOutput = File(archiveFile.parentFile, "remapped-obf.jar")
            val spigotOutput = File(archiveFile.parentFile, "remapped-spigot.jar")

            nmsProjects.forEach { nmsProject ->
                val configurations = nmsProject.configurations
                val mojangMapping = configurations.named("mojangMapping").get().firstOrNull()
                val spigotMapping = configurations.named("spigotMapping").get().firstOrNull()

                if (mojangMapping != null && spigotMapping != null) {
                    remap(archiveFile, obfOutput, mojangMapping, true)
                    remap(obfOutput, spigotOutput, spigotMapping)

                    spigotOutput.copyTo(archiveFile, true)
                    obfOutput.delete()
                    spigotOutput.delete()
                    println("Successfully obfuscate jar (${nmsProject.name})")
                } else {
                    logger.warn("Mojang and Spigot mapping should be specified for ${
                        path.drop(1).takeWhile { it != ':' }
                    }.")
                }
            }
        }
    }
    create<GradleJar>("sourcesJar") {
        archiveClassifier.set("sources")

        subprojects.filter { it.path != ":paper" }.onEach { from(it.sourceSets["main"].output) }.forEach { sourceProject ->
            from(sourceProject.sourceSets["main"].allSource)
        }
    }
    create<GradleJar>("debugJar") {
        archiveBaseName.set("Tap")
        archiveVersion.set("") // For bukkit plugin update
        archiveClassifier.set("DEBUG")

        subprojects.forEach { subproject ->
            from(subproject.sourceSets["main"].output)
        }

        var dest = File(rootDir, ".debug/plugins")
        val pluginName = archiveFileName.get()
        val pluginFile = File(dest, pluginName)
        if (pluginFile.exists()) dest = File(dest, "update")

        doLast {
            copy {
                from(archiveFile)
                into(dest)
            }
        }
    }

    create<DefaultTask>("setupWorkspace") {
        doLast {
            val versions = arrayOf(
                "1.17"
            )
            val buildtoolsDir = File(".buildtools")
            val buildtools = File(buildtoolsDir, "BuildTools.jar")

            val maven = File(System.getProperty("user.home"), ".m2/repository/org/spigotmc/spigot/")
            val repos = maven.listFiles { file: File -> file.isDirectory } ?: emptyArray()
            val missingVersions = versions.filter { version ->
                repos.find { it.name.startsWith(version) }?.also { println("Skip downloading spigot-$version") } == null
            }.also { if (it.isEmpty()) return@doLast }

            val download by registering(Download::class) {
                src("https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar")
                dest(buildtools)
            }
            download.get().download()

            runCatching {
                for (v in missingVersions) {
                    println("Downloading spigot-$v...")

                    javaexec {
                        workingDir(buildtoolsDir)
                        mainClass.set("-jar")
                        args = listOf("./${buildtools.name}", "--rev", v, "--disable-java-check", "--remapped")
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
    register<DefaultTask>("setupDebugServer") {
        dependsOn(":debugJar")
        doLast {
            fun runProcess(directory: File, vararg command: String) {
                val process = ProcessBuilder(*command).directory(directory).start()
                val buffer = ByteArray(1000)
                while (process.isAlive) {
                    if (process.inputStream.available() > 0) {
                        val count = process.inputStream.read(buffer)
                        System.out.write(buffer, 0, count)
                    }
                    if (process.errorStream.available() > 0) {
                        val count = process.errorStream.read(buffer)
                        System.err.write(buffer, 0, count)
                    }
                    Thread.sleep(1)
                }
                System.out.writeBytes(process.inputStream.readBytes())
                System.err.writeBytes(process.errorStream.readBytes())

                process.waitFor()
            }

            fun runGitProcess(directory: File, vararg command: String) {
                runProcess(directory, "git", "-c", "commit.gpgsign=false", "-c", "core.safecrlf=false", *command)
            }

            val projectDir = layout.projectDirectory.asFile
            val debugDir = File(projectDir, ".debug")
            val paperDir = File(projectDir, ".paper")
            val buildDir = File(paperDir, "Paper-Server/build/libs")
            val gradle = if (Os.isFamily(Os.FAMILY_WINDOWS)) "gradlew.bat" else "gradlew"

            var shouldUpdate = false

            if (paperDir.listFiles()?.isEmpty() != false) {
                shouldUpdate = true
                runGitProcess(projectDir, "submodule", "update", "--init")
            }

            if (project.hasProperty("updatePaper")) {
                shouldUpdate = true
                runGitProcess(paperDir, "fetch", "--all")
                runGitProcess(paperDir, "reset", "--hard", "\"origin/master\"")
            }

            if (shouldUpdate) {
                runProcess(paperDir, File(paperDir, gradle).absolutePath, "applyPatches")
                runProcess(paperDir, File(paperDir, gradle).absolutePath, "shadowJar")

                buildDir.listFiles()?.forEach { file ->
                    println("Copying ${file.name} into .debug")
                    file.copyTo(File(debugDir, file.name), true)
                }
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>(rootProject.name) {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(project(":api").tasks["dokkaJar"])

            repositories {
                mavenLocal()

                maven {
                    name = "central"

                    credentials.runCatching {
                        val nexusUsername: String by project
                        val nexusPassword: String by project
                        username = nexusUsername
                        password = nexusPassword
                    }.onFailure {
                        logger.warn("Failed to load nexus credentials, Check the gradle.properties")
                    }

                    url = uri(
                        if ("SNAPSHOT" in version) {
                            "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                        } else {
                            "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                        }
                    )
                }
            }

            pom {
                name.set(rootProject.name)
                description.set("Paper plugin library")
                url.set("https://github.com/monun/tap")

                licenses {
                    license {
                        name.set("GNU General Public License version 3")
                        url.set("https://opensource.org/licenses/GPL-3.0")
                    }
                }

                developers {
                    developer {
                        id.set("monun")
                        name.set("Monun")
                        email.set("monun1010@gmail.com")
                        url.set("https://github.com/monun")
                        roles.addAll("developer")
                        timezone.set("Asia/Seoul")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/monun/tap.git")
                    developerConnection.set("scm:git:ssh://github.com:monun/tap.git")
                    url.set("https://github.com/monun/tap")
                }
            }
        }
    }
}

signing {
    isRequired = true
    sign(tasks["sourcesJar"], project(":api").tasks["dokkaJar"], tasks["jar"])
    sign(publishing.publications[rootProject.name])
}
