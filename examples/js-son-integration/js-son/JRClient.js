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
