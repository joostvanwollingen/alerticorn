dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.wiremock:wiremock:3.9.2")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("org.junit-pioneer:junit-pioneer:2.3.0")
}

mavenPublishing {
    coordinates("nl.vanwollingen.alerticorn", "alerticorn-core", project.version.toString())
    pom {
        name.set("Alerticorn Core")
        description.set("A library to send messages to different messaging platforms")
    }
}
