ARG JDK_IMAGE

FROM ${JDK_IMAGE}

RUN java --version

COPY sandbox.jar /sandbox.jar

CMD java -jar /sandbox.jar