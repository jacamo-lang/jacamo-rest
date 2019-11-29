JaCaMo REST provides a REST API to interact with agents, artifact and the organisation. Currently, only the agents side is implemented.

# Using this project

Include jacamo-rest dependency in `build.gradle`:

```
repositories {
    mavenCentral()

    maven { url "https://raw.github.com/jacamo-lang/mvn-repo/master" }
    maven { url "http://jacamo.sourceforge.net/maven2" }

}

dependencies {
    compile group: 'org.jacamo', name: 'jacamo-rest', version: '0.2-SNAPSHOT'
}
```

and start the API server in your `.jcm` application file:

```
mas yourmas {

    ...

    // starts rest api on port 8080
    platform: jacamo.rest.JCMRest("--main 2181 --restPort 8080")

}

```

# Running this project

### Using a local gradle
* `$ gradle marcos` runs agent marcos and the REST platform
* `$ gradle bob` runs agents bob and alice. Bob sends a message to marcos using its rest API.

### Using a local docker
```sh
$ docker build  -t jomifred/jacamo-runrest .
$ docker network create jcm_net
$ docker run -ti --rm --net jcm_net  --name host1 -v "$(pwd)":/app jomifred/jacamo-runrest gradle marcos
$ docker run -ti --rm --net jcm_net  --name host2 -v "$(pwd)":/app jomifred/jacamo-runrest gradle bob_d
```
These commands build a docker image and launch marcos and bob projects. Usually super user privileges are necessary.

# More about jacamo-web

* Each agent has a REST API to receive message and be inspected
* ZooKeeper is used for name service. Bob can send a message to `marcos` using its name. ZooKeeper maps the name to a URL.
* DF service is provided also by ZooKeeper
* Java JAX-RS is used for the API

# REST API

* GET JSON `/overview`:
    returns an overview of the system including agents, artefacts and organisations
    
* GET JSON `/agents`:
    returns the list of running agents

* POST `/agents/{agentname}`
    creates a new agent.

* DELETE `/agents/{agentname}`
    Kills the agent.

* GET JSON `/agents/{agentname}/status`
    returns the agent's status (if idle, number of intentions, ....)

* GET JSON `/agents/{agentname}/mind/bb`
    returns the agent's beliefs as a list of strings

* GET JSON `/workspaces`
    returns the list of workspaces of the system

* GET JSON `/oe`
    returns the list of organisations of the system
    
* Full documentation: [jacamo-rest 0.3](https://app.swaggerhub.com/apis/sma-das/jacamo-rest/0.3)
