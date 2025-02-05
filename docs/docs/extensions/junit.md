# JUnit

## FAQ

### Using JUnit, do I have to register the extension on each test class?

`@ExtendWith` is the easiest option, but you have multiple ways to register the extension.

- If you have a class that all your tests derived from you can add the `@ExtendWith`-annotation there
- Alternatively, you can explore JUnit's mechanisms
  to [register extensions automatically.](https://junit.org/junit5/docs/current/user-guide/#extensions-registration)