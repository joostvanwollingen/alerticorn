plugins {
    kotlin("jvm")
    id("maven-publish")
}

group = "nl.vanwollingen.alerticorn"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.junit-pioneer:junit-pioneer:2.3.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "nl.vanwollingen.alerticorn"
            artifactId = "alerticorn-discord-notifier"
            version = "0.1"

            from(components["java"])

            artifact(tasks["sourcesJar"])
            artifact(tasks["dokkaJavadocJar"])
        }
    }
}