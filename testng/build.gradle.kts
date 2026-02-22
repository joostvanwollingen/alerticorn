dependencies {
    implementation(project(":core"))
    implementation("org.testng:testng:7.10.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.junit-pioneer:junit-pioneer:2.3.0")
    testImplementation("com.google.inject:guice:7.0.0") // Required for mocking ISuite (references Guice Injector)
}

mavenPublishing {
    coordinates("nl.vanwollingen.alerticorn", "alerticorn-testng", project.version.toString())
    pom {
        name.set("Alerticorn TestNG Listener")
        description.set("A listener for TestNG for use with Alerticorn")
    }
}
