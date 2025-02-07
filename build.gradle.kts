plugins {
    kotlin("jvm") version "2.0.21"
    id("org.jetbrains.dokka") version "2.0.0"
    id("maven-publish")
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "maven-publish")

    //Because we use junit-pioneer for env variable 'mocking'
    tasks.withType<Test>().all {
        jvmArgs("--add-opens","java.base/java.util=ALL-UNNAMED")
        jvmArgs("--add-opens","java.base/java.lang=ALL-UNNAMED")
    }

    tasks.register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    tasks.register<Jar>("dokkaJavadocJar") {
        dependsOn(tasks.dokkaJavadoc)
        from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
        archiveClassifier.set("javadoc")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test { // See 4️⃣
    useJUnitPlatform()
}