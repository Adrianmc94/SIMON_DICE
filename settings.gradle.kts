// settings.gradle.kts

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // 🔥 ELIMINA EL REPOSITORIO DE JETBRAINS si lo añadiste:
        // maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    // 🔥 ELIMINA EL BLOQUE resolutionStrategy si lo tienes
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SIMON-DICE"
include(":app")