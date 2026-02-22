# Alerticorn Examples

This directory contains example projects demonstrating how to integrate Alerticorn with different build tools, test frameworks, and notification platforms.

## Example Projects

| Directory | Build Tool | Test Framework | Notifier | Language |
|-----------|------------|----------------|----------|----------|
| [gradle-junit-slack](gradle-junit-slack/) | Gradle (Kotlin DSL) | JUnit 5 | Slack | Kotlin |
| [gradle-kotest-teams](gradle-kotest-teams/) | Gradle (Kotlin DSL) | Kotest | Teams | Kotlin |
| [maven-junit-discord](maven-junit-discord/) | Maven | JUnit 5 | Discord | Java |
| [maven-testng-slack](maven-testng-slack/) | Maven | TestNG | Slack | Java |

## Prerequisites

Alerticorn artifacts must be available in your local Maven repository. From the root of the alerticorn project, run:

```shell
./gradlew publishToMavenLocal
```

## Running the Examples

### Gradle examples

From this directory:

```shell
# Run a specific example
./gradlew :gradle-junit-slack:test
./gradlew :gradle-kotest-teams:test

# Run all Gradle examples
./gradlew test
```

### Maven examples

From this directory:

```shell
# Run a specific example
mvn test -f maven-junit-discord/pom.xml
mvn test -f maven-testng-slack/pom.xml
```

## Webhook Configuration

Each example contains a placeholder webhook URL that you must replace with your own. Look for the `WEBHOOK CONFIGURATION` comment block at the top of each test file.

### Using environment variables (recommended)

Alerticorn can resolve webhook URLs from environment variables, so you never need to hardcode them. The pattern is:

```
AC_<PLATFORM>_CHANNEL_<NAME>=<webhook_url>
```

For example:

```shell
# Slack
export AC_SLACK_CHANNEL_ALERTS=https://hooks.slack.com/services/YOUR/WEBHOOK/URL

# Discord
export AC_DISCORD_CHANNEL_ALERTS=https://discord.com/api/webhooks/YOUR/WEBHOOK

# Teams
export AC_TEAMS_CHANNEL_ALERTS=https://your-org.webhook.office.com/webhookb2/YOUR/WEBHOOK
```

Then in your test code, use the channel name instead of the raw URL:

```kotlin
@Message.Channel("alerts")
```

Alerticorn automatically resolves `"alerts"` to the webhook URL from the corresponding `AC_<PLATFORM>_CHANNEL_ALERTS` environment variable.

### Keeping webhook URLs safe

- **Never commit** real webhook URLs to version control.
- For local development, use a `.env` file and add `.env` to your `.gitignore`.
- In CI, use your platform's secret management (GitHub Actions secrets, GitLab CI variables, etc.).
- See the [Configuration documentation](https://joostvanwollingen.github.io/alerticorn/configuration) for full details.

## Mattermost

Mattermost is compatible with the Slack notifier since it uses the same webhook format. To use Alerticorn with Mattermost:

1. Add the `alerticorn-slack-notifier` dependency (same as the Slack examples).
2. Use `@Slack` or `@Message.Platform("slack")` on your tests.
3. Point `@Message.Channel` to your Mattermost webhook URL.

You can also create a custom `@Mattermost` annotation for clarity:

```kotlin
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Message.Platform("slack")
annotation class Mattermost
```

See the [Mattermost documentation](https://joostvanwollingen.github.io/alerticorn/notifiers/mattermost) for details.
