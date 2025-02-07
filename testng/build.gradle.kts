dependencies {
    implementation(project(":core"))
    implementation(project(":slack-webhook-notifier"))
    implementation("org.testng:testng:7.10.2")
    testImplementation("org.testng:testng:7.10.2")
}

tasks.test {
    useTestNG()
}