plugins {
    kotlin("jvm") version "2.1.0"
}

group = "nl.vanwollingen.alerticorn.examples"
version = "unspecified"

dependencies {
    testImplementation("nl.vanwollingen.alerticorn:alerticorn-core:0.1")
    testImplementation("nl.vanwollingen.alerticorn:alerticorn-slack-notifier:0.1")
    testImplementation("nl.vanwollingen.alerticorn:alerticorn-junit:0.1")
    testImplementation(kotlin("test"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter("5.10.3")
        }
    }
}
