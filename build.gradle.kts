import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    kotlin("plugin.serialization") version "1.9.23" // Match your Kotlin version
}

group = "com.bankbone"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    //
    // Import the JUnit 5 BOM to manage and align all JUnit dependency versions.
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation(libs.ktor.server.test.host)
    // Add the JUnit 5 engine for test execution
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    // Koin for testing
    testImplementation(libs.koinTest)
    testImplementation(libs.koinTestJunit5)
    // Use the kotlin-test-junit5 adapter for JUnit 5 to match `useJUnitPlatform()`.
    testImplementation(kotlin("test"))
    implementation(libs.koin.core)

}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL
        events("passed", "skipped", "failed")
    }

    // This ensures that if the test task fails for any reason (including setup),
    // the exception is re-thrown, which will fail the build.
    doLast {
        state.failure?.let { throw it }
    }
}
