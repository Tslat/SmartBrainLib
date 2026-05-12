import net.darkhax.curseforgegradle.Constants
import net.darkhax.curseforgegradle.TaskPublishCurseForge
import org.gradle.internal.extensions.stdlib.capitalized

plugins {
    id("project-setup")

    alias(libs.plugins.minotaur)
    alias(libs.plugins.curseforgegradle)
    alias(libs.plugins.loom)
}

val modId           : String by project
val modDisplayName  : String by project

dependencies {
    minecraft(libs.minecraft)
    implementation(libs.fabric)
    implementation(libs.fabric.api)
    compileOnly(project(":common"))

    // Mod Dependencies below
    //implementation(libs.geckolib.fabric)
}

loom {
    file("src/main/resources/$modId.classtweaker").takeIf { it.exists() }?.let(accessWidenerPath::set)

    runs {
        configureEach {
            runDir("runs/$name")
            ideConfigGenerated(true)
            configName = "Fabric ${name.capitalized()}"
        }

        named("client") {
            client()
            programArg("--username=Dev")
        }

        named("server") {
            server()
        }
    }
}

tasks.withType<ProcessResources>().configureEach {
    exclude("**/accesstransformer.cfg")
}

//<editor-fold defaultstate="collapsed" desc="<Publishing>">
// Must have your Modrinth API Key as an environment variable under 'MODRINTH_TOKEN'
modrinth {
    token = System.getenv("MODRINTH_TOKEN") ?: "Invalid/No API Token Found"
    uploadFile.set(tasks.jar)
    projectId.set(properties["modrinthProjectId"] as String)
    versionName = "Fabric ${libs.versions.minecraft.asProvider().get()}"
    versionType = "alpha"
    loaders.set(listOf("fabric"))
    versionNumber.set(project.version.toString())
    gameVersions.set(listOf(libs.versions.minecraft.asProvider().get()))
    dependencies {
        required.project("fabric-api")
    }

    if (rootProject.file("CHANGELOG.md").exists())
        changelog.set(rootProject.file("CHANGELOG.md").readText(Charsets.UTF_8))

    // Comment out below to enable publishing properly
    //debugMode = true
    // See below for other properties and info
    // https://github.com/modrinth/minotaur#available-properties
}

// Must have your CurseForge API Key as an environment variable under 'CURSEFORGE_TOKEN'
tasks.register<TaskPublishCurseForge>("publishToCurseForge") {
    group = "publishing"
    apiToken = System.getenv("CURSEFORGE_TOKEN") ?: "Invalid/No API Token Found"

    val mainFile = upload(properties["curseforgeProjectId"], tasks.jar)
    mainFile.displayName = "$modDisplayName Fabric ${libs.versions.minecraft.asProvider().get()} ${project.version}"
    mainFile.releaseType = "alpha"
    mainFile.addModLoader("Fabric")
    mainFile.addGameVersion(libs.versions.minecraft.asProvider().get())
    mainFile.addJavaVersion("Java ${libs.versions.java.get()}")
    mainFile.addRelation("fabric-api", Constants.RELATION_REQUIRED)
    mainFile.addEnvironment("Client", "Server")

    if (rootProject.file("CHANGELOG.md").exists()) {
        mainFile.changelog = rootProject.file("CHANGELOG.md").readText(Charsets.UTF_8)
        mainFile.changelogType = "markdown"
    }

    // Comment out below to enable publishing properly
    //debugMode = true
    // See below for other properties and info
    // https://github.com/Darkhax/CurseForgeGradle#available-properties
}

publishing {
    publications {
        create<MavenPublication>(modId) {
            from(components["java"])
            artifactId = base.archivesName.get()
        }
    }
}

tasks.named<DefaultTask>("publish").configure {
    finalizedBy("modrinth")
    finalizedBy("publishToCurseForge")
}
//</editor-fold>