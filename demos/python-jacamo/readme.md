# Demo description

In this demo we run a python program that interact with a JaCaMo application, using JaCaMo-REST API.


# Running JaCaMo application

You can run the JaCaMo application by the following command:

  `./gradlew`


This application has an *agent* named `bob`.  You can inspect it by:

- [JaCaMo Mind inspector -- html](http://127.0.0.1:3272)
- [JaCaMo REST API -- json](http://127.0.0.1:8080/agents/bob)

This application also has an *artifact* named `c1`.  You can inspect it by:

- [JaCaMo Mind inspector -- html](http://127.0.0.1:3272/agent-mind/bob), since the agent is focusing on the artifact, it has a belief `count` that comes from the artifact.
- [JaCaMo REST API -- json](http://127.0.0.1:8080/workspaces/w/artifacts/c1)


# ACL interaction from Python

In this first example, the python program sends ACL messages (like tell) to `bob`, as if the python program is an agent. If the python program was coded in Jason, the operation would be: `.send(bob,tell,vl(10))`.

Python `request` package should be installed:

  `python -m pip install requests`

To run the python program:

  `python demo-ag.py`


This program prints out the main agent information (in JSON) and then post a *tell* message in the agent mail box endpoint (`http://127.0.0.1:8080/agents/bob/inbox`). The message format is JSON:

```
{ "performative": "tell",
  "sender"      : "jomi",
  "receiver"    : "bob",
  "content"     : "vl(10)",
  "msgId"       : "34
}
```

The output of the python program is:

```
{'agent': 'bob', 'type': 'JaCaMoAgent', 'uri': 'http://127.0.0.1:8080/agents/bob', 'inbox': 'http://127.0.0.1:8080/agents/bob/inbox', 'roles': [], 'missions': [], 'workspaces': [{'workspace': '/main/w', 'artifacts': [{'artifact': 'c1', 'type': 'example.Counter'}]}, {'workspace': '/main'}]}
```

In the agent side, you will notice the new belief `vl(10)`.

You find more information about the REST API at https://github.com/jacamo-lang/jacamo-rest/tree/master/doc/rest-api.


# Artifact interaction from python

In this example, the python program changes an observable property (`count`) of an artifact (`c1`) in a workspace (`w`). The endpoint of this observable property is `http://127.0.0.1:8080/workspaces/w/artifacts/c1/properties/count`.

NB.: to change an observable property, the artifact must have an operation called  `doUpdateObsProperty`, see details at `src/env/example/Counter.java`.


To run the python program:

  `python demo-art.py`

The output of the python program is:

```
{'artifact': 'c1', 'type': 'example.Counter', 'properties': [{'count': ['3']}], 'operations': ['doDefineObsProperty', 'observeProperty', 'inc', 'doUpdateObsProperty'], 'observers': ['bob']}
Count value is  3.0
New count value is  13.0
```

In the agent side, you will notice the change in the belief `count`.
