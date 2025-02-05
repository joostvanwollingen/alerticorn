---
sidebar_position: 3
---

# Configuration

:::tip[Optional Configuration] 

Alerticorn's configuration is optional. By setting a default platform and channel, you only have to add a Message annotation to your test for the message to be sent.

:::

Alerticorn can retrieve webhook urls and the default channel and platform from environment variables. 

| Environment variable           | Purpose                                     | Example                                               |
|--------------------------------|---------------------------------------------|-------------------------------------------------------|
| `AC_DEFAULT_CHANNEL`           | Set the default channel name                | `AC_DEFAULT_CHANNEL=general`                          |
| `AC_DEFAULT_PLATFORM`          | Set the default platform                    | `AC_DEFAULT_PLATFORM=slack`                           |
| `AC_<PLATFORM>_CHANNEL_<NAME>` | Set the url for a platform specific channel | `AC_SLACK_CHANNEL_GENERAL=https://hooks.slack.com...` |

## Load Webhook Urls From Environment Variables

Although Alerticorn can be used using annotations only, it's recommended to provide environment variables for your
webhooks.
A environment variable for a channel should follow this pattern: `AC_<PLATFORM>_CHANNEL_<CHANNEL>`. For example:
`AC_SLACK_CHANNEL_GENERAL=https://hooks.slack.com/...`.

## Default Platform

You can specify a default platform to use by setting `AC_DEFAULT_PLATFORM`.

_Example:_ `AC_DEFAULT_PLATFORM=slack`

:::tip[Only one notifier?]

If Alerticorn detects a single notifier on the classpath, that will automatically be used for the default platform. 

:::

## Default Channel

You can specify a default platform to use by setting `AC_DEFAULT_CHANNEL`.

_Example:_ `AC_DEFAULT_CHANNEL=general`