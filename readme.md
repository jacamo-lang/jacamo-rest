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

To run:
* `gradle marcos` runs agent marcos and the REST platform
* `gradle bob` runs agents bob and alice. Bob sends a message to marcos using its rest API.

With docker:
* `docker build  -t jomifred/jacamo-runrest .` to build a docker image
* `docker network create jcm_net`
* `docker run -ti --rm --net jcm_net  --name host1 -v "$(pwd)":/app jomifred/jacamo-runrest gradle marcos` to run marcos.jcm
* `docker run -ti --rm --net jcm_net  --name host2 -v "$(pwd)":/app jomifred/jacamo-runrest gradle bob_d` to run bob.jcm

Notes:
* Each agent has a REST API to receive message and be inspected
* ZooKeeper is used for name service. Bob can send a message to `marcos` using its name. ZooKeeper maps the name to a URL.
* DF service is provided also by ZooKeeper
* Java JAX-RS is used for the API

See ClientTest.java for an example of Java client. It can be run with `gradle test1`.

# REST API

* GET JSON `/agents`:
    returns the list of running agents

* POST `/agents/{agentname}`
    creates a new agent.

* DELETE `/agents/{agentname}`
    Kills the agent.

* GET JSON `/agents/{agentname}/status`
    returns the agent's status (if idle, number of intentions, ....)

* GET XML  `/agents/{agentname}/mind`
    returns the mind state of the agent (beliefs, plans, intentions, ...)

* GET HTML `/agents/{agentname}/mind`
    returns the mind state of the agent

* GET JSON `/agents/{agentname}/mind/bb`
    returns the agent's beliefs as a list of strings

* POST XML `/agents/{agentname}/mb`
    Adds a message in the agent's mailbox. See class Message.java for details of the fields.

* GET TXT `/agents/{agentname}/plans`
    returns the agent's plans. A label can be used as argument:
    `/agents/{agentname}/plans?label=planT`

* POST FORM `/agents/{agentname}/plans`
    upload some plans into the agent's plan library

* GET HTML `/services`
    returns the DF state
