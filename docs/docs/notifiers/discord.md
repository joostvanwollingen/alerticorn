import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Discord

The Discord notifier adds support to route messages to Discord using webhooks.

### Creating a Discord Webhook

Follow the instructions on the Discord website to create
a [webhook](https://support.discord.com/hc/en-us/articles/228383668-Intro-to-Webhooks).

### Dependency

<Tabs>
<TabItem value="Kotlin" label="build.gradle.kts">

```kotlin
dependencies {
    testImplementation("nl.vanwollingen.alerticorn:alerticorn-discord-notifier:0.1") //To use Discord
}
```

</TabItem>
<TabItem value="Groovy" label="build.gradle">

```groovy
dependencies {
    testImplementation 'nl.vanwollingen.alerticorn:alerticorn-discord-notifier:0.1'
}
```

</TabItem>
<TabItem value="Maven" label="pom.xml">

```xml
<dependency>
    <groupId>nl.vanwollingen.alerticorn</groupId>
    <artifactId>alerticorn-discord-notifier</artifactId>
    <version>0.1</version>
    <scope>test</scope>
</dependency>
```

</TabItem>
</Tabs>

### Shorthand Annotation

`@Discord` can be used as a shorthand for `@Message.Platform("discord")`

```kotlin title="Example of using @Discord"
@Discord
@Message(title = "This would go to Discord")
@Test
fun test() {
    //...
}
```

### Configuration

Configure environment variables with `AC_DISCORD_CHANNEL_<your channel name>` to hold the channel webhooks.