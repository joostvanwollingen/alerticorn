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

            pom {
                name = "Alerticorn Slack Notifier"
                description = "A Slack Notifier to use with Alerticorn"
                url = "https://joostvanwollingen.github.io/alerticorn/"
                licenses {
                    license {
                        name = "MIT"
                        url = "https://github.com/joostvanwollingen/alerticorn/blob/main/LICENSE"
                    }
                }
                developers {
                    developer {
                        id = "joostvanwollingen"
                        name = "Joost van Wollingen"
                        email = "joostvanwollingen@gmail.com"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/joostvanwollingen/alerticorn.git"
                    developerConnection = "scm:git:ssh://git@github.com:joostvanwollingen/alerticorn.git"
                    url = "https://github.com/joostvanwollingen/alerticorn"
                }
            }
        }
    }
}