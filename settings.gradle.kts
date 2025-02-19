plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "alerticorn"
include("core")
include("slack-webhook-notifier")
include("junit")
include("testng")
include("discord-notifier")
include("teams-workflow-notifier")
