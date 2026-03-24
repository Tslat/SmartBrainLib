plugins {
	alias(libs.plugins.minotaur) apply false
	alias(libs.plugins.loom) apply false

	// Required for NeoGradle
	alias(libs.plugins.ideaext)
}