+++
pre = ""
title = "Enrich the GraphQL API"
weight = 10
tags = ["graphql", "query", "subscription"]
summary = "How to create a new Query, Subscription, or Mutation using the GraphQL API"
+++

It is possible to integrate new queries, mutations, or subscriptions into the GraphQL API.
This augmentation offers a more organized and anticipatable approach for interacting with the simulator,
thanks to the strong typing and the schema definition.

It is possible to use GraphQL APIs for the development of a module inside
Alchemist.
The `GraphQLClient` class will  take care of establishing a connection
to the GraphQL server, validate and compute operations and give back their results.
This component has been  developed in a multiplatform environment, so that applications
can lay on platforms like Android, iOS, Kotlin/JS or Kotlin/Native.

#### Create a new Query, Mutation, or Subscription
In order to create new queries, mutations, or subscriptions, follow these steps:

1. Include the `alchemist-graphql` common source set in your project.
```kotlin
implementation(alchemist("graphql"))
```

2. Define the structure of the data you need when executing one of the provided
   operations, inside a set of files with `.graphql` extension placed in the
   `alchemist-graphql/src/commonMain/resources/graphql/` directory.
    The following is an example of a query that retrieves the contents of a node
    by its ID:
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
   :alchemist-graphql:generateApolloSources` in order to
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
