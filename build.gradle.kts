import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm") version "2.2.0"
    id("org.jetbrains.dokka") version "2.1.0"
    id("org.jetbrains.dokka-javadoc") version "2.1.0"
    id("com.vanniktech.maven.publish") version "0.30.0" apply false
}

allprojects {
    group = "nl.vanwollingen.alerticorn"
    version = "0.2"
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "org.jetbrains.dokka-javadoc")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.vanniktech.maven.publish")

    //Because we use junit-pioneer for env variable 'mocking'
    tasks.withType<Test>().all {
        jvmArgs("--add-opens", "java.base/java.util=ALL-UNNAMED")
        jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    repositories {
        mavenCentral()
    }

    configure<MavenPublishBaseExtension> {
        configure(KotlinJvm(javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationJavadoc")))
        publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
        signAllPublications()

        pom {
            url.set("https://joostvanwollingen.github.io/alerticorn/")
            licenses {
                license {
                    name.set("MIT")
                    url.set("https://github.com/joostvanwollingen/alerticorn/blob/main/LICENSE")
                }
            }
            developers {
                developer {
                    id.set("joostvanwollingen")
                    name.set("Joost van Wollingen")
                    email.set("joostvanwollingen@gmail.com")
                }
            }
            scm {
                connection.set("scm:git:git://github.com/joostvanwollingen/alerticorn.git")
                developerConnection.set("scm:git:ssh://git@github.com:joostvanwollingen/alerticorn.git")
                url.set("https://github.com/joostvanwollingen/alerticorn")
            }
        }
    }
}
