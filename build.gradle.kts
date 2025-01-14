import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm") version "1.8.21"
    id("org.jetbrains.compose") version "1.5.0"
    application
    id("org.jlleitschuh.gradle.ktlint") version "11.3.2"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("org.jetbrains.compose.ui:ui:1.5.0")
    implementation("org.jetbrains.compose.material:material:1.5.0")
    implementation("org.jetbrains.compose.ui:ui-tooling-preview:1.5.0")
    implementation("org.jetbrains.compose.foundation:foundation:1.5.0")
    implementation("org.jetbrains.compose.runtime:runtime:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")
    implementation("org.jetbrains.skiko:skiko-awt-runtime-linux-x64:0.7.77")
    testImplementation(kotlin("test"))
    implementation("org.xerial:sqlite-jdbc:3.41.2.2")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
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
