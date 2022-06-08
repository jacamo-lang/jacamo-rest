JaCaMo REST provides a REST API to interact with agents, artifacts and organisations.

# Using this project

The simplest way to use JaCaMo-REST is by developing your MAS with JaCaMo >= 1.1, which includes JaCaMo-REST. An example is available at the [JaCaMo demos](https://github.com/jacamo-lang/jacamo/tree/master/demos/rest-comm). Examples focused on *integration* are also found at [demos](https://github.com/jacamo-lang/jacamo/tree/master/demos/integration).

Otherwise, we can include jacamo-rest dependency in `build.gradle`:

```
repositories {
    mavenCentral()

    maven { url "https://raw.githubusercontent.com/jacamo-lang/mvn-repo/master" }

}

dependencies {
    implementation group: 'org.jacamo'     , name: 'jacamo-rest'   , version: '0.7'
}
```

and start the API server in your `.jcm` application file:

```
mas yourmas {

    ...

    // starts rest api on port 8080
    platform: jacamo.rest.JCMRest("--restPort 8080")

}

```


# About jacamo-rest...

* Each agent has REST endpoints to receive messages and be inspected.
* The REST API also implements a name and yellow pages services. `Bob` can send a message to `marcos` using its name.
* Java JAX-RS is used for the API.
* For more information, see the paper: [Towards Jacamo-rest: A Resource-Oriented Abstraction for Managing Multi-Agent Systems](doc/paper.pdf)
* Information for developers and how to contribute can be found at [contributing](doc/contributing.md).

# REST API version 0.5

* Full documentation: [jacamo-rest 0.5](https://app.swaggerhub.com/apis/cleberjamaral/jacamo-rest/0.5)
* All endpoints accepts OPTIONS returning allowed verbs

## Overview

* ``GET /overview``: Returns MAS overview and all links

## Agents

* ``GET; POST /agents``: Retrieves agents collection - works as white pages - (with links); append an agent.
* ``GET; DELETE ../{agentuid}``: Returns agent data (mind, bb and intentions (obs 1); remove.
* ``GET; POST ../{agentuid}/plans`` Retrieves plans of the specified agent; append a plan.
* ``GET ../{agentuid}/log`` Returns log of the specified agent.
* ``POST ../{agentuid}/command`` Posts a new command.
* ``POST ../{agentname}/inbox`` Posts a new message.
* ``GET; POST ../{agentuid}/services`` Returns services provided by the specified agent; add a service to the agent.

(obs 1) /code is only provided in jacamo-web

## Workspaces

* ``GET; POST /workspaces``: Retrieves workspaces collection (with links); append a workspace.
* ``GET; POST ../{workspaceuid}/artifacts`` Retrieves artifacts collection; append an artifact.
* ``GET ../{workspaceuid}/artifacts/{artifactuid}`` Returns artifact data.
* ``GET /workspaces/{workspaceuid}/artifacts/{artifactuid}/properties/{obspropsuid}`` Returns obs props data.
* ``POST /workspaces/{workspaceuid}/artifacts/{artifactuid}/operations/{operationid}/execute``  Execute operation.

## Organisations

* ``GET /organisations``: Retrieves organisations collection.
* ``GET ../{organisationuid}`` Returns organisation data (with links to artifacts).
* ``POST ../{organisationuid}/roles`` append a role.

## Services
* ``GET /services``: Retrieves services collection and agents that provide them - works as yellow pages
