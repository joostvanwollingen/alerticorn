plugins {
    kotlin("jvm")
    id("maven-publish")
}

group = "nl.vanwollingen.alerticorn"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.wiremock:wiremock:3.9.2")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("org.junit-pioneer:junit-pioneer:2.3.0")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "nl.vanwollingen.alerticorn"
            artifactId = "alerticorn-core"
            version = "0.1"

            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["dokkaJavadocJar"])
        }
    }
}