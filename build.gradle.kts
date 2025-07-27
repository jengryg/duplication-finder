plugins {
    kotlin("jvm") version "2.2.0"
    application
    // Optional Gradle plugin for enhanced type safety and schema generation
    // https://kotlin.github.io/dataframe/gradle.html
    id("org.jetbrains.kotlinx.dataframe") version "0.15.0"
}

group = "utilities"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.18")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.19.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.1")

    implementation("org.jetbrains.kotlinx:dataframe:0.15.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("MainKt")
}