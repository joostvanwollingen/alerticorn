# JUnit

The JUnit extension adds support to use the [@Message annotation](/messaging/annotations/) on your test classes and
methods.

## Registering the extension with JUnit

In order to integrate Alerticorn with JUnit it must
be [registered](https://docs.junit.org/current/user-guide/extensions/registering-extensions.html) as
a [JUnit Extension](https://docs.junit.org/current/user-guide/extensions/overview.html).

The simplest method is to use the `@ExtendWith`-annotation from JUnit and include Alerticorn's `MessageExtension`-class
there.

```kotlin title="Example of extending your test class"
@ExtendWith(MessageExtension::class)
class MyTest {
    //...
}
```
 
## FAQ

### Using JUnit, do I have to register the extension on each test class?

`@ExtendWith` is the easiest option, but you have multiple ways to register the extension.

- If you have a class that all your tests derived from you can add the `@ExtendWith`-annotation there
- Alternatively, you can explore JUnit's mechanisms
  to [register extensions automatically.](https://docs.junit.org/current/user-guide/extensions/registering-extensions.html)