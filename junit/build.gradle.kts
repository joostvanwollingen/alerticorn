dependencies {
    implementation(project(":core"))
    implementation("org.junit.jupiter:junit-jupiter-api:6.0.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.junit-pioneer:junit-pioneer:2.3.0")
}

mavenPublishing {
    coordinates("nl.vanwollingen.alerticorn", "alerticorn-junit", project.version.toString())
    pom {
        name.set("Alerticorn JUnit Extension")
        description.set("An extension for JUnit 6 for use with Alerticorn")
    }
}
