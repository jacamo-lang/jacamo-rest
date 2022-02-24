#/bin/sh

./gradlew docSwagger

docker run --rm -v $(pwd):/opt swagger2markup/swagger2markup convert -i \
   /opt/build/generated/swagger-ui/swagger.json \
   -f /opt/doc/rest-api \
   -c /opt/scripts/swagger2markup.properties

docker run --rm -it -v $(pwd)/doc:/documents asciidoctor/docker-asciidoctor \
    asciidoctor rest-api.adoc
#/documents/doc/rest-api.adoc
