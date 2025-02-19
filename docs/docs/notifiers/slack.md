import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Slack

The Slack notifier adds support to route messages to Slack using Incoming Webhooks.

### Creating a Slack Incoming Webhook

Follow the instructions on the Slack website to create an [Incoming Webhook](https://api.slack.com/messaging/webhooks).

### Dependency

<Tabs>
<TabItem value="Kotlin" label="build.gradle.kts">

```kotlin
dependencies {
    testImplementation("nl.vanwollingen.alerticorn:alerticorn-slack-notifier:0.1") //To use Slack
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

`@Slack` can be used as a shorthand for `@Message.Platform("slack")`

```kotlin title="Example of using @Slack"
@Slack
@Message(title = "This would go to Slack")
@Test
fun test() {
    //...
}
```

### Configuration

Configure environment variables with `AC_SLACK_CHANNEL_<your channel name>` to hold the channel webhooks.