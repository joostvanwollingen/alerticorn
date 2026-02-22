plugins {
    kotlin("jvm") version "2.1.0"
}

group = "nl.vanwollingen.alerticorn.examples"
version = "unspecified"

dependencies {
    testImplementation("nl.vanwollingen.alerticorn:alerticorn-core:0.1")
    testImplementation("nl.vanwollingen.alerticorn:alerticorn-teams-workflow-notifier:0.1")
    testImplementation("nl.vanwollingen.alerticorn:alerticorn-kotest:0.1")
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
}

tasks.test {
    useJUnitPlatform()
}
