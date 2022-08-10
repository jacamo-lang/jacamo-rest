# Demo description

In this demo we run a python program that sends messages to a JaCaMo agent, using JaCaMo-REST API.


# Running JaCaMo application

You can run the JaCaMo application by the following command:

  `./gradlew`


This application has an agent named bob and you can inspect it by:

- [JaCaMo Mind inspector -- html](http://127.0.0.1:3272)
- [JaCaMo REST API -- json](http://127.0.0.1:8080/agents/bob)

# Running python application

Python `request` package should be installed:

  `python -m pip install requests`

Run the python program:

  `python demo.py`


This program prints out the main agent information (in JSON) and then post a *tell* message in the agent mail box. The message is format is JSON:

```
{ "performative": "tell",
  "sender"      : "jomi",
  "receiver"    : "bob",
  "content"     : "vl(10)",
  "msgId"       : "34"}
```

The output of the python program is:

```
{'agent': 'bob', 'type': 'JaCaMoAgent', 'uri': 'http://127.0.0.1:8080/agents/bob', 'inbox': 'http://127.0.0.1:8080/agents/bob/inbox', 'roles': [], 'missions': [], 'workspaces': [{'workspace': '/main/w', 'artifacts': [{'artifact': 'c1', 'type': 'example.Counter'}]}, {'workspace': '/main'}]}
```

In the agent side, you will notice the new belief.

You find more information about the REST API at https://github.com/jacamo-lang/jacamo-rest/tree/master/doc/rest-api.
