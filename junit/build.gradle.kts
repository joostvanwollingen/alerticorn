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
    implementation("org.junit.jupiter:junit-jupiter-api:5.11.3") //TODO how to deal with junit version properly to avoid constant updating?
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.junit-pioneer:junit-pioneer:2.3.0")

}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "nl.vanwollingen.alerticorn"
            artifactId = "alerticorn-junit"
            version = "0.1"

            from(components["java"])

            artifact(tasks["sourcesJar"])
            artifact(tasks["dokkaJavadocJar"])
        }
    }
}