# How to contribute?

* If you find a bug, please, [report this issue](https://github.com/jacamo-lang/jacamo-rest/issues).
* [Pull requests](https://github.com/jacamo-lang/jacamo-rest/pulls) are aso welcome!

# Coding

## Project structure

* **JCMRest** class starts JaCaMo platform inserting **RestAgArch** class, its agent architecture.
* **JCMRest** class configures and starts the web server, in which **RestAppConfig** class registers endpoints.
* Package **implementation** constains REST implementation, the facade of this API.
* Package **mediation** constains intermediary classes that links the facade and JaCaMo, the resolvers of this API.

## Compiling and running using gradle

* Requirements: jdk >= 15
* Considering you have cloned this repository using `$ git clone http://github.com/jacamo-lang/jacamo-rest`
* In the root folder of the project execute on mac/linux `$ ./gradlew run`, on windows `$ gradlew run`.
* To test this project, run ``./gradlew test``. For details, please, see [unit tests](https://github.com/jacamo-lang/jacamo-rest/tree/master/src/test/java/jacamo/rest).

### Other examples using gradle
* `$ ./gradlew marcos` runs agent marcos and the REST platform.
* `$ ./gradlew bob` runs agents bob and alice. Bob sends a message to marcos using its rest API.

## Using a local docker (example of two machines communicating)
```sh
$ docker build  -t jomifred/jacamo-runrest .
$ docker network create jcm_net
$ docker run -ti --rm --net jcm_net  --name host1 -v "$(pwd)":/app jomifred/jacamo-runrest gradle marcos
$ docker run -ti --rm --net jcm_net  --name host2 -v "$(pwd)":/app jomifred/jacamo-runrest gradle bob_d
```
These commands build a docker image and launch marcos and bob projects. Usually super user privileges are necessary.
