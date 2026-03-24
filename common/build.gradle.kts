plugins {
    id("smartbrainlib-convention")

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

version = modVersion

base {
    archivesName = "${modDisplayName}-common-${mcVersion}"
}

loom {
    accessWidenerPath = file("src/main/resources/${modId}.ct")
}

dependencies {
    minecraft(libs.minecraft)
    compileOnly(libs.mixin)
    compileOnly(libs.mixinextras.common)
}

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