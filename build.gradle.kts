@file:OptIn(ExperimentalComposeLibrary::class)
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm") version "2.0.21"
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    application
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
}

group = "org.example"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation("org.jetbrains.compose.ui:ui:1.7.3")
    implementation("org.jetbrains.compose.material:material:1.7.3")
    implementation("org.jetbrains.compose.ui:ui-tooling-preview:1.7.3")
    implementation("org.jetbrains.compose.foundation:foundation:1.7.3")
    implementation("org.jetbrains.compose.runtime:runtime:1.7.3")
    implementation("org.jetbrains.compose.ui:ui-util:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")
    implementation("org.jetbrains.skiko:skiko-awt-runtime-linux-x64:0.7.77")
    testImplementation(kotlin("test"))
    implementation(compose.desktop.currentOs)
    implementation(compose.components.resources)
    testImplementation(compose.uiTest)
    implementation("org.xerial:sqlite-jdbc:3.41.2.2")
    implementation("androidx.compose.material:material-icons-extended:1.7.3")
    implementation("androidx.compose.ui:ui-test-junit4:1.7.3")
    ktlintRuleset("com.pinterest.ktlint:ktlint-ruleset-standard:0.42.1")


}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("AppKt")
}

tasks.register<JavaExec>("runTerminal") {
    group = "application"
    mainClass.set("MainKt")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
    args = project.findProperty("args")?.toString()?.split(", ") ?: emptyList()
}

tasks.register<Test>("runTests") {
    group = "verification"
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = true
    }
    useJUnitPlatform()
}

ktlint {
    disabledRules.set(setOf("no-wildcard-imports"))
}
