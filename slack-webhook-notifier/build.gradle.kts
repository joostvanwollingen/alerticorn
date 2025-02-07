dependencies {
    implementation(project(":core"))
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.junit-pioneer:junit-pioneer:2.3.0")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "nl.vanwollingen.alerticorn"
            artifactId = "alerticorn-slack-notifier"
            version = project.version.toString()

            from(components["java"])

            artifact(tasks["sourcesJar"])
            artifact(tasks["dokkaJavadocJar"])
        }
    }
}