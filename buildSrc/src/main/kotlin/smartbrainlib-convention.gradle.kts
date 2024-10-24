import gradle.kotlin.dsl.accessors._6278ecd4aeba9585571b9ac6e140379d.java
import gradle.kotlin.dsl.accessors._6278ecd4aeba9585571b9ac6e140379d.publishing
import gradle.kotlin.dsl.accessors._6278ecd4aeba9585571b9ac6e140379d.versionCatalogs

plugins {
    `java`
    `maven-publish`
    `idea`
    `eclipse`
}

val libs = project.versionCatalogs.find("libs")

val modId                      : String by project
val modDisplayName             : String by project
val modAuthors                 : String by project
val modLicense                 : String by project
val modDescription             : String by project
val modHomepageUrl             : String by project
val modSourcesUrl              : String by project
val modIssuesUrl               : String by project
val modMavenUrl                : String by project
val modVersion                 = libs.get().findVersion("smartbrainlib").get()
val modJavaVersion             = libs.get().findVersion("java").get()
val mcVersion                  = libs.get().findVersion("minecraft").get()
val mcVersionRange             = libs.get().findVersion("minecraft.range").get()
val fapiVersion                = libs.get().findVersion("fabric.api").get()
val fapiVersionRange           = libs.get().findVersion("fabric.api.range").get()
val fabricVersion              = libs.get().findVersion("fabric").get()
val fabricVersionRange         = libs.get().findVersion("fabric.range").get()
val neoforgeVersionRange       = libs.get().findVersion("neoforge.range").get()
val neoforgeLoaderVersionRange = libs.get().findVersion("neoforge.loader.range").get()

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)

    withSourcesJar()
    withJavadocJar()
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

tasks.withType<JavaCompile>().configureEach {
    this.options.encoding = "UTF-8"
    this.options.getRelease().set(21)
}

repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }

        filter {
            includeGroup("maven.modrinth")
        }
    }
}

tasks.withType<ProcessResources>().configureEach {
    val expandProps = mapOf(
        "version"                       to modVersion,
        "group"                         to project.group,
        "mod_display_name"              to modDisplayName,
        "mod_authors"                   to modAuthors,
        "mod_id"                        to modId,
        "mod_license"                   to modLicense,
        "mod_description"               to modDescription,
        "homepage_url"                  to modHomepageUrl,
        "sources_url"                   to modSourcesUrl,
        "issues_url"                    to modIssuesUrl,
        "minecraft_version"             to mcVersion,
        "java_version"                  to modJavaVersion,
        "minecraft_version_range"       to mcVersionRange,
        "fabric_api_version"            to fapiVersion,
        "fabric_api_version_range"      to fapiVersionRange,
        "fabric_loader_version"         to fabricVersion,
        "fabric_loader_version_range"   to fabricVersionRange,
        "neoforge_version_range"        to neoforgeVersionRange,
        "neoforge_loader_version_range" to neoforgeLoaderVersionRange
    )

    filesMatching(listOf("pack.mcmeta", "fabric.mod.json", "META-INF/neoforge.mods.toml", "*.mixins.json")) {
        expand(expandProps)
    }

    inputs.properties(expandProps)
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes(mapOf(
            "Specification-Title"     to modDisplayName,
            "Specification-Vendor"    to modAuthors,
            "Specification-Version"   to modVersion,
            "Implementation-Title"    to modDisplayName,
            "Implementation-Version"  to modVersion,
            "Implementation-Vendor"   to modAuthors,
            "Built-On-Minecraft"      to mcVersion,
            "MixinConfigs"            to "$modId.mixins.json"
        ))
    }
}

publishing {
    repositories {
        if (System.getenv("cloudUsername") == null && System.getenv("cloudPassword") == null) {
            mavenLocal()
        }
        else maven {
            name = "Cloudsmith"
            url = uri(modMavenUrl)

            credentials {
                username = System.getenv("cloudUsername")
                password = System.getenv("cloudPassword")
            }
        }
    }
}