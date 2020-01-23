ARG java_version

FROM adoptopenjdk/openjdk${java_version}

COPY sandbox.jar /sandbox.jar

EXPOSE 80

CMD java -jar /sandbox.jar