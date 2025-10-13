import requests
import json

# get agent main description
api_url = "http://127.0.0.1:8080/agents/bob"
response = requests.get(api_url)
print(response.json())

inbox_url = response.json()['inbox']
#print(inbox_url)

msg     = {"performative":"tell", "sender":"jomi", "receiver":"bob", "content":"vl(10)", "msgId":"34"}
headers = {"Content-Type":"application/json"}
response = requests.post(inbox_url, data=json.dumps(msg), headers=headers)

# prints the agent BB
#print( requests.get(api_url+"/bb").json()["beliefs"])
