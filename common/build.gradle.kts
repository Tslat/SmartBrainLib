plugins {
    id("smartbrainlib-convention")

    alias(libs.plugins.moddevgradle)
}

val modId              : String by project
val modDisplayName     : String by project
val modModrinthId      : String by project
val modCurseforgeId    : String by project
val modChangelogUrl    : String by project
val modVersion         = libs.versions.smartbrainlib.get()
val javaVersion        = libs.versions.java.get()
val mcVersion          = libs.versions.minecraft.asProvider().get()
val parchmentMcVersion = libs.versions.parchment.minecraft.get()
val parchmentVersion   = libs.versions.parchment.asProvider().get()

version = modVersion

base {
    archivesName = "${modDisplayName}-common-${mcVersion}"
}

neoForge {
    neoFormVersion = libs.versions.neoform.get()
    validateAccessTransformers = true
    accessTransformers.files.setFrom("src/main/resources/META-INF/accesstransformer.cfg")

    parchment.minecraftVersion.set(parchmentMcVersion)
    parchment.mappingsVersion.set(parchmentVersion)
}

dependencies {
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