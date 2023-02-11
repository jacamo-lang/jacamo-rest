import requests
import json

# get artifact description
api_url = "http://127.0.0.1:8080/workspaces/w/artifacts/c1"
response = requests.get(api_url)
print(response.json())

# get the value of an observable property
count_url = "http://127.0.0.1:8080/workspaces/w/artifacts/c1/properties/count"
response = requests.get(count_url)
print("Count value is ",response.json()[0])

# update the value of an observable property
msg     = [ 13 ]
headers = {"Content-Type":"application/json"}
response = requests.post(count_url, data=json.dumps(msg), headers=headers)

response = requests.get(count_url)
print("New count value is ",response.json()[0])
