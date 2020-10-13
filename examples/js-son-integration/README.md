# Tutorial: Integration jacamo-rest and JS-son
This tutorial explains how to integrate jacamo-rest with the
[JS-son](https://github.com/TimKam/JS-son) JavaScript agent programming library.
As a simple example, we re-implement the
[Jason *room* example](https://github.com/jason-lang/jason/tree/master/examples/room) without the
*paranoid* agent and launch it as a jacamo-rest MAS (note that our implementation differs from the
Jason room example implementation).
We then implement the *paranoid* agent as a Node.js client with JS-son and connect it to the JaCaMo
multi-agent system via jacamo-rest.

## Prerequisites
This tutorial shows how to create Gradle and Node.js projects that manage all dependencies
automatically. The only requirements are to have Java, Gradle, and Node.js installed.

## jacamo-rest
Let us first set up the jacamo-rest project.
We create the folder ``jacamo`` and add a ``build.gradle`` file with the following content (to
specify repositories and dependencies):

```groovy
plugins {
  id 'java'
}

repositories {
    mavenCentral()

    maven { url "https://raw.github.com/jacamo-lang/mvn-repo/master" }
    maven { url "http://jacamo.sourceforge.net/maven2" }

}

dependencies {
    compile 'org.jacamo:jacamo-rest:0.5'
}
```

Then, we specify the multi-agent system in a new file we call ``Room.jcm``:

```groovy
mas room {
    agent porter
    agent claustrophobe
    agent paranoid
    platform: jacamo.rest.JCMRest("--main 2181 --restPort 8080")
}

```

Now, we implement the *porter* and *claustrophobe* agents as ordinary Jason agents:

* ``porter.asl``:

```prolog
start.

+start
  <- .print("Locks door")
  .broadcast(tell, locked(door)).


+!~locked(door)[source(claustrophobe)]
  <- .print("Unlocks door")
  .broadcast(tell, ~locked(door)).

+!~locked(door)[source(paranoid)]
  <- .print("Locks door")
  .broadcast(tell, locked(door)).

```

* ``claustrophobe.asl``:

```prolog
+locked(door)[source(porter)]
  <- .print("Please, unlock the door.");
  -~locked(door)[source(porter)];
  .send(porter,achieve,~locked(door)).

+~locked(door)[source(porter)]
  <-
  -locked(door)[source(porter)];
  .print("Thanks for unlocking the door!").
```

We also create a ``paranoid.asl`` file, in which we need to add some belief revision functionality:

```prolog
+locked(door)[source(porter)]
  <- -~locked(door)[source(porter)].

+~locked(door)[source(porter)]
  <- -locked(door)[source(porter)].
```

The reason for this is that we manage the revision of the paranoid agent's belief about the porter's
most recent announcement of the door lock status centrally. The JS-son agent obtains the belief
by querying the belief base of this agent mock.

Then, we create a Gradle wrapper by running ``gradle wrapper`` and we add a task to our Gradle build
file that starts the MAS:

```groovy
task run (type: JavaExec, dependsOn: 'classes') {
    group      ' JaCaMo'
    description 'Runs the room example'
    doFirst {
        mkdir 'log'
    }
    main 'jacamo.infra.JaCaMoLauncher'
    args 'Room.jcm'
    classpath sourceSets.main.runtimeClasspath
}
```

We can start the jacamo-rest MAS by executing ``./gradlew run``.

## JS-son
Let us implement the JS-son agent.
For this, we create a dedicated ``js-son`` folder. In this folder, we run ``npm init`` to generate a
``package.json`` file with roughly the following properties:

```json
{
  "name": "jacamo-rest-integration-example",
  "version": "0.0.1",
  "description": "Jacamo-rest integration example",
  "main": "index.js",
  "dependencies": {
    "js-son-agent": "^0.0.10"
  },
  "devDependencies": {},
}
```

Then, we create the ``JRClient.js`` file, in which we implement a small client that allows us to interact with jacamo-rest:

```javascript
/* implements a simple client to interact with jacamo-rest */
const http = require('http')

function JRClient(host, port=8080) {
    /* generate request options */
    this.genOptions = function(method, path, data) {
        return {
            host: host,
            port: port,
            path,
            method: method,
            headers:
              method === 'POST' ?
              {
                'Content-Type': 'application/json',
                'Content-Length': data.length
              } : {}
        }
    }
    /* issue request */
    this.send = function(
        method,
        endpoint,
        callback = _ => {},
        data = undefined,
        errorHandler = error => { console.log(error) }
    ) {
        const request = http.request(this.genOptions(method, endpoint, data), callback)
        request.on('error', errorHandler)
        if (data) request.write(data)
        request.end()
    }
}

module.exports = JRClient
```

Now, we create the ``paranoid.js`` file that implements the paranoid agent. The first part of the
agent is roughly just a standard belief-plan JS-son agent:

```javascript
// import JS-son
const { Belief, Plan, Agent } = require('js-son-agent')
const JRClient = require('./JRClient')

const args = process.argv.slice(2)
jrClient = new JRClient(args[0], args[1])

// implement paranoid agent
const beliefs = {
  ...Belief('door', { locked: true }),
  ...Belief('doorPrev', { locked: true }),
  ...Belief('requestCount', 0)
}

const plans = [
  Plan(
    beliefs => !beliefs.door.locked,
    function() {
      console.log('Please, lock the door.')
      this.beliefs.requestCount += 1
      this.beliefs.doorPrev.locked = false
      requestLockDoor()
      return []
    }
  ),
  Plan(
    beliefs => beliefs.door.locked && !beliefs.doorPrev.locked,
    function() {
      console.log('Thanks for locking the door!')
      this.beliefs.doorPrev.locked = true
      return []
    }
  ),
  Plan(
    beliefs => beliefs.requestCount == 10,
    function() {
      console.log('Will you shut up, man?')
      this.beliefs.requestCount = 0
      killClaustrophobe()
      return []
    }
  )
]

const agentId = 'paranoid'
const paranoid = new Agent(agentId, beliefs, {}, plans)
```

Still, it is important to note the following two differences:

1. *Minor difference:* we require the ``JRClient`` class that allows us to interact with
   jacamo-rest, we parse the command line arguments, and we instantiate a client.

2. **Major difference:** the agent's plans return an empty array, because the agent registers its
   actions directly with jacamo-rest. The environment merely handles the belief base update. Note
   that the ``requestLockDoor`` and ``killClaustrophobe`` functions are introduced further below.

We implement the ``requestLockDoor`` function that asks the porter agent to lock the door:

```javascript
function requestLockDoor () {
  const requestData = JSON.stringify(
    {
      performative: 'achieve',
      sender: 'paranoid',
      receiver: 'porter',
      content: 'locked(door)'
    }
  )
  jrClient.send('POST', '/agents/porter/inbox', _ => {}, requestData)
}
```

Similarly, we implement the ``killClaustrophobe`` function:

```javascript
function killClaustrophobe () {
  jrClient.send('DELETE', '/agents/claustrophobe', _ => {})
}
```

Finally, we implement the environment that handles the belief update of the agent in regular
intervals, by querying the belief base of the central mock agent and updating the door lock 
status accordingly:

```javascript
setInterval(() => {
  const beliefUpdate = {}
  const callback = result => {
    let body = ''
    result.on('data', data => { body += data })
    result.on('end', () => {
      const jBody = JSON.parse(body)
      const jBeliefs = jBody.beliefs
      const lastBelief = jBeliefs[jBeliefs.length - 1]
      if(lastBelief.belief === '~locked(door)[source(porter)]') {
        beliefUpdate.door = { locked: false }
      } else {
        beliefUpdate.door = { locked: true }
      }
      paranoid.next(beliefUpdate)
    })
  }
  jrClient.send('GET', '/agents/paranoid', callback)
}, 1000)
```

To run the paranoid agent, we execute ``node paranoid.js <host> <port>``, for example
``npm run start 192.168.0.106 8080``.
Note that initially, the agent will *always* request the porter to close the door: because the
centrally running claustrophobe and porter agents exchange messages much faster, the
paranoid agent never realizes that the porter actually locks the door.
This is why eventually, the paranoid agent simply decides to use jacamo-rest's capabilities
to kill the claustrophobe.
