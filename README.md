# Alerticorn: Notification Framework

<img src="docs/images/alerticorn-7.jpg" height="150" />

**Alerticorn** is a lightweight Kotlin library designed to easily send notifications when annotated test methods or code
blocks fail. It supports multiple notification platforms and can be integrated easily with your test framework.

## Documentation

Detailed documentation is available on https://joostvanwollingen.github.io/alerticorn/. 

## Features

- **Pluggable Notification System**: Use one of the popular platform notifiers out of the box, or easily add your
  notification platforms using the `Notifier` interface.
- **Customizable Message Formats**: Build dynamic messages using MessageBuilders
- **Small footprint**: Alerticorn has little dependencies, and supplies a lightweight HTTP client.
- **JUnit Integration**: Automatically send notifications when tests fail using a JUnit extension.
- **TestNG Integration**: Automatically send notifications when tests fail using TestNG listeners. (soon)
- **Kotest Integration**: Automatically send notifications when tests fail using Kotest listeners. (soon)

## Notification Platform Support

- ✅ Slack (webhooks)
- 🚧 Microsoft Teams
- ✅ Discord
- 🚧 Custom APIs

## Test Framework Support

- ✅ JUnit 5
- 🚧 TestNG
- 🚧 Kotest

# License

MIT, see [License](LICENSE)