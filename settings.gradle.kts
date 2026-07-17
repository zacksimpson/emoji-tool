import java.util.Properties

pluginManagement {
    includeBuild("light-sdk/plugin")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

val localProperties = Properties()
val localPropertiesFile = file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}
val ghUsername = localProperties.getProperty("gpr.user") ?: System.getenv("GH_PACKAGES_USER")
val ghPassword = localProperties.getProperty("gpr.key") ?: System.getenv("GH_PACKAGES_TOKEN")

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            name = "GitHubPackages-Keyboard"
            url = uri("https://maven.pkg.github.com/lightphone/light-keyboard")
            credentials {
                username = ghUsername
                password = ghPassword
            }
        }
    }
}

rootProject.name = "emoji-tool"

include(":lint-rules")
project(":lint-rules").projectDir = file("light-sdk/lint-rules")
include(":sdk:shared")
project(":sdk:shared").projectDir = file("light-sdk/sdk/shared")
include(":sdk:ui")
project(":sdk:ui").projectDir = file("light-sdk/sdk/ui")
include(":sdk:client")
project(":sdk:client").projectDir = file("light-sdk/sdk/client")

include(":tool")
