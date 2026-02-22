dependencies {
    implementation(project(":core"))
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.junit-pioneer:junit-pioneer:2.3.0")
}

mavenPublishing {
    coordinates("nl.vanwollingen.alerticorn", "alerticorn-slack-notifier", project.version.toString())
    pom {
        name.set("Alerticorn Slack Notifier")
        description.set("A Slack Notifier to use with Alerticorn")
    }
}
