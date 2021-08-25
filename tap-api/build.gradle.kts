plugins {
    id("org.jetbrains.dokka") version "1.5.0"
    `maven-publish`
    signing
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.30")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    implementation("org.mariuszgromada.math:MathParser.org-mXparser:4.4.2")
}

tasks {
    create<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    create<Jar>("dokkaJar") {
        archiveClassifier.set("javadoc")
        dependsOn("dokkaHtml")

        from("$buildDir/dokka/html/") {
            include("**")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("tap-api") {
            artifactId = "tap-api"
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["dokkaJar"])

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
                name.set("tap-api")
                description.set("Paper extension library")
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
    sign(tasks["jar"], tasks["sourcesJar"], tasks["dokkaJar"])
    sign(publishing.publications["tap-api"])
}
