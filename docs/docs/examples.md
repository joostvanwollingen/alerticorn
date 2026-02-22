# Examples

The [`examples/`](https://github.com/joostvanwollingen/alerticorn/tree/main/examples) directory contains complete
example projects covering different build tools, test frameworks, and notification platforms.

## Example Projects

| Directory                                                                                                              | Build Tool           | Test Framework | Notifier | Language |
|------------------------------------------------------------------------------------------------------------------------|----------------------|----------------|----------|----------|
| [gradle-junit-slack](https://github.com/joostvanwollingen/alerticorn/tree/main/examples/gradle-junit-slack)            | Gradle (Kotlin DSL)  | JUnit 5        | Slack    | Kotlin   |
| [gradle-kotest-teams](https://github.com/joostvanwollingen/alerticorn/tree/main/examples/gradle-kotest-teams)          | Gradle (Kotlin DSL)  | Kotest         | Teams    | Kotlin   |
| [maven-junit-discord](https://github.com/joostvanwollingen/alerticorn/tree/main/examples/maven-junit-discord)          | Maven                | JUnit 5        | Discord  | Java     |
| [maven-testng-slack](https://github.com/joostvanwollingen/alerticorn/tree/main/examples/maven-testng-slack)            | Maven                | TestNG         | Slack    | Java     |

## What Each Example Demonstrates

### gradle-junit-slack (Kotlin)

Shows JUnit 5 integration with the Slack notifier using Gradle. Demonstrates `@ExtendWith(MessageExtension::class)`,
`@Slack`, `@Message.Events`, `@Message.Template`, and the programmatic `RunWith.messageOnException()` API.

### gradle-kotest-teams (Kotlin)

Shows all three Kotest integration approaches with the Teams notifier:
- **Spec-level annotations** (`AlerticornExtension`) on an `AnnotationSpec`
- **Per-test DSL** using `alerticornTest()` and `alerticorn()` builder on a `FunSpec`
- **Project-level notifications** via `AlerticornProjectExtension` in `ProjectConfig`

### maven-junit-discord (Java)

Shows JUnit 5 integration with the Discord notifier using Maven. Demonstrates the same annotation
patterns as `gradle-junit-slack` but in Java, including the `RunWith.message()` programmatic API
with `BiFunction` and `Supplier`.

### maven-testng-slack (Java)

Shows TestNG integration with the Slack notifier using Maven. Demonstrates `@Listeners(MessageListener.class)`,
method-level `@Message` annotations, and suite-level notifications with `@Message.Events({Event.SUITE_COMPLETE})`.

## Webhook Configuration

Each example contains a placeholder webhook URL. See the
[examples README](https://github.com/joostvanwollingen/alerticorn/tree/main/examples#webhook-configuration) and the
[Configuration](/configuration) page for details on setting up webhook URLs safely using environment variables.

## Mattermost

Mattermost is compatible with the Slack notifier. See the [Mattermost notifier](/notifiers/mattermost) documentation
and the [examples README](https://github.com/joostvanwollingen/alerticorn/tree/main/examples#mattermost) for details.
