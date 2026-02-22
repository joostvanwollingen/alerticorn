dependencies {
    implementation(project(":core"))
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.junit-pioneer:junit-pioneer:2.3.0")
}

mavenPublishing {
    coordinates("nl.vanwollingen.alerticorn", "alerticorn-discord-notifier", project.version.toString())
    pom {
        name.set("Alerticorn Discord Notifier")
        description.set("A Discord Notifier to use with Alerticorn")
    }
}
