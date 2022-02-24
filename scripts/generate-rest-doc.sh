#/bin/sh

./gradlew docSwagger

rm -rf doc/rest-api
mkdir doc/rest-api

docker run --rm -v $(pwd):/opt swagger2markup/swagger2markup convert -i \
   /opt/build/generated/swagger-ui/swagger.json \
   -f /opt/doc/rest-api/swagger \
   -c /opt/scripts/swagger2markup.properties

docker run --rm -it -v $(pwd)/doc/rest-api:/documents asciidoctor/docker-asciidoctor \
    asciidoctor swagger.adoc

cp doc/rest-api/swagger.html readme.html
#/documents/doc/rest-api.adoc
