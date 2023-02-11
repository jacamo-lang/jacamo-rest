# Dockerfile to run JaCaMo applications
# based on JaCaMo SNAPSHOT release

# to build:
#    docker build  -t jomifred/jacamo-runrest .

FROM alpine

ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
ENV PATH $PATH:$JAVA_HOME/bin #:$JACAMO_HOME/scripts

RUN apk add --update --no-cache git gradle openjdk8-jre bash fontconfig ttf-dejavu

# put the app on app/ folder
WORKDIR /app

# download and run jacamo-rest (just to update local maven rep)
RUN git clone https://github.com/jacamo-lang/jacamo-rest.git && \
    cd jacamo-rest && \
    ./gradlew build

EXPOSE 3271
EXPOSE 3272
EXPOSE 3273
EXPOSE 8080

# use app/jacamo-rest as workdir
WORKDIR /app/jacamo-rest

CMD ["./gradlew","run"]
