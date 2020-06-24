# To run at hots 1

Run ngrok:

```
ngrok http 8080
```

note the given URL, edit `host1.jcm` and place this host after `--registerURL`.

Run the application of host 1:

```
./gradlew h1
```

# To run at hots 2

Run ngrok:

```
ngrok http 8080
```

note the given URL, edit `host2.jcm` and place this host after `--registerURL`. Place the host given for host1 after `--connect`.

Run the application of host 2:

```
./gradlew h2
```
