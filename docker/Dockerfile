ARG java_version

FROM adoptopenjdk/openjdk${java_version}

RUN apt update && apt install -y zip

WORKDIR /
COPY sandbox.jar /sandbox.jar

EXPOSE 8000

CMD pwd && ls /sandbox.jar