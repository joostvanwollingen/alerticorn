dependencies {
    implementation(project(":core"))
    implementation("io.kotest:kotest-framework-api:5.9.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.junit-pioneer:junit-pioneer:2.3.0")
}

mavenPublishing {
    coordinates("nl.vanwollingen.alerticorn", "alerticorn-kotest", project.version.toString())
    pom {
        name.set("Alerticorn Kotest Extension")
        description.set("An extension for Kotest for use with Alerticorn")
    }
}
