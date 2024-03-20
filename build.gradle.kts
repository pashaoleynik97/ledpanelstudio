import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "com.pashaoleynik97"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "com.github.pashaoleynik97.ledpanelstudio.main.MainKt"

        // https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Native_distributions_and_local_execution/README.md

        nativeDistributions {
            includeAllModules = true
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "ledpanelprj"
            packageVersion = "1.0.0"

            macOS {
                // a version for all macOS distributables
                packageVersion = "1.0.0"
                // a version only for the dmg package
                dmgPackageVersion = "1.0.0"
                // a version only for the pkg package
                pkgPackageVersion = "1.0.0"

                // a build version for all macOS distributables
                packageBuildVersion = "1.0.0"
                // a build version only for the dmg package
                dmgPackageBuildVersion = "1.0.0"
                // a build version only for the pkg package
                pkgPackageBuildVersion = "1.0.0"
                bundleID = "com.github.pashaoleynik97.ledpanelstudio.Studio"
            }
        }
    }
}
