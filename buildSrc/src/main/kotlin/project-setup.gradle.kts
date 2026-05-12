plugins {
    java
    `maven-publish`
    idea
    eclipse
    id("repositories")
}

val libs    = project.versionCatalogs.find("libs")
version     = getVersion("mod")

java {
    toolchain.languageVersion = JavaLanguageVersion.of(getVersion("java"))

    withSourcesJar()
    // Enable if you also want to generate a javadoc jar
    //withJavadocJar()
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

eclipse {
    classpath {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

tasks.withType<JavaCompile>().configureEach {
    this.options.encoding = "UTF-8"
    this.options.release.set(java.toolchain.languageVersion.get().asInt())
}

val modId:              String by project
val modDisplayName:     String by project
val modAuthors:         String by project
val modLicense:         String by project
val modDescription:     String by project
val modHomepage:        String by project
val modIssuesTracker:   String by project
val modGitRepo:         String by project

base {
    archivesName = "$modId-${project.name}-${getVersion("minecraft")}"
}

tasks.withType<Test>().configureEach {
    failOnNoDiscoveredTests = false
}

tasks.named<JavaCompile>("compileJava").configure {
    if (project != project(":common"))
        source(project(":common").sourceSets.main.get().allSource)
}

tasks.named<Jar>("sourcesJar").configure {
    if (project != project(":common"))
        from(project(":common").sourceSets.main.get().allSource)
}

tasks.withType<Javadoc>().configureEach {
    if (project != project(":common"))
        source(project(":common").sourceSets.main.get().allJava)
}

tasks.withType<ProcessResources>().configureEach {
    if (project != project(":common"))
        from(project(":common").sourceSets.main.get().resources)
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes(mapOf(
            "Specification-Title"     to modDisplayName,
            "Specification-Vendor"    to modAuthors,
            "Specification-Version"   to getVersion("mod"),
            "Implementation-Title"    to modDisplayName,
            "Implementation-Version"  to getVersion("mod"),
            "Implementation-Vendor"   to modAuthors,
            "Built-On-Minecraft"      to getVersion("minecraft"),
            "MixinConfigs"            to "$modId.mixins.json"
        ))
    }
}

tasks.withType<ProcessResources>().configureEach {
    if (project != project(":common"))
        from(project(":common").sourceSets.main.get().resources)

    val expandProps = mapOf(
        "group"                          to project.group,
        "mod_id"                         to modId,
        "mod_display_name"               to modDisplayName,
        "mod_license"                    to modLicense,
        "mod_authors"                    to modAuthors,
        "mod_description"                to modDescription,
        "mod_homepage"                   to modHomepage,
        "mod_issues_tracker"             to modIssuesTracker,
        "mod_git_repo"                   to modGitRepo,

        "version"                        to getVersion("mod"),
        "java_version"                   to getVersion("java"),

        "minecraft_version"              to getVersion("minecraft"),
        "minecraft_version_range"        to getVersion("minecraft.range"),

        "forge_version"                  to getVersion("forge"),
        "forge_version_range"            to getVersion("forge.range"),
        "forge_loader_version_range"     to getVersion("forge.fml.range"),

        "fabric_api_version"             to getVersion("fabric.api"),
        "fabric_api_version_range"       to getVersion("fabric.api.range"),
        "fabric_loader_version"          to getVersion("fabric"),
        "fabric_loader_version_range"    to getVersion("fabric.range"),

        "neoforge_version"               to getVersion("neoforge"),
        "neoforge_version_range"         to getVersion("neoforge.range"),
        "neoforge_loader_version_range"  to getVersion("neoforge.loader.range")
    )

    val strategy = duplicatesStrategy
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    filesMatching(listOf("pack.mcmeta", "fabric.mod.json", "META-INF/neoforge.mods.toml", "META-INF/mods.toml")) {
        expand(expandProps)
    }

    duplicatesStrategy = strategy

    inputs.properties(expandProps)
}

tasks.withType<Test>().configureEach {
    failOnNoDiscoveredTests = false
}

tasks.named<JavaCompile>("compileJava").configure {
    if (project != project(":common"))
        source(project(":common").sourceSets.main.get().allSource)
}

tasks.named<Jar>("sourcesJar").configure {
    if (project != project(":common"))
        from(project(":common").sourceSets.main.get().allSource)
}

tasks.withType<Javadoc>().configureEach {
    if (project != project(":common"))
        source(project(":common").sourceSets.main.get().allJava)
}

// Must have your maven host login username and password in your system's environment variables (see below references)
// Read more about environment variables here: https://www.howtogeek.com/787217/how-to-edit-environment-variables-on-windows-10-or-11/
// Don't forget to replace the maven URL below
// If your project is OSS, consider using Cloudsmith as your maven host: https://help.cloudsmith.io/docs/open-source-hosting-policy
publishing {
    repositories {
        if (System.getenv("CLOUDSMITH_USERNAME") == null && System.getenv("CLOUDSMITH_PASSWORD") == null) {
            mavenLocal()
        }
        else maven {
            name = "Cloudsmith"
            url = uri("https://maven.cloudsmith.io/tslat/sbl/")

            credentials {
                username = System.getenv("CLOUDSMITH_USERNAME")
                password = System.getenv("CLOUDSMITH_PASSWORD")
            }
        }
    }
}

gradle.projectsEvaluated {
    tasks.withType(JavaCompile::class) {
        options.compilerArgs.addAll(arrayOf("-Xmaxerrs", "1000"))
    }
}

fun getVersion(versionName: String): String {
    return libs.get().findVersion(versionName).get().requiredVersion
}