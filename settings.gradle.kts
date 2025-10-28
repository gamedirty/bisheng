pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "bisheng"
include(
    ":sample",
    ":bisheng:annotation",
    ":bisheng:compiler",
    ":bisheng:library",
    ":bisheng:lint",
    ":feature-user",
    ":feature-product"
)