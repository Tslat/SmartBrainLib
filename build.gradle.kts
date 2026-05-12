import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers

plugins {
    alias(libs.plugins.moddevgradle) apply false
    alias(libs.plugins.loom) apply false
    alias(libs.plugins.forgegradle) apply false
    alias(libs.plugins.forge.at) apply false
    alias(libs.plugins.forge.jarjar) apply false

    alias(libs.plugins.minotaur) apply false
    alias(libs.plugins.curseforgegradle) apply false
    alias(libs.plugins.ideaext)

    id("project-setup") apply false
    id("setup-refactoring")
}

idea.project.settings.taskTriggers.beforeSync(tasks.getByName("refactorOnInitialSetup"))