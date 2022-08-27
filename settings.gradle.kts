rootProject.name = "tap"

val prefix = rootProject.name

include("$prefix-api", "$prefix-core", "$prefix-plugin")

val dongle = "$prefix-dongle"
val dongleFile = file(dongle)
if (dongleFile.exists()) {
    include(dongle)
    // load nms
//    dongleFile.listFiles()?.filter {
//        it.isDirectory && it.name.startsWith("v")
//    }?.forEach { file ->
//        include(":$dongle:${file.name}")
//    }
//
    pluginManagement {
        repositories {
            gradlePluginPortal()
            maven("https://papermc.io/repo/repository/maven-public/")
        }
    }
    include("$dongle:v1.19.2")
}

val publish = "$prefix-publish"
if (file(publish).exists()) include(publish)