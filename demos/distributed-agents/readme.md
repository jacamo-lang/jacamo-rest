# To run at host 1

you likely need to run some tunneling tool to make your local MAS available in the internet, two possible options:

1. `ngrok http 8080`
2. `ssh -R 80:localhost:8080 serveo.net`

note the given URL, edit `host1.jcm` and place this host after `--registerURL`. It looks like
```
mas h1 {
    agent karlos

    platform: jacamo.rest.JCMRest("--rest-port 8080 --registerURL http://7a72c6d78d1b.ngrok.io")
}
```

Run the MAS application of host 1:

- `./gradlew h1`

# To run at host 2

Again, run the tunneling (assuming the MAS port is 8081)

1. `ngrok http 8081`
2. `ssh -R 80:localhost:8081 serveo.net`

and note the given URL. Edit `host2.jcm` and place this host after `--registerURL` and place the host given for host1 after `--connect`. It looks like
```
mas h2 {
    agent alice

    platform: jacamo.rest.JCMRest("--connect http://7a72c6d78d1b.ngrok.io --registerURL http://localhost:8081 --rest-port 8081")

}
```

Run the MAS application of host 2:

- `./gradlew h2`
