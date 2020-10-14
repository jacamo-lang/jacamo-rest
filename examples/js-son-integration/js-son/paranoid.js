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

/* function that sends a "lock door" request to the jacamo-rest/Jason porter agent */
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

/* function that kills the claustrophobe agent */
function killClaustrophobe () {
  jrClient.send('DELETE', '/agents/claustrophobe', _ => {})
}

/* environment: query the jacamo-rest paranoid agent's belief base in regular intervals and
update the local agent's belief base accordingly */
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

