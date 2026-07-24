rootProject.name = "syfoinntektsmelding"

pluginManagement {
    plugins {
        val kotlinVersion = providers.gradleProperty("kotlinVersion").get()
        val kotlinterVersion = providers.gradleProperty("kotlinterVersion").get()
        val versionsVersion = providers.gradleProperty("versionsVersion").get()

        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("org.jmailen.kotlinter") version kotlinterVersion
        id("com.github.ben-manes.versions") version versionsVersion
    }
}
