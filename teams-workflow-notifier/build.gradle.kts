dependencies {
    implementation(project(":core"))
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.junit-pioneer:junit-pioneer:2.3.0")
}

mavenPublishing {
    coordinates("nl.vanwollingen.alerticorn", "alerticorn-teams-workflow-notifier", project.version.toString())
    pom {
        name.set("Alerticorn Teams Notifier")
        description.set("A Teams Notifier to use with Alerticorn")
    }
}
