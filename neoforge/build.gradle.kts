import net.darkhax.curseforgegradle.TaskPublishCurseForge

plugins {
    id("smartbrainlib-convention")

    alias(libs.plugins.minotaur)
    alias(libs.plugins.curseforgegradle)
    alias(libs.plugins.loom)
}

val modId              : String by project
val modDisplayName     : String by project
val modModrinthId      : String by project
val modCurseforgeId    : String by project
val modChangelogUrl    : String by project
val modVersion         = libs.versions.smartbrainlib.get()
val javaVersion        = libs.versions.java.get()
val mcVersion          = libs.versions.minecraft.asProvider().get()
val neoforgeVersion    = libs.versions.neoforge.asProvider().get()

version = modVersion

base {
    archivesName = "${modDisplayName}-neoforge-${mcVersion}"
}

dependencies {
    minecraft(libs.minecraft)
    neoForge(libs.neoforge)
}


dependencies {
    compileOnly(project(":common"))
}

tasks.withType<Test>().configureEach {
    enabled = false;
}

tasks.named<JavaCompile>("compileJava").configure {
    source(project(":common").sourceSets.getByName("main").allSource)
}

tasks.named<Jar>("sourcesJar").configure {
    from(project(":common").sourceSets.getByName("main").allSource)
}

tasks.withType<Javadoc>().configureEach {
    source(project(":common").sourceSets.getByName("main").allJava)
}

tasks.withType<ProcessResources>().configureEach {
    from(project(":common").sourceSets.getByName("main").resources)
}

// modrinth {
//     token = System.getenv("modrinthKey") ?: "Invalid/No API Token Found"
//     projectId = modModrinthId
//     versionNumber.set(modVersion)
//     versionName = "NeoForge ${mcVersion}"
//     uploadFile.set(tasks.named<Jar>("jar"))
//     changelog = modChangelogUrl
//     gameVersions.set(listOf(mcVersion))
//     loaders.set(listOf("neoforge"))

//     //debugMode = true
//     //https://github.com/modrinth/minotaur#available-properties
// }

// tasks.register<TaskPublishCurseForge>("publishToCurseForge") {
//     group = "publishing"
//     apiToken = System.getenv("curseforge.apitoken") ?: "Invalid/No API Token Found"

//     val mainFile = upload(modCurseforgeId, tasks.jar)
//     mainFile.displayName = "${modDisplayName} NeoForge ${mcVersion} ${version}"
//     mainFile.releaseType = "release"
//     mainFile.addModLoader("NeoForge")
//     mainFile.addGameVersion(mcVersion)
//     mainFile.addJavaVersion("Java ${javaVersion}")
//     mainFile.changelog = modChangelogUrl

//     //debugMode = true
//     //https://github.com/Darkhax/CurseForgeGradle#available-properties
// }

publishing {
    publishing {
        publications {
            create<MavenPublication>(modId) {
                from(components["java"])
                artifactId = base.archivesName.get()
            }
        }
    }
}

tasks.named<DefaultTask>("publish").configure {
    finalizedBy("modrinth")
    finalizedBy("publishToCurseForge")
}