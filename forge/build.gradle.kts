import net.minecraftforge.jarjar.gradle.JarJar
import net.darkhax.curseforgegradle.TaskPublishCurseForge

plugins {
    id("project-setup")

    alias(libs.plugins.minotaur)
    alias(libs.plugins.curseforgegradle)
    alias(libs.plugins.forgegradle)
    alias(libs.plugins.forge.jarjar)
    alias(libs.plugins.forge.at)
}

val modId           : String by project
val modDisplayName  : String by project

jarJar.register {
    archiveClassifier.set("")
}

minecraft {
    rootProject.file("common/src/main/resources/META-INF/accesstransformer.cfg").takeIf { it.exists() }?.let {
        accessTransformers.setFrom(it)
    }

    runs {
        configureEach {
            workingDir.convention(layout.projectDirectory.dir("runs/${name}"))
            systemProperty("forge.logging.console.level", "debug")

            args("-mixin.config=${modId}.mixins.json")
        }

        register("client") {
            args("--username", "Dev")
        }

        register("client2") {
            args("--username", "Dev2")
        }

        register("server")
    }
}

repositories {
    maven(minecraft.mavenizer)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
    exclusiveContent {
        forRepository {
            maven {
                name = "Sponge"
                url = uri("https://repo.spongepowered.org/repository/maven-public")
            }
        }
        filter {
            includeGroupAndSubgroups("org.spongepowered")
        }
    }
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(minecraft.dependency(libs.forge))
    compileOnly(project(":common"))

    annotationProcessor(libs.forge.eventbusvalidator)
}

tasks.named<Jar>("jar").configure {
    archiveClassifier.set("slim")
}

//<editor-fold defaultstate="collapsed" desc="<Publishing>">
// Must have your Modrinth API Key as an environment variable under 'MODRINTH_TOKEN'
modrinth {
    token = System.getenv("MODRINTH_TOKEN") ?: "Invalid/No API Token Found"
    uploadFile.set(tasks.named<JarJar>("jarJar"))
    projectId.set(properties["modrinthProjectId"] as String)
    versionName = "Forge ${libs.versions.minecraft.asProvider().get()}"
    versionType = "alpha"
    loaders.set(listOf("forge"))
    versionNumber.set(project.version.toString())
    gameVersions.set(listOf(libs.versions.minecraft.asProvider().get()))

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

    val mainFile = upload(properties["curseforgeProjectId"], tasks.named<JarJar>("jarJar"))
    mainFile.displayName = "$modDisplayName Forge ${libs.versions.minecraft.asProvider().get()} ${project.version}"
    mainFile.releaseType = "alpha"
    mainFile.addModLoader("Forge")
    mainFile.addGameVersion(libs.versions.minecraft.asProvider().get())
    mainFile.addJavaVersion("Java ${libs.versions.java.get()}")
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
    publishing {
        publications {
            create<MavenPublication>(modId) {
                from(components["jarJar"])
                artifactId = base.archivesName.get()
            }
        }
    }
}

tasks.named<DefaultTask>("publish").configure {
    finalizedBy("modrinth")
    finalizedBy("publishToCurseForge")
}
//</editor-fold>

sourceSets.forEach {
    val dir = layout.buildDirectory.dir("sourcesSets/${it}.name")

    it.output.setResourcesDir(dir)
    it.java.destinationDirectory = dir
}