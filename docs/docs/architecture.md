# Architecture

```mermaid
---
config:
  look: handDrawn
  theme: default
---
graph 
   TestFramework[Test Framework]
   Extension[Test Framework Extension for JUnit, Kotest or TestNG]
   Test[Test Classes & Methods]
   Annotations[@Message]
   NotificationManager[Notification Manager]
   Notifiers[Notifiers for Slack, Discord, Teams]
   Test -->|annotated with| Annotations
   Extension --> |notifies| NotificationManager
   Extension --> |parses Annotations into a Message| Annotations
   NotificationManager --> |routes Message to| Notifiers
   Notifiers --> |sends Message to| Platform(Messaging Platforms)
   TestFramework --> |runs| Test
   TestFramework --> |is extended by| Extension
   
  subgraph Alerticorn Core
    NotificationManager
    Annotations
  end

  subgraph Alerticorn Extensions
    Extension
   end

subgraph Alerticorn Notifiers
    Notifiers
end
```