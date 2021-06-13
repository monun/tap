tasks {
    processResources {
        filesMatching("*.yml") {
            expand(project.properties)
        }
    }
}