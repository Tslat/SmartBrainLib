pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()

        // Fabric
        exclusiveContent {
            forRepository {
                maven {
                    name = "Fabric"
                    url = uri("https://maven.fabricmc.net")
                }
            }
            filter {
                includeGroupAndSubgroups("net.fabricmc")
                includeGroup("fabric-loom")
            }
        }

        // NeoForge
        exclusiveContent {
            forRepository {
                maven {
                    name = "NeoForge"
                    url = uri("https://maven.neoforged.net/releases")
                }
            }
            filter {
                includeGroupAndSubgroups("net.neoforged")
                includeGroup("codechicken")
            }
        }

        // Forge
        exclusiveContent {
            forRepository {
                maven {
                    name = "Forge"
                    url = uri("https://maven.minecraftforge.net/")
                }
            }
            filter {
                includeGroupAndSubgroups("net.minecraftforge")
            }
        }

        // Mixin
        exclusiveContent {
            forRepository {
                maven {
                    name = "SpongeForge"
                    url = uri("https://repo.spongepowered.org/repository/maven-public")
                }
            }
            filter {
                includeGroupAndSubgroups("org.spongepowered")
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// Keep this lowercase, without spaces or symbols
rootProject.name = "smartbrainlib"

include("common")
include("fabric")
include("forge")
include("neoforge")
