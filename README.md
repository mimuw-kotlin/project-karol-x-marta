[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/M0kyOMLZ)
# Mastermind

## Authors
- Karol Baciński (@karolus162 on GitHub)
- Marta Hering-Zagrocka (@mhz47 on GitHub)

## Description
Mastermind to prosta gra planszowa, w której gracz próbuje odgadnąć ukryty kod składający się z kolorowych pionków. W podstawowej wersji gry gracz będzie miał do dyspozycji 8 kolorów, z których będzie mógł wybrać 4, aby stworzyć swój kod. Po każdym ruchu gracz będzie otrzymywał informacje zwrotne, które pomogą mu odgadnąć kod. Gra kończy się, gdy gracz odgadnie kod lub skończy się mu liczba prób.

## Features
- single player mode
- several difficulty levels
- multiplayer 1v1 web mode
- GUI

## Game v1
- single player mode
- several options for game settings
- GUI
- terminal mode
- tests
- database with time scores

## Game v2
- multiplayer mode
- improved GUI
- more tests

## Libraries
- Compose 
- JUnit
- kotlinx
- sqlite-jdbc
- ktLint


## How to run
Dodtkowo, można uruchamiać grę poprzez gradelw:

- Aplikacja gry z GUI:

Właściwa gra (z gui) może być włączona poprzez ./gradlew runClient. 
Domyślnie uruchomi się z założeniem, że serwer znajduje się na locahoście na porcie 12345. 
Można to zmienić argumentami uruchomienia. 

Sposób podawania argumetów: -Pargs"flaga, wartość, flaga2, wartość...".

Dostępne flagi:
  -host,
  -port.

Przykładowe wywołanie: ./gradlew runClient -Pargs="-host, localhost, -port, 12346"

- Serwer (dla trybu multiplayer):

Serwer można uruchomić poprzez ./gradlew runServer na domyślnym porcie 12345 lub podając port jako argument wywołania, np. ./gradlew runServer -Pargs="12346".


- Wersja terminalowa (tylko single player):

Zostawiliśmy dodatkowo wersję terminalową, którą można uruchomić poprzez ./gradlew runTerminal,
gdzie można dołączyć flagi w ten sam sposób co w przypadku GUI.

Dostępne flagi:
-l (sequence length) - od 2 do 6,
-a (max attmpts) - od 3 do 12,
-c (color list, jako string kolorów oddzielonych spacjami) - długość od 3 do 8.

Przykładowe wywołanie: ./gradlew runTerminal -Pargs="-l, 4, -a, 10, -c, red blue green yellow"
Flagi są opcjonalne, domyślne wartości to: -l 4, -a 10, -c A B C D E F.

- Testy:

Testy można uruchomić poprzez ./gradlew runTests.

#### Uruchamianie gry z poziomu IDE:
- Aplikacja gry z GUI: App.kt
- Serwer: GameServer.kt
- Aplikacja gry z terminala (tylko single player): Main.kt

## Additional information
Aby postawić serwer na Studentsie trzeba podmienić plik build.gradle.kts, ponieważ tam nie ma Javy 21.

Zawartość pliku build.gradle.kts w wersji z Java 17:
```kotlin
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
    jvmToolchain(17)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
    options.release.set(17)
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
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        },
    )
}

tasks.register<JavaExec>("runServer") {
    group = "application"
    mainClass.set("GameServerKt")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
    args = project.findProperty("args")?.toString()?.split(", ") ?: emptyList()
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        },
    )
}

tasks.register<JavaExec>("runClient") {
    group = "application"
    mainClass.set("AppKt")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
    args = project.findProperty("args")?.toString()?.split(", ") ?: emptyList()
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        },
    )
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
```