# Demo description

In this demo we run agents in three machines:
- host1 runs karlos
- host2 runs alice
- host3 runs bob

During the execution
- alice sends messages to karlos that sends massages back
- bob sends a message to alice

The ANS (Agent Name Service) and DF (Directory Facilitator) run at host1

# Running host 1

you likely need to run some tunnelling tool to make your local MAS available in the internet, two possible options:

1. `ngrok http 8080`
2. `ssh -R 80:localhost:8080 serveo.net`

note the given URL, edit `host1.jcm` and place this host after `--registerURL`. It looks like

```
mas h1 {
    agent karlos

    platform: jacamo.rest.JCMRest("--rest-port 8080 --registerURL http://7a72c6d78d1b.ngrok.io")
}
```

Run the MAS application at host 1:

- `./gradlew h1`

The output is something like

```
[JCMRest] JaCaMo Rest API is running on http://127.0.0.1:8080/
    (as http://7a72c6d78d1b.ngrok.io/).
CArtAgO Http Server running on http://127.0.0.1:3273
Jason Http Server running on http://127.0.0.1:3272
```

# Running host 2

Again, run the tunnelling (assuming the MAS port is 8081)

1. `ngrok http 8081`
2. `ssh -R 80:localhost:8081 serveo.net`

and note the given URL. Edit `host2.jcm` and place this host after `--registerURL` and place the host given for host1 after `--connect`. It looks like

```
mas h2 {
    agent alice

    platform: jacamo.rest.JCMRest("--connect http://7a72c6d78d1b.ngrok.io --registerURL https://igitur.serveousercontent.com/ --rest-port 8081")

}
```

Run the MAS application of host 2:

- `./gradlew h2`

The output is something like

```
[JCMRest] JaCaMo Rest API is running on http://127.0.0.1:8081/
    (as https://igitur.serveousercontent.com/),
    connected to http://7a72c6d78d1b.ngrok.io.
CArtAgO Http Server running on http://127.0.0.1:3274
Jason Http Server running on http://127.0.0.1:3275
[alice] hi 9 from karlos
[alice] hi 7 from karlos
[alice] hi 5 from karlos
[alice] hi 3 from karlos
[alice] hi 1 from karlos
```

some messages are also printed at host1:

```
[karlos] hi 10 from alice
[karlos] hi 8 from alice
[karlos] hi 6 from alice
[karlos] hi 4 from alice
[karlos] hi 2 from alice
[karlos] last hi from alice
```


# Running host 3

Run the tunnelling (assuming the MAS port is 8082)

1. `ngrok http 8081`
2. `ssh -R 80:localhost:8082 serveo.net`

and note the given URL. Edit `host3.jcm` and place this host after `--registerURL` and place the host given for host1 after `--connect`. Then run the MAS application at host 3:

- `./gradlew h3`

The output is something like

```
JaCaMo Rest API is running on http://127.0.0.1:8082/
    (as https://insto.serveousercontent.com/),
    connected to http://7a72c6d78d1b.ngrok.io.
CArtAgO Http Server running on http://127.0.0.1:3276
Jason Http Server running on http://127.0.0.1:3277
[bob] hi 199 from alice
```

some messages are also printed in host2:

```
[alice] hi 200 from bob
```
