dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven("https://jitpack.io")
        jcenter() // Warning: this repository is going to shut down soon
    }
}
rootProject.name = "UVIndexLibray"
include(":app")
