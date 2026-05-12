// Add your dependency repositories here
repositories {

}


//<editor-fold defaultstate="collapsed" desc="<Boilerplate>">
/**
 * Standard Maven repository
 *
 * @param name The display name of the repository. Only used for logging
 * @param uri The maven repository URL
 * @param groups The artifact group identifiers for the repository
 */
fun RepositoryHandler.mavenRepo(name: String, uri: String, vararg groups: String) {
    exclusiveContent {
        forRepository {
            maven {
                this.name = name
                this.url = uri(uri)
            }
        }
        filter {
            for (group in groups) {
                includeGroup(group)
            }
        }
    }
}

/**
 * Flat-directory repository, acting as a folder in your project root directory for pre-compiled binaries
 *
 * @param folderName The path of the folder to use, relative to the project root. E.G. "libs"
 */
fun RepositoryHandler.folder(folderName: String) {
    flatDir {
        dirs("$projectDir/$folderName")
    }
}

/**
 * {@link https://central.sonatype.com MavenCentral}-based repository,
 * taking the artifact groups as the identifier
 *
 * @param groups The artifact group identifier for each MavenCentral artifact
 */
fun RepositoryHandler.mavenCentral(vararg groups: String) {
    exclusiveContent {
        forRepository {
            mavenCentral()
        }
        filter {
            for (group in groups) {
                includeGroup(group)
            }
        }
    }
}
//</editor-fold>