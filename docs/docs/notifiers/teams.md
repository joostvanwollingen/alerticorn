import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Teams

The Teams notifier adds support to route messages to Teams using Incoming Webhooks With Workflows.

### Creating a Teams Incoming Webhook

Follow the instructions on the Microsoft website to create an [Incoming Webhook](https://support.microsoft.com/en-us/office/create-incoming-webhooks-with-workflows-for-microsoft-teams-8ae491c7-0394-4861-ba59-055e33f75498).

:::info

The Teams notifier currently does not support authentication. When asked "Who can trigger the flow?" you should answer "Anyone".

:::

### Dependency

<Tabs>
<TabItem value="Kotlin" label="build.gradle.kts">

```kotlin
dependencies {
    testImplementation("nl.vanwollingen.alerticorn:alerticorn-teams-workflow-notifier:0.1") //To use Teams
}
```

</TabItem>
<TabItem value="Groovy" label="build.gradle">

```groovy
dependencies {
    testImplementation 'nl.vanwollingen.alerticorn:alerticorn-teams-workflow-notifier:0.1'
}
```

</TabItem>
<TabItem value="Maven" label="pom.xml">

```xml
<dependency>
    <groupId>nl.vanwollingen.alerticorn</groupId>
    <artifactId>alerticorn-teams-workflow-notifier</artifactId>
    <version>0.1</version>
    <scope>test</scope>
</dependency>
```

</TabItem>
</Tabs>

### Shorthand Annotation

`@Teams` can be used as a shorthand for `@Message.Platform("Teams")`

```kotlin title="Example of using @Teams"
@Teams
@Message(title = "This would go to Teams")
@Test
fun test() {
    //...
}
```

### Configuration

Configure environment variables with `AC_TEAMS_CHANNEL_<your channel name>` to hold the channel webhooks.