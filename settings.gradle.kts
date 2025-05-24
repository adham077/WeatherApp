import org.gradle.internal.impldep.org.bouncycastle.oer.its.etsi102941.Url

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        /*For jitpack*/
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "WeatherApp"
include(":app")
