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

## Use GraphQL service inside Alchemist
In order to attach the GraphQL API service to a simulation, you must specify
the `GraphQLMonitor` in the simulation's YAML file, providing the
server's host and port (if not specified, default URL is: `127.0.0.1:8081`) as
shown in the following example:
```yml
monitors:
  type: GraphQLMonitor
  parameters:
    host: <my-custom-host>
    port: <my-custom-port>
```
Once the YAML file is ready, the simulation can be started as usual.

### Simulation's overview in the Web Browser
Once the simulation is up and running, you can visit on your local web browser
the URL: `http://<my-custom-host>:<my-custom-port>/graphiql`. In this GraphiQL
playground you will be able to lookup all the operations and types structure
that are defined in the GraphQL schema, thanks to the documentation on the
sidebar, or execute operations defining the structure of the data that you will
need.
