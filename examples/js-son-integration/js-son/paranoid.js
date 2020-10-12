// import JS-son
const { Belief, Plan, Agent } = require('js-son-agent')
const http = require('http')
const args = process.argv.slice(2)

// implement paranoid agent
const beliefs = {
  ...Belief('door', { locked: true }),
  ...Belief('requestCount', 0)
}

const plans = [
  Plan(
    beliefs => !beliefs.door.locked,
    function() {
      console.log('Please, lock the door.')
      this.beliefs.requestCount += 1
      requestLockDoor()
      return []
    }
  ),
  Plan(
    beliefs => beliefs.door.locked,
    function() {
      console.log('Thanks for locking the door!')
      this.beliefs.requestCount = 0
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

/* http client that listens for updates and sends messages to the porter agent */

const genOptions = (method, path, data) => ({
  host: args[0],
  port: args[1],
  path,
  method: method,
  headers:
    method === 'POST' ?
    {
      'Content-Type': 'application/json',
      'Content-Length': data.length
    } : {}
})

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
  const post = http.request(genOptions('POST', '/agents/porter/inbox', requestData), _ => {})
  post.on('error', error => console.error(error))
  post.write(requestData)
  post.end()
}

/* function that kills the claustrophobe agent */
function killClaustrophobe () {
  const del = http.request(genOptions('DELETE', '/agents/claustrophobe'), () => {})
  del.on('error', error => console.error(error))
  del.end()
}

/* environment: query the jacamo-rest paranoid agent's belief base in regular intervals and
update the local agent's belief base accordingly */
setInterval(() => {
  const beliefUpdate = {}
  const get = http.request(genOptions('GET', '/agents/paranoid'), result => {
    let body = ''
    result.on('data', data => { body += data })
    result.on('end', () => {
      const jBody = JSON.parse(body)
      const beliefs = jBody.beliefs
      const lastBelief = beliefs[beliefs.length - 1]
      if(lastBelief.belief === '~locked(door)[source(porter)]') {
        beliefUpdate.door = { locked: false }
        
      } else {
        beliefUpdate.door = { locked: true }
      }
      const actions = paranoid.next(beliefUpdate)
    })
  })
  get.on('error', error => console.error(error))
  get.end()
}, 1000)

