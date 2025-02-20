import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Mattermost

Alerticorn supports sending messages to Mattermost using the Slack notifier, which is compatible with Mattermost.

### Creating a Mattermost Incoming Webhook

Follow the instructions on the Mattermost website to create
an [Incoming Webhook](https://developers.mattermost.com/integrate/webhooks/incoming/).

### Dependency

<Tabs>
<TabItem value="Kotlin" label="build.gradle.kts">

```kotlin
dependencies {
    testImplementation("nl.vanwollingen.alerticorn:alerticorn-slack-notifier:0.1")
}
```

</TabItem>
<TabItem value="Groovy" label="build.gradle">

```groovy
dependencies {
    testImplementation 'nl.vanwollingen.alerticorn:alerticorn-slack-notifier:0.1'
}
```

</TabItem>
<TabItem value="Maven" label="pom.xml">

```xml

<dependency>
    <groupId>nl.vanwollingen.alerticorn</groupId>
    <artifactId>alerticorn-slack-notifier</artifactId>
    <version>0.1</version>
    <scope>test</scope>
</dependency>
```

</TabItem>
</Tabs>

### Shorthand Annotation

There is no equivalent `@Mattermost` shorthand to indicate the platform. You can easily create one, that routes to the
Slack notifier.

```kotling title="Create a @Mattermost annotation"
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Message.Platform("slack")
annotation class Mattermost
```

```kotlin title="Example of using @Mattermost"
@Mattermost
@Message(title = "This would go to Mattermost")
@Test
fun test() {
    //...
}
```

### Configuration

Configure environment variables with `AC_SLACK_CHANNEL_<your channel name>` to hold the channel webhooks.