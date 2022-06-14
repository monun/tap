plugins {
    id("io.papermc.paperweight.userdev") version "1.3.7"
}

val projectAPI = project(":${rootProject.name}-api")
val projectCORE = project(":${rootProject.name}-core")

dependencies {
    paperDevBundle("1.19-R0.1-SNAPSHOT")
    implementation(projectAPI)
}

val pluginName = "Tap"
val packageName = "tap"
extra.set("pluginName", pluginName)
extra.set("packageName", packageName)

tasks {
    processResources {
        filesMatching("*.yml") {
            expand(project.properties)
            expand(extra.properties)
        }
    }

    fun registerPluginJar(name: String, vararg outputs: Project, configuration: Jar.() -> Unit) = register<Jar>(name) {
        archiveBaseName.set(pluginName)
        archiveVersion.set("")

        outputs.forEach { project ->
            from(project.sourceSets["main"].output)
        }

        configuration()

        doLast {
            copy {
                from(archiveFile)
                val plugins = File(rootDir, ".debug-server/plugins/")
                into(if (File(plugins, archiveFileName.get()).exists()) File(plugins, "update") else plugins)
            }
        }
    }

    registerPluginJar("pluginJar", projectAPI, projectCORE, project) {
        findProject(":${rootProject.name}-dongle")?.let { dongleProject ->
            val dongleJar = dongleProject.tasks.jar

            dependsOn(dongleJar)
            from(zipTree(dongleJar.get().archiveFile))
        }

        exclude("test.yml")
    }

    findProject(":${rootProject.name}-publish")?.let { publish ->
        registerPluginJar("testJar", project) {
            exclude("plugin.yml")
            rename("test.yml", "plugin.yml")

            publish.tasks.let { tasks ->
                dependsOn(tasks.named("publishApiPublicationToDebugRepository"))
                dependsOn(tasks.named("publishCorePublicationToDebugRepository"))
            }
        }
    }
}
