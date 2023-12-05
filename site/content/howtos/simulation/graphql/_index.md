+++
title = "Monitor and Control Simulations through GraphQL"
weight = 5
tags = ["api", "graphql"]
summary = "Monitor and Control a Simulation through a set of GraphQL APIs."
+++

A simulation in Alchemist can be monitored and controlled by a set of GraphQL
APIs which provide a standard and flexible way to query the system.

In the following guide, you will learn how to use the API service for having a
simple overview of the running simulation thanks to the playground
[GraphiQL](https://github.com/graphql/graphiql) with the ability to control the
simulation, or to develop an Alchemist sub-module that uses the API service.

## Using GraphQL service inside Alchemist
In order to attach the GraphQL API service to a simulation, you must specify
the `GraphQLSimulationLauncher` in the simulation's YAML file, providing the
server's host and port (if not specified, default URL is: `127.0.0.1:8081`) as
shown in the following example:
```yml
launcher:
  type: GraphQLServerLauncher
  parameters:
    host: <my-custom-host>
    port: <my-custom-port>
```
Once the YAML file is ready, the simulation can be started as usual.

{{% notice warning %}}
Note: At the current state, only one simulation at a time can be launched through the
GraphQL simulation launcher.
{{% /notice %}}

### Simulation's overview in the Web Browser
Once the simulation is up and running, you can visit on your local web browser
the URL: `http://<my-custom-host>:<my-custom-port>/graphiql`. In this GraphiQL
playground you will be able to lookup all the operations and types structure
that are defined in the GraphQL schema, thanks to the documentation on the
sidebar, or execute operations defining the structure of the data that you will
need.

### Using GraphQL APIs inside the JVM
If you plan to use GraphQL APIs for the development of a module inside
Alchemist, you can do so by using the provided `GraphQLClient`. This class will
take care of establishing a connection to the GraphQL server, validate and
compute operations and give back their results. This component has been
developed in a multiplatform environment, so that applications can lay on
platforms like Android, iOS, Kotlin/JS or Kotlin/Native.

In order to use the GraphQL API service, refer to the following steps:

1. Include the `alchemist-graphql` common source set in your project.

2. Define the structure of the data you need when executing one of the provided
   operations, inside a set of files with `.graphql` extension placed in the
   `alchemist-graphql/src/commonMain/resources/graphql/` directory.
```graphql
# NodeQuery.graphql
query Node($id: Int!) {
    nodeById(id: $id) {
        contents {
            entries {
                molecule {
                    name
                }
                concentration
            }
        }
    }
}
```

{{% notice tip "Use GraphiQL to build client's operations files" %}}
You can test the structure of the data that you need inside the GraphiQL
playground, in order to get predicable results when compiling your operations.
GraphiQL also notices you if you are writing incorrect operations with useful
explanation about the errors.
{{% /notice %}}

3. Run the Gradle task `./gradlew
   :alchemist-graphql:generateAlchemist-graphqlApolloSources` in order to
   generate Kotlin's source code that represents the results of operations
   called in previously defined files.

4. Use the defined GraphQL compiled operations on top of the `GraphQLClient`
    to execute them.

```kotlin
val client: GraphQLClient = GraphQLClientFactory.basicClient()
/* `NodeQuery` is the generated code for the
    GraphQL operation defined in the example above */
val node: Node? = client.query(NodeQuery(nodeId = 10)).execute().data
```
