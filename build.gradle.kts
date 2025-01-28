@file:OptIn(ExperimentalComposeLibrary::class)

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.kotlin.dsl.configure
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jlleitschuh.gradle.ktlint.KtlintExtension

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
    ktlintRuleset("io.nlopez.compose.rules:ktlint:0.4.22")
    implementation("org.jetbrains.compose.ui:ui-util:1.7.3")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile> {
    options.release.set(21)
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

tasks.register<JavaExec>("runServer") {
    group = "application"
    mainClass.set("GameServerKt")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
    args = project.findProperty("args")?.toString()?.split(", ") ?: emptyList()
}

tasks.register<JavaExec>("runClient") {
    group = "application"
    mainClass.set("AppKt")
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

configure<KtlintExtension> {
    version.set("1.5.0")

    filter {
        exclude("**/generated/**")
    }

    additionalEditorconfig.set(
        mapOf(
            "indent_size" to "4",
            "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
            "ktlint_standard_no-wildcard-imports" to "disabled",
        ),
    )
}
tasks.named<Tar>("distTar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
tasks.named<Zip>("distZip") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
